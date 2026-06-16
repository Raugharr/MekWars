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
 * Created on 26.03.2004
 *
 */
package mekwars.common;

import jakarta.persistence.MappedSuperclass;

import megamek.common.Aero;
import megamek.common.AmmoType;
import megamek.common.BattleArmor;
import megamek.common.Entity;
import megamek.common.EntityWeightClass;
import megamek.common.Mech;
import megamek.common.Mounted;
import megamek.common.Protomech;
import megamek.common.Tank;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;

import mekwars.common.campaign.pilot.Pilot;
import mekwars.common.campaign.targetsystems.TargetSystem;
import mekwars.common.campaign.targetsystems.TargetTypeNotImplementedException;
import mekwars.common.campaign.targetsystems.TargetTypeOutOfBoundsException;

import jakarta.persistence.MappedSuperclass;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * @author Helge Richter
 */
@MappedSuperclass
public abstract class Unit<T extends Unit<T>> {
    public static final int LIGHT = 0;
    public static final int MEDIUM = 1;
    public static final int HEAVY = 2;
    public static final int ASSAULT = 3;

    public static final int MEK = 0;
    public static final int VEHICLE = 1;
    public static final int INFANTRY = 2;
    public static final int PROTOMEK = 3;
    public static final int BATTLEARMOR = 4;
    public static final int AERO = 5;
    public static final int QUAD = 6;
    public static final int MEKWARRIOR = 7;
    public static final int MAXBUILD = 6;

    public static final int C3_NONE = 0;
    public static final int C3_SLAVE = 1;
    public static final int C3_MASTER = 2;
    public static final int C3_IMPROVED = 3;
    public static final int C3_MMASTER = 4;

    public static final int STATUS_OK = 1;
    public static final int STATUS_UNMAINTAINED = 2; // @urgru 7/18/04
    public static final int STATUS_FORSALE = 3; // @urgru 12.29.05

    public static final int TOTALTYPES = 6;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected int id;
    @Transient
    protected Entity unitEntity;
    private int BV;
    private int scrappableFor = -1;
    @Transient
    private Pilot pilot;
    @Transient
    private Player owner;

    private int type;
    private int weightClass;
    private int status = Unit.STATUS_OK;
    private String producer;
    private String unitFilename;
    private int posId;
    private String modelName;

    private int maintainanceLevel = 100; // @urgru 8/2/04
    @Column(name = "c3_level")
    private int c3Level = 0; // @Torren 12/13/04 0=None 1=Slave 2=Master 3=Independent

    public int simpleRepairCost = 0;
    private int currentRepairCost = 0;
    private int lifeTimeRepairCost = 0;

    @Transient
    protected TargetSystem targetSystem = new TargetSystem();

    private boolean isSupportUnit = false;
    private boolean ChristmasUnit = false;
    private boolean pilotIsRepairing = false;

    // CONSTRUCTOR
    public Unit() {
        // no content
    }

    // STATIC METHODS
    /*
     * Unit's static methods handle generalized information
     * about unit weight classes and types, including text to
     * int conversion, and vice versa.
     */
    /**
     * @param Weightclass
     * @return a String describing the weightclass (light, medium etc)
     */
    public static String getWeightClassDesc(int weightclass) {
        if (weightclass == LIGHT) return "Light";
        if (weightclass == MEDIUM) return "Medium";
        if (weightclass == HEAVY) return "Heavy";
        if (weightclass == ASSAULT) return "Assault";
        return "Unknown";
    }

    public static int getWeightIDForName(String name) {
        if (name.equalsIgnoreCase("LIGHT")) return LIGHT;
        if (name.equalsIgnoreCase("MEDIUM")) return MEDIUM;
        if (name.equalsIgnoreCase("HEAVY")) return HEAVY;
        if (name.equalsIgnoreCase("ASSAULT")) return ASSAULT;
        return 0;
    }

    public static int getEntityWeight(Entity ent) {
        int weight = ent.getWeightClass();
        if (weight == EntityWeightClass.WEIGHT_LIGHT) return Unit.LIGHT;
        if (weight == EntityWeightClass.WEIGHT_MEDIUM) return Unit.MEDIUM;
        if (weight == EntityWeightClass.WEIGHT_HEAVY) return Unit.HEAVY;
        if (weight == EntityWeightClass.WEIGHT_ASSAULT) return Unit.ASSAULT;

        return Unit.LIGHT;
    }

