/*
 * MekWars - Copyright (C) 2004
 *
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet)
 * Original Author - Nathan Morris (urgru@verizon.net)
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
import mekwars.server.campaign.SHouse;
import mekwars.server.campaign.SPlayer;

import java.util.List;
import java.util.StringTokenizer;

public class HouseContractsCommand implements Command {
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
                    StringBuilder toSend = new StringBuilder();
                    int count = 0;
                    SPlayer player = CampaignMain.cm.getPlayer(username);

                    // if a merc, show everyone's employers
                    if (player.getMyHouse().isMercHouse()) {
                        List<SPlayer> players =
                                session.createQuery(
                                                "SELECT p FROM SPlayer WHERE p.house_id = :houseId",
                                                SPlayer.class)
                                        .getResultList();
                        // store the merchouse
                        SHouse ourH = player.getMyHouse();

                        // employed players
                        toSend.append("Employed: <br>");
                        for (SPlayer currP : players) {
                            if (!ourH.getHouseFightingFor(currP).equals(ourH)) {
                                toSend.append(
                                        currP.getName()
                                                + ": "
                                                + ourH.getHouseFightingFor(currP).getColoredName()
                                                + "<br>");
                                count++;
                            }
                        }

                        // if none employed, say so.
                        if (count == 0) toSend.append("- NONE<br><br>");
                        else toSend.append("<br>");

                        // unemployed players
                        count = 0; // reset count
                        toSend.append("Unemployed: <br>");
                        for (SPlayer currP : players) {
                            if (ourH.getHouseFightingFor(currP).equals(ourH)) {
                                toSend.append(
                                        currP.getName()
                                                + ": "
                                                + ourH.getHouseFightingFor(currP).getColoredName()
                                                + "<br>");
                                count++;
                            }
                        }

                        // if none unemployed, say so.
                        if (count == 0) toSend.append("- NONE<br>");

                        // send the string
                        CampaignMain.cm.toUser(toSend.toString(), username, true);
                        return;
                    }

                    // not a merc, therefor must be is a normal faction member
                    toSend.append("Mercenaries employed by your faction: <br>");

                    // get merc factions, loop through
                    List<SPlayer> mercenaryPlayers =
                            session.createQuery(
                                            "SELECT p FROM SPlayer WHERE LEFT JOIN SHouse h ON h.id"
                                                + " == p.house_id WHERE h.dtype == 'MercHouse'",
                                            SPlayer.class)
                                    .getResultList();
                    for (SPlayer currP : mercenaryPlayers) {
                        if (currP.getHouseFightingFor().equals(player.getMyHouse())) {
                            toSend.append(" - " + currP.getName() + "<br>");
                            count++;
                        }
                    }

                    // if none unemployed, say so.
                    if (count == 0) toSend.append("- NONE<br>");

                    CampaignMain.cm.toUser(toSend.toString(), username, true);
                });
    } // end process()
}
