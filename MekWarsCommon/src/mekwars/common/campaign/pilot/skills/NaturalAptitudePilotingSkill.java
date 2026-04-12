/*
 * MekWars - Copyright (C) 2004
 *
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet)
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */

/*
 * Created on 24.04.2004
 */
package mekwars.common.campaign.pilot.skills;

import megamek.common.Entity;

import mekwars.common.campaign.pilot.Pilot;

/**
 * If a pilot has this skill, it levels as if it had one level less of the ability
 *
 * @author Helge Richter
 */
public class NaturalAptitudePilotingSkill extends PilotSkill {
    public NaturalAptitudePilotingSkill(int id) {
        super(id, "Natural Aptitude: Piloting", "NAP");
        setDescription(
                "The pilot checks leveling for piloting at one level higher then current i.e. 5"
                    + " instead of 4 for a 4/5 pilot");
    }

    public NaturalAptitudePilotingSkill() {
        // TODO: replace with ReflectionProvider
    }

    @Override
    public int getBVMod(Entity unit, Pilot pilot) {
        return 0;
    }
}
