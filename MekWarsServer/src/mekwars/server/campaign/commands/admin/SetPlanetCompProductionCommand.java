/*
 * MekWars - Copyright (C) 2005
 *
 * Original author - Torren (torren@users.sourceforge.net)
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
 * @author Torren (Jason Tighe) Created on 11.08.2005 Allows SO's to set a planets component
 *     production base on the fly.
 */
package mekwars.server.campaign.commands.admin;

import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.MWServ;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SHouse;
import mekwars.server.campaign.SPlanet;
import mekwars.server.campaign.commands.Command;

import java.util.StringTokenizer;

public class SetPlanetCompProductionCommand implements Command {
    int accessLevel = IAuthenticator.ADMIN;
    String syntax = "Planet Name#Number Of Components";

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

        String PlanetName = command.nextToken();
        int compProduction = 0;
        SPlanet planet = CampaignMain.cm.getPlanetFromPartialString(PlanetName, username);

        if (planet == null) {
            CampaignMain.cm.toUser(PlanetName + " not found.", username, true);
            return;
        }

        try {
            if (command.hasMoreTokens()) compProduction = Integer.parseInt(command.nextToken());
        } catch (Exception ex) {
            CampaignMain.cm.toUser(
                    "Invalid Syntax: SetPlanetCompProduction#PlanetName#NumberOfComponents",
                    username,
                    true);
            return;
        }

        SHouse owner = (SHouse) planet.getOwner();

        if (owner != null) {
            owner.setComponentProduction(
                    owner.getComponentProduction() - planet.getCompProduction());
            owner.setComponentProduction(owner.getComponentProduction() + compProduction);
        }

        planet.setCompProduction(compProduction);

        CampaignMain.cm.toUser(
                planet.getName()
                        + " has had its component production set to "
                        + planet.getCompProduction(),
                username,
                true);
        CampaignMain.cm.doSendModMail(
                "NOTE",
                username
                        + " has set planet "
                        + PlanetName
                        + "'s component production to "
                        + planet.getCompProduction());
        planet.updated();
    }
}
