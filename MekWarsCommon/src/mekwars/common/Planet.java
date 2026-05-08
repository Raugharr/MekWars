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
 * Created on 23.03.2004
 *
 */
package mekwars.common;

import jakarta.persistence.Transient;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

import mekwars.common.entities.MWEntity;
import mekwars.common.persistence.EntityStore;
import mekwars.common.util.BinReader;
import mekwars.common.util.BinWriter;
import mekwars.common.util.Position;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Helge Richter
 */
@Entity(name = "planet")
public class Planet implements Comparable<Object>, MWEntity {
    // VARIABLES
    /**
     * Unique id of this planet. Mutable field (although it will not change, it has to be
     * transfered)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id = EntityStore.UNSET_ID;

    /** name of the planet. Should be unique among planets too. */
    private String name;

    /**
     * position of this planet in the inner spehre map. Ranges from about -700 to 700 in both
     * directions.
     */
    @Embedded private Position position; // distance calculates faster, also fewer casts

    /**
     * The unit factories on this planet. Type is UnitFactory Mutable field (has to be transfered)
     */
    @OneToMany
    @JoinColumn(name = "planet_id")
    private List<UnitFactory> unitFactories = new ArrayList<UnitFactory>();

    /** The environment modifiers for the planet. */
    // @OneToOne
    // @JoinColumn(name = "planet_environments_id")
    private PlanetEnvironments environments = new PlanetEnvironments();

    /** A human readable description of the planet. */
    private String description = "";

    /** Amount of bays to add to the faction holding the planet. */
    private int baysProvided = 0;

    /** Whether you can conquer the planet with Conquer - task. */
    private boolean conquerable = true;

    /** How much components are produced through this planet. */
    private int componentProduction = 0;

    /** The influence each faction has on this planet. Mutable field (has to be transfered) */
    @Embedded private Influences influence;

    // size

    /**
     * Vars for temperature vaccum and gravity
     *
     * @author jtighe
     */

    /**
     * Min Planet ownership to allow a faction to use the planets resources defaults to -1 so that
     * the server wide on is used.
     */
    private int minPlanetOwnership = -1;

    /** Boolean that states if a planet is a homeworld or not */
    private boolean homeWorld = false;

    /* Original Owner of the planet */
    private String originalOwner = "";

    /*
     * This allows SO's to set flags for planets and to be used in ops.
     */
    private Map<String, String> planetFlags = new HashMap<String, String>();

    /*
     * Max Planet Points. this Allows SO's to set the conquer points of a planet
     * That way some planets are harder to conquer then others.
     */
    private int maxConquestPoints = 100;

    // CONSTRUCTORS
    public Planet(String name, Position position, Influences influence) {
        if (influence == null) {
            throw new IllegalArgumentException();
        }
        setName(name);
        setPosition(position);
        setInfluence(influence);
    }

    /** Used for serialization */
    public Planet() {
        // no content
    }

    /** Read the stream back to a Planet object. */
    public Planet(BinReader in, CampaignData data) throws IOException {
        this.binIn(in, data);
    }

    // METHODS
    /**
     * @return Returns the baysProvided.
     */
    public int getBaysProvided() {
        return baysProvided;
    }

    /**
     * @param baysProvided The baysProvided to set.
     */
    public void setBaysProvided(int baysProvided) {
        this.baysProvided = baysProvided;
    }

    /**
     * @return Returns the componentProduction.
     */
    public int getCompProduction() {
        return componentProduction;
    }

    /**
     * @param componentProduction The componentProduction to set.
     */
    public void setCompProduction(int componentProduction) {
        this.componentProduction = componentProduction;
    }

    /**
     * @author Torren (Jason Tighe)
     * @return the id of the current owner of the planet
     */
    public Integer getPlanetOwner() {
        return getInfluence().getOwner();
    }

    /**
     * @author Torren (Jason Tighe)
     * @param faction
     * @return returns if the faction is the planet owner
     */
    public boolean isOwner(int factionid) {
        Integer ownerID = getPlanetOwner();

        if (ownerID == null) {
            return false;
        }
        return ownerID == factionid;
    }

    /**
     * @return Returns the conquerable.
     */
    public boolean isConquerable() {
        return conquerable;
    }

