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

package mekwars.client.campaign.sort;

import mekwars.client.campaign.CUnit;
import mekwars.client.util.CUnitComparator;
import mekwars.client.util.MultiPassSorter;

import java.util.List;

/*
 * Hangar sorting mechanisms. Client and server need not order hangars in
 * the same fashion, since all transactions (after the initial data feed)
 * take place on a unit by unit basis.
 *
 * Sort options: - BV - Name - Type - Unit ID - Weight - No sort [load
 * order]
 *
 * BV is (for all intents and purposes) an exclusive sort. The others can
 * lead to significant clustering. Hence, secondary filters can be applied.
 */
public final class HangarSorter {
    private static final List<String> SORT_CHOICES =
            List.of(
                    "Name",
                    "Battle Value",
                    "Gunnery Skill",
                    "ID Number",
                    "MP (Jumping)",
                    "MP (Walking)",
                    "Pilot Kills",
                    "Unit Type",
                    "Weight (Class)",
                    "Weight (Tons)",
                    "No Sort");

    private static final MultiPassSorter<CUnit> sorter =
            new MultiPassSorter<>(SORT_CHOICES, CUnitComparator::new, CUnitComparator.HQSORT_NONE);

    public static void sort(List<CUnit> units, List<String> sortingOrder) {
        sorter.sort(units, sortingOrder);
    }

    private HangarSorter() {}
}
