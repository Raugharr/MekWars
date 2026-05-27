/*
 * MekWars - Copyright (C) 2026
 *
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet) Original author Helge Richter (McWizard)
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */

package mekwars.common.entities;

import mekwars.common.House;
import mekwars.common.campaign.pilot.skills.PilotSkill;
import mekwars.common.campaign.pilot.skills.PilotSkillStore;
import mekwars.common.entities.factiontrait.Trait;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/** Represents a list of skills a pilot can obtain for a specific {@link House}. */
public class FactionTrait implements Entity {
    private static final Logger LOGGER = LogManager.getLogger(FactionTrait.class);
    private static int NEXT_ID = 1;

    private List<Trait> traitList = new ArrayList<>();
    private int id;
    private String name;

    public FactionTrait(String name, List<Trait> traitList) {
        this.id = NEXT_ID;
        this.name = name;
        NEXT_ID++;
        this.traitList.addAll(traitList);
    }

    /** Serialize a FactionTrait from a string. */
    public FactionTrait(String buffer) {
        StringTokenizer traitTokenizer = new StringTokenizer(buffer, "*");

        try {
            try {
                name = traitTokenizer.nextToken();
            } catch (NumberFormatException exception) {
                LOGGER.error("Unable to parse FactionTrait: expected integer.");
            }
            while (traitTokenizer.hasMoreElements()) {
                try {
                    int id = Integer.parseInt(traitTokenizer.nextToken());
                    int modifier = Integer.parseInt(traitTokenizer.nextToken());
                    PilotSkill pilotSkill = PilotSkillStore.getPilotSkill(id);

                    if (pilotSkill != null) {
                        traitList.add(new Trait(pilotSkill, modifier));
                    } else {
                        LOGGER.error(
                                "Unable to parse FactionTrait: invalid PilotSkill id '{}'.", id);
                    }
                } catch (NumberFormatException exception) {
                    LOGGER.error("Unable to parse FactionTrait: expected integer.");
                }
            }
        } catch (NoSuchElementException exception) {
            LOGGER.error("Unable to parse FactionTrait: expected token but recieved nothing.");
        }
    }

    public List<Trait> getTraits() {
        return traitList;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
        NEXT_ID = id + 1;
    }

    @Override
    public String getName() {
        return name;
    }

    public void serialize(StringBuilder builder) {
        builder.append(getName()).append("*");
        for (Trait trait : getTraits()) {
            trait.serialize(builder);
        }
    }
}
