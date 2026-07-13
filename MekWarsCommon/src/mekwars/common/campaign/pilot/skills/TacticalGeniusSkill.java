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


import mekwars.common.campaign.pilot.Pilot;

/**
 * A pilot who has a Tactical Genius may reroll their initiative once per turn. The second roll must
 * be accepted.\n\nNote: Only one Tactical Genius may be utilized per team. @@author Torren (Jason
 * Tighe)
 */
public class TacticalGeniusSkill extends PilotSkill {
    public TacticalGeniusSkill(int id) {
        super(id, "Tactical Genius", "TG");
        setDescription(
                "A pilot who has a Tactical Genius may reroll their initiative once per turn.  The"
                        + " second roll must be accepted.");
    }

    public TacticalGeniusSkill() {
        // TODO: replace with ReflectionProvider
    }

    @Override
    public void modifyPilot(Pilot pilot) {
        pilot.addMegamekOption("tactical_genius", true);
        pilot.setBvMod(pilot.getBVMod() + 0.02);
    }
}
