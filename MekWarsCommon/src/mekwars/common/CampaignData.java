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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;
import megamek.common.AmmoType;
import mekwars.common.util.BinReader;
import mekwars.common.util.BinWriter;
import mekwars.common.persistence.NamedEntityStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * TODO: It seems, that all operations done here are needed independly of the
 * semantic of the underlying structure. Planets are handled equal to factions
 * and each function is doubled. If this is true, it should be managed in an
 * generic way to reduce code bloat and code replication.
 * Campaign is the base of the data holding classes for client and server. Here
 * all campaign relevant information as Houses, Planets and Player data is
 * stored.
 * In this base class some methods are provided to retrieve these informations
 * to use in common data classes like House or Planet when reffering to
 * ressources.
 * Notice: Please read the doc to binOut before adding new data types.
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

    private Hashtable<String, String> ServerBannedAmmo = new Hashtable<>();
    private Vector<Integer> bannedTargetingSystems = new Vector<>();
    private Hashtable<String, Integer> commands = new Hashtable<>();
    private TreeMap<String, String> planetOpFlags = new TreeMap<>();

    private Properties serverConfigs = new Properties();

    /**
     * Retrieve a specific planet.
     * 
     * @param id
     *            The id of the planet.
     * @return The requested Planet. This is usually a subclass of Planet.
     */
    public Planet getPlanet(int id) {
        return planets.get(id);
    }

    /**
     * Retrieve a planet by its name. Please try to use planet Id's when lookup
     * for a planet instead (if you have the choice).
     */
    public Planet getPlanetByName(String name) {
        return planets.getByName(name);
    }

    /**
     * @author jtighe Retrieve a factory by its name.
     * 
     */
    public UnitFactory getFactoryByName(Planet p, String name) {
        for (UnitFactory e : p.getUnitFactories()) {
            if (e.getName().equalsIgnoreCase(name)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Retrieves all planets.
     */
    public Collection<Planet> getAllPlanets() {
        return planets.values();
    }

    /**
     * Adds a planet to the campaign storage. If it was already within the
     * storage, it replaces the old object.
     * 
     * @param planet
     *            The planet to hold.
     * @see You should use XStream to initialize CampaignData
     */
    public void addPlanet(Planet planet) {
        planets.put(planet);
    }

    /**
     * BUMM - Blow up a planet.
     * 
     * @param id
     *            The id of the blown up planet.
     */
    public void removePlanet(int id) {
        planets.remove(id);
    }

    /**
     * Remove all planets.
     */
    public void clearPlanets() {
        planets.clear();
    }

    /**
     * Retrieve a specific faction.
     * 
     * @param id
     *            The id of the House.
     * @return The requested faction.
     */
    public House getHouse(int ID) {
        return factions.get(ID);
    }

    /**
     * Retrieves all factions.
     */
    public Collection<House> getAllHouses() {
        return factions.values();
    }

    /**
     * Adds a faction to the campaign storage. If it was already within the
     * storage, it replaces the old object.
     * 
     * @param planet
     *            The faction to hold.
     * @TODO You should use XStream to initialize CampaignData
     */
    public void addHouse(House faction) {
        factions.put(faction);
    }

    /**
     * Remove a house from the server this is normally only for single faction
     * servers
     * 
     * @param Integer
     *            id
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
     * @return
     * @TODO This seems to be only needed, because some serialization work with
     *       transmitting the factions name instead of its id.
     */
    public House getHouseByName(String name) {
        return factions.getByName(name);
    }

    /**
     * Remove all factions.
     */
    public void clearHouses() {
        factions.clear();
    }

    /**
     * Since I have no idea how TinyXML is operating and since McWizard does not
     * allow me to use my loved JDom and finally since Enkel does not like
     * XML-Transfer anyway, I use this to encode/decode the whole object.. (Imi)
     * 
     * There is another aspect of binOut to keep in mind. Since a MD5 hash is
     * build after each differential update to keep the data in sync, this
     * function has to provide THE SAME output each time it is run, regardless
     * of the underlying virtual machine. Currently this is done by only using
     * container classes, that remain the elements in a stable order. If you
     * need to add a container with unstable order (as Hash*), you have to make
     * sure, the data is odered before writing it out with binOut.
     * 
     * TODO: check http://jira.codehaus.org/secure/ViewIssue.jspa?key=XSTR-27 to
     * see whether a better way of serialization is available ;-)
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

    /**
     * Create empty campaign data.
     */
    public CampaignData() {
        cd = this;
    }

    /**
     * Generate the campaign data from an binary stream.
     */
    public CampaignData(BinReader in) throws IOException {
        cd = this;
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

    /**
     * @author Torren (Jason Tighe)
     * 
     *         this returns a hashtable of all current MM munitions 06/10/05
     *         using the Name of the munition as the key
     * @return Hashtable
     */
    public Hashtable<String, AmmoType.Munitions> getMunitionsByName() {
        Hashtable<String, AmmoType.Munitions> munitions = new Hashtable<String, AmmoType.Munitions>();

        munitions.put("Standard", AmmoType.Munitions.M_STANDARD);

        // AC Munition Types
        munitions.put("LBX Cluster", AmmoType.Munitions.M_CLUSTER);
        munitions.put("AC Armor Piercing", AmmoType.Munitions.M_ARMOR_PIERCING);
        munitions.put("AC Flechette", AmmoType.Munitions.M_FLECHETTE);
        munitions.put("AC Incendiary", AmmoType.Munitions.M_INCENDIARY_AC);
        munitions.put("AC Precision", AmmoType.Munitions.M_PRECISION);
        munitions.put("AC Tracer", AmmoType.Munitions.M_TRACER);

        // ATM Munition Types
        munitions.put("ATM Extended Range", AmmoType.Munitions.M_EXTENDED_RANGE);
        munitions.put("ATM High Explosive", AmmoType.Munitions.M_HIGH_EXPLOSIVE);

        // LRM & SRM Munition Types
        munitions.put("LRM/SRM Fragmentation", AmmoType.Munitions.M_FRAGMENTATION);
        munitions.put("LRM/SRM Listen Kill", AmmoType.Munitions.M_LISTEN_KILL);
        munitions.put("LRM/SRM Anti-TSM", AmmoType.Munitions.M_ANTI_TSM);
        munitions.put("LRM/SRM Narc", AmmoType.Munitions.M_NARC_CAPABLE);
        munitions.put("LRM/SRM Artemis", AmmoType.Munitions.M_ARTEMIS_CAPABLE);
        munitions.put("LRM/SRM Heat-Seeking", AmmoType.Munitions.M_HEAT_SEEKING);
        munitions.put("LRM/SRM Dead-Fire", AmmoType.Munitions.M_DEAD_FIRE);
        munitions.put("LRM/SRM Tandem-Charge", AmmoType.Munitions.M_TANDEM_CHARGE);

        // LRM Munition Types
        // Incendiary is special, though...
        munitions.put("LRM Incendiary", AmmoType.Munitions.M_INCENDIARY_LRM);
        munitions.put("LRM Flare", AmmoType.Munitions.M_FLARE);
        munitions.put("LRM SemiGuided", AmmoType.Munitions.M_SEMIGUIDED);
        munitions.put("LRM Swarm", AmmoType.Munitions.M_SWARM);
        munitions.put("LRM Swarm I", AmmoType.Munitions.M_SWARM_I);
        munitions.put("LRM Thunder", AmmoType.Munitions.M_THUNDER);
        munitions.put("LRM Thunder Augmented", AmmoType.Munitions.M_THUNDER_AUGMENTED);
        munitions.put("LRM Thunder Inferno", AmmoType.Munitions.M_THUNDER_INFERNO);
        munitions.put("LRM Thunder VibraBomb", AmmoType.Munitions.M_THUNDER_VIBRABOMB);
        munitions.put("LRM Thunder Active", AmmoType.Munitions.M_THUNDER_ACTIVE);
        munitions.put("LRM Follow The Leader", AmmoType.Munitions.M_FOLLOW_THE_LEADER);
        munitions.put("Multi Purpose", AmmoType.Munitions.M_MULTI_PURPOSE);

        // SRM Munition Types
        munitions.put("SRM Inferno", AmmoType.Munitions.M_INFERNO);
        munitions.put("SRM Acid", AmmoType.Munitions.M_AX_HEAD);

        // Torps
        munitions.put("LRT/SRT", AmmoType.Munitions.M_TORPEDO);

        // iNarc Munition Types
        munitions.put("iNarc Explosive", AmmoType.Munitions.M_EXPLOSIVE);
        munitions.put("iNarc ECM", AmmoType.Munitions.M_ECM);
        munitions.put("iNarc HayWire", AmmoType.Munitions.M_HAYWIRE);
        munitions.put("iNarc Nemesis", AmmoType.Munitions.M_NEMESIS);

        // Narc Munition Types
        munitions.put("Narc Explosive", AmmoType.Munitions.M_NARC_EX);

        // Arrow IV Munition Types
        munitions.put("Arrow IV Homing", AmmoType.Munitions.M_HOMING);
        munitions.put("Arrow IV FASCAM", AmmoType.Munitions.M_FASCAM);
        munitions.put("Arrow IV Inferno", AmmoType.Munitions.M_INFERNO_IV);
        munitions.put("Arrow IV VibraBomb", AmmoType.Munitions.M_VIBRABOMB_IV);
        munitions.put("Arrow IV Smoke", AmmoType.Munitions.M_SMOKE);
        munitions.put("Arrow IV Davy Crockett", AmmoType.Munitions.M_DAVY_CROCKETT_M);
        return munitions;
    }

    /**
     * @author Torren (Jason Tighe)
     * 
     *         this returns a hashtable of all current MM munitions 06/10/05
     *         using the Number of the munition as the key
     * @return Hashtable
     */
    public Hashtable<AmmoType.Munitions, String> getMunitionsByNumber() {
        Hashtable<AmmoType.Munitions, String> munitions = new Hashtable<AmmoType.Munitions, String>();

        munitions.put(AmmoType.Munitions.M_STANDARD, "Standard");

        // AC Munition Types
        munitions.put(AmmoType.Munitions.M_CLUSTER, "LBX Cluster");
        munitions.put(AmmoType.Munitions.M_ARMOR_PIERCING, "AC Armor Piercing");
        munitions.put(AmmoType.Munitions.M_FLECHETTE, "AC Flechette");
        munitions.put(AmmoType.Munitions.M_INCENDIARY_AC, "AC Incendiary");
        munitions.put(AmmoType.Munitions.M_PRECISION, "AC Precision");
        munitions.put(AmmoType.Munitions.M_TRACER, "AC Tracer");

        // ATM Munition Types
        munitions.put(AmmoType.Munitions.M_EXTENDED_RANGE, "ATM Extended Range");
        munitions.put(AmmoType.Munitions.M_HIGH_EXPLOSIVE, "ATM High Explosive");

        // LRM & SRM Munition Types
        munitions.put(AmmoType.Munitions.M_FRAGMENTATION, "LRM/SRM Fragmentation");
        munitions.put(AmmoType.Munitions.M_LISTEN_KILL, "LRM/SRM Listen Kill");
        munitions.put(AmmoType.Munitions.M_ANTI_TSM, "LRM/SRM Anti-TSM");
        munitions.put(AmmoType.Munitions.M_NARC_CAPABLE, "LRM/SRM Narc");
        munitions.put(AmmoType.Munitions.M_ARTEMIS_CAPABLE, "LRM/SRM Artemis");
        munitions.put(AmmoType.Munitions.M_HEAT_SEEKING, "LRM/SRM Heat-Seeking");
        munitions.put(AmmoType.Munitions.M_TANDEM_CHARGE, "LRM/SRM Tandem-Charge");
        munitions.put(AmmoType.Munitions.M_DEAD_FIRE, "LRM/SRM Dead-Fire");

        // LRM Munition Types
        // Incendiary is special though...
        munitions.put(AmmoType.Munitions.M_INCENDIARY_LRM, "LRM Incendiary");
        munitions.put(AmmoType.Munitions.M_FLARE, "LRM Flare");
        munitions.put(AmmoType.Munitions.M_SEMIGUIDED, "LRM SemiGuided");
        munitions.put(AmmoType.Munitions.M_SWARM, "LRM Swarm");
        munitions.put(AmmoType.Munitions.M_SWARM_I, "LRM Swarm I");
        munitions.put(AmmoType.Munitions.M_THUNDER, "LRM Thunder");
        munitions.put(AmmoType.Munitions.M_THUNDER_AUGMENTED, "LRM Thunder Augmented");
        munitions.put(AmmoType.Munitions.M_THUNDER_INFERNO, "LRM Thunder Inferno");
        munitions.put(AmmoType.Munitions.M_THUNDER_VIBRABOMB, "LRM Thunder VibraBomb");
        munitions.put(AmmoType.Munitions.M_THUNDER_ACTIVE, "LRM Thunder Active");
        munitions.put(AmmoType.Munitions.M_FOLLOW_THE_LEADER, "LRM Follow The Leader");
        munitions.put(AmmoType.Munitions.M_MULTI_PURPOSE, "Multi Purpose");

        // SRM Munition Types
        munitions.put(AmmoType.Munitions.M_INFERNO, "SRM Inferno");
        munitions.put(AmmoType.Munitions.M_AX_HEAD, "SRM Acid");

        // Torps
        munitions.put(AmmoType.Munitions.M_TORPEDO, "LRT/SRT");

        // iNarc Munition Types
        munitions.put(AmmoType.Munitions.M_EXPLOSIVE, "iNarc Explosive");
        munitions.put(AmmoType.Munitions.M_ECM, "iNarc ECM");
        munitions.put(AmmoType.Munitions.M_HAYWIRE, "iNarc HayWire");
        munitions.put(AmmoType.Munitions.M_NEMESIS, "iNarc Nemesis");

        // Narc Munition Types
        munitions.put(AmmoType.Munitions.M_NARC_EX, "Narc Explosive");

        // Arrow IV Munition Types
        munitions.put(AmmoType.Munitions.M_HOMING, "Arrow IV Homing");
        munitions.put(AmmoType.Munitions.M_FASCAM, "Arrow IV FASCAM");
        munitions.put(AmmoType.Munitions.M_INFERNO_IV, "Arrow IV Inferno");
        munitions.put(AmmoType.Munitions.M_VIBRABOMB_IV, "Arrow IV VibraBomb");
        munitions.put(AmmoType.Munitions.M_SMOKE, "Arrow IV Smoke");
        munitions.put(AmmoType.Munitions.M_DAVY_CROCKETT_M, "Arrow IV Davy Crockett");
        return munitions;
    }

    public void setServerBannedAmmo(Hashtable<String, String> ban) {
        ServerBannedAmmo = ban;
    }

    public Hashtable<String, String> getServerBannedAmmo() {
        return ServerBannedAmmo;
    }

    public void setBannedTargetingSystems(Vector<Integer> ban) {
        bannedTargetingSystems = ban;
    }

    public Vector<Integer> getBannedTargetingSystems() {
        return bannedTargetingSystems;
    }

    /**
     * extracts data from the BinReader and places it into the client side hash
     * table.
     * 
     * @param in
     * @param userLevel
     */
    public void importAccessLevels(BinReader in) {
        Hashtable<String, Integer> commandTemp = getCommandTable();

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

    public void setCommandTable(Hashtable<String, Integer> commands) {
        this.commands = commands;
    }

    public Hashtable<String, Integer> getCommandTable() {
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

    public Properties getServerConfigs() {
        return serverConfigs;
    }

    public void setServerConfigs(Properties configs) {
        serverConfigs = configs;
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
}
