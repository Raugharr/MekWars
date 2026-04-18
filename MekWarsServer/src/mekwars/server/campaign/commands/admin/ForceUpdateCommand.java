/*
 * MekWars - Copyright (C) 2006
 *
 * Original author - Jason Tighe (torren@users.sourceforge.net)
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 */

package mekwars.server.campaign.commands.admin;


import mekwars.common.CampaignData;
import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.MWServ;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.commands.Command;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Allows SO's to force clients to update without a major version change
 *
 * <p>Syntax /c forceupdate#Key#[Player/Dedicated/All] <code>Player/Dedicated/All</code> are
 * optional and will kick those entities off so that they have to update right away.
 */
public class ForceUpdateCommand implements Command {
    private static final Logger LOGGER = LogManager.getLogger(ForceUpdateCommand.class);

    int accessLevel = IAuthenticator.MODERATOR;

    public int getExecutionLevel() {
        return accessLevel;
    }

    public void setExecutionLevel(int i) {
        accessLevel = i;
    }

    String syntax = "Update Key#[Player/Dedicated/All]";

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

        String updateKey = "";
        String whoToKick = "";

        try {
            updateKey = command.nextToken();
        } catch (Exception ex) {
            CampaignMain.cm.toUser(
                    "You must supply a Key<br>"
                            + "Syntax  /c forceupdate#Key[Clear]#[Player/Dedicated/All]<br>"
                            + "Player/Dedicated/All are optional and will kick those entities<br>"
                            + "off so that they have to update right away.",
                    username);
            return;
        }

        if (updateKey.equalsIgnoreCase("Clear") || updateKey.equalsIgnoreCase("-1")) updateKey = "";

        CampaignMain.cm.getCampaignOptions().setProperty("ForceUpdateKey", updateKey);
        CampaignData.cd.getCampaignOptions().getDefaultOptions().createConfig(MWServ.getInstance().getConfigParam("CAMPAIGNCONFIG"));

        CampaignMain.cm.doSendModMail("NOTE", username + " set the Force Update Key");
        CampaignMain.cm.toUser(
                "Make sure to add UPDATEKEY=" + updateKey + "<br>To the serverdata.dat", username);
        if (command.hasMoreTokens()) {
            whoToKick = command.nextToken();
            CampaignMain.cm.doSendModMail("NOTE", username + " is kicking " + whoToKick);
            boolean players = false;
            boolean deds = false;

            if (whoToKick.equalsIgnoreCase("all")) {
                players = true;
                deds = true;
            } else if (whoToKick.toLowerCase().startsWith("player")) players = true;
            else deds = true;

            ConcurrentLinkedQueue<String> users =
                    new ConcurrentLinkedQueue<String>(MWServ.getInstance().getUsers().keySet());
            for (String toKick : users) {
                if (MWServ.getInstance().isAdmin(toKick)) continue;
                if (players && !toKick.toLowerCase().startsWith("[dedicated]")) {
                    CampaignMain.cm.toUser(
                            "You have been forced to update by " + username + "!", toKick);
                    CampaignMain.cm.toUser("PL|FCU|Bye Bye", toKick, false);
                } else if (deds && toKick.toLowerCase().startsWith("[dedicated]")) {
                    try {
                        MWServ.getInstance().doStoreMail(toKick + ",update", username);
                        Thread.sleep(120);
                    } catch (Exception ex) {
                        LOGGER.error("Exception: ", ex);
                    }
                }
            } // end for
        } // end hasMore Commands
    }
}
