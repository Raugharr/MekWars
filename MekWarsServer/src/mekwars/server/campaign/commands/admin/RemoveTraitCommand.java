/*
 * MekWars - Copyright (C) 2005
 *
 * Original author - nmorris (urgru@users.sourceforge.net)
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 */

package mekwars.server.campaign.commands.admin;

import mekwars.common.CampaignData;
import mekwars.common.House;
import mekwars.common.io.file.FactionTraitFile;
import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.MWServ;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.commands.Command;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.StringTokenizer;

public class RemoveTraitCommand implements Command {
    private static final Logger LOGGER = LogManager.getLogger(RemoveTraitCommand.class);

    int accessLevel = IAuthenticator.ADMIN;
    String syntax = "Faction Name#Trait Name#CONFIRM";

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

        if (accessLevel != 0) {
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
        }

        // Syntax AddTrait Faction#TraitName#SkillList($)

        String factionName = "common";
        String traitName = "none";
        String confirmString = "";

        try {
            factionName = command.nextToken();
            traitName = command.nextToken();
            confirmString = command.nextToken();
        } catch (Exception ex) {
            LOGGER.error("Exception: ", ex);
        }

        if (!confirmString.equals("CONFIRM")) return;

        FactionTraitFile factionTraitFile = CampaignData.cd.getFactionTraitFileByHouse(factionName);

        if (factionTraitFile == null) {
            return;
        }

        if (factionTraitFile.removeFactionTrait(traitName)) {
            CampaignMain.cm.toUser("Trait " + traitName + " has been removed.", username, true);
            CampaignMain.cm.doSendModMail(
                    "NOTE", username + " has removed trait " + traitName + ".");
            factionTraitFile.save();
        }
    }
}
