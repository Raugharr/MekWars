/*
 * MekWars - Copyright (C) 2007
 *
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

/**
 * @author jtighe
 *     <p>Command Saves the server config to its defined file
 */
package mekwars.server.campaign.commands.admin;

import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.MWServ;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SHouse;
import mekwars.server.campaign.commands.Command;

import java.util.StringTokenizer;

public class AdminSaveFactionConfigsCommand implements Command {

    int accessLevel = IAuthenticator.ADMIN;
    String syntax = "Faction Name";

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

        String faction = "";

        try {
            faction = command.nextToken();
        } catch (Exception ex) {
            CampaignMain.cm.toUser(
                    "Invalid syntax. Try: AdminSaveFactionConfigs#faction", username, true);
            return;
        }

        SHouse h = CampaignMain.cm.getHouseFromPartialString(faction, username);

        if (h == null) return;

        h.getHouseOptions().save();
        h.setUsedMekBayMultiplier(h.getHouseOptions().getFloatConfig("UsedPurchaseCostMulti"));
        CampaignMain.cm.toUser("AM:Status saved!", username, true);
        CampaignMain.cm.doSendModMail("NOTE", username + " has saved " + faction + "'s configs");
    } // end process
}
