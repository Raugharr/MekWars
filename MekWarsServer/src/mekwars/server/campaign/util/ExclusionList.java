/*
 * MekWars - Copyright (C) 2005
 *
 * original author - nmorris (urgru@users.sourceforge.net)
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

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author urgru
 *     <p>Simple class which contains two vectors. The first is a list of players that an SPlayer is
 *     unwilling to play against (the "no-play" list). The second is an *admin controlled* list with
 *     the same effect. This is used to bar players from playing one another. It can be of unlimited
 *     length, and may or may not count against a player's own exclusion cap.
 *     <p>Included in the utilities package because this is really just a glorified vector bag which
 *     can to/from string itself. Not worthy of server.campaign.* =)
 */
@Entity
public class ExclusionList {
    public static final int NO_EXCLUSION = 0;
    public static final int PLAYER_EXCLUDED = 1;
    public static final int ADMIN_EXCLUDED = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne(mappedBy = "exclusionList")
    private SPlayer owner;

    @ElementCollection
    @CollectionTable(
            name = "player_excludes",
            joinColumns = @JoinColumn(name = "exclusion_list_id"))
    @Column(name = "player_name")
    private List<String> playerExcludes = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "admin_excludes", joinColumns = @JoinColumn(name = "exclusion_list_id"))
    @Column(name = "player_name")
    private List<String> adminExcludes = new ArrayList<>();

    public ExclusionList() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /** Method which sets an owner name. The name is used to send messaged */
    public void setOwner(SPlayer owner) {
        this.owner = owner;
    }

    /**
     * Method which adds a player to exclude lists. Simple. Does NOT check size, etc. This should be
     * done externally.
     *
     * @param fromAdmin - boolean, indicating origin of add
     * @param name - String, name of player to exclude
     */
    public void addExclude(boolean fromAdmin, String name) {
        if (fromAdmin) adminExcludes.add(name.toLowerCase());
        else playerExcludes.add(name.toLowerCase());
    }

    /**
     * Method which checks to see if a player is already included on an Exclusion list. Outside
     * classes which modify exclusions lists should check to ensure that duplications in Player and
     * Admin lists are properly handled.
     *
     * @param name - String, name to check
     * @return - int indicating which list, if any, contains name
     */
    public int checkExclude(String name) {
        if (playerExcludes.contains(name.toLowerCase())) return PLAYER_EXCLUDED;
        else if (adminExcludes.contains(name.toLowerCase())) return ADMIN_EXCLUDED;
        else return NO_EXCLUSION;
    }

    /**
     * Method which removes a player from an exclude list. Note that an admin can remove from EITHER
     * list, while a player can remove only from his own. Again, this is something that has to be
     * enforced externally (relies on truthful passing of the fromAdmin boolean).
     *
     * @param name - String, name to remove
     * @return
     */
    public void removeExclude(boolean fromAdmin, String name) {
        if (fromAdmin) {
            playerExcludes.remove(name.toLowerCase());
            adminExcludes.remove(name.toLowerCase());
        } else playerExcludes.remove(name.toLowerCase());
    }

    /**
     * Method which checks the excludes.
     *
     * <p>Both lists are checked for player who have left the campaign (deleted or unenrolled).
     *
     * <p>The player list is checked to ensure that it is not over maxSize(). If it is, names are
     * pruned, back to front, until the list equals maxsize or has size() == 0.
     */
    private void validateExcludes() {
        // first, look for missing players on the admin list.
        removeDeletedPlayers(adminExcludes);
        removeDeletedPlayers(playerExcludes);

        int maxSize = CampaignMain.cm.getIntegerConfig("NoPlayListSize");
        boolean adminListCountsForCap =
                CampaignMain.cm.getBooleanConfig("NoPlaysFromAdminsCountForMax");

        // finally, look for overflows in the player list.
        if (playerExcludes.size() > maxSize) {
            int excludeSize = 0;
            if (adminListCountsForCap) {
                excludeSize = playerExcludes.size() + adminExcludes.size();
            } else {
                excludeSize = playerExcludes.size();
            }

            while (excludeSize > maxSize && playerExcludes.size() > 0) {
                String currName = playerExcludes.get(playerExcludes.size() - 1);
                playerExcludes.remove(currName);
                CampaignMain.cm.toUser(
                        "Your No-Play list was too long. " + currName + " was removed.",
                        owner.getName(),
                        true);
                excludeSize = excludeSize - 1;
            }
        }
    } // end validateExcludes()

    /*
     * simple sizechecks for the excludes. no external
     * handling of the vectors.
     */
    public List<String> getPlayerExcludes() {
        return playerExcludes;
    }

    public List<String> getAdminExcludes() {
        return adminExcludes;
    }

    /*
     * The meat of things. To/From strong methods for both
     * the player and admin Exclude sheets. These are run
     * when a player is saved/loaded from disk.
     */
    public void adminExcludeFromString(String buffer, String delimiter) {
        StringTokenizer ST = new StringTokenizer(buffer, delimiter);
        while (ST.hasMoreElements()) {
            String curr = ST.nextToken();
            if (curr.equals("0")) return;
            // else
            this.addExclude(true, curr);
        }
    }

    public void playerExcludeFromString(String buffer, String delimiter) {
        StringTokenizer ST = new StringTokenizer(buffer, delimiter);
        while (ST.hasMoreElements()) {
            String curr = ST.nextToken();
            if (curr.equals("0")) return;
            // else
            this.addExclude(false, curr);
        }

        /*
         * Player excludes are loaded after admin excludes, so we
         * can assume that loading/filling of the ExclusionList is
         * now complete. Call the validator in order to ensure that
         * all the excludes are valid, and the lists aren't overful.
         */
        this.validateExcludes();
    }

    public String adminExcludeToString(String token) {
        StringBuilder result = new StringBuilder();

        if (adminExcludes.size() == 0) {
            result.append("0");
            result.append(token);
        } else {
            for (String currName : adminExcludes) {
                result.append(currName);
                result.append(token);
            }
        }

        return result.toString();
    }

    public String playerExcludeToString(String token) {
        StringBuilder result = new StringBuilder();

        if (playerExcludes.size() == 0) {
            result.append("0");
            result.append(token);
        } else {
            for (String currName : playerExcludes) {
                result.append(currName);
                result.append(token);
            }
        }

        return result.toString();
    }

    private void removeDeletedPlayers(List<String> excludeList) {
        excludeList.removeIf(
                currName -> {
                    boolean playerExists = CampaignMain.cm.getPlayer(currName) != null;
                    if (!playerExists) {
                        CampaignMain.cm.toUser(
                                currName + " has left the campaign. No-Play list updated.",
                                owner.getName(),
                                true);
                    }
                    return !playerExists;
                });
    }
} // end ExcludeList
