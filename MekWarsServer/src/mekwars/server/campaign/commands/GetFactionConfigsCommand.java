/*
 * MekWars - Copyright (C) 2007
 *
 * Original author - Torren (torren@users.sourceforge.net)
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
 * Created on 02.25.2007
 *
 */
package mekwars.server.campaign.commands;

import mekwars.common.util.TokenReader;
import mekwars.server.MWServ;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SHouse;
import mekwars.server.campaign.SPlayer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Enumeration;
import java.util.StringTokenizer;

/**
 * @author Torren (Jason Tighe) Send a factions config to the player. Optional Faction Name variable
 *     for Staff to pull different factions configs.
 */
public class GetFactionConfigsCommand implements Command {
    private static final Logger LOGGER = LogManager.getLogger(GetFactionConfigsCommand.class);

    int accessLevel = 0;
    String syntax = "";

    public String getSyntax() {
        return syntax;
    }

    public int getExecutionLevel() {
        return 0;
    }

    public void setExecutionLevel(int i) {}

    public void process(StringTokenizer command, String username) {
        try {
            if (accessLevel != 0) {
                int userLevel = MWServ.getInstance().getUserLevel(username);
                if (userLevel < getExecutionLevel()) {
                    CampaignMain.cm.toUser(
                            "AM:Insufficient access level for command. Level: "
                                    + userLevel
                                    + ". Required: "
                                    + accessLevel
                                    + ".",
                            username,
                            true);
                    CampaignMain.cm.toUser("PL|FC|DONE#DONE", username, false);
                    return;
                }
            }

            SPlayer player = CampaignMain.cm.getPlayer(username);
            String factionName = player.getMyHouse().getName();
            SHouse faction = null;
            long timeStamp = -1;

            try {
                timeStamp = TokenReader.readLong(command);
            } catch (Exception ex) {
                CampaignMain.cm.toUser("PL|FC|DONE#DONE", username, false);
                return;
            }

            if (command.hasMoreElements()) {
                factionName = TokenReader.readString(command);
            }

            faction = CampaignMain.cm.getHouseFromPartialString(factionName);

            if (faction == null || faction.getHouseOptions() == null) {
                CampaignMain.cm.toUser("PL|FC|DONE#DONE", username, false);
                return;
            }

            /*
             * No reason to update. you're up to date. Calling this command from
             * AdminMenu causes it to fully update each time.
             */
            if (timeStamp >= faction.getLongConfig("TIMESTAMP")) {
                CampaignMain.cm.toUser("PL|FC|DONE#DONE", username, false);
                return;
            }

            StringBuffer result = new StringBuffer("PL|FC|");
            String delimiter = "#";

            Enumeration<?> keys = faction.getHouseOptions().propertyNames();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                String value = faction.getConfig(key);
                result.append(key);
                result.append(delimiter);
                result.append(value);
                result.append(delimiter);
            } // End While

            result.append("DONE#DONE");
            CampaignMain.cm.toUser(result.toString(), username, false);
        } catch (Exception ex) {
            CampaignMain.cm.toUser("PL|FC|DONE#DONE", username, false);
            LOGGER.error("Exception: ", ex);
        }
    }
}
