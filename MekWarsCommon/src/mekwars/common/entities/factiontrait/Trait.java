/*
 * MekWars - Copyright (C) 2026
 *
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet) Original author Helge Richter (McWizard)
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */

package mekwars.common.entities.factiontrait;

import mekwars.common.campaign.pilot.skills.PilotSkill;

/** Represents a single {@link PilotSkill} and how likely it is to be picked */
public class Trait {
    private PilotSkill pilotSkill;
    private int modifier;

    public Trait(PilotSkill pilotSkill, int modifier) {
        this.pilotSkill = pilotSkill;
        this.modifier = modifier;
    }

    public PilotSkill getPilotSkill() {
        return pilotSkill;
    }

    public int getModifier() {
        return modifier;
    }

    public void serialize(StringBuilder builder) {
        builder.append(getPilotSkill().getId()).append("*");
        builder.append(getModifier()).append("*");
    }
}
