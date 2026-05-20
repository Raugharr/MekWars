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

import mekwars.common.CampaignData;
import mekwars.common.House;
import mekwars.common.util.HibernateUtil;
import mekwars.server.MWServ;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SHouse;
import mekwars.server.campaign.SPlayer;

import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class PlayersCommand implements Command {
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

    // NOTE: There Are Problems WIth This Code and the display of MERCENARY PLAYERS
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
                    String toSend = "";

                    /*
                     * Fighting players are always broken into a
                     * separate grouping, so we'll do them first.
                     */
                    toSend += "<br><h2>Fighting Players:</h2><br>";
                    for (House vh : CampaignMain.cm.getData().getAllHouses()) {
                        SHouse h = (SHouse) vh;
                        List<SPlayer> fightingPlayers =
                                session.createQuery(
                                                "FROM SPlayer WHERE house_id = :houseId"
                                                        + " AND status = :status",
                                                SPlayer.class)
                                        .setParameter("houseId", h.getId())
                                        .setParameter("status", SPlayer.STATUS_FIGHTING)
                                        .getResultList();
                        if (!fightingPlayers.isEmpty()) {
                            // alpha-sort the players
                            TreeMap<String, SPlayer> sortedPlayers = new TreeMap<String, SPlayer>();
                            sortedPlayers.putAll(
                                    fightingPlayers.stream()
                                            .collect(Collectors.toMap(SPlayer::getName, p -> p)));

                            toSend += "<b>" + h.getColoredNameAsLink() + ":</b> ";
                            for (SPlayer currP : sortedPlayers.values())
                                toSend += currP.getName() + ", ";

                            // strip the last comma
                            toSend = toSend.substring(0, toSend.length() - 2) + "<br>";
                        }
                    }

                    /*
                     * Now, we care about whether or not activity status is
                     * being hidden. If not, split reserve and active lists.
                     */
                    if (!(CampaignData.cd
                            .getCampaignOptions()
                            .getBooleanConfig("HideActiveStatus"))) {
                        toSend += ("<br><h2>Active Duty Players:</h2><br>");
                        for (House vh : CampaignMain.cm.getData().getAllHouses()) {

                            SHouse h = (SHouse) vh;
                            List<SPlayer> activePlayers =
                                    session.createQuery(
                                                    "FROM SPlayer WHERE house_id ="
                                                            + " :houseId AND status = :status",
                                                    SPlayer.class)
                                            .setParameter("houseId", h.getId())
                                            .setParameter("status", SPlayer.STATUS_ACTIVE)
                                            .getResultList();
                            if (!activePlayers.isEmpty()) {
                                // alpha-sort the players
                                TreeMap<String, SPlayer> sortedPlayers =
                                        new TreeMap<String, SPlayer>();
                                sortedPlayers.putAll(
                                        activePlayers.stream()
                                                .collect(
                                                        Collectors.toMap(
                                                                SPlayer::getName, p -> p)));

                                toSend += ("<b>" + h.getColoredNameAsLink() + ":</b> ");
                                for (SPlayer currP : sortedPlayers.values()) {
                                    toSend += (currP.getName() + ", ");
                                }

                                // strip the last comma
                                toSend = toSend.substring(0, toSend.length() - 2) + "<br>";
                            }
                        }

                        toSend += "<br><h2>Reserve Duty Players:</h2><br>";
                        for (House vh : CampaignMain.cm.getData().getAllHouses()) {

                            SHouse h = (SHouse) vh;
                            List<SPlayer> reservePlayers =
                                    session.createQuery(
                                                    "FROM SPlayer WHERE house_id ="
                                                            + " :houseId AND status = :status",
                                                    SPlayer.class)
                                            .setParameter("houseId", h.getId())
                                            .setParameter("status", SPlayer.STATUS_RESERVE)
                                            .getResultList();
                            if (!reservePlayers.isEmpty()) {
                                // alpha-sort the players
                                TreeMap<String, SPlayer> sortedPlayers =
                                        new TreeMap<String, SPlayer>();
                                sortedPlayers.putAll(
                                        reservePlayers.stream()
                                                .collect(
                                                        Collectors.toMap(
                                                                SPlayer::getName, p -> p)));

                                toSend += ("<b>" + h.getColoredNameAsLink() + ":</b> ");
                                for (SPlayer currP : sortedPlayers.values()) {
                                    if (currP.isInvisible()) {
                                        continue;
                                    }
                                    toSend += (currP.getName() + ", ");
                                }

                                // strip the last comma
                                toSend = toSend.substring(0, toSend.length() - 2) + "<br>";
                            }
                        }

                        /*
                         * Else, we're hiding the active status. Show Players who are online
                         * in all houses, but not fighting. Italicize the active players in
                         * the requestor's house.
                         */
                    } else {
                        TreeMap<String, SPlayer> combinedTable;

                        toSend += ("<br><h2>Online Players (Not Fighting):</h2><br>");
                        for (House vh : CampaignMain.cm.getData().getAllHouses()) {
                            SHouse h = (SHouse) vh;
                            combinedTable = new TreeMap<String, SPlayer>();
                            List<SPlayer> onlinePlayers =
                                    session.createQuery(
                                                    "FROM SPlayer WHERE house_id = :houseId"
                                                            + " AND status != :loggedOutStatus",
                                                    SPlayer.class)
                                            .setParameter("houseId", h.getId())
                                            .setParameter(
                                                    "loggedOutStatus", SPlayer.STATUS_LOGGEDOUT)
                                            .getResultList();

                            for (SPlayer p : onlinePlayers) {
                                combinedTable.put(p.getName(), p);
                            }
                            combinedTable.putAll(
                                    onlinePlayers.stream()
                                            .collect(Collectors.toMap(SPlayer::getName, p -> p)));

                            boolean playersFaction =
                                    h.equals(CampaignMain.cm.getPlayer(username).getMyHouse());
                            boolean isAdmin = MWServ.getInstance().isAdmin(username);

                            if (!combinedTable.isEmpty()) {
                                // add all players
                                toSend += ("<b>" + h.getColoredNameAsLink() + ":</b> ");
                                for (SPlayer currP : combinedTable.values()) {

                                    if (currP.isInvisible()) continue;

                                    if ((playersFaction || isAdmin)
                                            && currP.getDutyStatus() == SPlayer.STATUS_ACTIVE) {
                                        toSend += ("<i>" + currP.getName() + "</i>, ");
                                    } else {
                                        toSend += (currP.getName() + ", ");
                                    }
                                }

                                // strip the last comma. send number of active players to mods and
                                // admins
                                toSend = toSend.substring(0, toSend.length() - 2);
                                if (isAdmin || playersFaction) {
                                    Integer activeCount =
                                            session.createQuery(
                                                            "SELECT COUNT(p) FROM SPlayer p"
                                                                    + " WHERE p.house_id ="
                                                                    + " :houseId AND p.status ="
                                                                    + " :status",
                                                            Integer.class)
                                                    .setParameter("houseId", h.getId())
                                                    .setParameter("status", SPlayer.STATUS_ACTIVE)
                                                    .uniqueResult();
                                    toSend += " (Active: " + activeCount + ")<br>";
                                } else toSend += "<br>";
                            }
                        }
                    }
                    // send to the requestor
                    CampaignMain.cm.toUser("SM|" + toSend, username, false);
                });
    } // end process()
} // end PlayersCommand
