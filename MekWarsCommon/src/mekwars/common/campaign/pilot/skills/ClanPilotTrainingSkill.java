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

import mekwars.common.campaign.pilot.Pilot;
import megamek.common.Entity;

/**
 * @author Helge Richter
 */
public class ClanPilotTrainingSkill extends PilotSkill {
    public ClanPilotTrainingSkill() {
        // TODO: replace with ReflectionProvider
    }

    public ClanPilotTrainingSkill(int id) {
        super(id, "Clan Pilot Training", "CPT");
        setDescription("Pilot has a +1 penalty for physical attacks, because clans do not train for dishonourable combat.");
    }

    @Override
    public void modifyPilot(Pilot pilot) {
        pilot.addMegamekOption("clan_pilot_training", true);
        // piilot.setBvMod(pilot.getBVMod() + 0.01);
    }

    @Override
    public int getChance(int unitType, Pilot p) {
        return 0;
    }

    @Override
    public int getBVMod(Entity unit, Pilot pilot) {
        return 0;
    }
}
