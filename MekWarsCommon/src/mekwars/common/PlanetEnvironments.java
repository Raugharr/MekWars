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

package mekwars.common;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;

import mekwars.common.util.BinReader;
import mekwars.common.util.BinWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Represents a collection of continents, usually for one planet
 *
 * @author Imi (immanuel.scholz@gmx.de) seen, modified and made totally bad by McWizard
 *     <p>Imi: *crhm*..."totally bad"... ;-) TODO: simplify this class. subclass it from ArrayList
 *     or something like that
 */
@Entity
public class PlanetEnvironments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** The list of all continents. Type=Continent */
    @OneToMany
    @JoinColumn(name = "planet_id")
    private List<Continent> continents = new ArrayList<>();

    public int getSize() {
        return id;
    }

    /** Returns the number of terrains in this set. */
    public int size() {
        return continents.size();
    }

    public List<Continent> getContinents() {
        return Collections.unmodifiableList(continents);
    }

    /**
     * Add a terrain to the current set. This will vanish, when Terrains are initialized through
     * XStream. @TODO You should not need this and you should only initialize the terrain set with
     * either XStream or binIn()
     */
    public synchronized void add(Continent newPE) {
        continents.add(newPE);
    }

    public synchronized void remove(String terrain) {
        int count = 0;
        for (Object land : continents) {

            // Check for multiple terrains with the same name.
            if (((Continent) land).getEnvironment().getName().equals(terrain)) {
                break;
            }
            count++;
        }

        if (count < continents.size()) {
            continents.remove(count);
        }
    }

    public synchronized void removeAll() {
        continents.clear();
    }

    /** Return the environment with the most probability to occour. */
    public Continent getBiggestEnvironment() {
        Continent result = new Continent(0, new Terrain(), new AdvancedTerrain());
        for (Continent p : continents) {
            if (p.getSize() > result.getSize()) result = p;
        }
        return result;
    }

    /** Return the total probability of all environments. */
    public int getTotalEnivronmentPropabilities() {
        int result = 0;
        for (Continent C : continents) result += C.getSize();
        return result;
    }

    /** Returns a randomEnvironment based on the probability of each Environment. */
    public Continent getRandomEnvironment(Random r) {
        // use the skewer draw algorithm from Knuth.
        int probs = getTotalEnivronmentPropabilities();
        for (Continent pe : continents) {
            if (r.nextInt(probs) < pe.getSize()) {

                probs = pe.getEnvironment().getTotalEnvironmentProbabilities();
                for (PlanetEnvironment env : pe.getEnvironment().getEnvironments()) {
                    if (r.nextInt(probs) < env.getEnvironmentalProbability()) {
                        return pe;
                    }
                    probs -= env.getEnvironmentalProbability();
                }
            }
            probs -= pe.getSize();
        }
        return new Continent(0, null, null);
    }

    /** Writes as binary stream */
    public void binOut(BinWriter out) {
        out.println(continents.size(), "terrain.size");
        for (Continent C : continents) {
            out.println(C.getSize(), "size");
            out.println(C.getEnvironment().getId(), "id");
            out.println(C.getAdvancedTerrain().getId(), "aid");
        }
    }

    /** Read from a binary stream */
    public void binIn(BinReader in, CampaignData data) throws IOException {
        int size = in.readInt("terrain.size");
        for (int i = 0; i < size; ++i) {
            int percent = in.readInt("size");
            int id = in.readInt("id");
            int aid = in.readInt("aid");
            Terrain T = data.getTerrain(id);
            AdvancedTerrain AT = data.getAdvancedTerrain(aid);
            Continent C = new Continent(percent, T, AT);
            add(C);
        }
    }
}
