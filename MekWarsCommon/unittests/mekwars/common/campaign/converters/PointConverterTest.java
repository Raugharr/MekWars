/*
 * MekWars - Copyright (C) 2004 
 * 
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet)
 * Original author Helge Richter (McWizard)
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Point;
import org.junit.jupiter.api.Test;
import mekwars.common.util.MMNetXStream;

public class PointConverterTest {
    @Test
    public void serialize() {
        MMNetXStream xstream = new MMNetXStream(); 
        Point point = new Point(1, 2);
        String xml = xstream.toXML(point);
        Point newPoint = (Point) xstream.fromXML(xml);

        assertEquals(newPoint.getX(), 1.0);
        assertEquals(newPoint.getY(), 2.0);
    }
}
