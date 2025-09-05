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
import mekwars.common.universe.FactionTag;
import java.util.EnumSet;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.NewbieHouse;
import mekwars.server.campaign.SHouse;
import mekwars.server.campaign.mercenaries.MercHouse;

public class SHouseConverter implements Converter {
    public boolean canConvert(Class clazz) {
        return clazz.equals(SHouse.class);
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

        int baseGunner = 4;
        int basePilot = 5;

        boolean conquerable = true;
        boolean inHouseAttacks = false;
        boolean canDefectFrom = true;
        boolean canDefectTo = true;
        /*
         *  TODO: This is IS to keep this backwords compatible with the current data. Once a
         *  version has been released this should be set to noneOf and checked explicitly.
         */
        EnumSet<FactionTag> tags = EnumSet.of(FactionTag.IS);

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String nodeName = reader.getNodeName();
            if (nodeName.equals("name")) {
                name = reader.getValue();
            } else if (nodeName.equals("color")) {
                color = reader.getValue();
            } else if (nodeName.equals("abbreviation")) {
                abbreviation = reader.getValue();
            } else if (nodeName.equals("type")) {
                type = reader.getValue();
            } else if (nodeName.equals("logo")) {
                logo = reader.getValue();
            } else if (nodeName.equals("baseGunner")) {
                baseGunner = Integer.parseInt(reader.getValue());
            } else if (nodeName.equals("basePilot")) {
                basePilot = Integer.parseInt(reader.getValue());
            } else if (nodeName.equals("conquerable")) {
                conquerable = Boolean.parseBoolean(reader.getValue());
            } else if (nodeName.equals("inHouseAttacks")) {
                inHouseAttacks = Boolean.parseBoolean(reader.getValue());
            } else if (nodeName.equals("housePlayerColor")) {
                housePlayerColor = reader.getValue();
            } else if (nodeName.equals("canDefectFrom")) {
                canDefectFrom = Boolean.parseBoolean(reader.getValue());
            } else if (nodeName.equals("canDefectTo")) {
                canDefectTo = Boolean.parseBoolean(reader.getValue());
            } else if (nodeName.equals("tags")) {
                tags = (EnumSet<FactionTag>) context.convertAnother(null, EnumSet.class);
            }
            reader.moveUp();
        }
        SHouse house = null; 
        if (type.equals("House")) {
            house = new SHouse(name, color, baseGunner, basePilot, abbreviation);
        } else if (type.equals("Mercenary")) {
            house = new MercHouse(name, color, baseGunner, basePilot, abbreviation);
        } else if (type.equals("Newbie")) {
            house = new NewbieHouse(name, color, baseGunner, basePilot, abbreviation);
        } else {
            new ConversionException("type must be one of the following: 'House', 'Mercenary', 'Newbie'");
        }
        if (logo != null) {
            house.setLogo(logo);
        }
        house.setInHouseAttacks(inHouseAttacks);
        house.setConquerable(conquerable);
        house.setHousePlayerColors(housePlayerColor);
        house.setTags(tags);
        return house;
    }
}
