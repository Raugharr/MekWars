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

import common.UnitFactory;

import server.campaign.SUnitFactory;

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

	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		// TODO: Implement
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
			if(nodeName.equals("name")) {
				name = reader.getValue();
			} else if(nodeName.equals("size")) {
				size = reader.getValue();
			} else if(nodeName.equals("founder")) {
				founder = reader.getValue();
			} else if(nodeName.equals("ticksUntilRefresh")) {
				ticksUntilRefresh = Integer.valueOf(reader.getValue());
			} else if(nodeName.equals("refreshSpeed")) {
				refreshSpeed = Integer.valueOf(reader.getValue());
			} else if(nodeName.equals("buildTableFolder")) {
				buildTableFolder = reader.getValue();
			} else if(nodeName.equals("accessLevel")) {
				accessLevel = Integer.valueOf(reader.getValue());
			} else if(nodeName.equals("type")) {
				if (nodeName.equals("vehicle")) {
					type = type + UnitFactory.BUILDVEHICLES;
				} else if (nodeName.equals("infantry")) {
					type = type + UnitFactory.BUILDINFANTRY;
				} else if (nodeName.equals("battleArmor")) {
					type = type + UnitFactory.BUILDBATTLEARMOR;
				} else if (nodeName.equals("protomek")) {
					type = type + UnitFactory.BUILDPROTOMECHS;
				} else if (nodeName.equals("aero")) {
					type = type + UnitFactory.BUILDAERO;
				} else {
					type = type + UnitFactory.BUILDMEK;
				}
			}
			reader.moveUp();
		}
		if(name == null) throw new ConversionException("Name expected");
		if(founder == null) throw new ConversionException("Founder expected");
		if(size == null) throw new ConversionException("Size expected");
        return new SUnitFactory(name, null, size, founder, ticksUntilRefresh, refreshSpeed, type, buildTableFolder, accessLevel);
	}
}
