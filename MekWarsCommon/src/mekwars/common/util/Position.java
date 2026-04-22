/*
 * MekWars - Copyright (C) 2004
 *
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet)
 * Original author - helge richter (mcwizard@gmx.de)
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

package mekwars.common.util;

import jakarta.persistence.Embeddable;

@Embeddable
public class Position {
    public double x;
    public double y;

    public Position() { }

    public Position(double xpos, double ypos) {
        x = xpos;
        y = ypos;
    }

    public double getX() {
        return x;
    }

    /**
     * @param x The x to set.
     */
    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    /**
     * @param y The y to set.
     */
    public void setY(double y) {
        this.y = y;
    }

    public double distanceSq(double xpos, double ypos) {
        return Math.sqrt(Math.pow(x - xpos, 2) + Math.pow(y - ypos, 2));
    }

    public double distanceSq(Position p) {
        return distanceSq(p.x, p.y);
    }
}
