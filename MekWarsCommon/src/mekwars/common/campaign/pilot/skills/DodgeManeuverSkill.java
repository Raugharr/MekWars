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

import mekwars.common.Unit;
import mekwars.common.campaign.pilot.Pilot;

/**
 * @author Helge Richter
 */
public class DodgeManeuverSkill extends PilotSkill {
    public DodgeManeuverSkill(int id) {
        super(id, "Dodge Maneuver", "DM");
        setDescription(
                "Enables the unit to make a dodge maneuver instead of a physical attack. This"
                        + " maneuver adds +2 to the BTH to physical attacks against the unit.");
    }

    public DodgeManeuverSkill() {
        // TODO: replace with ReflectionProvider
    }

    @Override
    public void modifyPilot(Pilot p) {
        p.addMegamekOption("dodge_maneuver", true);
        // p.setBvMod(p.getBVMod() + 0.01);
    }

    @Override
    public int getChance(int unitType, Pilot p) {
        if (unitType != Unit.MEK) {
            return 0;
        }
        return super.getChance(unitType, p);
    }

    @Override
    public String getBVModConfig() {
        return "DodgeManeuverBaseBVMod";
    }
}
