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

import mekwars.common.campaign.CampaignOptions;
import mekwars.common.campaign.HouseOptions;
import mekwars.common.persistence.BannedAmmoStore;
import mekwars.common.persistence.NamedEntityStore;
import mekwars.common.util.BinReader;
import mekwars.common.util.BinWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

/**
 * TODO: It seems, that all operations done here are needed independly of the semantic of the
 * underlying structure. Planets are handled equal to factions and each function is doubled. If this
 * is true, it should be managed in an generic way to reduce code bloat and code replication.
 * Campaign is the base of the data holding classes for client and server. Here all campaign
 * relevant information as Houses, Planets and Player data is stored. In this base class some
 * methods are provided to retrieve these informations to use in common data classes like House or
 * Planet when reffering to ressources. Notice: Please read the doc to binOut before adding new data
 * types.
 *
 * @author Imi (immanuel.scholz@gmx.de)
 */
public class CampaignData implements TerrainProvider {
    private static final Logger LOGGER = LogManager.getLogger(CampaignData.class);

    public static CampaignData cd;

    private NamedEntityStore<House> factions = new NamedEntityStore<>();
    private NamedEntityStore<Planet> planets = new NamedEntityStore<>();
    private NamedEntityStore<Terrain> terrains = new NamedEntityStore<>();
    private NamedEntityStore<AdvancedTerrain> advancedTerrains = new NamedEntityStore<>();

    private Vector<Integer> bannedTargetingSystems = new Vector<>();
    private HashMap<String, Integer> commands = new HashMap<>();
    private Map<String, HouseOptions> houseOptionsMap = new HashMap<>();
    private TreeMap<String, String> planetOpFlags = new TreeMap<>();

    private BannedAmmoStore bannedAmmoStore = new BannedAmmoStore();
    private CampaignOptions campaignOptions;

    public BannedAmmoStore getBannedAmmoStore() {
        return bannedAmmoStore;
    }

    /**
     * Retrieve a specific planet.
     *
     * @param id The id of the planet.
     * @return The requested Planet. This is usually a subclass of Planet.
     */
    public Planet getPlanet(int id) {
        return planets.get(id);
    }

    /**
     * Retrieve a planet by its name. Please try to use planet Id's when lookup for a planet instead
     * (if you have the choice).
     */
    public Planet getPlanetByName(String name) {
        return planets.getByName(name);
    }

    /**
     * @author jtighe Retrieve a factory by its name.
     */
    public UnitFactory getFactoryByName(Planet p, String name) {
        for (UnitFactory e : p.getUnitFactories()) {
            if (e.getName().equalsIgnoreCase(name)) {
                return e;
            }
        }
        return null;
    }

    /** Retrieves all planets. */
    public Collection<Planet> getAllPlanets() {
        return planets.values();
    }

    /**
     * Adds a planet to the campaign storage. If it was already within the storage, it replaces the
     * old object.
     *
     * @param planet The planet to hold.
     * @see You should use XStream to initialize CampaignData
     */
    public void addPlanet(Planet planet) {
        planets.put(planet);
    }

    /**
     * BUMM - Blow up a planet.
     *
     * @param id The id of the blown up planet.
     */
    public void removePlanet(int id) {
        planets.remove(id);
    }

    /** Remove all planets. */
    public void clearPlanets() {
        planets.clear();
    }

    /**
     * Retrieve a specific faction.
     *
     * @param id The id of the House.
     * @return The requested faction.
     */
    public House getHouse(int ID) {
        return factions.get(ID);
    }

    /** Retrieves all factions. */
    public Collection<House> getAllHouses() {
        return factions.values();
    }

    /**
     * Adds a faction to the campaign storage. If it was already within the storage, it replaces the
     * old object.
     *
     * @param planet The faction to hold. @TODO You should use XStream to initialize CampaignData
     */
    public void addHouse(House faction) {
        factions.put(faction);
    }

