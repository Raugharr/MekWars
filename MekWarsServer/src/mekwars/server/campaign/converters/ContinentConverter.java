/*
 * MekWars - Copyright (C) 2004 
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

package mekwars.server.campaign.converters;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import mekwars.common.AdvancedTerrain;
import mekwars.common.Continent;
import mekwars.common.Terrain;

import mekwars.server.campaign.CampaignMain;

public class ContinentConverter implements Converter {
    public boolean canConvert(Class clazz) {
        return clazz.equals(Continent.class);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer,
            MarshallingContext context) {
        Continent continent = (Continent) source;

        writer.startNode("size");
        writer.setValue(Integer.toString(continent.getSize()));
        writer.endNode();

        writer.startNode("terrain");
        writer.setValue(continent.getEnvironment().getName());
        writer.endNode();

        writer.startNode("advancedTerrain");
        writer.setValue(continent.getAdvancedTerrain().getName());
        writer.endNode();
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        int size = 0;
        String terrainName = null;
        String advancedTerrainName = null;

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String nodeName = reader.getNodeName();
            if (nodeName.equals("size")) {
                size = Integer.parseInt(reader.getValue());
            } else if (nodeName.equals("terrain")) {
                terrainName = reader.getValue();
            } else if (nodeName.equals("advancedTerrain")) {
                advancedTerrainName = reader.getValue();
            }
            reader.moveUp();
        }
        
        Terrain terrain = CampaignMain.cm.getData().getTerrainByName(terrainName);
        if (terrain == null) {
            throw new ConversionException("terrain '" + terrainName + "' not found");
        }
        AdvancedTerrain advancedTerrain = CampaignMain.cm.getData().getAdvancedTerrainByName(advancedTerrainName);
        if (advancedTerrain == null) {
            throw new ConversionException("advancedTerrain '" + advancedTerrainName + "' not found");
        }
        return new Continent(size, terrain, advancedTerrain);
    }
}
