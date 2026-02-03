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

import java.util.ResourceBundle;
import mekwars.client.MWClient;
import mekwars.client.campaign.CArmy;
import mekwars.client.campaign.CUnit;
import mekwars.common.Unit;
import mekwars.common.util.UnitUtils;
import mekwars.client.gui.icons.UnitStatusIcon;

public class RepairStatus implements UnitStatus {
    private static final int REPAIRING = 0;
    private static final int PENDING = 1;
    private static final int UNMAINTAINED = 2;
    private static final int MAINTAINED = 3;

    private static final UnitStatusIcon[] ICONS = new UnitStatusIcon[] {
        new UnitStatusIcon("data/images/status/repairing.gif") {
            @Override
            public String getToolTipText(ResourceBundle resourceBundle) {
                return resourceBundle.getString("unitStatus.repairing.tooltip");
            }
        },
        new UnitStatusIcon("data/images/status/pending.gif") {
            @Override
            public String getToolTipText(ResourceBundle resourceBundle) {
                return resourceBundle.getString("unitStatus.pending.tooltip");
            }
        },
        new UnitStatusIcon("data/images/status/unmaint.gif") {
            @Override
            public String getToolTipText(ResourceBundle resourceBundle) {
                return resourceBundle.getString("unitStatus.unmaintenance.tooltip");
            }
        },
        new UnitStatusIcon("data/images/status/maint.gif") {
            @Override
            public String getToolTipText(ResourceBundle resourceBundle) {
                return resourceBundle.getString("unitStatus.maintenance.tooltip");
            }
        },
    };
    private MWClient mwClient;

    public RepairStatus(MWClient mwClient) {
        this.mwClient = mwClient;
    }

    public UnitStatusIcon apply(CUnit unit, CArmy army) {
        if (mwClient.isUsingAdvanceRepairs()) {
            if (UnitUtils.isRepairing(unit.getEntity())) {
                return ICONS[REPAIRING];
            } else if (mwClient.getRMT() != null && mwClient.getRMT().hasQueuedOrders(unit.getId())) {
                return ICONS[PENDING];
            }
        } else {
            if (unit.getStatus() == Unit.STATUS_UNMAINTAINED) {
                return ICONS[UNMAINTAINED];
            } else {
                return ICONS[MAINTAINED];
            }
        }
        return null;
    }

    public String getToolTipText() {
        return "unitStatus.repair.tooltip";
    }
}