    /**
     * Remove a house from the server this is normally only for single faction servers
     *
     * @param Integer id
     */
    public void removeHouse(int id) {
        House faction = factions.get(id);

        if (faction == null) {
            return;
        }
        String factionName = faction.getName().toLowerCase();
        factions.remove(id);

        File factionFile = new File("./campaign/factions/" + factionName + ".dat");
        if (factionFile.exists()) {
            factionFile.delete();
        }

        factionFile = new File("./campaign/factions/" + factionName + ".bak");
        if (factionFile.exists()) {
            factionFile.delete();
        }
    }

    /**
     * Retrieve a faction by its name.
     *
     * @param name
     * @return @TODO This seems to be only needed, because some serialization work with transmitting
     *     the factions name instead of its id.
     */
    public House getHouseByName(String name) {
        return factions.getByName(name);
    }

    /** Remove all factions. */
    public void clearHouses() {
        factions.clear();
    }

    /**
     * Since I have no idea how TinyXML is operating and since McWizard does not allow me to use my
     * loved JDom and finally since Enkel does not like XML-Transfer anyway, I use this to
     * encode/decode the whole object.. (Imi)
     *
     * <p>There is another aspect of binOut to keep in mind. Since a MD5 hash is build after each
     * differential update to keep the data in sync, this function has to provide THE SAME output
     * each time it is run, regardless of the underlying virtual machine. Currently this is done by
     * only using container classes, that remain the elements in a stable order. If you need to add
     * a container with unstable order (as Hash*), you have to make sure, the data is odered before
     * writing it out with binOut.
     *
     * <p>TODO: check http://jira.codehaus.org/secure/ViewIssue.jspa?key=XSTR-27 to see whether a
     * better way of serialization is available ;-)
     */
    public void binOut(BinWriter out) throws IOException {
        binTerrainsOut(out);
        binHousesOut(out);
        binPlanetsOut(out);
    }

    /**
     * Outputs all factions
     *
     * @see CampaignData.binOut()
     */
    public void binHousesOut(BinWriter out) throws IOException {
        out.println(factions.size(), "factions.size");
        for (House house : factions.values()) {
            house.binOut(out);
        }
    }

    /**
     * Outputs updated houses
     *
     * @see CampaignData.binOut()
     */
    public void binHousesOut(ArrayList<House> houses, BinWriter out) throws IOException {
        out.println(houses.size(), "houses.size");
        for (House house : houses) {
            house.binOut(out);
        }
    }

    /**
     * Outputs all terrains
     *
     * @see CampaignData.binOut()
     */
    public void binTerrainsOut(BinWriter out) throws IOException {
        out.println(terrains.size(), "terrains.size");
        for (Terrain pe : terrains.values()) {
            pe.binOut(out);
        }
        out.println(advancedTerrains.size(), "advTerrains.size");
        for (AdvancedTerrain pe : advancedTerrains.values()) {
            pe.binOut(out);
        }
    }

    /**
     * Outputs all planets
     *
     * @see CampaignData.binOut()
     */
    public void binPlanetsOut(BinWriter out) throws IOException {
        out.println(planets.size(), "planets.size");
        for (Planet p : planets.values()) {
            p.binOut(out);
        }
    }

    /**
     * Outputs all planets
     *
     * @see CampaignData.binOut()
     */
    public void binPlanetsOut(ArrayList<Planet> planets, BinWriter out) throws IOException {
        out.println(planets.size(), "planets.size");
        for (Planet planet : planets) {
            planet.binOut(out);
        }
    }

    /** Create empty campaign data. */
    public CampaignData(CampaignOptions campaignOptions) {
        cd = this;
        this.campaignOptions = campaignOptions;
    }

    /** Generate the campaign data from an binary stream. */
    public CampaignData(CampaignOptions campaignOptions, BinReader in) throws IOException {
        this(campaignOptions);
        int size = in.readInt("terrains.size");
        for (int i = 0; i < size; ++i) {
            Terrain pe = new Terrain();
            pe.binIn(in, this);
            addTerrain(pe);
        }
        int Advsize = in.readInt("advTerrains.size");
        for (int i = 0; i < Advsize; ++i) {
            AdvancedTerrain pe = new AdvancedTerrain();
            pe.binIn(in);
            addAdvancedTerrain(pe);
        }

        size = in.readInt("factions.size");
        for (int i = 0; i < size; ++i) {
            addHouse(new House(in));
        }

        size = in.readInt("planets.size");
        for (int i = 0; i < size; ++i) {
            addPlanet(new Planet(in, this));
        }
    }

