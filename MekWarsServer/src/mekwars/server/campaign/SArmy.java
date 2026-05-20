/*
 * MekWars - Copyright (C) 2004
 *
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet)
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */

/*
 * Created on 21.05.2004
 */

package mekwars.server.campaign;

import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import megamek.common.Aero;
import megamek.common.AmmoType;
import megamek.common.BattleArmor;
import megamek.common.Crew;
import megamek.common.Entity;
import megamek.common.Infantry;
import megamek.common.Mech;
import megamek.common.Mounted;
import megamek.common.Protomech;
import megamek.common.Tank;
import megamek.common.VTOL;
import megamek.common.battlevalue.BVCalculator;

import mekwars.common.Army;
import mekwars.common.CampaignData;
import mekwars.common.Player;
import mekwars.common.Unit;
import mekwars.common.campaign.operations.Operation;
import mekwars.common.util.TokenReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Helge Richter
 */
@jakarta.persistence.Entity
public class SArmy extends Army<SUnit> {
    private static final Logger LOGGER = LogManager.getLogger(SArmy.class);

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(
        name = "army_opponents",
        joinColumns = @JoinColumn(name = "army_id"),
        inverseJoinColumns = @JoinColumn(name = "oppend_army_id")
    )
    private List<SArmy> opponents = new ArrayList<>();

    public SArmy(Player owner) {
        super(owner);
    }

    public SArmy(int id, Player owner) {
        super(owner);
        setId(id);
    }

    public void addUnit(SUnit u) {
        super.addUnit(u);
        super.setBV(0);
        setRawForceSize(-1);
    }

    public void addUnit(SUnit u, int position) {
        super.addUnit(u, position);
        super.setBV(0);
        setRawForceSize(-1);
    }

    public void removeUnit(int id) {
        Iterator<SUnit> iterator = getUnits().iterator();
        while (iterator.hasNext()) {
            SUnit unit = iterator.next();

            if (unit.getId() == id) {
                iterator.remove();
                break;
            }
        }

        removeUnitFromC3Network(id);
        super.setBV(0);
        setRawForceSize(-1);
        removeCommander(id);
    }

    public int getSemiGuidedBV() {
        int bv = 0;

        for (SUnit unit : getUnits()) {
            for (Mounted ammo : unit.getEntity().getAmmo()) {
                if (((AmmoType) ammo.getType())
                        .getMunitionType()
                        .contains(AmmoType.Munitions.M_SEMIGUIDED)) {
                    bv += ((AmmoType) ammo.getType()).getBV(unit.getEntity());
                }
            }
        }

        return bv;
    }

    @Override
    public int getBV() {
        if (super.getBV() == 0) {
            calcBV();
        }
        return super.getBV();
    }

    public void calcBV() {
        int total = 0;
        int subTotal = 0;
        double c3BV = 0;

        boolean hasTAGHomingCombo = hasTAGAndHomingCombo();
        boolean hasSemiGuided = hasTAGAndSemiGuidedCombo();

        for (Unit currU : getUnits()) {
            // Bad units in the queue(possible issues with rest. best to protect
            // now.
            if (currU == null) {
                continue;
            }

            SUnit u = (SUnit) currU;

            c3BV = u.getBVForMatch();

            if (u.hasBeenC3LinkedTo(this) || getC3Network().get(u.getId()) != null) {
                int totalForceBV = 0;
                totalForceBV += u.getEntity().calculateBattleValue(true, true);
                for (Unit c3Unit : getUnits()) {
                    if (c3Unit == null) {
                        continue;
                    }

                    SUnit subUnit = (SUnit) c3Unit;

                    if (!u.equals(subUnit) && isSameC3Network(u.getId(), subUnit.getId())) {
                        totalForceBV += subUnit.getEntity().calculateBattleValue(true, true);
                    }
                }
                c3BV += totalForceBV *= 0.05;
            }

            subTotal = (int) Math.round(c3BV);

            // Arrow IV adjustments
            if (hasTAGHomingCombo) {
                final Crew crew = u.getEntity().getCrew();
                double temp =
                        subTotal
                                / BVCalculator.bvSkillMultiplier(
                                        crew.getGunnery(), crew.getPiloting());
                if (u.hasTAG()) {
                    temp += 200;
                }
                if (u.hasHoming()) {
                    temp += 200;
                }
                temp *= BVCalculator.bvSkillMultiplier(crew.getGunnery(), crew.getPiloting());
                subTotal = (int) temp;
            }
            total += subTotal;
        }

        if (hasSemiGuided) {
            total += getSemiGuidedBV();
        }
        super.setBV(total);
    }

