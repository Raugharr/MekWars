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

import megamek.common.AmmoType;

import mekwars.common.entities.BannedAmmo;
import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.MWServ;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SHouse;
import mekwars.server.campaign.commands.Command;
import mekwars.server.io.FileSystem;

import java.util.StringTokenizer;

public class AdminSetHouseAmmoBanCommand implements Command {
    int accessLevel = IAuthenticator.ADMIN;
    String syntax = "Faction Name#Munition Number";

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
        String ammoName = "";
        try {
            faction = command.nextToken();
            ammoName = command.nextToken();
        } catch (Exception ex) {
            CampaignMain.cm.toUser(
                    "Invalid syntax. Try: adminsethouseammoban#faction#munitionnumber",
                    username,
                    true);
        }

        SHouse h = CampaignMain.cm.getHouseFromPartialString(faction, username);
        if (h != null) {
            AmmoType.Munitions munition =
                    BannedAmmo.getMunitionByNumber(Integer.parseInt(ammoName));
            BannedAmmo bannedAmmo = CampaignMain.cm.getData().getBannedAmmoStore().get(munition, h);

            if (bannedAmmo != null) {
                CampaignMain.cm.getData().getBannedAmmoStore().remove(munition, h);
                CampaignMain.cm.toUser(
                        "Ban on " + bannedAmmo.getName() + " lifted for " + h.getName() + ".",
                        username,
                        true);
                CampaignMain.cm.doSendModMail(
                        "NOTE",
                        username
                                + " lifted the ban on "
                                + bannedAmmo.getName()
                                + " for "
                                + h.getName()
                                + ".");
            } else {
                bannedAmmo = CampaignMain.cm.getData().getBannedAmmoStore().add(munition, h);
                CampaignMain.cm.toUser(
                        "Banned " + bannedAmmo.getName() + " for " + h.getName() + ".",
                        username,
                        true);
                CampaignMain.cm.doSendModMail(
                        "NOTE",
                        username + " banned " + bannedAmmo.getName() + " for " + h.getName() + ".");
            }

            h.updated();
            FileSystem.getInstance()
                    .getBanAmmoFile()
                    .save(System.currentTimeMillis(), CampaignMain.cm.getData());
        }
    }
}
