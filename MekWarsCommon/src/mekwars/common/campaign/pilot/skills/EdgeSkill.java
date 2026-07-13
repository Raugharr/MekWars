/*
 * MekWars - Copyright (C) 2004
 *
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet)
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

/*
 * Created on 18.04.2004
 *
 */
package mekwars.common.campaign.pilot.skills;

import mekwars.common.CampaignData;
import mekwars.common.Unit;
import mekwars.common.campaign.pilot.Pilot;

/**
 * Reduces the bay-consume of the unit.
 *
 * @author Helge Richter
 */
public class EdgeSkill extends PilotSkill {
    boolean edgeWhenTac = true;
    boolean edgeWhenKo = true;
    boolean edgeWhenHeadhit = true;
    boolean edgeWhenExplosion = true;

    public EdgeSkill(int id) {
        super(id, "Edge", "ED");
        setDescription("Allows Pilot to reroll 1 roll(per level) per game.");
    }

    public EdgeSkill() {
        // TODO: Remove when no longer necessary
    }

    @Override
    public void modifyPilot(Pilot p) {
        p.addMegamekOption("edge", true);
    }

    @Override
    public int getChance(int unitType, Pilot p) {
        int maxEdgeChances =
                CampaignData.cd.getCampaignOptions().getIntegerConfig("MaxEdgeChanges");

        if (unitType != Unit.MEK) {
            return 0;
        }

        if (p.getSkills().has(PilotSkill.EdgeSkillID)
                && (p.getSkills().getPilotSkill(PilotSkill.EdgeSkillID).getLevel()
                        > maxEdgeChances)) {
            return 0;
        }

        return super.getChance(unitType, p);
    }

    @Override
    public void addToPilot(Pilot pilot) {
        // this.setLevel(1);
        pilot.getSkills().add(this);
    }

    /**
     * @param level The level to set.
     */
    @Override
    public void setLevel(int level) {
        if (level < 1) {
            super.setLevel(1);
        } else {
            super.setLevel(level);
        }
    }

    public boolean getTac() {
        return edgeWhenTac;
    }

    public boolean getKO() {
        return edgeWhenKo;
    }

    public boolean getHeadHit() {
        return edgeWhenHeadhit;
    }

    public boolean getExplosion() {
        return edgeWhenExplosion;
    }

    public void setTac(boolean value) {
        edgeWhenTac = value;
    }

    public void setKO(boolean value) {
        edgeWhenKo = value;
    }

    public void setHeadHit(boolean value) {
        edgeWhenHeadhit = value;
    }

    public void setExplosion(boolean value) {
        edgeWhenExplosion = value;
    }
}