    /**
     * Method which compares two armies and returns a boolean which indicates whether they fall
     * within each others' unit limits and have a generic BV match.
     */
    public boolean matches(SArmy enemy, Operation operation) {
        int flatCap = operation.getIntValue("MaxBVDifference");
        double percentCap = operation.getDoubleValue("MaxBVPercent");

        // catch a 0 BV, just in case getBV(false) calls lead here
        if (enemy.getBV() == 0 && !operation.getBooleanValue("MULArmiesOnly")) {
            return false;
        }

        // determine BV difference between the two armies
        int enemyOpBV = enemy.getOperationsBV(this);
        int myOpBV = getOperationsBV(enemy);
        int bvDiff = Math.abs(enemyOpBV - myOpBV);

        // percentage caps arent being used, only check the straight cap from
        // the params
        if (percentCap == 0) {
            if (bvDiff > flatCap) {
                return false;
            }
        // percentage caps are being used. see which is larger
        // (percent or straight) and check as appropriate.
        } else {
            double percentDiff = 0;

            // use smaller army to determine percentage; gives narrowest legal
            // range possible
            // TODO: smalledDiff and smalledBV are identical, don't know why.
            double smallestDiff = Math.min(enemyOpBV, myOpBV);
            double smallestBV = Math.min(enemyOpBV, myOpBV);
            double precentTotal = percentCap * smallestBV;

            percentDiff = (double) bvDiff / smallestDiff;

            if (precentTotal < flatCap) {
                if (bvDiff > flatCap) {
                    return false;
                }
            } else { // percent cap is greater than flat
                if (percentDiff > percentCap) {
                    return false;
                }
            }
        }
        return true;
    } 

    public String getInaccurateDescription() {
        if (CampaignData.cd.getCampaignOptions().getBooleanConfig("ShowUnitTypeCounts")) {
            StringBuilder toReturn = new StringBuilder("(Units: ");
            int numMechs = 0,
                    numVees = 0,
                    numVTOLs = 0,
                    numInf = 0,
                    numProtos = 0,
                    numBA = 0,
                    numAero = 0;
            for (Unit unit : getUnits()) {
                Entity e =
                        CampaignMain.cm
                                .getPlayer(getOwner().getName())
                                .getUnit(unit.getId())
                                .getEntity();
                if (e instanceof Mech) {
                    numMechs++;
                } else if (e instanceof VTOL) {
                    numVTOLs++;
                } else if (e instanceof Tank) {
                    numVees++;
                } else if (e instanceof Infantry) {
                    numInf++;
                } else if (e instanceof Aero) {
                    numAero++;
                } else if (e instanceof BattleArmor) {
                    numBA++;
                } else if (e instanceof Protomech) {
                    numProtos++;
                }
            }
            int items = 0;
            if (numMechs > 0) {
                toReturn.append(numMechs + " Meks");
                items++;
            }
            if (numVees > 0) {
                if (items > 0) {
                    toReturn.append(", ");
                }
                toReturn.append(numVees + " Vees");
                items++;
            }
            if (numVTOLs > 0) {
                if (items > 0) {
                    toReturn.append(", ");
                }
                toReturn.append(numVTOLs + " VTOLs");
                items++;
            }
            if (numInf > 0) {
                if (items > 0) {
                    toReturn.append(", ");
                }
                toReturn.append(numInf + " Inf");
                items++;
            }
            if (numBA > 0) {
                if (items > 0) {
                    toReturn.append(", ");
                }
                toReturn.append(numBA + " BA");
                items++;
            }
            if (numProtos > 0) {
                if (items > 0) {
                    toReturn.append(", ");
                }
                toReturn.append(numProtos + " Protomechs");
                items++;
            }
            if (numAero > 0) {
                if (items > 0) {
                    toReturn.append(", ");
                }
                toReturn.append(numAero + " Aero");
                items++;
            }
            toReturn.append(" / BV: " + getBV() + ")");
            return toReturn.toString();
        } else {
            return "(Units: " + getAmountOfUnits() + " / BV: " + getBV() + ")";
        }
    }

