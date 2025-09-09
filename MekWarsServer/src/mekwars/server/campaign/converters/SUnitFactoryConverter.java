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

import java.util.Vector;
import mekwars.common.UnitFactory;
import mekwars.server.campaign.SUnitFactory;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class SUnitFactoryConverter implements Converter {
    public boolean canConvert(Class clazz) {
        return clazz.equals(SUnitFactory.class);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer,
            MarshallingContext context) {
        SUnitFactory unitFactory = (SUnitFactory) source;

        writer.startNode("name");
        writer.setValue(unitFactory.getName());
        writer.endNode();

        writer.startNode("size");
        writer.setValue(unitFactory.getSize());
        writer.endNode();

        writer.startNode("founder");
        writer.setValue(unitFactory.getFounder());
        writer.endNode();

        writer.startNode("ticksUntilRefresh");
        writer.setValue(Integer.toString(unitFactory.getTicksUntilRefresh()));
        writer.endNode();

        writer.startNode("buildTableFolder");
        writer.setValue(unitFactory.getBuildTableFolder());
        writer.endNode();

        writer.startNode("accessLevel");
        writer.setValue(Integer.toString(unitFactory.getAccessLevel()));
        writer.endNode();

        Vector<String> types = new Vector<String>();
        if (unitFactory.canProduce(UnitFactory.BUILDVEHICLES)) {
            types.add("Vehicle");
        }
        if (unitFactory.canProduce(UnitFactory.BUILDINFANTRY)) {
            types.add("Infantry");
        }
        if (unitFactory.canProduce(UnitFactory.BUILDBATTLEARMOR)) {
            types.add("BattleArmor");
        }
        if (unitFactory.canProduce(UnitFactory.BUILDPROTOMECHS)) {
            types.add("Protomek");
        }
        if (unitFactory.canProduce(UnitFactory.BUILDAERO)) {
            types.add("Aero");
        }
        if (unitFactory.canProduce(UnitFactory.BUILDMEK)) {
            types.add("Mek");
        }

        for (String type : types) {
            writer.startNode("type");
            writer.setValue(type);
            writer.endNode();
        }
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        int type = 0;
        int ticksUntilRefresh = 0;
        int accessLevel = 0;
        int refreshSpeed = 100;
        String size = null;
        String founder = null;
        String buildTableFolder = "0";
        String name = null;

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String nodeName = reader.getNodeName();
            if (nodeName.equals("name")) {
                name = reader.getValue();
            } else if (nodeName.equals("size")) {
                size = reader.getValue();
            } else if (nodeName.equals("founder")) {
                founder = reader.getValue();
            } else if (nodeName.equals("ticksUntilRefresh")) {
                ticksUntilRefresh = Integer.valueOf(reader.getValue());
            } else if (nodeName.equals("refreshSpeed")) {
                refreshSpeed = Integer.valueOf(reader.getValue());
            } else if (nodeName.equals("buildTableFolder")) {
                buildTableFolder = reader.getValue();
            } else if (nodeName.equals("accessLevel")) {
                accessLevel = Integer.valueOf(reader.getValue());
            } else if (nodeName.equals("type")) {
                if (nodeName.equals("Vehicle")) {
                    type = type + UnitFactory.BUILDVEHICLES;
                } else if (nodeName.equals("Infantry")) {
                    type = type + UnitFactory.BUILDINFANTRY;
                } else if (nodeName.equals("BattleArmor")) {
                    type = type + UnitFactory.BUILDBATTLEARMOR;
                } else if (nodeName.equals("Protomek")) {
                    type = type + UnitFactory.BUILDPROTOMECHS;
                } else if (nodeName.equals("Aero")) {
                    type = type + UnitFactory.BUILDAERO;
                } else if (nodeName.equals("Mek")) {
                    type = type + UnitFactory.BUILDMEK;
                }
            }
            reader.moveUp();
        }
        if (name == null) {
               throw new ConversionException("Name expected");
        }
        if (founder == null) {
               throw new ConversionException("Founder expected");
        }
        if (size == null) {
               throw new ConversionException("Size expected");
        }
        return new SUnitFactory(name, null, size, founder, ticksUntilRefresh, refreshSpeed, type, buildTableFolder, accessLevel);
    }
}
