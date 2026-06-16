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

package mekwars.server.campaign.data;

import jakarta.persistence.MappedSuperclass;

import mekwars.common.House;

import java.time.Instant;

/**
 * Adds the ability to trace the last change time to a planet.
 *
 * @author Imi (immanuel.scholz@gmx.de)
 */
@MappedSuperclass
public class TimeUpdateHouse extends House {

    /** Constructor used for serialization */
    public TimeUpdateHouse() {
        super();
        updated();
    }

    /** The time at which this data was changed last. */
    private Instant timestamp;

    /**
     * @return Returns the timestamp which this data was last changed.
     */
    public Instant getLastChanged() {
        return timestamp;
    }

    /** Mark the data as updated. */
    public void updated() {
        timestamp = Instant.now();
    }
}
