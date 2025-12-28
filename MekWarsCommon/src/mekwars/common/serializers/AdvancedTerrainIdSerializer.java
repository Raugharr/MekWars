/*
 * MekWars - Copyright (C) 2025
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */

package mekwars.common.serializers;

import mekwars.common.AdvancedTerrain;
import mekwars.common.CampaignData;
import mekwars.common.entities.Entity;

public class AdvancedTerrainIdSerializer extends IdSerializer {
    public AdvancedTerrainIdSerializer() {
        super(AdvancedTerrain.class);
    }

    protected Entity getId(int id) {
        return CampaignData.cd.getAdvancedTerrain(id);
    }
}
