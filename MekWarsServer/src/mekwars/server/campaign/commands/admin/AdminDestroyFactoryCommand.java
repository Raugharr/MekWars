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

import mekwars.common.UnitFactory;
import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.MWServ;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SPlanet;
import mekwars.server.campaign.commands.Command;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.StringTokenizer;

public class AdminDestroyFactoryCommand implements Command {
    private static final Logger LOGGER = LogManager.getLogger(AdminDestroyFactoryCommand.class);

    int accessLevel = IAuthenticator.ADMIN, factoryID;

    public int getExecutionLevel() {
        return accessLevel;
    }

    public void setExecutionLevel(int i) {
        accessLevel = i;
    }

    String syntax = "Planet Name#Factory Name";

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

        try {
            SPlanet p = CampaignMain.cm.getPlanetFromPartialString(command.nextToken(), username);
            String factoryname = command.nextToken();
            if (p == null) {
                CampaignMain.cm.toUser("Planet not found:", username, true);
                return;
            }

            if (p.getUnitFactories().size() < 1) {
                CampaignMain.cm.toUser("This planet does not have any factories!", username, true);
                return;
            }

            UnitFactory foundFactory = null;
            for (UnitFactory UF : p.getUnitFactories()) {
                if (UF.getName().equalsIgnoreCase(factoryname)) {
                    foundFactory = UF;
                    break;
                }
            }

            if (foundFactory == null) {
                CampaignMain.cm.toUser("Factory " + factoryname + " not found", username, true);
                return;
            }

            p.getUnitFactories().remove(foundFactory);

            p.updated();
            CampaignMain.cm.toUser(
                    factoryname + " removed from " + p.getName() + ".", username, true);
            CampaignMain.cm.doSendModMail(
                    "NOTE", username + "  removed " + factoryname + " from " + p.getName() + ".");
        } catch (Exception ex) {
            LOGGER.error("Exception: ", ex);
        }
    }
}
