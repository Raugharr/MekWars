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

package mekwars.server.campaign.commands.mod;

import mekwars.common.util.HibernateUtil;
import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.MWServ;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SPlayer;
import mekwars.server.campaign.commands.Command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class ListMultiPlayerGroupsCommand implements Command {
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

        HibernateUtil.inTransaction(
                session -> {
                    String toSend = "AM:List of Multiplayergroups:";

                    List<SPlayer> allPlayers =
                            session.createNamedQuery("Player.getAllLoggedIn", SPlayer.class)
                                    .getResultList();

                    HashMap<Integer, List<SPlayer>> result = new HashMap<Integer, List<SPlayer>>();
                    for (SPlayer player : allPlayers) {
                        if (player.getGroupAllowance() != 0) {
                            List<SPlayer> list;
                            if (result.get(player.getGroupAllowance()) == null) {
                                list = new ArrayList<SPlayer>();
                            } else {
                                list = result.get(player.getGroupAllowance());
                            }
                            list.add(player);
                            result.put(player.getGroupAllowance(), list);
                        }
                    }

                    for (Map.Entry<Integer, List<SPlayer>> entries : result.entrySet()) {
                        Integer GroupID = entries.getKey();
                        List<SPlayer> members = entries.getValue();

                        toSend += "<br>Group #" + GroupID + ":";
                        for (SPlayer player : members) {
                            toSend += player.getName() + " + ";
                        }
                        toSend = toSend.substring(0, toSend.lastIndexOf("+") - 1);
                    }
                    CampaignMain.cm.toUser(toSend, username, true);
                });
    }
}
