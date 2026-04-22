/*
 * MekWars - Copyright (C) 2008
 *
 * Original author - jtighe (torren@users.sourceforge.net)
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 */

package mekwars.common;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;

import mekwars.common.entities.MWEntity;
import mekwars.common.util.BinReader;
import mekwars.common.util.BinWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * A Terrain Base Terrain container for all environments. Each environment can be a different theme
 * to allow for different times of year.
 */
@Entity
public final class Terrain implements MWEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name = "None";

    @OneToMany
    @JoinColumn(name = "terrain_id")
    private List<PlanetEnvironment> environments = new ArrayList<>();

    /** For Serialisation. */
    public Terrain() {}

    public Terrain(String s) {
        StringTokenizer ST = new StringTokenizer(s, "$");
        // Read the TE$;
        ST.nextToken();
        // Read the Data

        name = ST.nextToken();

        while (ST.hasMoreElements()) {
            PlanetEnvironment PE = new PlanetEnvironment(ST);
            environments.add(PE);
        }
    }

    public String toString() {
        String result = "TE$";
        result += name + "$";

        for (PlanetEnvironment env : environments) {
            result += env.toString();
        }
        return result;
    }

    /** Writes as binary stream */
    public void binOut(BinWriter out) throws IOException {
        out.println(id, "id");
        out.println(name, "name");

        out.println(environments.size(), "environmentsize");

        for (PlanetEnvironment env : environments) {
            env.binOut(out);
        }
    }

    /** Read from a binary stream */
    public void binIn(BinReader in, CampaignData data) throws IOException {
        id = in.readInt("id");
        name = in.readLine("name");

        int environments = in.readInt("environmentsize");

        for (int pos = 0; pos < environments; pos++) {
            PlanetEnvironment PE = new PlanetEnvironment();

            PE.binIn(in, data);

            this.environments.add(PE);
        }
    }

    /**
     * @return Returns the id.
     */
    public int getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return List<PlanetEnvironments>
     */
    public List<PlanetEnvironment> getEnvironments() {
        return this.environments;
    }

    public String toImageDescription() {
        if (environments.size() > 0) return environments.get(0).toImageDescription();

        return "";
    }

    public String toImageAbsolutePathDescription() {
        if (environments.size() > 0) return environments.get(0).toImageAbsolutePathDescription();

        return "";
    }

    /** Return the total probability of all environments. */
    public int getTotalEnvironmentProbabilities() {
        return environments.stream().mapToInt(PlanetEnvironment::getEnvironmentalProbability).sum();
    }
}
