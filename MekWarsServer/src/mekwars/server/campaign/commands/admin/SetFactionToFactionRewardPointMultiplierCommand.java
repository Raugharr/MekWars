/*
 * MekWars - Copyright (C) 2007
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
import mekwars.server.campaign.SHouse;
import mekwars.server.campaign.commands.Command;

import java.util.StringTokenizer;

public class SetFactionToFactionRewardPointMultiplierCommand implements Command {

    int accessLevel = IAuthenticator.ADMIN;
    String syntax = "Faction Name#Faction Name#Multipler";

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

        SHouse faction1 = null;
        SHouse faction2 = null;
        double multiplier = 0.0;
        try {
            faction1 = CampaignMain.cm.getHouseFromPartialString(command.nextToken(), username);
            faction2 = CampaignMain.cm.getHouseFromPartialString(command.nextToken(), username);
            multiplier = Double.parseDouble(command.nextToken());
        } catch (Exception e) {
            CampaignMain.cm.toUser(
                    "Improper command. Try: /c SetFactionToFactionRewardPointMultiplier#" + syntax,
                    username,
                    true);
            return;
        }

        if (faction1 == null || faction2 == null) {
            return;
        }

        String rewardMultiplier =
                faction1.getName() + "To" + faction2.getName() + "RewardPointMultiplier";

        CampaignData.cd
                .getCampaignOptions()
                .setProperty(rewardMultiplier, Double.toString(multiplier));

        CampaignMain.cm.toUser(
                "You set the "
                        + CampaignMain.cm.getCampaignOptions().getConfig("RPShortName")
                        + " multipler for "
                        + faction1.getName()
                        + " to "
                        + faction2.getName()
                        + " to "
                        + multiplier,
                username,
                true);
        CampaignMain.cm.doSendModMail(
                "NOTE",
                username
                        + " has set "
                        + CampaignMain.cm.getCampaignOptions().getConfig("RPShortName")
                        + " multipler for "
                        + faction1.getName()
                        + " to "
                        + faction2.getName()
                        + " to "
                        + multiplier);
    }
}
