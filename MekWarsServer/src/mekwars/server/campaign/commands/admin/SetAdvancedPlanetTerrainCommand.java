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

// import java.util.Hashtable;
import mekwars.common.AdvancedTerrain;
import mekwars.common.Continent;
// import mekwars.common.Terrain;
import mekwars.common.PlanetEnvironments;
// import mekwars.common.PlanetEnvironment;
import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.MWServ;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SPlanet;
import mekwars.server.campaign.commands.Command;

import java.util.StringTokenizer;

public class SetAdvancedPlanetTerrainCommand implements Command {
    int accessLevel = IAuthenticator.ADMIN;
    String syntax = "Planet Name$Terrain ID$AdvTerrain ID";

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

        SPlanet planet = (SPlanet) CampaignMain.cm.getData().getPlanetByName(command.nextToken());
        if (planet == null) {
            CampaignMain.cm.toUser("Unknown Planet", username, true);
            return;
        }

        int id = Integer.parseInt(command.nextToken());
        int aid = Integer.parseInt(command.nextToken());
        //		int conid = 0;

        AdvancedTerrain AT = CampaignMain.cm.getData().getAdvancedTerrain(aid);
        PlanetEnvironments originalPe = planet.getEnvironments();
        PlanetEnvironments changedPe = new PlanetEnvironments();

        for (Continent continent : originalPe.getContinents()) {
            if (continent.getEnvironment().getId() == id) {
                changedPe.add(new Continent(continent.getSize(), continent.getEnvironment(), AT));
            } else {
                changedPe.add(continent);
            }
        }

        planet.setEnvironments(changedPe);
        planet.updated();

        CampaignMain.cm.toUser(
                "Advanced Terrain set for terrain: "
                        + CampaignMain.cm.getData().getTerrain(id).getName()
                        + "("
                        + advancedTerrain.getName()
                        + ") on planet "
                        + planet.getName(),
                username,
                true);
        CampaignMain.cm.doSendModMail(
                "NOTE",
                username
                        + " has set Advanced Terrain for terrain: "
                        + CampaignMain.cm.getData().getTerrain(id).getName()
                        + "("
                        + advancedTerrain.getName()
                        + ") on planet "
                        + planet.getName());
    }
}
