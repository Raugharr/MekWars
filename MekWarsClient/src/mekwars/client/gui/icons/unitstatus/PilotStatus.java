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
import megamek.common.Mech;
import mekwars.client.campaign.CArmy;
import mekwars.client.campaign.CUnit;
import mekwars.client.gui.icons.UnitStatusIcon;

public class PilotStatus implements UnitStatus {
    private static final int NO_PILOT = 0;
    private static final int WOUND = 1;
    private static final int EJECT = 2;
    private static final int NO_EJECT = 3;

    private static final UnitStatusIcon[] ICONS = new UnitStatusIcon[] {
        new UnitStatusIcon("data/images/status/nopilot.gif") {
            @Override
            public String getToolTipText(ResourceBundle resourceBundle) {
                return resourceBundle.getString("unitStatus.nopilot.tooltip");
            }
        },
        new UnitStatusIcon("data/images/status/wound.gif") {
            @Override
            public String getToolTipText(ResourceBundle resourceBundle) {
                return resourceBundle.getString("unitStatus.wound.tooltip");
            }
        },
        new UnitStatusIcon("data/images/status/eject.gif") {
            @Override
            public String getToolTipText(ResourceBundle resourceBundle) {
                return resourceBundle.getString("unitStatus.eject.tooltip");
            }
        },
        new UnitStatusIcon("data/images/status/noeject.gif") {
            @Override
            public String getToolTipText(ResourceBundle resourceBundle) {
                return resourceBundle.getString("unitStatus.noeject.tooltip");
            }
        },
    };

    public UnitStatusIcon apply(CUnit unit, CArmy army) {
        Entity entity = unit.getEntity();
        if (unit.hasVacantPilot()) {
            return ICONS[NO_PILOT];
        } else if (unit.getPilot().getHits() > 0) {
            return ICONS[WOUND];
        } else if (entity instanceof Mech && ((Mech) entity).isAutoEject()) {
            return ICONS[EJECT];
        }
        return ICONS[NO_EJECT];
    }
}
