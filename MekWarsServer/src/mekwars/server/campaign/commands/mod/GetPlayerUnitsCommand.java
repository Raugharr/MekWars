/*
 * MekWars - Copyright (C) 2004
 *
 * Original Author - Nathan Morris (urgru@users.sourceforge.net)
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

package mekwars.server.campaign.commands.mod;

import java.util.StringTokenizer;
import megamek.common.Infantry;
import mekwars.common.Unit;
import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.MWServ;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SPlayer;
import mekwars.server.campaign.SUnit;
import mekwars.server.campaign.commands.Command;

public class GetPlayerUnitsCommand implements Command {

    int accessLevel = IAuthenticator.MODERATOR;
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

    public void process(StringTokenizer command, String Username) {

        /*
         * NOTE: This command is unlogged, as its a GUI precursor to
         * other commands (admintransfer, etc) which players should
         * have access to. A moderators who does not have access to the
         * adminplayersstatus command could conceivably use this to
         * spoof a view of another player's hangar, but if we're letting
         * shady people be mods ....
         */

        try {
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
            String username = command.nextToken();
            String commandName = command.nextToken();
            String extraCommands = null;

            if (command.hasMoreTokens()) extraCommands = command.nextToken();

            Command commandMethod =
                    CampaignMain.cm.getServerCommands().get(commandName.toUpperCase());

            if (commandMethod == null) {
                CampaignMain.cm.toUser("AM:Unknown command " + commandName + ".", Username, true);
                return;
            }

            if (commandMethod.getExecutionLevel() > userLevel) {
                CampaignMain.cm.toUser(
                        "AM:Insufficient access level for command. Level: "
                                + userLevel
                                + ". Required: "
                                + commandMethod.getExecutionLevel()
                                + ".",
                        Username,
                        true);
                return;
            }

            SPlayer target = CampaignMain.cm.getPlayer(username);

            if (target == null) {
                CampaignMain.cm.toUser("AM:Unknown user " + username + ".", Username, true);
                return;
            }

            String result = commandName + "|" + username + "|";

            for (SUnit unit : target.getUnits()) {
                if (unit.getType() == Unit.MEK
                        || unit.getType() == Unit.VEHICLE
                        || unit.getType() == Unit.AERO)
                    result +=
                            unit.getId()
                                    + " "
                                    + unit.getModelName()
                                    + " ("
                                    + unit.getPilot().getGunnery()
                                    + "/"
                                    + unit.getPilot().getPiloting()
                                    + ")#";
                else if (unit.getType() == Unit.INFANTRY || unit.getType() == Unit.BATTLEARMOR) {
                    if (((Infantry) unit.getEntity()).canMakeAntiMekAttacks())
                        result +=
                                unit.getId()
                                        + " "
                                        + unit.getModelName()
                                        + " ("
                                        + unit.getPilot().getGunnery()
                                        + "/"
                                        + unit.getPilot().getPiloting()
                                        + ")#";
                    else
                        result +=
                                unit.getId()
                                        + " "
                                        + unit.getModelName()
                                        + " ("
                                        + unit.getPilot().getGunnery()
                                        + ")#";
                } else
                    result +=
                            unit.getId()
                                    + " "
                                    + unit.getModelName()
                                    + " ("
                                    + unit.getPilot().getGunnery()
                                    + ")#";
            }

            if (extraCommands != null) result += "|" + extraCommands;

            CampaignMain.cm.toUser("LPU|" + result, Username, false);
        } catch (Exception ex) {

        }
    } // end process()
} // end GetPlayerUnitsCommand
