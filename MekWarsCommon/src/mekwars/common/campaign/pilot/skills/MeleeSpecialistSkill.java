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

import megamek.common.BattleArmor;
import megamek.common.Entity;
import megamek.common.Mech;

import mekwars.common.CampaignData;
import mekwars.common.House;
import mekwars.common.MegaMekPilotOption;
import mekwars.common.Unit;
import mekwars.common.campaign.CampaignOptions;
import mekwars.common.campaign.pilot.Pilot;

/**
 * @author Helge Richter
 */
public class MeleeSpecialistSkill extends PilotSkill {

    public MeleeSpecialistSkill(int id) {
        super(id, "Melee Specialist", "MS");
        setDescription(
                "Enables the unit to do 1 additional point of damage with physical attacks and"
                    + " subtracts one from the attacker movement modifier (to a minimum of zero)."
                    + " Note: This ability is only used for BattleMechs.");
    }

    public MeleeSpecialistSkill() {
        // TODO: replace with ReflectionProvider
    }

    @Override
    public void modifyPilot(Pilot p) {
    }

    @Override
    public int getBVMod(Entity unit, Pilot p) {
        CampaignOptions campaignOptions = CampaignData.cd.getCampaignOptions();
        double tonnage = unit.getWeight();
        double numberOfHatchets = 0;
        double hatchetMod = campaignOptions.getDoubleConfig("HatchetRating");
        double baseBV = campaignOptions.getDoubleConfig("MeleeSpecialistBaseBVMod");
        double speedFactor;
        if (campaignOptions.getBooleanConfig("MeleeSpecialistUseSpeedFactor")) {
            int maxManeuvering = Math.max(unit.getJumpMP(), unit.getActiveUMUCount());
            double speedCalc = unit.getRunMP() + (Math.round(maxManeuvering / 2.0) - 5) / 10;
            // Adds a BV malus based on unit movement capability
            speedFactor = Math.pow(1 + speedCalc, 1.2);
        } else {
            speedFactor = 1.0;
        }

        if (p.getHouse() == null) {
            hatchetMod = campaignOptions.getDoubleConfig("HatchetRating");
            baseBV = campaignOptions.getDoubleConfig("MeleeSpecialistBaseBVMod");
        } else {
            hatchetMod = p.getHouse().getHouseOptions().getDoubleConfig("HatchetRating");
            baseBV = p.getHouse().getHouseOptions().getDoubleConfig("MeleeSpecialistBaseBVMod");
        }

        if (!(unit instanceof Mech) && !(unit instanceof BattleArmor)) {
            return 0;
        }

        numberOfHatchets = unit.getClubs().size();

        double total = baseBV * (tonnage / 10) * speedFactor + (hatchetMod * numberOfHatchets);
        return (int) total;
    }

    @Override
    public int getChance(int unitType, Pilot p) {
        if (unitType != Unit.MEK) {
            return 0;
        }
        return super.getChance(unitType, p);
    }
}
