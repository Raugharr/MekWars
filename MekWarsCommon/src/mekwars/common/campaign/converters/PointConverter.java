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

package mekwars.common.campaign.converters;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import java.awt.Point;

public class PointConverter implements Converter {
    public boolean canConvert(Class clazz) {
        return clazz.equals(Point.class);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer,
            MarshallingContext context) {
        Point point = (Point) source;

        writer.startNode("x");
        writer.setValue(Integer.toString(Math.toIntExact(Math.round(point.getX()))));
        writer.endNode();

        writer.startNode("y");
        writer.setValue(Integer.toString(Math.toIntExact(Math.round(point.getY()))));
        writer.endNode();
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        int x = 0;
        int y = 0;

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String nodeName = reader.getNodeName();
            if (nodeName.equals("x")) {
                x = Integer.parseInt(reader.getValue());
            } else if (nodeName.equals("y")) {
                y = Integer.parseInt(reader.getValue());
            }
            reader.moveUp();
        }
        return new Point(x, y);
    }
}