    public static int getEntityType(Entity ent) {
        if (ent instanceof Mech) return Unit.MEK;
        if (ent instanceof Tank) return Unit.VEHICLE;
        if (ent instanceof BattleArmor) return Unit.BATTLEARMOR;
        if (ent instanceof Protomech) return Unit.PROTOMEK;
        if (ent instanceof Aero) return Unit.AERO;

        return Unit.INFANTRY;
    }

    public static String getTypeClassDesc(int type) {
        if (type == Unit.MEK) return "Mek";
        if (type == Unit.VEHICLE) return "Vehicle";
        if (type == Unit.INFANTRY) return "Infantry";
        if (type == Unit.BATTLEARMOR) return "BattleArmor";
        if (type == Unit.PROTOMEK) return "ProtoMek";
        if (type == Unit.AERO) return "Aero";

        return "Unknown";
    }

    public static int getTypeIDForName(String name) {
        // If the string contains V, it's supposely a Vehicle
        if (name.toLowerCase().startsWith("v")) return VEHICLE;

        // I = Infantry. the I in Vehicle is not affected, because it's caught above
        if (name.toLowerCase().startsWith("i")) return INFANTRY;

        // P = ProtoMek
        if (name.toLowerCase().startsWith("p")) return PROTOMEK;

        // B = BattleArmor
        if (name.toLowerCase().startsWith("b")) return BATTLEARMOR;

        // A = Aero
        if (name.toLowerCase().startsWith("a")) return AERO;

        // Default = Mek
        return MEK;
    }

    // METHODS
    /*
     * Nearly all gets and sets.
     */

    /**
     * Method that returns a model name. Name is "checkModel" to avoid confusion with the
     * SUnit.getModelName() method, which should be used instead of this method wherever possible.
     *
     * @return Returns the modelname.
     */
    public String checkModelName() {
        return modelName;
    }

    /**
     * @param modelname The modelname to set.
     */
    public void setModelname(String modelName) {
        this.modelName = modelName;
    }

    public int getBV() {
        return Math.max(0, BV);
    }

    public void setBV(int bv) {
        BV = Math.max(0, bv);
    }

    public int getScrappableFor() {
        return scrappableFor;
    }

    public void setScrappableFor(int scrappableFor) {
        this.scrappableFor = scrappableFor;
    }

    /**
     * @return Returns the pilot.
     */
    public Pilot getPilot() {
        return pilot;
    }

    /**
     * @param pilot The pilot to set.
     */
    public void setPilot(Pilot pilot) {
        this.pilot = pilot;
        pilot.setUnit(this);
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }


    abstract public Army getArmy();

    abstract public void setArmy(Army army);
    
    /**
     * @return Returns the posId.
     */
    public int getPosId() {
        return posId;
    }

    /**
     * @param posId The posId to set.
     */
    public void setPosId(int pid) {
        posId = pid;
    }

    /**
     * @return Returns the producer.
     */
    public String getProducer() {
        return producer;
    }

    /**
     * @param producer The producer to set.
     */
    public void setProducer(String producer) {
        this.producer = producer;
    }

    /**
     * @return Returns the type.
     */
    public int getType() {
        return type;
    }

    /**
     * @param type The type to set.
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return Returns the unitFilename.
     */
    public String getUnitFilename() {
        return unitFilename;
    }

    /**
     * @param unitFilename The unitFilename to set.
     */
    public void setUnitFilename(String unitFilename) {
        this.unitFilename = unitFilename;
    }

    /**
     * @return Returns the weightclass.
     */
    public int getWeightClass() {
        return weightClass;
    }

    /**
     * @param weightclass The weightclass to set.
     */
    public void setWeightClass(int weightClass) {
        this.weightClass = weightClass;
    }

    /**
     * @return Returns the status.
     */
    public int getStatus() {
        return status;
    }

    /**
     * @param status The status to set.
     */
    public void setStatus(int i) {
        status = i;
    }

    /**
     * @return Returns the id.
     */
    public int getId() {
        return id;
    }

    /**
     * @param id The id to set.
     */
    public void setId(int i) {
        id = i;
    }

    public Entity getEntity() {
        return unitEntity;
    }

    public void setEntity(Entity unitEntity) {
        this.unitEntity = unitEntity;
    }

    /**
     * @return return the maintainance status
     */
    public int getMaintainanceLevel() {
        return maintainanceLevel;
    }

