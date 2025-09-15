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

package mekwars.updater;

import megamek.Version;

public abstract class AbstractVersionUpdater implements Comparable<AbstractVersionUpdater> {
    public abstract void update();
    
    public abstract Version getVersion();
    
    public int compareTo(final AbstractVersionUpdater other) {
    	return getVersion().compareTo(other.getVersion());
    } 
}