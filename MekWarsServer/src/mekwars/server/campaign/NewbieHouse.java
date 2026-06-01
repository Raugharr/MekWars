/*
 * MekWars - Copyright (C) 2004
 *
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet)
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

package mekwars.server.campaign;

import mekwars.common.CampaignData;
import mekwars.common.Unit;
import mekwars.common.campaign.CampaignOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class NewbieHouse extends NonConqHouse {
    TreeMap<String, Integer> resetPlayers = new TreeMap<String, Integer>();

    /** Used for serialization */
    public NewbieHouse() {
        super();
    }

    public NewbieHouse(
            String name, String houseColor, int baseGunner, int basePilot, String abbreviation) {
        super(name, houseColor, baseGunner, basePilot, abbreviation);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("[N]");
        result.append(super.toString());
        return result.toString();
    }

    /**
     * @urgru 8/18/04
     *     <p>Override the getBaysProvided from SHouse and return NewbieHouseBays from the
     *     campaignconfig.txt
     * @return int number of bays given to SOLies
     */
    @Override
    public int getBaysProvided() {
        return CampaignData.cd.getCampaignOptions().getIntegerConfig("NewbieHouseBays");
    }

    public String cleanupHangarAndPP() {
        hangar.clear();
        return "";
    }

    @Override
    public SUnit getEntity(int weightClass, int typeId) {
        SUnit m = super.getEntity(weightClass, typeId);
        if (m == null) {
            m = this.getRandomUnit(typeId, weightClass, null).get(0);
        }
        return m;
    }

    public void setPP(int weight, int val) {
        // Newbiefaction can never get PP
        // currentPP.setElementAt(new Integer(0),weight-1);
    }

    public String potentialHouseProduction() {
        return "";
    }

    @Override
    public int getMoney() {
        return 0;
    }

    @Override
    public int getPP(int weight, int typeId) {
        // Always enough components to get raided..
        return 1 * this.getPPCost(weight, typeId);
    }

    public List<SUnit> getRandomUnit(int unitType, int weightClass, String houseName) {
        String factionName;
        if (houseName == null) factionName = CampaignMain.cm.getConfig("NewbieHouseName");
        else factionName = houseName;

        String unitFilename =
                BuildTable.getUnitFilename(
                        factionName,
                        Unit.getWeightClassDesc(weightClass),
                        unitType,
                        BuildTable.STANDARD);

        List<SUnit> newbieUnits = new ArrayList<>();
        if (unitFilename.toLowerCase().trim().endsWith(".mul")) {
            newbieUnits.addAll(SUnit.createMULUnits(this, unitFilename, "Training Unit"));
        } else {
            // build the new unit
            SUnit newbieUnit = new SUnit(factionName, unitFilename, weightClass);
            newbieUnit.setProducer("Training Unit");
            newbieUnits.add(newbieUnit);
        }
        return newbieUnits;
    }

    /**
     * Method that checks a player's eligibility for a unit reset and assigns new units if the
     * player is under-armed. Also used by DefectCommand to strip and replace a player's SOL units
     * if so configured.
     *
     * @param p - player requesting new units
     * @param forceReset - if true, ignore player's unit count. used by DefectCommand to always pass
     *     reset check.
     * @return - Human readable outcome of request.
     */
    public String requestNewMech(SPlayer p, boolean forceReset, String houseName) {

        // don't let fighting player's change their units.
        if (p.getDutyStatus() == SPlayer.STATUS_FIGHTING)
            return "You may not request new units while playing a game.";

        if (p.getDutyStatus() == SPlayer.STATUS_ACTIVE)
            return "You may not request new units while on active duty.";

        String lowerName = p.getName().toLowerCase();
        String toSend = "Your units were reset";

        /*
         * see if the player has enough units already. if this is a defection or the
         * player is immune and has resets remaining, ignore the number of units in
         * his hangar and generate a new set.
         */
        int replace = CampaignMain.cm.getIntegerConfig("NumUnitsToQualifyForNew");
        if (forceReset) replace = 999999;

        if (resetPlayers.containsKey(lowerName) && resetPlayers.get(lowerName) > 0) {
            replace = 999999;

            // decrement the reset counter & tell player
            int remainingResets = resetPlayers.get(lowerName) - 1;
            toSend += " (" + remainingResets + " post-game resets remaining).";
            resetPlayers.put(lowerName, remainingResets);

        } else {
            toSend += ".";
        }

        if (p.getUnits().size() > replace) return "You already have enough units.";

        // get new units, replace PPQ, then resend the player's data
        p.stripOfAllUnits(false);
        if (CampaignMain.cm.getBooleanConfig("AllowPersonalPilotQueues")) {
            p.getPersonalPilotQueue().flushQueue();
            CampaignMain.cm.toUser(
                    "PL|PPQ|" + p.getPersonalPilotQueue().toString(true), p.getName(), false);
        }

        getNewSOLUnits(p, houseName);

        /*
         * send complete army/unit/tech update if this is a normal reset, but
         * refrain if this is a defection (forceReset), in which case the status
         * will be completely reset during login to newHouse.
         */
        if (!forceReset) CampaignMain.cm.toUser("PS|" + p.toString(true), p.getName(), false);

        // inform him of the positive outcome
        return toSend;
    }

    public void addResetPlayer(SPlayer p, Integer numResets) {
        this.resetPlayers.put(p.getName().toLowerCase(), numResets);
    }

    public void removeResetPlayer(SPlayer p) {
        this.resetPlayers.remove(p.getName().toLowerCase());
    }

    public int getResetsRemaining(SPlayer p) {
        return this.resetPlayers.get(p.getName().toLowerCase());
    }

    /**
     * Override SHouse.removePlayer() in order to add a reset removal. Keeps SPlayers who defect out
     * of the reset list.
     */
    @Override
    public void removePlayer(SPlayer p, boolean donateMechs) {
        super.removePlayer(p, donateMechs);
        this.removeResetPlayer(p);
    }

    @Override
    public boolean isNewbieHouse() {
        return true;
    }

    /**
     * A method which gets a new SOL force and assigns it to a player. Top heavy b/c of the number
     * of server configurables involved.
     *
     * <p>Note that the player is *not* assured 1 elite pilot and 1 green pilot (this differs from
     * the old MMNET implementation). Pilots may still be elite or green randomly.
     *
     * <p>Retuns a string which is (sometimes) added to the enroll/reset messages sent to the
     * player.
     *
     * @urgru 12/29/04
     */
    public String getNewSOLUnits(SPlayer p, String houseName) {
        CampaignOptions campaignOptions = CampaignData.cd.getCampaignOptions();
        List<SUnit> units = new ArrayList<>();

        // meks
        int numLMeks = campaignOptions.getIntegerConfig("SOLLightMeks");
        int numMMeks = campaignOptions.getIntegerConfig("SOLMediumMeks");
        int numHMeks = campaignOptions.getIntegerConfig("SOLHeavyMeks");
        int numAMeks = campaignOptions.getIntegerConfig("SOLAssaultMeks");

        // vehicles
        int numLVehs = campaignOptions.getIntegerConfig("SOLLightVehs");
        int numMVehs = campaignOptions.getIntegerConfig("SOLMediumVehs");
        int numHVehs = campaignOptions.getIntegerConfig("SOLHeavyVehs");
        int numAVehs = campaignOptions.getIntegerConfig("SOLAssaultVehs");

        // infantry
        int numLInf = campaignOptions.getIntegerConfig("SOLLightInf");
        int numMInf = campaignOptions.getIntegerConfig("SOLMediumInf");
        int numHInf = campaignOptions.getIntegerConfig("SOLHeavyInf");
        int numAInf = campaignOptions.getIntegerConfig("SOLAssaultInf");

        // protomechs
        int numLPM = campaignOptions.getIntegerConfig("SOLLightProtoMek");
        int numMPM = campaignOptions.getIntegerConfig("SOLMediumProtoMek");
        int numHPM = campaignOptions.getIntegerConfig("SOLHeavyProtoMek");
        int numAPM = campaignOptions.getIntegerConfig("SOLAssaultProtoMek");

        // BattleArmor
        int numLBA = campaignOptions.getIntegerConfig("SOLLightBattleArmor");
        int numMBA = campaignOptions.getIntegerConfig("SOLMediumBattleArmor");
        int numHBA = campaignOptions.getIntegerConfig("SOLHeavyBattleArmor");
        int numABA = campaignOptions.getIntegerConfig("SOLAssaultBattleArmor");

        // Aero
        int numLAero = campaignOptions.getIntegerConfig("SOLLightAero");
        int numMAero = campaignOptions.getIntegerConfig("SOLMediumAero");
        int numHAero = campaignOptions.getIntegerConfig("SOLHeavyAero");
        int numAAero = campaignOptions.getIntegerConfig("SOLAssaultAero");

        // for loops.
        for (int i = 0; i < numLMeks; i++) {
            units.addAll(this.getRandomUnit(Unit.MEK, Unit.LIGHT, houseName));
        }

        for (int i = 0; i < numMMeks; i++) {
            units.addAll(this.getRandomUnit(Unit.MEK, Unit.MEDIUM, houseName));
        }

        for (int i = 0; i < numHMeks; i++) {
            units.addAll(this.getRandomUnit(Unit.MEK, Unit.HEAVY, houseName));
        }

        for (int i = 0; i < numAMeks; i++) {
            units.addAll(this.getRandomUnit(Unit.MEK, Unit.ASSAULT, houseName));
        }

        for (int i = 0; i < numLVehs; i++) {
            units.addAll(this.getRandomUnit(Unit.VEHICLE, Unit.LIGHT, houseName));
        }

        for (int i = 0; i < numMVehs; i++) {
            units.addAll(this.getRandomUnit(Unit.VEHICLE, Unit.MEDIUM, houseName));
        }

        for (int i = 0; i < numHVehs; i++) {
            units.addAll(this.getRandomUnit(Unit.VEHICLE, Unit.HEAVY, houseName));
        }

        for (int i = 0; i < numAVehs; i++) {
            units.addAll(this.getRandomUnit(Unit.VEHICLE, Unit.ASSAULT, houseName));
        }

        for (int i = 0; i < numLInf; i++) {
            units.addAll(this.getRandomUnit(Unit.INFANTRY, Unit.LIGHT, houseName));
        }

        for (int i = 0; i < numMInf; i++) {
            units.addAll(this.getRandomUnit(Unit.INFANTRY, Unit.MEDIUM, houseName));
        }

        for (int i = 0; i < numHInf; i++) {
            units.addAll(this.getRandomUnit(Unit.INFANTRY, Unit.HEAVY, houseName));
        }

        for (int i = 0; i < numAInf; i++) {
            units.addAll(this.getRandomUnit(Unit.INFANTRY, Unit.ASSAULT, houseName));
        }

        for (int i = 0; i < numLPM; i++) {
            units.addAll(this.getRandomUnit(Unit.PROTOMEK, Unit.LIGHT, houseName));
        }

        for (int i = 0; i < numMPM; i++) {
            units.addAll(this.getRandomUnit(Unit.PROTOMEK, Unit.MEDIUM, houseName));
        }

        for (int i = 0; i < numHPM; i++) {
            units.addAll(this.getRandomUnit(Unit.PROTOMEK, Unit.HEAVY, houseName));
        }

        for (int i = 0; i < numAPM; i++) {
            units.addAll(this.getRandomUnit(Unit.PROTOMEK, Unit.ASSAULT, houseName));
        }

        for (int i = 0; i < numLBA; i++) {
            units.addAll(this.getRandomUnit(Unit.BATTLEARMOR, Unit.LIGHT, houseName));
        }

        for (int i = 0; i < numMBA; i++) {
            units.addAll(this.getRandomUnit(Unit.BATTLEARMOR, Unit.MEDIUM, houseName));
        }

        for (int i = 0; i < numHBA; i++) {
            units.addAll(this.getRandomUnit(Unit.BATTLEARMOR, Unit.HEAVY, houseName));
        }

        for (int i = 0; i < numABA; i++) {
            units.addAll(this.getRandomUnit(Unit.BATTLEARMOR, Unit.ASSAULT, houseName));
        }

        for (int i = 0; i < numLAero; i++) {
            units.addAll(this.getRandomUnit(Unit.AERO, Unit.LIGHT, houseName));
        }

        for (int i = 0; i < numMAero; i++) {
            units.addAll(this.getRandomUnit(Unit.AERO, Unit.MEDIUM, houseName));
        }

        for (int i = 0; i < numHAero; i++) {
            units.addAll(this.getRandomUnit(Unit.AERO, Unit.HEAVY, houseName));
        }

        for (int i = 0; i < numAAero; i++) {
            units.addAll(this.getRandomUnit(Unit.AERO, Unit.ASSAULT, houseName));
        }

        // now add the units to player and get a return string
        StringBuilder toReturn = new StringBuilder();
        for (SUnit currUnit : units) {

            // add the unit to the player and the tracker
            p.addUnit(currUnit, true, false);

            // construct the info string
            toReturn.append(currUnit.getVerboseModelName());

            toReturn.append(", ");
        }
        // remove last 2 chars (", ")
        if (units.isEmpty()) {
            toReturn.delete(toReturn.length() - 2, toReturn.length());
        }

        return toReturn.toString();
    }
}
