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

import jakarta.persistence.OneToMany;
import jakarta.persistence.Embeddable;

import mekwars.common.Unit;

import java.util.ArrayList;
import java.util.List;

@Embeddable
public class HasUnits<T extends Unit> {
    @OneToMany
    private List<T> units = new ArrayList<>();

    /**
     * @see IHasUnits#getUnits
     */
    public List<T> getAll() {
        return units;
    }

    /**
     * @see IHasUnits#getUnit(int)
     */
    public T get(int id) {
        for (T unit : units) {
            if (unit.getId() == id) {
                return unit;
            }
        }
        return null;
    }

    /**
     * @see IHasUnits#addUnit(Unit, int)
     */
    public void add(int position, T unit) {
        units.add(position, unit);
    }

    /**
     * @see IHasUnits#addUnit(Unit)
     */
    public void add(T unit) {
        units.add(unit);
    }

    /**
     * @see IHasUnits#removeUnit(int)
     */
    public boolean remove(int id) {
        return units.removeIf(unit -> unit.getId() == id);
    }

    /**
     * @see IHasUnits#getUnitCount()
     */
    public int count() {
        return units.size();
    }

    /**
     * @see IHasUnits#countUnits(int, int)
     */
    public int count(int type, int weightClass) {
        if ((type < 0) || (type > Unit.AERO)) {
            return 0;
        }
        if ((weightClass < 0) || (weightClass > Unit.ASSAULT)) {
            return 0;
        }
        // Actually count them now
        int count = 0;
        for (Unit unit : units) {
            if (!unit.isChristmasUnit()
                    && (unit.getType() == type)
                    && (unit.getWeightClass() == weightClass)) {
                count++;
            }
        }
        return count;
    }

    /**
     * @see IHasUnits#clear()
     */
    public void clear() {
        units.clear();
    }
}
