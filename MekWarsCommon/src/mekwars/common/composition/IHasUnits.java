/*
 * MekWars - Copyright (C) 2026
 *
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet)
 * Original author Helge Richter (McWizard)
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

package mekwars.common.composition;

import mekwars.common.Unit;

import java.util.Comparator;
import java.util.List;

public interface IHasUnits {
    /**
     * Returns an unmodifiable list of all units.
     * @return An unmodifiable list of all units.
     */
    List<? extends Unit> getUnits();

    /**
     * Get the unit with the given id or null if it is not in the list.
     * @return The Unit with the given id or null.
     */
    Unit getUnit(int id);

    /**
     * Adds a unit to the given position.
     */
    void addUnit(int position, Unit unit);

    /**
     * Adds a unit to the end of the list.
     */
    void addUnit(Unit unit);

    /**
     * Removes a single unit with the given id.
     * @return true if the unit has been removed, otherwise false.
     */
    boolean removeUnit(int id);

    /**
     * Returns how many units are stored.
     * @return How many units are stored.
     */
    int getUnitCount();

    /**
     * Counts the units of a given type and weight
     *
     * @param type
     * @param weightClass
     * @return number of units matching the given type and weight.
     */
    int countUnits(int type, int weightClass);

    /**
     * Removes all units from the list.
     */
    void clearUnits();
}
