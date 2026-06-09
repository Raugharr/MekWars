/*
 * MekWars - Copyright (C) 2026
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

package mekwars.server.campaign;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import mekwars.common.CampaignData;
import mekwars.common.Unit;
import mekwars.common.campaign.BasePilotStats;
import mekwars.common.campaign.CampaignOptions;
import mekwars.server.campaign.pilot.SPilot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class PilotQueuesTest {
    @Mock private SHouse house;

    @Mock private CampaignMain campaignMain;

    @Mock private CampaignData campaignData;

    @Mock private CampaignOptions campaignOptions;

    private PilotQueues pilotQueues;
    private BasePilotStats basePilotStats;

    @BeforeEach
    public void setup() {
        CampaignData.cd = campaignData;
        CampaignMain.cm = campaignMain;

        pilotQueues = new PilotQueues(house);
        basePilotStats = new BasePilotStats();
    }

    @Nested
    class AddPilotTest {
        @Test
        public void testNullifyVacantPilot() {
            SPilot vacantPilot = new SPilot(house, "Vacant", 4, 5);
            int initialSize = pilotQueues.getQueueSize(Unit.MEK);

            pilotQueues.addPilot(Unit.MEK, vacantPilot, false);

            assertEquals(initialSize, pilotQueues.getQueueSize(Unit.MEK));
        }

        @Test
        public void testSkipSkillAdjustmentWhenFlagTrue() {
            SPilot pilot = new SPilot(house, "Pilot", 3, 4);

            pilotQueues.addPilot(Unit.MEK, pilot, true);

            assertEquals(3, pilot.getGunnery());
            assertEquals(4, pilot.getPiloting());
        }

        @Test
        public void testNotReduceSkillsWhenDisabled() {
            when(CampaignData.cd.getCampaignOptions()).thenReturn(campaignOptions);
            when(campaignOptions.getBooleanConfig("ReduceSkillsInQue")).thenReturn(false);

            SPilot pilot = new SPilot(house, "pilot", 3, 4);

            pilotQueues.addPilot(Unit.MEK, pilot, false);

            assertEquals(3, pilot.getGunnery());
            assertEquals(4, pilot.getPiloting());
        }

        @Test
        public void testAddToCorrectTypeQueue() {
            SPilot mechPilot = new SPilot(house, "MechPilot", 4, 5);
            SPilot vehiclePilot = new SPilot(house, "VehiclePilot", 4, 5);

            pilotQueues.addPilot(Unit.MEK, mechPilot, true);
            pilotQueues.addPilot(Unit.VEHICLE, vehiclePilot, true);

            assertEquals(1, pilotQueues.getQueueSize(Unit.MEK));
            assertEquals(1, pilotQueues.getQueueSize(Unit.VEHICLE));
        }
    }

    @Nested
    class GetPilotTest {
        @BeforeEach
        public void setup() {
            when(house.getBasePilotStats()).thenReturn(basePilotStats);
            when(CampaignData.cd.getCampaignOptions()).thenReturn(campaignOptions);
            when(campaignOptions.getIntegerConfig("BornSkillChance")).thenReturn(5);
        }

        @Test
        public void testReturnExistingPilotFromQueue() {
            List<SPilot> queue = pilotQueues.getPilotQueue(Unit.MEK);
            SPilot pilot = new SPilot(house, "TestPilot", 4, 5);
            pilotQueues.addPilot(Unit.MEK, pilot);

            when(campaignMain.getRandomNumber(anyInt())).thenReturn(0);

            SPilot result = pilotQueues.getPilot(Unit.MEK);

            assertSame(pilot, result);
            assertEquals(PilotQueues.MIN_PILOTS - 1, queue.size());
        }

        @Test
        public void testRefillQueueToTenBeforeReturning() {
            when(campaignMain.getRandomNumber(anyInt())).thenReturn(0);

            SPilot result = pilotQueues.getPilot(Unit.MEK);

            assertNotNull(result);
            assertEquals(PilotQueues.MIN_PILOTS - 1, pilotQueues.getQueueSize(Unit.MEK));
        }
    }
}
