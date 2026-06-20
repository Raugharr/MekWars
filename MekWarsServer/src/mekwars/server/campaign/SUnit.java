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

import jakarta.persistence.AssociationOverride;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import megamek.common.AmmoType;
import megamek.common.AmmoType.Munitions;
import megamek.common.BattleArmor;
import megamek.common.Crew;
import megamek.common.CrewType;
import megamek.common.CriticalSlot;
import megamek.common.Entity;
import megamek.common.Infantry;
import megamek.common.MULParser;
import megamek.common.Mech;
import megamek.common.MechFileParser;
import megamek.common.MechSummary;
import megamek.common.MechSummaryCache;
import megamek.common.Mounted;
import megamek.common.Tank;
import megamek.common.WeaponType;
import megamek.common.enums.Gender;
import megamek.common.loaders.EntityLoadingException;
import megamek.common.options.PilotOptions;

import mekwars.common.Army;
import mekwars.common.CampaignData;
import mekwars.common.House;
import mekwars.common.MegaMekPilotOption;
import mekwars.common.Unit;
import mekwars.common.campaign.operations.Operation;
import mekwars.common.campaign.pilot.Pilot;
import mekwars.common.campaign.pilot.skills.PilotSkill;
import mekwars.common.campaign.pilot.skills.PilotSkillStore;
import mekwars.common.campaign.pilot.skills.TraitSkill;
import mekwars.common.campaign.pilot.skills.WeaponSpecialistSkill;
import mekwars.common.campaign.targetsystems.TargetSystem;
import mekwars.common.util.TokenReader;
import mekwars.common.util.UnitUtils;
import mekwars.server.MWServ;
import mekwars.server.campaign.pilot.SPilot;
import mekwars.server.campaign.util.SerializedMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * A class representing an MM.Net Entity
 *
 * @author Helge Richter (McWizard) Jun 10/04 - Dave Poole added an overloaded constructor to allow
 *     creation of a new SUnit with the same UnitID as an existing Mech to facilitate repodding
 */
@jakarta.persistence.Entity
@Table(name = "unit")
public final class SUnit extends Unit<SUnit> implements Comparable<SUnit> {
    private static final Logger LOGGER = LogManager.getLogger(SUnit.class);

    @ManyToOne
    @JoinColumn(name = "army_id")
    private SArmy army;
    private long passesMaintainanceUntil = 0;
    private int lastCombatPilot = -1;

    /** For Serialization. */
    public SUnit() {
        super();
    }

    /**
     * Construct a new unit.
     *
     * @param p flavour string (es: Built by Kurita on An-Ting)
     * @param filename to read this entity from
     */
    public SUnit(String p, String Filename, int weightclass) {
        super();
        int gunnery = 4;
        int piloting = 5;

        SHouse house = CampaignMain.cm.getHouseFromPartialString(p, null);

        setUnitFilename(Filename);
        init();

        if (house != null) {
            setPilot(house.getNewPilot(getType()));
        } else {
            setPilot(
                    new SPilot(
                            house,
                            SPilot.getRandomPilotName(CampaignMain.cm.getR()),
                            gunnery,
                            piloting));
        }

        setWeightClass(weightclass); // default weight class.

        setProducer(p);
        setId(CampaignMain.cm.getAndUpdateCurrentUnitID());
    }

    /**
     * Constructs a new Unit with the id for an existing unit (repod)
     *
     * @param p - flavour string (es: Built by Kurita on An-Ting)
     * @param Filename - filename to read this entity from
     * @param weightclass - int defining weightclass
     * @param replaceId - unitID to assign a new SUnit
     */
    public SUnit(int replaceId, String p, String Filename) {
        super();
        setUnitFilename(Filename);
        Entity ent = SUnit.loadMech(getUnitFilename());
        setEntity(ent);
        init();
        setPilot(new SPilot(null, "Vacant", 99, 99)); // only used for repods. A real
        // pilot is
        // transferred in later.
        setId(replaceId);
        setProducer(p);
        unitEntity = ent;
    }

    public SArmy getArmy() {
        return army;
    }

    public void setArmy(Army army) {
        this.army = (SArmy) army;
    }

