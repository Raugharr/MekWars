package mekwars.client.common.campaign.clientutils;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import megamek.MMConstants;
import megamek.Version;
import megamek.common.Building;
import megamek.common.Entity;
import megamek.common.Game;
import megamek.common.Mech;
import megamek.common.MechWarrior;
import megamek.common.enums.GamePhase;
import megamek.common.event.*;
import megamek.server.GameManager;
import megamek.server.Server;
import mekwars.client.cmd.Command;
import mekwars.client.common.campaign.clientutils.protocol.CConnector;
import mekwars.client.common.campaign.clientutils.protocol.IClient;
import mekwars.client.common.campaign.clientutils.protocol.commands.IProtCommand;
import mekwars.common.MMGame;
import mekwars.common.campaign.Buildings;
import mekwars.common.campaign.clientutils.IClientConfig;
import mekwars.common.campaign.clientutils.IClientUser;
import mekwars.common.campaign.clientutils.IGameHost;
import mekwars.common.campaign.clientutils.SerializeEntity;
import mekwars.common.campaign.clientutils.protocol.TransportCodec;
import mekwars.common.util.UnitUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class GameHost implements GameListener, IGameHost {
    private static final Logger LOGGER = LogManager.getLogger(GameHost.class);
    
    public static final int STATUS_DISCONNECTED = 0;
    public static final int STATUS_LOGGEDOUT = 1;
    public static final int STATUS_RESERVE = 2;
    public static final int STATUS_ACTIVE = 3;
    public static final int STATUS_FIGHTING = 4;
    
    public static final String CAMPAIGN_PREFIX = "/"; // prefix for campaign commands
    public static final String COMMAND_DELIMITER = "|"; // delimiter for client commands
    
    public String myUsername = "";// public b/c used in RGTS command to set server status. HACK!
    
    protected TreeMap<String, IProtCommand> ProtCommands;
    
    protected IClientConfig Config;

    protected CConnector Connector;
    
    protected Server myServer = null;
    protected Date mytime = new Date(System.currentTimeMillis());
    protected TreeMap<String, MMGame> servers = new TreeMap<String, MMGame>();// hostname,mmgame
    protected Vector<String> decodeBuffer = new Vector<String>(1, 1);// used to buffer incoming data until CMainFrame is built

    protected Buildings buildingTemplate = null;

    /**
     * Maps the task prefixes as HS, PL, SP etc. to a command under package cmd.
     * key: String, value: cmd.Command
     */
    HashMap<String, Command> commands = new HashMap<String, Command>();
    
    protected int savedGamesMaxDays = 30; // max number of days a save game can be before
    // its deleted.
    
    protected GamePhase currentPhase = GamePhase.DEPLOYMENT;
    protected int turn = 0;
    protected int myPort = -1;
    private int status = 0;
    
	@Override
	public void gameBoardChanged(GameBoardChangeEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gameBoardNew(GameBoardNewEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gameClientFeedbackRquest(GameCFREvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gameEnd(GameEndEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gameEntityChange(GameEntityChangeEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gameEntityNew(GameEntityNewEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gameEntityNewOffboard(GameEntityNewOffboardEvent arg0) {
		// TODO Auto-generated method stub
		
	}

    /*
     * When an entity is removed from play, check the reason. If the unit is
     * ejected, captured or devestated and the player is invovled in the game at
     * hand, report the removal to the server. The server stores these reports
     * in pilotTree and deathTree in order to auto-resolve games after a player
     * disconnects. NOTE: This send thefirst possible removal condition, which
     * means that a unit which is simultanously head killed and then CT cored
     * will show as salvageable.
     */
    public void gameEntityRemove(GameEntityRemoveEvent e) {// only send if the
        // player is
        // actually involved
        // in the game

        // get the entity
        megamek.common.Entity removedE = e.getEntity();
        if (removedE.getOwner().getName().startsWith("War Bot")) {
            return;
        }

        String toSend = SerializeEntity.serializeEntity(removedE, true, false, isUsingAdvanceRepairs());
        serverSend("IPU|" + toSend);
    }

	protected abstract boolean isUsingAdvanceRepairs();

	@Override
	public void gameMapQuery(GameMapQueryEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gameNewAction(GameNewActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

    public void gamePhaseChange(GamePhaseChangeEvent e) {
        try {

            /*
             * Reporting phases show deaths - units that try to stand and blow
             * their ammo, units that have ammo explode from head, etc. This is
             * also an opportune time to correct isses with the gameRemoveEntity
             * ISU's. Removals happen ASAP, even if the removal condition and
             * final condition of the unit are not the same (ie - remove on
             * Engine crits even when a CT core comes later in the round).
             */
            sendServerGameUpdate();

        } catch (Exception ex) {
            LOGGER.error("Error reporting game!");
            LOGGER.error("Exception: ", ex);
        }
    }

	@Override
	public void gamePlayerChange(GamePlayerChangeEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gamePlayerChat(GamePlayerChatEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gamePlayerConnected(GamePlayerConnectedEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gamePlayerDisconnected(GamePlayerDisconnectedEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gameReport(GameReportEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gameSettingsChange(GameSettingsChangeEvent arg0) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void gameTurnChange(GameTurnChangeEvent e) {
        if (myServer != null) {
            if (turn == 0) {
                serverSend("SHS|" + getUsername() + "|Running");
            } else if ((myServer.getGame().getPhase() != currentPhase)
                    && myServer.getGame().getOptions()
                            .booleanOption("paranoid_autosave")) {
                sendServerGameUpdate();
                currentPhase = myServer.getGame().getPhase();
            }
            turn += 1;

        }
    }

	public void gameVictory(GameVictoryEvent e) {
        sendGameReport();
        LOGGER.info("GAME END");	
	}
    
    protected abstract void sendGameReport();

	public boolean isAdmin() {
        return getUser(getUsername()).getUserlevel() >= 200;
    }

    public boolean isMod() {
        return getUser(getUsername()).getUserlevel() >= 100;
    }
	
    public String getUsername() {
        return myUsername;
    }
    
    protected abstract IClientUser getUser(String name);
    
    public int getBuildingsLeft() {
        Enumeration<Building> buildings = ((Game)myServer.getGame()).getBoard()
                .getBuildings();
        int buildingCount = 0;
        while (buildings.hasMoreElements()) {
            buildings.nextElement();
            buildingCount++;
        }
        return buildingCount;
    }
    
    public void purgeOldLogs() {

        long daysInSeconds = ((long) savedGamesMaxDays) * 24 * 60 * 60 * 1000;

        File saveFiles = new File("./logs/backup");
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
                    LOGGER.info("Purging File: "
                            + savedFile.getName() + " Time: " + lastTime
                            + " purge Time: "
                            + (System.currentTimeMillis() - daysInSeconds));
                    savedFile.delete();
                } catch (Exception ex) {
                    LOGGER.error("Error trying to delete these files!");
                    LOGGER.error("Exception: ", ex);
                }
            }
        }
    }
    
    public void sendGameOptionsToServer() {
        StringBuilder packet = new StringBuilder();

        try {
            FileInputStream gameOptionsFile = new FileInputStream("./mmconf/gameoptions.xml");
            BufferedReader gameOptions = new BufferedReader(new InputStreamReader(gameOptionsFile));

            while (gameOptions.ready()) {
                packet.append(gameOptions.readLine() + "#");
            }
            gameOptions.close();
            gameOptionsFile.close();
        } catch (Exception ex) {
        }

        sendChat(GameHost.CAMPAIGN_PREFIX + "c servergameoptions#" + packet.toString());
    }
    

    public TreeMap<String, MMGame> getServers() {
        return servers;
    }
    
    public void sendChat(String s) {
        // Sends the content of the Chatfield to the server
        // We need the StringTokenizer to enable Mulitline comments
        StringTokenizer st = new StringTokenizer(s, "\n");

        while (st.hasMoreElements()) {
            String str = (String) st.nextElement();
            // don't send empty lines
            if (!str.trim().equals("")) {
                serverSend("CH|" + str);
            }
        }
    }
    
    public String doEscape(String str) {

        if (str.indexOf("<a href=\"MEKINFO") != -1) {
            return str;
        }

        // This function removes HTML Tags from the Chat, so no code may harm
        // anyone
        str = doEscapeString(str, '&', "&amp;");
        str = doEscapeString(str, '<', "&lt;");
        str = doEscapeString(str, '>', "&gt;");
        return str;
    }
    
    public String doEscapeString(String t, int character, String replace) {

        // find all occurences of character in t and replace them with replace
        int pos = t.indexOf(character);
        if (pos != -1) {
            String res = "";
            if (pos > 0) {
                res += t.substring(0, pos);
            }
            res += replace;
            if (pos < t.length()) {
                res += doEscapeString(t.substring(pos + 1), character, replace);
            }
            return res;
        }
        return t;
    }
    
    public CConnector getConnector() {
        return Connector;
    }

    public void serverSend(String s) {
        try {
            Connector.send(IClient.PROTOCOL_PREFIX + "comm" + "\t" + TransportCodec.encode(s));
        } catch (Exception e) {
            LOGGER.error("Exception: ", e);
        }
    }

    /**
     * @throws InvalidVersionException When the GameHost's {@link Version} is not allowed.
     *
     * @throws DuplicateHostException When a GameHost is already started.
     *
     * @throws UnknownHostException If the IP of the GameHost cannot be found.
     */
    public void startHost(boolean dedicated, boolean deploy,
            boolean loadSavegame) throws Exception {

        //@salient - check quirk xml file sizes with server
        if(Boolean.parseBoolean(getServerConfigs("EnableQuirks"))) {
            File canon = new File("data" + File.separator + "canonUnitQuirks.xml");
            File custom = new File("data" + File.separator + "mmconf" + File.separator + "unitQuirksOverride.xml");
            long canonFileLength = canon.length(); // returns 0L if does not exist
            long customFileLength = custom.length();        
            sendChat(GameHost.CAMPAIGN_PREFIX + "c QUIRKCHECK#" + canonFileLength + "#" + customFileLength); 
        }

        // reread the config to allow the user to change setting during runtime
        String ip = "127.0.0.1";
        if (!getConfigParam("IP:").isEmpty()) {// IP Setting set, override IP
            // detection.
                ip = getConfigParam("IP:");
                InetAddress IA = InetAddress.getByName(ip); // Resolve Dyndns
                // Entries
                ip = IA.getHostAddress();
        }

        Version MMVersion = new Version(getServerConfigs("AllowedMegaMekVersion"));
        if (!MMVersion.equals("-1")
                && !MMVersion.is(megamek.MMConstants.VERSION)) {
            throw new InvalidVersionException("You are using an invalid version of MegaMek. Please use version " + MMVersion.toString());
        }

        if (servers.get(myUsername) != null) {
            throw new DuplicateHostException("Attempted to start a second host while host was already running.");
        }

        int MaxPlayers = Integer.parseInt(getConfigParam("MAXPLAYERS:"));
        String comment = getConfigParam("COMMENT:");
        String gpassword = getConfigParam("GAMEPASSWORD:");

        if (gpassword == null) {
            gpassword = "";
        }
        try {
            myServer = new Server(gpassword, myPort, new GameManager());
            if (loadSavegame) {
                loadSavegame();
            }
        } catch (Exception ex) {
            try {
                if (myServer == null) {
                    LOGGER.error("Error opening dedicated server. Result = null host.", ex);
                } else {
                    LOGGER.error("Error opening dedicated server. Will attempt a .die().", ex);
                    myServer.die();
                    myServer = null;
                }
            } catch (Exception e) {
                LOGGER.error("Further error while trying to clean up failed host attempt.", e);
            }
            return;
        }

       ((Game)myServer.getGame()).addGameListener(this);
        // Send the new game info to the Server
        serverSend("NG|"
                + new MMGame(myUsername, ip, myPort, MaxPlayers,
                        MMConstants.VERSION, comment)
                        .toString());
    }

    // Stop & send the close game event to the Server
    public void stopHost() {
        serverSend("CG");// send close game to server
        try {
            if (myServer != null) {
                myServer.die();
            }
        } catch (Exception ex) {
            LOGGER.error("Megamek Error: ", ex);
        }
        myServer = null;
    }

    public void retrieveOpData(String type, String data) {
        StringTokenizer st = new StringTokenizer(data, "#");
        String opName = st.nextToken();
        File opFile = new File("./data/operations/" + type);

        if (!opFile.exists()) {
            opFile.mkdirs();
        }

        opFile = new File("./data/operations/" + type + "/" + opName + ".txt");
        try {
            FileOutputStream out = new FileOutputStream(opFile);
            PrintStream p = new PrintStream(out);
            while (st.hasMoreTokens()) {
                p.println(st.nextToken().replaceAll("\\(pound\\)", "#"));
            }
            p.close();
            out.close();
        } catch (Exception ex) {
            LOGGER.error("Exception: ", ex);
        }
    }

    public void retrieveMul(String data) {
        StringTokenizer st = new StringTokenizer(data, "#");
        String mulName = st.nextToken();
        File mulFile = new File("./data/armies/");

        if (!mulFile.exists()) {
            mulFile.mkdirs();
        }

        mulFile = new File("./data/armies/" + mulName);
        try {
            FileOutputStream out = new FileOutputStream(mulFile);
            PrintStream p = new PrintStream(out);
            while (st.hasMoreTokens()) {
                p.println(st.nextToken().replaceAll("\\(pound\\)", "#"));
            }
            p.close();
            out.close();
        } catch (Exception ex) {
            LOGGER.catching(ex);
        }
    }

    /**
     * Changes the duty to a new status.
     * 
     * @param newStatus
     */
    public void setStatus(int newStatus) {
        status = newStatus;
    }

    public int getStatus() {
        return status;
    }

    public String getStatusString() {
        if (status == STATUS_DISCONNECTED) {
            return ("Not connected");
        }
        if (status == STATUS_LOGGEDOUT) {
            return ("Logged out");
        }
        if (status == STATUS_RESERVE) {
            return ("Reserve duty");
        }
        if (status == STATUS_ACTIVE) {
            return ("Active duty");
        }
        if (status == STATUS_FIGHTING) {
            return ("Fighting");
        }
        return ("");
    }

    protected boolean setData(String command) {
        return(true);
    }

    protected void sendServerGameUpdate() {
        // Report the mech stat

        // Only send data for units currently on the board.
        // any units removed from play will have already sent thier final
        // update.
        Iterator<Entity> en = ((Game)myServer.getGame()).getEntities();
        while (en.hasNext()) {
            Entity ent = en.next();
            if (ent.getOwner().getName().startsWith("War Bot")
                    || (!(ent instanceof MechWarrior)
                            && !UnitUtils.hasArmorDamage(ent)
                            && !UnitUtils.hasISDamage(ent)
                            && !UnitUtils.hasCriticalDamage(ent)
                            && !UnitUtils.hasLowAmmo(ent) && !UnitUtils
                                .hasEmptyAmmo(ent))) {
                continue;
            }
            if ((ent instanceof Mech) && (ent.getInternal(Mech.LOC_CT) <= 0)) {
                serverSend("IPU|"
                        + SerializeEntity.serializeEntity(ent, true, true,
                                isUsingAdvanceRepairs()));
            } else {
                serverSend("IPU|"
                        + SerializeEntity.serializeEntity(ent, true, false,
                                isUsingAdvanceRepairs()));
            }
        }
    }



    /*
     * Actual GUI-mode parseData. Before we started streaming data over the chat
     * part, this was called directly. Now we buffer all incoming non-data chat
     * and spit it out at once when the GUI draws. Once the GUI is up, this is
     * called by a simple pass through from doParseDataInput(), above.
     *
     * Ded's call the helper directly to bypass the buffer.
     */
    protected void doParseDataHelper(String input) {
        try {

            // 0-length input is spurious call from MWDedHost constructor.
            if (input.length() == 0) {
                return;
            }

            StringTokenizer ST = null;
            String task = null;

            LOGGER.debug(input);

            // Create a String Tokenizer to parse the elements of the input
            ST = new StringTokenizer(input, COMMAND_DELIMITER);
            task = ST.nextToken();

            if (!commands.containsKey(task)) {
                try {
                    Class<?> cmdClass = Class.forName(getClass().getPackage().getName() + ".cmd." + task);
                    Constructor<?> c = cmdClass.getConstructor(new Class[]
                        { getClass() });
                    Command cmd = (Command) c.newInstance(new Object[]
                        { this });
                    commands.put(task, cmd);
                } catch (Exception e) {
                    LOGGER.error("Unable to store command", e);
                }
            }
            if (commands.containsKey(task)) {
                commands.get(task).execute(input);
            }
        } catch (Exception ex) {
            LOGGER.error("Unable to parse data", ex);
        }
    }
}