    /**
     * @param maintainanceLevel maintainance level to set.
     *     <p>since maintainance is expressed as a percentage, dont let this this exceed 100 or drop
     *     below 0.
     */
    public void setMaintainanceLevel(int maintainanceLevel) {
        this.maintainanceLevel = Math.max(0, Math.min(100, maintainanceLevel));
    }

    /**
     * @param int amount of maintanance to add.
     */
    public void addToMaintainanceLevel(int i) {
        setMaintainanceLevel(maintainanceLevel + i);
    }

    public int getBVForMatch() {
        if (CampaignData.cd.getCampaignOptions().getBooleanConfig("UseBaseBVForMatching")) {
            return getBaseBV();
        }
        return getBV();
    }

    public int linkToC3Network(Army<?> army, Unit master) {
        if (army == null || master == null) {
            return -1;
        }

        if (army.getUnit(this.getId()) == null) {
            return -1;
        }

        if (this.getC3Level() == C3_NONE) {
            return -1;
        }

        if (this.getC3Level() == C3_SLAVE) {
            if (master.getC3Level() != C3_MASTER
                    && master.getC3Level() != C3_MMASTER) { // master is a slave or doens't have C3
                return -1;
            }

            if (master.hasBeenC3LinkedTo(army) && !master.hasC3SlavesLinkedTo(army)) {
                return -1;
            }

            if (!master.checkC3mNetworkHasOpen(army, this.getC3Level())) {
                return -1;
            }

            army.getC3Network().put(this.getId(), master.getId());
            return master.getId();
        } else if (this.getC3Level() == C3_MASTER || this.getC3Level() == C3_MMASTER) {

            if (master.getC3Level() != C3_MASTER && master.getC3Level() != C3_MMASTER) {
                // master is really a slave or doesn't have C3
                return -1;
            }

            if (this.hasBeenC3LinkedTo(army) && !this.hasC3SlavesLinkedTo(army)) {
                // other units have linked to this unit so it cannot link to
                // other units
                return -1;
            }

            if (master.getC3Level() != Unit.C3_MMASTER
                    && master.hasBeenC3LinkedTo(army)
                    && master.hasC3SlavesLinkedTo(army)) {
                return -1;
            }

            if (!master.checkC3mNetworkHasOpen(army, this.getC3Level())) {
                return -1;
            }

            army.getC3Network().put(this.getId(), master.getId());
            return master.getId();
        } else if (this.getC3Level() == C3_IMPROVED) {
            if (master.getC3Level() != C3_IMPROVED) {
                return -1;
            }

            if (this.hasBeenC3LinkedTo(army)) {
                return -1;
            }

            if (!master.checkC3iNetworkHasOpen(army)) {
                return -1;
            }

            army.getC3Network().put(this.getId(), master.getId());
            return master.getId();
        }
        return -1;
    }

    // check to see if someone else is linked to this unit
    public boolean hasBeenC3LinkedTo(Army<?> army) {

        if (army.getUnit(this.getId()) == null) return false;

        for (int c3U : army.getC3Network().values()) {
            if (c3U == this.getId()) {
                return true;
            }
        }

        return false;
    }

