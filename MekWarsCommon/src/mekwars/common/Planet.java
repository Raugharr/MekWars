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

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;

import mekwars.common.entities.MWEntity;
import mekwars.common.util.BinReader;
import mekwars.common.util.BinWriter;
import mekwars.common.util.Position;

import org.hibernate.StatelessSession;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.FetchProfile;
import org.hibernate.annotations.FetchProfileOverride;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.annotations.processing.CheckHQL;
import org.hibernate.query.MutationQuery;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author Helge Richter
 */
@CheckHQL
@NamedQuery(name = "Planet.findByName", query = "FROM Planet WHERE name = :name")
@NamedQuery(name = "Planet.findLikeName", query = "FROM Planet WHERE LOWER(name) LIKE LOWER(:name)")
@FetchProfile(name = "EagerPlanet")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Entity
public class Planet implements Comparable<Object>, MWEntity {
    /**
     * Unique id of this planet. Mutable field (although it will not change, it has to be
     * transfered)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

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
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "planet_id")
    @FetchProfileOverride(profile = Planet_.PROFILE_EAGER_PLANET, mode = FetchMode.JOIN)
    private List<UnitFactory> unitFactories = new ArrayList<UnitFactory>();

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

    /** House that has the most influence on this planet. */
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private House owner = null;

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
    private boolean homeworld = false;

    /* Original Owner of the planet */
    private String originalOwner = "";

    /*
     * This allows SO's to set flags for planets and to be used in ops.
     */
    @ElementCollection
    @CollectionTable(name = "planet_flag", joinColumns = @JoinColumn(name = "planet_id"))
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    @FetchProfileOverride(profile = Planet_.PROFILE_EAGER_PLANET, mode = FetchMode.JOIN)
    private Map<String, String> planetFlags = new HashMap<String, String>();

    /*
     * Max Planet Points. this Allows SO's to set the conquer points of a planet
     * That way some planets are harder to conquer then others.
     */
    private int conquestPoints = 100;

    @OneToMany(mappedBy = "planet", cascade = CascadeType.ALL)
    @FetchProfileOverride(profile = Planet_.PROFILE_EAGER_PLANET, mode = FetchMode.JOIN)
    private List<Continent> continents = new ArrayList<>();

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
    public int getComponentProduction() {
        return componentProduction;
    }

    /**
     * @param componentProduction The componentProduction to set.
     */
    public void setComponentProduction(int componentProduction) {
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

    public void addUnitFactory(UnitFactory unitFactory) {
        // unitFactory.setPlanet(this);
        unitFactories.add(unitFactory);
    }

    /**
     * @return Returns the continents.
     */
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
        continent.setPlanet(this);
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
        return null;
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

    /** Write itself into the stream. */
    public void binOut(BinWriter out) throws IOException {
        out.println(getId(), "id");
        out.println(getName(), "name");
        out.println(getPosition().getX(), "x");
        out.println(getPosition().getY(), "y");
        out.println(getUnitFactories().size(), "unitFactories.size");
        for (UnitFactory i : getUnitFactories()) {
            i.binOut(out);
        }
        out.println(continents.size(), "terrain.size");
        for (Continent continent : continents) {
            out.println(continent.getId(), "continent_id");
            out.println(continent.getSize(), "size");
            out.println(continent.getEnvironment().getId(), "id");
            out.println(continent.getAdvancedTerrain().getId(), "aid");
        }
        out.println(getDescription(), "description");
        out.println(getBaysProvided(), "baysProvided");
        out.println(isConquerable(), "conquerable");
        out.println(getComponentProduction(), "componentProduction");
        getInfluence().binOut(out);
        out.println(getMinPlanetOwnership(), "minplanetownership");
        out.println(isHomeworld(), "homeworld");
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
        unitFactories = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            UnitFactory unitFactory = new UnitFactory();

            unitFactory.binIn(in);
            addUnitFactory(unitFactory);
        }
        continents = new ArrayList<Continent>();
        int terrainSize = in.readInt("terrain.size");
        for (int i = 0; i < terrainSize; ++i) {
            int continentId = in.readInt("continent_id");
            int percent = in.readInt("size");
            int id = in.readInt("id");
            int aid = in.readInt("aid");
            Terrain T = data.getTerrain(id);
            AdvancedTerrain AT = data.getAdvancedTerrain(aid);
            Continent continent = new Continent(this, percent, T, AT);

            continent.setId(continentId);
            addContinent(continent);
        }
        setDescription(in.readLine("description"));
        setBaysProvided(in.readInt("baysProvided"));
        setConquerable(in.readBoolean("conquerable"));
        setComponentProduction(in.readInt("componentProduction"));
        setInfluence(new Influences());
        getInfluence().binIn(in);
        setMinPlanetOwnership(in.readInt("minplanetownership"));
        setHomeworld(in.readBoolean("homeworld"));
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
                        + (int) getPosition().getX()
                        + " x "
                        + (int) getPosition().getY()
                        + " y<br>"
                        + Math.round(getPosition().distanceSq(0.0, 0.0))
                        + " Lightyears from the galaxy center <br><br>");

