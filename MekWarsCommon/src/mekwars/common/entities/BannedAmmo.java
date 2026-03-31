/*
 * MekWars - Copyright (C) 2026
 *
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet) Original author Helge Richter (McWizard)
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */

package mekwars.common.entities;

import megamek.common.AmmoType;

import mekwars.common.House;

import java.util.Set;
import java.util.HashMap;
import java.util.Map;

public class BannedAmmo implements Entity {
    private static HashMap<String, AmmoType.Munitions> MUNITIONS_BY_NAME = createMunitionsByName();
    private static HashMap<AmmoType.Munitions, String> MUNITIONS_BY_NUMBER =
            createMunitionsByNumber();
    private static final AmmoType.Munitions[] MUNITION_VALUES = AmmoType.Munitions.values();
    // If house is null, then the ammo is system banned.
    private House house;
    private AmmoType.Munitions munition;

    public BannedAmmo(AmmoType.Munitions munition, House house) {
        this.munition = munition;
        this.house = house;
    }

    public House getHouse() {
        return house;
    }

    public AmmoType.Munitions getMunition() {
        return munition;
    }

    public int getId() {
        return munition.ordinal();
    }

    /**
     * Does nothing, the Id of a BannedAmmo is based on the ordinal value of the
     * {@link AmmoType.Munitions}.
     */
    public void setId(int id) {}

    public String getName() {
        return MUNITIONS_BY_NUMBER.get(munition);
    }

    public static Set<String> getAllMunitions() {
        return MUNITIONS_BY_NAME.keySet();
    }

    public static AmmoType.Munitions getMunitionByName(String name) {
        return MUNITIONS_BY_NAME.get(name);
    }

    public static AmmoType.Munitions getMunitionByNumber(int id) {
        if (id >= 0 && id < MUNITION_VALUES.length) {
            return MUNITION_VALUES[id];
        }
        return null;
    }

    /**
     * Note: This should eventually be swapped out for using some MegaMek native method that
     * probably exists.
     *
     * @return HashMap<String, AmmoType.Munitions>
     */
    private static HashMap<String, AmmoType.Munitions> createMunitionsByName() {
        HashMap<String, AmmoType.Munitions> munitions =
                new HashMap<String, AmmoType.Munitions>();

        munitions.put("Standard", AmmoType.Munitions.M_STANDARD);

        // AC Munition Types
        munitions.put("Cluster", AmmoType.Munitions.M_CLUSTER);
        munitions.put("AC Armor Piercing", AmmoType.Munitions.M_ARMOR_PIERCING);
        munitions.put("AC Flechette", AmmoType.Munitions.M_FLECHETTE);
        munitions.put("AC Incendiary", AmmoType.Munitions.M_INCENDIARY_AC);
        munitions.put("AC Precision", AmmoType.Munitions.M_PRECISION);
        munitions.put("AC Tracer", AmmoType.Munitions.M_TRACER);

        // ATM Munition Types
        munitions.put("ATM Extended Range", AmmoType.Munitions.M_EXTENDED_RANGE);
        munitions.put("ATM High Explosive", AmmoType.Munitions.M_HIGH_EXPLOSIVE);

        // LRM & SRM Munition Types
        munitions.put("LRM/SRM Fragmentation", AmmoType.Munitions.M_FRAGMENTATION);
        munitions.put("LRM/SRM Listen Kill", AmmoType.Munitions.M_LISTEN_KILL);
        munitions.put("LRM/SRM Anti-TSM", AmmoType.Munitions.M_ANTI_TSM);
        munitions.put("LRM/SRM Narc", AmmoType.Munitions.M_NARC_CAPABLE);
        munitions.put("LRM/SRM Artemis", AmmoType.Munitions.M_ARTEMIS_CAPABLE);
        munitions.put("LRM/SRM Heat-Seeking", AmmoType.Munitions.M_HEAT_SEEKING);
        munitions.put("LRM/SRM Dead-Fire", AmmoType.Munitions.M_DEAD_FIRE);
        munitions.put("LRM/SRM Tandem-Charge", AmmoType.Munitions.M_TANDEM_CHARGE);

        // LRM Munition Types
        // Incendiary is special, though...
        munitions.put("LRM Incendiary", AmmoType.Munitions.M_INCENDIARY_LRM);
        munitions.put("LRM Flare", AmmoType.Munitions.M_FLARE);
        munitions.put("LRM SemiGuided", AmmoType.Munitions.M_SEMIGUIDED);
        munitions.put("LRM Swarm", AmmoType.Munitions.M_SWARM);
        munitions.put("LRM Swarm I", AmmoType.Munitions.M_SWARM_I);
        munitions.put("LRM Thunder", AmmoType.Munitions.M_THUNDER);
        munitions.put("LRM Thunder Augmented", AmmoType.Munitions.M_THUNDER_AUGMENTED);
        munitions.put("LRM Thunder Inferno", AmmoType.Munitions.M_THUNDER_INFERNO);
        munitions.put("LRM Thunder VibraBomb", AmmoType.Munitions.M_THUNDER_VIBRABOMB);
        munitions.put("LRM Thunder Active", AmmoType.Munitions.M_THUNDER_ACTIVE);
        munitions.put("LRM Follow The Leader", AmmoType.Munitions.M_FOLLOW_THE_LEADER);
        munitions.put("Multi Purpose", AmmoType.Munitions.M_MULTI_PURPOSE);

        // SRM Munition Types
        munitions.put("SRM Inferno", AmmoType.Munitions.M_INFERNO);
        munitions.put("SRM Acid", AmmoType.Munitions.M_AX_HEAD);

        // Torps
        munitions.put("LRT/SRT", AmmoType.Munitions.M_TORPEDO);

        // iNarc Munition Types
        munitions.put("iNarc Explosive", AmmoType.Munitions.M_EXPLOSIVE);
        munitions.put("iNarc ECM", AmmoType.Munitions.M_ECM);
        munitions.put("iNarc HayWire", AmmoType.Munitions.M_HAYWIRE);
        munitions.put("iNarc Nemesis", AmmoType.Munitions.M_NEMESIS);

        // Narc Munition Types
        munitions.put("Narc Explosive", AmmoType.Munitions.M_NARC_EX);

        // Arrow IV Munition Types
        munitions.put("Arrow IV Homing", AmmoType.Munitions.M_HOMING);
        munitions.put("Arrow IV FASCAM", AmmoType.Munitions.M_FASCAM);
        munitions.put("Arrow IV Inferno", AmmoType.Munitions.M_INFERNO_IV);
        munitions.put("Arrow IV VibraBomb", AmmoType.Munitions.M_VIBRABOMB_IV);
        munitions.put("Arrow IV Smoke", AmmoType.Munitions.M_SMOKE);
        munitions.put("Arrow IV Davy Crockett", AmmoType.Munitions.M_DAVY_CROCKETT_M);
        return munitions;
    }

    private static HashMap<AmmoType.Munitions, String> createMunitionsByNumber() {
        HashMap<AmmoType.Munitions, String> munitionsByNumber = new HashMap<>();

        for (Map.Entry<String, AmmoType.Munitions> entry : createMunitionsByName().entrySet()) {
            munitionsByNumber.put(entry.getValue(), entry.getKey());
        }
        return munitionsByNumber;
    }
}
