/*
 * MekWars - Copyright (C) 2026
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

package mekwars.common;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import mekwars.common.campaign.CampaignOptions;
import mekwars.common.campaign.HouseOptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlayerTest {
    @Mock private CampaignData campaignData;

    @Mock private CampaignOptions campaignOptions;

    @Mock private HouseOptions houseOptions;

    @Mock private House house;

    private Player player;

    @BeforeEach
    void setup() {
        player = Mockito.mock(Player.class, Mockito.CALLS_REAL_METHODS);

        player.setMyHouse(house);
        when(campaignData.getCampaignOptions()).thenReturn(campaignOptions);
    }

    @Nested
    class DoPayTechniciansMathTest {
        @BeforeEach
        void setup() {
            CampaignData.cd = campaignData;
            when(campaignOptions.getIntegerConfig("MaxTechsToHire")).thenReturn(-1);
        }

        @Test
        void testZeroTechnicians() {
            player.setTechnicians(0);
            player.doPayTechniciansMath();

            // 0 technician = 0
            assertEquals(0, player.getCurrentTechPayment());
        }

        @Test
        void testNegativeAdditivePertech() {
            player.setTechnicians(1);
            when(houseOptions.getFloatConfig("AdditivePerTech")).thenReturn(-0.04f);
            when(houseOptions.getFloatConfig("AdditiveCostCeiling")).thenReturn(1.20f);
            when(house.getHouseOptions()).thenReturn(houseOptions);

            player.doPayTechniciansMath();

            // Invalid AdditivePerTech = 0
            assertEquals(0, player.getCurrentTechPayment());
        }

        @Nested
        class WithTechniciansTest {
            @BeforeEach
            void setup() {
                when(houseOptions.getFloatConfig("AdditivePerTech")).thenReturn(0.04f);
                when(houseOptions.getFloatConfig("AdditiveCostCeiling")).thenReturn(1.20f);
                when(houseOptions.getBooleanConfig("UseSlidingHangarLimits")).thenReturn(false);
                when(house.getHouseOptions()).thenReturn(houseOptions);
            }

            @Test
            void testOneTechnician() {
                player.setTechnicians(1);
                player.doPayTechniciansMath();

                // 1 technician = 1 * 0.04 = 0.04, rounded = 0
                assertEquals(0, player.getCurrentTechPayment());
            }

            @Test
            void testMultipleTechniciansBelowCeiling() {
                player.setTechnicians(5);
                player.doPayTechniciansMath();

                // 1 + 2 + 3 + 4 + 5 = 15, 15 * 0.04 = 0.6, rounded = 1
                assertEquals(1, player.getCurrentTechPayment());
            }

            @Test
            void testTechniciansAboveCeiling() {
                player.setTechnicians(35);
                player.doPayTechniciansMath();

                // First 30 techs: 1+2+...+30 = 465, 465 * 0.04 = 18.6
                // Next 5 techs: 5 * 1.20 = 6.0
                // Total: 18.6 + 6.0 = 24.6, rounded = 25
                assertEquals(25, player.getCurrentTechPayment());
            }
        }
    }
}
