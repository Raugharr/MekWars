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
import jakarta.persistence.Transient;

import java.util.Date;

import mekwars.common.Influences;
import mekwars.common.Planet;
import mekwars.common.util.Position;

/**
 * Adds the ability to trace the last change time to a planet.
 * 
 * @author Imi (immanuel.scholz@gmx.de)
 */
@MappedSuperclass
public class TimeUpdatePlanet extends Planet {

    public TimeUpdatePlanet(String name, Position position, Influences flu) {
        super(name, position, flu);
    }

    public TimeUpdatePlanet() {
        super();
    }

    /**
     * The time at which this data was changed last.
     */
    private Date updatedAt;

    /**
     * @return Returns the timestamp which this data was last changed.
     */
    @Transient
    public Date getLastChanged() {
        return updatedAt;
    }
    
    /**
     * Mark the data as updated.
     */
    public void updated() {
        updatedAt = new Date();
    }
    
	/**
	 * @param timestamp The timestamp to set.
	 */
	public void setTimestamp(Date timestamp) {
		this.updatedAt = timestamp;
	}
}
