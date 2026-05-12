/*
 * MekWars - Copyright (C) 2004, 2005, 2006
 *
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megamek)
 * Original author Helge Richter (McWizard)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 */

package mekwars.client.campaign;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import mekwars.client.MWClient;
import mekwars.client.GUIClient;
import mekwars.client.common.campaign.clientutils.GameHost;
import mekwars.client.io.FileSystem;
import mekwars.client.util.CArmyComparator;
import mekwars.client.util.CUnitComparator;
import mekwars.common.CampaignData;
import mekwars.common.House;
import mekwars.common.Player;
import mekwars.common.SubFaction;
import mekwars.common.Unit;
import mekwars.common.util.TokenReader;
import mekwars.common.util.UnitComponents;
import mekwars.common.util.UnitUtils;
import megamek.common.CriticalSlot;
import megamek.common.OffBoardDirection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class for Player object used by Client
 */
public class CPlayer extends Player<CUnit> {
    private static final Logger LOGGER = LogManager.getLogger(CPlayer.class);

    public static final String DELIMITER = "#"; // delimiter for player strings

    private MWClient mwclient;
    private String House;

    private int bays;
    private int freeBays = 0;
    private int hangarPenalty;
    private int hangarPurchasePenalties[][] = new int[6][4];

    private List<CArmy> armies;
    private List<CUnit> autoArmy;

    private List<String> adminExcludes;
    private List<String> playerExcludes;

    private CPersonalPilotQueues personalPilotQueue;

    private House houseFightingFor = null;

    private int repairLocation = 0;
    private int repairTechType = 0;
    private int repairRetries = 0;

    private int conventionalMinesAllowed = 0;
    private int vibraMinesAllowed = 0;

    private UnitComponents partsCache = new UnitComponents();

    private String subFactionName = "";

    public CPlayer(MWClient client) {
        mwclient = client;
        bays = 0;
        House = "";
        armies = new ArrayList<>();
        autoArmy = new ArrayList<>();
        setMyHouse(new House());
        personalPilotQueue = new CPersonalPilotQueues();
        adminExcludes = new ArrayList<String>();
        playerExcludes = new ArrayList<String>();
    }

