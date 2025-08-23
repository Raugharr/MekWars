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

import server.campaign.CampaignMain;
import server.campaign.NewbieHouse;
import server.campaign.SHouse;
import server.campaign.mercenaries.MercHouse;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class SHouseConverter implements Converter {
	public boolean canConvert(Class clazz) {
		return clazz.equals(SHouseConverter.class);
	}

	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		// TODO: Implement
	}

	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		String name = null;
		String color = "#00FF00";
		String abbreviation = null;
		String housePlayerColor = "#000000";
		String logo = null;
		String type = null;

		int money = 0;
		int baseGunner = 4;
		int basePilot = 5;

		boolean conquerable = true;
		boolean inHouseAttacks = false;
		boolean canDefectFrom = true;
		boolean canDefectTo = true;
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			String nodeName = reader.getNodeName();
			if(nodeName.equals("name")) {
				name = reader.getValue();
			} else if(nodeName.equals("money")) {
				money = Integer.valueOf(reader.getValue());
			} else if(nodeName.equals("color")) {
				color = reader.getValue();
			} else if(nodeName.equals("abbreviation")) {
				abbreviation = reader.getValue();
			} else if(nodeName.equals("type")) {
				type = reader.getValue();
			} else if(nodeName.equals("logo")) {
				logo = reader.getValue();
			} else if(nodeName.equals("baseGunner")) {
				baseGunner = Integer.valueOf(reader.getValue());
			} else if(nodeName.equals("basePilot")) {
				basePilot = Integer.valueOf(reader.getValue());
			} else if(nodeName.equals("conquerable")) {
				conquerable = Boolean.valueOf(reader.getValue());
			} else if(nodeName.equals("inHouseAttacks")) {
				inHouseAttacks = Boolean.valueOf(reader.getValue());
			} else if(nodeName.equals("housePlayerColor")) {
				housePlayerColor = reader.getValue();
			} else if(nodeName.equals("canDefectFrom")) {
				canDefectFrom = Boolean.valueOf(reader.getValue());
			} else if(nodeName.equals("canDefectTo")) {
				canDefectTo = Boolean.valueOf(reader.getValue());
			}
			reader.moveUp();
		}
		SHouse house = null; 
		int id = CampaignMain.cm.getData().getUnusedHouseID();
		if(type.equals("House")) {
			house = new SHouse(id, name, color, baseGunner, basePilot, abbreviation);
		} else if(type.equals("Mercenary")) {
			house = new MercHouse(id, name, color, baseGunner, basePilot, abbreviation);
		} else if(type.equals("Newbie")) {
			house = new NewbieHouse(id, name, color, baseGunner, basePilot, abbreviation);
		} else {
			new ConversionException("type must be one of the following: 'House', 'Mercenary', 'Newbie'");
		}
		if(logo != null) {
			house.setLogo(logo);
		}
		house.setInHouseAttacks(inHouseAttacks);
		house.setConquerable(conquerable);
		house.setHousePlayerColors(housePlayerColor);
		return house;
	}
}
