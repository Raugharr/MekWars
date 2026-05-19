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

import mekwars.common.Unit;
import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.MWServ;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SHouse;
import mekwars.server.campaign.SUnit;
import mekwars.server.campaign.commands.Command;

import java.util.StringTokenizer;
import java.util.List;

public class AdminPurgeHouseBaysCommand implements Command {

    int accessLevel = IAuthenticator.ADMIN;
    String syntax = "Faction Name#[ALL]unittype#[ALL]unitsize";

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
        String strType = "";
        String strClass = "";
        int unitType = Unit.MEK;
        int unitClass = Unit.LIGHT;

        try {
            faction = command.nextToken();
            strType = command.nextToken();
        } catch (Exception ex) {
            CampaignMain.cm.toUser(
                    "Invalid syntax. Try: AdminPurgeHouseBays#faction#[ALL]unittype#[ALL]unitsize",
                    username,
                    true);
            return;
        }

        SHouse h = CampaignMain.cm.getHouseFromPartialString(faction, username);

        if (h == null) {
            return;
        }

        try {
            if (strType.equalsIgnoreCase("ALL")) {
                for (List<List<SUnit>> hangers : h.getHangar().values()) {
                    for (int size = Unit.LIGHT; size <= Unit.ASSAULT; size++) {
                        hangers.get(size).clear();
                    }
                }
            } // else select a unit type
            else {
                strClass = command.nextToken();
                unitType = Integer.parseInt(strType);
                List<List<SUnit>> hanger = h.getHangar(unitType);

                if (strClass.equalsIgnoreCase("ALL")) {
                    for (int size = Unit.LIGHT; size <= Unit.ASSAULT; size++) {
                        hanger.get(size).clear();
                    }
                } // else one unit size
                else {
                    unitClass = Integer.parseInt(strClass);
                    hanger.get(unitClass).clear();
                }
            }
        } catch (Exception ex) {
            CampaignMain.cm.toUser(
                    "Invalid syntax. Try: AdminPurgeHouseBays#faction#[ALL]unittype#[ALL]unitsize",
                    username,
                    true);
            return;
        }

        h.updated();
        CampaignMain.cm.doSendModMail("NOTE", username + " has purged bays for " + h.getName());
    }
} // end AdminPurgeHouseBaysCommand
