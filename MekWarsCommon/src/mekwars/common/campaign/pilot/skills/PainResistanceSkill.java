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

package mekwars.common.campaign.pilot.skills;

import megamek.common.Entity;
import megamek.common.Mech;

import mekwars.common.CampaignData;
import mekwars.common.House;
import mekwars.common.MegaMekPilotOption;
import mekwars.common.Unit;
import mekwars.common.campaign.pilot.Pilot;

/**
 * @author Helge Richter
 */
public class PainResistanceSkill extends PilotSkill {

    public PainResistanceSkill() {
        // TODO: replace with ReflectionProvider
    }

    public PainResistanceSkill(int id) {
        super(id, "Pain Resistance", "PR");
        setDescription(
                "When making consciousness rolls, 1 is added to all rolls. Also, damage received"
                    + " from ammo explosions is reduced to 1. Note: This ability is only used for"
                    + " BattleMechs.");
    }

    @Override
    public void modifyPilot(Pilot p) {
        // super.addToPilot(p);
        p.addMegamekOption(new MegaMekPilotOption("pain_resistance", true));
        p.setBvMod(p.getBVMod() + 0.01);
    }

    @Override
    public int getChance(int unitType, Pilot p) {
        if (unitType != Unit.MEK) {
            return 0;
        }
        return super.getChance(unitType, p);
    }

    @Override
    public int getBVMod(Entity unit, Pilot p) {
        // BK repeat of comments in getBVMod(Entity unit)
        int PainResistanceBVBaseMod = p.getHouse().getHouseOptions().getIntegerConfig("PainResistanceBaseBVMod");
        boolean b = false;
        if (unit instanceof Mech) {
            Mech m = (Mech) unit;
            b = m.hasCASEII();
        }
        if (unit.hasCase() || b) {
            return (int)
                    (2 * unit.calculateBattleValue(false, true) * PainResistanceBVBaseMod / 100);
        } else {
            return (int) (unit.calculateBattleValue(false, true) * PainResistanceBVBaseMod / 100);
        }
    }
}
