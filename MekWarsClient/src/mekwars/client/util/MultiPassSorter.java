/*
 * MekWars - Copyright (C) 2026
 *
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megamek)
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

package mekwars.client.util;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class MultiPassSorter<T> {
    private final List<String> choices;
    private final Function<Integer, Comparator<T>> comparatorFactory;
    private final int noneValue;

    public MultiPassSorter(
            List<String> choices,
            Function<Integer, Comparator<T>> comparatorFactory,
            int noneValue) {
        this.choices = choices;
        this.comparatorFactory = comparatorFactory;
        this.noneValue = noneValue;
    }

    public void sort(List<T> items, List<String> sortingOrder) {
        Comparator<T> comparator = null;
        Set<Integer> seen = new HashSet<>();

        for (String configValue : sortingOrder) {
            int sortOrder = resolve(configValue);
            if (sortOrder != noneValue && seen.add(sortOrder)) {
                comparator =
                        comparator == null
                                ? comparatorFactory.apply(sortOrder)
                                : comparator.thenComparing(comparatorFactory.apply(sortOrder));
            }
        }

        if (comparator != null) {
            items.sort(comparator);
        }
    }

    private int resolve(String configValue) {
        int index = choices.indexOf(configValue);

        return index >= 0 ? index : noneValue;
    }
}
