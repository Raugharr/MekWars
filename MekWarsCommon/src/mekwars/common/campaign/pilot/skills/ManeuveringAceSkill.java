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

import megamek.common.Entity;

import mekwars.common.CampaignData;
import mekwars.common.House;
import mekwars.common.MegaMekPilotOption;
import mekwars.common.campaign.pilot.Pilot;

/**
 * Maneuvering like a quad.
 *
 * @author Helge Richter and Immanuel Scholz
 */
public class ManeuveringAceSkill extends PilotSkill {
    public ManeuveringAceSkill(int id) {
        super(id, "Maneuvering Ace", "MA");
        setDescription(
                "Enables the unit to move laterally like a Quad. Units also receive a -1 BTH to"
                        + " rolls against skidding.");
    }

    public ManeuveringAceSkill() {
        // TODO: replace with ReflectionProvider
    }

    @Override
    public void modifyPilot(Pilot pilot) {
        pilot.addMegamekOption(new MegaMekPilotOption("maneuvering_ace", true));
        // pilot.setBvMod(pilot.getBVMod() + 0.01);
    }

    @Override
    public int getBVMod(Entity unit, Pilot pilot) {
        double topSpeed = unit.getRunMP();
        House house = pilot.getHouse();
        double baseBVMod = house.getHouseOptions().getDoubleConfig("ManeuveringAceBaseBVMod");
        double speedRating = house.getHouseOptions().getDoubleConfig("ManeuveringAceSpeedRating");
        double total = topSpeed / speedRating;
        total *= baseBVMod;
        return (int) total;
    }
}
