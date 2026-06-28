/*
 * MekWars - Copyright (C) 2008
 *
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

import mekwars.common.Unit;
import mekwars.common.campaign.pilot.Pilot;

public class PainShunt extends PilotSkill {

    public PainShunt(int id) {
        super(id, "Pain Shunt", "PS");
        setDescription("MD Pain Shunt");
    }

    @Override
    public void modifyPilot(Pilot p) {
        // super.addToPilot(p);
        p.addMegamekOption("pain_shunt", true);
        p.setBvMod(p.getBVMod() + 0.01);
    }

    @Override
    public int getChance(int unitType, Pilot p) {
        if (unitType == Unit.PROTOMEK) {
            return 0;
        }
        return super.getChance(unitType, p);
    }
}
