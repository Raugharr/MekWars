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

import java.util.StringTokenizer;

import megamek.common.AmmoType;
import mekwars.common.entities.BannedAmmo;
import mekwars.server.MWServ;
import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.commands.Command;
import mekwars.server.io.FileSystem;

public class AdminSetServerAmmoBanCommand implements Command {
    int accessLevel = IAuthenticator.ADMIN;
    String syntax = "Munition Number";

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

        String ammoName = "";
        try {
            ammoName = command.nextToken();
        } catch (Exception ex) {
            CampaignMain.cm.toUser("Invalid syntax. Try: adminsetserveradmmoban#munitionnumber", username, true);
        }

        AmmoType.Munitions munition = BannedAmmo.getMunitionByNumber(Integer.parseInt(ammoName));
        BannedAmmo bannedAmmo = CampaignMain.cm.getData().getBannedAmmoStore().get(munition, null);

        if (bannedAmmo != null) {
            CampaignMain.cm.getData().getBannedAmmoStore().remove(munition, null);
            CampaignMain.cm.toUser("Server-wide ban on " + bannedAmmo.getName() + " lifted.", username, true);
            CampaignMain.cm.doSendModMail("NOTE", username + " lifted the server-wide ban on " + bannedAmmo.getName() + ".");
        } else {
            bannedAmmo = CampaignMain.cm.getData().getBannedAmmoStore().add(munition, null);
            CampaignMain.cm.toUser(bannedAmmo.getName() + " banned server-wide.", username, true);
            CampaignMain.cm.doSendModMail("NOTE", username + " banned " + bannedAmmo.getName() + " server-wide.");
        }
        FileSystem.getInstance().getBanAmmoFile().save(
            System.currentTimeMillis(),
            CampaignMain.cm.getData()
        );
    }
}
