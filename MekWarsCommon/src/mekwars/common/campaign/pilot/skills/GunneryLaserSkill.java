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
 * Created on 18.04.2004
 *
 */
package mekwars.common.campaign.pilot.skills;

import megamek.common.Crew;
import megamek.common.Entity;
import megamek.common.Mounted;
import megamek.common.WeaponType;
import megamek.common.battlevalue.BVCalculator;

import mekwars.common.CampaignData;
import mekwars.common.MegaMekPilotOption;
import mekwars.common.campaign.pilot.Pilot;

/**
 * NOTE: This is a unofficial rule. Pilot gets a -1 to-hit bonus on all energy-based weapons (Laser,
 * PPC, and Flamer). @@author Torren (Jason Tighe)
 */
public class GunneryLaserSkill extends PilotSkill {
    public GunneryLaserSkill(int id) {
        super(id, "Gunnery/Laser", "GL");
        setDescription(
                "NOTE: This is a unofficial rule. Pilot gets a -1 to-hit bonus on all energy-based"
                        + " weapons (Laser, PPC, and Flamer).");
    }

    public GunneryLaserSkill() {
        // TODO: replace with ReflectionProvider
    }

    @Override
    public void modifyPilot(Pilot pilot) {
        pilot.addMegamekOption(new MegaMekPilotOption("gunnery_laser", true));
        // pilot.setBvMod(pilot.getBVMod() + 0.02);
    }

    @Override
    public int getBVMod(Entity unit, Pilot p) {
        if (CampaignData.cd.getCampaignOptions().getBooleanConfig("USEFLATGUNNERYLASERMODIFIER")) {
            return getBVModFlat(unit);
        }
        // new bv cost for GunneryX and Weapon Specialist skills,
        // also known as "if it gets a 1 better gunnery with all its weapons then it should pay for
        // the full level of gunnery"
        // the formula applies the "PilotBVSkillMultiplier" delta to (bv% of effected weapons verse
        // all weapons)
        // parallel code is used in GunneryLaserSkill.java, GunneryMissileSkill.java,
        // GunneryBallisticsSkill.java, and WeaponSpecialistSkill.java
        double sumWeaponBV = 0;
        double effectedWeaponBV = 0;
        final Crew crew = unit.getCrew();
        double bvSkillDelta =
                BVCalculator.bvSkillMultiplier(crew.getGunnery() - 1, crew.getPiloting())
                        / BVCalculator.bvSkillMultiplier(crew.getGunnery(), crew.getPiloting());
        for (Mounted weapon : unit.getWeaponList()) {
            sumWeaponBV += weapon.getType().getBV(unit);
            if (weapon.getType().hasFlag(WeaponType.F_ENERGY)
                    && !weapon.getType().hasFlag(WeaponType.F_AMS)) {
                effectedWeaponBV += weapon.getType().getBV(unit);
            }
        }
        return (int)
                (unit.calculateBattleValue(false, true)
                        * (effectedWeaponBV / sumWeaponBV)
                        * (bvSkillDelta - 1));
    }

    public int getBVModFlat(Entity unit) {
        int numberOfLasers = 0;
        int gunneryLaserBVBaseMod =
                CampaignData.cd.getCampaignOptions().getIntegerConfig("GunneryLaserBaseBVMod");

        for (Mounted weapon : unit.getWeaponList()) {
            if (weapon.getType().hasFlag(WeaponType.F_ENERGY)) {
                numberOfLasers++;
            }
        }
        return numberOfLasers * gunneryLaserBVBaseMod;
    }
}
