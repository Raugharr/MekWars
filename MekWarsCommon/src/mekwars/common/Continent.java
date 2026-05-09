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
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;

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
    private AdvancedTerrain advancedTerrain;

    private int size = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planet_id")
    private Planet planet;

    public Continent(int size, Terrain terrain, AdvancedTerrain advancedTerrain) {
        this.size = size;
        this.environment = terrain;
        this.advancedTerrain = advancedTerrain;
    }

    public Continent(Planet planet, int size, Terrain terrain, AdvancedTerrain advancedTerrain) {
        this(size, terrain, advancedTerrain);
        this.planet = planet;
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
    public boolean equals(Object object) {
        if (!(object instanceof Continent)) return false;
        Continent continent = (Continent) object;

        if (continent.getSize() != getSize()) return false;
        if (!continent.getEnvironment().equals(getEnvironment())) return false;
        if (!continent.getAdvancedTerrain().equals(getAdvancedTerrain())) return false;
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
        return advancedTerrain;
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

    public Planet getPlanet() {
        return planet;
    }

    public void setPlanet(Planet planet) {
        this.planet = planet;
    }

    public String getDropBoxName() {
        return getEnvironment().getName()
                + "("
                + getAdvancedTerrain().getName()
                + ") %"
                + getSize();
    }
}