    /**
     * Special getDescription() which also shows an ID number. Used by SPlayer's getStatus and the
     * ShowToHouseCommand.
     */
    public String getDescription(boolean accurate, boolean showID, boolean idShouldLink) {

        String toReturn = "";
        if (accurate) {
            if (showID && !idShouldLink) {
                toReturn += "#" + getId();
            } else if (showID && idShouldLink) {
                toReturn += "<a href=\"MEKWARS/c sth#a#" + getId() + "\">#" + getId() + "</a>";
            }

            if (isDisabled()) {
                toReturn += " (disabled)";
            }
            toReturn += " - ";

            toReturn += this.getDescription(accurate);
        } else {
            toReturn += this.getDescription(accurate);
        }

        return toReturn;
    }

    public String getDescription(boolean accurate) {
        return getDescription(accurate, null);
    }

    public String getDescription(boolean accurate, SArmy opposingArmy) {
        if (accurate) {
            StringBuilder result = new StringBuilder();

            // only show a name if one is set
            if (getName().trim().length() != 0) {
                result.append("\"" + getName() + "\" - ");
            }

            Iterator<SUnit> iterator = getUnits().iterator();
            while (iterator.hasNext()) {
                SUnit unit = iterator.next();
                if (isCommander(unit.getId())) {
                    result.append("<i>");
                }
                result.append(unit.getSmallDescription());
                if (isCommander(unit.getId())) {
                    result.append("</i>");
                }
                if (iterator.hasNext()) {
                    result.append(", ");
                }
            }
            result.append("; BV: " + getBV());

            if (opposingArmy != null && getBV() != getOperationsBV(opposingArmy)) {
                result.append(
                        " (BV vs "
                                + opposingArmy.getRawForceSize()
                                + " units : "
                                + getOperationsBV(opposingArmy)
                                + ")");
            }

            return result.toString();
        }
        return getInaccurateDescription();
    }

    public void fromString(String s, String delimiter, SPlayer p) {
        StringTokenizer ST = new StringTokenizer(s, delimiter);
        setId(TokenReader.readInt(ST));
        setName(TokenReader.readString(ST));
        setLowerLimiter(TokenReader.readInt(ST));
        setUpperLimiter(TokenReader.readInt(ST));
        int count = TokenReader.readInt(ST);
        for (int i = 0; i < count; i++) {
            int id = TokenReader.readInt(ST);

            if (id != 0) {
                // already been replaced --Torren
                addUnit(p.getUnit(id));
            }
        }
        count = TokenReader.readInt(ST);
        for (int i = 0; i < count; i++) {
            int key = TokenReader.readInt(ST);
            int unit = TokenReader.readInt(ST);
            getC3Network().put(key, unit);
        }
        setOpForceSize(TokenReader.readFloat(ST));

        count = TokenReader.readInt(ST);
        for (int i = 0; i < count; i++) {
            int unit = TokenReader.readInt(ST);
            addCommander(unit);
        }
        boolean lock = TokenReader.readBoolean(ST);
        if (lock) {
            playerLockArmy();
        } else {
            playerUnlockArmy();
        }
        boolean disabled = TokenReader.readBoolean(ST);
        if (disabled) {
            disableArmy();
        } else {
            enableArmy();
        }
    }

    public String getMinimalInfo() {
        return getDescription(true);
    }

    public String getInfo() {
        return getDescription(true);
    }

    /*
     * Opponent Methods. Used to get, set, add and remove opposing forces.
     */
    public void setOpponents(List<SArmy> v) {
        opponents = v;
    }

    public List<SArmy> getOpponents() {
        return opponents;
    }

    public void addOpponent(SArmy a) {
        // for now, just tack it on to the list.
        // TODO: Sort by faction.
        try {
            opponents.add(a);
        } catch (Exception e) {
            LOGGER.error("Error adding army to opponentList. Trace follows.");
            LOGGER.error("Exception: ", e);
        }
    } // end addOpponent

    public void removeOpponent(SArmy a) {
        try {
            opponents.remove(a);
        } catch (Exception e) {
            LOGGER.error("Error removing army from opponentList. Trace follows.");
            LOGGER.error("Exception: ", e);
        }
    }

    @Override
    public void setName(String name) {
        super.setName(name);

        if (name.trim().length() >= 0) {
            CampaignMain.cm.toUser("PL|RNA|" + getId() + "#" + name, getOwner().getName(), false);
        }
    }

    public void setPlayerLock(int aid, boolean lock) {
        if (lock) {
            super.playerLockArmy();
            CampaignMain.cm.toUser("PL|LA|" + getId(), getOwner().getName(), false);
        } else {
            super.playerUnlockArmy();
            CampaignMain.cm.toUser("PL|ULA|" + getId(), getOwner().getName(), false);
        }
    }

