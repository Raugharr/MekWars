/*
 * MekWars - Copyright (C) 2004
 *
 * Original author - nmorris (urgru@users.sourceforge.net)
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
package mekwars.server.campaign.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.spy;

import mekwars.common.CampaignData;
import mekwars.common.campaign.CampaignOptions;
import mekwars.common.campaign.operations.Operation;
import mekwars.server.MWServ;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SArmy;
import mekwars.server.campaign.SHouse;
import mekwars.server.campaign.SPlayer;
import mekwars.server.campaign.operations.newopmanager.I_OperationManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
public class OpponentListHelperTest {
    private OpponentListHelper helper;
    @Mock private SPlayer searchPlayer;
    @Mock private SHouse searchHouse;
    @Mock private SArmy searchArmy;
    @Mock private ExclusionList searchExclusionList;
    @Mock private SPlayer enemyPlayer;
    @Mock private SHouse enemyHouse;
    @Mock private SArmy enemyArmy;
    @Mock private ExclusionList enemyExclusionList;
    @Mock private I_OperationManager opManager;
    @Mock private CampaignMain campaignMain;
    @Mock private CampaignOptions campaignOptions;
    @Mock private CampaignData campaignData;
    @Mock private MWServ mwServ;

    private MockedStatic<MWServ> staticMWServ;

    @BeforeEach
    public void setup() {
        CampaignMain.cm = campaignMain;
        CampaignData.cd = campaignData;
        staticMWServ = mockStatic(MWServ.class);

        staticMWServ.when(MWServ::getInstance).thenReturn(mwServ);
    }

    @AfterEach
    public void teardown() {
        staticMWServ.close();
    }

    @Nested
    public class MatchedEnemyArmiesTest {
        @BeforeEach
        public void setup() {
            when(searchPlayer.getHouseFightingFor()).thenReturn(searchHouse);

            when(enemyPlayer.getHouseFightingFor()).thenReturn(enemyHouse);
        }

        @Nested
        public class WithArmies {
            @Mock private Operation operation;

            @Test
            public void testReturnsEmptyWhenDisabled() {
                when(searchArmy.isDisabled()).thenReturn(true);
                when(searchPlayer.getArmies()).thenReturn(List.of(searchArmy));

                helper = new OpponentListHelper(searchPlayer);
                List<SArmy> matched = helper.matchedEnemyArmies(enemyPlayer);

                assertTrue(matched.isEmpty());
            }

            @Test
            public void testReturnsEmptyWhenEnabled() {
                when(searchArmy.isDisabled()).thenReturn(false);
                when(searchPlayer.getArmies()).thenReturn(List.of(searchArmy));
                when(enemyPlayer.getArmies()).thenReturn(List.of(enemyArmy));

                when(campaignMain.getOpsManager()).thenReturn(opManager);
                when(searchArmy.getLegalOperations()).thenReturn(Set.of("Assault"));
                when(opManager.getOperation(eq("Assault"))).thenReturn(operation);

                when(searchArmy.matches(enemyArmy, operation)).thenReturn(true);

                helper = new OpponentListHelper(searchPlayer);
                List<SArmy> matched = helper.matchedEnemyArmies(enemyPlayer);

                assertEquals(1, matched.size());
                assertEquals(matched.get(0), enemyArmy);
                verify(searchArmy).addOpponent(enemyArmy);
                verify(enemyArmy).addOpponent(searchArmy);
            }
        }

        @Nested
        public class WithoutArmies {
            @Test
            public void testReturnsEmptyWhenNoEnemies() {
                helper = new OpponentListHelper(searchPlayer);
                List<SArmy> matched = helper.matchedEnemyArmies(enemyPlayer);

                assertTrue(matched.isEmpty());
            }
        }
    }
}
