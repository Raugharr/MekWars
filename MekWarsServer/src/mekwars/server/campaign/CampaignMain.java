/*
 *
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet)
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */

package mekwars.server.campaign;

import com.thoughtworks.xstream.XStream;

import megamek.client.Client;
import megamek.common.Mounted;
import megamek.common.WeaponType;
import megamek.common.options.IOption;

import mekwars.common.AdvancedTerrain;
import mekwars.common.CampaignData;
import mekwars.common.Equipment;
import mekwars.common.House;
import mekwars.common.Influences;
import mekwars.common.Planet;
import mekwars.common.Terrain;
import mekwars.common.campaign.CampaignOptions;
import mekwars.common.campaign.operations.Operation;
import mekwars.common.io.file.FactionTraitFile;
import mekwars.common.flags.PlayerFlags;
import mekwars.common.util.HibernateUtil;
import mekwars.common.util.MekwarsFileReader;
import mekwars.server.MWServ;
import mekwars.server.campaign.commands.*;
import mekwars.server.campaign.commands.admin.*;
import mekwars.server.campaign.commands.helpers.HireAndMaintainHelper;
import mekwars.server.campaign.commands.helpers.HireAndRequestNewHelper;
import mekwars.server.campaign.commands.helpers.HireAndRequestUsedHelper;
import mekwars.server.campaign.commands.helpers.RemoveAndAddNoPlayHelper;
import mekwars.server.campaign.commands.leader.DemotePlayerCommand;
import mekwars.server.campaign.commands.leader.FactionLeaderFluffCommand;
import mekwars.server.campaign.commands.leader.FactionLeaderMuteCommand;
import mekwars.server.campaign.commands.leader.GetComponentConversionCommand;
import mekwars.server.campaign.commands.leader.PromotePlayerCommand;
import mekwars.server.campaign.commands.leader.PurchaseFactoryCommand;
import mekwars.server.campaign.commands.leader.ResearchTechLevelCommand;
import mekwars.server.campaign.commands.leader.ResearchUnitCommand;
import mekwars.server.campaign.commands.leader.SetComponentConversionCommand;
import mekwars.server.campaign.commands.leader.ViewFactionPartsCacheCommand;
import mekwars.server.campaign.commands.mod.AddLeaderCommand;
import mekwars.server.campaign.commands.mod.AddPartsCommand;
import mekwars.server.campaign.commands.mod.AnnounceCommand;
import mekwars.server.campaign.commands.mod.BanCommand;
import mekwars.server.campaign.commands.mod.BanIPCommand;
import mekwars.server.campaign.commands.mod.BanListCommand;
import mekwars.server.campaign.commands.mod.BuildTableValidatorCommand;
import mekwars.server.campaign.commands.mod.CheckCommand;
import mekwars.server.campaign.commands.mod.ConfigCommand;
import mekwars.server.campaign.commands.mod.CreateSubFactionCommand;
import mekwars.server.campaign.commands.mod.FixAmmoCommand;
import mekwars.server.campaign.commands.mod.GetModLogCommand;
import mekwars.server.campaign.commands.mod.GetPlayerUnitsCommand;
import mekwars.server.campaign.commands.mod.GooseCommand;
import mekwars.server.campaign.commands.mod.GrantEXPCommand;
import mekwars.server.campaign.commands.mod.GrantInfluenceCommand;
import mekwars.server.campaign.commands.mod.GrantMoneyCommand;
import mekwars.server.campaign.commands.mod.GrantRewardCommand;
import mekwars.server.campaign.commands.mod.GrantTechPointsCommand;
import mekwars.server.campaign.commands.mod.GrantTechsCommand;
import mekwars.server.campaign.commands.mod.HardTerminateCommand;
import mekwars.server.campaign.commands.mod.IPListCommand;
import mekwars.server.campaign.commands.mod.IgnoreCommand;
import mekwars.server.campaign.commands.mod.IgnoreListCommand;
import mekwars.server.campaign.commands.mod.KickCommand;
import mekwars.server.campaign.commands.mod.ListMultiPlayerGroupsCommand;
import mekwars.server.campaign.commands.mod.MMOTDCommand;
import mekwars.server.campaign.commands.mod.ModDeactivateCommand;
import mekwars.server.campaign.commands.mod.ModFullRepairCommand;
import mekwars.server.campaign.commands.mod.ModGamesCommand;
import mekwars.server.campaign.commands.mod.ModLogCommand;
import mekwars.server.campaign.commands.mod.ModNoPlayCommand;
import mekwars.server.campaign.commands.mod.ModRefreshFactoryCommand;
import mekwars.server.campaign.commands.mod.ModTerminateCommand;
import mekwars.server.campaign.commands.mod.ModeratorMailCommand;
import mekwars.server.campaign.commands.mod.NotifyFightingCommand;
import mekwars.server.campaign.commands.mod.RemoveLeaderCommand;
import mekwars.server.campaign.commands.mod.RemovePartsCommand;
import mekwars.server.campaign.commands.mod.RemoveSubFactionCommand;
import mekwars.server.campaign.commands.mod.ServerAnnouncementCommand;
import mekwars.server.campaign.commands.mod.SetEloCommand;
import mekwars.server.campaign.commands.mod.SetHouseBasePilotSkillsCommand;
import mekwars.server.campaign.commands.mod.SetMMOTDCommand;
import mekwars.server.campaign.commands.mod.SetSMOTDCommand;
import mekwars.server.campaign.commands.mod.SetSubFactionConfigCommand;
import mekwars.server.campaign.commands.mod.StripAllPartsCacheCommand;
import mekwars.server.campaign.commands.mod.TerminateContractCommand;
import mekwars.server.campaign.commands.mod.TouchCommand;
import mekwars.server.campaign.commands.mod.UnBanCommand;
import mekwars.server.campaign.commands.mod.UnBanIPCommand;
import mekwars.server.campaign.commands.mod.UnlockLancesCommand;
import mekwars.server.campaign.commands.mod.UpdateServerUnitsCacheCommand;
import mekwars.server.campaign.commands.mod.ViewPlayerPartsCommand;
import mekwars.server.campaign.commands.mod.ViewPlayerPersonalPilotQueueCommand;
import mekwars.server.campaign.commands.mod.ViewPlayerUnitCommand;
import mekwars.server.campaign.market2.Market2;
import mekwars.server.campaign.market2.PartsMarket;
import mekwars.server.campaign.mercenaries.ContractInfo;
import mekwars.server.campaign.mercenaries.MercHouse;
import mekwars.server.campaign.operations.OperationManager;
import mekwars.server.campaign.operations.ShortOperation;
import mekwars.server.campaign.operations.newopmanager.I_OperationManager;
import mekwars.server.campaign.operations.newopmanager.NewOperationManager;
import mekwars.server.campaign.util.ChatRoom;
import mekwars.server.campaign.util.ChristmasHandler;
import mekwars.server.campaign.util.HouseRankingHelpContainer;
import mekwars.server.campaign.util.MechStatistics;
import mekwars.server.campaign.util.Statistics;
import mekwars.server.campaign.util.WhoToHTML;
import mekwars.server.campaign.util.scheduler.MWScheduler;
import mekwars.server.campaign.util.scheduler.TickJob;
import mekwars.server.campaign.votes.VoteManager;
import mekwars.server.common.util.SMMNetXStream;
import mekwars.server.io.FileSystem;
import mekwars.server.util.HtmlSanitizer;
import mekwars.server.util.MWPasswd;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

public final class CampaignMain implements Serializable {
    private static final Logger LOGGER = LogManager.getLogger(CampaignMain.class);

    private static final long serialVersionUID = -8671163467590633378L;

    /**
     * I realized, that almost every class needs access to the current global campaign state. So I
     * decided (after consultation with McWizard) to make this back reference obsolete by
     * introducing a public static member (Java's pardon to a global variable). Although this
     * reduces code size, complexity of code and memory footprint, this is still a HACK! Java wasn't
     * invented to step back to the old days of global variables. Object oriented coding should try
     * to minimize cross references.. But someday you gotta do what you gotta do..... Imi.
     */
    public static CampaignMain cm;

    private Client megaMekClient = new Client("MWServer", "None", 0);
    private CampaignData data;
    private Hashtable<String, Command> Commands = new Hashtable<String, Command>();
    private Hashtable<String, MechStatistics> MechStats = new Hashtable<String, MechStatistics>();
    private Hashtable<String, String> omniVariantMods = new Hashtable<String, String>();
    private Hashtable<String, Equipment> blackMarketEquipmentCostTable =
            new Hashtable<String, Equipment>();
    private int gamesCompleted; // used by Tracker
    private int currentUnitID = 1;
    private int currentPilotID = 1;
    private Market2 market;
    private PartsMarket partsmarket;
    private VoteManager voteManager;
    private I_OperationManager opsManager;
    private Vector<ContractInfo> unresolvedContracts = new Vector<ContractInfo>(1, 1);
    private UnitCosts unitCostLists = null;
    private boolean isArchiving = false;
    private Random r = new Random(System.currentTimeMillis());
    private Date housePlanetDate = new Date();
    private HashMap<String, ChatRoom> chatRooms = new HashMap<String, ChatRoom>();
    private HtmlSanitizer htmlSanitizer = null;

    /**
     * This is a hash collection of all the players that have yet to log into their houses This
     * catch all is to keep from having to load the player file over and over again. Once the player
     * has been logged in they are removed from this hash and added to the houses memory.
     */
    private Hashtable<String, SPlayer> lostSouls = new Hashtable<String, SPlayer>();

    private Vector<String> supportUnits = new Vector<String>();
    private PlayerFlags defaultPlayerFlags = new PlayerFlags();
    private MWScheduler scheduler;
    private ChristmasHandler christmas;
    private SMMNetXStream xstream;

    public CampaignMain() {
        cm = this;
        CampaignOptions campaignOptions =
                new CampaignOptions(FileSystem.getInstance().getCampaignConfig());

        data = new CampaignData(campaignOptions);

        if (!getConfig("AllowedMegaMekVersion").equals("-1")) {
            data.getCampaignOptions()
                    .setProperty("AllowedMegaMekVersion", megamek.MMConstants.VERSION.toString());
        }

        /*
         * Create the auction environment/market. Notice that the new market
         * implementation does not save a .dat file. While saving the status was
         * a nice idea, it was creating dupes and NPEs after crashes.
         */

        market = new Market2();
        partsmarket = new PartsMarket();

        xstream = new SMMNetXStream();

        // load megamek gameoptions;
        LOGGER.info("Loading MegaMek Game Options");
        cm.megaMekClient.getGame().getOptions().loadOptions();
    }

    public void start() {
        gamesCompleted = 0;
        cm.loadTopUnitID();

        try {
            long size =
                    HibernateUtil.fromTransaction(
                            session ->
                                    session.createQuery("SELECT COUNT(*) FROM Terrain", Long.class)
                                            .getSingleResult());

            if (size == 0) {
                Session session = HibernateUtil.getInstance().getCurrentSession();
                Transaction transaction = session.beginTransaction();

                try {
                    loadTerrainData(session);
                    loadAdvancedTerrainData(session);
                    loadFactionData();
                    loadPlanetData(session);
                    transaction.commit();
                } catch (Exception e) {
                    transaction.rollback();
                    throw e;
                }
            }
        } catch (IOException exception) {
            LOGGER.error("Unable to parse file", exception);
        }

        if (Files.exists(FileSystem.getInstance().getBanAmmoPath())) {
            FileSystem.getInstance().getBanAmmoFile().load(data);
        }

        // misc loads.
        cm.loadOmniVariantMods();
        cm.loadBlackMarketSettings();

        cm.loadBannedTargetSystems();
        cm.loadSupportUnitDefinitions();

        // create command hashs
        init();

        if (Boolean.parseBoolean(cm.getConfig("UseCalculatedCosts"))) {
            unitCostLists = new UnitCosts();
            unitCostLists.loadUnitCosts();
        }

        // Load the Mech-Statistics
        try {
            for (String line : Files.readAllLines(FileSystem.getInstance().getMechStat())) {
                MechStatistics m = new MechStatistics(line);
                MechStats.put(m.getMechFileName(), m);
            }
        } catch (Exception ex) {
            LOGGER.error("Problems reading unit statistics data", ex);
        }

        if (Boolean.parseBoolean(getConfig("HTMLOUTPUT"))) {
            Statistics.doRanking();
        }

        // Start a VoteManager.
        voteManager = new VoteManager(this);

        /*
         * start an OperationManager. The manager loads all ops files and
         * creates necessary instances of Validators, Resolvers and other helper
         * objects as part of its construction.
         */
        createNewOpsManager();

        // Start up the HTML Sanitizer
        htmlSanitizer =
                new HtmlSanitizer(
                        getBooleanConfig("AllowLinksInMOTD"),
                        getBooleanConfig("AllowPlanetsInMOTD"));

        // Load the default player flags
        defaultPlayerFlags.loadFromDisk();

        // Load the scheduler
        scheduler = MWScheduler.getInstance();
        scheduler.start();

        // Load the Christmas Handler and set the start and end dates
        christmas = ChristmasHandler.getInstance();
        christmas.schedule();
    }