        result.append("<b>Industry:</b><br>");
        // factories
        if (getComponentProduction() > 0) {
            result.append(
                    "Heavy industry allows an export of "
                            + getComponentProduction()
                            + " parts.<br>");
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
                        + (int) getPosition().getX()
                        + " x "
                        + (int) getPosition().getY()
                        + " y<br>"
                        + Math.round(getPosition().distanceSq(0.0, 0.0))
                        + " Lightyears from the galaxy center <br><br>");

        result.append("<b>Industry:</b><br>");
        // factories
        if (getComponentProduction() > 0) {
            result.append(
                    "Heavy industry allows an export of "
                            + getComponentProduction()
                            + " parts.<br>");
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

    public int getMinPlanetOwnership() {
        if (minPlanetOwnership < 0) {
            minPlanetOwnership =
                    CampaignData.cd.getCampaignOptions().getIntegerConfig("MinPlanetOwnerShip");
        }

        return minPlanetOwnership;
    }

    public void setMinPlanetOwnership(int ownership) {
        minPlanetOwnership = ownership;
    }

    public void setHomeworld(boolean homeworld) {
        this.homeworld = homeworld;
    }

    public boolean isHomeworld() {
        return homeworld;
    }

    public void setOriginalOwner(String owner) {
        originalOwner = owner;
    }

    public String getOriginalOwner() {
        return originalOwner;
    }

    public Map<String, String> getPlanetFlags() {
        return planetFlags;
    }

    public void setPlanetFlags(Map<String, String> flags) {
        planetFlags = flags;
    }

    public int getConquestPoints() {
        return conquestPoints;
    }

    public void setConquestPoints(int points) {
        conquestPoints = Math.max(1, points);
    }

    public House checkOwner() {
        if (getInfluence() == null) {
            return null;
        }

        Integer houseId = this.getInfluence().getOwner();

        if (houseId == null) {
            return null;
        }

        House house = CampaignData.cd.getHouse(houseId);

        if (this.getInfluence().getInfluence(houseId) < this.getMinPlanetOwnership()) {
            return null;
        }
        return house;
    }

    public House getOwner() {
        if (owner == null) {
            checkOwner();
        }
        return owner;
    }

    public void setOwner(House newOwner) {
        if (newOwner != null) {
            owner = newOwner;
        }
    }

    public String getShortDescription() {
        StringBuilder result = new StringBuilder(getName());
        Continent biggestContinent = getBiggestContinent();
        Terrain terrain = null;
        AdvancedTerrain advancedTerrain = null;

        if (biggestContinent != null) {
            terrain = biggestContinent.getEnvironment();
            advancedTerrain = biggestContinent.getAdvancedTerrain();
        }

        if (terrain != null && terrain.getEnvironments().size() > 0) {
            result.append(" " + terrain.getEnvironments().get(0).toImageDescription());
            result.append(" " + terrain.getEnvironments().get(0).getName());
        }
        if (advancedTerrain != null) {
            result.append(" " + advancedTerrain.WeatherForcast());
        }

        if (this.getUnitFactories().size() > 0) {
            for (int i = 0; i < this.getUnitFactories().size(); i++) {
                UnitFactory unitFactory = this.getUnitFactories().get(i);
                result.append(unitFactory.getIcons());
            }
        }
        if (terrain != null && getTotalEnvironmentProbabilities() > 0) {
            long probabilities =
                    Math.round(
                            (double) biggestContinent.getSize()
                                    * 100
                                    / getTotalEnvironmentProbabilities());
            result.append(" (" + probabilities + "% correct)");
        } else {
            result.append(" (100% correct)");
        }
        return result.toString();
    }

    /** Return the environment with the most probability to occour. */
    public Continent getBiggestEnvironment() {
        return continents.stream().max(Comparator.comparingInt(Continent::getSize)).orElse(null);
    }

    @Override
    public boolean equals(Object object) {
        Planet planet = null;

        try {
            planet = (Planet) object;
        } catch (ClassCastException e) {
            return false;
        }

        return planet != null && planet.getId() == this.getId();
    }

    public void sync(StatelessSession session) {
        Integer prevBatchSize = session.getJdbcBatchSize();

        session.setJdbcBatchSize(0);
        session.upsert(this);

        for (Continent continent : getContinents()) {
            session.upsert(continent);
        }

        for (UnitFactory unitFactory : getUnitFactories()) {
            session.upsert(unitFactory);
        }
        MutationQuery influenceQuery =
                session.createNativeMutationQuery(
                        "INSERT INTO planet_influence (influence, planet_id, house_id) "
                                + "VALUES (:influence, :planet_id, :house_id) "
                                + "ON CONFLICT(planet_id, house_id) DO UPDATE SET "
                                + "influence = excluded.influence");
        for (Map.Entry<Integer, Integer> entry : getInfluence().entrySet()) {
            influenceQuery
                    .setParameter("influence", entry.getValue())
                    .setParameter("planet_id", getId())
                    .setParameter("house_id", entry.getKey())
                    .executeUpdate();
        }
        session.setJdbcBatchSize(prevBatchSize);
    }
}
