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

import mekwars.common.House;
import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.MWServ;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SHouse;
import mekwars.server.campaign.SUnit;
import mekwars.server.campaign.commands.Command;

import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class AdminListAndRemoveOMGCommand implements Command {
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

        /*
         * We know that OMG's are always light meks, so we can loop
         * through every faction's light mek queue to remove them.
         */
        for (House house : CampaignMain.cm.getData().getAllHouses()) {
            SHouse faction = (SHouse) house;
            List<SUnit> badUnitList = faction.getHangar().stream()
                .filter(unit -> unit.getModelName().equals("OMG-UR-FD"))
                .collect(Collectors.toList());

            for (SUnit unit : badUnitList) {
                CampaignMain.cm.doSendModMail(
                        "NOTE",
                        username
                                + " removed an OMG from the "
                                + faction.getName()
                                + " bays. Should have been a "
                                + unit.getUnitFilename()
                                + ".");
                CampaignMain.cm.toUser(
                        "Removed an OMG from the "
                                + faction.getName()
                                + " bays. Should have been a "
                                + unit.getUnitFilename()
                                + ".",
                        username,
                        true);
                faction.removeUnit(unit, true);
            }
        }
    }
}