    /**
     * Method which checks a unit for illegal ammo and replaces it with default ammo loads. useful
     * for removing faction banned ammo from salvage. Note that this is primarily designed to strip
     * L2 ammo from L2 units (eg - precision AC) and replace it with normal ammo. L3 ammos may lead
     * to some oddities and should be banned or allowed server wide rather than on a house-by-house
     * basis.
     *
     * @param u - unit to check
     * @param h - SHouse unit is joining
     */
    public static void checkAmmoForUnit(SUnit u, SHouse h) {
        Entity en = u.getEntity();
        int year = CampaignMain.cm.getIntegerConfig("CampaignYear");

        boolean wasChanged = false;

        for (Mounted mAmmo : en.getAmmo()) {
            AmmoType ammoType = (AmmoType) mAmmo.getType();

            if (ammoType.getAmmoType() == AmmoType.T_ATM) {
                continue;
            }

            if (ammoType.getAmmoType() == AmmoType.T_AC_LBX) {
                continue;
            }

            if (ammoType.getAmmoType() == AmmoType.T_SRM_STREAK) {
                continue;
            }

            if (ammoType.getAmmoType() == AmmoType.T_LRM_STREAK) {
                continue;
            }

            // TODO: This change is correct?
            if (ammoType.getMunitionType().contains(AmmoType.Munitions.M_STANDARD)) {
                continue;
            }

            for (Munitions munition : ammoType.getMunitionType()) {
                if (CampaignMain.cm.getData().getBannedAmmoStore().isBanned(munition, h)) {
                    Vector<AmmoType> types = AmmoType.getMunitionsFor(ammoType.getAmmoType());
                    Enumeration<AmmoType> allTypes = types.elements();

                    boolean defaultFound = false;
                    while (allTypes.hasMoreElements() && !defaultFound) {
                        AmmoType currType = allTypes.nextElement();

                        if ((currType.getTechLevel(year) <= en.getTechLevel())
                                && (currType.getMunitionType()
                                        .contains(AmmoType.Munitions.M_STANDARD))
                                && (currType.getRackSize() == ammoType.getRackSize())) {
                            mAmmo.changeAmmoType(currType);
                            if (mAmmo.byShot()) {
                                mAmmo.setShotsLeft(mAmmo.getOriginalShots());
                            } else {
                                mAmmo.setShotsLeft(ammoType.getShots());
                            }
                            defaultFound = true;
                            wasChanged = true;
                        }
                    }
                }
            }
        }
        if (wasChanged) {
            u.setEntity(en);
        }
    }

    /** Return the number of techs/bays required for a unit of given size/type. */
    public static int getHangarSpaceRequired(
            int typeid, int weightclass, int baymod, String model, SHouse faction) {

        if (typeid == Unit.PROTOMEK) {
            return 0;
        }

        if ((typeid == Unit.INFANTRY) && CampaignMain.cm.getBooleanConfig("FootInfTakeNoBays")) {

            // check types
            boolean isFoot = model.startsWith("Foot");
            boolean isAMFoot = model.startsWith("Anti-Mech Foot");

            if (isFoot || isAMFoot) {
                return 0;
            }
        }

        int result = 1;
        String techAmount =
                "TechsFor" + Unit.getWeightClassDesc(weightclass) + Unit.getTypeClassDesc(typeid);
        if (faction != null) {
            result = faction.getIntegerConfig(techAmount);
        } else {
            result = CampaignMain.cm.getIntegerConfig(techAmount);
        }

        if (!CampaignMain.cm.isUsingAdvanceRepair()) {
            // skill)
            result += baymod;
        }

        // no negative techs
        if (result < 0) {
            result = 0;
        }

        return result;
    }

    public static int getHangarSpaceRequired(
            int typeid,
            int weightclass,
            int baymod,
            String model,
            boolean unitSupported,
            SHouse faction) {
        if (unitSupported) {
            return SUnit.getHangarSpaceRequired(typeid, weightclass, baymod, model, faction);
        }
        return (int)
                (SUnit.getHangarSpaceRequired(typeid, weightclass, baymod, model, faction)
                        * CampaignMain.cm.getFloatConfig("NonFactionUnitsIncreasedTechs"));
    }

    /**
     * Pass-through method that gets the number of bays/techs required for a given unit by drawing
     * its characteristics and feeding them to getHangarSpaceRequired(int,int,int,String).
     */
    public static int getHangarSpaceRequired(SUnit u, SHouse faction) {
        return SUnit.getHangarSpaceRequired(
                u.getType(),
                u.getWeightClass(),
                u.getPilot().getBayModifier(),
                u.getModelName(),
                faction);
    }

    public static int getHangarSpaceRequired(SUnit u, boolean unitSupported, SHouse faction) {
        if (unitSupported) {
            return SUnit.getHangarSpaceRequired(
                    u.getType(),
                    u.getWeightClass(),
                    u.getPilot().getBayModifier(),
                    u.getModelName(),
                    faction);
        }
        return SUnit.getHangarSpaceRequired(
                u.getType(),
                u.getWeightClass(),
                u.getPilot().getBayModifier(),
                u.getModelName(),
                unitSupported,
                faction);
    }

    /**
     * Simple static method that access configs and returns a unit's influence on map size. Called
     * by ShortOperation when changing status from Waiting -> In_Progress.
     *
     * @return - configured map weighting
     */
    public static int getMapSizeModification(SUnit u) {
        if (u.getType() == Unit.VEHICLE) {
            return CampaignMain.cm.getIntegerConfig("VehicleMapSizeFactor");
        }
        if (u.getType() == Unit.INFANTRY) {
            return CampaignMain.cm.getIntegerConfig("InfantryMapSizeFactor");
        }
        if (u.getType() == Unit.MEK) {
            return CampaignMain.cm.getIntegerConfig("MekMapSizeFactor");
        }
        if (u.getType() == Unit.BATTLEARMOR) {
            return CampaignMain.cm.getIntegerConfig("BattleArmorMapSizeFactor");
        }
        if (u.getType() == Unit.AERO) {
            return CampaignMain.cm.getIntegerConfig("AeroMapSizeFactor");
        }
        if (u.getType() == Unit.PROTOMEK) {
            return CampaignMain.cm.getIntegerConfig("ProtoMekMapSizeFactor");
        }
        return 0; // no known type? return 0.
    }

