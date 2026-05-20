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

import megamek.common.TechConstants;

import mekwars.common.util.HibernateUtil;
import mekwars.server.MWServ;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SHouse;
import mekwars.server.campaign.SPlanet;
import mekwars.server.campaign.SPlayer;
import mekwars.server.campaign.operations.ShortOperation;
import mekwars.server.campaign.util.PlanetNameComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class HouseCommand implements Command {
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

        if (!command.hasMoreElements()) {
            CampaignMain.cm.toUser("AM:Improper format. Try: /c faction#name", username, false);
            return;
        }

        HibernateUtil.inTransaction(
                session -> {
                    String name = (String) command.nextElement();
                    SHouse house = (SHouse) CampaignMain.cm.getData().getHouseByName(name);
                    if (house == null) {
                        CampaignMain.cm.toUser(
                                "AM:Could not find faction. Command fails.", username, false);
                        return;
                    }

                    // cleared breaks. start assembling a status return.
                    String output =
                            "<br><b><u>Status for: " + house.getColoredName() + "</u></b><br>";

                    // add up number of various player numbers
                    long totalOnline =
                            session.createQuery(
                                            "SELECT COUNT(p) FROM SPlayer p LEFT JOIN SHOUSE h ON"
                                                    + " h.id == p.house_id WHERE p.status != "
                                                    + SPlayer.STATUS_LOGGEDOUT,
                                            Long.class)
                                    .getSingleResult();

                    int sinceLastRestart = house.getSmallPlayers().size();

                    output +=
                            "Players: "
                                    + totalOnline
                                    + " online, "
                                    + sinceLastRestart
                                    + " logins since last restart.<br>";

                    // sort out planets owned/fighting on, etc.
                    // use this loop to generate a ranking as well.
                    List<SPlanet> ownedWorlds = new ArrayList<SPlanet>();
                    List<SPlanet> contestedWorlds = new ArrayList<SPlanet>();
                    int totalOwnership = 0;

                    List<SPlanet> planets =
                            session.createQuery(
                                            "SELECT p FROM SPlanet p LEFT JOIN Influence i LEFT"
                                                + " JOIN SHOUSE ON p.id == i.planet_id AND h.id =="
                                                + " :houseId",
                                            SPlanet.class)
                                    .setParameter("houseId", house.getId())
                                    .getResultList();

                    for (SPlanet planet : planets) {
                        // update total
                        int ownership = planet.getInfluence().getInfluence(house.getId());
                        totalOwnership = totalOwnership + ownership;

                        // update lists
                        if (planet.getOwner() != null && planet.getOwner().equals(house)) {
                            ownedWorlds.add(planet);
                        } else if (ownership > 0) {
                            contestedWorlds.add(planet);
                        }
                    }

                    // Current Ranking
                    String rankString = "";
                    int diff = totalOwnership - house.getInitialHouseRanking();
                    if (diff > 0) rankString += "+" + diff;
                    else rankString += diff;
                    rankString += "/" + house.getInitialHouseRanking();

                    output += "Ranking: " + rankString + "<br>";

                    // assume at least one player
                    if (sinceLastRestart < 1) sinceLastRestart = 1;

                    int ownedWorldsSize = ownedWorlds.size();

                    if (ownedWorldsSize < 1) ownedWorldsSize = 1;

                    // economy stats
                    output +=
                            "Total Economic Value: "
                                    + house.getComponentProduction()
                                    + "<br>"
                                    + "  - Avg. Planet: "
                                    + house.getComponentProduction() / ownedWorldsSize
                                    + "<br>"
                                    + "  - Per Capita: "
                                    + house.getComponentProduction() / sinceLastRestart
                                    + "<br><br>";

                    output +=
                            "<br><b>Tech Level: </b>"
                                    + TechConstants.getLevelDisplayableName(house.getTechLevel())
                                    + ".";

                    if (CampaignMain.cm.getPlayer(username).getMyHouse().equals(house)
                            || MWServ.getInstance().isModerator(username)) {
                        output +=
                                " Current Research: "
                                        + house.getTechResearchPoints()
                                        + " of "
                                        + CampaignMain.cm.getConfig("TechPointsNeedToLevel");
                    }

                    output += "<br>";

                    // sort planets by alpha, instead of ID
                    Collections.sort(ownedWorlds, new PlanetNameComparator());
                    Collections.sort(contestedWorlds, new PlanetNameComparator());

                    Iterator<SPlanet> i = ownedWorlds.iterator();
                    output += "<b>Planets (Owned):</b>";

                    if (!i.hasNext()) output += " none<br>";
                    else output += "<br>";

                    while (i.hasNext()) {
                        SPlanet currPlanet = (SPlanet) i.next();
                        output += currPlanet.getNameAsColoredLink();
                        if (currPlanet.getFactoryCount() > 0) output += "*";

                        // show % owned, if < 100
                        int amtOwned = currPlanet.getInfluence().getInfluence(house.getId());
                        if (amtOwned < currPlanet.getConquestPoints())
                            output += " (" + amtOwned + "cp)";

                        if (i.hasNext()) output += ", ";
                        else output += "<br>";
                    }

                    output += "<br><b>Planets (Contested):</b>";
                    i = contestedWorlds.iterator();

                    if (!i.hasNext()) output += " none<br>";
                    else output += "<br>";

                    while (i.hasNext()) {
                        SPlanet currPlanet = (SPlanet) i.next();
                        output += currPlanet.getNameAsColoredLink();
                        if (currPlanet.getFactoryCount() > 0) output += "*";

                        // show % owned
                        SHouse owner = (SHouse) currPlanet.getOwner();
                        if (owner != null)
                            output +=
                                    " ("
                                            + currPlanet.getInfluence().getInfluence(house.getId())
                                            + "cp, "
                                            + owner.getColoredAbbreviation(false)
                                            + " "
                                            + currPlanet.getInfluence().getInfluence(owner.getId())
                                            + "cp)";
                        else
                            output +=
                                    "("
                                            + currPlanet.getInfluence().getInfluence(house.getId())
                                            + "cp, No Owner)";

                        if (i.hasNext()) output += ", ";
                        else output += "<br>";
                    }

                    // show the games the factions' players are involved in
                    output += "<br><b>Current Games: </b>";
                    String gameStrings = "";

                    Iterator<ShortOperation> games =
                            CampaignMain.cm.getOpsManager().getRunningOps().values().iterator();
                    while (games.hasNext()) {
                        ShortOperation so = (ShortOperation) games.next();
                        if (so.hasPlayerWhoseHouseBeginsWith(house.getName()))
                            gameStrings += "<br>" + so.getInfo(false, false);
                    } // end while(more elements)

                    if (gameStrings.trim().equals("")) output += "none.<br>";
                    else output += gameStrings + "<br>";

                    CampaignMain.cm.toUser("SM|" + output, username, false);
                });
    }
}
