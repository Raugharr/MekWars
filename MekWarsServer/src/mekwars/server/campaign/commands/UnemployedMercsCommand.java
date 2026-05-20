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

package mekwars.server.campaign.commands;

import mekwars.common.util.HibernateUtil;
import mekwars.server.MWServ;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SPlayer;
import mekwars.server.campaign.mercenaries.MercHouse;

import java.util.List;
import java.util.StringTokenizer;

public class UnemployedMercsCommand implements Command {
    int accessLevel = 0;
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

        HibernateUtil.inTransaction(
                session -> {
                    String s = "Unemployed Mercenaries: ";
                    // TODO: When we make House an @Entity, clean this up.
                    List<MercHouse> mh = CampaignMain.cm.getMercHouses();
                    for (int i = 0; i < mh.size(); i++) {
                        MercHouse searchHouse = mh.get(i);
                        List<SPlayer> playerList =
                                session.createQuery(
                                                "SELECT p FROM SPlayer p LEFT JOIN SHouse h ON h.id"
                                                        + " == p.house_id",
                                                SPlayer.class)
                                        .getResultList();

                        boolean foundMerc = false;
                        for (SPlayer player : playerList) {
                            if (player.getMyHouse().getHouseFightingFor(player).isMercHouse()) {
                                if (!foundMerc) {
                                    s += player.getName();
                                    foundMerc = true;
                                } else {
                                    s += ", " + player.getName();
                                }
                            }
                        }
                    }
                    CampaignMain.cm.toUser(s, username, true);
                });
    }
}