    // checks that a master only has slaves connected to it
    public boolean hasC3SlavesLinkedTo(Army<?> army) {
        if (army.getUnit(this.getId()) == null) {
            return false;
        }

        for (Integer c3Slave : army.getC3Network().keySet()) {
            Integer c3Master = army.getC3Network().get(c3Slave);
            if (c3Master.intValue() == this.getId()) {
                if (army.getUnit(c3Slave.intValue()).getC3Level() != Unit.C3_SLAVE) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean checkC3mNetworkHasOpen(Army<?> army, int c3Type) {
        int MAX_UNITS = 4;
        int unitCount = 1; // this unit is already in the network :)

        if (army == null) {
            return false;
        }

        if (army.getUnit(this.getId()) == null) {
            return false;
        }

        if (army.getC3Network().get(this.getId())
                != null) { // meaning hes already linked to someone
            return false;
        }

        if (this.getC3Level() == C3_MMASTER) {
            int slaveCount = 0, masterCount = 0;
            int maxMasters = 2;
            int maxSlaves = 3;

            for (Integer c3Slave : army.getC3Network().keySet()) {
                Integer c3Master = army.getC3Network().get(c3Slave);
                if (c3Master.intValue() == this.getId()) {
                    Unit tempUnit = army.getUnit(c3Slave.intValue());
                    if (tempUnit.getC3Level() == C3_SLAVE) {
                        slaveCount++;
                    } else {
                        masterCount++; // we are going to assume that no C3I's are in a C3M/S
                        // network
                    }
                }
            }

            if (c3Type != Unit.C3_SLAVE && masterCount >= maxMasters) {
                return false;
            }

            if (c3Type == Unit.C3_SLAVE && slaveCount >= maxSlaves) {
                return false;
            }

            return true;
        }

        for (Integer c3Unit : army.getC3Network().values()) {
            if (c3Unit.intValue() == this.getId()) unitCount++;
        }

        return unitCount < MAX_UNITS;
    }

    public boolean checkC3iNetworkHasOpen(Army<?> army) {
        int MAX_UNITS = 6;
        int unitCount = 1; // this unit is already in the network :)

        if (army == null) {
            return false;
        }

        if (army.getUnit(this.getId()) == null) {
            return false;
        }

        if (army.getC3Network().get(this.getId()) != null) {
            return false;
        }

        for (Integer c3U : army.getC3Network().values()) {
            if (c3U.intValue() == this.getId()) {
                unitCount++;
            }
        }

        return unitCount < MAX_UNITS;
    }

    public void getC3Type(Entity unit) {
        unit.setShutDown(false);
        if (unit.hasC3S()) {
            this.setC3Level(C3_SLAVE); // Slave
        } else if (unit.hasC3MM()) {
            this.setC3Level(C3_MMASTER); // Dual Master
        } else if (unit.hasC3M()) {
            this.setC3Level(C3_MASTER); // Master
        } else if (unit.hasC3i()) {
            this.setC3Level(C3_IMPROVED); // Improved
        } else {
            this.setC3Level(C3_NONE);
        }
    }

    /**
     * @Gets the units current C3 Level 0=None 1=Slave 2=Master 3=Independent 4=Dual Masters
     */
    public int getC3Level() {
        return c3Level;
    }

    /**
     * @param level Sets the units current C3 Level
     */
    public void setC3Level(int level) {
        c3Level = level;
    }

    public AmmoType getEntityAmmo(int weaponType, String ammoName) {
        for (AmmoType ammoType : AmmoType.getMunitionsFor(weaponType)) {
            if (ammoType.getInternalName().equalsIgnoreCase(ammoName)) {
                return ammoType;
            }
        }

        // couldn't find the ammo retun null and just use the entities standard ammo.
        return null;
    }

    public boolean hasVacantPilot() {
        if (this.getPilot() == null || this.getPilot().getName().equalsIgnoreCase("Vacant"))
            return true;

        return false;
    }

    public void setRepairCosts(int current, int life) {
        currentRepairCost = current;
        lifeTimeRepairCost = life;
    }

    public void addRepairCost(int cost) {
        if (cost < 0) {
            currentRepairCost = 0;
        } else {
            currentRepairCost += cost;
            lifeTimeRepairCost += cost;
        }
    }

    public int getCurrentRepairCost() {
        return currentRepairCost;
    }

    public int getLifeTimeRepairCost() {
        return lifeTimeRepairCost;
    }

    public boolean getPilotIsRepairing() {
        return pilotIsRepairing;
    }

    public void setPilotIsRepairing(boolean repair) {
        pilotIsRepairing = repair;
    }

    public int getBaseBV() {
        return getEntity().calculateBattleValue(false, true);
    }

    public boolean isSinglePilotUnit() {
        return this.getType() == MEK
                || this.getType() == PROTOMEK
                || this.getType() == QUAD
                || this.getType() == AERO;
    }

    /**
     * @return the isSupportUnit
     */
    public boolean isSupportUnit() {
        return isSupportUnit;
    }

    /**
     * @param isSupportUnit the isSupportUnit to set
     */
    public void setSupportUnit(boolean isSupportUnit) {
        this.isSupportUnit = isSupportUnit;
    }

    /**
     * @return the christmasUnit
     */
    public boolean isChristmasUnit() {
        return ChristmasUnit;
    }

    /**
     * @param christmasUnit the christmasUnit to set
     */
    public void setChristmasUnit(boolean christmasUnit) {
        ChristmasUnit = christmasUnit;
    }

    /**
     * @author Salient
     * @return the christmasUnit piggy back off of xmas
     */
    public boolean isLocked() {
        return ChristmasUnit;
    }

    /**
     * @author Salient
     * @param christmasUnit the christmasUnit to set piggy back off of xmas
     */
    public void setLocked(boolean lockUnit) {
        ChristmasUnit = lockUnit;
    }

    public boolean hasTAG() {
        return getEntity().hasTAG();
    }

    public boolean hasHoming() {
        for (Mounted ammo : getEntity().getAmmo()) {
            if (((AmmoType) ammo.getType())
                    .getMunitionType()
                    .contains(AmmoType.Munitions.M_HOMING)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSemiGuided() {
        for (Mounted ammo : getEntity().getAmmo()) {
            if (((AmmoType) ammo.getType())
                    .getMunitionType()
                    .contains(AmmoType.Munitions.M_SEMIGUIDED)) {
                return true;
            }
        }
        return false;
    }

    public void setTargetSystem(int type) {
        if ((type != TargetSystem.TS_TYPE_STANDARD) && CampaignData.cd.targetSystemIsBanned(type)) {
            setTargetSystem(TargetSystem.TS_TYPE_STANDARD);
        }
        try {
            targetSystem.setTargetSystem(type);
        } catch (TargetTypeOutOfBoundsException e) {
            e.printStackTrace();
        } catch (TargetTypeNotImplementedException e) {
            e.printStackTrace();
        }
    }

    public String getTargetSystemTypeDesc() {
        return targetSystem.getCurrentTypeName();
    }

    public TargetSystem getTargetSystem() {
        return targetSystem;
    }

    /**
     * @param omniVehicleListPath The path to the omnivehiclelist.txt file.
     */
    protected boolean isOmni(String omniVehicleListPath) {
        boolean isOmni = getEntity().isOmni();
        String targetChassis = getEntity().getChassis();

        if ((getType() == Unit.VEHICLE) && !isOmni) {
            try {
                FileInputStream fis = new FileInputStream("./data/buildtables/omnivehiclelist.txt");
                BufferedReader dis = new BufferedReader(new InputStreamReader(fis));
                while (dis.ready()) {
                    String chassie = dis.readLine();
                    // check to see if the chassies listed in the file match
                    // omni vehicle chassies.
                    if (targetChassis.equalsIgnoreCase(chassie)) {
                        dis.close();
                        fis.close();
                        return true;
                    }
                }
                dis.close();
                fis.close();
            } catch (Exception ex) {
                // Simply means no omniveh list present. Ignore.
            }
        }
        return isOmni;
    }

    /**
     * A method which returns the MU cost of a specified campaign unit.
     *
     * @return int - # of MU it takes to buy a unit of the given weight class
     */
    public static int getPriceForUnit(int weightClass, int typeId, House producer) {
        int result = Integer.MAX_VALUE;
        String classtype =
                Unit.getWeightClassDesc(weightClass) + Unit.getTypeClassDesc(typeId) + "Price";

        if (typeId == Unit.MEK) {
            result =
                    CampaignData.cd
                            .getCampaignOptions()
                            .getIntegerConfig(Unit.getWeightClassDesc(weightClass) + "Price");
        } else {
            result = CampaignData.cd.getCampaignOptions().getIntegerConfig(classtype);
        }

        // modify the result by the faction price modifier
        result += producer.getHouseUnitPriceMod(typeId, weightClass);
        return Math.max(0, result);
    }

    /**
     * A method which returns the PP COST of a unit. Meks and Vehicles are segregated by
     * weightClass. Infantry are flat priced accross
     *
     * <p>all weight classes. @ param weight - the weight class to be checked @ return int - the PP
     * cost
     */
    public static int getPPForUnit(int weightClass, int typeId, House producer) {
        int result = Integer.MAX_VALUE;
        String classtype =
                Unit.getWeightClassDesc(weightClass) + Unit.getTypeClassDesc(typeId) + "PP";

        if (typeId == Unit.MEK) {
            result =
                    CampaignData.cd
                            .getCampaignOptions()
                            .getIntegerConfig(Unit.getWeightClassDesc(weightClass) + "PP");
        } else {
            result = CampaignData.cd.getCampaignOptions().getIntegerConfig(classtype);
        }

        // adjust PP cost by faction specific mod
        result += producer.getHouseUnitComponentMod(typeId, weightClass);

        return Math.max(0, result);
    }
}
