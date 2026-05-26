/*
 * MekWars - Copyright (C) 2025
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import mekwars.common.campaign.CampaignOptions;
import mekwars.common.persistence.MWEntityStore;
import mekwars.common.util.Position;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CampaignDataTest {
    @Mock private Position position;

    @Mock private Influences influence;
    
    @Mock private CampaignOptions campaignOptions;

    private CampaignData data = new CampaignData(campaignOptions);

    @Test
    public void testAddPlanet() {
        Planet testPlanet = new Planet("TestPlanet1", position, influence);

        assertEquals(MWEntityStore.UNSET_ID, testPlanet.getId());
        data.addPlanet(testPlanet);
        assertEquals(1, testPlanet.getId());
    }

    @Test
    public void testAddHouse() {
        House testHouse = new House();

        assertEquals(MWEntityStore.UNSET_ID, testHouse.getId());
        data.addHouse(testHouse);
        assertEquals(1, testHouse.getId());
    }
}
