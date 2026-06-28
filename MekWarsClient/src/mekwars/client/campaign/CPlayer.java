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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import jakarta.persistence.Entity;

import mekwars.client.GUIClientConfig;
import mekwars.client.MWClient;
import mekwars.client.common.campaign.clientutils.GameHost;
import mekwars.client.campaign.sort.ArmySorter;
import mekwars.client.campaign.sort.HangarSorter;
import mekwars.client.io.FileSystem;
import mekwars.common.CampaignData;
import mekwars.common.House;
import mekwars.common.Player;
import mekwars.common.Unit;
import mekwars.common.util.TokenReader;
import mekwars.common.util.UnitUtils;
import megamek.common.CriticalSlot;
import megamek.common.OffBoardDirection;
import mekwars.common.composition.HasUnits;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class for Player object used by Client
 */
@Entity
public class CPlayer extends Player {
    private static final Logger LOGGER = LogManager.getLogger(CPlayer.class);

    public static final String DELIMITER = "#"; // delimiter for player strings

    private String House;

    private int bays;
    private int freeBays = 0;
    private int hangarPenalty;
    private int hangarPurchasePenalties[][] = new int[6][4];

    private HasUnits<CUnit> units = new HasUnits<>();
    private List<CArmy> armies;

    private List<String> adminExcludes;
    private List<String> playerExcludes;

    private CPersonalPilotQueues personalPilotQueue;

    private House houseFightingFor = null;

    private int repairLocation = 0;
    private int repairTechType = 0;
    private int repairRetries = 0;

    private int conventionalMinesAllowed = 0;
    private int vibraMinesAllowed = 0;

    public CPlayer() {
        bays = 0;
        House = "";
        armies = new ArrayList<>();
        setMyHouse(new House());
        personalPilotQueue = new CPersonalPilotQueues();
        adminExcludes = new ArrayList<String>();
        playerExcludes = new ArrayList<String>();
    }

    /**
     * @see IHasUnits#getUnits
     */
    @Override
    public List<CUnit> getUnits() {
        return (List<CUnit>) Collections.unmodifiableList(units.getAll());
    }

    /**
     * @see IHasUnits#getUnit(int)
     */
    @Override
    public CUnit getUnit(int id) {
        return units.get(id);
    }

    /**
     * @see IHasUnits#addUnit(Unit, int)
     */
    @Override
    public void addUnit(int position, Unit unit) {
        unit.setOwner(this);
        units.add(position, (CUnit) unit);
    }

    /**
     * @see IHasUnits#addUnit(Unit)
     */
    @Override
    public void addUnit(Unit unit) {
        unit.setOwner(this);
        units.add((CUnit) unit);
    }

    /**
     * @see IHasUnits#removeUnit(int)
     */
    @Override
    public boolean removeUnit(int id) {
        CUnit unit = units.get(id);
        
        if (unit != null) {
            unit.setOwner(null);
            units.remove(id);
            return true;
        }
        return false;
    }

    /**
     * @see IHasUnits#getUnitCount()
     */
    @Override
    public int getUnitCount() {
        return units.count();
    }

    /**
     * @see IHasUnits#countUnits(int, int)
     */
    @Override
    public int countUnits(int type, int weightClass) {
        return units.count(type, weightClass);
    }

