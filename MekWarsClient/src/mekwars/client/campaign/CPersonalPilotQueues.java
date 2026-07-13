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
 * Created:       03/25/05
 * Last refactor: 01/12/06
 */
package mekwars.client.campaign;

import mekwars.client.campaign.pilot.CPilot;

import mekwars.common.CampaignData;
import mekwars.common.House;
import mekwars.common.Unit;
import mekwars.common.campaign.PersonalPilotQueues;
import mekwars.common.campaign.pilot.Pilot;
import mekwars.common.campaign.pilot.skills.PilotSkill;
import mekwars.common.util.TokenReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.StringTokenizer;

/**
 * @author Torren (Jason Tighe)
 *     <p>Client-side holder of Personal Pilot Queue information. The queue is a collection of
 *     pilots, managed by a player, which may be moved between eligible units (restricted by type
 *     and weightclass). This client-side representation is necessary in order to draw menus and
 *     controls in the CHQPanel.
 */
public class CPersonalPilotQueues extends PersonalPilotQueues {
    private static final Logger LOGGER = LogManager.getLogger(CPersonalPilotQueues.class);

    /**
     * Private method which reads SPilot data from a PPQ string. Eliminates dulpicative code in
     * formString's multiple loops thorugh the full data.
     */
    private Pilot getPilotFromString(String pilotData) {
        StringTokenizer subTokenizer = new StringTokenizer(pilotData, "#");
        String pilotname = TokenReader.readString(subTokenizer);
        int exp = TokenReader.readInt(subTokenizer);
        int gunnery = TokenReader.readInt(subTokenizer);
        int piloting = TokenReader.readInt(subTokenizer); // will always be 5
        String houseName = TokenReader.readString(subTokenizer);

        House pilotHouse = CampaignData.cd.getHouseByName(houseName);
        // set up the pilot
        Pilot pilot = new CPilot(pilotHouse, pilotname, gunnery, piloting);
        pilot.setExperience(exp);

        // read skills, if any
        int skillAmount = TokenReader.readInt(subTokenizer);
        for (int i = 0; i < skillAmount; i++) {
            PilotSkill skill =
                    new PilotSkill(
                            TokenReader.readInt(subTokenizer),
                            TokenReader.readString(subTokenizer),
                            TokenReader.readInt(subTokenizer),
                            TokenReader.readString(subTokenizer));

            if (skill.getName().equals("Weapon Specialist")) // WS skill has an
                // extra var
                pilot.setWeapon(TokenReader.readString(subTokenizer));

            if (skill.getName().equals("Trait")) { // Trait skill has an extra var
                pilot.setHouse(
                        CampaignData.cd.getHouseByName(TokenReader.readString(subTokenizer)));
            }

            if (skill.getName().equals("Edge")) {
                pilot.setTac(TokenReader.readBoolean(subTokenizer));
                pilot.setKO(TokenReader.readBoolean(subTokenizer));
                pilot.setHeadHit(TokenReader.readBoolean(subTokenizer));
                pilot.setExplosion(TokenReader.readBoolean(subTokenizer));
            }

            pilot.getSkills().add(skill);
        }

        // read the kills, if any

        pilot.setKills(TokenReader.readInt(subTokenizer));

        // all done. whoopdie doo.
        return pilot;
    }

    /**
     * Method to add a pilot to the client side queue. This discrete update saves bandwidth by
     * allowing a single pilot (instead of the whole queue, as was done in the past) to be sent down
     * when a game ends w/ a dispossessed pilot, a new pilot is hired, etc.
     *
     * <p>Format: PL|AP2PPQ|Unit Type|Unit Weight Class|Pilot Data
     */
    public void addPilot(StringTokenizer ST) {
        try {
            int pilotType = TokenReader.readInt(ST);
            int pilotClass = TokenReader.readInt(ST);
            Pilot pilot = getPilotFromString(TokenReader.readString(ST));

            this.getUnitTypeQueue(pilotType).get(pilotClass).add(pilot);
        } catch (Exception ex) {
            LOGGER.error("Error while adding pilot to PPQ", ex);
        }
    }

    /**
     * Method that removes a specific pilot from the PPQ. This discrete update saves bandwidth by
     * eliminating the need to send the entire hangar to the player when a pilot is removed.
     *
     * <p>Format: PL|RPPPQ|Unit Type|Unit Weight|Position
     */
    public void removePilot(StringTokenizer ST) {
        try {
            int pilotType = TokenReader.readInt(ST);
            int pilotClass = TokenReader.readInt(ST);
            int pilotPosition = TokenReader.readInt(ST);

            this.getUnitTypeQueue(pilotType).get(pilotClass).remove(pilotPosition);
        } catch (Exception ex) {
            LOGGER.error("Unable to remove pilot form queue", ex);
        }
    }

    /**
     * Convert a server-generated String into usedful data - actual pilots, in proper type and
     * class-based Queues.
     *
     * <p>NOTE: String send by the server is generated in SPPQueues.java, and delimited with $'s
     * (main) and #'s (subtokens).
     */
    public void fromString(String stringFromServer) {
        StringTokenizer mainTokenizer = new StringTokenizer(stringFromServer, "$");

        flushQueue();
        // loop once to read in meks (light -> assault lists)
        for (int weightClass = Unit.LIGHT; weightClass <= Unit.ASSAULT; weightClass++) {
            int listSize = TokenReader.readInt(mainTokenizer);

            for (int count = 0; count < listSize; count++) {
                Pilot toAdd = this.getPilotFromString(TokenReader.readString(mainTokenizer));
                this.getUnitTypeQueue(Unit.MEK).get(weightClass).add(toAdd);
            }
        }

        // loop a second time to read in protomeks (light -> assault lists)
        for (int weightClass = Unit.LIGHT; weightClass <= Unit.ASSAULT; weightClass++) {
            int listSize = TokenReader.readInt(mainTokenizer);

            for (int count = 0; count < listSize; count++) {
                Pilot toAdd = this.getPilotFromString(TokenReader.readString(mainTokenizer));
                this.getUnitTypeQueue(Unit.PROTOMEK).get(weightClass).add(toAdd);
            }
        }

        // loop a third time to read in Aeros (light -> assault lists)
        for (int weightClass = Unit.LIGHT; weightClass <= Unit.ASSAULT; weightClass++) {
            int listSize = TokenReader.readInt(mainTokenizer);

            for (int count = 0; count < listSize; count++) {
                Pilot toAdd = this.getPilotFromString(TokenReader.readString(mainTokenizer));
                this.getUnitTypeQueue(Unit.AERO).get(weightClass).add(toAdd);
            }
        }
    }
} // end CPPQ
