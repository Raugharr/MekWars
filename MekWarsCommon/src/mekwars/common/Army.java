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

/*
 * Created on 21.05.2004
 *
 */
package mekwars.common;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;

import megamek.common.Entity;

import mekwars.common.campaign.CampaignOptions;
import mekwars.common.composition.IHasUnits;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * A virtual army which can contain any combination of units
 *
 * @author Helge Richter
 */
@MappedSuperclass
public abstract class Army implements IHasUnits {
    private static final Logger LOGGER = LogManager.getLogger(Army.class);

    public static final int NO_LIMIT = -1;

    private String name = " ";

    private int upperLimiter = NO_LIMIT;
    private int lowerLimiter = NO_LIMIT;

    private int bv = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private boolean locked = false;

    private boolean armyPlayerLocked = false; // Used by players to keep armies
    // from being cleared
    private boolean armyDisabled = false;

    private float opForceSize = NO_LIMIT;

    @Transient
    private Map<Integer, Integer> c3Network = new HashMap<Integer, Integer>();

    @Transient
    private List<Integer> commanders = new ArrayList<Integer>();
    private float rawForceSize = -1;
    @Transient
    private Set<String> legalOperations = new TreeSet<>();

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private Player owner;

    public Army() {}

    public Army(Player owner) {
        this.owner = owner;
    }

    public boolean isDisabled() {
        return armyDisabled;
    }

    public void disableArmy() {
        armyDisabled = true;
    }

    public void enableArmy() {
        armyDisabled = false;
    }

    public void toggleArmyDisabled() {
        armyDisabled = !armyDisabled;
    }

    public boolean isPlayerLocked() {
        return armyPlayerLocked;
    }

    public void playerLockArmy() {
        armyPlayerLocked = true;
    }

    public void playerUnlockArmy() {
        armyPlayerLocked = false;
    }

    /**
     * @return Returns the locked.
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * @param locked The locked to set.
     */
    public void setLocked(boolean b) {
        locked = b;
    }

    /**
     * @return return the BV.
     */
    public int getBV() {

        if (bv < 0) {
            return 0;
        }

        return bv;
    }

    /**
     * @param bv The bV to set.
     */
    public void setBV(int i) {
        bv = i;
    }

    /**
     * @return Returns the lowerLimit.
     */
    public int getLowerLimiter() {
        return lowerLimiter;
    }

    /**
     * @param lowerLimit The lowerLimit to set.
     */
    public void setLowerLimiter(int lowerLimit) {
        lowerLimiter = lowerLimit;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String s) {
        name = s.trim();
    }

    public void addUnit(int position, Unit unit) {
        unit.setArmy(this);
    }

    public void addUnit(Unit unit) {
        unit.setArmy(this);
    }

    public int getUnitPosition(int id) {
        int index = 0;

        for (Unit unit : getUnits()) {
            if (unit.getId() == id) {
                return index;
            }
            ++index;
        }
        return -1;
    }

    /**
     * This will pull The number of unit types this army holds i.e. type = Unit.MEK all meks will be
     * counted.
     *
     * @param type The unit type to check against. MEK VEHICLE
     * @return number of unit type that exist in this army
     */
    public int getNumberOfUnitTypes(int type) {
        int count = 0;

        for (Unit unit : getUnits()) {
            if (unit.getType() == type) {
                count++;
            }
        }

        return count;
    }

