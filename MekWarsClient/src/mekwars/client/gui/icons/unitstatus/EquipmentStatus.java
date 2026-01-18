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
import mekwars.client.campaign.CArmy;
import mekwars.client.campaign.CUnit;
import mekwars.common.util.UnitUtils;
import mekwars.client.gui.icons.UnitStatusIcon;

public class EquipmentStatus implements UnitStatus {
    private static final int CRITICAL = 0;

    private static final UnitStatusIcon[] ICONS = new UnitStatusIcon[] {
        new UnitStatusIcon("data/images/status/critical.gif") {
            @Override
            public String getToolTipText(ResourceBundle resourceBundle) {
                return resourceBundle.getString("unitStatus.critical.tooltip");
            }
        },
    };

    public UnitStatusIcon apply(CUnit unit, CArmy army) {
        if (UnitUtils.hasCriticalDamage(unit.getEntity())) {
            return ICONS[CRITICAL];
        }
        return null;
    }
}
