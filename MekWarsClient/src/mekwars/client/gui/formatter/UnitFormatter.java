/*
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

package mekwars.client.gui.formatter;

import megamek.common.Infantry;

import mekwars.client.MWClient;
import mekwars.client.campaign.CUnit;
import mekwars.common.CampaignData;
import mekwars.common.Unit;

public class UnitFormatter {
    /**
     * @return a smaller description
     */
    public static String getSmallDescription(CUnit unit) {
        if ((unit.getType() == Unit.MEK)
                || (unit.getType() == Unit.VEHICLE)
                || (unit.getType() == Unit.AERO)) {
            return unit.getModelName()
                    + " ["
                    + unit.getPilot().getGunnery()
                    + "/"
                    + unit.getPilot().getPiloting()
                    + "]";
        }

        if ((unit.getType() == Unit.INFANTRY) || (unit.getType() == Unit.BATTLEARMOR)) {
            if (((Infantry) unit.getEntity()).canMakeAntiMekAttacks()) {
                return unit.getModelName()
                        + " ["
                        + unit.getPilot().getGunnery()
                        + "/"
                        + unit.getPilot().getPiloting()
                        + "]";
            }
            return unit.getModelName() + " [" + unit.getPilot().getGunnery() + "]";
        }
        return unit.getModelName() + " [" + unit.getPilot().getGunnery() + "]";
    }

    public static String getDisplayInfo(MWClient mwclient, CUnit unit, String armyText) {
        String tinfo = "";

        if ((unit.getType() == Unit.MEK) && !unit.getEntity().isOmni()) {
            tinfo =
                    "<html><body>#"
                            + unit.getId()
                            + " "
                            + unit.getEntity().getChassis()
                            + ", "
                            + unit.getModelName();
        } else {
            tinfo = "<html><body>#" + unit.getId() + " " + unit.getModelName();
        }

        if ((unit.getType() == Unit.MEK)
                || (unit.getType() == Unit.VEHICLE)
                || (unit.getType() == Unit.AERO)) {
            tinfo +=
                    " ("
                            + unit.getPilot().getName()
                            + ", "
                            + unit.getPilot().getGunnery()
                            + "/"
                            + unit.getPilot().getPiloting()
                            + ") <br>";
        } else if ((unit.getType() == Unit.BATTLEARMOR) || (unit.getType() == Unit.INFANTRY)) {
            if (((Infantry) unit.getEntity()).canMakeAntiMekAttacks()) {
                tinfo +=
                        " ("
                                + unit.getPilot().getName()
                                + ", "
                                + unit.getPilot().getGunnery()
                                + "/"
                                + unit.getPilot().getPiloting()
                                + ") <br>";
            } else {
                tinfo +=
                        " ("
                                + unit.getPilot().getName()
                                + ", "
                                + unit.getPilot().getGunnery()
                                + ") <br>";
            }
        } else {
            tinfo +=
                    " ("
                            + unit.getPilot().getName()
                            + ", "
                            + unit.getPilot().getGunnery()
                            + ") <br>";
        }

        if (unit.getType() == Unit.VEHICLE) {
            tinfo += " Movement: " + unit.getEntity().getMovementModeAsString() + "<br>";
        }

        tinfo += "BV: ";

        if (CampaignData.cd.getCampaignOptions().getBooleanConfig("UseBaseBVForMatching")) {
            tinfo += unit.getBaseBV();
        } else {
            tinfo += unit.getBV();
        }

        // if (CampaignData.cd.getCampaignOptions().getBooleanConfig("RIGHTHERE")))
        if (Boolean.parseBoolean(mwclient.getConfigParam("ShowUnitBaseBV"))) {
            if (unit.getBV() != unit.getBaseBV()) {
                tinfo += " (" + unit.getBaseBV() + ")";
            }
        }
        tinfo +=
                " // Exp: "
                        + unit.getPilot().getExperience()
                        + " // Kills: "
                        + unit.getPilot().getKills()
                        + "<br> ";

        if (unit.getPilot().getSkills().size() > 0) {
            tinfo += "Skills: ";
            /*
             * Iterator it = unit.getPilot().getSkills().getSkillIterator(); while
             * (it.hasNext()) { tinfo += ((PilotSkill) it.next()).getName(); if
             * (it.hasNext()) tinfo += ", "; }
             */
            tinfo +=
                    unit.getPilot()
                            .getSkillString(
                                    false,
                                    mwclient.getData()
                                            .getHouseByName(mwclient.getPlayer().getHouse())
                                            .getBasePilotSkill(unit.getType()));
            tinfo += "<br>";
        }

        if (unit.getPilot().getHits() > 0) {
            tinfo += "Hits: " + Integer.toString(unit.getPilot().getHits()) + "<br>";
        }

        if (armyText != null && !armyText.isEmpty()) {
            tinfo += armyText + "<br>";
        }

        String capacity = unit.getEntity().getUnusedString();

        if ((capacity != null) && (capacity.trim().length() > 0)) {
            if (CampaignData.cd
                    .getCampaignOptions()
                    .getBooleanConfig("UseFullCapacityDescription")) {
                if (capacity.endsWith("<br>")) {
                    capacity = capacity.substring(0, capacity.length() - 4);
                }

                if (capacity.indexOf("<br>") > -1) {
                    tinfo += "Cargo:<br>" + capacity + "<br>";
                } else {
                    tinfo += "Cargo: " + capacity + "<br>";
                }
            } else if (capacity.startsWith("Troops")) {
                capacity = capacity.substring(9); // strip "Troops - " from
                // string
                tinfo += "Cargo: " + capacity + "<br>";
            }
        }

        if (unit.getLifeTimeRepairCost() > 0) {
            tinfo +=
                    "Repair Costs: "
                            + unit.getCurrentRepairCost()
                            + "/"
                            + unit.getLifeTimeRepairCost()
                            + "<br>";
        }
        tinfo += unit.getProducer();

        if ((unit.getScrappableFor() > 0)
                && !CampaignData.cd.getCampaignOptions().getBooleanConfig("UseAdvanceRepair")
                && !CampaignData.cd.getCampaignOptions().getBooleanConfig("UseSimpleRepair")) {
            tinfo +=
                    "<br><br><b>Scrap Value: "
                            + mwclient.moneyOrFluMessage(true, false, unit.getScrappableFor())
                            + "</b>";
        }

        tinfo += "</body></html>";
        return tinfo;
    }

    private UnitFormatter() {}
}