    @Override
    public void toggleArmyDisabled() {
        super.toggleArmyDisabled();
        CampaignMain.cm.toUser("PL|TAD|" + getId(), getOwner().getName(), false);
    }

    private boolean isLegalMekToInfantryRatio() {
        int infcount = 0;
        int mekcount = 0;
        for (Unit unit : getUnits()) {
            if (unit.getType() == Unit.INFANTRY) {
                infcount++;
            } else if (unit.getType() == Unit.MEK) {
                mekcount++;
            }
        }

        if (infcount == 0) {
            return true;
        }

        if (mekcount == 0) {
            return false;
        }

        int ratio = (infcount * 100) / mekcount;

        return ratio <= CampaignData.cd.getCampaignOptions().getIntegerConfig("MekToInfantryRatio");
    }

    private boolean isLegalMekToVehicleRatio() {
        int veecount = 0;
        int mekcount = 0;
        for (Unit unit : getUnits()) {
            if (unit.getType() == Unit.VEHICLE) {
                veecount++;
            } else if (unit.getType() == Unit.MEK) {
                mekcount++;
            }
        }

        if (veecount == 0) {
            return true;
        }

        if (mekcount == 0) {
            return false;
        }

        int ratio = (veecount * 100) / mekcount;

        return ratio <= CampaignData.cd.getCampaignOptions().getIntegerConfig("MekToVehicleRatio");
    }

    public void checkLegalRatio(String username) {
        if (CampaignData.cd.getCampaignOptions().getBooleanConfig("AllowRatios")) {
            if (!isLegalMekToInfantryRatio()) {
                CampaignMain.cm.toUser(
                        "This army has an Illegal Mek to Infantry ratio and will not be allowed to"
                                + " participate in games.",
                        username,
                        true);
            } else if (!isLegalMekToVehicleRatio()) {
                CampaignMain.cm.toUser(
                        "This army has an Illegal Mek to Vehicle ratio and will not be allowed to"
                                + " participate in games.",
                        username,
                        true);
            } else {
                CampaignMain.cm.toUser("Army Ratio Checks", username, true);
            }
        }
    }

    @Override
    public void setLowerLimiter(int lowerLimit) {
        int buffer = CampaignData.cd.getCampaignOptions().getIntegerConfig("LowerLimitBuffer");

        if (lowerLimit < buffer && lowerLimit != Army.NO_LIMIT) {
            lowerLimit = buffer;
            CampaignMain.cm.toUser(
                    "Army " + getId() + "'s lower limit set to " + buffer + ".",
                    getOwner().getName(),
                    true);
            CampaignMain.cm.toUser(
                    "PL|SAB|" + getId() + "#" + getLowerLimiter() + "#" + getUpperLimiter(),
                    getOwner().getName(),
                    false);
        }

        super.setLowerLimiter(lowerLimit);
    }

    @Override
    public void setUpperLimiter(int upperLimit) {
        int buffer = CampaignData.cd.getCampaignOptions().getIntegerConfig("UpperLimitBuffer");

        if (upperLimit < buffer && upperLimit != Army.NO_LIMIT) {
            upperLimit = buffer;
            CampaignMain.cm.toUser(
                    "Army " + getId() + "'s upper limit set to " + buffer + ".",
                    getOwner().getName(),
                    true);
            CampaignMain.cm.toUser(
                    "PL|SAB|" + getId() + "#" + getLowerLimiter() + "#" + getUpperLimiter(),
                    getOwner().getName(),
                    false);
        }
        super.setUpperLimiter(upperLimit);
    }

    public boolean isUnitInArmy(SUnit unit) {
        if (unit == null) {
            return false;
        }
        for (SUnit newUnit : getUnits()) {
            if (newUnit.equals(unit)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasPilotWithTooManySkills() {
        if (!CampaignData.cd.getCampaignOptions().getBooleanConfig("PlayersCanBuyPilotUpgrades")) {
            return false;
        }
        int maxPilotSkills = CampaignData.cd.getCampaignOptions().getIntegerConfig("MaxPilotUpgrades");

        if (maxPilotSkills == -1) {
            return false;
        }

        for (Unit unit : getUnits()) {
            if (unit.getPilot().getSkills().size() > maxPilotSkills) {
                return true;
            }
        }
        return false;
    }
}
