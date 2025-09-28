/*
 * MekWars - Copyright (C) 2008
 *
 * Original author - jtighe (torren@users.sourceforge.net)
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 */

package mekwars.server.campaign.commands.leader;

import java.util.StringTokenizer;
import mekwars.server.MWServ;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SHouse;
import mekwars.server.campaign.SPlayer;
import mekwars.server.campaign.commands.Command;

public class ViewFactionPartsCacheCommand implements Command {

    int accessLevel = CampaignMain.cm.getIntegerConfig("factionLeaderLevel");

    public int getExecutionLevel() {
        return accessLevel;
    }

    public void setExecutionLevel(int i) {
        accessLevel = i;
    }

    String syntax = "";

    public String getSyntax() {
        return syntax;
    }

    public void process(StringTokenizer command, String Username) {

        if (accessLevel != 0) {
            int userLevel = MWServ.getInstance().getUserLevel(Username);
            if (userLevel < getExecutionLevel()) {
                CampaignMain.cm.toUser(
                        "AM:Insufficient access level for command. Level: "
                                + userLevel
                                + ". Required: "
                                + accessLevel
                                + ".",
                        Username,
                        true);
                return;
            }
        }

        int year = CampaignMain.cm.getIntegerConfig("CampaignYear");

        SPlayer player = CampaignMain.cm.getPlayer(Username);
        SHouse house = player.getMyHouse();

        if (command.hasMoreElements() && MWServ.getInstance().isModerator(Username))
            house = CampaignMain.cm.getHouseFromPartialString(command.nextToken(), Username);

        if (house == null) return;

        String results = "SM|" + house.getUnitParts().tableizeComponents(year);

        CampaignMain.cm.toUser(results, Username, false);
    }
} // end RequestSubFactionPromotionCommand class