    /**
     * @see common.TerrainProvider#getTerrain(int)
     */
    public Terrain getTerrain(int id) {
        return terrains.get(id);
    }

    /**
     * @see common.TerrainProvider#getAllTerrains()
     */
    public Collection<Terrain> getAllTerrains() {
        return terrains.values();
    }

    /**
     * @see common.TerrainProvider#addTerrain(common.PlanetEnvironment)
     */
    public void addTerrain(Terrain terrain) {
        terrains.put(terrain);
    }

    public Terrain getTerrainByName(String name) {
        return terrains.getByName(name);
    }

    /*adding the advanced terrain to the campaign data*/
    /**
     * @see common.TerrainProvider#getAdvancedTerrain(int)
     */
    public AdvancedTerrain getAdvancedTerrain(int id) {
        return advancedTerrains.get(id);
    }

    /**
     * @see common.TerrainProvider#getAllTerrains()
     */
    public Collection<AdvancedTerrain> getAllAdvancedTerrains() {
        return advancedTerrains.values();
    }

    /**
     * @see common.TerrainProvider#addTerrain(common.PlanetEnvironment)
     */
    public void addAdvancedTerrain(AdvancedTerrain newAdvTerrain) {
        advancedTerrains.put(newAdvTerrain);
    }

    public AdvancedTerrain getAdvancedTerrainByName(String name) {
        return advancedTerrains.getByName(name);
    }

    public void setBannedTargetingSystems(Vector<Integer> ban) {
        bannedTargetingSystems = ban;
    }

    public Vector<Integer> getBannedTargetingSystems() {
        return bannedTargetingSystems;
    }

    /**
     * extracts data from the BinReader and places it into the client side hash table.
     *
     * @param in
     * @param userLevel
     */
    public void importAccessLevels(BinReader in) {
        HashMap<String, Integer> commandTemp = getCommandTable();

        try {
            int size = in.readInt("CommandSize");
            for (int pos = 0; pos < size; pos++) {
                String commandName = in.readLine("CommandName");
                int accessLevel = in.readInt("AccessLevel");
                commandTemp.put(commandName, accessLevel);
            }
        } catch (Exception ex) {
            LOGGER.error("Unable to import acccess levels", ex);
        } // in is empty move on.
        setCommandTable(commandTemp);
    }

    public void setCommandTable(HashMap<String, Integer> commands) {
        this.commands = commands;
    }

    public HashMap<String, Integer> getCommandTable() {
        return commands;
    }

    public int getAccessLevel(String command) {
        int level = 200;

        if (getCommandTable().get(command.toUpperCase()) != null) {
            level = getCommandTable().get(command.toUpperCase()).intValue();
        }

        return level;
    }

    public TreeMap<String, String> getPlanetOpFlags() {
        return planetOpFlags;
    }

    public CampaignOptions getCampaignOptions() {
        return campaignOptions;
    }

    public boolean targetSystemIsBanned(int id) {
        if (bannedTargetingSystems.contains(id)) {
            return true;
        }
        return false;
    }

    public House getHouseFromPartialString(String houseString) {
        // store matches so we can tell player if there's more than one
        int numMatches = 0;
        House theMatch = null;

        for (House currH : getAllHouses()) {
            House shouse = (House) currH;
            // exact match
            if (shouse.getName().equals(houseString)) {
                return shouse;
            }

            // store all matches
            if (shouse.getName().startsWith(houseString)) {
                theMatch = shouse;
                numMatches++;
            }
        }

        // only one match! send it back.
        return theMatch;
    }

    /**
     * Returns the HouseOptions for the given House.
     *
     * @return The {@link HouseOptions} for the given {@link House}
     */
    public HouseOptions getHouseOptions(String house) {
        return houseOptionsMap.get(house);
    }

    /**
     * @param path The path of the file to load from
     * @param house The house to store the config for
     */
    public void loadHouseOptions(Path path, House house) {
        if (houseOptionsMap.get(house.getName()) != null) {
            return;
        }

        houseOptionsMap.put(house.getName(), new HouseOptions(path));
    }
}
