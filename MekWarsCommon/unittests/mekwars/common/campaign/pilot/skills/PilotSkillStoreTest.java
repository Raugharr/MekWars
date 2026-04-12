/*
 * MekWars - Copyright (C) 2026
 *
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet)
 * Original author Helge Richter (McWizard)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 */

package mekwars.common.campaign.pilot.skills;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import mekwars.common.CampaignData;
import mekwars.common.House;
import mekwars.common.campaign.CampaignOptions;
import mekwars.common.campaign.HouseOptions;
import mekwars.common.campaign.pilot.Pilot;
import mekwars.common.entities.FactionTrait;
import mekwars.common.entities.factiontrait.Trait;
import mekwars.common.io.file.FactionTraitFile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class PilotSkillStoreTest {

    @Mock private FactionTraitFile factionTraitFile;

    @Mock private FactionTrait factionTrait;

    @Mock private Pilot pilot;

    @Mock private PilotSkills pilotSkills;

    @Mock private PilotSkill traitPilotSkill;

    @Mock private CampaignData campaignData;

    @Mock private CampaignOptions campaignOptions;

    @Mock private House house;

    @Mock private HouseOptions houseOptions;

    @BeforeEach
    public void setUp() {
        PilotSkillStore.initializePilotSkills();
    }

    @Nested
    public class GetRandomSkillTest {
        @Test
        public void testNullPilot() {
            PilotSkill skill = PilotSkillStore.getRandomSkill(null, 0);
            assertNull(skill);
        }

        @Nested
        public class PilotTraitTest {
            @BeforeEach
            public void setup() {
                CampaignData.cd = campaignData;
                when(pilot.getHouse()).thenReturn(house);
                when(campaignData.getCampaignOptions()).thenReturn(campaignOptions);
                when(campaignOptions.getIntegerConfig("MaxEdgeChanges")).thenReturn(1);
                when(house.getHouseOptions()).thenReturn(houseOptions);
            }

            @Nested
            public class WithTrait {
                @BeforeEach
                public void setup() {
                    when(house.getName()).thenReturn("Comstar");
                    when(houseOptions.getIntegerConfig(anyString())).thenReturn(0);
                    when(pilot.getSkills()).thenReturn(pilotSkills);
                    when(pilotSkills.has(PilotSkill.TraitID)).thenReturn(true);
                    when(pilot.getTraitName()).thenReturn("Dodge Maneuver");
                    when(campaignData.getFactionTraitFileByHouse(anyString())).thenReturn(factionTraitFile);
                    when(factionTraitFile.getFactionTraitByName(pilot.getTraitName())).thenReturn(factionTrait);
                }

                @Test
                public void testPilotHavingTrait() {
                    when(factionTrait.getTraits()).thenReturn(List.of(new Trait(traitPilotSkill, 100)));
                    when(traitPilotSkill.getId()).thenReturn(PilotSkill.DodgeManeuverSkillID);

                    PilotSkill skill = PilotSkillStore.getRandomSkill(pilot, 0);

                    assertEquals(traitPilotSkill.getId(), skill.getId());
                }

                @Test
                public void testPilotWithMultipleWeightedTraits() {
                    when(factionTrait.getTraits())
                            .thenReturn(
                                    List.of(
                                            new Trait(traitPilotSkill, 90),
                                            new Trait(traitPilotSkill, 10)));
                    when(traitPilotSkill.getId()).thenReturn(PilotSkill.DodgeManeuverSkillID);

                    int highWeightCount = 0;
                    int iterations = 100;

                    for (int i = 0; i < iterations; i++) {
                        PilotSkill skill = PilotSkillStore.getRandomSkill(pilot, 0);
                        if (skill.equals(traitPilotSkill)) {
                            highWeightCount++;
                        }
                    }

                    double highRatio = (double) highWeightCount / iterations;

                    assertTrue(highRatio > 0.85);
                }
            }

            @Test
            public void testPilotWithoutTraits() {
                when(houseOptions.getIntegerConfig(anyString())).thenReturn(100);
                when(pilot.getSkills()).thenReturn(pilotSkills);
                when(pilotSkills.has(PilotSkill.TraitID)).thenReturn(false);

                PilotSkill skill = PilotSkillStore.getRandomSkill(pilot, 0);

                assertNotNull(skill);
                assertNotEquals(PilotSkill.TraitID, skill.getId());
            }
        }
    }
}
