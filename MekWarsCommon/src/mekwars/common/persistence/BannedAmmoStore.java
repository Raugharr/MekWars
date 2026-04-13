/*
 * MekWars - Copyright (C) 2026
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

package mekwars.common.persistence;

import megamek.common.AmmoType;

import mekwars.common.House;
import mekwars.common.entities.BannedAmmo;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class BannedAmmoStore {
    private ArrayList<BannedAmmo> bannedAmmoList = new ArrayList<>();

    /**
     * Adds the provided munition to the house's banned list.
     *
     * <p>If House is null the munition will be banned system wide.
     *
     * @return The added {@link BannedAmmo}
     */
    public BannedAmmo add(AmmoType.Munitions munition, House house) {
        BannedAmmo bannedAmmo = get(munition, house);
        if (bannedAmmo != null) {
            return bannedAmmo;
        }
        bannedAmmo = new BannedAmmo(munition, house);
        bannedAmmoList.add(bannedAmmo);
        return bannedAmmo;
    }

    /** Removes the provided munition from the house's banned ammo list. */
    public void remove(AmmoType.Munitions munition, House house) {
        int index = 0;

        for (BannedAmmo bannedAmmo : bannedAmmoList) {
            if (bannedAmmo.getMunition() == munition
                    && (house == null || house.equals(bannedAmmo.getHouse()))) {
                bannedAmmoList.remove(index);
                return;
            }
            ++index;
        }
    }

    /**
     * Returns the BannedAmmo that corresponds to the provided munition, otherwise null.
     *
     * @return The {@link BannedAmmo} that corresponds to the provided munition, otherwise null.
     */
    public BannedAmmo get(AmmoType.Munitions munition, House house) {
        for (BannedAmmo bannedAmmo : bannedAmmoList) {
            if (bannedAmmo.getMunition() == munition
                    && (house == null || house.equals(bannedAmmo.getHouse()))) {
                return bannedAmmo;
            }
        }
        return null;
    }

    /**
     * Returns true if {@code munition} is banned by {@code house}.
     *
     * @return true if {@code munition} is banned by {@code house}.
     */
    public boolean isBanned(AmmoType.Munitions munition, House house) {
        for (BannedAmmo bannedAmmo : bannedAmmoList) {
            if (bannedAmmo.getMunition() == munition
                    && (house == null || house.equals(bannedAmmo.getHouse()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if any munition in {@code munitions} is banned by {@code house}.
     *
     * @return true if any munition in {@code munitions} is banned by {@code house}.
     */
    public boolean isBanned(EnumSet<AmmoType.Munitions> munitions, House house) {
        for (AmmoType.Munitions munition : munitions) {
            if (isBanned(munition, house)) {
                return true;
            }
        }
        return false;
    }

    public Map<Optional<House>, List<BannedAmmo>> groupByHouse() {
        return bannedAmmoList.stream()
                .collect(
                        Collectors.groupingBy(
                                bannedAmmo -> Optional.ofNullable(bannedAmmo.getHouse())));
    }

    /**
     * Get every {@link BannedAmmo} for the given house.
     *
     * @return Every {@link BannedAmmo} for the given house.
     */
    public List<BannedAmmo> getByHouse(House house) {
        return bannedAmmoList.stream()
                .filter(
                        bannedAmmo ->
                                (house != null)
                                        ? house.equals(bannedAmmo.getHouse())
                                        : bannedAmmo.getHouse() == null)
                .collect(Collectors.toList());
    }

    /** Remove all banned ammos. */
    public void clear() {
        bannedAmmoList.clear();
    }
}
