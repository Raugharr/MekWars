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

package mekwars.server.campaign.commands.mod;

import  mekwars.common.campaign.pilot.PilotQueueFormatter;
import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.MWServ;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SPlayer;
import mekwars.server.campaign.commands.Command;

import java.util.StringTokenizer;

/**
 * Return a human readable string that describes the pilots currently in a player's personal queues.
 */
public class ViewPlayerPersonalPilotQueueCommand implements Command {
    int accessLevel = IAuthenticator.MODERATOR;
    String syntax = "Player name";

    public int getExecutionLevel() {
        return accessLevel;
    }

    public void setExecutionLevel(int i) {
        accessLevel = i;
    }

    public String getSyntax() {
        return syntax;
    }

    public void process(StringTokenizer command, String username) {

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
                return;
            }
        }

        // get the player you wish to use

        if (!command.hasMoreTokens()) {
            CampaignMain.cm.toUser("Syntax: ViewPlayerPersonalPilotQueue#Name#", username);
            return;
        }
        SPlayer player = CampaignMain.cm.getPlayer(command.nextToken());

        String pilotList = PilotQueueFormatter.renderAllPilotLists(player);

        if (!pilotList.isEmpty()) {
            CampaignMain.cm.toUser("SM|" + pilotList, username, false);
        } else {
            CampaignMain.cm.toUser(
                    "SM|" + player.getName() + " doesn't have any reserve pilots at the moment.",
                    username,
                    false);
        }
        CampaignMain.cm.doSendModMail(
                "NOTE", username + " has viewed " + player.getName() + "'s pilot queue");
    }
}
