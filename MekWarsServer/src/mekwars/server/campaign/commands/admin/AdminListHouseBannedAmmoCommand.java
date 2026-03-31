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

package mekwars.server.campaign.commands.admin;


import java.util.List;
import java.util.StringTokenizer;

import mekwars.common.entities.BannedAmmo;
import mekwars.server.MWServ;
import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SHouse;
import mekwars.server.campaign.commands.Command;

public class AdminListHouseBannedAmmoCommand implements Command {
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
        //access level check
        int userLevel = MWServ.getInstance().getUserLevel(username);
        if (userLevel < getExecutionLevel()) {
            CampaignMain.cm.toUser("AM:Insufficient access level for command. Level: " + userLevel + ". Required: " + accessLevel + ".", username, true);
            return;
        }

        String faction = null;
        if (command.hasMoreTokens()) {
            faction = command.nextToken();
        } else {
            CampaignMain.cm.toUser("Unkown House. Syntax: /c AdminListHouseBannedAmmo#HouseName", username, true);
            return;
        }

        SHouse house = CampaignMain.cm.getHouseFromPartialString(faction, username);

        List<BannedAmmo> houseBannedAmmo = CampaignMain.cm.getData().getBannedAmmoStore().getByHouse(house);

        if (houseBannedAmmo.isEmpty()) {
            CampaignMain.cm.toUser(faction + " is not currently banning any ammo.", username, true);
        } else {
            houseBannedAmmo.forEach(each -> CampaignMain.cm.toUser(each.getName(), username, true));
        }
    } //end process
}
