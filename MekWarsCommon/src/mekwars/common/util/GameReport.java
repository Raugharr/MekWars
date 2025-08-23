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

package common.util;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import common.GameInterface;
import common.campaign.Buildings;
import common.campaign.clientutils.SerializeEntity;

import megamek.common.Entity;

/*
 * This class is responsible for reports that are generated on both the client and server.
 */
public class GameReport {
    public static StringBuilder prepareReport(GameInterface myGame,
            boolean usingAdvancedRepairs, Buildings buildingTemplate) {
        StringBuilder result = new StringBuilder();
        String name = "";
        // Parse the real playername from the Modified In game one..
        String winnerName = "";
        if (myGame.hasWinner()) {

            int numberOfWinners = 0;
            // Multiple Winners
            List<String> winners = myGame.getWinners();

            // TODO: Winners is sometimes coming up empty. Let's see why
            MWLogger.errLog("Finding winners:");
            MWLogger.errLog(winners.toString());

            for (String winner : winners) {
                StringTokenizer st = new StringTokenizer(winner, "~");
                name = "";
                while (st.hasMoreElements()) {
                    name = st.nextToken().trim();
                }
                // some of the players set themselves as a team of 1.
                // This keeps that from happening.
                if (numberOfWinners > 0) {
                    winnerName += "*";
                }
                numberOfWinners++;

                winnerName += name;
            }
            if (winnerName.endsWith("*")) {
                winnerName = winnerName.substring(0, winnerName.length() - 1);
            }
            winnerName += "#";
        }

        else {
            winnerName = "DRAW#";
        }

        result.append(winnerName);

        // Report the mech stat
        Enumeration<Entity> en = myGame.getDevastatedEntities();
        while (en.hasMoreElements()) {
            Entity ent = en.nextElement();
            if (ent.getOwner().getName().startsWith("War Bot")) {
                continue;
            }
            result.append(SerializeEntity.serializeEntity(ent, true, false,
                    usingAdvancedRepairs));
            result.append("#");
        }
        en = myGame.getGraveyardEntities();
        while (en.hasMoreElements()) {
            Entity ent = en.nextElement();
            if (ent.getOwner().getName().startsWith("War Bot")) {
                continue;
            }
            result.append(SerializeEntity.serializeEntity(ent, true, false,
                    usingAdvancedRepairs));
            result.append("#");

        }
        Iterator<Entity> en2 = myGame.getEntities();
        while (en2.hasNext()) {
            Entity ent = en2.next();
            if (ent.getOwner().getName().startsWith("War Bot")) {
                continue;
            }
            result.append(SerializeEntity.serializeEntity(ent, true, false,
                    usingAdvancedRepairs));
            result.append("#");
        }
        en = myGame.getRetreatedEntities();
        while (en.hasMoreElements()) {
            Entity ent = en.nextElement();
            if (ent.getOwner().getName().startsWith("War Bot")) {
                continue;
            }
            result.append(SerializeEntity.serializeEntity(ent, true, false,
                    usingAdvancedRepairs));
            result.append("#");
        }

        if (buildingTemplate != null) {
            result.append("BL*" + buildingTemplate);
        }
        MWLogger.infoLog("CR|" + result);
        return result;
    }
}
