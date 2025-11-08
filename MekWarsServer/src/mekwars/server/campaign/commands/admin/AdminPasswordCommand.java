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

import java.util.StringTokenizer;

import mekwars.common.util.MWLogger;
import mekwars.server.MWServ;
import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SPlayer;
import mekwars.server.campaign.commands.Command;
import mekwars.server.util.MWPasswd;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AdminPasswordCommand implements Command {
    private static final Logger LOGGER = LogManager.getLogger(AdminPasswordCommand.class);

    // conforming methods
    int accessLevel = IAuthenticator.REGISTERED;

    public int getExecutionLevel() {
        return accessLevel;
    }

    public void setExecutionLevel(int i) {
        accessLevel = i;
    }

    String syntax = "Admin Password Commands:<br>" + " /c adminpassword#save - save the password file<br>" + " /c adminpassword#remove#NAME - remove NAME's password<br>" + " /c adminpassword#level#NAME#LEVEL - set a player's userlevel.<br>" + " EXAMPLE: /c adminpassword#remove#urgru<br>" + " EXAMPLE: /c adminpassword#level#urgru#200";

    public String getSyntax() {
        return syntax;
    }

    public void process(StringTokenizer command, String Username) {

        if (accessLevel != 0) {
            int userLevel = MWServ.getInstance().getUserLevel(Username);
            if (userLevel < getExecutionLevel()) {
                CampaignMain.cm.toUser("AM:Insufficient access level for command. Level: " + userLevel + ". Required: " + accessLevel + ".", Username, true);
                return;
            }
        }

        if (!command.hasMoreTokens()) {
            CampaignMain.cm.toUser("Admin Password Commands:<br>" + " /c adminpassword#save - save the password file<br>" + " /c adminpassword#remove#NAME - remove NAME's password<br>" + " /c adminpassword#level#NAME#LEVEL - set a player's userlevel.<br>" + " EXAMPLE: /c adminpassword#remove#urgru<br>" + " EXAMPLE: /c adminpassword#level#urgru#200", Username, true);
            return;
        }

        String action = command.nextToken();
        if (action.equalsIgnoreCase("save")) {
            try {
                MWPasswd.save();
                CampaignMain.cm.toUser("Password-file saved!", Username, true);
                CampaignMain.cm.doSendModMail("NOTE", Username + " has saved the password-file.");
            } catch (Exception ex) {
                CampaignMain.cm.toUser("Problems saving password file!", Username, true);
            }
        }

        else if (action.equalsIgnoreCase("remove")) {
            String target = command.nextToken();
            MWPasswd.removeRecord(target);
            try {
                MWPasswd.save();
            } catch (Exception ex) {
                LOGGER.error("Exception: ", ex);
            }
            CampaignMain.cm.toUser("Password for " + target + " removed!", Username, true);
            CampaignMain.cm.doSendModMail("NOTE", Username + " has removed " + target + "'s password");
        }

        else if (action.equalsIgnoreCase("level")) {

            String target = command.nextToken();
            int level = Integer.parseInt(command.nextToken());
            SPlayer p = CampaignMain.cm.getPlayer(target);

            try {
                MWPasswd.getRecord(target).setAccess(level);
            } catch (Exception ex) {
                CampaignMain.cm.toUser(target + " is not registered. Have them register, then try again.", Username);
                return;
            }

            try {
                MWServ.getInstance().getClient(target).setAccessLevel(level);
                MWServ.getInstance().getUser(target).setLevel(level);
                MWServ.getInstance().sendRemoveUserToAll(target, false);
                MWServ.getInstance().sendNewUserToAll(target, false);
                MWPasswd.writeRecord(p.getPassword(), target);

                if (p != null) {
                    CampaignMain.cm.doSendToAllOnlinePlayers("PI|DA|" + CampaignMain.cm.getPlayerUpdateString(p), false);
                }

            } catch (Exception ex) {
                LOGGER.error("Exception: ", ex);
            }

            CampaignMain.cm.toUser("Level for " + target + " set to " + level + "!", Username, true);
            CampaignMain.cm.doSendModMail("NOTE", Username + " has set " + target + "'s level to " + level);
        }
    }
}