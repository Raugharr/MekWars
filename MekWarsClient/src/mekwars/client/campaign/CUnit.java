/*
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

import megamek.common.AmmoType;
import megamek.common.CriticalSlot;
import megamek.common.Infantry;
import megamek.common.Mech;
import megamek.common.Mounted;
import megamek.common.OffBoardDirection;
import megamek.common.options.IOption;
import megamek.common.options.IOptionGroup;
import megamek.common.options.Quirks;

import mekwars.client.MWClient;
import mekwars.common.CampaignData;
import mekwars.common.House;
import mekwars.common.MegaMekPilotOption;
import mekwars.common.Player;
import mekwars.common.Unit;
import mekwars.common.campaign.pilot.Pilot;
import mekwars.common.campaign.pilot.skills.PilotSkill;
import mekwars.common.campaign.targetsystems.TargetTypeNotImplementedException;
import mekwars.common.campaign.targetsystems.TargetTypeOutOfBoundsException;
import mekwars.common.util.TokenReader;
import mekwars.common.util.UnitUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Enumeration;
import java.util.List;
import java.util.StringJoiner;
import java.util.StringTokenizer;

/** Class for unit object used by Client */
public class CUnit extends Unit {
    private static final Logger LOGGER = LogManager.getLogger(CUnit.class);

    private String htmlQuirkList = " ";
    private String quirkList = " ";

    public CUnit() {
        init();
    }

    private void init() {
        setBV(0);
        setStatus(STATUS_OK);
        setProducer("unknown origin");
    }

