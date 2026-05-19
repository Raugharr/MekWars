/*
 * MekWars - Copyright (C) 2026
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

package mekwars.common.campaign.pilot;

import mekwars.common.Player;
import mekwars.common.Unit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Deque;

/**
 * Formatter for building HTML strings to display pilot queue information. Extracted to eliminate
 * duplication across command classes.
 */
public class PilotQueueFormatter {
    private static final Logger LOGGER = LogManager.getLogger(PilotQueueFormatter.class);

    /**
     * Builds an HTML string describing pilots in every weight class queue.
     *
     * @param player The player to build the pilot list for.
     * @return The string containing the contents of every weight class's current pilots for the
     *     player.
     */
    public static <T extends Unit> String renderAllPilotLists(Player<T> player) {
        StringBuilder pilotListBuilder = new StringBuilder();

        pilotListBuilder.append(renderPilotSection(player, "Mek Pilots", Unit.MEK));
        pilotListBuilder.append(renderPilotSection(player, "ProtoMek Pilots", Unit.PROTOMEK));
        pilotListBuilder.append(renderPilotSection(player, "Aero Pilots", Unit.AERO));
        return pilotListBuilder.toString();
    }

    public static <T extends Unit> String renderPilotSection(
            Player<T> player, String title, int sectionType) {
        // set up a string buffer to contain the return info
        StringBuilder builder = new StringBuilder();

        // process MEK pilots first
        for (int weightClass = Unit.LIGHT; weightClass <= Unit.ASSAULT; weightClass++) {
            Deque<Pilot> currentList =
                    player.getPersonalPilotQueue().getPilotQueue(sectionType, weightClass);

            builder.append(renderPilotList(currentList, weightClass));
        }

        if (builder.length() > 0) {
            builder.insert(0, "<u>" + title + "</u>:<br>");
        }
        return builder.toString();
    }

    /**
     * Builds an HTML string describing pilots in a weight class queue.
     *
     * @param currentList Queue of pilots to display
     * @param weightClass Weight class constant (e.g., Unit.LIGHT, Unit.ASSAULT)
     * @return true if any pilots were added, false otherwise
     */
    public static String renderPilotList(Collection<Pilot> currentList, int weightClass) {
        StringBuilder builder = new StringBuilder();

        if (currentList != null && !currentList.isEmpty()) {
            // add the weight class description to table
            builder.append(Unit.getWeightClassDesc(weightClass) + ":<UL>");

            // add all pilots in the list to the table
            for (Pilot currPilot : currentList) {
                if (currPilot == null) {
                    LOGGER.warn("Null pilot encountered in queue; possible data corruption");
                    continue;
                }

                builder.append(renderPilot(currPilot, weightClass));
            }

            // close the weight class table block
            builder.append("</UL>");
        }
        return builder.toString();
    }

    public static String renderPilot(Pilot pilot, int weightClass) {
        StringBuilder builder = new StringBuilder();

        builder.append(
                "<LI>#"
                        + pilot.getPilotId()
                        + " "
                        + pilot.getName()
                        + " ("
                        + pilot.getGunnery()
                        + "/"
                        + pilot.getPiloting());

        // append skill descriptions
        String skills = pilot.getSkillString(true);
        if (skills != null && !skills.trim().isEmpty()) {
            builder.append(", ");
            builder.append(skills);
        }

        // close the description block.
        builder.append(")");

        // show hits, if tracking and > 0
        if (pilot.getHits() > 0) {
            builder.append(" Hits: ").append(pilot.getHits());
        }
        builder.append("</LI>");
        return builder.toString();
    }

    private PilotQueueFormatter() {}
}