    /**
     * This will pull The number of unit types this army holds i.e. type = Unit.MEK all meks will be
     * counted.
     *
     * @param type The unit type to check against.
     * @param countSupport Whether or not to count Support Units.
     * @return
     */
    public int getNumberOfUnitTypes(int type, boolean countSupport) {
        int count = 0;

        for (Unit unit : getUnits()) {
            if (unit.getType() == type) {
                if (!unit.isSupportUnit() || (unit.isSupportUnit() && countSupport)) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * This method will return the total number of support units in the army
     *
     * @return Total number of support units in the army
     */
    public int getTotalSupportUnits() {
        return (int) getUnits().stream()
                .filter(Unit::isSupportUnit)
                .count();
    }

    /**
     * @return Returns the upperLimit.
     */
    public int getUpperLimiter() {
        return upperLimiter;
    }

    /**
     * @param upperLimit The upperLimit to set.
     */
    public void setUpperLimiter(int upperLimit) {
        upperLimiter = upperLimit;
    }

    /**
     * @return Returns the iD.
     */
    public int getId() {
        return id;
    }

    public Player getOwner() {
        return owner;
    }

    public Unit getUnit(int unitId) {

        for (Unit currU : getUnits()) {
            if (currU.getId() == unitId) {
                return currU;
            }
        }

        return null;
    }

    /**
     * @param id The iD to set.
     */
    public void setId(int id) {
        this.id = id;
    }

    public String toString(boolean toClient, String delimiter) {
        StringBuilder result = new StringBuilder();
        result.append(getId());
        result.append(delimiter);
        if (toClient) {
            result.append(getBV());
            result.append(delimiter);
            result.append(isLocked());
            result.append(delimiter);
        }
        if (getName().length() > 0) {
            result.append(getName());
        } else {
            result.append(" ");
        }
        result.append(delimiter);
        result.append(getLowerLimiter());
        result.append(delimiter);
        result.append(getUpperLimiter());
        result.append(delimiter);
        result.append(getUnits().size());
        result.append(delimiter);
        for (Unit unit : getUnits()) {
            result.append(unit.getId());
            result.append(delimiter);
        }
        result.append(delimiter);
        result.append(getC3Network().size());
        result.append(delimiter);
        for (Integer currI : getC3Network().keySet()) {
            result.append(currI);
            result.append(delimiter);
            result.append(getC3Network().get(currI));
            result.append(delimiter);
        }
        result.append(opForceSize);
        result.append(delimiter);

        result.append(commanders.size());
        result.append(delimiter);
        for (Integer unitId : commanders) {
            result.append(unitId);
            result.append(delimiter);
        }
        result.append(Boolean.toString(armyPlayerLocked));
        result.append(delimiter);
        result.append(Boolean.toString(armyDisabled));
        result.append(delimiter);
        return result.toString();
    }

    /**
     * @return Returns the C3Networks.
     */
    public Map<Integer, Integer> getC3Network() {
        return c3Network;
    }

    /**
     * @param c3Network The C3Networks to set.
     */
    public void setC3Network(Map<Integer, Integer> network) {
        c3Network = network;
    }

    public void removeUnitFromC3Network(int unitId) {
        if (getC3Network().get(unitId) != null) {
            getC3Network().remove(unitId);
            return;
        }

        Iterator<Integer> i = getC3Network().keySet().iterator();
        while (i.hasNext()) {
            Integer slave = i.next();
            Integer master = getC3Network().get(slave);
            if (master.intValue() == unitId) {
                i.remove();
            }
        }
    }

    /**
     * Finds out if unitOne and unitTwo are in the ame c3 Network
     *
     * @param unitOne
     * @param unitTwo
     * @return
     */
    public boolean isSameC3Network(int unitOne, int unitTwo) {
        if (getC3Network().containsKey(unitOne) && getC3Network().get(unitOne) == unitTwo) {
            return true;
        }

        if (getC3Network().containsKey(unitTwo) && getC3Network().get(unitTwo) == unitOne) {
            return true;
        }

        Integer networkOne = getC3Network().get(unitOne);
        Integer networkTwo = getC3Network().get(unitTwo);

        if (networkOne != null && networkOne.equals(networkTwo)) {
            return true;
        }

        return false;
    }

    public float getOpForceSize() {
        return opForceSize;
    }

    public void setOpForceSize(float force) {
        opForceSize = force;
    }

    public List<Integer> getCommanders() {
        return commanders;
    }

    public boolean isCommander(int id) {
        return commanders.contains(id);
    }

    public void removeCommander(Integer id) {
        commanders.remove(id);
    }

    public void addCommander(int id) {
        if (isCommander(id)) {
            return;
        }
        commanders.add(id);
    }

    /**
     * @return The raw force size (Force Mod Rule)
     */
    public float getRawForceSize() {
        // dont recalculate if it isnt necessary
        if (rawForceSize != -1) {
            return rawForceSize;
        }
        CampaignOptions campaignOptions = CampaignData.cd.getCampaignOptions();

        // no break, generate a raw force size
        for (Unit u : this.getUnits()) {
            if (u.getType() == Unit.INFANTRY) {
                rawForceSize += campaignOptions.getFloatConfig("InfantryOperationsBVMod");
            } else if (u.getType() == Unit.VEHICLE) {
                rawForceSize += campaignOptions.getFloatConfig("VehicleOperationsBVMod");
            } else if (u.getType() == Unit.BATTLEARMOR) {
                rawForceSize += campaignOptions.getFloatConfig("BAOperationsBVMod");
            } else if (u.getType() == Unit.AERO) {
                rawForceSize += campaignOptions.getFloatConfig("AeroOperationsBVMod");
            } else if (u.getType() == Unit.PROTOMEK) {
                rawForceSize += campaignOptions.getFloatConfig("ProtoOperationsBVMod");
            } else {
                // all other allowed types have a 1.0 weight
                rawForceSize += campaignOptions.getFloatConfig("MekOperationsBVMod");
            }
        }
        return rawForceSize;
    }

    /**
     * @param rfs - the forcesize to set (Operations Rule)
     */
    public void setRawForceSize(float rfs) {
        rawForceSize = rfs;
    }

    /**
     * Method that returns an army's legal operations. Used throughout the client to build GUI
     * elements.
     */
    public Set<String> getLegalOperations() {
        return legalOperations;
    }

    public boolean addLegalOperation(String operation) {
        return legalOperations.add(operation);
    }

    public boolean removeLegalOperation(String operation) {
        return legalOperations.remove(operation);
    }

    /**
     * This method is used to port saved legal ops info to a newly added CArmy, if an army with the
     * same ID previously existed. This allows the server to send updates (lost 1 type, etc) instead
     * of resending all of an army's ops whenever data is resent to the client.
     *
     * <p>Should only be used on the client side, see CPlayer.setArmyData() for usage details.
     */
    public void setLegalOperations(Set<String> ts) {
        legalOperations = ts;
    }

    public double forceSizeModifier(double opposingForceSize) {
        double myForceSize = 0;

        this.setRawForceSize(-1);
        myForceSize = this.getRawForceSize();

        if (myForceSize > opposingForceSize) {
            return ((opposingForceSize / myForceSize) + (myForceSize / opposingForceSize)) - 1;
        }
        return 1.0;
    }

    /**
     * @author Torren 2/23/2007 New Tech Manual rules on force Size. This returns the new <code>BV
     *     </code> of the <code>this</code> army which is considerd the larger force
     */
    public int getOperationsBV(Army opposingForce) {
        // if not using the operations rules, return a normal BV.
        boolean usingOpRules =
                CampaignData.cd.getCampaignOptions().getBooleanConfig("UseOperationsRule");
        if (!usingOpRules) {
            return getBV();
        }

        if (opposingForce == null) {
            return getBV();
        }
        return (int) Math.round(getBV() * forceSizeModifier(opposingForce.getOpForceSize()));
    }

    public boolean hasTAGAndHomingCombo() {
        boolean hasTAG = false;
        boolean hasHoming = false;

        for (Unit unit : getUnits()) {
            hasTAG |= unit.hasTAG();
            hasHoming |= unit.hasHoming();

            if (hasTAG && hasHoming) {
                return true;
            }
        }
        return false;
    }

    public boolean hasTAGAndSemiGuidedCombo() {
        boolean hasTAG = false;
        boolean hasSemiGuided = false;

        for (Unit unit : getUnits()) {
            hasTAG |= unit.hasTAG();
            hasSemiGuided |= unit.hasSemiGuided();

            if (hasTAG && hasSemiGuided) {
                return true;
            }
        }
        return false;
    }

    /**
     * Used by Operations to determine how many mines to assign to attacker/defender, in lieu of BV.
     */
    public double getTotalTonnage() {
        return getUnits().stream().map(Unit::getEntity).mapToDouble(Entity::getWeight).sum();
    }

    public double getAverageWalk() {
        return getUnits().stream()
                .map(Unit::getEntity)
                .mapToInt(Entity::getWalkMP)
                .average()
                .orElse(0.0);
    }

    public double getAverageJump() {
        return getUnits().stream()
                .map(Unit::getEntity)
                .mapToInt(Entity::getJumpMP)
                .average()
                .orElse(0.0);
    }

    public int getAmountOfUnitsWithoutInfantry() {
        return (int) getUnits().stream().filter(unit -> unit.getType() != Unit.INFANTRY).count();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Army)) {
            return false;
        }

        Army army = (Army) object;
        Player owner = getOwner();
        Player otherOwner = army.getOwner();

        return (!Objects.equals(getId(), army.getId()))
                && Objects.equals(
                        owner != null ? owner.getName() : null,
                        otherOwner != null ? otherOwner.getName() : null);
    }

    @Override
    public int hashCode() {
        Player owner = getOwner();
        return Objects.hash(getId(), owner != null ? owner.getName() : null);
    }
}