    /*
     * AR-related statics.
     */
    // METHODS
    /**
     * @return the Serialized Version of this entity
     */
    public String toString(boolean toPlayer) {
        SerializedMessage msg = new SerializedMessage("$");
        // Recalculate the unit's bv. There is a reason we are sending new data
        // to the player
        if (toPlayer) {
            setBV(0);
            getBV();
        }

        msg.append("CM");
        msg.append(getUnitFilename());
        msg.append(getPosId());
        msg.append(getStatus());
        msg.append(getProducer());
        msg.append(((SPilot) getPilot()).toFileFormat("#", toPlayer));
        if (toPlayer) {
            msg.append(getPilot().getMegamekOptions().size());
            for (MegaMekPilotOption mmo : getPilot().getMegamekOptions()) {
                msg.append(mmo.getMmname());
                msg.append(mmo.isValue());
            }
            msg.append(getType());
            msg.append(getBV());
        }
        msg.append(getWeightClass());
        msg.append(getId());

        // error units don't need the rest of this data sent.
        if (getModelName().equals("OMG-UR-FD")) {
            return msg.getMessage();
        }

        if (getEntity() instanceof Mech) {
            unitEntity = getEntity();
            msg.append(((Mech) unitEntity).isAutoEject());
        }
        List<Mounted> en_Ammo = unitEntity.getAmmo();
        msg.append(en_Ammo.size());
        for (Mounted mAmmo : en_Ammo) {

            boolean hotloaded = mAmmo.isHotLoaded();
            if (!CampaignMain.cm
                    .getMegaMekClient()
                    .getGame()
                    .getOptions()
                    .booleanOption("tacops_hotload")) {
                hotloaded = false;
            }

            AmmoType ammoType = (AmmoType) mAmmo.getType();
            msg.append(ammoType.getAmmoType());
            msg.append(ammoType.getInternalName());
            msg.append(mAmmo.getUsableShotsLeft());
            msg.append(hotloaded);
        }

        if ((unitEntity instanceof Mech) || (unitEntity instanceof Tank)) {
            int mgCount = CampaignMain.cm.getMachineGunCount(unitEntity.getWeaponList());
            msg.append(mgCount);

            if (mgCount > 0) {

                for (int location = 0; location < unitEntity.locations(); location++) {
                    for (int slot = 0; slot < unitEntity.getNumberOfCriticals(location); slot++) {
                        CriticalSlot crit = unitEntity.getCritical(location, slot);

                        if ((crit == null) || (crit.getType() != CriticalSlot.TYPE_EQUIPMENT)) {
                            continue;
                        }

                        Mounted m = crit.getMount();

                        if ((m == null) || !(m.getType() instanceof WeaponType)) {
                            continue;
                        }

                        WeaponType wt = (WeaponType) m.getType();

                        if (!wt.hasFlag(WeaponType.F_MG)) {
                            continue;
                        }

                        msg.append(location);
                        msg.append(slot);
                        msg.append(m.isRapidfire());
                    }
                }
            }
        } else {
            msg.append(0);
        }

        msg.append(0);
        msg.append(targetSystem.getCurrentType());
        msg.append(isSupportUnit() ? "1" : "0");
        msg.append(getScrappableFor());
        if (CampaignMain.cm.isUsingAdvanceRepair()) {
            // do not need to save ammo twice so set sendAmmo to False
            msg.append(UnitUtils.unitBattleDamage(getEntity(), false));
        } else {
            msg.append("%%-%%-%%-");
        }

        if (toPlayer) {
            msg.append(getPilotIsRepairing());
        }
        if (!toPlayer) {
            msg.append(getLastCombatPilot());
        }

        msg.append(getCurrentRepairCost());
        msg.append(getLifeTimeRepairCost());
        msg.append(this.isChristmasUnit());
        return msg.getMessage();
    }

