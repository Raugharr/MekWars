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

/*
 * Created on 14.04.2004
 *
 */
package mekwars.server.campaign.commands.admin;

import mekwars.common.UnitFactory;
import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.MWServ;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SPlanet;
import mekwars.server.campaign.SUnitFactory;
import mekwars.server.campaign.commands.Command;

import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;

/**
 * @author Helge Richter
 */
public class AdminCreateFactoryCommand implements Command {
    int accessLevel = IAuthenticator.ADMIN;
    String syntax = "planet name#factory name#size#faction#type#subfolder#SubfactionAccessLevel";

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
        SPlanet planet = CampaignMain.cm.getPlanetFromPartialString(command.nextToken(), username);
        String name = command.nextToken();
        String size = command.nextToken();
        String faction = command.nextToken();
        String buildTableFolder = "0";
        int accessLevel = 0;

        int type = Integer.parseInt(command.nextToken());

        if (command.hasMoreElements()) buildTableFolder = command.nextToken();
        if (command.hasMoreElements()) accessLevel = Integer.parseInt(command.nextToken());

        SUnitFactory factory =
                new SUnitFactory(
                        name, planet, size, faction, 0, 100, type, buildTableFolder, accessLevel);

        HibernateUtil.getInstance().inTransaction(session -> session.persist(factory));

        if (planet.getOwner() != null) {
            planet.getOwner().removePlanet(planet);
            planet.getOwner().addPlanet(planet);
        }
        planet.updated();

        CampaignMain.cm.toUser("Factory created!", username, true);

        CampaignMain.cm.doSendModMail(
                "NOTE",
                username
                        + " has created factory "
                        + factory.getName()
                        + " on planet "
                        + planet.getName());
    }
}
