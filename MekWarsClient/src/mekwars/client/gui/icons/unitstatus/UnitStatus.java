/*
 * MekWars - Copyright (C) 2025
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

package mekwars.client.gui.icons.unitstatus;

import mekwars.client.campaign.CArmy;
import mekwars.client.campaign.CUnit;
import mekwars.client.gui.icons.UnitStatusIcon;

@FunctionalInterface
public interface UnitStatus {
    /**
     * Returns an {@link UnitStatusIcon} that displays the status of the mech. Can return null if
     * there is no status.
     */
    UnitStatusIcon apply(CUnit unit, CArmy army);
}
