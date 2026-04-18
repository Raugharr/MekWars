/*
 * MekWars - Copyright (C) 2004
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

package mekwars.server.campaign.commands.admin;

import mekwars.common.CampaignData;
import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.MWServ;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.commands.Command;

import java.util.StringTokenizer;

public class AdminUnlockCampaignCommand implements Command {

    int accessLevel = IAuthenticator.ADMIN;
    String syntax = "";

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

        // access level check
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

        if (CampaignData.cd.getCampaignOptions().getBooleanConfig("CampaignLock") != true) {
            CampaignMain.cm.toUser("AM:Campaign is already unlocked.", username, true);
            return;
        }

        // reset the lock property so players can activate
        CampaignData.cd.getCampaignOptions().setProperty("CampaignLock", "false");

        // tell the admin he has unlocked the campaign
        CampaignMain.cm.doSendToAllOnlinePlayers(
                "AM:" + username + " unlocked the campaign!", true);
        CampaignMain.cm.toUser(
                "AM:You unlocked the campaign. Players may now activate.", username, true);
        CampaignMain.cm.doSendModMail("NOTE", username + " unlocked the campaign");
    } // end Process()
}
