/*
 * MekWars - Copyright (C) 2004
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

import java.util.ResourceBundle;
import megamek.common.Entity;
import mekwars.client.campaign.CArmy;
import mekwars.client.campaign.CUnit;
import mekwars.common.util.UnitUtils;
import mekwars.client.gui.icons.UnitStatusIcon;

public class AmmoStatus implements UnitStatus {
    private static final int EMPTY = 0;
    private static final int LOW = 1;

    private static final UnitStatusIcon[] ICONS = new UnitStatusIcon[] {
        new UnitStatusIcon("data/images/status/empty.gif") {
            @Override
            public String getToolTipText(ResourceBundle resourceBundle) {
                return resourceBundle.getString("unitStatus.empty.tooltip");
            }
        },
        new UnitStatusIcon("data/images/status/low.gif") {
            @Override
            public String getToolTipText(ResourceBundle resourceBundle) {
                return resourceBundle.getString("unitStatus.low.tooltip");
            }
        },
    };

    public UnitStatusIcon apply(CUnit unit, CArmy army) {
        Entity entity = unit.getEntity();

        if (UnitUtils.hasEmptyAmmo(entity)) {
            return ICONS[EMPTY];
        } else if (UnitUtils.hasLowAmmo(entity)) {
            return ICONS[LOW];
        }
        return null;
    }
}