    /**
     * @see IHasUnits#clear()
     */
    @Override
    public void clearUnits() {
        units.clear();
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
        newArmy.fromString(data, this, "%");

        // Save the old army's legal operations.
        CArmy oldArmy = getArmy(newArmy.getId());
        if (oldArmy != null) {
            newArmy.setLegalOperations(oldArmy.getLegalOperations());
        }

        // swap the armies
        removeArmy(newArmy.getId());
        if (armies.size() < newArmy.getId()) {
            armies.add(newArmy);
        } else {
            armies.add(newArmy.getId(), newArmy);
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
            tmek = new CUnit();
            if (tmek.setData(TokenReader.readString(ST))) {
                addUnit(tmek);
                LOGGER.debug("Adding unit {} to hanger", tmek.checkModelName());
            }
        }

        Armiescount = (TokenReader.readInt(ST));
        for (i = 0; i < Armiescount; i++) {
            CArmy army = new CArmy();
            army.fromString(TokenReader.readString(ST), this, "%");
            armies.add(army);
            LOGGER.debug("Adding army {} with {} units", army.getName(), army.getUnitCount());
        }

        bays = TokenReader.readInt(ST);
        freeBays = TokenReader.readInt(ST);
        setRating(Double.parseDouble(TokenReader.readString(ST)));
        setInfluence(TokenReader.readInt(ST));
        setTechnicians(TokenReader.readInt(ST));
        doPayTechniciansMath();
        setRewardPoints(TokenReader.readInt(ST));
        String string = TokenReader.readString(ST);
        setMekTokens(Integer.parseInt(string));
        House = TokenReader.readString(ST);
        setHouseFightingFor(TokenReader.readString(ST));
        setLogo(TokenReader.readString(ST));
        setInvisible(TokenReader.readBoolean(ST));

        if (CampaignData.cd.getCampaignOptions().getBooleanConfig("UsePartsRepair")) {
            getUnitComponents().fromString(TokenReader.readString(ST), "|");
        } else {
            TokenReader.readString(ST);
        }

        setAutoReorder(TokenReader.readBoolean(ST));

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
            CUnit unit = new CUnit();
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
            if (i.next().getId() == lanceID) {
                i.remove();
                return true;
            }
        }
        return false;
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
        setMyHouse(CampaignData.cd.getHouseByName(faction));
        House = faction;
    }

    public String getHouse() {
        return House;
    }

    public void setMyHouse(House house) {
       House = house.getName();
       super.setMyHouse(house);
    }

    public void setHouseFightingFor(String faction) {
        houseFightingFor = CampaignData.cd.getHouseByName(faction);
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
                if ((armies.get(i)).getId() == possibleNewID) {
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

    public CArmy getArmy(int id) {
        for (CArmy currA : armies) {
            if (currA.getId() == id) {
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
                result.append(currA.getId() + " ");
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
                getArmy(army).addUnit(position, getUnit(unitid));
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
            int unitId = TokenReader.readInt(ST);
            int bv = TokenReader.readInt(ST);

            Iterator<CUnit> iterator = getArmy(army).getUnits().iterator();
            while (iterator.hasNext()) {
                CUnit unit = iterator.next();

                if (unit.getId() == unitId) {
                    iterator.remove();
                    getArmy(army).removeCommander(unitId); //Baruk Khazad!  20151108c it is safe to removeCommander regardless of whether isCommander or not
                    break;
                }
            }

            getArmy(army).setBV(bv);
            getArmy(army).getC3Network().remove(unitId);
        }
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
    }// end updateOperations

    public void repositionArmyUnit(String data) {
        StringTokenizer ST = new StringTokenizer(data, DELIMITER);
        int army = TokenReader.readInt(ST);
        int unitid = TokenReader.readInt(ST);
        int position = TokenReader.readInt(ST);

        CArmy a = getArmy(army);

        // remove the unit
        Iterator<CUnit> iterator = a.getUnits().iterator();
        while (iterator.hasNext()) {
            CUnit unit = iterator.next();

            if (unit.getId() == unitid) {
                iterator.remove();
                break;
            }
        }

        // then re-add the unit
        getArmy(army).addUnit(position, getUnit(unitid));
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

            if (CampaignData.cd.isUsingAdvanceRepair() && (status == Unit.STATUS_UNMAINTAINED)) {
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

    @Override
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
    }

    public List<String> getAdminExcludes() {
        return adminExcludes;
    }

    public List<String> getPlayerExcludes() {
        return playerExcludes;
    }

    /**
     * Method which resorts every unit.
     */
    public void sortHangar() {
        HangarSorter.sort(units.getAll(),
                List.of(
                        GUIClientConfig.getInstance().getParam("PRIMARYHQSORTORDER"),
                        GUIClientConfig.getInstance().getParam("SECONDARYHQSORTORDER"),
                        GUIClientConfig.getInstance().getParam("TERTIARYHQSORTORDER")));
    }

    /**
     * Method which resorts every army.
     */
    public void sortArmies() {
        ArmySorter.sort(getArmies(),
                List.of(
                        GUIClientConfig.getInstance().getParam("PRIMARYARMYSORTORDER"),
                        GUIClientConfig.getInstance().getParam("SECONDARYARMYSORTORDER"),
                        GUIClientConfig.getInstance().getParam("TERTIARYARMYSORTORDER")));
    }

    public int getHangarSpaceRequired(int typeid, int weightclass, int baymod, String model) {
        if (typeid == Unit.PROTOMEK) {
            return 0;
        }

        if ((typeid == Unit.INFANTRY) && CampaignData.cd.getCampaignOptions().getBooleanConfig("FootInfTakeNoBays")) {
            // check types
            boolean isFoot = model.startsWith("Foot");
            boolean isAMFoot = model.startsWith("Anti-Mech Foot");

            if (isFoot || isAMFoot) {
                return 0;
            }
        }

        int result = 1;
        String techAmount = "TechsFor" + Unit.getWeightClassDesc(weightclass) + Unit.getTypeClassDesc(typeid);
        result = CampaignData.cd.getCampaignOptions().getIntegerConfig(techAmount);

        // Apply Pilot Mods (Astech skill)
        if (!CampaignData.cd.isUsingAdvanceRepair()) {
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
    }
}
