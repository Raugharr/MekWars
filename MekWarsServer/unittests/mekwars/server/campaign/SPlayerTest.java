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

package mekwars.server.campaign;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import mekwars.common.CampaignData;
import mekwars.common.campaign.CampaignOptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Hashtable;

@ExtendWith(MockitoExtension.class)
class SPlayerTest {
    @Mock private SHouse house;

    @Mock private CampaignMain campaignMain;

    @Mock private CampaignData campaignData;

    @Mock private CampaignOptions campaignOptions;

    private SPlayer player;

    @BeforeEach
    public void setup() {
        CampaignMain.cm = campaignMain;
        CampaignData.cd = campaignData;
        when(CampaignMain.cm.getIntegerConfig("NoPlayListSize")).thenReturn(5);
        when(campaignData.getCampaignOptions()).thenReturn(campaignOptions);
        when(campaignOptions.getConfig("NewbieHouseName")).thenReturn("NoHouse");
        when(CampaignData.cd.getHouseByName("NoHouse")).thenReturn(house);

        player = spy(new SPlayer());
    }

    @Test
    public void testAddMoney_PreventsNegativeBalance() {
        when(house.getIntegerConfig("MaxSOLCBills")).thenReturn(1_000);

        player.addMoney(100);
        player.addMoney(-150);

        assertEquals(0, player.getMoney());
    }

    @Nested
    class GetRemainingMekTokensTest {
        @Test
        public void testCalculatesCorrectly() {
            int freeBuildLimit = 10;
            when(house.getConfig("FreeBuild_Limit")).thenReturn(Integer.toString(freeBuildLimit));
            when(house.getIntegerConfig("FreeBuild_Limit")).thenReturn(freeBuildLimit);
            doNothing().when(CampaignMain.cm).toUser("PL|UMT|3", "", false);
            doNothing().when(player).setSave();

            player.addMekTokens(3);

            assertEquals(7, player.getRemainingMekTokens());
            assertEquals(10, player.getMekTokenLimit());
        }

        @Test
        public void testHasUnusedReturnsTrue() {
            when(house.getConfig("FreeBuild_Limit")).thenReturn("5");

            player.addMekTokens(2);

            assertTrue(player.hasUnusedMekTokens());
        }
    }

    @Nested
    class AddExperienceTest {
        private Hashtable<String, SmallPlayer> smallPlayers = new Hashtable<>();

        @BeforeEach
        public void setup() {
            when(player.getMyHouse()).thenReturn(house);
            when(player.getMyHouse().getSmallPlayers()).thenReturn(smallPlayers);
        }

        @Test
        public void testRewardRolloverTriggersRewardPoints() {
            when(house.getIntegerConfig("MinimumHouseBays")).thenReturn(20);
            when(house.getIntegerConfig("XPRollOverCap")).thenReturn(100);
            when(house.getIntegerConfig("FluXPRollOverCap")).thenReturn(0);
            when(campaignMain.getConfig("RPShortName")).thenReturn("RP");
            doNothing().when(campaignMain).toUser(anyString(), anyString(), anyBoolean());

            /*
             * Start with 90 XP, add 20 -> 110. Should roll over 100, grant 1 RP, reset counter to 10
             */
            player.addExperience(90, false);
            player.addExperience(20, false);

            assertEquals(10, player.getXpTillReward());
            verify(player).addRewardPoints(1);
            verify(player).setXpTillReward(10);
        }

        @Test
        public void testRewardRollver_DoesNotTriggerRewardPoints() {
            when(house.getIntegerConfig("MinimumHouseBays")).thenReturn(20);
            when(house.getIntegerConfig("XPRollOverCap")).thenReturn(100);
            when(house.getIntegerConfig("FluXPRollOverCap")).thenReturn(0);
            doNothing().when(campaignMain).toUser(anyString(), anyString(), anyBoolean());

            /*
             * Start with 90 XP, add 20 -> 110. Should roll over 100, but does not grant an RP because
             * a moderator gave experience.
             */
            player.addExperience(90, true);
            player.addExperience(20, false);

            assertEquals(20, player.getXpTillReward());
            verify(player, never()).addRewardPoints(1);
            verify(player).setXpTillReward(20);
        }

        @Test
        public void testFluRolloverTriggersInfluence() {
            when(house.getIntegerConfig("MinimumHouseBays")).thenReturn(20);
            when(house.getIntegerConfig("XPRollOverCap")).thenReturn(0);
            when(house.getIntegerConfig("FluXPRollOverCap")).thenReturn(100);
            when(campaignMain.getConfig("FluShortName")).thenReturn("FLU");
            doNothing().when(campaignMain).toUser(anyString(), anyString(), anyBoolean());

            /*
             * Start with 90 XP, add 20 -> 110. Should roll over 100, grant 1 FLU, reset counter to 10
             */
            player.addExperience(90, false);
            player.addExperience(20, false);

            assertEquals(10, player.getXpTillFlu());
            verify(player).addInfluence(1);
            verify(player).setXpTillFlu(10);
        }

        @Test
        public void testFluRollover_DoesNotTriggersInfluence() {
            when(house.getIntegerConfig("MinimumHouseBays")).thenReturn(20);
            when(house.getIntegerConfig("XPRollOverCap")).thenReturn(0);
            when(house.getIntegerConfig("FluXPRollOverCap")).thenReturn(100);
            doNothing().when(campaignMain).toUser(anyString(), anyString(), anyBoolean());

            /*
             * Start with 90 XP, add 20 -> 110. Should roll over 100, but does not grant a FLU because a moderator gave experience.
             */
            player.addExperience(90, true);
            player.addExperience(20, false);

            assertEquals(20, player.getXpTillFlu());
            verify(player, never()).addInfluence(1);
            verify(player).setXpTillFlu(20);
        }

        @Test
        public void testPreventsNegative() {
            player.addExperience(-50, false);

            assertEquals(0, player.getExperience());
        }
    }
}
