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

package mekwars.common.net.data.packets;

import com.esotericsoftware.kryo.serializers.FieldSerializer.Bind;
import java.util.ArrayList;
import java.util.Collection;
import mekwars.common.entities.Entity;
import mekwars.common.net.AbstractPacket;
import mekwars.common.AdvancedTerrain;
import mekwars.common.CampaignData;
import mekwars.common.Terrain;
import mekwars.common.House;
import mekwars.common.Planet;
import mekwars.common.serializers.FixedArraySerializer;

/**
 * Packet responsible for updating a {@link DataClient} entity list.
 */
public class UpdateEntities extends AbstractPacket {
    private ArrayList<Terrain> terrains = new ArrayList<Terrain>();
    private ArrayList<AdvancedTerrain> advancedTerrains = new ArrayList<AdvancedTerrain>();
    private ArrayList<House> houses = new ArrayList<House>();
    private ArrayList<Planet> planets = new ArrayList<Planet>();

    public UpdateEntities() { }
    
    /**
     * Serializes a {@link CampaignData} into an UpdateEntities packet.
     */
    public UpdateEntities(CampaignData campaignData) {
        for (Terrain terrain : campaignData.getAllTerrains()) {
            terrains.add(terrain);
        }

        for (AdvancedTerrain advancedTerrain : campaignData.getAllAdvancedTerrains()) {
            advancedTerrains.add(advancedTerrain);
        }

        for (House house : campaignData.getAllHouses()) {
            houses.add(house);
        }

        for (Planet planet : campaignData.getAllPlanets()) {
            planets.add(planet);
        }
    }
    
    @Override
    public DataPacketType getType() {
        return DataPacketType.UPDATE_ENTITIES;
    }

    // public Iterator<Terrain> getTerrain() {
    //     return terrains.iterator();
    // }

    // public Iterator<AdvancedTerrain> getAdvancedTerrain() {
    //     return advancedTerrains.iterator();
    // }

    // public Iterator<House> getFactions() {
    //     return factions.iterator();
    // }

    // public Iterator<Planet> getPlanets() {
    //     return planets.iterator();
    // }

    // // public Iterator<Operation> getOperations() {
    // //     return operations.iterator();
    // // }
    
    /**
     * Updates a CampaignData to include all of the entities in the UpdateEntities packet.
     *
     * @param campaignData the {@link CampaignData to update}.
     */
    public void update(CampaignData campaignData) {
        campaignData.updateTerrains(terrains);
        campaignData.updateAdvancedTerrains(advancedTerrains);
        campaignData.updateHouses(houses);
        campaignData.updatePlanets(planets);
    }
}
