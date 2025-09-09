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
import java.util.TreeMap;
import java.util.Vector;
import mekwars.common.CampaignData;
import mekwars.common.Continent;
import mekwars.common.House;
import mekwars.common.Influences;
import mekwars.common.PlanetEnvironments;
import mekwars.common.UnitFactory;
import mekwars.server.campaign.SPlanet;
import mekwars.server.campaign.SUnitFactory;

public class SPlanetConverter implements Converter {
    public boolean canConvert(Class clazz) {
        return clazz.equals(SPlanet.class);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer,
            MarshallingContext context) {
        SPlanet planet = (SPlanet) source;

        for (Continent continent : planet.getEnvironments().toArray()) {
            writer.startNode("continent");
            context.convertAnother(continent);
            writer.endNode();
        }

        writer.startNode("name");
        writer.setValue(planet.getName());
        writer.endNode();

        writer.startNode("xCoord");
        writer.setValue(Double.toString(planet.getPosition().x));
        writer.endNode();

        writer.startNode("yCoord");
        writer.setValue(Double.toString(planet.getPosition().y));
        writer.endNode();

        writer.startNode("componentProduction");
        writer.setValue(Integer.toString(planet.getCompProduction()));
        writer.endNode();

        writer.startNode("influence");
        context.convertAnother(planet.getInfluence());
        writer.endNode();

        for (UnitFactory unitFactory : planet.getUnitFactories()) {
            writer.startNode("unitFactory");
            context.convertAnother(unitFactory);
            writer.endNode();
        }

        writer.startNode("isHomeworld");
        writer.setValue(Boolean.toString(planet.isHomeWorld()));
        writer.endNode();

        writer.startNode("originalOwner");
        writer.setValue(planet.getOriginalOwner());
        writer.endNode();

        if (planet.getPlanetFlags().size() > 0) {
            writer.startNode("operationFlags");
            for (String key : planet.getPlanetFlags().keySet()) {
                writer.startNode("operationFlag");

                writer.startNode("key");
                writer.setValue(key);
                writer.endNode();

                writer.startNode("value");
                writer.setValue(planet.getPlanetFlags().get(key));
                writer.endNode();

                writer.endNode();
            }
            writer.endNode();
        }
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        PlanetEnvironments planetEnvironments = new PlanetEnvironments();
        String xcoord = null;
        String ycoord = null;
        String name = null;
        int componentProduction = 0;
        Influences influences = null;
        Vector<UnitFactory> unitFactoryList = new Vector<UnitFactory>();
        boolean isHomeworld = false;
        String originalOwnerString = null;
        TreeMap<String, String> planetFlags = null;

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String nodeName = reader.getNodeName();
            if (nodeName.equals("continent")) {
                Continent continent = (Continent) context.convertAnother(null, Continent.class);
                planetEnvironments.add(continent);
            } else if (nodeName.equals("name")) {
                name = reader.getValue();
            } else if (nodeName.equals("yCoord")) {
                ycoord = reader.getValue();
            } else if (nodeName.equals("xCoord")) {
                xcoord = reader.getValue();
            } else if (nodeName.equals("componentProduction")) {
                componentProduction = Integer.parseInt(reader.getValue());
            } else if (nodeName.equals("influence")) {
                influences = (Influences) context.convertAnother(null, Influences.class);
            } else if (nodeName.equals("unitFactory")) {
                SUnitFactory unitFactory = (SUnitFactory) 
                    context.convertAnother(
                            null,
                            SUnitFactory.class
                    );
                unitFactoryList.add(unitFactory);
            } else if (nodeName.equals("isHomeworld")) {
                isHomeworld = Boolean.parseBoolean(reader.getValue());
            } else if (nodeName.equals("originalOwner")) {
                originalOwnerString = reader.getValue();
            } else if (nodeName.equals("operationFlags")) {
                //reader.moveDown();
                planetFlags = unmarshalOperationFlags(reader, context);
                //reader.moveUp();
            }
            reader.moveUp();
        }
        if (name == null) {
            throw new ConversionException("name is null");
        }
        SPlanet planet = new SPlanet(
                name,
                influences,
                componentProduction,
                Double.parseDouble(xcoord),
                Double.parseDouble(ycoord)
            );
        for (UnitFactory unitFactory : unitFactoryList) {
            ((SUnitFactory) unitFactory).setPlanet(planet);
        }
        planet.setHomeWorld(isHomeworld);
        if (originalOwnerString == null) {
            Integer owner = influences.getOwner();
            
            House house = CampaignData.cd.getHouse(owner);
            originalOwnerString = house.getName();
        }
        planet.setEnvironments(planetEnvironments);
        planet.setUnitFactories(unitFactoryList);
        planet.setOriginalOwner(originalOwnerString);
        if (planetFlags != null) {
            planet.setPlanetFlags(planetFlags);
        }
        return planet;
    }

    public TreeMap<String, String> unmarshalOperationFlags(HierarchicalStreamReader reader,
            UnmarshallingContext context) {
        TreeMap<String, String> operationFlags = new TreeMap<String, String>();

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String nodeName = reader.getNodeName();
            if (nodeName.equals("operationFlag")) {
                //reader.moveDown();
                unmarshalOperationFlag(operationFlags, reader, context);
                //reader.moveUp();
            }
            reader.moveUp();
        }
        return operationFlags;
    }

    public void unmarshalOperationFlag(TreeMap<String, String> operationFlags, HierarchicalStreamReader reader,
            UnmarshallingContext context) {
        String key = null;
        String value = null;

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String nodeName = reader.getNodeName();
            if (nodeName.equals("key")) {
                key = reader.getValue();
            } else if (nodeName.equals("value")) {
                value = reader.getValue();
            }
            reader.moveUp();
        }
        operationFlags.put(key, value);
    }
}
