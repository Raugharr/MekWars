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

import mekwars.common.Influences;
import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.MWServ;
import mekwars.common.util.HibernateUtil;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.NewbieHouse;
import mekwars.server.campaign.SHouse;
import mekwars.server.campaign.SPlanet;
import mekwars.server.campaign.commands.Command;

import java.util.HashMap;
import java.util.StringTokenizer;

public class AdminCreateSolarisCommand implements Command {

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

        // Add the Newbie-SHouse
        SHouse solaris =
            new NewbieHouse(
                    CampaignMain.cm.getConfig("NewbieHouseName"), "#33CCCC", 4, 5, "SOL");

        CampaignMain.cm.addHouse(solaris);
        HashMap<Integer, Integer> solFlu = new HashMap<Integer, Integer>();
        solFlu.put(
                CampaignMain.cm
                .getHouseFromPartialString(
                    CampaignMain.cm.getConfig("NewbieHouseName"), null)
                .getId(),
                100);
        HibernateUtil.inTransaction(session -> {
            SPlanet newbieP = new SPlanet("Solaris VII", new Influences(solFlu), 0, -3, -2);
            CampaignMain.cm.addPlanet(session, newbieP);

            solaris.addPlanet(newbieP);
            CampaignMain.cm.toUser(CampaignMain.cm.getConfig("NewbieHouseName"), username, true);
        });
        CampaignMain.cm.doSendModMail(
                "NOTE", username + " has created " + CampaignMain.cm.getConfig("NewbieHouseName"));
    }
}