    public boolean decodeCommand(String command) {
        StringTokenizer ST;
        String element;

        ST = new StringTokenizer(command, "|");
        element = TokenReader.readString(ST);
        if (!element.equals("PL")) {
            return (false);
        }
        element = TokenReader.readString(ST);
        command = command.substring(3);

        if (element.equals("DA")) {// is a PI|DA
            if (!setData(command)) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Called from PL after PL|SAD received. Adds a new army OR replaces an old
     * army's data with new dump.
     */
    public void setArmyData(String data) {
        CArmy newArmy = new CArmy();
        newArmy.fromString(data, this, "%", mwclient);

        // Save the old army's legal operations.
        CArmy oldArmy = getArmy(newArmy.getID());
        if (oldArmy != null) {
            newArmy.setLegalOperations(oldArmy.getLegalOperations());
        }

        // swap the armies
        removeArmy(newArmy.getID());
        if (armies.size() < newArmy.getID()) {
            armies.add(newArmy);
        } else {
            armies.add(newArmy.getID(), newArmy);
        }
    }

    /**
     * Complete setData command. Called in response to a PS| sent by the server.
     *
     * @param - data
     * @return - success
     */
    public boolean setData(String data) {
        StringTokenizer ST;
        String element;
        CUnit tmek;
        int i, Armiescount, Hangarcount;

        ST = new StringTokenizer(data, "~");
        element = TokenReader.readString(ST);
        if (!element.equals("CP")) {
            return false;
        }

        for (int x = 0; x < UnitUtils.TECH_ELITE; x++) {
            setTotalTechs(x, 0);
            setAvailableTechs(x, 0);
        }

        armies.clear();
        clearUnits();

        setName(TokenReader.readString(ST));

        setMoney(TokenReader.readInt(ST));
        setExperience(TokenReader.readInt(ST));

        Hangarcount = TokenReader.readInt(ST);
        for (i = 0; i < Hangarcount; i++) {
            tmek = new CUnit(mwclient);
            if (tmek.setData(TokenReader.readString(ST))) {
                addUnit(tmek);
                LOGGER.debug("Adding unit {} to hanger", tmek.checkModelName());
            }
        }

        Armiescount = (TokenReader.readInt(ST));
        for (i = 0; i < Armiescount; i++) {
            CArmy army = new CArmy();
            army.fromString(TokenReader.readString(ST), this, "%", mwclient);
            armies.add(army);
            LOGGER.debug("Adding army {} with {} units", army.getName(), army.getAmountOfUnits());
        }

        bays = TokenReader.readInt(ST);
        freeBays = TokenReader.readInt(ST);
        setRating(Double.parseDouble(TokenReader.readString(ST)));
        setInfluence(TokenReader.readInt(ST));
        setTechnicians(TokenReader.readInt(ST));
        doPayTechniciansMath();
        setRewardPoints(TokenReader.readInt(ST));
        String string = TokenReader.readString(ST);
        setMekToken(Integer.parseInt(string));
        House = TokenReader.readString(ST);
        setHouseFightingFor(TokenReader.readString(ST));
        setLogo(TokenReader.readString(ST));
        setInvisible(TokenReader.readBoolean(ST));

        if (Boolean.parseBoolean(mwclient.getServerConfigs("UsePartsRepair"))) {
            partsCache.fromString(TokenReader.readString(ST), "|");
        } else {
            TokenReader.readString(ST);
        }

        setAutoReorder(TokenReader.readBoolean(ST));

        //flags.loadDefaults(mwclient.getPlayer().getDefaultPlayerFlags().export());
        //flags.loadPersonal(TokenReader.readString(ST));
        LOGGER.info("My Player Flags: " + flags.export());
        // traps run. sort the HQ. this isn't duplicative, b/c
        // direct lods (PS instead of PL) don't trigger sorts.
        sortHangar();
        return true;
    }

    /**
     * Called by PL|HD - adds a single unit to the hangar.
     */
    public void setHangarData(String data) {
        try {
            CUnit unit = new CUnit(mwclient);
            if (unit.setData(data)) {
                addUnit(unit);
                sortHangar();// sort it!
            }
        } catch (Exception e) {
            LOGGER.error("Exception: ", e);
        }
    }

    /**
     * Called by PL|UU - updates a unit's data.
     */
    public void updateUnitData(StringTokenizer st) {
        try {
            CUnit currUnit = getUnit(TokenReader.readInt(st));
            currUnit.setData(TokenReader.readString(st));
            sortHangar();// properties have changes. sort. YARR!
        } catch (Exception e) {
            LOGGER.error("Exception: ", e);
        }
    }

    public void updateUnitMachineGuns(StringTokenizer st) {
        try {
            CUnit currUnit = getUnit(TokenReader.readInt(st));
            int location = TokenReader.readInt(st);
            int slot = TokenReader.readInt(st);
            boolean selection = TokenReader.readBoolean(st);

            CriticalSlot crit = currUnit.getEntity().getCritical(location, slot);
            crit.getMount().setRapidfire(selection);

            sortHangar();// properties have changes. sort. YARR!
        } catch (Exception e) {
            LOGGER.error("Exception: ", e);
        }
    }

    /**
     * Remove an army from a player's set. This can be called directly from a
     * PL|RA command, or indirectly by PL|SAD via CPlayer.setArmyData(), which
     * removes all old instances of an army before adding the new data.
     */
    public boolean removeArmy(int lanceID) {

        for (Iterator<CArmy> i = armies.iterator(); i.hasNext();) {
            if (i.next().getID() == lanceID) {
                i.remove();
                mwclient.getGUIClient().getMainFrame().updateAttackMenu();// removing an army
                                                                          // needs to reset
                                                                          // menu
                return (true);
            }
        }
        return (false);
    }

    /**
     * @return Returns the armies.
     */
    public List<CArmy> getArmies() {
        return armies;
    }

    public void setBays(int tbays) {
        bays = tbays;
    }

    public void setFreeBays(int freeBays) {
        this.freeBays = freeBays;
    }

    public void setHouse(String faction) {
        setMyHouse(mwclient.getData().getHouseByName(faction));
        House = faction;

        /*
         * Get the faction configs before starting anything else. I could pause
         * the client and wait for the configs but I'll let it go. --Torren
         */
        mwclient.sendChat(GameHost.CAMPAIGN_PREFIX + "c getfactionconfigs#0" + mwclient.getServerConfigs("TIMESTAMP"));

        /*
         * Now that we have a house set, we can check for BM access properly. Do
         * the BM buy and sell button checks.
         */
        if (mwclient.getGUIClient().getMainFrame().getMainPanel().getBMPanel() != null) {
            mwclient.getGUIClient().getMainFrame().getMainPanel().getBMPanel().checkFactionAccess();
        }

        /*
         * Same thing for the HQ. We have a house, so we can rebuild the button
         * bar w/ or w/o a reset button, as appropriate.
         */
        if (mwclient.getGUIClient().getMainFrame().getMainPanel().getHQPanel() != null) {
            mwclient.getGUIClient().getMainFrame().getMainPanel().getHQPanel().reinitialize();
        }
    }

    public String getHouse() {
        return House;
    }

    public void setMyHouse(House house) {
       House = house.getName();
       super.setMyHouse(house);
    }

    public void setHouseFightingFor(String faction) {
        houseFightingFor = mwclient.getData().getHouseByName(faction);
    }

    public House getHouseFightingFor() {
        return houseFightingFor;
    }

    public int getBays() {
        return bays;
    }

    public int getFreeBays() {
        return freeBays;
    }

    @Override
    public void setTechnicians(int tech) {
        super.setTechnicians(tech);
        doPayTechniciansMath();
    }

    /**
     * Calculate the the ID that would be assined to a newly created army. This
     * is used by the army builder to construct /c exm# commands for an as-yet
     * non-existant army.
     */
    public int getNextNewArmyID() {
        int newID = -1;
        int possibleNewID = 0;

        while (newID == -1) {
            for (int i = 0; i < armies.size(); i++) {
                if ((armies.get(i)).getID() == possibleNewID) {
                    newID = i;
                }
            }
            if (newID == -1) {
                newID = possibleNewID;
            } else {
                possibleNewID++;
                newID = -1;
            }
        }
        return newID;
    }

    /**
     * Method which greates an autoarmy. takes in a string with weight classes,
     * and uses server configs (path, filenames) to construct units of those
     * weights.
     *
     * Units are added to servers when a player joins a game, same as units from
     * locked armies.
     */
    public void setAutoArmy(StringTokenizer st) {
        /*
         * clear the previous autoarmy. Auto army is always called first, and is
         * cleared correctly even if only gun emplacements are sent.
         */
        autoArmy = new ArrayList<CUnit>();

        // if its a null, this was just a clearing call.
        if (st == null) {
            return;
        }

        while (st.hasMoreTokens()) {
            String filename = TokenReader.readString(st);
            if (filename.equals("CLEAR")) {
                return;
            }

            // get the distance
            int distInBoards = Integer.parseInt(mwclient.getServerConfigs("DistanceFromMap"));
            int distInHexes = distInBoards * 17;// 17 hexes per board.

            CUnit currUnit = new CUnit(mwclient);

            /*
             * This is needed to set the edge for auto arty when auto edge is
             * set for players. Else, arty edge is set in MM when the players
             * click on the edge they want.
             */
            OffBoardDirection direction = OffBoardDirection.NORTH;
            switch (mwclient.getPlayerStartingEdge()) {
                case 0:
                    break;
                case 1:
                case 2:
                case 3:
                    direction = OffBoardDirection.NORTH;
                    break;
                case 4:
                    direction = OffBoardDirection.EAST;
                    break;
                case 5:
                case 6:
                case 7:
                    direction = OffBoardDirection.SOUTH;
                    break;
                case 8:
                    direction = OffBoardDirection.WEST;
                    break;
            }

            currUnit.setAutoUnitData(filename, distInHexes, direction);
            autoArmy.add(currUnit);
        }// end while(tokens)
    }// end setAutoArmy()

    /**
     * Method which greates an autoarmy gun emplacements. takes in a string with
     * weight classes, and uses server configs (path, filenames) to construct
     * units of those weights.
     *
     * Units are added to servers when a player joins a game, same as units from
     * locked armies.
     */
    public void setAutoGunEmplacements(StringTokenizer st) {
        // if its a null, this was just a clearing call.
        if (st == null) {
            return;
        }

        while (st.hasMoreTokens()) {
            String filename = TokenReader.readString(st);
            if (filename.equals("CLEAR")) {
                return;
            }

            CUnit currUnit = new CUnit(mwclient);
            currUnit.setAutoUnitData(filename, 0, OffBoardDirection.NORTH);
            autoArmy.add(currUnit);
        }// end while(tokens)
    }// end setAutoArmy()

    public void setMULCreatedArmy(StringTokenizer st) {
        while (st.hasMoreElements()) {
            String data = TokenReader.readString(st);
            if (data.equalsIgnoreCase("CLEAR")) {
                return;
            }

            CUnit cm = new CUnit();
            cm.setData(data);
            autoArmy.add(cm);
        }
    }

    /**
     * Method which returns the autoArmy arraylist.
     */
    public List<CUnit> getAutoArmy() {
        return autoArmy;
    }

    public CArmy getArmy(int id) {
        for (CArmy currA : armies) {
            if (currA.getID() == id) {
                return currA;
            }
        }
        return null;
    }

    //@salient- compare client quirks with server
    // lol while this works, realized the way i'm doing things makes this check meaningless...
    // what needs to be checked is the hosts xmls, not the client quirks
    // which are already set by the server anyway....
    //    public String getAllQuirkInfoForActivation()
    //    {
    //        StringJoiner quirksList = new StringJoiner("*");
    //        List<Integer> idList = new ArrayList<Integer>();
    //
    //        for (CArmy currA : Armies)
    //        {
    //            for (Unit currU : currA.getUnits())
    //            {
    //                CUnit currCU = (CUnit) currU;
    //                if(currCU.hasQuirks())
    //                {
    //                    int ID = currCU.getId();
    //                    if(idList.contains(ID)) //skip dupes
    //                        continue;
    //                    idList.add(ID);
    //                    quirksList.add(String.valueOf(ID));
    //                    quirksList.add(currCU.getQuirksList());
    //                }
    //            }
    //        }
    //        LOGGER.debug(quirksList.toString());
    //        return quirksList.toString();
    //    }

    public int getAmountOfTimesUnitExistsInArmies(int unitID) {
        int result = 0;
        for (CArmy currA : armies) {
            if (currA.getUnit(unitID) != null) {
                result++;
            }
        }
        return result;
    }

    public String getArmiesUnitIsIn(int unitID) {
        StringBuilder result = new StringBuilder();
        for (CArmy currA : armies) {
            if (currA.getUnit(unitID) != null) {
                result.append(currA.getID() + " ");
            }
        }
        return result.toString();
    }

    public synchronized ArrayList<Unit> getLockedUnits() {
        ArrayList<Unit> result = new ArrayList<Unit>();
        for (CArmy currA : armies) {
            if (currA.isLocked()) {
                result.addAll(currA.getUnits());
            }
        }
        return result;
    }

    public synchronized CArmy getLockedArmy() {

        for (CArmy currA : armies) {
            if (currA.isLocked()) {
                return currA;
            }
        }
        return null;
    }

    public void addArmyUnit(String data) {
        StringTokenizer ST = new StringTokenizer(data, DELIMITER);

        if (ST.hasMoreTokens()) {
            int army = TokenReader.readInt(ST);
            int unitid = TokenReader.readInt(ST);
            int bv = TokenReader.readInt(ST);
            int position = TokenReader.readInt(ST);
            if (position >= 0) {
                getArmy(army).addUnit(getUnit(unitid), position);
            } else {
                getArmy(army).addUnit(getUnit(unitid));
            }
            getArmy(army).setBV(bv);
            sortArmies();
        }
    }

    public void removeArmyUnit(String data) {
        StringTokenizer ST = new StringTokenizer(data, DELIMITER);
        if (ST.hasMoreTokens()) {
            int army = TokenReader.readInt(ST);
            int unitid = TokenReader.readInt(ST);
            int bv = TokenReader.readInt(ST);

            Iterator<Unit> i = getArmy(army).getUnits().iterator();
            while (i.hasNext()) {
                if (i.next().getId() == unitid) {
                    i.remove();
                    getArmy(army).removeCommander(unitid); //Baruk Khazad!  20151108c it is safe to removeCommander regardless of whether isCommander or not
                    break;
                }
            }

            getArmy(army).setBV(bv);
            getArmy(army).getC3Network().remove(unitid);
        }
        mwclient.getGUIClient().refreshGUI(GUIClient.REFRESH_HQPANEL);
    }

    /**
     * Method called from PL| which updates a CArmy's legalOperations tree.
     */
    public void updateOperations(String data) {

        if (data.equals("CLEAR")) {
            for (CArmy army : getArmies()) {
                army.getLegalOperations().clear();
            }
            return;
        }

        StringTokenizer tokenizer = new StringTokenizer(data, "*");
        int armyID = TokenReader.readInt(tokenizer);
        CArmy army = getArmy(armyID);

        // System.err.println(" ArmyID: "+armyID+ " Army: "+army);
        if (army == null) {
            return;
        }

        while (tokenizer.hasMoreTokens()) {
            String mode = "";
            String name = "";

            try {
                mode = TokenReader.readString(tokenizer);
                name = TokenReader.readString(tokenizer);
            } catch (NoSuchElementException e) {
                return;
            }

            if (mode.equals("a")) {
                army.getLegalOperations().add(name);
            } else if (mode.equals("r")) {
                army.getLegalOperations().remove(name);
            }
        }// end while(more tokens)

        // update the CMainFrame Attack menu
        mwclient.getGUIClient().getMainFrame().updateAttackMenu();

    }// end updateOperations

    public void repositionArmyUnit(String data) {
        StringTokenizer ST = new StringTokenizer(data, DELIMITER);
        int army = TokenReader.readInt(ST);
        int unitid = TokenReader.readInt(ST);
        int position = TokenReader.readInt(ST);

        CArmy a = getArmy(army);

        // remove the unit
        Iterator<Unit> i = a.getUnits().iterator();
        while (i.hasNext()) {
            if (i.next().getId() == unitid) {
                i.remove();
                break;
            }
        }

        // then re-add the unit
        getArmy(army).addUnit(getUnit(unitid), position);
    }

    public void setUnitStatus(String data) {
        StringTokenizer ST = new StringTokenizer(data, DELIMITER);

        if (ST.hasMoreTokens()) {
            int unitid = TokenReader.readInt(ST);
            int status = TokenReader.readInt(ST);
            CUnit unit = getUnit(unitid);

            if (unit == null) {
                return;
            }

            if (mwclient.isUsingAdvanceRepairs() && (status == Unit.STATUS_UNMAINTAINED)) {
                unit.setStatus(Unit.STATUS_OK);
            } else {
                unit.setStatus(status);
            }
        }
    }

    public void setArmyName(String data) {
        StringTokenizer ST = new StringTokenizer(data, DELIMITER);
        if (ST.hasMoreTokens()) {
            int army = TokenReader.readInt(ST);
            String name = TokenReader.readString(ST);

            if ("-1".equals(name)) {
                name = "";
            }
            if (getArmy(army) != null) {
                getArmy(army).setName(name);
            }
        }
    }

    public void playerLockArmy(int aid) {
        if (getArmy(aid) != null) {
            getArmy(aid).playerLockArmy();
        }
    }

    public void playerUnlockArmy(int aid) {
        if (getArmy(aid) != null) {
            getArmy(aid).playerUnlockArmy();
        }
    }

    public void toggleArmyDisabled(int aid) {
        if (getArmy(aid) != null) {
            getArmy(aid).toggleArmyDisabled();
        }
    }

    public void setArmyBV(String data) {
        StringTokenizer ST = new StringTokenizer(data, DELIMITER);

        if (ST.hasMoreTokens()) {
            int army = TokenReader.readInt(ST);
            if (getArmy(army) != null) {
                getArmy(army).setBV(TokenReader.readInt(ST));
            } else {
                LOGGER.error("Bad Army id: " + army);
            }
        }
    }

    public void setArmyLimit(String data) {
        StringTokenizer ST = new StringTokenizer(data, DELIMITER);

        if (ST.hasMoreTokens()) {
            int army = TokenReader.readInt(ST);
            int lowerLimit = TokenReader.readInt(ST);
            int upperLimit = TokenReader.readInt(ST);

            getArmy(army).setLowerLimiter(lowerLimit);
            getArmy(army).setUpperLimiter(upperLimit);
        }
    }

    public void setArmyOpForceSize(String data) {
        StringTokenizer ST = new StringTokenizer(data, DELIMITER);

        if (ST.hasMoreTokens()) {
            int army = TokenReader.readInt(ST);
            float opForceSize = TokenReader.readFloat(ST);

            getArmy(army).setOpForceSize(opForceSize);
        }

    }

    public void setArmyLock(String data) {
        StringTokenizer ST = new StringTokenizer(data, DELIMITER);

        if (ST.hasMoreTokens()) {
            int army = TokenReader.readInt(ST);
            boolean lock = TokenReader.readBoolean(ST);
            getArmy(army).setLocked(lock);
        }
    }

    public void setPlayerPersonalPilotQueue(CPersonalPilotQueues queue) {
        personalPilotQueue = queue;
    }

    public CPersonalPilotQueues getPersonalPilotQueue() {
        return personalPilotQueue;
    }

    /**
     * Exclude method, called after receipt of PL|AEU| (Admin Exclude Update).
     * Because NP lists are expected to be small (2-5 players), the entire list
     * is sent every time.
     *
     * @urgru 4.3.05
     */
    public void setAdminExcludes(String buffer, String token) {
        adminExcludes.clear();
        StringTokenizer ST = new StringTokenizer(buffer, token);
        while (ST.hasMoreElements()) {
            String curr = TokenReader.readString(ST);
            if (!curr.equals("0")) {
                adminExcludes.add(curr);
            }
        }
        mwclient.getGUIClient().getMainFrame().getMainPanel().getUserListPanel().repaint();
    }

    /**
     * Exclude method, called after receipt of PL|PEU| (Player Exclude Update).
     * Because NP lists are expected to be small (2-5 players), the entire list
     * is sent every time.
     */
    public void setPlayerExcludes(String buffer, String token) {
        playerExcludes.clear();
        StringTokenizer ST = new StringTokenizer(buffer, token);
        while (ST.hasMoreElements()) {
            String curr = TokenReader.readString(ST);
            if (!curr.equals("0")) {
                playerExcludes.add(curr);
            }
        }
        mwclient.getGUIClient().getMainFrame().getMainPanel().getUserListPanel().repaint();
    }

    public List<String> getAdminExcludes() {
        return adminExcludes;
    }

    public List<String> getPlayerExcludes() {
        return playerExcludes;
    }

    /*
     * Hangar sorting mechanisms. Client and server need not order hangars in
     * the same fashion, since all transactions (after the initial data feed)
     * take place on a unit by unit basis.
     *
     * Sort options: - BV - Name - Type - Unit ID - Weight - No sort [load
     * order]
     *
     * BV is (for all intents and purposes) an exclusive sort. The others can
     * lead to significant clustering. Hence, secondary filters can be applied.
     */

    /**
     * Method which resorts every unit. Inefficient, but we hate clients.
     * Because we're evil. So there.
     *
     * @urgru 4.4.05
     */
    public void sortHangar() {
        // load configs
        String primeSortOrder = mwclient.getConfigParam("PRIMARYHQSORTORDER");
        String secondarySortOrder = mwclient.getConfigParam("SECONDARYHQSORTORDER");
        String tertiarySortOrder = mwclient.getConfigParam("TERTIARYHQSORTORDER");

        // Choices [note - this array must be duplicated in CHQPanel's
        // maybeShowPopup()]
        String[] choices =
        { "Name", "Battle Value", "Gunnery Skill", "ID Number", "MP (Jumping)", "MP (Walking)", "Pilot Kills", "Unit Type", "Weight (Class)", "Weight (Tons)", "No Sort" };

        // determine which sort will dominate
        int primarySort = CUnitComparator.HQSORT_NONE;
        for (int i = 0; i < choices.length; i++) {
            if (primeSortOrder.equals(choices[i])) {
                primarySort = i;
            }
        }

        // determine secondary sort
        int secondarySort = CUnitComparator.HQSORT_NONE;
        for (int i = 0; i < choices.length; i++) {
            if (secondarySortOrder.equals(choices[i])) {
                secondarySort = i;
            }
        }

        // determine tertiary sort
        int tertiarySort = CUnitComparator.HQSORT_NONE;
        for (int i = 0; i < choices.length; i++) {
            if (tertiarySortOrder.equals(choices[i])) {
                tertiarySort = i;
            }
        }

        // run third sort
        if ((tertiarySort != primarySort) && (tertiarySort != secondarySort) && (tertiarySort != CUnitComparator.HQSORT_NONE)) {
            sortUnits(new CUnitComparator(tertiarySort));
        }

        // run the second sort
        if ((primarySort != secondarySort) && (secondarySort != CUnitComparator.HQSORT_NONE)) {
            sortUnits(new CUnitComparator(secondarySort));
        }

        // now the primary sort
        if (primarySort != CUnitComparator.HQSORT_NONE) {
            sortUnits(new CUnitComparator(primarySort));
        }
    }

    /*
     * Hangar sorting mechanisms. Client and server need not order hangars in
     * the same fashion, since all transactions (after the initial data feed)
     * take place on a unit by unit basis.
     *
     * Sort options: - BV - Name - Type - Unit ID - Weight - No sort [load
     * order]
     *
     * BV is (for all intents and purposes) an exclusive sort. The others can
     * lead to significant clustering. Hence, secondary filters can be applied.
     */

    /**
     * Method which resorts every unit. Inefficient, but we hate clients.
     * Because we're evil. So there.
     *
     * @urgru 4.4.05
     */
    public void sortArmies() {
        // load configs
        String primeSortOrder = mwclient.getConfigParam("PRIMARYARMYSORTORDER");
        String secondarySortOrder = mwclient.getConfigParam("SECONDARYARMYSORTORDER");
        String tertiarySortOrder = mwclient.getConfigParam("TERTIARYARMYSORTORDER");

        // Choices [note - this array must be duplicated in CHQPanel's
        // maybeShowPopup()]
        String[] choices =
        { "Name", "Battle Value", "ID Number", "Max Tonnage", "Avg Walk MP", "Avg Jump MP", "Number Of Units", "No Sort" };

        // determine which sort will dominate
        int primarySort = CArmyComparator.ARMYSORT_NONE;
        for (int i = 0; i < choices.length; i++) {
            if (primeSortOrder.equals(choices[i])) {
                primarySort = i;
            }
        }

        // determine secondary sort
        int secondarySort = CArmyComparator.ARMYSORT_NONE;
        for (int i = 0; i < choices.length; i++) {
            if (secondarySortOrder.equals(choices[i])) {
                secondarySort = i;
            }
        }

        // determine tertiary sort
        int tertiarySort = CArmyComparator.ARMYSORT_NONE;
        for (int i = 0; i < choices.length; i++) {
            if (tertiarySortOrder.equals(choices[i])) {
                tertiarySort = i;
            }
        }

        // we know this holds CUnits. Can safely cast.
        Object[] armiesArray = armies.toArray();

        // run third sort
        if ((tertiarySort != primarySort) && (tertiarySort != secondarySort) && (tertiarySort != CArmyComparator.ARMYSORT_NONE)) {
            Arrays.sort(armiesArray, new CArmyComparator(tertiarySort));
        }

        // run the second sort
        if ((primarySort != secondarySort) && (secondarySort != CArmyComparator.ARMYSORT_NONE)) {
            Arrays.sort(armiesArray, new CArmyComparator(secondarySort));
        }

        // now the primary sort
        if (primarySort != CArmyComparator.ARMYSORT_NONE) {
            Arrays.sort(armiesArray, new CArmyComparator(primarySort));
        }

        // overwrite the hangar with a new arraylist constructed from the
        // unitsArray.
        ArrayList<CArmy> Army2 = new ArrayList<>();
        for (Object element : armiesArray) {
            Army2.add((CArmy) element);
        }

        // replace the hangar and flush the array
        armies = Army2;
        armiesArray = null;
    }

    public int getHangarSpaceRequired(int typeid, int weightclass, int baymod, String model) {
        if (typeid == Unit.PROTOMEK) {
            return 0;
        }

        if ((typeid == Unit.INFANTRY) && Boolean.parseBoolean(mwclient.getServerConfigs("FootInfTakeNoBays"))) {
            // check types
            boolean isFoot = model.startsWith("Foot");
            boolean isAMFoot = model.startsWith("Anti-Mech Foot");

            if (isFoot || isAMFoot) {
                return 0;
            }
        }

        int result = 1;
        String techAmount = "TechsFor" + Unit.getWeightClassDesc(weightclass) + Unit.getTypeClassDesc(typeid);
        result = Integer.parseInt(mwclient.getServerConfigs(techAmount));

        // Apply Pilot Mods (Astech skill)
        if (!mwclient.isUsingAdvanceRepairs()) {
            result += baymod;
        }

        // no negative techs
        if (result < 0) {
            result = 0;
        }

        return result;
    }// end getHangarSpaceRequired()

    public void applyUnitRepairs(StringTokenizer data) {
        CUnit unit = getUnit(TokenReader.readInt(data));
        unit.applyRepairs(TokenReader.readString(data));
    }

    public void updateTotalTechs(String data) {
        StringTokenizer techs = new StringTokenizer(data, "%");
        int slot = 0;

        while (techs.hasMoreTokens()) {
            setTotalTechs(slot, TokenReader.readInt(techs));
            slot++;
        }
    }

    public void updateAvailableTechs(String data) {
        StringTokenizer techs = new StringTokenizer(data, "%");
        int slot = 0;

        while (techs.hasMoreTokens()) {
            setAvailableTechs(slot, TokenReader.readInt(techs));
            slot++;
        }
    }

    public void setRepairLocation(int loc) {
        repairLocation = loc;
    }

    public int getRepairLocation() {
        return repairLocation;
    }

    public void setRepairTechType(int type) {
        repairTechType = type;
    }

    public int getRepairTechType() {
        return repairTechType;
    }

    public void setRepairRetries(int retries) {
        repairRetries = retries;
    }

    public int getRepairRetries() {
        return repairRetries;
    }

    public void resetRepairs() {
        repairLocation = 0;
        repairTechType = 0;
        repairRetries = 0;
    }

    public void setConventionalMinesAllowed(int mines) {
        conventionalMinesAllowed = mines;
    }

    public int getConventionalMinesAllowed() {
        return conventionalMinesAllowed;
    }

    public void setVibraMinesAllowed(int mines) {
        vibraMinesAllowed = mines;
    }

    public int getVibraMinesAllowed() {
        return vibraMinesAllowed;
    }

    public void setMines(StringTokenizer st) {
        setConventionalMinesAllowed(TokenReader.readInt(st));
        setVibraMinesAllowed(TokenReader.readInt(st));
    }

    public void setFactionConfigs(String data) {
        /**
         * FIXME: This is a hack. Currently the house config files only exist on the server and are
         * then appended to the campaignconfig.txt above. In order to make the server and client
         * both use the House's config file we create a dummy config below that will have no
         * parameters and pass through to the campaignconfig.txt. Later this hack will be removed
         * when house config files exist properly on the client side.
         */
        if (CampaignData.cd.getHouseOptions(getMyHouse().getName()) == null) {
            Path configPath = FileSystem.getInstance().getFactionConfigPath(getMyHouse().getName());

            CampaignData.cd.loadHouseOptions(configPath, getMyHouse());
        }

        if (data.startsWith("DONE#DONE")) {
            mwclient.setWaiting(false);
            return;
        }

        StringTokenizer ST = new StringTokenizer(data, DELIMITER);
        // mwclient.getServerConfigs().clear();
        // mwclient.getServerConfigData();
        while (ST.hasMoreTokens()) {
            String key = TokenReader.readString(ST);
            String value = TokenReader.readString(ST);

            mwclient.getServerConfigs().setProperty(key, value);
        }
        mwclient.setWaiting(false);
    }

    public UnitComponents getPartsCache() {
        return partsCache;
    }

    public void setSubFaction(String name) {
        subFactionName = name;
    }

    public SubFaction getSubFaction() {
        SubFaction mySubFaction = getMyHouse().getSubFactionList().get(subFactionName);
        if (mySubFaction == null) {
            return new SubFaction();
        }

        return mySubFaction;
    }

    public int getSubFactionAccess() {
        SubFaction mySubFaction = getMyHouse().getSubFactionList().get(subFactionName);
        if (mySubFaction == null) {
            return 0;
        }

        return Integer.parseInt(mySubFaction.getConfig("AccessLevel"));
    }

    public String getSubFactionName() {
        return subFactionName;
    }

    public int getHangarPenalty() {
        return hangarPenalty;
    }

    public int getHangarPurchasePenalty(int type, int weight) {
        return hangarPurchasePenalties[type][weight];
    }

    public void setHangarPenalty(int p) {
        hangarPenalty = p;
    }

    public void setHangarPurchasePenalty(int type, int weight, int p) {
        hangarPurchasePenalties[type][weight] = p;
    }

    public void parseHangarPenaltyString(String readString) {
        StringTokenizer st = new StringTokenizer(readString, "*");
        setHangarPenalty(Integer.parseInt(st.nextToken()));
        for (int type = Unit.MEK; type < Unit.MAXBUILD; type++) {
            for (int weight = Unit.LIGHT; weight <= Unit.ASSAULT; weight++) {
                setHangarPurchasePenalty(type, weight, Integer.parseInt(st.nextToken()));
            }
        }
        mwclient.getGUIClient().getMainFrame().getMainPanel().getHSPanel().updateDisplay();
    }
}