    /**
     * Reads a Entity from a String
     *
     * @param s A string to read from
     * @return the remaining String
     */
    public String fromString(String s) {
        try {
            s = s.substring(3);

            StringTokenizer ST = new StringTokenizer(s, "$");
            setUnitFilename(TokenReader.readString(ST));

            setPosId(TokenReader.readInt(ST));
            int newstate = TokenReader.readInt(ST); // status read-in
            setProducer(TokenReader.readString(ST));
            SPilot p = new SPilot();
            p.fromFileFormat(TokenReader.readString(ST), "#");

            setWeightClass(TokenReader.readInt(ST));

            setId(TokenReader.readInt(ST));
            if (CampaignMain.cm.getCurrentUnitID() <= getId()) {
                CampaignMain.cm.setCurrentUnitID(getId() + 1);
            }

            if (getId() == 0) {
                setId(CampaignMain.cm.getAndUpdateCurrentUnitID());
            }
            /*
             * Handle unit status. FOR_SALE and AdvanceRepair both require
             * special handling. If the unit is FOR_SALE, make sure a listing
             * still exists. If not, the server probably crashed and the unit
             * should be returned to normal.
             */
            if ((newstate == STATUS_FORSALE)
                    && (CampaignMain.cm.getMarket().getListingForUnit(getId()) == null)) {
                setStatus(STATUS_OK);
            } else if (CampaignMain.cm.isUsingAdvanceRepair()) {
                setStatus(STATUS_OK);
            } else {
                setStatus(newstate);
            }

            unitEntity = SUnit.loadMech(getUnitFilename());
            setEntity(unitEntity);
            init();
            this.setPilot(p);

            // if its an OMG unit it won't have Ammo
            if (getModelName().equals("OMG-UR-FD")) {
                return s;
            }

            if (unitEntity instanceof Mech) {
                ((Mech) unitEntity).setAutoEject(TokenReader.readBoolean(ST));
            }
            String defaultField = "0";
            if (ST.hasMoreElements()) {
                Entity en = getEntity();
                int maxCrits = TokenReader.readInt(ST);
                defaultField = TokenReader.readString(ST);
                List<Mounted> e = en.getAmmo();
                for (int count = 0; count < maxCrits; count++) {
                    int weaponType = Integer.parseInt(defaultField);
                    String ammoName = TokenReader.readString(ST);
                    int shots = TokenReader.readInt(ST);
                    boolean hotloaded = false;
                    // needed to make backwards compatibility better.
                    try {
                        defaultField = TokenReader.readString(ST);
                        hotloaded = Boolean.parseBoolean(defaultField);
                        defaultField = TokenReader.readString(ST);
                    } catch (Exception ex) {
                        hotloaded = false;
                    }

                    if (!CampaignMain.cm
                            .getMegaMekClient()
                            .getGame()
                            .getOptions()
                            .booleanOption("tacops_hotload")) {
                        hotloaded = false;
                    }

                    Mounted mWeapon = e.get(count);

                    AmmoType ammoType = getEntityAmmo(weaponType, ammoName);
                    if (ammoType == null) {
                        // loaded --Torren.
                        continue;
                    }

                    if (CampaignMain.cm
                            .getData()
                            .getBannedAmmoStore()
                            .isBanned(ammoType.getMunitionType(), null)) {
                        continue;
                    }

                    mWeapon.changeAmmoType(ammoType);
                    mWeapon.setShotsLeft(shots);
                    mWeapon.setHotLoad(hotloaded);
                }
                setEntity(en);
            }
            int maxMachineGuns = Integer.parseInt(defaultField);
            Entity en = getEntity();
            for (int count = 0; count < maxMachineGuns; count++) {
                int location = TokenReader.readInt(ST);
                int slot = TokenReader.readInt(ST);
                boolean selection = TokenReader.readBoolean(ST);
                try {
                    CriticalSlot cs = en.getCritical(location, slot);
                    Mounted m = cs.getMount();
                    m.setRapidfire(selection);
                } catch (Exception ex) {
                }
            }
            setEntity(en);
            targetSystem.setEntity(en);
            TokenReader.readString(ST); // unused
            int tsType = TokenReader.readInt(ST);
            if ((tsType != TargetSystem.TS_TYPE_STANDARD)
                    && CampaignData.cd.targetSystemIsBanned(tsType)) {
                tsType = TargetSystem.TS_TYPE_STANDARD;
            }
            targetSystem.setTargetSystem(tsType);

            TokenReader.readInt(ST); // Placeholder for isSupportUnit
            // Now we need to override this.  Needs to be set in the string,
            // so we don't need to keep a list of all support units client-side
            // but should be dynamic server-side.
            if (CampaignMain.cm
                    .getSupportUnits()
                    .contains(getUnitFilename().trim().toLowerCase())) {
                setSupportUnit(true);
            } else {
                setSupportUnit(false);
            }

            setScrappableFor(TokenReader.readInt(ST));

            if (CampaignMain.cm.isUsingAdvanceRepair()
                    && ((unitEntity instanceof Mech) || (unitEntity instanceof Tank))) {
                UnitUtils.applyBattleDamage(
                        unitEntity,
                        TokenReader.readString(ST),
                        ((MWServ.getInstance().getRTT() != null)
                                && (MWServ.getInstance().getRTT().unitRepairTimes(getId())
                                        != null)));
            } else {
                TokenReader.readString(ST);
            }
            setLastCombatPilot(TokenReader.readInt(ST));

            setRepairCosts(TokenReader.readInt(ST), TokenReader.readInt(ST));

            setChristmasUnit(TokenReader.readBoolean(ST));

            // quirks might be changed by SO, drop old quirks, then reset them.
            if (ST.hasMoreTokens()) {
                TokenReader.readString(ST);
            }
            return s;
        } catch (Exception ex) {
            LOGGER.error("Unable to Load SUnit: '{}', '{}'", s, ex.getMessage());
            // the unit should still be good return what did get set
            return s;
        }
    }

