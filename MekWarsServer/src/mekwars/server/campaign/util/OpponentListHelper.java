/*
 * MekWars - Copyright (C) 2004
 *
 * Original author - nmorris (urgru@users.sourceforge.net)
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

package mekwars.server.campaign.util;

import mekwars.common.CampaignData;
import mekwars.common.House;
import mekwars.common.campaign.operations.Operation;
import mekwars.common.util.HibernateUtil;
import mekwars.common.util.StringUtils;
import mekwars.server.MWServ;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SArmy;
import mekwars.server.campaign.SHouse;
import mekwars.server.campaign.SPlayer;
import mekwars.server.campaign.operations.newopmanager.I_OperationManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * @author urgru
 *     <p>OpponentListHelper takes an army (or set of armies) and compares it/them to all other
 *     active armies in order to generate an opponent list. The "list" is stored as a vector of
 *     armies in the given force's SArmyData. The Helper also updates matching opposition armies w/
 *     pointers to the newly active force.
 *     <p>Each activate command creates a new Helper, looping all of the activating players.
 */
public class OpponentListHelper {
    private static final Logger LOGGER = LogManager.getLogger(OpponentListHelper.class);
    private static final List<BiPredicate<SPlayer, SPlayer>> OPPONENT_DISQUALIFIERS =
            List.of(
                    SPlayer::equals,
                    (search, opponent) -> MWServ.getInstance().getIThread().isImmune(opponent),
                    (search, opponent) -> opponent.getArmies().isEmpty(),
                    (search, opponent) ->
                            opponent.getExclusionList().checkExclude(search.getName())
                                    != ExclusionList.NO_EXCLUSION,
                    (search, opponent) ->
                            search.getExclusionList().checkExclude(opponent.getName())
                                    != ExclusionList.NO_EXCLUSION,
                    (search, opponent) ->
                            CampaignData.cd.getCampaignOptions().getBooleanConfig("IPCheck")
                                    && search.hasSameIP(opponent));

    // VARIABLES
    private SPlayer searchPlayer;
    private TreeMap<String, List<SArmy>> potentialOpponents = new TreeMap<String, List<SArmy>>();

    private int currentMode = -1;

    public static final int MODE_ADD = 0;
    public static final int MODE_REMOVE = 1;

    public OpponentListHelper(SPlayer p) {
        searchPlayer = p;
    }

    public void execute(int mode) {
        currentMode = mode;

        if (mode == MODE_ADD) this.getOpponentsForAll();
        if (mode == MODE_REMOVE) this.removeOpponentsForAll();
    }

    /** Method which finds and updates potential opponents for all of a given player's armies. */
    private void getOpponentsForAll() {
        /*
         * Check all active players from all factions for potential opponents.
         * Don't add one self, housemates (unless in house attacks are enabled),
         * immune players, people at the same IP, or excluded players to creep
         * into the OLH.
         */
        for (House h : CampaignMain.cm.getData().getAllHouses()) {
            SHouse currHouse = (SHouse) h;

            // check all active player and ONLY active players
            for (SPlayer currPlayer : currHouse.getActivePlayers().values()) {
                if (!isValidOpponent(currPlayer)) {
                    continue;
                }
                /*
                 * Player passes all bars. Check his armies against those
                 * of the activating player. If the armies match, add the
                 * enemy army to possDefendArmies and crossing the armies
                 * as opponents in each others' lists.
                 */
                List<SArmy> matched = matchedEnemyArmies(currPlayer);

                potentialOpponents.put(currPlayer.getName(), matched);
            }
        }
    }

    /**
     * Method which removes all opponentlists for a player, and removes his armies from other
     * player's opplists.
     */
    private void removeOpponentsForAll() {
        /*
         * Player is being moved to inactive or fighting status. This means
         * he is no longer an eligible attack target. Need to remove his oplists
         * and clear his entries on other players oplists.
         */
        for (SArmy currArmy : searchPlayer.getArmies()) {
            // remove curr army from all opparmies which link it.
            for (SArmy oppArmy : currArmy.getOpponents()) {
                oppArmy.removeOpponent(currArmy);

                // add to the
                String currName = oppArmy.getOwner().getName().toLowerCase();
                if (potentialOpponents.get(currName) == null)
                    potentialOpponents.put(currName, new ArrayList<SArmy>());
            }
            // reset currArmy's OpponentList
            currArmy.setOpponents(new Vector<SArmy>(1, 1));
        }
    }