    /**
     * @param conquerable The conquerable to set.
     */
    public void setConquerable(boolean conquerable) {
        this.conquerable = conquerable;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return sting w/ link and name
     */
    public String getNameAsLink() {
        return "<a href=\"JUMPTOPLANET" + name + "#\">" + name + "</a>";
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the position.
     */
    public Position getPosition() {
        return position;
    }

    /**
     * @param position The position to set.
     */
    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     * @return Returns the Factories.
     */
    public List<UnitFactory> getUnitFactories() {
        return unitFactories;
    }

    /**
     * @param Factories The Factories to set.
     */
    public void setUnitFactories(List<UnitFactory> unitFactories) {
        this.unitFactories = unitFactories;
    }

    /**
     * @return Returns the continents.
     */
    @OneToOne
    @JoinColumn(name = "planet_environments_id")
    public List<Continent> getContinents() {
        return continents;
    }

    /**
     * Checks if this planet has any continents defined.
     *
     * @return true if planet has at least one continent, false otherwise
     */
    public boolean hasContinents() {
        return !continents.isEmpty();
    }

    /**
     * Adds a continent to this planet.
     *
     * @param continent The continent to add
     */
    public synchronized void addContinent(Continent continent) {
        continents.add(continent);
    }

    /**
     * Removes a continent by terrain name.
     *
     * @param terrainName The terrain name to remove
     */
    public synchronized void removeContinent(String terrainName) {
        Iterator<Continent> iterator = continents.iterator();

        while (iterator.hasNext()) {
            Continent continent = iterator.next();

            if (continent.getEnvironment().getName().equals(terrainName)) {
                iterator.remove();
                break;
            }
        }
    }

    /** Removes all continents from this planet. */
    public synchronized void removeAllContinents() {
        continents.clear();
    }

    /**
     * Returns the total probability of all environments.
     *
     * @return Total environment probability
     */
    public int getTotalEnvironmentProbabilities() {
        return continents.stream().mapToInt(Continent::getSize).sum();
    }

    /**
     * Returns the continent with the highest probability.
     *
     * @return Biggest continent
     */
    public Continent getBiggestContinent() {
        return continents.stream().max(Comparator.comparingInt(Continent::getSize)).orElse(null);
    }

    /**
     * Returns a random continent based on probability weighting.
     *
     * @param r Random number generator
     * @return Random continent
     */
    public Continent getRandomContinent(Random r) {
        // use the skewer draw algorithm from Knuth.
        int continentProbability = getTotalEnvironmentProbabilities();

        for (Continent continent : continents) {
            if (r.nextInt(continentProbability) < continent.getSize()) {
                return continent;
            }
            continentProbability -= continent.getSize();
        }
        return new Continent(0, null, null);
    }

    /**
     * @return Returns the influence.
     */
    public Influences getInfluence() {
        return influence;
    }

    /**
     * @param influence The influence to set.
     */
    public void setInfluence(Influences influence) {
        this.influence = influence;
    }

    /** checks for any unused CP and assignes them to House None id -1 */
    public void updateInfluences() {
        int totalCP = getConquestPoints();

        for (House house : getInfluence().getHouses()) {
            if (house.getId() == -1) {
                continue;
            }
            totalCP -= getInfluence().getInfluence(house.getId());
        }

        if (totalCP > 0) {
            getInfluence().updateHouse(-1, totalCP);
        }
    }

    /** Comparable after the id */
    public int compareTo(Object o) {
        Planet p = (Planet) o;
        return getId() < p.getId() ? -1 : (getId() == p.getId() ? 0 : 1);
    }

    // /** Encode all mutable fields into the stream. Use as few bits as possible. */
    // public void encodeMutableFields(BinWriter out, CampaignData dataProvider) throws IOException
    // {
    //     out.println(getId(), "id");
    //     getInfluence().encodeMutableFields(out, dataProvider);
    //     binOut(out);
    // }

    // /** Decode all mutable fields from the stream. */
    // public void decodeMutableFields(BinReader in, CampaignData dataProvider) throws IOException {
    //     setId(in.readInt("id"));
    //     getInfluence().decodeMutableFields(in, dataProvider);
    //     binIn(in, dataProvider);
    // }

    /** Write itself into the stream. */
    public void binOut(BinWriter out) throws IOException {
        out.println(getId(), "id");
        out.println(getName(), "name");
        out.println(getPosition().x, "x");
        out.println(getPosition().y, "y");
        out.println(getUnitFactories().size(), "unitFactories.size");
        for (UnitFactory i : getUnitFactories()) {
            i.binOut(out);
        }
        out.println(continents.size(), "terrain.size");
        for (Continent C : continents) {
            out.println(C.getSize(), "size");
            out.println(C.getEnvironment().getId(), "id");
            out.println(C.getAdvancedTerrain().getId(), "aid");
        }
        out.println(getDescription(), "description");
        out.println(getBaysProvided(), "baysProvided");
        out.println(isConquerable(), "conquerable");
        out.println(getCompProduction(), "componentProduction");
        getInfluence().binOut(out);
        out.println(getMinPlanetOwnerShip(), "minplanetownership");
        out.println(isHomeWorld(), "homeworld");
        out.println(getOriginalOwner(), "originalowner");
        out.println(getPlanetFlags().size(), "PlanetFlags.size");
        for (String key : getPlanetFlags().keySet()) {
            out.println(key, "PlanetFlags.key");
            out.println(getPlanetFlags().get(key), "PlayerFlags.value");
        }
        out.println(getConquestPoints(), "MaxInfluence");
    }

    public void binIn(BinReader in, CampaignData data) throws IOException {
        setId(in.readInt("id"));
        setName(in.readLine("name"));
        setPosition(new Position(in.readDouble("x"), in.readDouble("y")));
        int size = in.readInt("unitFactories.size");
        setUnitFactories(new ArrayList<UnitFactory>(size));
        for (int i = 0; i < size; ++i) {
            UnitFactory uf = new UnitFactory();
            uf.binIn(in);
            getUnitFactories().add(uf);
        }
        continents = new ArrayList<Continent>();
        int terrainSize = in.readInt("terrain.size");
        for (int i = 0; i < terrainSize; ++i) {
            int percent = in.readInt("size");
            int id = in.readInt("id");
            int aid = in.readInt("aid");
            Terrain T = data.getTerrain(id);
            AdvancedTerrain AT = data.getAdvancedTerrain(aid);
            Continent C = new Continent(percent, T, AT);
            addContinent(C);
        }
        setDescription(in.readLine("description"));
        setBaysProvided(in.readInt("baysProvided"));
        setConquerable(in.readBoolean("conquerable"));
        setCompProduction(in.readInt("componentProduction"));
        setInfluence(new Influences());
        getInfluence().binIn(in);
        setMinPlanetOwnerShip(in.readInt("minplanetownership"));
        setHomeWorld(in.readBoolean("homeworld"));
        setOriginalOwner(in.readLine("originalowner"));
        Map<String, String> map = new HashMap<String, String>();
        size = in.readInt("PlanetFlags.size");
        for (int i = 0; i < size; ++i) {
            String key;
            String value;
            key = in.readLine("PlanetFlags.key");
            value = in.readLine("PlanetFlags.value");
            map.put(key, value);
        }
        setPlanetFlags(map);

        setConquestPoints(in.readInt("MaxInfluence"));
    }

    /** Returns a long description of this planet as html-code. */
    public StringBuilder getLongDescription(boolean client) {

        StringBuilder result = new StringBuilder("Information for Planet: <b>");
        result.append(getName() + "</b><br><br>");
        // result.append("</b> ("+ getDescription() + ")<br><br>");
        result.append(
                "<b>Location:</b> "
                        + (int) getPosition().x
                        + " x "
                        + (int) getPosition().y
                        + " y<br>"
                        + Math.round(getPosition().distanceSq(0.0, 0.0))
                        + " Lightyears from the galaxy center <br><br>");

        result.append("<b>Industry:</b><br>");
        // factories
        if (getCompProduction() > 0) {
            result.append(
                    "Heavy industry allows an export of " + getCompProduction() + " parts.<br>");
        }
        if (getBaysProvided() > 0) {
            result.append(
                    "A base on this world provides all players with "
                            + getBaysProvided()
                            + " extra bays.<br>");
        }

        if (getUnitFactories().size() > 0) {
            String founder = "";
            if (getUnitFactories().size() == 1) {
                result.append("<br><b>Factory:</b><br>");
            } else {
                result.append("<br><b>Factories:</b><br>");
            }
            for (UnitFactory u : getUnitFactories()) {
                founder = u.getFounder();
                String openImage = "./data/images/open" + founder + ".gif";

                if (!new File(openImage).exists()) {
                    openImage = "./data/images/open.gif";
                }

                result.append(
                        "<img src=\"file:///"
                                + new File(openImage).getAbsolutePath()
                                + "\">"
                                + u.getSize()
                                + " "
                                + u.getFullTypeString()
                                + u.getName()
                                + " built by "
                                + founder
                                + "<br>");
            }
        }

        result.append("<br><b>Planetary Conditions</b><br>");

        result.append("<br><b>Terrain:</b><br>");
        int maxProbab = getTotalEnvironmentProbabilities();
        if (continents.isEmpty()) {
            result.append("nothing special");
        } else {
            for (Continent pe : continents) {
                int curProb = (pe.getSize() * 100 / maxProbab);
                if (curProb < 10) {
                    result.append("0");
                }
                result.append(curProb + "% ");
                if (client) {
                    result.append(pe.getEnvironment().toImageAbsolutePathDescription());
                } else {
                    result.append(pe.getEnvironment().toImageDescription());
                }
                String terrainName = pe.getEnvironment().getName();

                result.append(
                        " " + terrainName + " (" + pe.getAdvancedTerrain().getDisplayName() + ")");
                result.append("<br>");

                result.append("  Atmosphere: ");
                result.append(pe.getAdvancedTerrain().getAtmosphere().toString());
                result.append("<br>");

                result.append("  Gravity: " + pe.getAdvancedTerrain().getGravity() + "<br>");
                result.append("  Average Low: " + pe.getAdvancedTerrain().getLowTemp() + "<br>");
                result.append("  Average High: " + pe.getAdvancedTerrain().getHighTemp() + "<br>");
                result.append("<br>");
                result.append(pe.getAdvancedTerrain().WeatherForcast());
                result.append("<br>");
            }
        }

        // influence
        result.append("<br><b>Influence:</b><br>");
        for (House h : getInfluence().getHouses()) {
            String color = "#999999";
            String name = "None";
            int id = -1;

            if (h != null) {
                color = h.getHouseColor();
                name = h.getName();
                id = h.getId();
            }

            result.append(
                    "<font color="
                            + color
                            + ">"
                            + name
                            + "</font> ("
                            + getInfluence().getInfluence(id)
                            + ")");
            result.append(", ");
        } // End for Each

        result.replace(result.length() - 2, result.length(), "<br>");

        if (getPlanetFlags().size() > 0) {
            result.append("<br><b>Points of Interest:</b><br>");
            for (String value : getPlanetFlags().values()) {
                result.append(value + ", ");
            }
            result.replace(result.length() - 2, result.length(), "<br> <br>");
        } // end if planet has flags
        return result;
    }

    /**
     * @see Only a hack! Only use if you know what you're doing!
     * @param id The id to set.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return Returns the id.
     */
    @Id
    public int getId() {
        return id;
    }

    public StringBuilder getAdvanceDescription(int level) {

        StringBuilder result = new StringBuilder();

        result.append("Information for Planet: <b>");
        result.append(getName() + "</b>");

        if (level >= 100) {
            result.append(" (ID: " + getId() + ")");
        }

        result.append("<br><br>");
        // result.append("</b> ("+ getDescription() + ")<br><br>");
        result.append(
                "<b>Location:</b> "
                        + (int) getPosition().x
                        + " x "
                        + (int) getPosition().y
                        + " y<br>"
                        + Math.round(getPosition().distanceSq(0.0, 0.0))
                        + " Lightyears from the galaxy center <br><br>");

        result.append("<b>Industry:</b><br>");
        // factories
        if (getCompProduction() > 0) {
            result.append(
                    "Heavy industry allows an export of " + getCompProduction() + " parts.<br>");
        }
        if (getBaysProvided() > 0) {
            result.append(
                    "A warehouse on this world provides all players with "
                            + getBaysProvided()
                            + " extra .<br><br>");
        }
        if (getUnitFactories().size() > 0) {
            String founder = "";
            if (getUnitFactories().size() == 1) {
                result.append("<br><b>Factory:</b><br>");
            } else {
                result.append("<br><b>Factories:</b><br>");
            }
            for (UnitFactory u : getUnitFactories()) {
                founder = u.getFounder();
                String openImage = "./data/images/open" + founder + ".gif";

                if (!new File(openImage).exists()) {
                    openImage = "./data/images/open.gif";
                }

                result.append(
                        "<img src=\"file:///"
                                + new File(openImage).getAbsolutePath()
                                + ">"
                                + u.getSize()
                                + " "
                                + u.getFullTypeString()
                                + u.getName()
                                + " built by "
                                + founder
                                + "<br>");
            }
        }

        result.append("<br><b>Terrain:</b><br>");
        int maxProbab = getTotalEnvironmentProbabilities();
        if (continents.isEmpty()) {
            result.append("nothing special");
        } else {
            for (Continent pe : continents) {
                int curProb = (pe.getSize() * 100 / maxProbab);
                if (curProb < 10) {
                    result.append("0");
                }
                result.append(curProb + "% ");
                result.append(pe.getEnvironment().toImageAbsolutePathDescription());
                result.append(" " + pe.getEnvironment().getName());
                result.append(" - " + pe.getAdvancedTerrain().getName());
                result.append("<br>Atmosphere: ");
                result.append(pe.getAdvancedTerrain().getAtmosphere().toString());
                result.append("<br>");
                result.append("Gravity: " + pe.getAdvancedTerrain().getGravity());
                result.append("<br>Average Low: " + pe.getAdvancedTerrain().getLowTemp());
                result.append("<br>Average High: " + pe.getAdvancedTerrain().getHighTemp());
                result.append("<br>Night Temp Mod: " + pe.getAdvancedTerrain().getNightTempMod());
                result.append("<br>" + pe.getAdvancedTerrain().WeatherForcast());
            }
        }

        // influence
        result.append("<br><br><b>Influence:</b><br>");
        for (House h : getInfluence().getHouses()) {

            String color = "#999999";
            String name = "None";
            int id = -1;

            if (h != null) {
                color = h.getHouseColor();
                name = h.getName();
                id = h.getId();
            }

            result.append(
                    "<font color="
                            + color
                            + ">"
                            + name
                            + "</font> ("
                            + getInfluence().getInfluence(id)
                            + ")");
            result.append(", ");
        } // while*/
        result.replace(result.length() - 2, result.length(), "<br>");
        if (getPlanetFlags().size() > 0) {
            result.append("<br><b>Points of Intereset:</b><br>");
            for (String value : getPlanetFlags().values()) {
                result.append(value + ", ");
            }
            result.delete(result.length() - 2, result.length());
            result.append("<br><br>");
        } // end if planet has flags

        return result;
    }

    public int getFactoryCount() {
        return getUnitFactories().size();
    }

    public int getMinPlanetOwnerShip() {
        if (minPlanetOwnership < 0)
            minPlanetOwnership =
                    CampaignData.cd.getCampaignOptions().getIntegerConfig("MinPlanetOwnerShip");

        return minPlanetOwnership;
    }

    public void setMinPlanetOwnerShip(int ownership) {
        minPlanetOwnership = ownership;
    }

    public void setHomeWorld(boolean homeworld) {
        homeWorld = homeworld;
    }

    public boolean isHomeWorld() {
        return homeWorld;
    }

    public void setOriginalOwner(String owner) {
        originalOwner = owner;
    }

    public String getOriginalOwner() {
        return originalOwner;
    }

    @ElementCollection
    @CollectionTable(name = "planet_flags", joinColumns = @JoinColumn(name = "planet_id"))
    @MapKeyColumn(name = "flag_key")
    @Column(name = "flag_value")
    public Map<String, String> getPlanetFlags() {
        return planetFlags;
    }

    public void setPlanetFlags(Map<String, String> flags) {
        planetFlags = flags;
    }

    public int getConquestPoints() {
        return maxConquestPoints;
    }

    public void setConquestPoints(int points) {
        maxConquestPoints = Math.max(1, points);
    }

    public String getShortDescription(boolean withTerrain) {
        StringBuilder result = new StringBuilder(getName());

        if (withTerrain) {
            Continent p = getEnvironments().getBiggestEnvironment();
            Terrain pe = p.getEnvironment();
            AdvancedTerrain ape = p.getAdvancedTerrain();
            if (pe != null && pe.getEnvironments().size() > 0) {
                result.append(" " + pe.getEnvironments().get(0).toImageDescription());
                result.append(" " + pe.getEnvironments().get(0).getName());
            }
            if (ape != null) result.append(" " + ape.WeatherForcast());

            if (this.getUnitFactories().size() > 0) {
                for (int i = 0; i < this.getUnitFactories().size(); i++) {
                    UnitFactory MF = (this.getUnitFactories().get(i));
                    result.append(MF.getIcons());
                }
            }
            if (pe != null && getEnvironments().getTotalEnivronmentPropabilities() > 0)
                result.append(
                        " ("
                                + Math.round(
                                        (double) p.getSize()
                                                * 100
                                                / getEnvironments()
                                                        .getTotalEnivronmentPropabilities())
                                + "% correct)");
            else result.append(" (100% correct)");
        }
        return result.toString();
    }

    @Override
    public boolean equals(Object object) {
        Planet planet = null;

        try {
            planet = (Planet) object;
        } catch (ClassCastException e) {
            return false;
        }

        if (planet == null) {
            return false;
        }

        return planet.getId() == this.getId();
    }
}