    /**
     * @return a description of the entity including pilot
     */
    public String getDescription(boolean showLink) {
        String status = "";

        if (CampaignMain.cm.isUsingAdvanceRepair()) {
            if (UnitUtils.hasCriticalDamage(getEntity())) {
                status = "Is Critically Damaged";
            } else if (UnitUtils.hasArmorDamage(getEntity())) {
                status = "Has Minor Armor Damage";
            } else if (UnitUtils.isRepairing(getEntity())) {
                status = "Is Currently Under Going Repairs";
            } else {
                status = "Is Fully Functional";
            }
        } else {
            if (getStatus() == Unit.STATUS_UNMAINTAINED) {
                status = "Unmaintained" + " (" + getMaintainanceLevel() + "%)";
            } else {
                status = "Maintained" + " (" + getMaintainanceLevel() + "%)";
            }
        }

        String idToShow = "";
        if (showLink) {
            idToShow = "<a href=\"MEKWARS/c sth#u#" + getId() + "\">#" + getId() + "</a>";
        } else {
            idToShow = "#" + getId();
        }
        String dialogBox =
                "<a href=\"MEKINFO"
                        + getEntity().getChassis()
                        + " "
                        + getEntity().getModel().replace("\"", "%22")
                        + "#"
                        + getBVForMatch()
                        + "#"
                        + getPilot().getGunnery()
                        + "#"
                        + getPilot().getPiloting()
                        + "\">"
                        + getModelName()
                        + "</a>";

        if ((getType() == Unit.MEK) || (getType() == Unit.VEHICLE)) {
            return idToShow
                    + " "
                    + dialogBox
                    + " ("
                    + getPilot().getGunnery()
                    + "/"
                    + getPilot().getPiloting()
                    + ") ["
                    + getPilot().getExperience()
                    + " EXP "
                    + getPilot().getSkillString(false)
                    + "] Kills: "
                    + getPilot().getKills()
                    + " "
                    + getProducer()
                    + ". BV: "
                    + getBVForMatch()
                    + " "
                    + status;
        }

        if ((getType() == Unit.INFANTRY) || (getType() == Unit.BATTLEARMOR)) {
            if (((Infantry) getEntity()).canMakeAntiMekAttacks()) {
                return idToShow
                        + " "
                        + dialogBox
                        + " ("
                        + getPilot().getGunnery()
                        + "/"
                        + getPilot().getPiloting()
                        + ") ["
                        + getPilot().getExperience()
                        + " EXP "
                        + getPilot().getSkillString(false)
                        + "] Kills: "
                        + getPilot().getKills()
                        + " "
                        + getProducer()
                        + ". BV: "
                        + getBVForMatch()
                        + " "
                        + status;
            }
        }
        // else
        return idToShow
                + " "
                + dialogBox
                + " ("
                + getPilot().getGunnery()
                + ") ["
                + getPilot().getExperience()
                + " EXP "
                + getPilot().getSkillString(false)
                + "] Kills: "
                + getPilot().getKills()
                + " "
                + getProducer()
                + ". BV: "
                + getBVForMatch()
                + " "
                + status;
    }

    /**
     * @return a smaller description
     */
    public String getSmallDescription() {
        String result;
        if ((getType() == Unit.MEK) || (getType() == Unit.VEHICLE) || (getType() == Unit.AERO)) {
            result =
                    getModelName()
                            + " ["
                            + getPilot().getGunnery()
                            + "/"
                            + getPilot().getPiloting();
        } else if ((getType() == Unit.INFANTRY) || (getType() == Unit.BATTLEARMOR)) {
            if (((Infantry) getEntity()).canMakeAntiMekAttacks()) {
                result =
                        getModelName()
                                + " ["
                                + getPilot().getGunnery()
                                + "/"
                                + getPilot().getPiloting();
            } else {
                result = getModelName() + " [" + getPilot().getGunnery();
            }
        } else {
            result = getModelName() + " [" + getPilot().getGunnery();
        }

        if (!getPilot().getSkillString(true).equals(" ")) {
            result += getPilot().getSkillString(true);
        }
        result += "]";
        return result;
    }

    /**
     * Returns the Modelname for this Unit
     *
     * @return the Modelname
     */
    public String getModelName() {
        if (checkModelName() == null) {
            unitEntity = SUnit.loadMech(getUnitFilename());
            init();
        }

        return checkModelName();
    }

    public String getVerboseModelName() {
        // Includes Pilot Stats in ModelName
        if ((getType() == Unit.MEK) || (getType() == Unit.VEHICLE) || (getType() == Unit.AERO)) {
            return getModelName()
                    + " ("
                    + getPilot().getGunnery()
                    + "/"
                    + getPilot().getPiloting()
                    + ")";
        }

        if ((getType() == Unit.INFANTRY) || (getType() == Unit.BATTLEARMOR)) {
            if (((Infantry) getEntity()).canMakeAntiMekAttacks()) {
                return getModelName()
                        + " ("
                        + getPilot().getGunnery()
                        + "/"
                        + getPilot().getPiloting()
                        + ")";
            }
        }

        return getModelName() + " (" + getPilot().getGunnery() + ")";
    }

