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

import common.Continent;
import common.PlanetEnvironments;

import server.campaign.SPlanet;
import server.campaign.SUnitFactory;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import java.util.Vector;

public class PlanetConverter implements Converter {
	public boolean canConvert(Class clazz) {
		return clazz.equals(SPlanet.class);
	}

	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		// TODO: Implement
	}

	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    	PlanetEnvironments planetEnvironments = new PlanetEnvironments();
		String xCoord = null;
		String yCoord = null;
		String name = null;
		int componentProduction = 0;
		Vector<SUnitFactory> unitFactoryList = new Vector<SUnitFactory>();

		while (reader.hasMoreChildren()) {
			reader.moveDown();
			String nodeName = reader.getNodeName();
			if(nodeName.equals("continent")) {
				Continent continent = (Continent) context.convertAnother(null, Continent.class);
				planetEnvironments.add(continent);
			} else if(nodeName.equals("yCoord")) {
				yCoord = reader.getValue();
			} else if(nodeName.equals("xCoord")) {
				xCoord = reader.getValue();
			} else if(nodeName.equals("componentProduction")) {
				componentProduction = Integer.valueOf(reader.getValue());
			} else if(nodeName.equals("unitFactory")) {
				SUnitFactory unitFactory = (SUnitFactory) context.convertAnother(null, SUnitFactory.class);
				unitFactoryList.add(unitFactory);
			}
			reader.moveUp();
		}
		SPlanet planet = new SPlanet(name, null, componentProduction, Double.parseDouble(xCoord), Double.parseDouble(yCoord));
		for(SUnitFactory unitFactory: unitFactoryList) {
			unitFactory.setPlanet(planet);
		}
		return planet;
	}
}