    public boolean setData(String data) {
        StringTokenizer ST;
        String element;
        String unitDamage = null;
        LOGGER.info("PDATA: " + data);

        ST = new StringTokenizer(data, "$");
        element = TokenReader.readString(ST);
        if (!element.equals("CM")) {
            return (false);
        }

        setUnitFilename(TokenReader.readString(ST));
        setId((TokenReader.readInt(ST)));
        setStatus(TokenReader.readInt(ST));

        setProducer(TokenReader.readString(ST));
        String pilotname = "John Denver";
        int gunnery = 4;
        int piloting = 5;
        int exp = 0;
        Pilot p = null;
        StringTokenizer STR = new StringTokenizer(TokenReader.readString(ST), "#");
        pilotname = TokenReader.readString(STR);
        exp = TokenReader.readInt(STR);
        gunnery = TokenReader.readInt(STR);
        piloting = TokenReader.readInt(STR);
        House house = CampaignData.cd.getHouseByName(TokenReader.readString(STR));
        p = new Pilot(house, pilotname, gunnery, piloting);
        p.setExperience(exp);
        int skillAmount = TokenReader.readInt(STR);
        for (int i = 0; i < skillAmount; i++) {
            PilotSkill skill =
                    new PilotSkill(
                            TokenReader.readInt(STR),
                            TokenReader.readString(STR),
                            TokenReader.readInt(STR),
                            TokenReader.readString(STR));

            if (skill.getName().equals("Weapon Specialist")) {
                p.setWeapon(TokenReader.readString(STR));
            }

            if (skill.getName().equals("Trait")) {
                p.setTraitName(TokenReader.readString(STR));
            }

            if (skill.getName().equals("Edge")) {
                p.setTac(TokenReader.readBoolean(STR));
                p.setKO(TokenReader.readBoolean(STR));
                p.setHeadHit(TokenReader.readBoolean(STR));
                p.setExplosion(TokenReader.readBoolean(STR));
            }
            p.getSkills().add(skill);
        }

        p.setKills(TokenReader.readInt(STR));

        p.setHits(TokenReader.readInt(STR));

        int mmoptionsamount = TokenReader.readInt(ST);
        for (int i = 0; i < mmoptionsamount; i++) {
            MegaMekPilotOption mo =
                    new MegaMekPilotOption(
                            TokenReader.readString(ST),
                            Boolean.parseBoolean(TokenReader.readString(ST)));
            p.addMegamekOption(mo);
        }

        setType(TokenReader.readInt(ST));
        // setType(getEntityType(UnitEntity));
        setPilot(p);
        setBV(Math.max(TokenReader.readInt(ST), 0));

        setWeightClass(TokenReader.readInt(ST));
        // if (this.getType() == Unit.MEK || this.getType() == Unit.VEHICLE)
        // setWeightclass(getEntityWeight(UnitEntity));
        setId(TokenReader.readInt(ST));

        createEntity();

        // don't try to set ammo and eject on an OMG
        if (getModelName().startsWith("Error") || getModelName().startsWith("OMG")) {
            unitEntity.setExternalId(getId());
            return true;
        }

        // set autoeject if its a mech
        if ((unitEntity instanceof Mech) && ST.hasMoreElements()) {
            ((Mech) unitEntity).setAutoEject(Boolean.parseBoolean(TokenReader.readString(ST)));
        }

        // then set up ammo loadout
        {
            try {
                int maxCrits = TokenReader.readInt(ST);
                List<Mounted> ammoMountedList = unitEntity.getAmmo();
                for (int count = 0; count < maxCrits; count++) {
                    int weaponType = TokenReader.readInt(ST);
                    String ammoName = TokenReader.readString(ST);
                    int shots = TokenReader.readInt(ST);
                    boolean hotloaded = TokenReader.readBoolean(ST);

                    Mounted mWeapon = ammoMountedList.get(count);

                    AmmoType at = getEntityAmmo(weaponType, ammoName);
                    mWeapon.changeAmmoType(at);
                    mWeapon.setShotsLeft(shots);
                    mWeapon.setHotLoad(hotloaded);
                }
            } catch (Exception ex) {
                // ammo crits change or something bad. just continue with the
                // next unit
                return true;
            }
        } // end ammo

        // setup rapid fire Machine guns, if any
        {
            int maxMachineGuns = TokenReader.readInt(ST);
            for (int count = 0; count < maxMachineGuns; count++) {
                int location = TokenReader.readInt(ST);
                int slot = TokenReader.readInt(ST);
                boolean selection = TokenReader.readBoolean(ST);
                CriticalSlot cs = unitEntity.getCritical(location, slot);

                Mounted mg = cs.getMount();

                mg.setRapidfire(selection);
            }
        } // Machine Guns

        TokenReader.readString(ST); // unused

        targetSystem.setEntity(unitEntity);
        try {
            targetSystem.setTargetSystem(TokenReader.readInt(ST));
        } catch (TargetTypeOutOfBoundsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TargetTypeNotImplementedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        int suppUnit = TokenReader.readInt(ST);
        if (suppUnit == 1) {
            setSupportUnit(true);
        } else {
            setSupportUnit(false);
        }

        setScrappableFor(TokenReader.readInt(ST));

        unitDamage = TokenReader.readString(ST);

        setPilotIsRepairing(TokenReader.readBoolean(ST));

        setRepairCosts(TokenReader.readInt(ST), TokenReader.readInt(ST));

        setChristmasUnit(TokenReader.readBoolean(ST));

        // @salient Quirks - set unit quirks, or drop data if quirks have been turned off
        if (ST.hasMoreTokens()
                && CampaignData.cd.getCampaignOptions().getBooleanConfig("EnableQuirks")) {
            setUnitQuirks(TokenReader.readString(ST));
        } else if (ST.hasMoreTokens()) {
            TokenReader.readString(ST);
        }

        unitEntity.setExternalId(getId());

        if (unitDamage != null) {
            UnitUtils.applyBattleDamage(unitEntity, unitDamage, true);
        }

        getC3Type(unitEntity);

        return (true);
    }

    // @salient this method is only accessible when quirks are enabled.
    private void setUnitQuirks(String data) {
        StringTokenizer st = new StringTokenizer(data, "!");
        if (st.hasMoreTokens()) {
            htmlQuirkList = TokenReader.readString(st);
            quirkList = TokenReader.readString(st);
        }

        if (quirkList != null) {
            st = new StringTokenizer(quirkList, "&");
            while (st.hasMoreTokens()) {
                String quirk = TokenReader.readString(st);
                if (quirk.equalsIgnoreCase("none") == false)
                    unitEntity.getQuirks().getOption(quirk).setValue(true);
            }
        }
    }

    public String getHtmlQuirksList() {
        return htmlQuirkList;
    }

    public String getQuirksList() {
        return quirkList;
    }

    // @salient debug method, i really just used this once to make sure the quirks were being set
    // but i'll leave it in case one day someone needs it.
    public String quirkCheck() {
        StringJoiner quirksList = new StringJoiner("&");

        for (Enumeration<IOptionGroup> optionGroups = unitEntity.getQuirks().getGroups();
                optionGroups.hasMoreElements(); ) {
            IOptionGroup group = optionGroups.nextElement();
            if (unitEntity.getQuirks().count(group.getKey()) > 0) {
                for (Enumeration<IOption> options = group.getOptions();
                        options.hasMoreElements(); ) {
                    IOption option = options.nextElement();
                    if (option != null && option.booleanValue()) {
                        quirksList.add(option.getName());
                    }
                }
            }
        }
        return quirksList.toString();
    }

    public boolean hasQuirks() {
        for (Enumeration<IOptionGroup> optionGroups = unitEntity.getQuirks().getGroups();
                optionGroups.hasMoreElements(); ) {
            IOptionGroup group = optionGroups.nextElement();
            if (unitEntity.getQuirks().count(group.getKey()) > 0) {
                for (Enumeration<IOption> options = group.getOptions();
                        options.hasMoreElements(); ) {
                    IOption option = options.nextElement();
                    if (option != null && option.booleanValue()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Method which generates data for an auto unit. Since auto units have no unique properties this
     * can be assembled client side rather than sent from the server.
     *
     * @urgru 1/4/05
     */
    public void setAutoUnitData(House house, String filename, int distance,
            OffBoardDirection edge) {

        setUnitFilename(filename);
        setPilot(new Pilot(house, "Autopilot", 4, 5));
        createEntity();// make the entity
        if (distance > 0) {
            unitEntity.setOffBoard(distance, edge); // move
            // it
            // offboard
        }
    }

    public String getModelName() {
        if (getType() != MEK) {
            return new String(getEntity().getChassis() + " " + getEntity().getModel()).trim();
        }

        if (getEntity().isOmni()) {
            return new String(getEntity().getChassis() + " " + getEntity().getModel()).trim();
        }

        if (getEntity().getModel().trim().length() > 0) {
            return getEntity().getModel().trim();
        }
        return getEntity().getChassis().trim();
    }

    /** Tries to set UnitEntity from the global MekFileName */
    public void createEntity() {
        unitEntity = UnitUtils.createEntity(getUnitFilename());
        unitEntity.setCrew(UnitUtils.createEntityPilot(this, unitEntity.isClan()));

        if (unitEntity == null) {
            LOGGER.error("Error unit failed to load. Exiting.");
            System.exit(1);
        }

        if (unitEntity.getChassis().equals("Error")) {
            setProducer("Unable to find " + getUnitFilename() + " on clients system!");
        }
        getC3Type(unitEntity);
    }

    public boolean isOmni() {
        return super.isOmni("./data/mechfiles/omnivehiclelist.txt");
    }

    public int getOriginalBV() {
        return unitEntity.calculateBattleValue(false, false);
    }

    public void applyRepairs(String data) {
        createEntity();
        UnitUtils.applyBattleDamage(unitEntity, data, true);
    }

    public void setAntiAir(boolean aa) {
        Quirks quirks = unitEntity.getQuirks();
        quirks.getOption("anti_air").setValue(aa);
    }
}
