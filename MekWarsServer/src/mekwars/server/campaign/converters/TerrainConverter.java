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
import common.PlanetEnvironment;
import common.Terrain;

public class TerrainConverter implements Converter {
    public boolean canConvert(Class clazz) {
        return clazz.equals(Terrain.class);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        // TODO: Implement
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        String name = null;
        Terrain terrain = new Terrain();

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String nodeName = reader.getNodeName();
            if (nodeName.equals("environment")) {
                PlanetEnvironment environment = (PlanetEnvironment)
                    context.convertAnother(
                        null,
                        PlanetEnvironment.class
                    );
                terrain.getEnvironments().add(environment);
            } else if (nodeName.equals("name")) {
                name = reader.getValue();
                terrain.setName(name);
            }
            reader.moveUp();
        }
        if (terrain.getName() == null) {
            throw new ConversionException("name is null");
        }
        return terrain;
    }
}
