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
import java.util.stream.Collectors;

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
                h.clearHangar();
            // else select a unit type
            } else {
                strClass = command.nextToken();
                final int unitType = Integer.parseInt(strType);

                if (strClass.equalsIgnoreCase("ALL")) {
                    List<SUnit> hangar = h.getHangar(unitType);
                    for (SUnit unit : hangar) {
                        h.removeUnit(unit, false);
                    }
                } else {
                    final int unitClass = Integer.parseInt(strClass);
                    List<SUnit> hangar = h.getHangar()
                        .stream()
                            .filter(unit -> unit.getType() == unitType)
                            .filter(unit -> unit.getWeightClass() == unitClass)
                            .collect(Collectors.toList());
                    for (SUnit unit : hangar) {
                        h.removeUnit(unit, false);
                    }
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
