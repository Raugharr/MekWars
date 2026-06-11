/*
 * MekWars - Copyright (C) 2004
 *
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet) Original author Helge Richter (McWizard)
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */

package mekwars.client;

// This is the Client used for connecting to the master server.
// @Author: Helge Richter (McWizard@gmx.de)

import java.awt.Dimension;
import java.awt.FileDialog;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.JOptionPane;
import megamek.Version;
import megamek.client.ui.swing.GameOptionsDialog;
import megamek.common.CriticalSlot;
import megamek.common.Entity;
import megamek.common.EquipmentType;
import megamek.common.Game;
import megamek.common.Mech;
import megamek.common.event.GameCFREvent;
import megamek.common.event.GameEvent;
import megamek.common.icons.Camouflage;
import megamek.common.options.GameOptions;
import megamek.common.options.IBasicOption;
import megamek.server.Server;
import mekwars.client.campaign.CBMUnit;
import mekwars.client.campaign.CCampaign;
import mekwars.client.campaign.CPlayer;
import mekwars.client.campaign.CUnit;
import mekwars.client.common.campaign.clientutils.GameHost;
import mekwars.client.common.campaign.clientutils.protocol.CConnector;
import mekwars.client.common.campaign.clientutils.protocol.IClient;
import mekwars.client.common.campaign.clientutils.protocol.commands.AckSignonPCmd;
import mekwars.client.common.campaign.clientutils.protocol.commands.CommPCmd;
import mekwars.client.common.campaign.clientutils.protocol.commands.IProtCommand;
import mekwars.client.common.campaign.clientutils.protocol.commands.PingPCmd;
import mekwars.client.common.campaign.clientutils.protocol.commands.PongPCmd;
import mekwars.client.gui.CCommPanel;
import mekwars.client.gui.commands.IGUICommand;
import mekwars.client.gui.commands.MailGCmd;
import mekwars.client.gui.commands.PingGCmd;
import mekwars.client.gui.dialog.ArmyViewerDialog;
import mekwars.client.io.FileSystem;
import mekwars.client.net.hpgnet.HPGClient;
import mekwars.client.protocol.DataFetchClient;
import mekwars.client.sound.SoundManager;
import mekwars.client.util.RepairManagmentThread;
import mekwars.client.util.SalvageManagmentThread;
import mekwars.common.AdvancedTerrain;
import mekwars.common.BMEquipment;
import mekwars.common.CampaignData;
import mekwars.common.Equipment;
import mekwars.common.GameWrapper;
import mekwars.common.House;
import mekwars.common.Influences;
import mekwars.common.MMGame;
import mekwars.common.Planet;
import mekwars.common.PlanetEnvironment;
import mekwars.common.Unit;
import mekwars.common.campaign.Buildings;
import mekwars.common.campaign.UnitRepairCostCalculator;
import mekwars.common.util.GameReport;
import mekwars.common.util.ThreadManager;
import mekwars.common.util.TokenReader;
import mekwars.common.util.UnitUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class MWClient extends GameHost implements IClient {
    private static final Logger LOGGER = LogManager.getLogger(MWClient.class);

    // Holds campaign data as factions and planets..
    private CampaignData data = null;
    private DataFetchClient dataFetcher;

    public static final Version CLIENT_VERSION = new Version("9.0.0"); // change this with

    // all client
    // changes @Torren
    private TimeOutThread TO;
    private Collection<CUser> Users;
    private Server myServer = null;
    private List<ClientThread> mmClientThreads = new ArrayList<>();
    private Vector<IBasicOption> GameOptions = new Vector<IBasicOption>(1, 1);
    private GUIClient guiClient;

    private boolean SignOff = false;
    private SoundManager soundManager;

    private String password = "";
    private int gameCount = 0; // number of games played on a ded
    private long lastResetCheck = System.currentTimeMillis();
    private int dedRestartAt = 50; // number of games played on a ded before auto
    // restart.

    private long TimeOut = 120;
    private long LastPing = 0;

    private PlanetEnvironment currentEnvironment;
    private AdvancedTerrain aTerrain = null;

    private TreeMap<String, String[]> allOps;// all operations, from OpList.txt

    private Dimension MapSize;
    private int mapMedium = 0;

    private Game game = new Game();
    private HPGClient hpgClient; 

    public static final String GUI_PREFIX = "/"; // prefix for commands in GUI

    public static final int IGNORE_PUBLIC = 0;
    public static final int IGNORE_HOUSE = 1;
    public static final int IGNORE_PRIVATE = 2;

    private CCampaign theCampaign;
    private CPlayer myPlayer;

    int LastStatus = STATUS_DISCONNECTED;

    TreeMap<String, IGUICommand> GUICommands = new TreeMap<String, IGUICommand>();

    String LastQuery = ""; // receiver of last mail
    Vector<String> IgnorePublic = new Vector<String>(1, 1); // people whose
    // public messages
    // are ignored
    Vector<String> IgnoreHouse = new Vector<String>(1, 1); // people whose
    // faction messages
    // are ignored
    Vector<String> IgnorePrivate = new Vector<String>(1, 1); // people whose
    // private
    // messages are
    // ignored
    Vector<String> KeyWords = new Vector<String>(1, 1); // words announced with
    // sound

    // Starting edge for players in building ops
    private int playerStartingEdge = Buildings.EDGE_UNKNOWN;

    // Bot commands
    private boolean usingBots = false;
    private boolean botsOnSameTeam = false;

    // Advanced Repair Queue
    private RepairManagmentThread RMT = null;
    private SalvageManagmentThread SMT = null;

    private boolean waitingOnCommand = false;

    private HashMap<String, Equipment> blackMarketEquipmentList = new HashMap<String, Equipment>();

    // Main-Method
    public static void main(String[] args) {
        GUIClientConfig config;
        boolean dedicated = false;
        int i;
        
        LOGGER.info("Starting MekWars Client Version: {}", CLIENT_VERSION);
        try {
            for (i = 0; i < args.length; i++) {
                if (args[i].equalsIgnoreCase("-dedicated")
                        || args[i].equalsIgnoreCase("-d")) {
                    dedicated = true;
                }
            }
            config = new GUIClientConfig(dedicated);
            FileSystem.getInstance().createDirectories();

            /*
             * Config files have been loaded, and command line args have been
             * parsed. Construct the actual client. NOTE: Client constrtuctor
             * attempts to pull the oplist, campaign config and other
             * non-interactive data over the DATAPORT before client.start()
             * attempts to connect to the chat server on the SERVERPORT.
             */
            new MWClient(config);

        } catch (Exception ex) {
            LOGGER.catching(ex);
            System.exit(1);
        }
    }

    
    public MWClient(GUIClientConfig config) {
        ProtCommands = new TreeMap<>();
        Config = config;

        try {
            RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
            LOGGER.info("RT Info: " + rt.getName());
        } catch (Exception ex) {
            LOGGER.catching(ex);
        }
        Connector = new CConnector(this);

        Users = Collections.synchronizedList(new Vector<CUser>(1, 1));
        theCampaign = new CCampaign(this);
        myPlayer = theCampaign.getPlayer();
        createProtCommands();
        createGUICommands();

        hpgClient = new HPGClient(this);
        try {
            String trackerEnabledConfig = getConfigParam("TrackerEnabled");
            if (Boolean.parseBoolean(trackerEnabledConfig)) {
                String trackerAddress = getConfigParam("TrackerAddress");
                hpgClient.connect(new InetSocketAddress(trackerAddress,
                            HPGClient.TRACKER_PORT));
            }
        } catch (IOException e) {
            LOGGER.error("Unable to connect to HPGTracker");
        }

        guiClient = new GUIClient(this, config);
        guiClient.init();
            soundManager = new SoundManager(config);


        myUsername = getConfigParam("NAME");

        dedRestartAt = Integer.parseInt(getConfigParam("DEDAUTORESTART"));
        savedGamesMaxDays = Integer
                .parseInt(getConfigParam("MAXSAVEDGAMEDAYS"));
        myPort = Integer.parseInt(getConfigParam("PORT"));
        IgnorePublic = splitString(Config.getParam("IGNOREPUBLIC"), ",");
        IgnoreHouse = splitString(Config.getParam("IGNOREHOUSE"), ",");
        IgnorePrivate = splitString(Config.getParam("IGNOREPRIVATE"), ",");
        KeyWords = splitString(Config.getParam("KEYWORDS"), ",");

        /*
         * Start the pruge thread when the client starts, not when the host
         * starts. This prevents the creation of multiple threads when the host
         * is restarted, or after disconnections.
         */
        LOGGER.info("Starting pAS");
        PurgeAutoSaves pAS = new PurgeAutoSaves();
        new Thread(pAS).start();

        // start checking for timeouts
        TimeOut = Long.parseLong(Config.getParam("TIMEOUT"));
        LastPing = System.currentTimeMillis() / 1000;
        TO = new TimeOutThread(this);
        TO.run();
    }

    /*
     * Get the GUIClient, will be NULL for dedicated servers.
     */
    public GUIClient getGUIClient() {
        return guiClient;
    }

    public String getPassword() {
        return password;
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    /*
     * NOTE: this list is ancient. sometimes useful. often out of date. List of
     * Abreviations for the protocol used by the client only: NG = New Game
     * (NG|<IP>|<Port>|<MaxPlayers>|<Version>|<Comment>) CG = Close Game (CG) GB
     * = Goodbye (Client exit) (GB) SO = Sign-On (SO|<Version>|<UserName>) Used
     * by Both: CH = Chat Server news:(CH|<text>) Client Chat:
     * (CH|<UserName>|<Color>|<Text>) Used only by the Server: SL|NG = Games
     * (GS|<MMGame.toString()>|<MMGame.toString()|...) SL|CG = close game SL|JG
     * = add a player to game list SL|LG = remove a player from game list SL|SHS
     * = Set Host Status (SHS|<GameID>|<Status>) US = Users
     * (US|<MMClientInfo.toString()>|<MMClientInfo.toString()>|..) UG = User
     * Gone (UG|<MMClientInfo.toString>|[Gone]) Gone is used when the client
     * didn't just change his name NU = New User
     * (NU|<MMClientInfo.toString>|[NEW]) NEW is used the same way as GONE in UG
     * ER = Error (Not yet used) (ER|<ErrorLevel>|<description>) NN = New name
     * (My name Change was successful) CT = Campaign Task Offset (CT|Offset) CS
     * = Campaign Status (CS|Status) GO = Game Options
     * (GO|OPTION1NAME|OPTION1VALUE|OPTION2NAME...) PE = SPlanet Environment
     * (Used to initialize the MM map generator) HS = SHouse Status TI = Tick
     * Info (TI|TIMETILLNEXT) SP = Show PopupWindow SM = Show Miscellaneous
     * (Puts text into Misc Tab)
     */
    @Override
    public synchronized void doParseDataInput(String input) {

        // non-null main frame, unbuffer or just pass through
        if (decodeBuffer.size() > 0) {
            Iterator<String> i = decodeBuffer.iterator();
            while (i.hasNext()) {
                String currS = i.next();
                doParseDataHelper(currS);
                i.remove();
            }
        } else {
            doParseDataHelper(input);
        }
    }

    public String createFilenameChecksum(String filename) throws Exception {
        byte[] b = createChecksum(filename);
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    public byte[] createChecksum(String filename) throws Exception {
        InputStream fis = new FileInputStream(filename);

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;
        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);
        fis.close();
        return complete.digest();
    }

    public void processGUIInput(String input) {
        String s = null;

        if (input.startsWith(GUI_PREFIX)) {
            input = input.substring(GUI_PREFIX.length());
            StringTokenizer ST = new StringTokenizer(input, " #");
            s = ST.nextToken();
            if (s.equalsIgnoreCase("c")) {
                s = "c " + ST.nextToken().toLowerCase();
            }
            IGUICommand command = getGUICommand(s);
            if ((command != null) && command.check(s)) {
                if (!command.execute(input)) {
                    LOGGER.error("COMMAND ERROR: wrong command executed.");
                }
                return;
            }
            // else
            input = CAMPAIGN_PREFIX + input;

            sendChat(input);
            s = "Sent command: " + '"'
                    + input.substring(CAMPAIGN_PREFIX.length()) + '"';
            getGUIClient().addToChat(s, CCommPanel.CHANNEL_PLOG, null);
        }

        else {
            sendChat(input);
            String color = getUser(myUsername).getColor();
            String addon = getUser(myUsername).getAddon();
            addon = addon.isEmpty() ? "" : " [" + addon + "]";
            s = "<font color=\"" + color + "\"><b>" + myUsername + addon
                    + "</b></font><b>:</b> " + input;
            if (Config.isParam("TIMESTAMP")) {
                s = "<font color=\"" + Config.isParam("CHATFONTCOLOR") + "\">"
                        + getShortTime() + "</font>" + s;

            }
            getGUIClient().addToChat(s, CCommPanel.CHANNEL_PLOG, null);
            chatCaptureForBot(myUsername,addon,input); //@salient
        }
    }// end processGUIInput
    
    public boolean decodeCommand(String command) {
        StringTokenizer ST;
        String element;

        ST = new StringTokenizer(command, "|");
        element = TokenReader.readString(ST);
        command = command.substring(3);

        if (element.equals("PS")) {
            if (!getPlayer().setData(command)) {
                getGUIClient().addToChat("Player data load failed!<br>");
                return(false);
            }
            return(true);
        }

        if (element.equals("CC")) { // Campaign Command 
            String commandid = TokenReader.readString(ST);
            if (commandid.equals("AT")) {//incoming attack

                if (getConfig().isParam("ENABLEATTACKSOUND")) {
                    getSoundManager().doPlaySound(getConfigParam("SOUNDONATTACK"));
                }

                getGUIClient().addToChat("<font color=\"red\"><b>Your forces are under attack!</b></font>", CCommPanel.CHANNEL_HMAIL);
                getGUIClient().addToChat("<font color=\"red\"><b>Your forces are under attack!</b></font>", CCommPanel.CHANNEL_PMAIL,"Server");
                if (getConfig().isParam("POPUPONATTACK")) {
                    int opID = TokenReader.readInt(ST);
                    int teams = TokenReader.readInt(ST);
                    new ArmyViewerDialog(this,null,ST,ArmyViewerDialog.AVD_DEFEND,null,null,opID,teams);
                }
            }
            if (commandid.equals("NT")) {//next tick
                int time = TokenReader.readInt(ST);
                boolean decrement = TokenReader.readBoolean(ST);
                processTick(time);

                /*
                 * Decrements tick counters for units without explicit auction
                 * length being sent from the server to save a bit of bandwidth.
                 */
                if (decrement) {
                    for (CBMUnit currUnit : getCampaign().getBlackMarket().values()) {
                        currUnit.decrementSalesTicks();
                    }
                    getGUIClient().refreshGUI(GUIClient.REFRESH_BMPANEL);
                }
            }
            return (true);
        }
        if (element.equals("CA")) {
            if (!setData(command)) {
                getGUIClient().addToChat("<b>Error: Campaign data load failed.</b><br>");
                return(false);
            }
            return(true);
        }
        if (element.equals("PL")) {
            if (!getPlayer().decodeCommand(command)) {
                getGUIClient().addToChat("<b>Error: Player data load failed.</b><br>");
                return(false);
            }
            return(true);
        }
        if (element.equals("MS")) {
            if (!getCampaign().showMsg(command)) {
                getGUIClient().addToChat("<b>Error: Message show failed.</b><br>");
                return(false);
            }
            return(true);
        }
        if (element.equals("ST")) {
            if (!getCampaign().showStatus(command)) {
                getGUIClient().addToChat("<b>Error: Status show failed.</b><br>");
                return(false);
            }
            return(true);
        }

        getGUIClient().addToChat("<b>Error: Wrong campaign command from server.</b><br>");
        return(false);
    }

    protected void createGUICommands() {
        addGUICommand(new PingGCmd(this));
        addGUICommand(new MailGCmd(this));
    }

    protected void addGUICommand(IGUICommand command) {
        GUICommands.put(command.getName(), command);
        if (command.isAlias()) {
            GUICommands.put(command.getAlias(), command);
        }
    }

    IGUICommand getGUICommand(String command) {
        return GUICommands.get(command);
    }

    protected void createProtCommands() {
        addProtCommand(new CommPCmd(this));
        addProtCommand(new PingPCmd(this));
        addProtCommand(new PongPCmd(this));
        addProtCommand(new AckSignonPCmd(this));
    }

    protected void addProtCommand(IProtCommand command) {
        ProtCommands.put(command.getName(), command);
    }

    IProtCommand getProtCommand(String command) {
        return ProtCommands.get(command);
    }

    public CCampaign getCampaign() {
        return theCampaign;
    }

    public CPlayer getPlayer() {
        return myPlayer;
    }

    public String getLastQuery() {
        return LastQuery;
    }

    public void setLastQuery(String name) {
        LastQuery = name;
    }

    public void setLastPing(long lastping) {
        LastPing = lastping;
    }

    /**
     * Connect the DataFetcher to the server and get all relevent information from it.
     */
    public void connectDataFetcher() {
        try {
            FileSystem.getInstance().setConfigDir(Config);
        } catch (IOException exception) {
            LOGGER.error("Unable to set config directory", exception);
            JOptionPane.showMessageDialog(
                null,
                MessageFormat.format(
                    guiClient.getResourceString("errors.badConfigDir.text"),
                    Config
                ),
                guiClient.getResourceString("errors.header.text"),
                JOptionPane.ERROR_MESSAGE
            );
            System.exit(0);
        }
        dataFetcher = new DataFetchClient(Integer.parseInt(Config
            .getParam("DATAPORT")), Integer.parseInt(Config
            .getParam("SOCKETTIMEOUTDELAY")));

        try {
            BufferedReader dis = new BufferedReader(new InputStreamReader(
                new FileInputStream(FileSystem.getInstance().getDataLastUpdated().toString()
            )));
            Date lastTS = new Date(Long.parseLong(dis.readLine()));
            dataFetcher.setLastTimestamp(lastTS);
            dis.close();
        } catch (Exception exception) {
            LOGGER.warn("Couldn't read timestamp of last datafetch. Will need to fetch all planetchanges since last full update.", exception);
        }
        // Start the data fetcher, get ops/map/etc

        dataFetcher.setData(Config.getParam("SERVERIP"), FileSystem.getInstance().getConfigDir().toString());
        /*
         * Now that the data fetcher has been created, get the OpList.txt.
         * Note that this is BEFORE map data and other fetch/checks, because
         * the Ops absolutely must be available in order to contruct the
         * GUI.
         */
        try {
            dataFetcher.checkForMostRecentOpList();
        } catch (IOException e) {
            Object[] options = {
                guiClient.getResourceString("options.exit.text"),
                guiClient.getResourceString("options.continue.text"),
            };
            int selectedValue = JOptionPane
                    .showOptionDialog(
                            null,
                            guiClient.getResourceString("errors.header.text"),
                            guiClient.getResourceString("errors.noOpList.text"),
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.ERROR_MESSAGE, null, options,
                            options[0]);
            if (selectedValue == 0) {
                System.exit(0); // exit, if they so choose
            }
        }
    }

    public Vector<String> getIgnorePublic() {
        return IgnorePublic;
    }

    public Vector<String> getIgnoreHouse() {
        return IgnoreHouse;
    }

    public Vector<String> getIgnorePrivate() {
        return IgnorePrivate;
    }

    public RepairManagmentThread getRMT() {
        return RMT;
    }

    public SalvageManagmentThread getSMT() {
        return SMT;
    }

    public Vector<String> getIgnored(int type) {
        if (type == IGNORE_PUBLIC) {
            return IgnorePublic;
        }
        if (type == IGNORE_HOUSE) {
            return IgnoreHouse;
        }
        if (type == IGNORE_PRIVATE) {
            return IgnorePrivate;
        }
        return (new Vector<String>(1, 1));
    }

    public boolean isIgnored(String name, int type) {

        // Do not ignore the staff.
        if (getUser(name.trim()).getUserlevel() >= 100) {
            return false;
        }

        // return true if non-mod is ignored
        for (String next : getIgnored(type)) {
            if (name.trim().equalsIgnoreCase(next.trim())) {
                return true;
            }
        }

        // otherwise, return false
        return false;
    }

    public boolean hasKeyWords(String input) {
        for (String currS : KeyWords) {
            if (input.toLowerCase().indexOf(currS.toLowerCase()) > -1) {
                return true;
            }
        }
        return (false);
    }

    public void setPlayerStartingEdge(int edge) {
        playerStartingEdge = edge;
    }

    public int getPlayerStartingEdge() {
        return playerStartingEdge;
    }

    public String getShortTime() {
        mytime = new Date();
        StringTokenizer s = new StringTokenizer(mytime.toString());
        s.nextElement();
        s.nextElement();
        s.nextElement();
        String t = (String) s.nextElement();
        s = new StringTokenizer(t, ":");
        String result = "[" + s.nextElement() + ":" + s.nextElement() + "] ";
        return result;
    }

    protected Vector<String> splitString(String string, String splitter) {
        Vector<String> vector = new Vector<String>(1, 1);
        String[] splitted = string.split(splitter);
        for (String element : splitted) {
            vector.add(element.trim());
        }

        /*
         * Remove empty entries from the set. Strip ",," and "" from the vector.
         * Helps with ignore and keyword lists.
         */
        Iterator<String> i = vector.iterator();
        while (i.hasNext()) {
            String currString = i.next();
            if (currString.trim().length() == 0) {
                i.remove();
            }
        }

        return vector;
    }

    public synchronized CUser getUser(String name) {

        for (CUser currUser : Users) {
            if (currUser.getName().equalsIgnoreCase(name)) {
                return currUser;
            }
        }
        CUser dummyUser = new CUser();
        dummyUser.setColor(Config.getParam("CHATFONTCOLOR"));
        return dummyUser;
    }

    public synchronized ArrayList<String> getPartialUser(String u) {

        String result = "";
        TreeSet<String> userNames = new TreeSet<String>();

        // there are spaces in the text so get the last word
        if (u.trim().indexOf(" ") != -1) {
            result = u.substring(u.trim().lastIndexOf(" ")).trim();
            u = u.substring(0, u.trim().lastIndexOf(" ")).trim();
        } else {// The name is the first word.
            result = u.trim();
            u = "";
        }

        if (result.length() < 1) {
            return null;
        }

        int myLevel = getUser(getPlayer().getName()).getUserlevel();
        for (CUser usr : Users) {
            if (usr.getName().toLowerCase().startsWith(result.toLowerCase())
                    && (!usr.isInvis() || (usr.isInvis() && (myLevel >= usr
                            .getUserlevel())))) {
                userNames.add(usr.getName());
            }
        }

        // We have a sorted tree set. Convert to an ArrayList so we can work
        // with them more easily.
        ArrayList<String> test = new ArrayList<String>();
        test.addAll(userNames);
        return test;
    }

    public synchronized void clearUserCampaignData() {
        for (CUser currUser : Users) {
            currUser.clearCampaignData();
        }
    }

    /**
     * Method which parses OpList.txt in order to set up a tree which conatins
     * names (as keys) and information (as values) for all game types. Various
     * portions of the GUI code use this tree to properly draw themselves. Kept
     * in MWClient in order to be universally available; however, this is poor
     * design ... *sigh*
     */
    public void setupAllOps() {
        allOps = new TreeMap<String, String[]>();
        try {
            Path opListPath = FileSystem.getInstance().getOpList();
            if (!Files.exists(opListPath)) {
                LOGGER.error("OpList.txt does not exist.");
                return;
            }

            InputStream in = new BufferedInputStream(Files.newInputStream(FileSystem.getInstance().getOpList()));
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            // skip past the first line - its just a timestamp.
            String currLine = br.readLine();
            if (currLine != null) {
                currLine = br.readLine();
            }

            while (currLine != null) {

                // if there's a hanging line, move to next.
                if (currLine.trim().length() == 0) {
                    currLine = br.readLine();
                    continue;
                }

                // set up tokenizer and make tree entry
                StringTokenizer st = new StringTokenizer(currLine, "*");

                String name = st.nextToken();// key
                String range = st.nextToken();
                String color = st.nextToken();
                String hasLong = st.nextToken();// int, not a boolean
                String facInfo = st.nextToken();// "all", "only", "none"
                String homeInfo = st.nextToken();// "all", "only", "none"
                String launchOn = st.nextToken();// int, percentage
                String launchFrom = st.nextToken();// int, percentage
                String minOwn = st.nextToken();// int, percentage
                String maxOwn = st.nextToken();// int, percentage
                String reserveOnly = st.nextToken();// boolean
                String activeOnly = st.nextToken();// boolean

                String legalDefenders = st.nextToken();
                String allowPlanetFlags = st.nextToken();
                String disallowPlanetFlags = st.nextToken();
                String minAccessLevel = st.nextToken();// int
                String minOwnIBD = st.nextToken(); // boolean

                // TODO: Replace explicit numerical references with static ints.
                String[] props = {// value bag
                range,// 0
                        color,// 1
                        hasLong,// 2
                        facInfo,// 3
                        homeInfo,// 4
                        launchOn,// 5
                        launchFrom,// 6
                        minOwn,// 7
                        maxOwn,// 8
                        legalDefenders,// 9
                        allowPlanetFlags,// 10
                        disallowPlanetFlags,// 11
                        reserveOnly,// 12
                        activeOnly,// 13
                        minAccessLevel, // 14
                        minOwnIBD // 15
                };
                allOps.put(name, props);

                // load next line
                currLine = br.readLine();

            }// end while (lines remain to tokenize)

            br.close();
            in.close();

        } catch (Exception e) {
            LOGGER.error("Error in setupAllOps()");
            LOGGER.catching(e);
        }
    }// end setupAllOps

    /**
     * Method which returns the master list of Operations (as assembled from
     * OpList.txt) for use in display code.
     */
    public TreeMap<String, String[]> getAllOps() {
        return allOps;
    }

    public void setUsingBots(Boolean using) {
        usingBots = using;
    }

    public boolean isUsingBots() {
        return usingBots;
    }

    public void setBotsOnSameTeam(Boolean sameTeam) {
        botsOnSameTeam = sameTeam;
    }

    public boolean isBotsOnSameTeam() {
        return botsOnSameTeam;
    }

    public synchronized Collection<CUser> getUsers() {
        return Users;
    }

    public String getProtocolVersion() {
        return "4";
    }

    public void setUsername(String s) {
        myUsername = s.trim();
    }

    public void setPassword(String s) {
        password = s;
    }

    public GUIClientConfig getConfig() {
        return (GUIClientConfig) Config;
    }

    public void setConfig() {
        Config = new GUIClientConfig(false);
    }

    public String getConfigParam(String p) {
        String tparam = "";

        if (p.endsWith(":")) {
            p = p.substring(0, p.lastIndexOf(":"));
        }
        if (p.equals("NAME") && !(myUsername.isEmpty())) {
            return myUsername;
        }
        if (p.equals("NAMEPASSWORD") && !password.isEmpty()) {
            return password;
        }

        tparam = Config.getParam(p);
        if (tparam == null) {
            LOGGER.error("Unable to find param {}", p);
            tparam = "";
        }

        return (tparam);
    }

    // IClient interface
    @Override
    public void systemMessage(String message) {

        String sysColour = getConfigParam("SYSMESSAGECOLOR");
        message = "<font color=\"" + sysColour + "\"><b>" + message
                + "</b></font>";

        getGUIClient().addToChat(message, CCommPanel.CHANNEL_SLOG);
        if (Config.isParam("MAINCHANNELSM")) {
            getGUIClient().addToChat(message);
        }
    }

    @Override
    public void errorMessage(String message) {
        JOptionPane.showMessageDialog(getGUIClient().getMainFrame(), message);
    }

    @Override
    public void processIncoming(String incoming) {
        IProtCommand pcommand = null;

        if (incoming.startsWith(IClient.PROTOCOL_PREFIX)) {
            incoming = incoming.substring(IClient.PROTOCOL_PREFIX.length());
            StringTokenizer ST = new StringTokenizer(incoming,
                    PROTOCOL_DELIMITER);
            String s = ST.nextToken();
            pcommand = getProtCommand(s);
            if ((pcommand != null) && pcommand.check(s)) {
                if (!pcommand.execute(incoming)) {
                    LOGGER.error("Wrong protocol command '{}' executed or execution failed.", incoming);
                }
                return;
            }
            if (pcommand == null) {
                if (incoming.equalsIgnoreCase("denied	/denied")) {
                    // let them know it's a wrong password
                    JOptionPane.showMessageDialog(getGUIClient().getMainFrame(),
                            "Unknown Username/Password combination.");
                } else {
                    LOGGER.error("COMMAND ERROR: unknown protocol command '{}' from server.", incoming);
                }
                return;
            }
        } else {
            LOGGER.error("Received protocol command '{}' without protocol prefix.", incoming);
            return;
        }
    }

    @Override
    public void connectionLost() {
        setStatus(STATUS_DISCONNECTED);
        if (SignOff) {
            return;
        }

        errorMessage("Connection lost.");
        Users.clear();
        getGUIClient().getMainFrame().changeStatus(getStatus(), LastStatus);
    }

    @Override
    public void connectionEstablished() {

        LastPing = System.currentTimeMillis() / 1000;
        LOGGER.info("Connected. Signing on.");

        String VersionSubID = new java.rmi.dgc.VMID().toString();
        StringTokenizer ST = new StringTokenizer(VersionSubID, ":");

        /*
         * If password is blank, send a filler password instead of an empty
         * token. This prevents the no-password "whitescreen" error. HACKY. It
         * would be probably be better to actually fix the server SignOn so an
         * empty password creates a nobody, but this does the trick ...
         */
        String passToSend = getConfigParam("NAMEPASSWORD");
        if ((passToSend == null) || (passToSend.length() == 0)) {
            passToSend = "1337";
        }

        Connector.send(IClient.PROTOCOL_PREFIX + "signon\t" + getConfigParam("NAME")
                + "\t" + passToSend + "\t" + getProtocolVersion() + "\t"
                + Config.getParam("COLOR") + "\t" + CLIENT_VERSION + "\t"
                + ST.nextToken());
        setStatus(STATUS_LOGGEDOUT);
        getGUIClient().getMainFrame().changeStatus(getStatus(), LastStatus);
    }

    // IClient interface
    public void connectToServer() {
        connectToServer(Config.getParam("SERVERIP"),
                Config.getIntParam("SERVERPORT"));
    }

    public void connectToServer(String ip, int port) {
        if ((myUsername == null) || myUsername.isEmpty()) {
            errorMessage("Username not set.");
            return;
        }
        // connect to specific ip and port
        // System exits from connector on failure.
        Connector.connect(ip, port);
        /*
         * Send client version and saved mail request to the server. Doing
         * this after the main frame is build and visible will (I hope) fix
         * the "PM Ping Crash" TT users have with Client 0.1.44.5.
         */
        sendChat(GameHost.CAMPAIGN_PREFIX + "c setclientversion#"
                + getUsername().trim() + "#" + MWClient.CLIENT_VERSION);
        sendChat("/getsavedmail");
    }

    public void goodbye() {
        SignOff = true;
        if ((getStatus() > STATUS_LOGGEDOUT)) {
            getConfig().setParam(
                    "PANELDIVIDER",
                    Integer.toString(getGUIClient().getMainFrame().getMainPanel()
                            .getTabSPane().getDividerLocation()));
            getConfig().setParam(
                    "VERTICALDIVIDER",
                    Integer.toString(getGUIClient().getMainFrame().getMainPanel()
                            .getMainSPane().getDividerLocation()));
            getConfig().setParam(
                    "PLAYERPANELDIVIDER",
                    Integer.toString(getGUIClient().getMainFrame().getMainPanel()
                            .getSideSPane().getDividerLocation()));
            getConfig().setParam("WINDOWSTATE",
                    Integer.toString(getGUIClient().getMainFrame().getExtendedState()));
            getConfig().setParam("WINDOWHEIGHT",
                    Integer.toString(getGUIClient().getMainFrame().getHeight()));
            getConfig().setParam("WINDOWWIDTH",
                    Integer.toString(getGUIClient().getMainFrame().getWidth()));
            getConfig().setParam("WINDOWLEFT",
                    Integer.toString(getGUIClient().getMainFrame().getX()));
            getConfig().setParam("WINDOWTOP",
                    Integer.toString(getGUIClient().getMainFrame().getY()));
            getConfig().saveConfig();
        }
        if (getStatus() != STATUS_DISCONNECTED) {
            // serverSend("GB");
            Connector.send(IClient.PROTOCOL_PREFIX + "signoff");
            dataFetcher.closeDataConnection();
            Connector.closeConnection();
        }

        if (getConfig().isParam("ENABLEEXITCLIENTSOUND")) {
            getSoundManager().doPlaySound(getConfigParam("SOUNDONEXITCLIENT"), false);
        }
    }

    @Override
    public void loadSavegame() {
        FileDialog f = new FileDialog(getGUIClient().getMainFrame(), "Load Savegame");
        f.setDirectory(System.getProperty("user.dir") + "/savegames");
        f.setVisible(true);
        myServer.loadGame(new File(f.getDirectory(), f.getFile()));
    }


    @Override
    public void startHost(boolean dedicated, boolean deploy,
            boolean loadSavegame) {

        List<Unit> meks;
        List<CUnit> autoArmy;
        
        try {
            super.startHost(dedicated, deploy, loadSavegame);
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(null, "Unable to start host " + exception.getMessage());
            LOGGER.error("Unable to start host", exception);
            return;
        }

        if (deploy) {
            meks = myPlayer.getLockedUnits();
            autoArmy = myPlayer.getAutoArmy();
        } else {
            meks = new ArrayList<Unit>();
            autoArmy = new ArrayList<CUnit>();
        }
        LOGGER.info("Joining own game!");

        ClientThread MMGameThread = new ClientThread(myUsername,
                myUsername, "127.0.0.1", myPort, this, meks, autoArmy);
        mmClientThreads.add(MMGameThread);
        ThreadManager.getInstance().runInThreadFromPool(MMGameThread);
        serverSend("JG|" + myUsername);
    }

    public boolean loadGame(String filename) {// load saved game
        if ((myServer != null) && (filename != null) && !filename.isEmpty()) {
            boolean loaded = myServer.loadGame(new File("./savegames/",
                    filename));
            ((Game) myServer.getGame()).addGameListener(this);
            return loaded;
        }

        // else (null server/filename)
        if (myServer == null) {
            LOGGER.error("MyServer == NULL!");
        }
        if (filename == null) {
            LOGGER.error("Filename == NULL!");
        } else if (filename.equals("")) {
            LOGGER.error("Filename == \"\"!");
        }

        return false;
    }

    public boolean loadGameWithFullPath(String filename) {// load saved game
        if ((myServer != null) && (filename != null) && !filename.isEmpty()) {
            boolean loaded = myServer.loadGame(new File(filename));
            ((Game)myServer.getGame()).addGameListener(this);
            return loaded;

        }

        // else (null server/filename)
        if (myServer == null) {
            LOGGER.error("MyServer == NULL!");
        }
        if (filename == null) {
            LOGGER.error("Filename == NULL!");
        } else if (filename.isEmpty()) {
            LOGGER.error("Filename == \"\"!");
        }

        return false;
    }

    public boolean isServerRunning() {
        return myServer != null;
    }

    public void startClient(String hostName, boolean deploy) {
        List<Unit> meks = new ArrayList<Unit>();
        List<CUnit> autoArmy = new ArrayList<CUnit>();

        // If a row is selected
        if ((servers.size() > 0) && (hostName != null)
                && (hostName.trim().length() > 0)) {

            // get server from tree
            MMGame toJoin = servers.get(hostName);

            // allow people to re-enter games they're in
            if ((toJoin.getCurrentPlayers().size() >= toJoin.getMaxPlayers())
                    && !toJoin.getCurrentPlayers().contains(myUsername)
                    && !isMod()) {
                getGUIClient().showInfoWindow("This game is already full");
                return;
            }

            String serverip = toJoin.getIp();
            int serverport = toJoin.getPort();

            // if player is joining his OWN host, use loopback
            if (myUsername.equalsIgnoreCase(toJoin.getHostName())) {
                serverip = "localhost";
            }

            if (deploy) {
                meks = myPlayer.getLockedUnits();
                autoArmy = myPlayer.getAutoArmy();
            }

            ClientThread tmpThread = new ClientThread(Config.getParam("NAME"),
                    hostName, serverip, serverport, this, meks, autoArmy);
            mmClientThreads.add(tmpThread);
            ThreadManager.getInstance().runInThreadFromPool(tmpThread);
            serverSend("JG|" + toJoin.getHostName());
            toJoin = null;
        } else {
            getGUIClient().showInfoWindow("You have to select a game!");
        }
    }

    public void closingGame(String hostName) {
        // update battles tab for all players, via server
        LOGGER.info("Leaving " + hostName);
        serverSend("LG|" + hostName);

        System.gc();
    }

    public Vector<IBasicOption> getGameOptions() {
        return GameOptions;
    }

    public Dimension getMapSize() {
        return MapSize;
    }

    public int getMapMedium() {
        return mapMedium;
    }

    public PlanetEnvironment getCurrentEnvironment() {
        return currentEnvironment;
    }

    public AdvancedTerrain getCurrentAdvancedTerrain() {
        return aTerrain;
    }

    public void getBlackMarketSettings() {
        try {
            dataFetcher.getBlackMarketSettings(this);
        } catch (Exception ex) {
            LOGGER.catching(ex);
        }
    }

    protected class TimeOutThread extends Thread {
        MWClient mwclient;

        public TimeOutThread(MWClient client) {
            mwclient = client;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(TimeOut * 100);
                } catch (Exception ex) {
                    LOGGER.catching(ex);
                }
                if (getStatus() != MWClient.STATUS_DISCONNECTED) {
                    long timeout = (System.currentTimeMillis() / 1000)
                            - LastPing;
                    if (timeout > TimeOut) {
                        systemMessage("Ping timeout (" + timeout + " s)");
                        Connector.closeConnection();
                    }
                } else {
                    LastPing = System.currentTimeMillis() / 1000;
                }
            }
        }
    }

    /**
     * Reloads new planet data from the server. This will be done asynchron, so
     * you may have to wait a bit ;-)
     */
    synchronized public void refreshData() {

        // if the map isnt visible, skip the refresh. waste of bandwidth.
        if (!getConfig().isParam("MAPTABVISIBLE")) {
            LOGGER.info("Map visibility disabled. Skipping map data fetch!");
            return;
        }

        if (!dataFetcher.getPlanetsUpdate(data)) {
            // LOGGER.info("MD5 does not match! Retrieve all
            // planet data again.");
            LOGGER.error("MD5 does not match! But the md5 seems broken anyway...");
            /*
             * try { data = dataFetcher.getAllData(); } catch (IOException e) {
             * MMClient.LOGGER.error("Exception: ", e);
             * JOptionPane.showMessageDialog(null,
             * "The map data could not be retrieved. The map will be disabled.\nTry again later."
             * ); getGUIClient().getMainFrame().getMainPanel().getMapPanel().setEnabled(false);
             * }
             */
        }
        // refresh the changed set... if more than one place want
        // to know about, maybe a listener system would be better..
        Map<Integer, Influences> changesSinceLastRefresh = dataFetcher
                .getChangesSinceLastRefresh();
        if (getGUIClient().getMainFrame() != null) {
            getGUIClient().getMainFrame().getMainPanel().getMapPanel().getMap()
                    .dataFetched(changesSinceLastRefresh);
        }
        LOGGER.info("update for new planet data finished");
    }

    public Map<Integer, Influences> getChangesSinceLastRefresh() {
        return dataFetcher.getChangesSinceLastRefresh();
    }

    /**
     * @author jtighe Reload all the server data back into the client. Used when
     *         the client runs a command that changes the server's data (ie -
     *         admin makes map change).
     */
    public void reloadData() {
        try {
            data = dataFetcher.getAllData();
        } catch (Exception ex) {
            if (!(ex instanceof SocketException)) {
                LOGGER.catching(ex);
            }
        }
        try {
            dataFetcher.getServerConfigData(this);
        } catch (IOException e1) {
            LOGGER.catching(e1);
        }
    }

    public void getServerConfigData() {
        try {
            dataFetcher.getServerConfigData(this);
        } catch (Exception ex) {
            if (!(ex instanceof SocketException)) {
                LOGGER.catching(ex);
            }
        }
    }

    public void loadServerCommmands() {
        try {
            dataFetcher.getAccessLevels(getData());
        } catch (Exception ex) {
            if (!(ex instanceof SocketException)) {
                LOGGER.error("Error loading Server Commands files");
                LOGGER.catching(ex);
            }
        }
    }

    public void loadServerTraitFiles() {
        try {
            dataFetcher.getServerTraitFiles();
        } catch (Exception ex) {
            if (!(ex instanceof SocketException)) {

                LOGGER.error("Error loading Server Trait files");
                LOGGER.catching(ex);
            }
        }
    }

    public void loadBannedAmmo() {
        try {
            dataFetcher.getBannedAmmoData(this);
        } catch (Exception ex) {
            if (!(ex instanceof SocketException)) {
                LOGGER.error("Error loading Server banned ammo file");
                LOGGER.catching(ex);
            }
        }
    }

    /**
     * @return Returns the data.
     */
    public CampaignData getData() {
        if ((data == null)) {

            // Lets reload everything from the cache and then pull down and
            // planet changes
            // If a majory campaign change has happened i.e. new terrains/houses
            // then the clients will be able to use the refresh all command via
            // CMainFrame --Torren
            try {
                LOGGER.info("try to import the planetcache");
                // sanity check
                dataFetcher.checkServerVersion(this);
                // data = dataFetcher.getAllData();

                data = dataFetcher.getCacheData(FileSystem.getInstance().getConfigDir().toString());
                if ((data == null) || (data.getAllPlanets().size() == 0)
                        || (data.getAllHouses().size() == 0)) {
                    throw new Exception("data still empty");
                }
                refreshData();
                dataFetcher.setLastTimestamp(new Date(System.currentTimeMillis()));
                dataFetcher.store();
                LOGGER.info("cache data loaded");
                // Lets start the repair thread
            } catch (Throwable e) {

                if (!(e instanceof FileNotFoundException)) {
                    LOGGER.catching((Exception) e);
                }
                LOGGER.info("need to fetch all planet data..");
                try {
                    data = dataFetcher.getAllData();
                    dataFetcher.store();
                } catch (ConnectException e1) {
                    LOGGER.catching(e1);
                    Object[] options = { "Exit", "Continue" };
                    int selectedValue = JOptionPane.showOptionDialog(null,
                            "Could not connect to server to fetch map data.",
                            "Connection error!", JOptionPane.DEFAULT_OPTION,
                            JOptionPane.ERROR_MESSAGE, null, options,
                            options[0]);
                    if (selectedValue == 0) {
                        System.exit(0);
                    }
                } catch (IOException e1) {
                    LOGGER.catching(e1);
                    JOptionPane
                            .showMessageDialog(null,
                                    "Server is busy while fetching planet data.\nTry again later.");
                } catch (Throwable e1) {
                    LOGGER.catching((Exception) e1);
                    Object[] options = { "Exit", "Continue" };
                    int selectedValue = JOptionPane
                            .showOptionDialog(
                                    null,
                                    "Unknown error while fetching map data. Please\n"
                                            + "report this bug, and keep your error logs handy.",
                                    "Unknown error!",
                                    JOptionPane.DEFAULT_OPTION,
                                    JOptionPane.ERROR_MESSAGE, null, options,
                                    options[0]);
                    if (selectedValue == 0) {
                        System.exit(0);// exit, if they so choose
                    }
                }
            }

            try {
                dataFetcher.getServerConfigData(this);
                if (Boolean.parseBoolean(getServerConfigs("UseAdvanceRepair"))) {
                    RMT = new RepairManagmentThread(
                            Long.parseLong(getServerConfigs("TimeForEachRepairPoint")) * 1000,
                            this);
                    RMT.start();
                }
                if (Boolean.parseBoolean(getServerConfigs("UsePartsRepair"))) {
                    SMT = new SalvageManagmentThread(
                            Long.parseLong(getServerConfigs("TimeForEachRepairPoint")) * 1000,
                            this);
                    SMT.start();
                }
            } catch (Exception ex) {
                LOGGER.error("Unable to fetch Server configs.");
                LOGGER.catching(ex);
            }

            try {
                dataFetcher.getBannedAmmoData(this);
            } catch (Exception ex) {
                LOGGER.error("Unable to fetch server banned ammo data.");
                LOGGER.catching(ex);
            }

            // close the connection.a
            dataFetcher.closeDataConnection();
        }

        return data;
    }

    public double getAmmoCost(String ammo) {
        EquipmentType eq = EquipmentType.get(ammo);


        if (eq == null) {
            return -1;
        }

        if (!getCampaign().getBlackMarketParts().containsKey(
                eq.getInternalName())) {
            return -1;
        }

        if (getCampaign().getBlackMarketParts().get(eq.getInternalName())
                .getCost() > 0) {
            return getCampaign().getBlackMarketParts()
                    .get(eq.getInternalName()).getCost();
        }

        return -1.0;
    }

    /**
     * Does things when a tick is arrived.
     */
    public void processTick(int time) {
        // set tick counter
        getGUIClient().getMainFrame().getMainPanel().getPlayerPanel()
                .setNextTick(System.currentTimeMillis() + time);
        getGUIClient().getMainFrame().getMainPanel().getMapPanel().getMap().processTick();
        System.gc(); // Decicded to have the client do a GC every tick as
        // well.
    }

    public void setIgnoreHouse() {
        IgnoreHouse = splitString(Config.getParam("IGNOREHOUSE"), ",");
    }

    public void setIgnorePublic() {
        IgnorePublic = splitString(Config.getParam("IGNOREPUBLIC"), ",");
    }

    public void setIgnorePrivate() {
        IgnorePrivate = splitString(Config.getParam("IGNOREPRIVATE"), ",");

    }

    public void setKeyWords() {
        KeyWords = splitString(Config.getParam("KEYWORDS"), ",");
    }

    /**
     * Sets the current advanced terrain and map size that will be used on next
     * playboard
     */
    public void setAdvancedTerrain(AdvancedTerrain aTerrain) {
        this.aTerrain = aTerrain;
    }

    /**
     * Sets the current environment, map size and map medium that will be used
     * on next playboard
     */
    public void setEnvironment(PlanetEnvironment pe, Dimension map,
            int mapMedium) {
        currentEnvironment = pe;
        MapSize = map;
        this.mapMedium = mapMedium;
    }

    public void setBuildingTemplate(Buildings buildingTemplate) {
        this.buildingTemplate = buildingTemplate;
    }

    public Buildings getBuildingTemplate() {
        return buildingTemplate;
    }

    /**
     * Changes the duty to a new status.
     *
     * @param newStatus
     */
    @Override
    public void setStatus(int newStatus) {
        LastStatus = getStatus();
        super.setStatus(newStatus);

        if (getStatus() == MWClient.STATUS_RESERVE) {
            // there commands now send as part of the MWClient contructor.
            // sendChat(GameHost.CAMPAIGN_PREFIX + "c setclientversion#" +
            // this.myUsername+ "#" + CLIENT_VERSION);
            // this.sendChat("/getsavedmail");
        } else if (getStatus() == STATUS_LOGGEDOUT) {
            clearUserCampaignData();
        }

        // update the activity button
        if (getStatus() == MWClient.STATUS_FIGHTING) {
            // this.getGUIClient().getMainFrame().getMainPanel().getUserListPanel().setActivityButton(false);
            getGUIClient().getMainFrame().getMainPanel().getUserListPanel()
                    .setActivityButtonEnabled(false);
        } else if (getStatus() == MWClient.STATUS_ACTIVE) {
            if (LastStatus != MWClient.STATUS_FIGHTING) {
                getGUIClient().getMainFrame().getMainPanel().getUserListPanel()
                        .setActivityButton(false);
            }
            getGUIClient().getMainFrame().getMainPanel().getUserListPanel()
                    .setActivityButtonEnabled(true);
        } else if (getStatus() == MWClient.STATUS_DISCONNECTED) {
            getGUIClient().getMainFrame().getMainPanel().getUserListPanel()
                    .setActivateButtonText("Disconnected");
            getGUIClient().getMainFrame().getMainPanel().getUserListPanel()
                    .setActivityButtonEnabled(false);
        } else if (getStatus() == MWClient.STATUS_LOGGEDOUT) {
            getGUIClient().getMainFrame().getMainPanel().getUserListPanel()
                    .setActivateButtonText("Login");
            getGUIClient().getMainFrame().getMainPanel().getUserListPanel()
                    .setActivityButtonEnabled(true);
        } else if (getStatus() == MWClient.STATUS_RESERVE) {
            if (LastStatus != MWClient.STATUS_LOGGEDOUT) {
                getGUIClient().getMainFrame().getMainPanel().getUserListPanel()
                        .setActivityButton(true);
            }
            getGUIClient().getMainFrame().getMainPanel().getUserListPanel()
                    .setActivityButtonEnabled(true);
        }

        // update the CMainFrame Attack menu
        getGUIClient().getMainFrame().updateAttackMenu();

        getGUIClient().getMainFrame().changeStatus(getStatus(), LastStatus);
        getGUIClient().refreshGUI(GUIClient.REFRESH_HQPANEL);
        getGUIClient().refreshGUI(GUIClient.REFRESH_PLAYERPANEL);
    }

    public String getServerConfigs(String key) {
        String property = CampaignData.cd.getCampaignOptions().getConfig(key);

        if ("-1".equals(property)) {
            LOGGER.error("You're missing the config variable: {} in serverconfig!", key);
            return "-1";
        }
        return property.trim();
    }

    //@Salient ... ugh... how can i get to the damn house configs