    public void loadSupportUnitDefinitions() {
        LOGGER.info("Entering loadSupportUnitDefinitions");

        Path tsPath = FileSystem.getSupportUnits();
        if (!Files.exists(tsPath)) {
            return;
        }

        Vector<String> units = new Vector<String>();
        try {
            for (String line : Files.readAllLines(tsPath)) {
                line = line.trim().toLowerCase();
                if (line.startsWith("#") || line.length() < 5) {
                    continue;
                }
                if (!units.contains(line)) {
                    units.add(line);
                    LOGGER.info("Adding Support Unit: " + line);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Unable to read {}", tsPath.toString(), e);
        } finally {
            CampaignMain.cm.setSupportUnits(units);
        }
    }

    /** Saves the current campaign state to a file system. */
    public void toFile() {
        try {
            // wait for the backup to finsh before you start saving files.
            while (cm.isArchiving()) {
                Thread.sleep(125);
            }

            saveFactionData();
            savePlanetData();

            // Save omni variant mods
            cm.saveOmniVariantMods();

            // Save Mech-Stats
            FileOutputStream out = new FileOutputStream("./campaign/mechstat.dat");
            PrintStream p = new PrintStream(out);
            for (MechStatistics currStats : MechStats.values()) {
                p.println(currStats.toString());
            }
            p.close();
            out.close();

            try {
                // Save the Readable Mechstats

                out = new FileOutputStream(getConfig("MechstatPath"));
                p = new PrintStream(out);
                p.println(
                        "<html><head><link rel=\"stylesheet\" type=\"text/css\""
                            + " href=\"format.css\"><style"
                            + " type=\"text/css\"></style></head><body><font face=\"Verdana, Arial,"
                            + " Helvetica, sans-serif\">");
                for (int i = 0; i <= 3; i++) {
                    p.println(Statistics.doGetMechStats(i));
                    p.println("<br>");
                }
                p.println("</font></body></style></html>");
                p.close();
                out.close();
            } catch (FileNotFoundException efnf) {
                // ignore
            }

            LOGGER.info("STATUS SAVED");

        } catch (Exception ex) {
            LOGGER.error("Problems saving configuration to file");
            LOGGER.error("Exception: ", ex);
        }
    }

    public boolean isLoggedIn(String username) {

        // always treat deds as logged in
        if (username.startsWith("[Dedicated]")) {
            return true;
        }

        // Use database query to check if player is online (not STATUS_LOGGEDOUT)
        Integer result = HibernateUtil.fromTransaction(session -> 
            session.createQuery("SELECT COUNT(p) FROM SPlayer p WHERE p.name = :name AND p.status != :loggedOutStatus", Integer.class)
                .setParameter("name", username)
                .setParameter("loggedOutStatus", SPlayer.STATUS_LOGGEDOUT)
                .uniqueResult()
        );
        
        return result != null && result > 0;
    }

    @Deprecated(since = "9.0.0", forRemoval = false)
    public CampaignOptions getCampaignOptions() {
        return data.getCampaignOptions();
    }

    @Deprecated(since = "9.0.0", forRemoval = false)
    public boolean getBooleanConfig(String key) {
        return getCampaignOptions().getBooleanConfig(key);
    }

    @Deprecated(since = "9.0.0", forRemoval = false)
    public int getIntegerConfig(String key) {
        return getCampaignOptions().getIntegerConfig(key);
    }

    @Deprecated(since = "9.0.0", forRemoval = false)
    public long getLongConfig(String key) {
        return getCampaignOptions().getLongConfig(key);
    }

    @Deprecated(since = "9.0.0", forRemoval = false)
    public double getDoubleConfig(String key) {
        return getCampaignOptions().getDoubleConfig(key);
    }

    @Deprecated(since = "9.0.0", forRemoval = false)
    public float getFloatConfig(String key) {
        return getCampaignOptions().getFloatConfig(key);
    }

    @Deprecated(since = "9.0.0", forRemoval = false)
    public String getConfig(String key) {
        return getCampaignOptions().getConfig(key);
    }

    /**
     * Method that allows other classes to access the opsManager instance via the static
     * CampaignMain.
     */
    public I_OperationManager getOpsManager() {
        return opsManager;
    }

    public void createNewOpsManager() {
        if (CampaignMain.cm.getBooleanConfig("UseNewOpManager")) {
            opsManager = new NewOperationManager();
        } else {
            opsManager = new OperationManager();
        }
    }

    public void fromUser(String text, String username) {

        // if you don't have a client signon to the server then you do not get
        // to send commands
        if (MWServ.getInstance().getClient(username) == null) {
            return;
        }

        /*
         * Only a few commands should be accepted from a logged out player.
         * Unless the command is enroll, login, or register, return without
         * further processing. Register won't succeed unless player has a
         * campaign account.
         */
        if (!isLoggedIn(username)
                && (text.toUpperCase().indexOf("ENROLL") == -1)
                && (text.toUpperCase().indexOf("LOGIN") == -1)
                && (text.toUpperCase().indexOf("REGISTER") == -1)
                && (text.toUpperCase().indexOf("GETSERVERCONFIGS") == -1)
                && (text.toUpperCase().indexOf("SETCLIENTVERSION") == -1)
                && (text.toUpperCase().indexOf("GETSAVEDMAIL") == -1)) {
            toUser("You are not logged in!", username, true);
            return;
        }

        text = text.substring(2);
        // Date d = new Date(System.currentTimeMillis());
        // LOGGER.info(d + ":" + "Command from User " + username
        // + ": "
        // + text);
        // LOGGER.info(username + ": " + text);

        StringTokenizer ST = new StringTokenizer(text, "#");
        if (ST.hasMoreElements()) {

            // check command type
            String task = ((String) ST.nextElement()).toUpperCase();

            // idle checker omit pong command
            if (!task.equals("PONG")) {
                try {
                    this.getPlayer(username).setLastTimeCommandSent(System.currentTimeMillis());
                } catch (Exception ex) {
                    if (!username.startsWith("[Dedicated]")) {
                        // commands
                        LOGGER.error("Command received from a null player (" + username + ")?");
                    }
                }
            }

            // New Method (much cleaner)
            if (Commands.get(task) != null) {

                // log non-chat commands
                if (task.equals("MAIL")
                        || task.equals("HOUSEMAIL")
                        || task.equals("HM")
                        || task.equals("MODERATORMAIL")
                        || task.equals("MM")
                        || task.equals("INCHARACTER")
                        || task.equals("IC")) {
                    // do nothing
                } else {
                    LOGGER.info(username + ": " + text);
                }

                Command c = Commands.get(task);
                try {
                    c.process(ST, username);
                } catch (Exception ex) {
                    LOGGER.error("Exception: ", ex);
                    CampaignMain.cm.toUser(
                            "AM:Invalid Syntax: /" + task + " " + c.getSyntax(), username);
                }
                return;
            } // if the text is a command
        } // end while(more elements)
    } // end fromUser

    public SPlanet getPlanetFromPartialString(String PlanetName, String username) {

        // store matches so we can tell player if there's more than one
        int numMatches = 0;
        SPlanet theMatch = null;

        for (Planet currP : data.getAllPlanets()) {
            SPlanet p = (SPlanet) currP;

            // exact match
            if (p.getName().equals(PlanetName)) {
                return p;
            }

            // store all matches
            if (p.getName().startsWith(PlanetName)) {
                theMatch = p;
                numMatches++;
            }
        }

        // too many matches
        if (numMatches > 1) {
            if (username != null) {
                toUser(
                        "\""
                                + PlanetName
                                + "\" is not unique ["
                                + numMatches
                                + " matches]. Please be more specific.",
                        username);
            }
            return null;
        }

        if (numMatches == 0) {
            if (username != null) {
                toUser(
                        "Couldn't find a planet whose name begins with \""
                                + PlanetName
                                + "\". Try again.",
                        username,
                        true);
            }
            return null;
        }

        // only one match! send it back.
        return theMatch;
    }

    public void doSendHouseMail(SHouse h, String username, String text) {

        // send the text to all logged in players
        text = "(Housemail)" + username + ":" + text;
        this.doSendToAllOnlinePlayers(h, text, true);

        // then add it to the faction's log
        LOGGER.info(h.getName(), text.substring(11));
    }

    /**
     * Loop through all online players (all houses, all three duty modes) and send mail to those
     * players who are mods.
     */
    public void doSendModMail(String username, String text) {
        int sendCommandLevel = 0;
        int commandLevel = CampaignMain.cm.getServerCommands().get("MM").getExecutionLevel();
        int userLevel = 0;
        try {
            if (username.equalsIgnoreCase("NOTE")) {
                if (!CampaignMain.cm.getBooleanConfig(
                        "AllowLowerLevelUsersToSeeUpperLevelUsersDoings")) {
                    sendCommandLevel =
                            MWServ.getInstance()
                                    .getUserLevel(text.substring(0, text.indexOf(" ")).trim());
                } else {
                    sendCommandLevel = 100;
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Exception: ", ex);
        }

        // Note it to the logs
        LOGGER.info(username + ": " + text);
        text = "(Moderator Mail) " + username + ": " + text;
        
        // Query all players in each house who are not logged out
        for (House vh : data.getAllHouses()) {
            SHouse h = (SHouse) vh;
            int houseId = h.getId();
            
            List<SPlayer> onlinePlayers = HibernateUtil.fromTransaction(session -> 
                session.createQuery(
                    "FROM SPlayer WHERE myHouse.id = :houseId AND status != :loggedOutStatus", 
                    SPlayer.class)
                    .setParameter("houseId", houseId)
                    .setParameter("loggedOutStatus", SPlayer.STATUS_LOGGEDOUT)
                    .getResultList()
            );
            
            for (SPlayer p : onlinePlayers) {
                userLevel = MWServ.getInstance().getUserLevel(p.getName());
                if (userLevel >= commandLevel && userLevel >= sendCommandLevel) {
                    this.toUser(text, p.getName(), true);
                }
            }
        }
    }

    /**
     * After an error, loop through all online players and send text of the error to anyone who has
     * modmail access.
     */
    public void doSendErrLog(String text) {
        text = "(Error Log): " + text;
        Command command = CampaignMain.cm.getServerCommands().get("MM");
        
        // Query all players in each house who are not logged out
        for (House vh : data.getAllHouses()) {
            SHouse h = (SHouse) vh;
            int houseId = h.getId();
            
            List<SPlayer> onlinePlayers = HibernateUtil.fromTransaction(session -> 
                session.createQuery(
                    "FROM SPlayer WHERE myHouse.id = :houseId AND status != :loggedOutStatus", 
                    SPlayer.class)
                    .setParameter("houseId", houseId)
                    .setParameter("loggedOutStatus", SPlayer.STATUS_LOGGEDOUT)
                    .getResultList()
            );
            
            for (SPlayer p : onlinePlayers) {
                if (MWServ.getInstance().getUserLevel(p.getName()) >= command.getExecutionLevel()) {
                    this.toUser(text, p.getName(), true);
                }
            }
        }
    }

    /**
     * @return Returns the mechStats.
     */
    public Hashtable<String, MechStatistics> getMechStats() {
        return MechStats;
    }

    public void doProcessAutomaticReport(String s, String username) {

        /*
         * Format should be: Winner#DE#...Unit...#GY#...Units...#AL#...Units...
         */

        /*
         * return if the username isn't listed if (s.indexOf(username) == -1)
         * return;
         */

        // Now adays deds and Hosts actually report the game not the players.
        // So we need to check the winner if the winner is NULL due to a DRAW
        // Then check the name of the first player in the report string which
        // is the second element in a * delimited string
        // -Torren
        TreeSet<String> players = new TreeSet<String>();

        StringTokenizer report = new StringTokenizer(s, "#");
        SPlayer reporter = this.getPlayer(report.nextToken());

        while (report.hasMoreElements()) {
            StringTokenizer report2 = new StringTokenizer(report.nextToken(), "*");

            String test = report2.nextToken();

            // dont bother trying to process auto army or MechWarriors.
            if (test.equals("MW") || test.equals("-1")) {
                continue;
            }

            // keep parsing until we find a players name!
            while (report2.hasMoreTokens()) {
                SPlayer player = getPlayer(report2.nextToken());
                if (player != null) {
                    if (!players.contains(player.getName().toLowerCase())) {
                        players.add(player.getName().toLowerCase());
                    }

                    if (reporter == null) {
                        reporter = player;
                    }
                    break;
                }
            }
        }

        if (reporter == null) {
            LOGGER.error("reporter is null! " + s);
            return;
        }

        /*
         * If the player isn't in any ShortOperations, he obviously has no
         * standing to report. Tasks code used to sort winners and losers at
         * this point, but we handle that in the ShortResovler.
         */
        ShortOperation so = getOpsManager().getShortOpForPlayer(reporter);
        if (so == null) {
            return;
        }

        if (!so.validatePlayers(players)) {
            LOGGER.error("Unable to validate all players for: " + s);
            return;
        }

        if (so.hasPlayer(reporter)) {
            Operation o = getOpsManager().getOperation(so.getName());
            getOpsManager().resolveShortAttack(o, so, s);
            return;
        }
    } // end doProcessAutomaticReport

    /**
     * Method which pre-processes auto-disconnection info updates. Clients connected to a host send
     * these updates when a unit is removed from play - this does not necessarily mean the unit is
     * dead. It could have fled or been pushed from the field, etc. ClientThread weeds out observers
     * client side.
     */
    public void addInProgressUpdate(String s, String username) {

        // Return if user isn't an SPlayer.
        SPlayer reporter = this.getPlayer(username);
        if (reporter == null) {
            return;
        }

        // If the reporting player isnt in a game, toss it.
        ShortOperation so = getOpsManager().getShortOpForPlayer(reporter);
        if (so == null) {
            return;
        }

        // If the short operation has more than two players
        if (so.getAllPlayerNames().size() > 2) {
            return;
        }

        // now that we have a game for the player, pass the destruction
        // string along to the short operation for handling.
        so.addInProgressUpdate(s);
    }

    public CampaignData getData() {
        return data;
    }

    public List<MercHouse> getMercHouses() {
        List<MercHouse> result = new ArrayList<MercHouse>();

        for (House currH : data.getAllHouses()) {
            SHouse sh = (SHouse) currH;
            if (sh.isMercHouse()) {
                result.add((MercHouse) currH);
            }
        }
        return result;
    }

    /**
     * Login a player to the server. Called by login, enroll command and (most commonly) SignOn. If
     * we find that the player is already in a faction, leave things as they are. If the player is
     * not present in a house status hashtable, use this.getPlayer() to check the save queue and, if
     * necessary, read the player in from text. Any player who logs in should be put into the
     * Reserve list. If he is reconnecting, the SignOn command will pass him through a reconnection
     * check and clean up the various Operations threads, etc. Players with no account (null
     * this.getPlayer()) are also handled in SignOn, but we need to check there here as well in case
     * the player ignores the SignOn click-through and attempts to log in anyway.
     */
    public void doLoginPlayer(String username) {
        HibernateUtil.inTransaction(session -> {
            // Loop through the houses and make sure he's not already logged in
            for (House vh : data.getAllHouses()) {
                SHouse currH = (SHouse) vh;
                if (currH.isLoggedIntoFaction(username)) {
                    toUser(
                            "You are already logged in to " + currH.getColoredNameAsLink() + ".",
                            username,
                            true);
                    return;
                }
            }

            /*
             * He's not in a house. lets look in the save queue and pfiles. If the
             * getPlayer is null, extend an invitation to enroll (same as in
             * SignOn.java, for uniformity).
             */
            SPlayer toLogin = this.getPlayer(username);

            if (toLogin == null) {
                this.toUser(
                        "<font color=\"teal\"><br>---<br>"
                                + "It appears that you haven't signed up for this server's "
                                + "campaign.<br><a href=\"MEKWARS/c enroll\">Click here to get "
                                + "started.</a><br>---<br></font>",
                        username,
                        true);
                return;
            }

            /*
             * Now that we have a player who needs to be placed in a house. The
             * player holds a faction name in his .dat file, which is used to
             * bootstrap a link to the SHouse into SPlayer at load time. We may
             * assume that this data is valid (if not, we have much deeper problems
             * with the data we're using here) and put the player into the
             * approperiate faction. Note that the old MMNET code looped through the
             * houses until it found one that purported to "own" the player. This is
             * a pretty dramatic reversal of process, and not as OO-appropriate :-(
             */
            SHouse loginHouse = toLogin.getMyHouse();
            if (loginHouse == null) {
                toUser("    . Major problem. Report ASAP.", username, true);
                CampaignMain.cm.doSendModMail(
                        "NOTE",
                        toLogin.getName()
                                + " has a null login faction! Moving to "
                                + CampaignMain.cm.getConfig("NewbieHouseName"));
                loginHouse =
                        CampaignMain.cm.getHouseFromPartialString(
                                CampaignMain.cm.getConfig("NewbieHouseName"));
                toLogin.setMyHouse(loginHouse);
            }
            String s = loginHouse.doLogin(toLogin);

            /*
             * String returned from house includes motd, etc. The house performs one
             * last-ditch check to see if the player is already in the house and may
             * return a null if it finds the player present, despite the failure of
             * all of our previous location attempts.
             */
            if (s != null) {
                // send the login message/MOTD
                toUser(s, username, true);

                // Send the player his basic info (units, techs, etc)
                CampaignMain.cm.toUser("PS|" + toLogin.toString(true), username, false);

                if (isUsingAdvanceRepair()) {

                    if (!toLogin.hasRepairingUnits()) {
                        CampaignMain.cm.toUser(
                                "PL|UTT|" + toLogin.totalTechsToString(), username, false);
                        CampaignMain.cm.toUser(
                                "PL|UAT|" + toLogin.totalTechsToString(), username, false);
                    } else {
                        CampaignMain.cm.toUser(
                                "PL|UTT|" + toLogin.totalTechsToString(), username, false);
                        CampaignMain.cm.toUser(
                                "PL|UAT|" + toLogin.availableTechsToString(), username, false);
                    }
                }

                /*
                 * Player is logging in so clear their armies opps and send the
                 * player his army eligibilities.
                 */
                for (SArmy currA : toLogin.getArmies()) {
                    currA.getLegalOperations().clear();
                    CampaignMain.cm.getOpsManager().checkOperations(currA, false);
                }

                // send all currently online players to the one logging in
                StringBuilder result = new StringBuilder("PI|PL|");
                for (House vh : data.getAllHouses()) {
                    SHouse currH = (SHouse) vh;
                    List<SPlayer> playerList = 
                        session.createQuery(
                                    "SELECT COUNT(p) SPlayer p WHERE"
                                        + " :houseId = p.house_id AND status = :statusId",
                                        SPlayer.class)
                                .setParameter("houseId", currH.getId())
                                .setParameter("status", SPlayer.STATUS_LOGGEDOUT)
                                .getResultList();
                    for (SPlayer currP : playerList) {
                        result.append(getPlayerUpdateString(currP) + "|");
                    }
                }
                toUser(result.toString(), username, false);

                // Add the logging in player to everyone who is already online
                this.doSendToAllOnlinePlayers("PI|DA|" + getPlayerUpdateString(toLogin), false);

                /*
                 * Once the player is logged in, set his last-command-sent to the
                 * current time. This stops the idle-kicking code from immediately
                 * logging out players who've just come online and not yet sent any
                 * commands.
                 */
                toLogin.setLastTimeCommandSent(System.currentTimeMillis());
                toLogin.setLastOnline(System.currentTimeMillis());

                /*
                 * Check if Staff Member and send MMOTD if so.
                 */
                if (MWServ.getInstance().isModerator(username)) {
                    CampaignMain.cm.toUser(
                            "(Moderator Mail) Mod MOTD: " + CampaignMain.cm.getConfig("MMOTD"),
                            username);
                }

                /*
                 * INCREDIBLY BAD HACK! As player's sign into factions, get an IP
                 * and add it to the LOGGER. With the demise of nfc.log (removed
                 * from NFC2, which was grafted into MekWars), there is a need for a
                 * grepable iplog.0 to search for double accounts and re-
                 * entering/ban circumventing players. Despite the heinous way we
                 * draw the IP, this should work. @urgru 1.29.06 :-(
                 */
                LOGGER.info("Name: " + username + " IP: " + MWServ.getInstance().getIP(username));
                CampaignMain.cm.toUser("PL|SUD|1", username, false);
                CampaignMain.cm.toUser("PL|SHP|" + toLogin.buildHangarPenaltyString(), username, false);

                // Send him the Tick Counter
                try {
                    CampaignMain.cm.toUser(
                            "CC|NT|" + TickJob.millisecondsUntilNextFire() + "|" + false,
                            username,
                            false);
                } catch (Exception exception) {
                    LOGGER.catching(exception);
                }

                // Check for Christmas
                if (ChristmasHandler.getInstance().isItChristmas()) {
                    // Check if the user has received his Christmas Gifts
                    if (!ChristmasHandler.getInstance().userHasReceivedGifts(username)) {
                        // He needs his presents!!!
                        ChristmasHandler.getInstance().sendChristmasGifts(this.getPlayer(username));
                    } else {
                        // No presents for you!
                        // CampaignMain.cm.toUser("AM:You have already received presents", username,
                        // true);
                    }
                }
            }
        });
    } // end CampaignMain.doLogin(String userName)

    /**
     * Log a player out of the campaign. The CampaignMain portion of logout is markedly simpler than
     * login. All of the more complex code (like chickening and disconnection thread spinning) is
     * dealt with in SHouse. Note that all players who log out are inserted into the savePlayer hash
     * for removal. this.getPlayer() will retreive the memory resident SPlayer from the save queue
     * if the player returns before the purge.
     */
    public void doLogoutPlayer(
            String name) { // start Baruk Khazad! 20151110   created method so all old
        // doLogoutPlayer calls will continue to work without need for change
        doLogoutPlayer(name, true);
    }

    public void doLogoutPlayer(
            String name,
            Boolean bSavePlayerOrNot) { // Baruk Khazad! 20151110   added method parameter
        // bSavePlayerOrNot to allow for command.DeleteAccount to
        // skip the SavePlayer call

        // if the name is null or blank, return.
        if (name == null || name.trim().length() == 0) {
            return;
        }

        // if there is not player with the given name, return
        SPlayer toLogout = this.getPlayer(name);
        if (toLogout == null) {
            return;
        }

        /*
         * double check to make sure the SPlayer object does not reside in the
         * lost Souls hash this is incase someone connected but never logged
         * into thier house or never registered and enrolled.
         */
        releaseLostSoul(name);
        // set save, then log the player out of his house
        // start Baruk Khazad! 20151110  put IF wrapper around setSave() so deleted players can be
        // told to logout without being saved(which basically recreates their account
        if (bSavePlayerOrNot) {
            toLogout.setSave();
        }
        // end Baruk Khazad! 20151110
        toLogout.getMyHouse().doLogout(toLogout); // hacky.

        // clear the addon and send the new logged out status to all players
        this.doSendToAllOnlinePlayers("PI|CS|" + name + "|" + SPlayer.STATUS_LOGGEDOUT, false);
        toUser("[*] You've logged out of the campaign.", name, true);
    }

    public String getPlayerUpdateString(SPlayer player) {

        StringBuffer result = new StringBuffer();
        if (player == null) {
            return result.toString();
        }

        // Hide Reserve and Active Status
        int status = player.getDutyStatus();
        if (status == SPlayer.STATUS_RESERVE
                && Boolean.parseBoolean(getConfig("HideActiveStatus"))) {
            status = SPlayer.STATUS_ACTIVE;
        }

        result.append(player.getName());
        result.append("|");
        result.append(player.getExperience());
        result.append("#");
        if (Boolean.parseBoolean(getConfig("HideELO"))) {
            result.append("0");
        } else {
            result.append(player.getRatingRounded());
        }

        result.append("#");
        result.append(status);
        result.append("#");
        if (player.getFluffText().equals("")) {
            result.append(" #");
        } else {
            result.append(player.getFluffText());
            result.append("#");
        }

        result.append(player.getHouseFightingFor().getName());
        result.append("#");
        result.append(player.getMyHouse().isMercHouse());
        result.append("#");
        result.append(player.getSubFactionName());
        return result.toString();
    }

    /**
     * This sends status updates of Player p to all players
     *
     * @param p
     */
    public void sendPlayerStatusUpdate(SPlayer p, boolean sendToAll) {

        // get the player's actual status
        int realStatus = p.getDutyStatus();
        int sendStatus = realStatus;

        // if obfuscating active/deactive status, change sendstatus
        if (realStatus == SPlayer.STATUS_RESERVE
                && Boolean.parseBoolean(getConfig("HideActiveStatus"))) {
            sendStatus = SPlayer.STATUS_ACTIVE;
        }

        // send the obfuscated status to everyone, and real status to player
        if (sendToAll) {
            this.doSendToAllOnlinePlayers("PI|CS|" + p.getName() + "|" + sendStatus, false);
        }
        this.toUser("CS|" + realStatus, p.getName(), false);
    }

    /**
     * Get an SPlayer, by name. This searches the reserve, active and fighting hashes of all
     * factions until the player is found or factions are exhausted. If a player is not in a
     * faction, check the to-save hash. Its entirely possible that the player is already in memory,
     * but logged out and is awaiting a purge. If no matching player is found online, the server
     * will attempt to read one in from a text file. If even this fails, a null is returned. NOTE: A
     * player brought into memory using getPlayer is not automatically logged into his house.
     * Temporary loads (ex: commands targetted at offline players) will put the player directly into
     * the save queue, as if he was logged out. This is why the save queue is/must be searched prior
     * to* reading the text file.
     */
    public SPlayer getPlayer(String playerName) {
        // Fix for Draw games.
        if (playerName.equalsIgnoreCase("DRAW") || playerName.toUpperCase().startsWith("DRAW#")) {
            return null;
        }

        if (lostSouls.containsKey(playerName.toLowerCase())) {
            return lostSouls.get(playerName.toLowerCase());
        }

        Session session = HibernateUtil.getInstance().getCurrentSession();
        return session.createNamedQuery("SPlayer.findByName", SPlayer.class)
             .setParameter("name", playerName)
             .uniqueResult();
    }

    /**
     * Method which loads a player file from text. THIS SHOULD NOT BE USED.
     * CampaignMain.getPlayer(String name) will check to see if a player is already in memory, and
     * then call this loader if the player needs to be brought in from text. If you need to get a
     * player, always use .getPlayer(String name) instead. A player who is loaded is put into the
     * CampaignMain
     */
    private SPlayer loadPlayerFile(String name, boolean mute) {
        if (!name.startsWith("[Dedicated]") && !name.startsWith("War Bot")) {

            MekwarsFileReader dis = null;

            try {
                // log the load attempt & create readers
                LOGGER.info("Loading pfile for: " + name);

                File pFile = new File("./campaign/players/" + name.toLowerCase() + ".dat");

                if (!pFile.exists()) {
                    return null;
                }

                dis = new MekwarsFileReader(pFile);

                // create player from string read by dis
                SPlayer p = new SPlayer();
                String pString = dis.readLine();

                if (pString == null) {
                    return null;
                }

                p.fromString(pString);

                return p;
            } catch (FileNotFoundException fnf) {

                if (!name.toLowerCase().startsWith("nobody")
                        && !name.equals("SERVER")
                        && !name.toLowerCase().startsWith("war bot")
                        && !name.toLowerCase().startsWith("[dedicated]")
                        && !mute) {
                    LOGGER.error("could not find a pfile for " + name);
                    LOGGER.debug(fnf);
                    LOGGER.debug("could not find a pfile for " + name);
                }
                return null;
            } catch (Exception ex) {
                if (!mute) {
                    LOGGER.error("Unable to load pfile for {}", name, ex);
                }
                return null;
            } finally {
                // close the streams and return player
                try {
                    if (dis != null) {
                        dis.close();
                    }
                } catch (Exception ex) {
                    LOGGER.error("Exception: ", ex);
                }
            }
        }
        return null;
    }

    public void toUser(String txt, String username) {
        toUser(txt, username, true);
    }

    public void toUser(String txt, String username, boolean isChat) {
        if (isChat) {
            MWServ.getInstance().fromCampaignMod("CH|" + txt, username);
        } else {
            MWServ.getInstance().fromCampaignMod(txt, username);
        }
    }

    public void init() {
        LOGGER.info("SERVER STARTED");

        // Fill the commands Table
        Commands.put("ACCEPTATTACKFROMRESERVE", new AcceptAttackFromReserveCommand());
        Commands.put("ACCEPTCONTRACT", new AcceptContractCommand());
        Commands.put("ACTIVATE", new ActivateCommand());
        Commands.put("ADDLEADER", new AddLeaderCommand());
        Commands.put("ADDOMNIVARIANTMOD", new AddOmniVariantModCommand());
        Commands.put("ADDPARTS", new AddPartsCommand());
        Commands.put("ADDSONG", new AddSongCommand());
        Commands.put("ADDTRAIT", new AddTraitCommand());
        Commands.put("ADMINADDSERVEROPFLAGS", new AdminAddServerOpFlagsCommand());
        Commands.put("ADMINALLOWHOUSEDEFECTION", new AdminAllowHouseDefectionCommand());
        Commands.put("ADMINCALCULATEHOUSERANKINGS", new AdminCalculateHouseRankingsCommand());
        Commands.put("ADMINCHANGEFACTIONCONFIG", new AdminChangeFactionConfigCommand());
        Commands.put("ADMINCHANGEPLANETOWNER", new AdminChangePlanetOwnerCommand());
        Commands.put("ADMINCHANGESERVERCONFIG", new AdminChangeServerConfigCommand());
        Commands.put("ADMINCREATEFACTION", new AdminCreateFactionCommand());
        Commands.put("ADMINCREATEPLANET", new AdminCreatePlanetCommand());
        Commands.put("ADMINCREATEFACTORY", new AdminCreateFactoryCommand());
        Commands.put("ADMINCREATESOLARIS", new AdminCreateSolarisCommand());
        Commands.put("ADMINCREATETERRAIN", new AdminCreateTerrainCommand());
        Commands.put("ADMINDESTROYFACTION", new AdminDestroyFactionCommand());
        Commands.put("ADMINDESTROYFACTORY", new AdminDestroyFactoryCommand());
        Commands.put("ADMINDESTROYPLANET", new AdminDestroyPlanetCommand());
        Commands.put("ADMINDESTROYTERRAIN", new AdminDestroyTerrainCommand());
        Commands.put("ADMINDONATE", new AdminDonateCommand());
        Commands.put("ADMINEXCHANGEPLANETOWNERSHIP", new AdminExchangePlanetOwnershipCommand());
        Commands.put("ADMINGETUNITCOMPONENTS", new AdminGetUnitComponentsCommand());
        Commands.put("ADMINGRANTCOMPONENTS", new AdminGrantComponentsCommand());
        Commands.put("ADMINHOUSEPILOTS", new AdminHousePilotsCommand());
        Commands.put("ADMINHOUSESTATUS", new AdminHouseStatusCommand());
        Commands.put("ADMINLOCKCAMPAIGN", new AdminLockCampaignCommand());
        Commands.put("ADMINLOCKFACTORY", new AdminLockFactoryCommand());
        Commands.put("ADMINLISTANDREMOVEOMG", new AdminListAndRemoveOMGCommand());
        Commands.put("ADMINLISTHOUSEBANNEDAMMO", new AdminListHouseBannedAmmoCommand());
        Commands.put("ADMINLISTSERVERBANNEDAMMO", new AdminListServerBannedAmmoCommand());
        Commands.put("ADMINMOVEPLANET", new AdminMovePlanetCommand());
        Commands.put("ADMINPASSWORD", new AdminPasswordCommand());
        Commands.put("ADMINPLAYERSTATUS", new AdminPlayerStatusCommand());
        Commands.put("ADMINPURGEHOUSEBAYS", new AdminPurgeHouseBaysCommand());
        Commands.put("ADMINPURGEHOUSECONFIGS", new AdminPurgeHouseConfigsCommand());
        Commands.put(
                "ADMINRANDOMLYSETPLANETPRODUCTION", new AdminRandomlySetPlanetProductionCommand());
        Commands.put("ADMINRECALCHANGARBVMC", new AdminRecalcHangarBvCommandMC()); // @salient
        Commands.put("ADMINRELOADHOUSECONFIGS", new AdminReloadHouseConfigsCommand());
        Commands.put(
                "ADMINRELOADHTMLSANITIZERCONFIGS", new AdminReloadHTMLSanitizerConfigsCommand());
        Commands.put("ADMINRELOADSUPPORTUNITS", new AdminReloadSupportUnitsCommand());
        Commands.put("ADMINREMOVEALLFACTORIES", new AdminRemoveAllFactoriesCommand());
        Commands.put("ADMINREMOVEALLTERRAIN", new AdminRemoveAllTerrainCommand());
        Commands.put("ADMINREMOVEPLANETOWNERSHIP", new AdminRemovePlanetOwnershipCommand());
        Commands.put("ADMINREMOVESERVEROPFLAGS", new AdminRemoveServerOpFlagsCommand());
        Commands.put("ADMINREMOVEUNITSONMARKET", new AdminRemoveUnitsOnMarketCommand());
        Commands.put("ADMINRENAMEPLANET", new AdminRenamePlanetCommand());
        Commands.put("ADMINREQUESTBUILDTABLE", new AdminRequestBuildTableCommand());
        Commands.put("ADMINRESETFACTIONCOMPONENTS", new AdminResetFactionComponentsCommand());
        Commands.put("ADMINRESETHOUSERANKINGS", new AdminResetHouseRankingsCommand());
        Commands.put("ADMINRESETPLAYER", new AdminResetPlayerCommand());
        Commands.put(
                "ADMINRETURNPLANETSTOORIGINALOWNERS",
                new AdminReturnPlanetsToOriginalOwnersCommand());
        Commands.put("ADMINSAVE", new AdminSaveCommand());
        Commands.put("ADMINSAVEBLACKMARKETCONFIGS", new AdminSaveBlackMarketConfigsCommand());
        Commands.put("ADMINSAVECOMMANDLEVELS", new AdminSaveCommandLevelsCommand());
        Commands.put("ADMINSAVEFACTIONCONFIGS", new AdminSaveFactionConfigsCommand());
        Commands.put("ADMINSAVEPLANETSTOXML", new AdminSavePlanetsToXMLCommand());
        Commands.put("ADMINSAVESERVERCONFIGS", new AdminSaveServerConfigsCommand());
        Commands.put("ADMINSETBLACKMARKETSETTING", new AdminSetBlackMarketSettingCommand());
        Commands.put("ADMINSETCOMMANDLEVEL", new AdminSetCommandLevelCommand());
        Commands.put("ADMINSETHOMEWORLD", new AdminSetHomeWorldCommand());
        Commands.put("ADMINSETHOUSEABBREVIATION", new AdminSetHouseAbbreviationCommand());
        Commands.put("ADMINSETHOUSEFLUFILE", new AdminSetHouseFluFileCommand());
        Commands.put("ADMINSETHOUSEPLAYERCOLOR", new AdminSetHousePlayerColorCommand());
        Commands.put("ADMINSETHOUSETECHLEVEL", new AdminSetHouseTechLevelCommand());
        Commands.put("ADMINSETPLANETGRAVITY", new AdminSetPlanetGravityCommand());
        Commands.put("ADMINSETPLANETOPFLAGS", new AdminSetPlanetOpFlagsCommand());
        Commands.put("ADMINSETPLANETORIGINALOWNER", new AdminSetPlanetOriginalOwnerCommand());
        Commands.put("ADMINSETPLANETTEMPERATURE", new AdminSetPlanetTemperatureCommand());
        Commands.put("ADMINSETPLANETVACUUM", new AdminSetPlanetVacuumCommand());
        Commands.put("ADMINSETHOUSEAMMOBAN", new AdminSetHouseAmmoBanCommand());
        Commands.put("ADMINSETSERVERAMMOBAN", new AdminSetServerAmmoBanCommand());
        Commands.put("ADMINSETSERVERTARGETBAN", new AdminSetServerTargetBanCommand());
        Commands.put("ADMINSCRAP", new AdminScrapCommand());
        Commands.put("ADMINSPOOF", new AdminSpoofCommand());
        Commands.put("ADMINTERMINATEALL", new AdminTerminateAllCommand());
        Commands.put("ADMINTRANSFER", new AdminTransferCommand());
        Commands.put("ADMINUNLOCKCAMPAIGN", new AdminUnlockCampaignCommand());
        Commands.put("ADMINUNLOCKUNITSMC", new AdminUnlockUnitsCommandMC());
        Commands.put("ADMINUPDATECLIENTPARAM", new AdminUpdateClientParamCommand());
        Commands.put("ADMINUPDATEPLANETOWNERSHIP", new AdminUpdatePlanetOwnershipCommand());
        Commands.put("ADMINUPDATEDEFAULTPLAYERFLAGS", new AdminUpdateDefaultPlayerFlagsCommand());
        Commands.put("ADMINUPLOADBUILDTABLE", new AdminUploadBuildTableCommand());
        Commands.put("ADMINVIEWLOG", new AdminViewLogCommand());
        Commands.put("ALL", new ArmyLowerLimiterCommand());
        Commands.put("ANNOUNCE", new AnnounceCommand());
        Commands.put("AOFS", new ArmyOpForceSizeCommand());
        Commands.put("AUL", new ArmyUpperLimiterCommand());
        Commands.put("ATTACK", new AttackCommand());
        Commands.put("ATTACKFROMRESERVE", new AttackFromReserveCommand());
        Commands.put("AUTOFILLBLACKMARKETSETTING", new AutoFillBlackMarketSettingCommand());
        Commands.put("AUTOPLANETSTATUS", new AutoPlanetStatusCommand());
        Commands.put("BID", new BidCommand());
        Commands.put("BMSTATUS", new BMStatusCommand());
        Commands.put("BUILDTABLELIST", new BuildTableListCommand());
        Commands.put("BUILDTABLEVALIDATOR", new BuildTableValidatorCommand());
        Commands.put("BUYBAYS", new BuyBaysCommand());
        Commands.put("BUYPARTS", new BuyPartsCommand());
        Commands.put("BUYPILOTSFROMHOUSE", new BuyPilotsFromHouseCommand());
        Commands.put("CALCDIST", new CalcDistCommand());
        Commands.put("CAMPAIGNCONFIG", new CampaignConfigCommand());
        Commands.put("CANCELOFFER", new CancelOfferCommand());
        Commands.put("CHANGEHOUSECOLOR", new ChangeHouseColorCommand());
        Commands.put("CHANGENAME", new ChangeNameCommand());
        // Double CA
        Commands.put("CHECKATTACK", new CheckAttackCommand());
        Commands.put("CA", new CheckAttackCommand());
        // @Salient - used for discord bot
        Commands.put("CHATBOT", new ChatBotHelperCommand());
        //
        Commands.put("CHECK", new CheckCommand());
        Commands.put("CHECKARMYELIGIBILITY", new CheckArmyEligibilityCommand());
        Commands.put("CHECKARMYLINK", new CheckArmyLinkCommand());
        Commands.put("CHECKDIST", new CheckDistCommand());
        Commands.put("COMMENCEOPERATION", new CommenceOperationCommand());
        // Double CRL
        Commands.put("CREATEARMY", new CreateArmyCommand());
        Commands.put("CRA", new CreateArmyCommand());
        //
        Commands.put("CREATEARMYFROMMUL", new CreateArmyFromMulCommand());
        Commands.put("CREATECHATROOM", new CreateChatRoomCommand());
        Commands.put("CREATEMERCFACTION", new CreateMercFactionCommand());
        Commands.put("CREATESUBFACTION", new CreateSubFactionCommand());
        Commands.put("CREATEPILOT", new CreatePilotCommand());
        Commands.put("CREATEUNIT", new CreateUnitCommand());
        Commands.put("DEACTIVATE", new DeactivateCommand());
        Commands.put("DECLINEATTACKFROMRESERVE", new DeclineAttackFromReserveCommand());
        Commands.put("DEFECT", new DefectCommand());
        Commands.put("DEFEND", new DefendCommand());
        Commands.put("DELETEACCOUNT", new DeleteAccountCommand());
        Commands.put("DEMOTEPILOT", new DemotePilotCommand());
        Commands.put("DEMOTEPLAYER", new DemotePlayerCommand());
        Commands.put("DIRECTSELLUNIT", new DirectSellUnitCommand());
        Commands.put(
                "DISPLAYPLAYERPERSONALPILOTQUEUE", new DisplayPlayerPersonalPilotQueueCommand());
        Commands.put("DISPLAYUNITREPAIRJOBS", new DisplayUnitRepairJobsCommand());
        Commands.put("DONATE", new DonateCommand());
        Commands.put("DONATEPILOT", new DonatePilotCommand());
        Commands.put("EC", new EmojiCommand()); // @salient
        // Double EHM
        Commands.put("EHM", new EmployeeHouseMailCommand());
        Commands.put("EMPLOYEEHOUSEMAIL", new EmployeeHouseMailCommand());
        //
        Commands.put("ENDCHRISTMAS", new EndChristmasCommand());
        Commands.put("ENROLL", new EnrollCommand());
        // Double EXU
        Commands.put("EXCHANGEUNIT", new ExchangeUnitCommand());
        Commands.put("EXU", new ExchangeUnitCommand());
        Commands.put("EXM", new ExchangeUnitCommand());
        // Exchange Pilots
        Commands.put("EXCHANGEPILOTINUNIT", new ExchangePilotInUnitCommand());
        Commands.put("EXP", new ExchangePilotInUnitCommand());
        Commands.put("FACTION", new HouseCommand()); // alias for house command
        Commands.put("FACTIONLEADERFLUFF", new FactionLeaderFluffCommand());
        Commands.put("FLF", new FactionLeaderFluffCommand());
        Commands.put("FACTIONLEADERMUTE", new FactionLeaderMuteCommand());
        Commands.put("FLM", new FactionLeaderMuteCommand());
        Commands.put("FINDCP", new FindContestedPlanetsCommand()); // BarukKahzad 20151129
        Commands.put("FIRETECHS", new FireTechsCommand());
        Commands.put("FIXAMMO", new FixAmmoCommand());
        Commands.put("FLUFF", new FluffCommand());
        Commands.put("FORCEDDEFECT", new ForcedDefectCommand());
        Commands.put("FORCEUPDATE", new ForceUpdateCommand());
        Commands.put("GAMES", new GamesCommand());
        Commands.put("GETCOMPONENTCONVERSION", new GetComponentConversionCommand());
        Commands.put("GETFACTIONCONFIGS", new GetFactionConfigsCommand());
        Commands.put("GETMODLOG", new GetModLogCommand());
        Commands.put("GETOPS", new GetOpsCommand());
        Commands.put("GETPLAYERUNITS", new GetPlayerUnitsCommand());
        Commands.put("GETSERVERMEGAMEKGAMEOPTIONS", new GetServerMegaMekGameOptionsCommand());
        Commands.put("GETSERVEROPFLAGS", new GetServerOpFlagsCommand());
        Commands.put("GOOSE", new GooseCommand());
        Commands.put("GRANTEXP", new GrantEXPCommand());
        Commands.put("GRANTINFLUENCE", new GrantInfluenceCommand());
        Commands.put("GRANTMONEY", new GrantMoneyCommand());
        Commands.put("GRANTREWARD", new GrantRewardCommand());
        Commands.put("GRANTTECHPOINTS", new GrantTechPointsCommand());
        Commands.put("GRANTTECHS", new GrantTechsCommand());
        Commands.put("HARDTERMINATE", new HardTerminateCommand());
        Commands.put("HIREANDMAINTAIN", new HireAndMaintainHelper());
        Commands.put("HIREANDREQUESTNEW", new HireAndRequestNewHelper());
        Commands.put("HIREANDREQUESTUSED", new HireAndRequestUsedHelper());
        Commands.put("HIRETECHS", new HireTechsCommand());
        Commands.put("HOUSE", new HouseCommand());
        Commands.put("HOUSECONTRACTS", new HouseContractsCommand());
        // Double HM
        Commands.put("HOUSEMAIL", new HouseMailCommand());
        Commands.put("HM", new HouseMailCommand());
        //
        Commands.put("HOUSERANKING", new HouseRankingCommand());
        Commands.put("HOUSESTATUS", new HouseStatusCommand());
        // Double IC
        Commands.put("INCHARACTER", new InCharacterCommand());
        Commands.put("IC", new InCharacterCommand());
        Commands.put("INVIS", new InvisCommand());
        Commands.put("ISITCHRISTMAS", new IsItChristmasCommand());
        // ISS
        Commands.put("ISSTATUS", new ISStatusCommand()); // legace commands for
        // the client
        Commands.put("ISS", new ISStatusCommand());
        Commands.put("US", new ISStatusCommand());
        Commands.put("UNIVERSESTATUS", new ISStatusCommand());
        //
        Commands.put("JOINATTACK", new JoinAttackCommand());
        Commands.put("LASTONLINE", new LastOnlineCommand());
        Commands.put("LINKUNIT", new LinkUnitCommand());
        Commands.put("LISTCOMMANDS", new ListCommandsCommand());
        Commands.put("LISTMULS", new ListMulsCommand());
        Commands.put("LISTMULTIPLAYERGROUPS", new ListMultiPlayerGroupsCommand());
        Commands.put("LISTSERVEROPFLAGS", new ListServerOpFlagsCommand());
        Commands.put("LISTSUBFACTIONS", new ListSubFactionCommand());
        Commands.put("LOGIN", new LoginCommand());
        Commands.put("LOGOUT", new LogoutCommand());
        // Double MStatus
        Commands.put("MERCSTATUS", new MercStatusCommand());
        Commands.put("MSTATUS", new MercStatusCommand());
        Commands.put("MMOTD", new MMOTDCommand());
        //
        // Double MM
        Commands.put("MODERATORMAIL", new ModeratorMailCommand());
        Commands.put("MM", new ModeratorMailCommand());
        //
        Commands.put("MODDEACTIVATE", new ModDeactivateCommand());
        Commands.put("MODGAMES", new ModGamesCommand());
        Commands.put("MODFULLREPAIR", new ModFullRepairCommand());
        Commands.put("MODLOG", new ModLogCommand());
        Commands.put("MODNOPLAY", new ModNoPlayCommand());
        Commands.put("MODREFRESHFACTORY", new ModRefreshFactoryCommand());
        Commands.put("MODTERMINATE", new ModTerminateCommand());
        Commands.put("MOTD", new MOTDCommand());
        Commands.put("MYBIDS", new MyBidsCommand());
        Commands.put("MYSTATUS", new MyStatusCommand());
        Commands.put("MYVOTES", new MyVotesCommand());
        Commands.put("NAMEARMY", new NameArmyCommand());
        Commands.put("NAMEPILOT", new NamePilotCommand());
        Commands.put("NOPLAY", new NoPlayCommand());
        Commands.put("NOTIFYFIGHTING", new NotifyFightingCommand());
        Commands.put("OFFERCONTRACT", new OfferContractCommand());
        Commands.put("PLANET", new PlanetCommand());
        Commands.put("PLAYERLOCKARMY", new PlayerLockArmyCommand());
        Commands.put("PLAYERS", new PlayersCommand());
        Commands.put("PLAYERUNLOCKARMY", new PlayerUnlockArmyCommand());
        Commands.put("PROMOTEPLAYER", new PromotePlayerCommand());
        Commands.put("PROMOTEPILOT", new PromotePilotCommand());
        Commands.put("PURCHASEFACTORY", new PurchaseFactoryCommand());
        Commands.put("QUIRKCHECK", new QuirkCheckCommand()); // @salient
        Commands.put("RANGE", new RangeCommand());
        Commands.put("RECALL", new RecallCommand());
        Commands.put("RECALLBID", new RecallBidCommand());
        Commands.put("REPOD", new RepodCommand());
        Commands.put("REPORTSTATUSMC", new ReportStatusMC()); // @salient
        Commands.put("REFRESHFACTORY", new RefreshFactoryCommand());
        Commands.put("REFUSECONTRACT", new RefuseContractCommand());
        Commands.put("RELOADALLAMMO", new ReloadAllAmmoCommand());
        Commands.put("REMOVEANDADDNOPLAY", new RemoveAndAddNoPlayHelper());
        // Double RML
        Commands.put("REMOVEARMY", new RemoveArmyCommand());
        Commands.put("RMA", new RemoveArmyCommand());
        //
        Commands.put("REMOVEFACTIONPILOT", new RemoveFactionPilotCommand());
        Commands.put("REMOVELEADER", new RemoveLeaderCommand());
        Commands.put("REMOVEPARTS", new RemovePartsCommand());
        Commands.put("REMOVEPILOT", new RemovePilotCommand());
        Commands.put("REMOVESONG", new RemoveSongCommand());
        Commands.put("REMOVESUBFACTION", new RemoveSubFactionCommand());
        Commands.put("REMOVETRAIT", new RemoveTraitCommand());
        Commands.put("REMOVEVOTE", new RemoveVoteCommand());
        Commands.put("REPAIRUNIT", new RepairUnitCommand());
        Commands.put("REQUEST", new RequestCommand());
        Commands.put("REQUESTBUILDTABLE", new RequestBuildTableCommand());
        Commands.put("REQUESTDONATED", new RequestDonatedCommand());
        Commands.put("REQUESTOPERATIONSETTINGS", new RequestOperationSettingsCommand());
        Commands.put("REQUESTSERVERMAIL", new RequestServerMailCommand());
        Commands.put("REQUESTSUBFACTIONPROMOTION", new RequestSubFactionPromotionCommand());
        Commands.put("RESEARCHTECHLEVEL", new ResearchTechLevelCommand());
        Commands.put("RESEARCHUNIT", new ResearchUnitCommand());
        Commands.put(
                "RESETFREEMEKS", new AdminResetFreeMeksCommand()); // @Salient added for free build
        Commands.put("RESTARTREPAIRTHREAD", new RestartRepairThreadCommand());
        Commands.put("RETRIEVEALLOPERATIONS", new RetrieveAllOperationsCommand());
        Commands.put("RETRIEVEOPERATION", new RetrieveOperationCommand());
        Commands.put("RETRIEVEMUL", new RetrieveMulCommand());
        Commands.put("RETRIEVEALLMULS", new RetrieveAllMulsCommand());
        Commands.put("RETIREPILOT", new RetirePilotCommand());
        Commands.put("SALVAGEUNIT", new SalvageUnitCommand());
        Commands.put("SAVETOJSON", new SPlayerToJsonCommand()); // @salient - for discord bot
        Commands.put("SCRAP", new ScrapCommand());
        Commands.put("SENDCLIENTDATA", new SendClientDataCommand());
        Commands.put("SELFPROMOTE", new SelfPromoteCommand()); // @salient - for subfactions
        Commands.put("SELL", new SellCommand());
        Commands.put("SELLBAYS", new SellBaysCommand());
        Commands.put("SENDTOMISC", new SendToMiscCommand());
        Commands.put("SERVERVERSION", new ServerVersionCommand());
        Commands.put("SERVERGAMEOPTIONS", new ServerGameOptionsCommand());
        Commands.put("SETADVANCEDPLANETTERRAIN", new SetAdvancedPlanetTerrainCommand());
        Commands.put("SETAUTOEJECT", new SetAutoEjectCommand());
        Commands.put("SETAUTOREORDER", new SetAutoReorderCommand());
        Commands.put("SETCLIENTVERSION", new SetClientVersionCommand());
        Commands.put("SETCOMPONENTCONVERSION", new SetComponentConversionCommand());
        Commands.put("SETEDGESKILLS", new SetEdgeSkillsCommand());
        Commands.put("SETELO", new SetEloCommand());
        Commands.put(
                "SETFACTIONTOFACTIONREWARDPOINTMULTIPLIER",
                new SetFactionToFactionRewardPointMultiplierCommand());
        Commands.put("SETHOUSEBASEPILOTSKILLS", new SetHouseBasePilotSkillsCommand());
        Commands.put("SETHOUSEBASEPILOTINGSKILLS", new SetHouseBasePilotingSkillsCommand());
        Commands.put("SETHOUSELOGO", new SetHouseLogoCommand());
        Commands.put("SETHOUSECONQUER", new SetHouseConquerCommand());
        Commands.put("SETOPERATION", new SetOperationCommand());
        Commands.put("SETMAINTAINED", new SetMaintainedCommand());
        Commands.put("SETMMOTD", new SetMMOTDCommand());
        Commands.put("SETMOTD", new SetMOTDCommand());
        Commands.put("SETMULTIPLAYERGROUP", new SetMultiPlayerGroupCommand());
        Commands.put("SETMYLOGO", new SetMyLogoCommand());
        Commands.put("SETPLANETCONQUER", new SetPlanetConquerCommand());
        Commands.put("SETPLANETCONQUERPOINTS", new SetPlanetConquerPointsCommand());
        Commands.put("SETPLANETMINOWNERSHIP", new SetPlanetMinOwnerShipCommand());
        Commands.put("SETPLANETWAREHOUSE", new SetPlanetWareHouseCommand());
        Commands.put("SETPLANETCOMPPRODUCTION", new SetPlanetCompProductionCommand());
        Commands.put("SETPLAYERFLAGS", new SetPlayerFlagsCommand());
        Commands.put("SETSUBFACTIONCONFIG", new SetSubFactionConfigCommand());
        Commands.put("SETTARGETSYSTEM", new SetTargetSystemCommand());
        Commands.put("SETUNITAMMO", new SetUnitAmmoCommand());
        Commands.put("SETUNITAMMOBYCRIT", new SetUnitAmmoByCritCommand());
        Commands.put("SETUNITBURST", new SetUnitBurstCommand());
        Commands.put("SETUNITCOMMANDER", new SetUnitCommanderCommand());
        Commands.put("SETUNMAINTAINED", new SetUnmaintainedCommand());
        // Double ShowToHouse
        Commands.put("SHOWTOHOUSE", new ShowToHouseCommand());
        Commands.put("STH", new ShowToHouseCommand());
        Commands.put("SIMPLEREPAIR", new SimpleRepairCommand());
        // Double SingASong
        Commands.put("SINGASONG", new SingASongCommand());
        Commands.put("SAS", new SingASongCommand());
        // @Salient for sol free build option
        Commands.put("SOLCREATEUNIT", new FreeBuildCreateUnitCommand());
        Commands.put("SOLDELETEUNIT", new SolDeleteUnitCommand());
        Commands.put("STARTCHRISTMAS", new StartChristmasCommand());
        Commands.put("STOPREPAIRJOB", new StopRepairJobCommand());
        Commands.put("STRIPALLPARTSCACHE", new StripAllPartsCacheCommand());
        Commands.put("STRIPUNITS", new StripUnitsCommand());
        Commands.put("TERMINATE", new TerminateCommand());
        Commands.put("TERMINATECONTRACT", new TerminateContractCommand());
        Commands.put("TICK", new TickCommand());
        Commands.put("TOGGLEARMYDISABLED", new ToggleArmyDisabledCommand());
        Commands.put("TOUCH", new TouchCommand());
        Commands.put("TRANSFERMONEY", new TransferMoneyCommand());
        Commands.put("TRANSFERPILOT", new TransferPilotCommand());
        Commands.put("TRANSFERUNIT", new TransferUnitCommand());
        Commands.put("TRANSFERINFLUENCE", new TransferInfluenceCommand()); // @salient
        Commands.put("TRANSFERREWARDPOINTS", new TransferRewardPointsCommand());
        Commands.put("UPDATEDISCORDINFO", new UpdateDiscordInfoCommand());
        Commands.put("UPDATEOPERATIONS", new UpdateOperationsCommand());
        Commands.put("UPDATESERVERUNITSCACHE", new UpdateServerUnitsCacheCommand());
        Commands.put("UPLOADMUL", new UploadMulCommand());
        Commands.put("UNEMPLOYEDMERCS", new UnemployedMercsCommand());
        Commands.put("UNENROLL", new UnenrollCommand());
        Commands.put("UNITPOSITION", new UnitPositionCommand());
        Commands.put("UNLOCKLANCES", new UnlockLancesCommand());
        Commands.put("USEREWARDPOINTS", new UseRewardPointsCommand());
        Commands.put("USEINFLUENCE", new UseInfluenceCommand());
        Commands.put("VIEWFACTIONPARTSCACHE", new ViewFactionPartsCacheCommand());
        Commands.put("VIEWPLAYERPARTS", new ViewPlayerPartsCommand());
        Commands.put("VIEWPLAYERPERSONALPILOTQUEUE", new ViewPlayerPersonalPilotQueueCommand());
        Commands.put("VIEWPLAYERUNIT", new ViewPlayerUnitCommand());
        Commands.put("VOTE", new VoteCommand());

        // Old / comamds move to be usable by /c or /
        Commands.put("AM", new ServerAnnouncementCommand());
        Commands.put("SA", new ServerAnnouncementCommand());
        Commands.put("SERVERANNOUNCEMENT", new ServerAnnouncementCommand());
        Commands.put("BAN", new BanCommand());
        Commands.put("BANIP", new BanIPCommand());
        Commands.put("BANLIST", new BanListCommand());
        Commands.put("COLOR", new ColorCommand());
        Commands.put("COLOUR", new ColorCommand());
        Commands.put("CONFIG", new ConfigCommand());
        Commands.put("GETSAVEDMAIL", new GetSavedMailCommand());
        Commands.put("IGNORE", new IgnoreCommand());
        Commands.put("IGNORELIST", new IgnoreListCommand());
        Commands.put("IPLIST", new IPListCommand());
        Commands.put("KICK", new KickCommand());
        Commands.put("MAIL", new MailCommand());
        Commands.put("ME", new MeCommand());
        Commands.put("ROLL", new RollCommand());
        Commands.put("REGISTER", new RegisterCommand());
        Commands.put("SHUTDOWN", new ShutdownCommand());
        Commands.put("SETSMOTD", new SetSMOTDCommand());
        Commands.put("SIGNOFF", new SignOffCommand());
        Commands.put("SMOTD", new SMOTDCommand());
        Commands.put("UNBAN", new UnBanCommand());
        Commands.put("UNBANIP", new UnBanIPCommand());

        // command for testing
        Commands.put("CODETEST", new CodeTestCommand());
        // ok we've put all the commands in the command hash now lets set the
        // levels
        try {
            MekwarsFileReader dis = new MekwarsFileReader("./data/commands/commands.dat");
            while (dis.ready()) {
                StringTokenizer command = new StringTokenizer(dis.readLine(), "#");
                String commandName = command.nextToken();
                if (Commands.containsKey(commandName)) {
                    (Commands.get(commandName))
                            .setExecutionLevel(Integer.parseInt(command.nextToken()));
                }
            }
            dis.close();
        } catch (Exception ex) {
            LOGGER.error("Unable to find commands.dat. Continuing with defaults in place");
            TreeMap<String, Command> commandTable =
                    new TreeMap<String, Command>(cm.getServerCommands());
            PrintStream p = null;
            try {

                File fp = new File("./data/commands");
                if (!fp.exists()) {
                    fp.mkdir();
                }

                FileOutputStream out = new FileOutputStream("./data/commands/commands.dat");
                p = new PrintStream(out);

                for (String commandName : commandTable.keySet()) {

                    Command commandMethod = CampaignMain.cm.getServerCommands().get(commandName);
                    if (commandMethod == null) {
                        continue;
                    }
                    p.println(commandName.toUpperCase() + "#" + commandMethod.getExecutionLevel());
                }
            } catch (Exception ex1) {
                LOGGER.error(ex1);
                LOGGER.error("Unable to save command levels");
            } finally {
                if (p != null) {
                    p.close();
                }
            }
        }

        // Is the server data already there? (config files)? if not, create one
        if (data.getAllPlanets().size() > 0 && data.getAllHouses().size() > 0) {
            return;
        }

        // No Planets Data yet? Parse XML and create world.
        if (data.getAllPlanets().size() == 0) {

            // First, clear out the factions' initialrankings.
            for (House house : data.getAllHouses()) {
                SHouse shouse = (SHouse) house;
                shouse.setInitialHouseRanking(0);
            }

            HashMap<Integer, Integer> solFlu = new HashMap<Integer, Integer>();
            solFlu.put(
                    CampaignMain.cm
                            .getHouseFromPartialString(
                                    CampaignMain.cm.getConfig("NewbieHouseName"), null)
                            .getId(),
                    100);
            SPlanet newbieP = new SPlanet("Solaris VII", new Influences(solFlu), 0, -3, -2);
            if (data.getPlanetByName("Solaris VII") == null) {
                HibernateUtil.inTransaction(
                        session -> {
                            addPlanet(session, newbieP);
                            CampaignMain.cm
                                    .getHouseFromPartialString(
                                            CampaignMain.cm.getConfig("NewbieHouseName"), null)
                                    .addPlanet(newbieP);
                        });
            }
        }

        // Save it on startup
        toFile();
    }

    public void addHouse(SHouse s) {
        data.addHouse(s);
    }

    public void addPlanet(Session session, SPlanet planet) {
        if (planet.getOriginalOwner().isEmpty()) {
            if (planet.getOwner() == null) {
                planet.setOriginalOwner(cm.getConfig("NewbieHouseName"));
            }
            planet.setOriginalOwner(planet.getOwner().getName());
        }
        session.persist(planet);
    }

    public synchronized void userRoll(String text, String username) {
        // added by VEGETA 2/8/2003
        int dice = 2;
        int sides = 6;
        int total = 0;
        int roll = 0;
        String x = "";

        if (text.trim().length() > 0) {

            StringTokenizer ST = new StringTokenizer(text, "d");
            try {
                if (ST.hasMoreElements()) {
                    x = (String) ST.nextElement();
                    dice = Integer.parseInt(x.trim());
                }
                if (ST.hasMoreElements()) {
                    x = (String) ST.nextElement();
                    sides = Integer.parseInt(x.trim());
                }

            } catch (NumberFormatException ex) {
                toUser("AM:/roll: error parsing arguments.", username, true);
                return;
            } catch (StringIndexOutOfBoundsException ex) {
                toUser("AM:/roll: error parsing arguments.", username, true);
                return;
            }
        }

        if (dice < 1 || sides < 2) {
            this.doSendToAllOnlinePlayers(
                    "AM:" + username + " loves the smell of napalm in the morning.", true);
            return;
        }

        if (dice > 20 || sides > 100) {
            this.doSendToAllOnlinePlayers("AM:" + username + " is a stupid haxx0r!", true);
            return;
        }

        StringBuilder diceBuffer = new StringBuilder();

        for (int i = 0; i < dice; i++) {
            // roll = random.nextInt(sides) + 1;
            roll = cm.getRandomNumber(sides) + 1;
            total += roll;

            // for one die, we're all set
            if (dice < 2) {
                diceBuffer.append(roll);
                continue;
            }

            // 2+ dice, use commas and "and"
            if (i < dice - 1) {
                diceBuffer.append(roll);
                diceBuffer.append(", ");
            } else {
                diceBuffer.append("and ");
                diceBuffer.append(roll);
            }
        }
        if (text != "") {
            this.doSendToAllOnlinePlayers(
                    "AM:"
                            + username
                            + " rolled "
                            + diceBuffer
                            + " for a total of "
                            + total
                            + ", using "
                            + text
                            + ".",
                    true);
        } else {
            this.doSendToAllOnlinePlayers(
                    "AM:"
                            + username
                            + " rolled "
                            + diceBuffer
                            + " for a total of "
                            + total
                            + ", using 2d6.",
                    true);
        }
    }

    public void addMechStat(
            String filename, int mechSize, int gamePlayed, int gameWon, int scrapped) {
        addMechStat(filename, mechSize, gamePlayed, gameWon, scrapped, 0);
    }

    public void addMechStat(
            String filename,
            int mechSize,
            int gamePlayed,
            int gameWon,
            int scrapped,
            int destroyed) {
        MechStatistics mechStatistics = null;
        if (MechStats.get(filename) == null) {
            mechStatistics = new MechStatistics(filename, mechSize);
        } else {
            mechStatistics = MechStats.get(filename);
        }
        mechStatistics.setOriginalBV(SUnit.loadMech(filename).calculateBattleValue());

        mechStatistics.addStats(gamePlayed, gameWon, mechStatistics.getOriginalBV());
        mechStatistics.setTimesScrapped(mechStatistics.getTimesScrapped() + scrapped);
        mechStatistics.setTimesDestroyed(mechStatistics.getTimesDestroyed() + destroyed);
        MechStats.put(filename, mechStatistics);
    }

    /**
     * Private method that sends KI| (kick) commands to idle players. Broken into a seperate method
     * to reduce code repetitiveness in slice().
     */
    private void checkAndRemoveIdle(SPlayer player, long maxIdleTime) {
        // dont boot mods
        if (MWServ.getInstance().isModerator(player.getName())) {
            return;
        }

        // if he's already logged out, who cares?
        // Well, it turns out that some people do care - see RFE 2126734
        if (player.getDutyStatus() <= SPlayer.STATUS_LOGGEDOUT
                && !CampaignMain.cm.getBooleanConfig("DisconnectIdleUsers")) {
            return;
        }

        // redundant, but never boot fighting players
        if (player.getDutyStatus() == SPlayer.STATUS_FIGHTING) {
            return;
        }

        // reserve or active player. check his times.
        // NOTE: KI| command is actualy campaign logout. GBB| a disco/kill.
        if (System.currentTimeMillis() - player.getLastTimeCommandSent() > maxIdleTime) {
            CampaignMain.cm.toUser(
                    "You were logged out by the server (excessive idle time).",
                    player.getName(),
                    true);
            if (!CampaignMain.cm.getBooleanConfig("DisconnectIdleUsers")) {
                CampaignMain.cm.toUser("KI|idler", player.getName(), false);
            } else {
                CampaignMain.cm.toUser("PL|GBB|idler", player.getName(), false);
            }
        }
    }

    /**
     * Slicer. Called by SliceThread @ the end of its config.txt defined wait duration. Gives
     * influence to active players, checks for (and kicks) idle players, and saves player files.
     * Slices are generally much shorter than ticks, and involve players and player data much more
     * heavily than factions/high-end campaign structures. This is the exact opposite of the .tick()
     * (see below).
     */
    public synchronized void slice(int sliceID) {
        HibernateUtil.inTransaction(session -> {
            // write log header
            LOGGER.info("Slice #" + sliceID + " Started: " + System.currentTimeMillis());

            WhoToHTML who = new WhoToHTML(CampaignMain.cm.getConfig("HTMLWhoPath"));

            // loop through all houses
            for (House vh : data.getAllHouses()) {
                SHouse currH = (SHouse) vh;
                // fahr
                LOGGER.info("Slice #" + sliceID + " house: " + currH.getName());

                // load max idle time, converted to ms
                long maxIdleTime = Long.parseLong(CampaignMain.cm.getConfig("MaxIdleTime")) * 60000;

                LOGGER.info("Slice #" + sliceID + " house: " + currH.getName() + " reservePlayers");
                List<SPlayer> reservePlayers = 
                    session.createQuery(
                                "SELECT COUNT(p) SPlayer p WHERE"
                                    + " :houseId = p.house_id AND status = :statusId",
                                    SPlayer.class)
                            .setParameter("houseId", currH.getId())
                            .setParameter("status", SPlayer.STATUS_RESERVE)
                            .getResultList();
                for (SPlayer currP : reservePlayers) {
                    if (maxIdleTime > 0) {
                        try {
                            checkAndRemoveIdle(currP, maxIdleTime);
                        } catch (Exception ex) {
                            LOGGER.info(
                                    "Slice #"
                                            + sliceID
                                            + " house: "
                                            + currH.getName()
                                            + " reservePlayer: "
                                            + currP.getName());
                            LOGGER.error("Exception: ", ex);
                        }
                    }
                    if (!currP.isInvisible()) {
                        who.addPlayer(currP);
                    }
                }

                /*
                 * Active players get the whole shebang - influence addition,
                 * maintainance, and an idle check (if enabled).
                 */
                LOGGER.info("Slice #" + sliceID + " house: " + currH.getName() + " ActivePlayers");
                List<SPlayer> activePlayers = 
                    session.createQuery(
                                "SELECT COUNT(p) SPlayer p WHERE"
                                    + " :houseId = p.house_id AND status = :statusId",
                                    SPlayer.class)
                            .setParameter("houseId", currH.getId())
                            .setParameter("status", SPlayer.STATUS_ACTIVE)
                            .getResultList();
                for (SPlayer currP : activePlayers) {
                    try {
                        currP.doMaintainance();
                        if (!currP.isInvisible()) {
                            who.addPlayer(currP);
                        }
                        if (maxIdleTime > 0) {
                            checkAndRemoveIdle(currP, maxIdleTime);
                        }
                    } catch (Exception ex) {
                        LOGGER.error("Exception: ", ex);
                        LOGGER.info(
                                "Slice #"
                                        + sliceID
                                        + " house: "
                                        + currH.getName()
                                        + " activePlayer: "
                                        + currP.getName());
                    }
                }

                // fighters only have maint. they get influence grants post-game.
                LOGGER.info("Slice #" + sliceID + " house: " + currH.getName() + " fightingPlayers");
                List<SPlayer> fightingPlayers = 
                    session.createQuery(
                                "SELECT COUNT(p) SPlayer p WHERE"
                                    + " :houseId = p.house_id AND status = :statusId",
                                    SPlayer.class)
                            .setParameter("houseId", currH.getId())
                            .setParameter("status", SPlayer.STATUS_FIGHTING)
                            .getResultList();
                for (SPlayer currP : fightingPlayers) {
                    try {
                        currP.doMaintainance();
                        if (!currP.isInvisible()) {
                            who.addPlayer(currP);
                        }
                        // People fighting are always up to date
                        if (maxIdleTime > 0) {
                            currP.setLastTimeCommandSent(System.currentTimeMillis() + maxIdleTime);
                        }
                    } catch (Exception ex) {
                        LOGGER.error("Exception: ", ex);
                        LOGGER.info(
                                "Slice #"
                                        + sliceID
                                        + " house: "
                                        + currH.getName()
                                        + " fightingPlayer: "
                                        + currP.getName());
                    }
                }
            } // end all houses

            if (CampaignMain.cm.getBooleanConfig("HTMLOUTPUT")) {
                who.outputHTML();
            }
            who = null;

            // check to see if we should save on this slice
            int saveOnSlice = CampaignMain.cm.getIntegerConfig("SaveEverySlice");
            if (saveOnSlice < 1) {
                saveOnSlice = 1;
            }
            if (sliceID % saveOnSlice == 0) {
                LOGGER.info("Slice #" + sliceID + " savePlayers()");
                try {
                    savePlayers(); // Once all of the saving is done clear
                } catch (Exception ex) {
                    LOGGER.error("Exception: ", ex);
                    LOGGER.info("Slice #" + sliceID + " savePlayers() failed");
                } // everything for the next tick.
                LOGGER.info("Slice #" + sliceID + " saveTopUnitID()");
                try {
                    saveTopUnitID();
                } catch (Exception ex) {
                    LOGGER.error("Exception: ", ex);
                    LOGGER.info("Slice #" + sliceID + " saveTopUnitID() failed");
                }
            }

            // write log header
            LOGGER.info("Slice #" + sliceID + " Finished: " + System.currentTimeMillis());
        });
    } // end the slice...

    /**
     * Tick is the main timekeeping unit of the server. At each tick, various statistics are checked
     * and shown to players (ex: house ranking) and various portions of the campaign are cleaned up
     * or finalized (ex: market sales). Most tick actions involve meta-functions, houses, the
     * market, and so on. The only tick mechanic that acts directly on players is Mezzo (pricemod)
     * drain.
     */
    public synchronized void tick(boolean real, int tickid) {
        HibernateUtil.inTransaction(session -> {
            // add header to log
            LOGGER.info("Tick #" + tickid + " Started");

            // log the number of games underway
            int gameCount = 0;
            for (ShortOperation currO : getOpsManager().getRunningOps().values()) {
                if (currO.getStatus() == ShortOperation.STATUS_INPROGRESS) {
                    gameCount++;
                }
            }
            LOGGER.info(gameCount + " games in progress.");

            // tick all houses
            int totalPlayersOnline = 0;
            for (House vh : data.getAllHouses()) {
                // we can safely cast to SHouse
                SHouse currH = (SHouse) vh;

                /*
                 * Total faction load for logs.
                 */
                long activePs =
                    session.createQuery(
                                    "SELECT COUNT(p) SPlayer p WHERE"
                                        + " :houseId = p.house_id AND status = :statusId",
                                    Long.class)
                            .setParameter("houseId", currH.getId())
                            .setParameter("status", SPlayer.STATUS_ACTIVE)
                            .getSingleResult();
                long fightingPs = 
                    session.createQuery(
                                    "SELECT COUNT(p) SPlayer p WHERE"
                                        + " :houseId = p.house_id AND status = :statusId",
                                    Long.class)
                            .setParameter("houseId", currH.getId())
                            .setParameter("status", SPlayer.STATUS_FIGHTING)
                            .getSingleResult();
                long totalFactionPlayers = 
                    session.createQuery(
                                    "SELECT COUNT(p) SPlayer p WHERE"
                                        + " :houseId = p.house_id AND status = :statusId",
                                    Long.class)
                            .setParameter("houseId", currH.getId())
                            .setParameter("status", SPlayer.STATUS_LOGGEDOUT)
                            .getSingleResult();

                LOGGER.info(
                        currH.getName()
                                + " has "
                                + totalFactionPlayers
                                + " members online ("
                                + activePs
                                + " active, "
                                + fightingPs
                                + " fighting)");

                // if there are any faction players online, tick the house
                if (totalFactionPlayers > 0 || real == false) {
                    String houseTickInfo = "";
                    List<SPlayer> playerList = 
                        session.createQuery(
                                    "SELECT COUNT(p) SPlayer p WHERE"
                                        + " :houseId = p.house_id AND status = :statusId",
                                        SPlayer.class)
                                .setParameter("houseId", currH.getId())
                                .setParameter("status", SPlayer.STATUS_LOGGEDOUT)
                                .getResultList();

                    if (!CampaignMain.cm.getBooleanConfig("ProcessHouseTicksAtSlice")) {
                        try {
                            LOGGER.debug("Starting Faction Tick");
                            houseTickInfo = currH.tick(real, tickid);
                            LOGGER.debug("Finished Faction Tick");
                        } catch (Exception e) {
                            LOGGER.error("Problems with faction tick.");
                            LOGGER.error("Exception: ", e);
                        }
                    }
                    // do some things (reset scraps, etc) for players
                    for (SPlayer currP : playerList) {
                        // Clear up any users that the server still thinks is
                        // connected.
                        if (MWServ.getInstance().getClient(currP.getName()) == null) {
                            LOGGER.debug("Logging out Player " + currP.getName());
                            doLogoutPlayer(currP.getName());
                            continue;
                        }

                        totalPlayersOnline++;
                        LOGGER.debug("Setting Scraps This tick for " + currP.getName());
                        currP.setScrapsThisTick(0);
                        LOGGER.debug("Setting Donations This tick for " + currP.getName());
                        currP.setDonatonsThisTick(0);
                        LOGGER.debug("Healing pilots This tick for " + currP.getName());
                        currP.healPilots();

                        LOGGER.debug("Updating faction info for " + currP.getName());
                        // return the result of the faction tick to everyone, to
                        // misc tab.
                        toUser("SM|" + houseTickInfo, currP.getName(), false);
                    }
                } // end if(there is a player in the faction)
            } // end for(all houses)

            // append the total player count to the logs
            LOGGER.info(
                    "Total players: "
                            + MWServ.getInstance().userCount(true)
                            + " online, "
                            + totalPlayersOnline
                            + " logged in.");

            /*
             * Send the latest game reports to the players, and increment the
             * removal-counters.
             */
            String generalResult = "<br>";
            String opsTick = opsManager.tick();
            if (opsTick.length() > 0) {
                generalResult += opsTick + "<br><br>";
            }

            // if the relative house rankings should be shown, do so.
            String rankTick = "";
            if (Boolean.parseBoolean(this.getConfig("ShowFactionRanks"))) {
                rankTick = Statistics.getReadableHouseRanking(true);
            }

            if (rankTick.length() > 0) {
                generalResult += rankTick + "<br><br>";
            }

            // send the combined & spaced string to players
            if (generalResult.toLowerCase().replace("<br>", " ").trim().length() > 0) {
                this.doSendToAllOnlinePlayers("PL|UDT|" + generalResult, false);
            }

            /*
             * Tick the market. This will resolve any auctions w/ 0 ticks remaining
             * and decrement all others.
             */
            try {
                market.tick();
            } catch (Exception ex) {
                LOGGER.error("Exception: ", ex);
            }

            LOGGER.info("Parts Market Tick Started");
            try {
                partsmarket.tick();
            } catch (Exception ex) {
                LOGGER.error("Exception: ", ex);
            }
            LOGGER.info("Parts Market Tick Finished");

            LOGGER.info("doRanking");
            // output player stats to HTML, if enabled.
            if (Boolean.parseBoolean(getConfig("HTMLOUTPUT"))) {
                Statistics.doRanking();
            }

            LOGGER.info("PurgePlayersFiles");
            // purge old player files
            purgePlayerFiles();

            LOGGER.info("Automated Backup");
            /*
             * finally, check to see if we should back up. note that the thread will
             * die immediately if it is not time to back up (last was written within
             * offset).
             */
            MWServ.getInstance().startAutomaticBackup();

            LOGGER.info("GC");
            // force a GC. this may not be necessary anymore?
            System.gc();

            // mainlog footer
            LOGGER.info("Tick #" + tickid + " Finished");
        });
    }

    /* The Planetary Control Way */
    public TreeSet<HouseRankingHelpContainer> getHouseRanking() {

        Hashtable<String, HouseRankingHelpContainer> factionContainer =
                new Hashtable<String, HouseRankingHelpContainer>();
        for (House currHouse : data.getAllHouses()) {
            SHouse h = (SHouse) currHouse;
            if (!h.isMercHouse() && !h.isNewbieHouse()) {
                HouseRankingHelpContainer hrc = new HouseRankingHelpContainer(h);
                factionContainer.put(h.getName(), hrc);
            }
        }

        for (Planet p : data.getAllPlanets()) {

            for (House currH : p.getInfluence().getHouses()) {
                SHouse hs = (SHouse) currH;
                if (hs == null) {
                    continue;
                }
                if (!hs.isNewbieHouse() && !hs.isMercHouse()) {
                    factionContainer
                            .get(hs.getName())
                            .addAmount(p.getInfluence().getInfluence(hs.getId()));
                }
            }
        }

        TreeSet<HouseRankingHelpContainer> s = new TreeSet<HouseRankingHelpContainer>();
        for (HouseRankingHelpContainer currContainer : factionContainer.values()) {
            s.add(currContainer);
        }

        return s;
    }

    /**
     * Send a bit of text to all players who are currently online. Can be chat, or a
     * command/message.
     */
    public void doSendToAllOnlinePlayers(String text, boolean isChat) {
        HibernateUtil.inTransaction(session -> {
            session.enableFilter("FILTER_BY_ONLINE");
            List<SPlayer> playerList = 
                session.createNamedQuery("SPlayer.getAllLoggedIn", SPlayer.class)
                    .getResultList();
            for (SPlayer player : playerList) {
                this.toUser(text, player.getName(), isChat);
            }
        });
    }

    /** Send a bit of text to all players in a given faction. Can be chat, or a command/message. */
    public void doSendToAllOnlinePlayers(SHouse h, String text, boolean isChat) {
        HibernateUtil.inTransaction(session -> {
            session.enableFilter("FILTER_BY_ONLINE");
            List<SPlayer> playerList = 
                session.createNamedQuery("SPlayer.findAllInHouse", SPlayer.class)
                    .setParameter("houseId", h.getId())
                    .getResultList();
            for (SPlayer player : playerList) {
                this.toUser(text, player.getName(), isChat);
            }
        });
    }

    /**
     * Update all player armies that are online This is normally called after operations have been
     * updated.
     */
    public void updateAllOnlinePlayerArmies() {
        doSendToAllOnlinePlayers("PL|UOE|CLEAR", false);
        HibernateUtil.inTransaction(session -> {
            for (House vh : data.getAllHouses()) {
                SHouse h = (SHouse) vh;

                session.enableFilter("FILTER_BY_ONLINE");
                List<SPlayer> playerList = 
                    session.createNamedQuery("SPlayer.findAllInHouse", SPlayer.class)
                        .setParameter("houseId", h.getId())
                        .getResultList();

                for (SPlayer currPlayer : playerList) {
                    for (SArmy army : currPlayer.getArmies()) {
                        army.getLegalOperations().clear();
                        CampaignMain.cm.getOpsManager().checkOperations(army, true);
                    }
                }
            }
        });
    }

    /**
     * Method that returns the SHouse that contains a player with a given name. If no factions has
     * such a player online, return a null.
     */
    public SHouse getHouseForPlayer(String username) {
        String playerName = username;
        // Use database query to find player by name
        return HibernateUtil.fromTransaction(session -> {
            List<SPlayer> players = session.createQuery(
                "FROM SPlayer WHERE name = :name", SPlayer.class)
                .setParameter("name", playerName)
                .getResultList();
            
            if (!players.isEmpty()) {
                return (SHouse) players.get(0).getMyHouse();
            }
            return null;
        });
    }

    public boolean isUsingIncreasedTechs() {
        return (CampaignMain.cm.getBooleanConfig("UseNonFactionUnitsIncreasedTechs")
                && !CampaignMain.cm.isUsingAdvanceRepair());
    }

    public Random getR() {
        return r;
    }

    public int getRandomNumber(int seed) {

        if (seed < 1) {
            return seed;
        }

        float answer = r.nextFloat() * (float) seed;

        return (int) Math.floor(answer);
    }

    public Market2 getMarket() {
        return market;
    }

    public PartsMarket getPartsMarket() {
        return partsmarket;
    }

    public Hashtable<String, Command> getServerCommands() {
        return Commands;
    }

    public double getAmmoCost(String ammo) {
        if (blackMarketEquipmentCostTable.containsKey(ammo)
                && blackMarketEquipmentCostTable.get(ammo).getMinCost() > 0) {
            return blackMarketEquipmentCostTable.get(ammo).getMinCost();
        }

        return -1.0;
    }

    public ChatRoom getChatRoom(String chatRoomName) {
        return chatRooms.get(chatRoomName);
    }

    public Collection<ChatRoom> getChatRoomList() {
        return chatRooms.values();
    }

    public void addChatRoom(String chatRoomName, ChatRoom chatRoom) {
        chatRooms.put(chatRoomName.toLowerCase(), chatRoom);
    }

    /**
     * @return the campaign's VoteManager
     */
    public VoteManager getVoteManager() {
        return voteManager;
    }

    /**
     * This retuns the blackMarketEquipmentCostTable This hashTable keeps track of all the mix/max
     * costs and parts production for the Black market. This is used to allow players to buy spare
     * parts to repair Their units.
     *
     * @return blackMarketEquipmentCostTable
     */
    public Hashtable<String, Equipment> getBlackMarketEquipmentTable() {
        return blackMarketEquipmentCostTable;
    }

    public Vector<ContractInfo> getUnresolvedContracts() {
        return unresolvedContracts;
    }

    /**
     * @return Returns the currentUnitID.
     */
    public int getCurrentUnitID() {
        return currentUnitID;
    }

    /**
     * @param currentUnitID The currentUnitID to set.
     */
    public void setCurrentUnitID(int currentUnitID) {
        this.currentUnitID = currentUnitID;
    }

    public synchronized int getAndUpdateCurrentUnitID() {
        currentUnitID++;
        return currentUnitID - 1;
    }

    public int getCurrentPilotID() {
        return currentPilotID;
    }

    public synchronized int getAndUpdateCurrentPilotID() {
        return ++currentPilotID;
    }

    public void setCurrentPilotID(int id) {
        currentPilotID = id;
    }

    public SHouse getHouseFromPartialString(String HouseString) {
        return getHouseFromPartialString(HouseString, null);
    }

    public SHouse getHouseById(int id) {
        return (SHouse) data.getHouse(id);
    }

    public SHouse getHouseFromPartialString(String houseString, String username) {
        return (SHouse) data.getHouseFromPartialString(houseString);
    }

    /**
     * Private method which writes out players who need to be saved and purges logged out/removable
     * players from RAM. Should be called only from .slice() or forceSave. See
     * this.forceSavePlayers() for more info on admin-initiated player saves.
     */
    private void savePlayers() {

        // go into sleep while the server is archiving player files
        while (isArchiving()) {
            try {
                Thread.sleep(125);
            } catch (Exception ex) {
                // do nothing
            }
        }

        // add log header
        Date d = new Date(System.currentTimeMillis());
        LOGGER.info(d + ": Starting Player Saving cycle");
        HibernateUtil.inTransaction(session -> {
            for (House vh : CampaignMain.cm.getData().getAllHouses()) {
                SHouse currH = (SHouse) vh;
                List<SPlayer> players = session.createQuery(
                            "SELECT COUNT(p) SPlayer p WHERE"
                                + " :houseId = p.house_id AND status = :statusId",
                        SPlayer.class)
                .setParameter("houseId", currH.getId())
                .setParameter("status", SPlayer.STATUS_LOGGEDOUT)
                .getResultList();
                for (SPlayer currP : players) {
                    savePlayerFile(currP);
                }
            }
        });

        // write out log footer
        d = new Date(System.currentTimeMillis());
        LOGGER.info(d + ": Player save cycle completed.");
    }

    /**
     * Public save method. Used by admins to save all online players and all players who are in the
     * save queue. Is called from /save, /shutdown, and /c adminsave.
     */
    public void forceSavePlayers(String username) {
        // first, save everyone online
        HibernateUtil.inTransaction(session -> {
            for (House vh : CampaignMain.cm.getData().getAllHouses()) {
                SHouse currH = (SHouse) vh;
                List<SPlayer> players = session.createQuery(
                        "SELECT COUNT(p) SPlayer p WHERE"
                            + " :houseId = p.house_id AND status = :statusId",
                        SPlayer.class)
                .setParameter("houseId", currH.getId())
                .setParameter("status", SPlayer.STATUS_LOGGEDOUT)
                .getResultList();
                for (SPlayer currP : players) {
                    savePlayerFile(currP);
                    if (username != null) {
                        CampaignMain.cm.toUser("AM:" + currP.getName() + " saved", username, true);
                    }
                }
            }
        });
    }

    /**
     * Public save method to save one player Used by changename and defect commands This is used so
     * the players have a Pfile created right away
     */
    public void forceSavePlayer(SPlayer p) {
        savePlayerFile(p);
    }

    /**
     * Private method which writes a player to the disc. This code was housed in SPlayer; however,
     * it is only called from CampaignMain and (from an OO standpoint) only CMain should know the
     * hardcoded paths which are used.
     *
     * @author nmorris 1/13/06
     */
    private void savePlayerFile(SPlayer p) {
        try {
            String fileName = p.getName().toLowerCase();
            FileOutputStream pout =
                    new FileOutputStream("./campaign/players/" + fileName.toLowerCase() + ".dat");
            PrintStream pfile = new PrintStream(pout);

            /*
             * Put a lock on the player while saving. Do NOT allow .toString()
             * to set a lock, or we'll get deadlocks.
             */
            synchronized (p) {
                pfile.println(p.toString(false));
            }

            pfile.close();
            pout.close();
        } catch (FileNotFoundException fnfe) {
            // Since we are saving to disk do nothing.
            // The proccess is most likely already being used.
            return;
        } catch (Exception ex) {
            LOGGER.error("Exception: ", ex);
            LOGGER.error("Unable to save " + p.getName().toLowerCase());
        }
    }

    public void loadBannedTargetSystems() {
        File tsFile = new File("./campaign/bantarget.dat");
        if (!tsFile.exists()) {
            return;
        }

        try {
            MekwarsFileReader dis = new MekwarsFileReader(tsFile);
            Vector<Integer> bans = new Vector<Integer>(1, 1);
            String line = dis.readLine();
            StringTokenizer st = new StringTokenizer(line, "#");
            while (st.hasMoreTokens()) {
                bans.add(Integer.parseInt(st.nextToken()));
            }
            getData().setBannedTargetingSystems(bans);
            dis.close();
        } catch (IOException e) {
            LOGGER.error("Exception: ", e);
        }
    }

    /** Load the black market settings from file. */
    public void loadBlackMarketSettings() {

        try {
            File bmFile = new File("./data/blackmarketsettings.dat");

            if (!bmFile.exists()) {
                return;
            }

            MekwarsFileReader dis = new MekwarsFileReader(bmFile);

            // Ignore Time Stamp
            dis.readLine();

            while (dis.ready()) {
                Equipment bme = new Equipment();
                String line = dis.readLine();
                StringTokenizer data = new StringTokenizer(line, "#");

                bme.setEquipmentInternalName(data.nextToken());
                bme.setMinCost(Double.parseDouble(data.nextToken()));
                bme.setMaxCost(Double.parseDouble(data.nextToken()));
                bme.setMinProduction(Integer.parseInt(data.nextToken()));
                bme.setMaxProduction(Integer.parseInt(data.nextToken()));

                cm.getBlackMarketEquipmentTable().put(bme.getEquipmentInternalName(), bme);
            }
            dis.close();
        } catch (Exception ex) {
            LOGGER.error("Exception: ", ex);
        }
    }

    public void saveTopUnitID() {

        int topID = cm.getCurrentUnitID();

        try {
            FileOutputStream pout = new FileOutputStream("./campaign/topserverid.dat");
            PrintStream unitIDFile = new PrintStream(pout);
            unitIDFile.println(topID);
            unitIDFile.println(cm.getCurrentPilotID());
            unitIDFile.close();
            pout.close();
        } catch (Exception ex) {
            LOGGER.error("Exception: ", ex);
        }
    }

    public void loadTopUnitID() {
        try {
            MekwarsFileReader dis = new MekwarsFileReader("./campaign/topserverid.dat");
            cm.setCurrentUnitID(Integer.parseInt(dis.readLine()));
            cm.setCurrentPilotID(Integer.parseInt(dis.readLine()));
            dis.close();
        } catch (FileNotFoundException FNFE) {
            // Do nothing.
            LOGGER.error("Unable to fine/open ./campaign/topserverid.dat. moving on.");
        } catch (Exception ex) {
            LOGGER.error("Exception: ", ex);
        }
    }

    public void setGamesCompleted(int i) {
        gamesCompleted = i;
    }

    public void addGamesCompleted(int i) {
        setGamesCompleted(getGamesCompleted() + i);
    }

    public int getGamesCompleted() {
        return gamesCompleted;
    }

    public int getMachineGunCount(List<Mounted> weaponList) {
        int count = 0;

        for (Mounted weapons : weaponList) {
            WeaponType weapon = (WeaponType) weapons.getType();
            if (weapon.hasFlag(WeaponType.F_MG)) {
                count++;
            }
        }
        return count;
    }

    // /**
    //  * Use to load a factions trait file.
    //  *
    //  * @author Torren (Jason Tighe)
    //  * @param faction
    //  * @return
    //  */
    // public Vector<String> getFactionTraits(String faction) {
    //     Vector<String> traits = new Vector<String>(1, 1);
    //     File traitNames = new File("./data/pilotnames/" + faction.toLowerCase() + "traitnames.txt");

    //     if (!traitNames.exists()) {
    //         traitNames = new File("./data/pilotnames/commontraitnames.txt");
    //     }

    //     try {

    //         MekwarsFileReader dis = new MekwarsFileReader(traitNames);
    //         while (dis.ready()) {
    //             traits.addElement(dis.readLine());
    //         }
    //         dis.close();
    //     } catch (FileNotFoundException nf) {
    //         LOGGER.error("File Not Found: " + traitNames);
    //     } catch (Exception ex) {
    //         LOGGER.error("Error loading Faction Traits: " + faction);
    //         LOGGER.error("Exception: ", ex);
    //     }

    //     traits.trimToSize();
    //     return traits;
    // }

    // public void saveFactionTraits(String faction, Vector<String> traits) {
    //     File traitFile = new File("./data/pilotnames/" + faction.toLowerCase() + "traitnames.txt");

    //     try {

    //         if (!traitFile.exists()) {
    //             traitFile.createNewFile();
    //         }

    //         FileOutputStream fos = new FileOutputStream(traitFile);
    //         PrintStream p = new PrintStream(fos);

    //         for (String tempTrait : traits) {
    //             p.println(tempTrait);
    //         }

    //         p.close();
    //         fos.close();

    //     } catch (Exception ex) {
    //         LOGGER.error("Error while saving trait file for faction: {}", faction, ex);
    //     }
    // }

    public void setOmniVariantMods(Hashtable<String, String> table) {
        omniVariantMods = table;
    }

    public Hashtable<String, String> getOmniVariantMods() {
        return omniVariantMods;
    }

    public void saveOmniVariantMods() {
        if (omniVariantMods.size() < 1) {
            return;
        }

        try {

            FileOutputStream out = new FileOutputStream("./campaign/omnivariantmods.dat");
            PrintStream p = new PrintStream(out);

            for (String currKey : cm.getOmniVariantMods().keySet()) {
                String currMod = cm.getOmniVariantMods().get(currKey);
                p.println(currKey + "#" + currMod);
            }

            p.close();
            out.close();
        } catch (Exception ex) {
            LOGGER.error("Error while saving omnivariantmods.dat");
            LOGGER.error("Exception: ", ex);
        }
    }

    /**
     * @author Torren (Jason Tighe) This method will go through and check all the player files and
     *     forceible unenroll anyone that is over <code>days</code> idle.
     */
    public void purgePlayerFiles() {
        long days = Long.parseLong(CampaignMain.cm.getConfig("PurgePlayerFilesDays"));
        // Turn purging off by setting it to 0 or less days
        if (days <= 0) {
            return;
        }

        // convert days to milliseconds
        days *= 24;
        days *= 60;
        days *= 60;
        days *= 1000;

        File[] playerList = new File("./campaign/players").listFiles();

        for (File player : playerList) {
            if (player.isDirectory()) {
                continue;
            }
            if (player.lastModified() + days < System.currentTimeMillis()) {
                String playerName = player.getName().substring(0, player.getName().indexOf(".dat"));
                SPlayer p = this.getPlayer(playerName);
                p.addExperience(100, true);
                Command c = CampaignMain.cm.getServerCommands().get("UNENROLL");
                c.process(new StringTokenizer("CONFIRM", "#"), playerName);
                LOGGER.info(playerName + " purged.");
            }
        }
    }

    /**
     * @author Torren (Jason Tighe)
     * @param money
     * @param shortname
     * @param amount
     * @return String Hokey function to return the correct syntax for long and short money/flu
     *     messages to the user.
     */
    public String moneyOrFluMessage(boolean money, boolean shortname, int amount) {
        return moneyOrFluMessage(money, shortname, amount, false);
    }

    public String moneyOrFluMessage(
            boolean money, boolean shortname, int amount, boolean showSign) {
        String result = NumberFormat.getInstance().format(amount);
        String moneyShort = cm.getConfig("MoneyShortName").toLowerCase();
        String moneyLong = cm.getConfig("MoneyLongName");
        String fluShort = cm.getConfig("FluShortName").toLowerCase();
        String fluLong = cm.getConfig("FluLongName");
        //        String RPShort = cm.getConfig("RPShortName");
        //        String RPLong = cm.getConfig("RPLongName");

        String sign = "+";

        if (amount < 0) {
            amount *= -1;
            sign = "-";
            result = NumberFormat.getInstance().format(amount);
        }

        if (!shortname) {
            result += " ";
        }

        if (money) {
            if (shortname) {
                if (amount == 1 && moneyShort.endsWith("s")) {
                    result += moneyShort.substring(0, moneyShort.length() - 1);
                } else if (amount > 1 && !moneyShort.endsWith("s")) {
                    result += moneyShort + "s";
                } else {
                    result += moneyShort;
                }
            } // end shortname if
            else {
                if (amount == 1 && moneyLong.endsWith("s")) {
                    result += moneyLong.substring(0, moneyLong.length() - 1);
                } else if (amount > 1 && !moneyLong.endsWith("s")) {
                    result += moneyLong + "s";
                } else {
                    result += moneyLong;
                }
            } // end shortname else
        } // end money if
        else {
            if (shortname) {
                result += fluShort;
            } // end shortname if
            else {
                result += fluLong;
            } // end shortname else
        } // end money else

        // add sign, if set
        if (showSign) {
            return sign + result;
        }

        return result.trim();
    }

    // @salient
    public String getCurrencyName(String cType, boolean shortDescription) {

        switch (cType.toLowerCase().trim()) {
            case "money":
            case "cb":
                if (shortDescription) return cm.getConfig("MoneyShortName");
                else return cm.getConfig("MoneyLongName");
            case "rewards":
            case "reward":
            case "rp":
                if (shortDescription) return cm.getConfig("RPShortName");
                else return cm.getConfig("RPLongName");
            case "influence":
            case "flu":
                if (shortDescription) return cm.getConfig("FluShortName");
                else return cm.getConfig("FluLongName");
            default:
                LOGGER.error(cType + "is not a valid currency");
                return null;
        }
    }

    public void updateISPLists(SPlayer player) {
        BufferedReader buff = null;
        try {
            File file = new File("./data/Providers");
            if (!file.exists()) {
                file.mkdir();
            }

            file = new File("./data/Providers/" + player.getLastISP() + ".prv");

            if (!file.exists()) {
                saveToISPLists(player);
                return;
            }

            FileInputStream in = new FileInputStream(file);
            buff = new BufferedReader(new InputStreamReader(in));

            while (buff.ready()) {
                String name = buff.readLine();

                if (name.equalsIgnoreCase(player.getName())) {
                    buff.close();
                    in.close();
                    return;
                }
            }
            saveToISPLists(player);

        } catch (Exception ex) {
        } finally {
            try {
                buff.close();
            } catch (IOException e) {
                LOGGER.error("Exception: ", e);
            }
        }
    }

    public void saveToISPLists(SPlayer player) {
        try {
            FileOutputStream out =
                    new FileOutputStream("./data/Providers/" + player.getLastISP() + ".prv", true);
            PrintStream p = new PrintStream(out);
            p.println(player.getName());
            p.close();
            out.close();
        } catch (Exception ex) {
        }
    }

    public void loadOmniVariantMods() {
        try {
            MekwarsFileReader dis = new MekwarsFileReader("./campaign/omnivariantmods.dat");
            while (dis.ready()) {
                StringTokenizer line = new StringTokenizer(dis.readLine(), "#");
                cm.getOmniVariantMods().put(line.nextToken(), line.nextToken());
            }
            dis.close();
        } catch (Exception ex) {
        }
    }

    public void setArchiving(boolean archive) {
        isArchiving = archive;
    }

    public boolean isArchiving() {
        return isArchiving;
    }

    public UnitCosts getUnitCostLists() {
        return cm.unitCostLists;
    }

    public Client getMegaMekClient() {
        return megaMekClient;
    }

    public void setMegaMekClient(Client mmClient) {
        cm.megaMekClient = mmClient;
    }

    public void saveBannedTargetSystems() {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream("./campaign/bantarget.dat");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        PrintStream p = new PrintStream(out);
        for (int ban : getData().getBannedTargetingSystems()) {
            p.print(ban);
            p.print("#");
        }
        p.println();
        p.close();
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadPlanetOpFlags() {
        File configFile = new File("./campaign/planetOpFlags.dat");
        if (!configFile.exists()) {
            LOGGER.error("No planetOpFlags.dat. Skipping.");
            return;
        }

        try {
            MekwarsFileReader dis = new MekwarsFileReader(configFile);
            dis.readLine(); // Time Stamp

            String nextLine = dis.readLine();
            if (nextLine == null) {
                LOGGER.error("Timestamp-only planetOpFlags.dat. Skipping.");
                return;
            }

            StringTokenizer st = new StringTokenizer(nextLine, "#");
            while (st.hasMoreTokens()) {
                data.getPlanetOpFlags().put(st.nextToken(), st.nextToken());
            }

            dis.close();
        } catch (Exception ex) {
            LOGGER.error("Error loading Planet Op Flags.");
            LOGGER.error("Exception: ", ex);
        }
    }

    public void savePlanetOpFlags() {
        // Save Planet Op Flags
        try {
            FileOutputStream out = new FileOutputStream("./campaign/planetOpFlags.dat");
            PrintStream p = new PrintStream(out);
            p.println(System.currentTimeMillis());
            for (String key : CampaignMain.cm.getData().getPlanetOpFlags().keySet()) {
                p.print(key);
                p.print("#");
                p.print(CampaignMain.cm.getData().getPlanetOpFlags().get(key));
                p.print("#");
            }
            p.close();
            out.close();
        } catch (Exception ex) {
            LOGGER.error("Error saving Planet Op Flags.");
            LOGGER.error("Exception: ", ex);
        }
    }

    /**
     * Loads terrain data.
     *
     * @throws IOException When the terrain file cannot be opened.
     */
    public void loadTerrainData(Session session) throws IOException {
        Path path = FileSystem.getInstance().getTerrain();

        try (InputStream inputStream = Files.newInputStream(path)) {
            Terrain[] terrainList = (Terrain[]) getXStream().fromXML(inputStream);
            for (Terrain terrain : terrainList) {
                session.persist(terrain);
            }
        }
    }

    /**
     * Loads advanced terrain data.
     *
     * @throws IOException When the advanced terrain file cannot be opened.
     */
    public void loadAdvancedTerrainData(Session session) throws IOException {
        Path path = FileSystem.getInstance().getAdvancedTerrain();

        try (InputStream inputStream = Files.newInputStream(path)) {
            AdvancedTerrain[] advancedTerrainList =
                    (AdvancedTerrain[]) getXStream().fromXML(inputStream);
            for (AdvancedTerrain advancedTerrain : advancedTerrainList) {
                session.persist(advancedTerrain);
            }
        }
    }

    public void loadFactionData() {
        File factionFile = new File("./campaign/factions");

        // Check for new faction save location
        if (!factionFile.exists() || factionFile.listFiles().length < 1) {
            if (data.getAllHouses().size() == 0) {
                SHouse none = new MercHouse();
                none.createNoneHouse();
                addHouse(none);

                try {
                    factionFile = new File("./data/factions.xml");
                    SHouse[] factionList = (SHouse[]) getXStream().fromXML(factionFile);
                    for (SHouse house : factionList) {
                        Path configPath = FileSystem.getInstance().getFactionConfigPath(house.getName());

                        CampaignData.cd.loadHouseOptions(configPath, house);
                        addHouse(house);
                    }
                } catch (Exception ex) {
                    LOGGER.error("Error while reading faction data -- bailing out", ex);
                    System.exit(1);
                }

                String newbieHouseName = CampaignMain.cm.getConfig("NewbieHouseName");
                if (data.getHouseByName(newbieHouseName) == null) {
                    // Add the Newbie-SHouse
                    SHouse solaris = new NewbieHouse(newbieHouseName, "#33CCCC", 4, 5, "SOL");
                    addHouse(solaris);
                }
            }
            return;
        }

        // filter out .bak's
        FilenameFilter filter = new datFileFilter();
        File[] factionFileList = factionFile.listFiles(filter);

        // load each file
        for (File faction : factionFileList) {
            try {
                MekwarsFileReader dis = new MekwarsFileReader(faction);
                String line = dis.readLine();
                SHouse house = null;
                if (line.startsWith("[N][C]")) {
                    line = line.substring(6);
                    house = new NewbieHouse();
                } else if (line.startsWith("[N]")) {
                    line = line.substring(3);
                    house = new NewbieHouse();
                } else if (line.startsWith("[M]")) {
                    line = line.substring(3);
                    house = new MercHouse();
                } else {
                    house = new SHouse();
                }
                house.fromString(line, r);
                if (isUsingIncreasedTechs()) {
                    house.addCommonUnitSupport();
                }
                addHouse(house);
                dis.close();
            } catch (Exception ex) {
                LOGGER.error("Unable to load " + faction.getName());
            }
        }
        // load the various construction modifiers for the houses added above
        factionFile = new File("./campaign/costmodifiers");
        if (!factionFile.exists()) {
            return; // done
        }

        for (House currH : data.getAllHouses()) {
            String saveName = currH.getName().toLowerCase().trim() + ".dat";
            File faction = new File("./campaign/costmodifiers/" + saveName);

            if (!faction.exists()) {
                continue;
            }
            try {
                MekwarsFileReader dis = new MekwarsFileReader(faction);

                String currLine = null;
                while ((currLine = dis.readLine()) != null) {
                    StringTokenizer tokenizer = new StringTokenizer(currLine, "$");
                    String cost = tokenizer.nextToken();
                    int type = Integer.parseInt(tokenizer.nextToken());
                    int weight = Integer.parseInt(tokenizer.nextToken());
                    int mod = Integer.parseInt(tokenizer.nextToken());

                    if (cost.equals("Price")) {
                        currH.setHouseUnitPriceMod(type, weight, mod);
                    } else if (cost.equals("Flu")) {
                        currH.setHouseUnitFluMod(type, weight, mod);
                    } else if (cost.equals("Comp")) {
                        currH.setHouseUnitComponentMod(type, weight, mod);
                    }
                }
                dis.close();
            } catch (Exception e) {
                LOGGER.error("Unable to load cost modifiers for " + currH.getName());
            }
        }
    }

    // Save Houses
    public void saveFactionData() {
        File factionFile = new File("./campaign/factions");
        if (!factionFile.exists()) {
            factionFile.mkdir();
            if (isUsingIncreasedTechs()) {
                File supportFile = new File("./campaign/factions/support");
                supportFile.mkdir();
            }
        }
        factionFile = new File("./campaign/costmodifiers");
        if (!factionFile.exists()) {
            factionFile.mkdir();
        }

        synchronized (data.getAllHouses()) {
            for (House currH : data.getAllHouses()) {
                SHouse h = (SHouse) currH;

                String saveName = h.getName().toLowerCase().trim() + ".dat";
                String backupName = h.getName().toLowerCase().trim() + ".bak";

                // standard save
                try {

                    File faction = new File("./campaign/factions/" + saveName);

                    if (faction.exists()) {

                        File backupFile = new File("./campaign/factions/" + backupName);
                        if (backupFile.exists()) {
                            backupFile.delete();
                        }

                        faction.renameTo(backupFile);
                    }
                    FileOutputStream out = new FileOutputStream(faction);
                    PrintStream p = new PrintStream(out);

                    p.println(h.toString());

                    try {
                        File factionCostMod = new File("./campaign/costmodifiers/" + saveName);
                        if (factionCostMod.exists()) {

                            File backupFile = new File("./campaign/costmodifiers/" + backupName);
                            if (backupFile.exists()) {
                                backupFile.delete();
                            }

                            factionCostMod.renameTo(backupFile);
                        }

                        FileOutputStream costModout = new FileOutputStream(factionCostMod);
                        PrintStream costModp = new PrintStream(costModout);

                        for (int type = 0; type < 5; type++) {
                            for (int weight = 0; weight < 4; weight++) {

                                if (h.getHouseUnitPriceMod(type, weight) != 0) {
                                    costModp.println(
                                            "Price$"
                                                    + type
                                                    + "$"
                                                    + weight
                                                    + "$"
                                                    + h.getHouseUnitPriceMod(type, weight));
                                }
                                if (h.getHouseUnitFluMod(type, weight) != 0) {
                                    costModp.println(
                                            "Flu$"
                                                    + type
                                                    + "$"
                                                    + weight
                                                    + "$"
                                                    + h.getHouseUnitFluMod(type, weight));
                                }
                                if (h.getHouseUnitComponentMod(type, weight) != 0) {
                                    costModp.println(
                                            "Comp$"
                                                    + type
                                                    + "$"
                                                    + weight
                                                    + "$"
                                                    + h.getHouseUnitComponentMod(type, weight));
                                }
                            }
                        }
                        costModp.close();
                        costModout.close();

                    } catch (Exception ex) {
                        LOGGER.error("Unable to save Faction: " + saveName + " cost Mods");
                        LOGGER.error("Exception: ", ex);
                    }
                    p.close();
                    out.close();
                } catch (Exception ex) {
                    LOGGER.error("Unable to save Faction: " + saveName);
                    LOGGER.error("Exception: ", ex);
                }
            }
        }
    }

    public void loadPlanetData(Session session) {
        loadPlanetOpFlags();

        File planetFile = new File("./campaign/planets");
        FilenameFilter filter = new datFileFilter();

        // Check for faction save dir & ensure dat files exist therein
        if (!planetFile.exists() || planetFile.listFiles(filter).length == 0) {
            try {
                File planetsFile = new File("./data/planets.xml");
                SPlanet[] planets = (SPlanet[]) getXStream().fromXML(planetsFile);

                for (SPlanet planet : planets) {
                    LOGGER.info("Adding planet {}", planet);
                    addPlanet(session, planet);
                    for (House house : planet.getInfluence().getHouses()) {
                        SHouse shouse = (SHouse) house;
                        if (shouse == null) {
                            LOGGER.error(
                                    "Null faction found while loading" + " Planets.xml. Planet: {}",
                                    planet.getName());
                            continue;
                        }

                        if (planet.getInfluence().getOwner() != null
                                && shouse.getId() == planet.getInfluence().getOwner().intValue()) {
                            shouse.addPlanet(planet);
                        }

                        int initialHouseRanking =
                                shouse.getInitialHouseRanking()
                                        + planet.getInfluence().getInfluence(shouse.getId());
                        shouse.setInitialHouseRanking(initialHouseRanking);
                    }
                }
            } catch (Exception ex) {
                LOGGER.error("Error while reading Planet Data", ex);
                System.exit(1);
            }
            return;
        }
        // dir and files exist. read them.
        File[] planetFileList = planetFile.listFiles(filter);
        for (File planet : planetFileList) {
            try {
                MekwarsFileReader dis = new MekwarsFileReader(planet);
                String line = dis.readLine();
                SPlanet p;
                if (line.startsWith("[N]")) {
                    line = line.substring(3);
                }
                p = new SPlanet();
                p.fromString(line, r, data);
                addPlanet(session, p);
                dis.close();
            } catch (Exception ex) {
                LOGGER.error("Unable to load " + planet.getName());
                LOGGER.error("Exception: ", ex);
            }
        }
    }

    public void updatePlayersAccessLevel(String playerName, int accessLevel) {
        SPlayer player = cm.getPlayer(playerName);

        if (player == null) {
            return;
        }
        try {
            MWServ.getInstance().getClient(playerName).setAccessLevel(accessLevel);
            MWServ.getInstance().getUser(playerName).setLevel(accessLevel);
            MWServ.getInstance().sendRemoveUserToAll(playerName, false);
            MWServ.getInstance().sendNewUserToAll(playerName, false);
            MWPasswd.writeRecord(player.getPassword(), playerName);
            cm.doSendToAllOnlinePlayers("PI|DA|" + cm.getPlayerUpdateString(player), false);
        } catch (Exception ex) {
        }
        forceSavePlayer(player);
    }

    /**
     * this removes a SPlayer object form the global hash. This is called when a player logs into a
     * house, in which case the house now stores the object, or when the player logs off, incase
     * they never bothred to register or login.
     *
     * @param soul
     */
    public void releaseLostSoul(String soul) {
        lostSouls.remove(soul.toLowerCase());
    }

    public Date getHousePlanetUpdate() {
        return housePlanetDate;
    }

    public void updateHousePlanetUpdate() {
        housePlanetDate = new Date();
    }

    // Save Planets
    public void savePlanetData() {
        savePlanetOpFlags();
        CampaignData.cd.savePlanets();
        // File planetFile = new File("./campaign/planets");
        // if (!planetFile.exists()) {
        //     planetFile.mkdir();
        // }
        // synchronized (data.getAllPlanets()) {

        //     for (Planet currP : data.getAllPlanets()) {
        //         SPlanet p = (SPlanet) currP;
        //         String saveName = p.getName().toLowerCase().trim() + ".dat";
        //         String backupName = p.getName().toLowerCase().trim() + ".bak";
        //         try {
        //             File planet = new File("./campaign/planets/" + saveName);

        //             if (planet.exists()) {

        //                 File backupFile = new File("./campaign/planets/" + backupName);
        //                 if (backupFile.exists()) {
        //                     backupFile.delete();
        //                 }

        //                 planet.renameTo(backupFile);
        //             }

        //             FileOutputStream out = new FileOutputStream("./campaign/planets/" +
        // saveName);
        //             PrintStream ps = new PrintStream(out);
        //             ps.println(p.toString());
        //             ps.close();
        //             out.close();
        //         } catch (Exception ex) {
        //             LOGGER.error("Unable to save planet: " + saveName);
        //             LOGGER.error("Exception: ", ex);
        //         }
        //     }
        // }
    }

    public void saveMegaMekGameOptions(StringTokenizer gameOptions) {
        File mmGameOptionsFolder = new File("./mmconf");

        if (!mmGameOptionsFolder.exists()) {
            mmGameOptionsFolder.mkdir();
        }

        File mmGameOptions = new File("./mmconf/gameoptions.xml");
        try {
            FileOutputStream fops = new FileOutputStream(mmGameOptions);
            PrintStream out = new PrintStream(fops);
            while (gameOptions.hasMoreTokens()) {
                out.println(gameOptions.nextToken());
            }
            out.close();
            fops.close();
        } catch (Exception ex) {
            LOGGER.error("Unable to save Mega Mek Game Options!");
            LOGGER.error("Exception: ", ex);
        }
    }

    public String getMegaMekOptionsToString() {
        StringBuffer result = new StringBuffer();

        Enumeration<IOption> options = cm.getMegaMekClient().getGame().getOptions().getOptions();

        while (options.hasMoreElements()) {
            IOption option = options.nextElement();

            result.append(option.getName()).append("|").append(option.getValue()).append("|");
        }
        return result.toString();
    }

    class datFileFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.endsWith(".dat"));
        }
    }

    /**
     * @return the supportUnits
     */
    public Vector<String> getSupportUnits() {
        return supportUnits;
    }

    /**
     * @param supportUnits the supportUnits to set
     */
    public void setSupportUnits(Vector<String> supportUnits) {
        this.supportUnits = supportUnits;
    }

    /**
     * @return the defaultPlayerFlags
     */
    public PlayerFlags getDefaultPlayerFlags() {
        return defaultPlayerFlags;
    }

    /**
     * @return the scheduler
     */
    public MWScheduler getScheduler() {
        return scheduler;
    }

    /**
     * @param scheduler the scheduler to set
     */
    public void setScheduler(MWScheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * @return The XStream.
     */
    public XStream getXStream() {
        return (XStream) xstream;
    }

    public HtmlSanitizer getHtmlSanitizer() {
        return htmlSanitizer;
    }

    @Deprecated(since = "9.0.0", forRemoval = false)
    public boolean isUsingAdvanceRepair() {
        return CampaignData.cd.isUsingAdvanceRepair();
    }

    public FactionTraitFile getFactionTraitFileByHouse(String houseName) {
        FactionTraitFile file = CampaignData.cd.getFactionTraitFileByHouse(houseName);

        if (file != null) {
            return file;
        }

        Path path = FileSystem.getInstance().getFactionTraitNamesPath(houseName);
        file = new FactionTraitFile(path, houseName);
        try {
            if (Files.exists(path)) {
                file.load();
            }
        } catch (IOException exception) {
            LOGGER.error("Unable to load FactionTraitFile", exception);
        }
        CampaignData.cd.addFactionTraitFile(file);
        return file;
    }
}
