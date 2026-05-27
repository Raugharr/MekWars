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

import mekwars.common.Unit;
import mekwars.common.campaign.pilot.Pilot;
import megamek.common.Entity;

/**
 * If a pilot has this skill they will have a +20% of returning home if ejected and left on the field.
 *
 * @author Helge Richter
 *
 */
public class SurvivalistSkill extends PilotSkill {
    public SurvivalistSkill(int id) {
        super(id, "Survivalist", "SV");
        setDescription("If a pilot has this skill they will have a +20% of returning home if ejected andleft on the field.");
    }

    public SurvivalistSkill() {
        // TODO: replace with ReflectionProvider
    }

    @Override
    public int getChance(int unitType, Pilot pilot) {
        if (unitType != Unit.MEK) {
            return 0;
        }

        return super.getChance(unitType, pilot);
    }

    @Override
    public int getBVMod(Entity unit, Pilot pilot) {
        return 0;
    }
}