//    public String getHouseConfigs(String key)
//    {
//        //CampaignData.cd.ge
//        SHouse house = CampaignData.cd.getHouseByName(this.getPlayer().getHouse());
//
//        return CampaignData.cd.getServerConfigs().getProperty(key).trim();
//    }

    @Deprecated(since = "9.0.0", forRemoval = false)
    public Properties getServerConfigs() {
        return CampaignData.cd.getCampaignOptions().getProperties();
    }

    public boolean isLeader() {
        return getUserLevel() >= Integer
                .parseInt(getServerConfigs("factionLeaderLevel"));
    }

    public int getUserLevel() {
        return getUser(getUsername()).getUserlevel();
    }

    public void clearSavedGames() {

        long daysInSeconds = ((long) savedGamesMaxDays) * 24 * 60 * 60 * 1000;

        File saveFiles = new File("./savegames/");
        if (!saveFiles.exists()) {
            return;
        }
        File[] fileList = saveFiles.listFiles();
        for (File savedFile : fileList) {
            long lastTime = savedFile.lastModified();
            if (savedFile.exists()
                    && savedFile.isFile()
                    && (lastTime < (System.currentTimeMillis() - daysInSeconds))) {
                try {
                    LOGGER.info("Purging File: {} Time: {} purge Time: {}",
                        savedFile.getName(),
                        lastTime,
                        (System.currentTimeMillis() - daysInSeconds)
                    );
                    savedFile.delete();
                } catch (Exception ex) {
                    LOGGER.error("Error trying to delete these files!");
                    LOGGER.catching(ex);
                }
            }
        }
    }

    public String getParanoidAutoSave() {

        File tempFile = new File("./savegames/");
        FilenameFilter filter = new AutoSaveFilter();
        File[] fileList = tempFile.listFiles(filter);
        long time = 0;
        String saveFile = "autosave.sav";
        for (File newFile : fileList) {
            if (newFile.lastModified() > time) {
                time = newFile.lastModified();
                saveFile = newFile.getName();
            }
        }
        return saveFile;
    }

    public void clearBanTargeting() {
        getData().getBannedTargetingSystems().clear();
    }

    public void loadBanTargeting(String line) {
        StringTokenizer st = new StringTokenizer(line, "#");
        while (st.hasMoreTokens()) {
            getData().getBannedTargetingSystems().add(
                    Integer.parseInt(st.nextToken()));
        }
    }

    public void saveBannedTargetingSystems(String timestamp) {
        // Save banned targeting systems
        try {
            OutputStream out = Files.newOutputStream(FileSystem.getInstance().getBanTargeting());
            PrintStream p = new PrintStream(out);
            p.println(timestamp);
            for (Integer targetingSytem : getData().getBannedTargetingSystems()) {
                p.print(targetingSytem);
                p.print("#");
            }
            p.close();
            out.close();
        } catch (Exception ex) {
        }
    }

    /**
     * @author Torren (Jason Tighe)
     * @param money
     * @param shortname
     * @param amount
     * @return String Hokey function to return the correct syntax for long and
     *         short money/flu messages to the user. ClientVersion
     */
    public String moneyOrFluMessage(boolean money, boolean shortname, int amount) {
        return moneyOrFluMessage(money, shortname, amount, false);
    }

    public String moneyOrFluMessage(boolean money, boolean shortname,
            int amount, boolean showSign) {
        String result = NumberFormat.getInstance().format(amount);

        String moneyShort = getServerConfigs("MoneyShortName");
        String moneyLong = getServerConfigs("MoneyLongName");
        String fluShort = getServerConfigs("FluShortName");
        String fluLong = getServerConfigs("FluLongName");
        // String RPLong = getServerConfigs("RPLongName");
        // String RPShort = getServerConfigs("RPShortName");

        String sign = "+";

        if (amount < 0) {
            amount *= -1;
            result = "";
            sign = "-";
        }

        if (money) {
            if (shortname) {
                if ((amount == 1) && moneyShort.endsWith("s")) {
                    result += moneyShort.substring(0, moneyShort.length() - 1);
                } else if ((amount > 1) && !moneyShort.endsWith("s")) {
                    result += moneyShort + "s";
                } else {
                    result += moneyShort;
                }
            } else {// longname
                if ((amount == 1) && moneyLong.endsWith("s")) {
                    result += " "
                            + moneyLong.substring(0, moneyLong.length() - 1);
                } else if ((amount > 1) && !moneyLong.endsWith("s")) {
                    result += " " + moneyLong + "s";
                } else {
                    result += " " + moneyLong;
                }
            }
        } else {// influence
            if (shortname) {
                result += fluShort;
            } else {
                result += " " + fluLong;
            }
        }

        // add sign, if set
        if (showSign) {
            result = sign + result;
        }

        return result.trim();
    }

    public void loadMegaMekClient() {
        try {
            setWaiting(true);
            sendChat(IClient.PROTOCOL_PREFIX + "c GetServerMegaMekGameOptions");
            try {
                while (isWaiting()) {
                    Thread.sleep(10);
                }
            } catch (Exception ex) {
                LOGGER.catching(ex);
            }
            GameOptions gameOptions = new GameOptions();
            gameOptions.loadOptions();
            GameOptionsDialog MMGOD = new GameOptionsDialog(getGUIClient().getMainFrame(),
                    gameOptions, true);
            MMGOD.update(gameOptions);
            MMGOD.setEditable(true);
            MMGOD.setVisible(true);
            MMGOD.dispose();
            File localGameOptions = new File("mmconf/gameoptions.xml");

            if (localGameOptions.lastModified() >= (System.currentTimeMillis() - 1000)) {
                sendGameOptionsToServer();
            }
        } catch (Exception ex) {
            LOGGER.error("Unable to pull server MegaMek Logs");
            LOGGER.catching(ex);
        }
    }

    public int getMinPlanetOwnerShip(Planet p) {

        if (p.getMinPlanetOwnerShip() == -1) {
            return Integer.parseInt(getServerConfigs("MinPlanetOwnerShip"));
        }

        return p.getMinPlanetOwnerShip();
    }

    public int getTotalRepairCosts(Entity unit) {
        int cost = 0;
        int systemCrits = 0;
        int engineCrits = 0;

        for (int critLocation = 0; critLocation < unit.locations(); critLocation++) {
            // These three location have rear armor so the user might be
            // selecting that armor instead of crit.
            if ((critLocation == Mech.LOC_CT) || (critLocation == Mech.LOC_LT)
                    || (critLocation == Mech.LOC_RT)) {
                if (unit.getArmor(critLocation, false) != unit.getOArmor(
                        critLocation, false)) {
                    cost += UnitRepairCostCalculator.getArmorCost(unit, critLocation)
                            * (unit.getOArmor(critLocation, false) - unit
                                    .getArmor(critLocation, false));
                }
                if (unit.getArmor(critLocation, true) != unit.getOArmor(
                        critLocation, true)) {
                    cost += UnitRepairCostCalculator.getArmorCost(unit, critLocation)
                            * (unit.getOArmor(critLocation, false) - unit
                                    .getArmor(critLocation, false));
                }
                if (unit.getInternal(critLocation) != unit
                        .getOInternal(critLocation)) {
                    cost += UnitRepairCostCalculator.getStructureCost(unit)
                            * (unit.getOInternal(critLocation) - unit
                                    .getInternal(critLocation));
                }
            }// end toros armor
            else {
                if (unit.getArmor(critLocation, false) != unit.getOArmor(
                        critLocation, false)) {
                    cost += UnitRepairCostCalculator.getArmorCost(unit, critLocation)
                            * (unit.getOArmor(critLocation, false) - unit
                                    .getArmor(critLocation, false));
                }
                if (unit.getInternal(critLocation) != unit
                        .getOInternal(critLocation)) {
                    cost += UnitRepairCostCalculator.getStructureCost(unit)
                            * (unit.getOInternal(critLocation) - unit
                                    .getInternal(critLocation));
                }
            }// end armor

            for (int critSlot = 0; critSlot < unit
                    .getNumberOfCriticals(critLocation); critSlot++) {

                CriticalSlot cs = unit.getCritical(critLocation, critSlot);

                if (cs == null) {
                    continue;
                }

                if (cs.isBreached()) {
                    continue;
                }

                if (!cs.isDamaged()) {
                    continue;
                }

                if (UnitUtils.isEngineCrit(cs)) {
                    engineCrits = UnitUtils.getNumberOfEngineCrits(unit);
                } else if (cs.getType() == CriticalSlot.TYPE_SYSTEM) {
                    systemCrits++;
                } else {
                    cost += UnitRepairCostCalculator.getCritCost(unit, cs);
                }
            }// end slot for
        }// end location for

        cost += Integer.parseInt(this.getServerConfigs("SystemCritRepairCost"))
                * systemCrits;
        cost += Integer.parseInt(this.getServerConfigs("EngineCritRepairCost"))
                * engineCrits;

        return cost;
    }

    public void setServerOpFlags(StringTokenizer st) {
        TreeMap<String, String> map = new TreeMap<String, String>();

        try {
            while (st.hasMoreTokens()) {
                map.put(st.nextToken(), st.nextToken());
            }
            getData().getPlanetOpFlags().clear();
            getData().getPlanetOpFlags().putAll(map);
        } catch (Exception ex) {
        }
    }

    public int getTechLaborCosts(Entity unit, int techType) {
        int techCost = Integer.parseInt(getServerConfigs(UnitUtils
                .techDescription(techType) + "TechRepairCost"));

        return UnitUtils.getTechLaborCosts(unit, techCost);
    }

    public boolean isUsingAdvanceRepairs() {
        return Boolean.parseBoolean(getServerConfigs("UseAdvanceRepair"))
                || Boolean.parseBoolean(getServerConfigs("UseSimpleRepair"));
    }

    public boolean isDedicated() {
        return false;
    }

    public void updateOpData(boolean deleteCache) {
        try {
            if (deleteCache) {
                Files.delete(FileSystem.getInstance().getOpList());
            }

            dataFetcher.checkForMostRecentOpList();
            setupAllOps();
        } catch (Exception ex) {
            LOGGER.catching(ex);
        }
    }

    public void updateParam(StringTokenizer ST) {
        try {
            getConfig().setParam(ST.nextToken(), ST.nextToken());
            getConfig().saveConfig();
            setConfig();
        } catch (Exception ex) {
            LOGGER.catching(ex);
        }
    }

    public Server getMyServer() {
        return myServer;
    }

    public boolean isWaiting() {
        return waitingOnCommand;
    }

    public void setWaiting(boolean waiting) {
        waitingOnCommand = waiting;
    }

    public HashMap<String, Equipment> getBlackMarketEquipmentList() {
        return blackMarketEquipmentList;
    }

    public void updatePartsBlackMarket(String data, int year) {
        StringTokenizer ST = new StringTokenizer(data, "#");
        boolean allowTechCrossOver = Boolean.parseBoolean(this
                .getServerConfigs("AllowCrossOverTech"));
        int houseTechLevel = getData().getHouseByName(getPlayer().getHouse())
                .getTechLevel();

        getCampaign().getBlackMarketParts().clear();

        while (ST.hasMoreTokens()) {

            BMEquipment bme = new BMEquipment();
            boolean error = false;
            boolean disallowed = false;
            try {
                error = false;
                disallowed = false;
                bme.setEquipmentInternalName(ST.nextToken());
                bme.setAmount(Integer.parseInt(ST.nextToken()));
                bme.setCost(Double.parseDouble(ST.nextToken()));
                bme.setCostUp(Boolean.parseBoolean(ST.nextToken()));

                bme.getTech(year);

                if (!allowTechCrossOver
                        && !UnitUtils
                                .isSameTech(bme.getTechLevel(), houseTechLevel)) {
                    disallowed = true;
                }
            } catch (Exception e) {
                LOGGER.error("Exception in Parts BM");
                LOGGER.catching(e);
                error = true;
            }

            if(!error && !disallowed) {
                getCampaign().getBlackMarketParts().put(
                    bme.getEquipmentInternalName(), bme);
            }
        }

        getGUIClient().getMainFrame().getMainPanel().refreshBME();
    }

    public void updatePlayerPartsCache(String data) {

        StringTokenizer ST = new StringTokenizer(data, "#");
        String key = ST.nextToken();
        int value = Integer.parseInt(ST.nextToken());

        if (value < 1) {
            getPlayer().getUnitComponents().remove(key, Math.abs(value));
        } else {
            getPlayer().getUnitComponents().add(key, value);
        }

        getGUIClient().getMainFrame().getMainPanel().refreshBME();
    }

    public void updateClient() {
        try {
            // this.stopHost();
            goodbye();
            Runtime runtime = Runtime.getRuntime();
            String[] call = { "java", "-jar", "MekWarsAutoUpdate.jar", "PLAYER" };
            runtime.exec(call);
        } catch (Exception ex) {
            LOGGER.catching(ex);
        }
        System.exit(0);
    }

    /*
     * INNER CLASSES
     */
    static class AutoSaveFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return (name.startsWith("autosave"));
        }
    }

    private static class PurgeAutoSaves implements Runnable {

        public PurgeAutoSaves() {
            super();
        }

        @Override
        public void run() {
            long twoHours = 2 * 60 * 60 * 1000;
            try {
                while (true) {
                    File saveFiles = new File("./savegames");
                    if (!saveFiles.exists()) {
                        return;
                    }
                    FilenameFilter filter = new AutoSaveFilter();
                    File[] fileList = saveFiles.listFiles(filter);
                    for (File savedFile : fileList) {
                        long lastTime = savedFile.lastModified();
                        if (savedFile.exists()
                                && savedFile.isFile()
                                && (lastTime < (System.currentTimeMillis() - twoHours))) {
                            try {
                                LOGGER.info("Purging File: {} Time: {} purge Time: {}",
                                    savedFile.getName(),
                                    lastTime,
                                    (System.currentTimeMillis() - twoHours)
                                );
                                savedFile.delete();
                            } catch (Exception ex) {
                                LOGGER.error("Error trying to delete these files!");
                                LOGGER.catching(ex);
                            }
                        }
                    }
                    Thread.sleep(twoHours);
                }
            } catch (Exception ex) {
                return;
            }
        }
    }// end PurgeAutoSaves

    public void createNewHouse(StringTokenizer st) {
        getData().addHouse(new House(st));
    }

    /**
     * redundant code since MM does not always send a discon event.
     */
    public void gamePlayerStatusChange(GameEvent e) {
    }

    protected void sendGameReport() {
        if (myServer == null) {
            return;
        }

        StringBuilder result = GameReport.prepareReport(
                new GameWrapper(((Game)myServer.getGame())), isUsingAdvanceRepairs(),
                getBuildingTemplate());

        // send the autoreport
        serverSend("CR|" + result.toString());

        // we may assume that a server which reports a game is no longer
        // "Running"
        serverSend("SHS|" + myUsername + "|Open");
    }

    public boolean getTargetSystemBanStatus(int type) {
        if (getData().getBannedTargetingSystems().contains(type)) {
            return true;
        }
        return false;
    }

    public List<ClientThread> getMMClients() {
        return mmClientThreads;
    }

    private void chatCaptureForBot(String username, String addon, String input) { //@salient
        if(!Boolean.parseBoolean(getServerConfigs("Enable_Bot_Chat")))
            return;

        String temp = getShortTime().trim() + username.trim() + addon.trim() + ":" + input;
        temp = String.format("%s%n", temp);

//        if(channel !=0)
//            return;

        //call a new command to capture chat server side
        sendChat(GameHost.CAMPAIGN_PREFIX + "CHATBOT " + temp);
    }

    @Override
    public void gameClientFeedbackRequest(GameCFREvent arg0) {
        // TODO Auto-generated method stub
        
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Camouflage getCamouflage() {
        Path path = Paths.get(getConfig().getParam("UNITCAMO"));
        Path filename = path.getFileName();
        Path parent = path.getParent();

        if (filename == null || parent == null) {
            LOGGER.warn("Invalid camouflage config '{}'", path.toString());
            return null;
        }
        return new Camouflage(parent.toString(), filename.toString());
    }

    public HPGClient getHpgClient() {
        return hpgClient;
    }
}
