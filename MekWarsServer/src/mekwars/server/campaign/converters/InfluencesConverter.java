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

package server.campaign.converters;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import common.Influences;
import common.UnitFactory;
import java.util.HashMap;
import server.campaign.SHouse;
import server.campaign.CampaignMain;

public class InfluencesConverter implements Converter {
    public boolean canConvert(Class clazz) {
        return clazz.equals(Influences.class);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        // TODO: Implement
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        HashMap<Integer, Integer> influences = new HashMap<Integer, Integer>();

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String nodeName = reader.getNodeName();

            if (nodeName.equals("inf")) {
                addInfluence(reader, influences);
            }
            reader.moveUp();
        }
        return new Influences(influences);
    }

    public void addInfluence(HierarchicalStreamReader reader, HashMap<Integer, Integer> influences) {
        String factionName = null;
        int influence = 0;

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String nodeName = reader.getNodeName();
            if (nodeName.equals("faction")) {
                factionName = reader.getValue();
            } else if (nodeName.equals("amount")) {
                influence = Integer.parseInt(reader.getValue());
            }
            reader.moveUp();
        }
        if (factionName == null) {
            throw new ConversionException("faction expected");
        }
    	SHouse house = CampaignMain.cm.getHouseFromPartialString(factionName, null);
        if (house == null) {
            throw new ConversionException("unable to find faction '" + factionName + "'");
        }
        influences.put(house.getId(), influence);
    }
}