    /**
     * Method which sends "New Opponent"-style messages to players who are already active, based on
     * contents of possibleOpponents hash.
     *
     * <p>Note that this is a public method, whereas nearly all other Helper methods are private.
     * Send info is called after the Helper is constrcuted.
     */
    public void sendInfoToOpponents(String s) {
        for (String currOppName : potentialOpponents.keySet()) {
            StringBuilder output = new StringBuilder("ED:[!] ");
            SPlayer currOpp = CampaignMain.cm.getPlayer(currOppName);

            if (currOpp == null) continue;

            // if opponent doesn't meet min active time, don't send.
            long minActiveTime =
                    CampaignData.cd.getCampaignOptions().getLongConfig("MinActiveTime") * 1000;
            if (System.currentTimeMillis() - currOpp.getActiveSince() <= minActiveTime) continue;

            // get the colored faction name
            SHouse searchHouse = searchPlayer.getHouseFightingFor();
            String colHouseName = searchHouse.getColoredNameAsLink();
            output.append(
                    StringUtils.aOrAn(searchHouse.getName(), false, false)
                            + " "
                            + colHouseName
                            + " unit "
                            + s);

            /*
             * If its an add, show exactly which armies can attack. A remove
             * just shows the faction which is leaving and won't need any more
             * text.
             */
            if (currentMode == MODE_ADD) {
                List<SArmy> currOppArmies = potentialOpponents.get(currOppName);

                if (currOppArmies.size() > 1) {
                    output.append("Armies ");
                    Iterator<SArmy> i = currOppArmies.iterator();
                    while (i.hasNext()) {
                        output.append(i.next().getId());
                        if (i.hasNext()) {
                            output.append(", ");
                        }
                    }
                    // try to remove the last instance of ", "
                    int lastComma = output.lastIndexOf(", ");
                    if (lastComma >= 0) {
                        String front = output.substring(0, lastComma);
                        String back = output.substring(lastComma + 2, output.length());
                        output = new StringBuilder(front + " and " + back);
                    }

                } else { // we can assume size == 1
                    SArmy currArmy = currOppArmies.get(0);
                    output.append("Army " + currArmy.getId());
                }
            }

            /*
             * Also give the user a link which he can click in order
             * to issue a checkattack command and see the matchups.
             */
            output.append(". [<a href=\"MEKWARS/c checkattack\">Report</a>]");
            CampaignMain.cm.toUser(output.toString(), currOppName, true);
        }
    }

    public boolean isValidOpponent(SPlayer currPlayer) {
        return OPPONENT_DISQUALIFIERS.stream()
                .noneMatch(disqualifier -> disqualifier.test(searchPlayer, currPlayer));
    }

    /**
     * @return A List sorted by the Army's id of all of searchPlayer's armies and any army owned by
     *     currPlayer that the army is elligible to attack.
     */
    public List<SArmy> matchedEnemyArmies(SPlayer currPlayer) {
        List<SArmy> matched = new ArrayList<>();
        SHouse searchHouse = searchPlayer.getHouseFightingFor();
        SHouse currHouse = currPlayer.getHouseFightingFor();
        boolean housesAreEqual = currHouse.equals(searchHouse);

        for (SArmy searchArmy : searchPlayer.getArmies()) {
            if (searchArmy.isDisabled()) {
                continue;
            }
            for (SArmy enemyArmy : currPlayer.getArmies()) {
                if (canAttack(searchArmy, enemyArmy, housesAreEqual)) {
                    searchArmy.addOpponent(enemyArmy);
                    enemyArmy.addOpponent(searchArmy);
                    if (!matched.contains(enemyArmy)) {
                        matched.add(enemyArmy);
                    }
                }
            }
        }
        matched.sort(Comparator.comparingInt(SArmy::getId));
        return matched;
    }

    /**
     * Returns if enemyArmy is a valid attack option for searchArmy. This does not validate if
     * searchArmy is in a valid state to attack.
     *
     * @return If enemyArmy is a valid attack option for searchArmy.
     */
    private boolean canAttack(SArmy searchArmy, SArmy enemyArmy, boolean housesAreEqual) {
        I_OperationManager manager = CampaignMain.cm.getOpsManager();
        Predicate<Operation> inFactionFighting =
                operation -> !housesAreEqual || operation.getBooleanValue("AllowInFaction");

        return !enemyArmy.isDisabled()
                && searchArmy.getLegalOperations().stream()
                        .map(manager::getOperation)
                        .filter(inFactionFighting)
                        .anyMatch(operation -> searchArmy.matches(enemyArmy, operation));
    }
}
