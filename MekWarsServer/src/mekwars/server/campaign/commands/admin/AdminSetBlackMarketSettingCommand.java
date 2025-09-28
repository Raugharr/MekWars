/*
 * MekWars - Copyright (C) 2007
 *
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
 * @author jtighe This command is used to set Black Market Settings for max/min cost and production.
 */
package mekwars.server.campaign.commands.admin;

import java.util.StringTokenizer;
import mekwars.common.Equipment;
import mekwars.common.util.MWLogger;
import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.MWServ;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.commands.Command;

public class AdminSetBlackMarketSettingCommand implements Command {

    int accessLevel = IAuthenticator.ADMIN;
    String syntax = "Item Name#Min Cost#Max Cost#Min Production#Max Production";

    public int getExecutionLevel() {
        return accessLevel;
    }

    public void setExecutionLevel(int i) {
        accessLevel = i;
    }

    public String getSyntax() {
        return syntax;
    }

    public void process(StringTokenizer command, String Username) {

        // access level check
        int userLevel = MWServ.getInstance().getUserLevel(Username);
        if (userLevel < getExecutionLevel()) {
            CampaignMain.cm.toUser(
                    "AM:Insufficient access level for command. Level: "
                            + userLevel
                            + ". Required: "
                            + accessLevel
                            + ".",
                    Username,
                    true);
            return;
        }

        // get config var and new setting
        String key = "";
        String minCost = "";
        String maxCost = "";
        String minProduction = "";
        String maxProduction = "";

        try {
            key = command.nextToken();
            minCost = command.nextToken();
            maxCost = command.nextToken();
            minProduction = command.nextToken();
            maxProduction = command.nextToken();

            Equipment bme = CampaignMain.cm.getBlackMarketEquipmentTable().get(key);

            if (bme == null) {
                bme = new Equipment();
                bme.setEquipmentInternalName(key);
            }

            bme.setMinCost(Double.parseDouble(minCost));
            bme.setMaxCost(Double.parseDouble(maxCost));
            bme.setMinProduction(Integer.parseInt(minProduction));
            bme.setMaxProduction(Integer.parseInt(maxProduction));

            CampaignMain.cm.getBlackMarketEquipmentTable().put(key, bme);

        } catch (Exception ex) {
            MWLogger.errLog(ex);
        }

        // NOTE:
        // NO MODMAIL for setting changes. Server Config GUI would spam too much.

    } // end process
}