    /**
     * @return the BV of this entity including all modifications
     */
    public int calcBV() {
        try {
            if (hasVacantPilot()) {
                getEntity().getCrew().setGunnery(4);
                getEntity().getCrew().setPiloting(5);
            } else {
                /*
                 * FIXME: This is an unfortunate hack. While we know the entity is clan or not we
                 * should base if the pilot is a clanner or not based on the faction not the mech.
                 * It looks like there is no way to get the faction this unit belongs to at the
                 * moment.
                 */
                getEntity().setCrew(UnitUtils.createEntityPilot(this, getEntity().isClan()));
            }

            // get a base BV from MegaMek
            int calcedBV = getEntity().calculateBattleValue();

            // Boost BV of super-fast tanks if the "FastHoverBVMod" is a
            // positive
            // number.
            int FastHoverBVMod = CampaignMain.cm.getIntegerConfig("FastHoverBVMod");
            if ((FastHoverBVMod > 0)
                    && (getType() == Unit.VEHICLE)
                    && (getEntity().getMovementMode() == megamek.common.EntityMovementMode.HOVER)) {
                if (getEntity().getWalkMP() >= 8) {
                    calcedBV += FastHoverBVMod;
                }
            }

            // Increase elite BV's by 5% if the "ElitePilotsBVMod" is enabled.
            if (CampaignMain.cm.getBooleanConfig("ElitePilotsBVMod")) {
                if (getPilot().getGunnery() < 3) {
                    calcedBV = (int) Math.round(calcedBV * 1.05);
                } else if (getPilot().getPiloting() < 3) {
                    calcedBV = (int) Math.round(calcedBV * 1.05);
                }
            }

            // Increase BV if the pilot has MaxTech/MechWarrior skills.
            calcedBV += getPilotSkillBV();

            if (hasVacantPilot()) {
                getEntity().getCrew().setGunnery(99);
                getEntity().getCrew().setPiloting(99);
            }
            return calcedBV;
        } catch (Exception ex) {
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public boolean equals(Object o) {
        SUnit m = null;
        try {
            m = (SUnit) o;
        } catch (ClassCastException e) {
            return false;
        }

        if (m == null) {
            return false;
        }

        if ((m.getId() == getId())
                && m.getUnitFilename().equals(getUnitFilename())
                && (m.getPilot().getGunnery() == getPilot().getGunnery())
                && (m.getPilot().getPiloting() == getPilot().getPiloting())) {
            return true;
        }
        return false;
    }

    /**
     * Sets the Pilot of this entity
     *
     * @param p A pilot
     */
    public void setPilot(SPilot p) {
        // zero BV any time a new pilot is added
        setBV(0);

        if (p == null) {
            return;
        }

        // any time the pilot changes set the unit commander flag to false.
        Crew mPilot =
                new Crew(
                        CrewType.SINGLE,
                        p.getName(),
                        1,
                        p.getGunnery(),
                        p.getPiloting(),
                        Gender.RANDOMIZE,
                        false,
                        null);
        Entity entity = getEntity();

        // Lazy Bug report. non Anti-Mek BA should not have a Piloting skill
        // better/worse then 5
        if ((getEntity() instanceof BattleArmor)
                && !((BattleArmor) getEntity()).canMakeAntiMekAttacks()
                && !hasVacantPilot()) {
            mPilot.setPiloting(5);
        }

        entity.setCrew(mPilot);
        setEntity(entity);

        if (p.getSkills().has(PilotSkill.WeaponSpecialistSkillID)) {
            for (PilotSkill skill : p.getSkills().getPilotSkills()) {
                if (skill.getName().equals("Weapon Specialist")
                        && p.getWeapon().equals("Default")) {
                    // LOGGER.error("setPilot inside");
                    p.getSkills().remove(skill);
                    ((WeaponSpecialistSkill) skill).assignWeapon(getEntity(), p);
                    skill.addToPilot(p);
                    skill.modifyPilot(p);
                    break;
                }
            }
        }

        p.setUnitType(getType());
        super.setPilot(p);
    }

    public void init() {
        setType(Unit.getEntityType(getEntity()));

        // Set Modelname
        if ((getType() != Unit.MEK) || getEntity().isOmni()) {
            setModelname(new String(unitEntity.getChassis() + " " + unitEntity.getModel()).trim());
        } else {

            if (unitEntity.getModel().trim().length() > 0) {
                setModelname(unitEntity.getModel().trim());
            } else {
                setModelname(unitEntity.getChassis().trim());
            }
        }
        getC3Type(unitEntity);

        if (getModelName().equals("OMG-UR-FD")) {
            setProducer("Error loading unit. Tried to build from " + getUnitFilename());
            setWeightClass(Unit.LIGHT);
        }
    }

    /**
     * Sets status to unmaintained. Factors out repetetive code checking maintainance status and
     * decreasing as unit is moved to unmaintained. Called from both Player and
     * SetUnmaintainedCommand. It would possible to bypass this code and set a unit as unmaintained
     * without incurring any maintainance penalty w/ Unit.setStatus(STATUS_UNMAINTAINED).
     *
     * @urgru 8/4/04
     */
    public void setUnmaintainedStatus() {
        if (CampaignMain.cm.isUsingAdvanceRepair()) {
            setStatus(STATUS_OK);
            return;
        }

        // load configurables
        int baseUnmaintained =
                CampaignData.cd.getCampaignOptions().getIntegerConfig("BaseUnmaintainedLevel");
        int unmaintPenalty =
                CampaignData.cd.getCampaignOptions().getIntegerConfig("UnmaintainedPenalty");

        // set the actual status
        setStatus(STATUS_UNMAINTAINED);

        /*
         * now change the maintainance levels. if the unit is well maintained,
         * drop it to the basevalue. otherwise, apply the standard penalty.
         */
        if (getMaintainanceLevel() >= (baseUnmaintained + unmaintPenalty)) {
            setMaintainanceLevel(baseUnmaintained);
        } else {
            addToMaintainanceLevel(-unmaintPenalty);
        }
    } // end setUnmaintainedStatus()

    public int getBV() {
        int toReturn = 0;

        if (super.getBV() <= 0) {
            toReturn = calcBV();
            super.setBV(toReturn);
        } else {
            toReturn = super.getBV();
        }
        return Math.max(0, toReturn);
    }

    /**
     * @return the megamek.common.entity this Unit represents
     */
    @Override
    public Entity getEntity() {
        if (super.getEntity() != null) {
            return super.getEntity();
        }

        setEntity(SUnit.loadMech(getUnitFilename()));
        return super.getEntity();
    }

    public static Entity loadMech(String filename) {
        if (filename == null) {
            return null;
        }

        MechSummary mechSummary = MechSummaryCache.getInstance().getMech(filename);
        if (mechSummary == null) {
            LOGGER.error(
                    "Cannot find MechSummary for '{}', please validate units.cache is correct",
                    filename);
            return UnitUtils.createOMG();
        }
        try {
            MechFileParser fileParser =
                    new MechFileParser(mechSummary.getSourceFile(), mechSummary.getEntryName());

            return fileParser.getEntity();
        } catch (EntityLoadingException e) {
            LOGGER.error("Cannot load unit '{}': {}", filename, e);
            return UnitUtils.createOMG();
        }
    } // end loadMech

    public void setPassesMaintainanceUntil(long passesMaintainanceUntil) {
        this.passesMaintainanceUntil = passesMaintainanceUntil;
    }

    public long getPassesMaintainanceUntil() {
        return passesMaintainanceUntil;
    }

    /**
     * @return the amount of EXP the pilot has
     */
    public int getExperience() {
        return getPilot().getExperience();
    }

    /**
     * @param experience the experience to set the pilot to
     */
    public void setExperience(Integer experience) {
        getPilot().setExperience(experience.intValue());
        // this.experience = experience;
    }

    public boolean isOmni() {
        return super.isOmni("./data/buildtables/omnivehiclelist.txt");
    }

    public int getPilotSkillBV() {
        int skillBV = 0;
        Iterator<PilotSkill> pilotSkills = getPilot().getSkills().getSkillIterator();

        for (PilotSkill skill : getPilot().getSkills().getPilotSkills()) {
            skillBV += skill.getBVMod(getEntity(), (SPilot) getPilot());
        }

        return skillBV;
    }

    public int getLastCombatPilot() {
        return lastCombatPilot;
    }

    public void setLastCombatPilot(int pilot) {
        lastCombatPilot = pilot;
    }

    @Override
    public void setWeightClass(int weightClass) {
        if ((weightClass > Unit.ASSAULT) || (weightClass < Unit.LIGHT)) {
            weightClass = Unit.getEntityWeight(getEntity());
        }

        super.setWeightClass(weightClass);
    }

    public static List<SUnit> createMULUnits(String filename) {
        String newbieHouseName = CampaignData.cd.getCampaignOptions().getConfig("NewbieHouseName");
        House house = CampaignData.cd.getHouseByName(newbieHouseName);

        return SUnit.createMULUnits(house, filename, "autoassigned unit");
    }

    public static List<SUnit> createMULUnits(House house, String filename, String fluff) {
        ArrayList<SUnit> mulUnits = new ArrayList<>();

        Vector<Entity> loadedUnits = null;
        File entityFile = new File("data/armies/" + filename);

        try {
            loadedUnits = new MULParser(entityFile, null).getEntities();
            loadedUnits.trimToSize();
        } catch (Exception ex) {
            LOGGER.error("Unable to load file " + entityFile.getName());
            LOGGER.error("Exception: ", ex);
            return mulUnits;
        }

        for (Entity en : loadedUnits) {
            SUnit cm = new SUnit();

            cm.setEntity(en);
            cm.setUnitFilename(UnitUtils.getEntityFileName(en));
            cm.setId(CampaignMain.cm.getAndUpdateCurrentUnitID());
            cm.init();
            cm.setProducer(fluff);

            SPilot pilot = null;
            pilot =
                    new SPilot(
                            house,
                            en.getCrew().getName(),
                            en.getCrew().getGunnery(),
                            en.getCrew().getPiloting());

            if (pilot.getName().equalsIgnoreCase("Unnamed")
                    || pilot.getName().equalsIgnoreCase("vacant")) {
                pilot.setName(SPilot.getRandomPilotName(CampaignMain.cm.getR()));
            }

            StringTokenizer skillList =
                    new StringTokenizer(
                            en.getCrew().getOptionList(",", PilotOptions.LVL3_ADVANTAGES), ",");

            while (skillList.hasMoreTokens()) {
                String skill = skillList.nextToken();

                if (skill.toLowerCase().startsWith("weapon_specialist")) {
                    pilot.addMegamekOption(new MegaMekPilotOption("weapon_specialist", true));
                    pilot.getSkills()
                            .add(PilotSkillStore.getPilotSkill(PilotSkill.WeaponSpecialistSkillID));
                    pilot.setWeapon(skill.substring("weapon_specialist".length()).trim());
                } else if (skill.toLowerCase().startsWith("edge ")) {
                    pilot.addMegamekOption(new MegaMekPilotOption("edge", true));
                    pilot.getSkills().add(PilotSkillStore.getPilotSkill(PilotSkill.EdgeSkillID));
                    try {
                        pilot.getSkills()
                                .getPilotSkill(PilotSkill.EdgeSkillID)
                                .setLevel(
                                        Integer.parseInt(skill.substring("edge ".length()).trim()));
                    } catch (Exception ex) {
                        pilot.getSkills().getPilotSkill(PilotSkill.EdgeSkillID).setLevel(1);
                    }
                } else if (skill.toLowerCase().equals("edge_when_headhit")) {
                    pilot.setHeadHit(true);
                } else if (skill.toLowerCase().equals("edge_when_tac")) {
                    pilot.setTac(true);
                } else if (skill.toLowerCase().equals("edge_when_ko")) {
                    pilot.setKO(true);
                } else if (skill.toLowerCase().equals("edge_when_explosion")) {
                    pilot.setExplosion(true);
                } else {
                    pilot.getSkills()
                            .add(PilotSkillStore.getPilotSkill(PilotSkill.getMMSkillID(skill)));
                    pilot.addMegamekOption(new MegaMekPilotOption(skill, true));
                }
            }

            skillList =
                    new StringTokenizer(
                            en.getCrew().getOptionList(",", PilotOptions.MD_ADVANTAGES), ",");

            while (skillList.hasMoreTokens()) {
                String skill = skillList.nextToken();

                pilot.getSkills()
                        .add(PilotSkillStore.getPilotSkill(PilotSkill.getMMSkillID(skill)));
                pilot.addMegamekOption(new MegaMekPilotOption(skill, true));
            }

            cm.setPilot(pilot);

            cm.setWeightClass(99); // let the SUnit code handle the weightclass

            mulUnits.add(cm);
        }
        return mulUnits;
    }

    /**
     * Compares SUnit IDs to support sorting of collections
     *
     * @author Spork
     */
    @Override
    public int compareTo(SUnit u) {
        return Integer.valueOf(getId()).compareTo(Integer.valueOf(u.getId()));
    }

    @Override
    public boolean isSupportUnit() {
        return CampaignMain.cm.getSupportUnits().contains(getUnitFilename().toLowerCase());
    }

    public void reportStateToPlayer(SPlayer player) {
        CampaignMain.cm.toUser("PL|UU|" + getId() + "|" + toString(true), player.getName(), false);
    }

    public boolean isOMGUnit() {
        return getModelName().equals("OMG-UR-FD");
    }

    public boolean canBeCapturedInOperation(Operation o) {
        switch (getType()) {
            case Unit.MEK:
                return o.getBooleanValue("ForceProduceAndCaptureMeks");
            case Unit.VEHICLE:
                return o.getBooleanValue("ForceProduceAndCaptureVees");
            case Unit.INFANTRY:
                return o.getBooleanValue("ForceProduceAndCaptureInfs");
            case Unit.PROTOMEK:
                return o.getBooleanValue("ForceProduceAndCaptureProtos");
            case Unit.BATTLEARMOR:
                return o.getBooleanValue("ForceProduceAndCaptureBAs");
            case Unit.AERO:
                return o.getBooleanValue("ForceProduceAndCaptureAeros");
        }
        return false;
    }

    /**
     * Creates a unit
     *
     * <p>create() takes a number of variables and creates a unit. This is called by both the
     * Christmas code and the /CreateUnit command.
     *
     * @param filename The file name of the unit
     * @param fluff Any flavor text
     * @param gunnery Gunnery skill of the pilot
     * @param piloting Piloting skill of the pilot
     * @param weight Weight class to be used (note: why? Why can't we get rid of this?)
     * @param skillTokens Pilot skills
     * @return the created unit
     */
    public static SUnit create(
            House house,
            String filename,
            String fluff,
            int gunnery,
            int piloting,
            Integer weight,
            String skillTokens) {
        boolean refigureWeightClass = false;

        if (weight == null) {
            weight = SUnit.LIGHT;
            // This is stupid.  We should not have to specify weight classes.  So now we do not.
            refigureWeightClass = true;
        }

        SUnit cm = new SUnit(fluff, filename, weight);

        if (refigureWeightClass) {
            cm.setWeightClass(cm.getEntity().getWeightClass());
            LOGGER.debug(
                    "Setting "
                            + cm.getEntity().getModel()
                            + " to weight class "
                            + cm.getEntity().getWeightClass());
        }

        SPilot pilot = null;
        if (gunnery == 99 || piloting == 99) {
            pilot = new SPilot(null, "Vacant", 99, 99);
        } else {
            pilot =
                    new SPilot(
                            house,
                            SPilot.getRandomPilotName(CampaignMain.cm.getR()),
                            gunnery,
                            piloting);
        }

        if (skillTokens != null) {
            StringTokenizer skillList = new StringTokenizer(skillTokens, ",");

            while (skillList.hasMoreTokens()) {
                String skill = skillList.nextToken();
                PilotSkill pSkill = null;
                if (skill.equalsIgnoreCase("random")) {
                    pSkill = PilotSkillStore.getRandomSkill(pilot, cm.getType());
                } else {
                    pSkill = PilotSkillStore.getPilotSkill(skill);
                }

                if (pSkill != null) {
                    if (pSkill instanceof TraitSkill) {
                        ((TraitSkill) pSkill).assignTrait(pilot);
                    }
                    pSkill.addToPilot(pilot);
                    pSkill.modifyPilot(pilot);
                }
            }
        }
        cm.setPilot(pilot);
        return cm;
    }
}
