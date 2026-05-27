/*
 * MekWars - Copyright (C) 2004
 *
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet)
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */

/*
 * Created on 18.04.2004
 */
package mekwars.common.campaign.pilot.skills;

import megamek.common.Entity;

import mekwars.common.CampaignData;
import mekwars.common.House;
import mekwars.common.Unit;
import mekwars.common.campaign.pilot.Pilot;
import mekwars.common.entities.FactionTrait;
import mekwars.common.io.file.FactionTraitFile;
import mekwars.common.util.RandomUtils;

import java.util.List;

/** Pilot traits for use with moding the gaining of other traits @@author Torren (Jason Tighe) */
public class TraitSkill extends PilotSkill {

    public TraitSkill(int id) {
        super(id, "Trait", "TN");
        setDescription("Pilot traits for use with moding the gaining of other skills");
    }

    public TraitSkill() {
        // TODO: replace with ReflectionProvider
    }

    @Override
    public int getChance(int unitType, Pilot pilot) {
        if (pilot.getSkills().has(this)) {
            return 0;
        }

        String chance = "chancefor" + getAbbreviation() + "for" + Unit.getTypeClassDesc(unitType);

        return pilot.getHouse().getHouseOptions().getIntegerConfig(chance);
    }

    @Override
    public int getBVMod(Entity unit, Pilot pilot) {
        return 0;
    }

    public void assignTrait(Pilot p) {
        FactionTraitFile factionTraitFile = CampaignData.cd.getFactionTraitFileByHouse(p.getHouse().getName());

        if (factionTraitFile == null) {
            return;
        }
        List<FactionTrait> traitNames = factionTraitFile.getFactionTraits();
        int size = traitNames.size();

        if (size < 1) {
            return;
        }

        FactionTrait traits = null;
        if (size == 1) {
            traits = traitNames.get(0);
        } else {
            traits = traitNames.get(RandomUtils.getRandomNumber(size));
        }
        p.setTraitName(traits.getName());
    }
}
