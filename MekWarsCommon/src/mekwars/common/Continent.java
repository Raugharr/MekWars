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

/*
 * Created on 04.05.2004
 *
 */
package mekwars.common;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * @author Helge Richter
 */
@Entity
public class Continent {
    @ManyToOne
    @JoinColumn(name = "terrain_id")
    private Terrain environment;

    @ManyToOne
    @JoinColumn(name = "advanced_terrain_id")
    private AdvancedTerrain advTerrain;

    private int size = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    public Continent(int size, Terrain env, AdvancedTerrain advTerr) {
        this.size = size;
        environment = env;
        advTerrain = advTerr;
    }

    public Continent() {
        // for serialisation
    }

    /**
     * @return Returns the size.
     */
    public int getSize() {
        return size;
    }

    /**
     * @param size The size to set.
     */
    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Continent)) return false;
        Continent cont = (Continent) o;
        if (cont.getSize() != getSize()) return false;
        if (cont.getEnvironment().equals(getEnvironment())) return false;
        if (cont.getAdvancedTerrain().equals(getAdvancedTerrain())) return false;
        return true;
    }

    /**
     * @return Returns the envID.
     */
    public Terrain getEnvironment() {
        return environment;
    }

    /**
     * @return Returns the envID.
     */
    public AdvancedTerrain getAdvancedTerrain() {
        return advTerrain;
    }

    /**
     * @return Returns the Continent ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the continent ID;
     *
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    public String getDropBoxName() {
        return getEnvironment().getName()
                + "("
                + getAdvancedTerrain().getName()
                + ") %"
                + getSize();
    }
}
