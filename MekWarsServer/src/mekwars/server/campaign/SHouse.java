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

package mekwars.server.campaign;

import megamek.common.Entity;
import megamek.common.TechConstants;

import mekwars.common.BMEquipment;
import mekwars.common.CampaignData;
import mekwars.common.Planet;
import mekwars.common.SubFaction;
import mekwars.common.Unit;
import mekwars.common.campaign.CampaignOptions;
import mekwars.common.campaign.ComponentList;
import mekwars.common.composition.HasUnits;
import mekwars.common.util.ComponentToCritsConverter;
import mekwars.common.util.StringUtils;
import mekwars.common.util.TokenReader;
import mekwars.common.util.UnitComponents;
import mekwars.common.util.UnitUtils;
import mekwars.common.util.RandomUtils;
import mekwars.server.MWServ;
import mekwars.server.campaign.commands.Command;
import mekwars.server.campaign.data.TimeUpdateHouse;
import mekwars.server.campaign.market2.IBuyer;
import mekwars.server.campaign.market2.ISeller;
import mekwars.server.campaign.mercenaries.ContractInfo;
import mekwars.server.campaign.mercenaries.MercHouse;
import mekwars.server.campaign.pilot.SPilot;
import mekwars.server.campaign.util.SerializedMessage;
import mekwars.server.io.FileSystem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A class holding a server-side representation of a House
 *
 * @author Helge Richter (McWizard)
 * @author Torren
 * @author Bob Eldred (Spork)
 * @version 2016.10.06
 *     <p>Modifications: - Changed addActivityPP to just keep track of PP, which is then used at the
 *     tick. Looping through the planets was taking way too long. - Moved component prodution to
 *     addActivityPP to enable access from a Quartz task
 */
public class SHouse extends TimeUpdateHouse
        implements Comparable<Object>, ISeller, IBuyer, Serializable {
    private static final Logger LOGGER = LogManager.getLogger(SHouse.class);

    protected HasUnits<SUnit> hangar = new HasUnits<>();
    // store all online players in *THREE* hashes, one for each primary status
    private ConcurrentHashMap<String, SPlayer> reservePlayers =
            new ConcurrentHashMap<String, SPlayer>();
    private ConcurrentHashMap<String, SPlayer> activePlayers =
            new ConcurrentHashMap<String, SPlayer>();
    private ConcurrentHashMap<String, SPlayer> fightingPlayers =
            new ConcurrentHashMap<String, SPlayer>();

    private ConcurrentHashMap<String, SPlanet> planets = new ConcurrentHashMap<String, SPlanet>();

    private Map<String, SmallPlayer> smallPlayers = new HashMap<String, SmallPlayer>();
    private ComponentList components = new ComponentList();
    private Map<Integer, Integer> unitComponents = new HashMap<Integer, Integer>();

    private int money;
    private int baysProvided = 0;
    private int componentProduction = 0;
    private int showProductionCountNext = 0;
    private int initialHouseRanking = 0;

    private String motd = "";
    private String announcement = "";

    private PilotQueues pilotQueues = new PilotQueues(this);

    private List<String> leaders = new ArrayList<>();
    private int techResearchPoints = 0;
    private UnitComponents unitParts = new UnitComponents();
    private Map<String, ComponentToCritsConverter> componentConverter =
            new HashMap<String, ComponentToCritsConverter>();

    private double activityPP = 0.0;

    @Override
    public String toString() {
        SerializedMessage result = new SerializedMessage("|");
        // result.append("HS�");
        result.append(getName());
        result.append(getMoney());
        result.append(getHouseColor());
        result.append(getBasePilotStats().getGunnery());
        result.append(getBasePilotStats().getPiloting());
        result.append(getAbbreviation());

        result.append(hangar.count());
        for (SUnit unit : hangar.getAll()) {
            result.append(unit.toString(false));
        }
        result.append(getLogo());

        if ("".equals(getAnnouncement())) {
            result.append(" ");
        } else {
            result.append(stripReturns(getAnnouncement()));
        }

        // Write the Components / BuildingPP's
        result.append("Components");
        getComponents().forEach(component -> result.append(component.getProductionPoints()));
        result.append("EndComponents");

        result.append(getInitialHouseRanking());
        result.append(isConquerable());
        result.append(getId());
        result.append(getHousePlayerColor());
        result.append(getHouseDefectionFrom());
        result.append(getPilotQueues().getQueueSize(Unit.MEK)); // Mek pilots first
        List<SPilot> PilotList = getPilotQueues().getPilotQueue(Unit.MEK);
        for (SPilot currP : PilotList) {
            result.append(currP.toFileFormat("#", false));
        } // veehs next
        result.append(getPilotQueues().getQueueSize(Unit.VEHICLE));
        PilotList = getPilotQueues().getPilotQueue(Unit.VEHICLE);
        for (SPilot currP : PilotList) {
            result.append(currP.toFileFormat("#", false));
        } // inf
        result.append(getPilotQueues().getQueueSize(Unit.INFANTRY));
        PilotList = getPilotQueues().getPilotQueue(Unit.INFANTRY);
        for (SPilot currP : PilotList) {
            result.append(currP.toFileFormat("#", false));
        }

        result.append(getHouseFluFile());

        // Store BattleArmor (Pilots)
        result.append(getPilotQueues().getQueueSize(Unit.BATTLEARMOR));
        PilotList = getPilotQueues().getPilotQueue(Unit.BATTLEARMOR);
        for (SPilot currPilot : PilotList) {
            result.append(currPilot.toFileFormat("#", false));
        }

        // Store ProtoMeks (Pilots)
        result.append(getPilotQueues().getQueueSize(Unit.PROTOMEK));
        PilotList = getPilotQueues().getPilotQueue(Unit.PROTOMEK);
        for (SPilot currPilot : PilotList) {
            result.append(currPilot.toFileFormat("#", false));
        }

        // Save faction MOTD
        if (getMotd().equals("")) {
            result.append(" ");
        } else {
            result.append(stripReturns(getMotd()));
        }

        result.append(getHouseDefectionTo());

        for (int pos = 0; pos < Unit.MAXBUILD; pos++) {
            result.append(getBasePilotStats().getGunnery(pos));
            result.append(getBasePilotStats().getPiloting(pos));
        }

        for (int pos = 0; pos < Unit.MAXBUILD; pos++) {
            String skill = getBasePilotStats().getSkills(pos);
            if (skill.length() < 1) {
                result.append(" ");
            } else {
                result.append(skill);
            }
        }

        result.append(getTechLevel());

        result.append(getSubfactions().size());

        for (SubFaction subfaction : getSubfactions()) {
            result.append(subfaction.toString());
        }

        result.append(leaders.size());
        for (String leader : leaders) {
            result.append(leader);
        }
        result.append(techResearchPoints);
        result.append(unitParts.toString("#"));
        result.append(componentConverter.size());

        for (String key : componentConverter.keySet()) {
            result.append(componentConverter.get(key).toString());
        }

        // Store Aero (Pilots)
        result.append(getPilotQueues().getQueueSize(Unit.AERO));
        PilotList = getPilotQueues().getPilotQueue(Unit.AERO);
        for (SPilot currPilot : PilotList) {
            result.append(currPilot.toFileFormat("#", false));
        }

        return result.toString();
    }

    /**
     * Carriage returns in the MOTD causing problems in house saves.
     *
     * @param motd
     * @return sanitized String
     */
    private String stripReturns(String motd) {
        return motd.replaceAll("[\\r\\n]", "");
    }

    public ComponentList getComponents() {
        return components;
    }

    public String fromString(String s, Random r) {
        try {

            StringTokenizer ST = new StringTokenizer(s, "|");
            setName(TokenReader.readString(ST));
            Path configPath = FileSystem.getInstance().getFactionConfigPath(getName());

            CampaignData.cd.loadHouseOptions(configPath, this);

            setMoney(TokenReader.readInt(ST));
            setHouseColor(TokenReader.readString(ST));
            getBasePilotStats().setGunnery(TokenReader.readInt(ST), Unit.MEK);
            getBasePilotStats().setPiloting(TokenReader.readInt(ST), Unit.MEK);

            setAbbreviation(TokenReader.readString(ST));

            boolean newbieHouse = isNewbieHouse();

            // Read units
            int hangarSize = TokenReader.readInt(ST);
            for (int i = 0; i < hangarSize; i++) {
                SUnit unit = new SUnit();
                unit.fromString(TokenReader.readString(ST));

                if (newbieHouse) {
                    int priceForUnit = getPriceForUnit(unit.getWeightClass(), unit.getType());
                    int rareSalesTime = Integer.parseInt(this.getConfig("RareMinSaleTime"));
                    CampaignMain.cm
                            .getMarket()
                            .addListing("Faction_" + getName(), unit, priceForUnit, rareSalesTime);
                    unit.setStatus(Unit.STATUS_FORSALE);
                }
                addUnit(unit, false);
            }

            setLogo(TokenReader.readString(ST));
            setAnnouncement(TokenReader.readString(ST));

            /*
             * Another bad-old-code feature. "Components" will be the next token
             * on any modern server. Loop remains in case someone tries to use
             * old MMNET data with players saved in-line.
             */
            String next = TokenReader.readString(ST);
            while (!next.equals("Components")) {
                next = TokenReader.readString(ST);
            }

            getComponents().fromString(ST);
            next = TokenReader.readString(ST);

            setInitialHouseRanking(TokenReader.readInt(ST));
            setConquerable(TokenReader.readBoolean(ST));

            TokenReader.readString(ST);
            String housePlayerColor = TokenReader.readString(ST);
            try {
                int redColor = Integer.parseInt(housePlayerColor);
                int greenColor = TokenReader.readInt(ST);
                int blueColor = TokenReader.readInt(ST);

                setHousePlayerColors(
                        Integer.toHexString(redColor)
                                + Integer.toHexString(greenColor)
                                + Integer.toHexString(blueColor));
            } catch (Exception ex) {
                setHousePlayerColors(housePlayerColor);
            }

            setHouseDefectionFrom(TokenReader.readBoolean(ST));

            // meks
            int pilotCount = TokenReader.readInt(ST);
            for (; pilotCount > 0; pilotCount--) {
                SPilot p = new SPilot();
                p.fromFileFormat(TokenReader.readString(ST), "#");
                getPilotQueues().loadPilot(Unit.MEK, p);
            }

            // vees
            pilotCount = TokenReader.readInt(ST);
            for (; pilotCount > 0; pilotCount--) {
                SPilot p = new SPilot();
                p.fromFileFormat(TokenReader.readString(ST), "#");
                getPilotQueues().loadPilot(Unit.VEHICLE, p);
            }

            // inf
            pilotCount = TokenReader.readInt(ST);
            for (; pilotCount > 0; pilotCount--) {
                SPilot p = new SPilot();
                p.fromFileFormat(TokenReader.readString(ST), "#");
                getPilotQueues().loadPilot(Unit.INFANTRY, p);
            }

            setHouseFluFile(TokenReader.readString(ST));

            // BattleArmor
            pilotCount = TokenReader.readInt(ST);
            for (; pilotCount > 0; pilotCount--) {
                SPilot p = new SPilot();
                p.fromFileFormat(TokenReader.readString(ST), "#");
                getPilotQueues().loadPilot(Unit.BATTLEARMOR, p);
            }

            // ProtoMeks
            pilotCount = TokenReader.readInt(ST);
            for (; pilotCount > 0; pilotCount--) {
                SPilot p = new SPilot();
                p.fromFileFormat(TokenReader.readString(ST), "#");
                getPilotQueues().loadPilot(Unit.PROTOMEK, p);
            }

            setMotd(TokenReader.readString(ST));

            setHouseDefectionTo(TokenReader.readBoolean(ST));

            try {
                for (int pos = 0; pos < Unit.MAXBUILD; pos++) {
                    getBasePilotStats().setGunnery(TokenReader.readInt(ST), pos);
                    getBasePilotStats().setPiloting(TokenReader.readInt(ST), pos);
                }
            } catch (Exception ex) {
                setPilotQueues(new PilotQueues(this));
            }

            try {
                for (int pos = 0; pos < Unit.MAXBUILD; pos++) {
                    String skill = TokenReader.readString(ST);
                    getBasePilotStats().setSkills(skill, pos);
                }
            } catch (Exception ex) {
                setPilotQueues(new PilotQueues(this));
            }

            setTechLevel(TokenReader.readInt(ST));

            int amount = TokenReader.readInt(ST);

            for (; amount > 0; amount--) {
                SubFaction newSubFaction = new SubFaction();
                newSubFaction.fromString(TokenReader.readString(ST));
                addSubfaction(newSubFaction);
            }

            amount = TokenReader.readInt(ST);
            for (; amount > 0; amount--) {
                leaders.add(TokenReader.readString(ST));
            }

            techResearchPoints = TokenReader.readInt(ST);

            if (CampaignMain.cm.getBooleanConfig("UsePartsRepair")) {
                unitParts.fromString(TokenReader.readString(ST), "#");
            } else {
                TokenReader.readString(ST);
            }

            int size = TokenReader.readInt(ST);
            for (; size > 0; size--) {
                ComponentToCritsConverter converter = new ComponentToCritsConverter();
                converter.setCritName(TokenReader.readString(ST));
                converter.setMinCritLevel(TokenReader.readInt(ST));
                converter.setComponentUsedType(TokenReader.readInt(ST));
                converter.setComponentUsedWeight(TokenReader.readInt(ST));
            }

            // Aero's
            pilotCount = TokenReader.readInt(ST);
            for (; pilotCount > 0; pilotCount--) {
                SPilot p = new SPilot();
                p.fromFileFormat(TokenReader.readString(ST), "#");
                getPilotQueues().loadPilot(Unit.AERO, p);
            }

            if (getComponentConverter().isEmpty()
                    && CampaignMain.cm.getBooleanConfig("UsePartsRepair")) {
                ComponentToCritsConverter converter = new ComponentToCritsConverter();
                converter.setComponentUsedType(SUnit.MEK);
                converter.setComponentUsedWeight(SUnit.LIGHT);
                converter.setMinCritLevel(100);
                getComponentConverter().put(converter.getCritName(), converter);
            }

            setPilotQueues(new PilotQueues(this));
            // faction name
            // for the queue

            // Stuff for MercHouse.. Has to be here until someone tells me how
            // to move it :) - McWiz
            if (isMercHouse()) {
                int contractamount = 0;

                contractamount = TokenReader.readInt(ST);
                Map<String, ContractInfo> merctable = new HashMap<String, ContractInfo>();
                for (int i = 0; i < contractamount; i++) {
                    ContractInfo ci = new ContractInfo();
                    ci.fromString(TokenReader.readString(ST));
                    merctable.put(ci.getPlayerName(), ci);
                }
                ((MercHouse) this).setOutstandingContracts(merctable);
            }

            setUsedMekBayMultiplier(Float.parseFloat(getConfig("UsedPurchaseCostMulti")));
            return s;
        } catch (Exception ex) {
            LOGGER.error("Exception: ", ex);
            LOGGER.error("Error while loading faction: " + getName() + " Going forward anyway ...");
            return s;
        }
    }

    /** Constructor used for serialization */
    public SHouse() {
        super();
        reservePlayers = new ConcurrentHashMap<String, SPlayer>();
        activePlayers = new ConcurrentHashMap<String, SPlayer>();
        fightingPlayers = new ConcurrentHashMap<String, SPlayer>();
        smallPlayers = new HashMap<String, SmallPlayer>();
    }

    /*
     * Players are stores in 3 seperate hashtables. Each hash is indicative of a
     * different activity level. As players move back and forth between these
     * levels, they are transferred from hash to hash. At NO TIME should a
     * player exist in multiple hashes.
     *
     * This 3-hash system replaces the old fighting/logged in 2 hash system and
     * the SPlayer's activity boolean.
     *
     * TODO: massively improve commenting here. @urgru 1.14.06
     */
    public ConcurrentHashMap<String, SPlayer> getReservePlayers() {
        return reservePlayers;
    }

    public ConcurrentHashMap<String, SPlayer> getActivePlayers() {
        return activePlayers;
    }

    public ConcurrentHashMap<String, SPlayer> getFightingPlayers() {
        return fightingPlayers;
    }

    public int getBaysProvided() {
        return baysProvided;
    }

    public int getComponentProduction() {
        return componentProduction;
    }

    public void setPilotQueues(PilotQueues q) {
        pilotQueues = q;
    }

    public SHouse(
            String name, String houseColor, int baseGunner, int basePilot, String abbreviation) {
        super();
        setAbbreviation(abbreviation);
        setHouseColor(houseColor);
        setName(name);

        setMoney(0);
    }

    public List<SUnit> getHangar() {
        return hangar.getAll();
    }

    public List<SUnit> getHangar(int typeId) {
        List<SUnit> unitList =
                hangar.getAll().stream()
                        .filter(unit -> unit.getType() == typeId)
                        .collect(Collectors.toList());
        return Collections.unmodifiableList(unitList);
    }

    public void clearHangar() {
        hangar.clear();
    }

    public boolean isNewbieHouse() {
        return false;
    }

    public boolean isMercHouse() {
        return false;
    }

    public SHouse getHouseFightingFor(SPlayer player) {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        SHouse h = null;

        try {
            h = (SHouse) o;
        } catch (ClassCastException e) {
            return false;
        }

        if (h == null) {
            return false;
        }

        if (h.getName().equals(getName())) {
            return true;
        }

        return false;
    }

    public void addDispossessedPilot(SUnit u, boolean skipSkillChange) {

        if (u.hasVacantPilot()) {
            return;
        }

        if (skipSkillChange) {
            getPilotQueues().addPilot(u.getType(), (SPilot) u.getPilot(), true);
        } else {
            // normal de-levalling addition
            getPilotQueues().addPilot(u.getType(), (SPilot) u.getPilot());
        }
    }

    public PilotQueues getPilotQueues() {
        return pilotQueues;
    }

    public SPilot getNewPilot(int uType) {
        SPilot pilot = getPilotQueues().getPilot(uType);
        return pilot;
    }

    /**
     * Method which checks all three activity states to see if a player w/ a given name is logged in
     * to the faction
     */
    public boolean isLoggedIntoFaction(String playerName) {

        String lowerName = playerName.toLowerCase();
        if (getReservePlayers().containsKey(lowerName)) {
            return true;
        } else if (getActivePlayers().containsKey(lowerName)) {
            return true;
        } else if (getFightingPlayers().containsKey(lowerName)) {
            return true;
        }

        // not in the faction under any status.
        return false;
    }

    public long remainingHangarSpaceForWeightclass(int weightClass, int typeId) {
        // don't want to count units that are for sale.
        long trueHangarSize = 
                hangar.getAll().stream()
                        .filter(unit -> unit.getType() == typeId)
                        .filter(unit -> unit.getWeightClass() == weightClass)
                        .filter(unit -> unit.getStatus() != Unit.STATUS_FORSALE)
                        .count();

        if (weightClass == Unit.LIGHT) {
            if (typeId != Unit.MEK) {
                return (getHouseOptions().getIntegerConfig("MaxLightUnits") / 2) - trueHangarSize;
            }
            return getHouseOptions().getIntegerConfig("MaxLightUnits") - trueHangarSize;
        }

        // else (nonlight weighclass)
        if (typeId != Unit.MEK) {
            return (getHouseOptions().getIntegerConfig("MaxOtherUnits") / 2) - trueHangarSize;
        }

        return getHouseOptions().getIntegerConfig("MaxOtherUnits") - trueHangarSize;
    }

    /**
     * A method to keep track of Production Points due to player activity. Called from a
     * PlayerActivityComponentJob
     *
     * @param armyWeight
     */
    public void addActivityPP(Double armyWeight) {
        activityPP += armyWeight;
        LOGGER.debug(
                "Adding "
                        + armyWeight
                        + " in production. "
                        + getName()
                        + " total now "
                        + activityPP);
    }

    public void resetActivityPP() {
        activityPP = 0;
    }

    /**
     * Returns the number of players who count for mintick production. Called from SHouse.tick().
     * Factored out to keep the tick more or less redable.
     *
     * <p>An active player counts if he has at least one army that is between min and max BVs to
     * count, and has been active for a whole tick.
     *
     * <p>Fighting players may or may not count, depending on the weight assigned by the admins to
     * the game-type they are playing.
     *
     * @return double Amount of Production produced by players
     */
    private double getNumberOfPlayersWhoCountForProduction() {
        return activityPP;
    }

    /**
     * have the faction perform tick duties (gather income, referesh factories) and clean out its
     * hangars and PP excesses (either via scrapping, industrial accidents, or BM sales), then
     * report the tick results to all of its faction members.
     */
    public String tick(boolean real, int tickid) {
        /*
         * Something in this block appears to be causing MMNet's hangs.
         * Unfortunately, it doesn't lend itself to very good logging. I'll see
         * what I can do.
         */
        LOGGER.debug("Inside SHouse.Tick for: " + getName());
        String result = "-------> <b>Tick! [" + tickid + "]</b><br>";
        StringBuilder hsUpdates = new StringBuilder();

        double tickworth = 0;

        LOGGER.debug("Getting number of players who count for production");

        // non-real ticks occur the first time a server starts, when free
        // minticks are given away
        if (!real) {
            tickworth = 10; // give 10 players worth ...
        } else {
            // if real, get the weighted number of valid players
            tickworth = getNumberOfPlayersWhoCountForProduction();
            resetActivityPP(); // Now that we have it, we need to clear it so they don't get counted
            // twice.
        }

        LOGGER.debug("     -> " + tickworth);

        LOGGER.debug("Calculating refresh points");

        // Refresh factories
        calcActivityPP(tickworth);

        /*
         * Loop throuhgh all hangars and component vectors, looking for
         * overages. Remove units (destroy or sell) and components (destroy or
         * build units) until under caps.
         *
         * This block of code was formerly SHouse.cleanUpHangarAndPP. Moved
         * inline with the rest of tick() in order to facilities house status
         * updates. @urgru 6.10.06
         */

        // strings to build on, so info can be sorted in event/type/weight order
        StringBuilder mechsProduced = new StringBuilder();
        StringBuilder industrialAccidents = new StringBuilder();
        StringBuilder scrapExcuses = new StringBuilder();
        StringBuilder marketAdditions = new StringBuilder();

        LOGGER.debug("Checking for Unit Overflow");
        /*
         * Loop though every type and weight class, looking for overflow. If
         * there are more units than allowed in the hangar, dispose of random
         * units by scrapping or selling (on Market) until back at cap.
         */
        for (int typeId = 0; typeId < Unit.TOTALTYPES; typeId++) {
            for (int i = Unit.LIGHT; i <= Unit.ASSAULT; i++) {

                // keep scrapping/selling until we're at cap.
                while (remainingHangarSpaceForWeightclass(i, typeId) < 0) {

                    final int captureTypeId = typeId;
                    final int captureWeightClass = i;
                    // get vector of units of the right weight, then select a
                    // random unit from the stack.
                    List<SUnit> v = 
                        hangar.getAll().stream()
                                .filter(unit -> unit.getType() == captureTypeId)
                                .filter(unit -> unit.getWeightClass() == captureWeightClass)
                                .collect(Collectors.toList());

                    // Get a unit.  If the SO has set the flag for selecting the oldest units,
                    // first, get that one, if not, get a random one.
                    SUnit randUnit;

                    if (CampaignMain.cm.getBooleanConfig("ScrapOldestUnitsFirst")) {
                        Collections.sort(v);
                        // Crap.  This could loop, if every unit is on the BM already.
                        // So, find the first unit thatis not already for sale
                        int unitToGet = -1;
                        for (int j = 0; j < v.size(); j++) {
                            if (unitToGet == -1 && v.get(j).getStatus() != Unit.STATUS_FORSALE) {
                                unitToGet = j;
                            }
                        }
                        if (unitToGet == -1) {
                            // Nothing to see here, move along
                            continue;
                        }
                        randUnit = v.get(unitToGet);
                    } else {
                        randUnit = v.get(CampaignMain.cm.getRandomNumber(v.size()));
                    }

                    if (randUnit.getStatus() == Unit.STATUS_FORSALE) {
                        continue;
                    }

                    int bmPercent = Integer.parseInt(this.getConfig("ChanceToSendUnitToBM"));
                    if (maySellOnBM()
                            && CampaignMain.cm.getRandomNumber(101) < bmPercent
                            && UnitUtils.mayBeSoldOnMarket(randUnit)) {

                        // Use standard factory pricing for the unit, and
                        // configured ticks.
                        // int minPrice = getPriceForUnit(i, typeId);
                        int minPrice = getBMPriceForUnit(i, typeId);
                        String saleTicksString =
                                Unit.getWeightClassDesc(randUnit.getWeightClass()) + "SaleTicks";
                        // add 1 to the sale tick due to a quirk with the BM
                        // autoupdate.
                        // The the unit is sent to the player before the new
                        // tick counter so the clients
                        // are a tick ahead of the server.
                        int saleTicks = Integer.parseInt(this.getConfig(saleTicksString)) + 1;

                        // Add the unit to the market, and tell the faction
                        CampaignMain.cm
                                .getMarket()
                                .addListing(getName(), randUnit, minPrice, saleTicks);
                        if (!Boolean.parseBoolean(CampaignMain.cm.getConfig("HiddenBMUnits"))) {
                            marketAdditions.append(
                                    StringUtils.aOrAn(randUnit.getModelName(), false)
                                            + " was added to the black market.<br>");
                        }
                        hsUpdates.append(getHSUnitRemovalString(randUnit)); // "remove"
                        // unit
                        // from
                        // client's
                        // perspective
                        randUnit.setStatus(Unit.STATUS_FORSALE);
                    } else {
                        String currScrapExcuse = getExcuseForUnitFailure(randUnit);
                        scrapExcuses.append(currScrapExcuse + "<br>");
                        hsUpdates.append(removeUnit(randUnit, false));
                    }
                } // end while(too many units)
            } // end weight class loop
        } // end unit type loop

        /*
         * Ok we've created components now lets see if we covert them into
         * crits.
         */
        if (getComponentConverter().size() > 0) {
            produceCrits();
        }

        LOGGER.debug("Doing Component Overflow");
        /*
         * Loop through all types/weightclasses as above, but look for component
         * overflow instead of hangar overage. Here we either scrap the
         * components (aka "industrial accident") or autoproduce a brand new
         * unit and drop it in the house hangar.
         *
         * We look for component overflow after hangar overflow in order to be
         * sure that newly autoproduced units aren't immediately dumped onto the
         * market or nuked.
         */
        for (int typeId = 0; typeId < Unit.TOTALTYPES; typeId++) {
            for (int weight = 0; weight < 4; weight++) {
                while (getPP(weight, typeId) > getMaxAllowedPP(weight, typeId)) {
                    int randomLossFactor =
                            CampaignMain.cm.getRandomNumber(getPPCost(weight, typeId)) + 1;

                    // see if we should have an accident
                    boolean accident = false;
                    SUnitFactory m =
                            getNativeFactoryForProduction(
                                    typeId,
                                    weight,
                                    CampaignMain.cm.getBooleanConfig(
                                            "OnlyUseOriginalFactoriesForAutoprod"));
                    int failureRateToUse;
                    if (Boolean.parseBoolean(this.getConfig("UseAutoProdClassic"))) {
                        failureRateToUse =
                                Integer.parseInt(this.getConfig("AutoProductionFailureRate"));
                    } else {
                        failureRateToUse =
                                Integer.parseInt(
                                        this.getConfig(
                                                "APFailureRate"
                                                        + Unit.getWeightClassDesc(weight)
                                                        + Unit.getTypeClassDesc(typeId)));
                    }
                    if (CampaignMain.cm.getRandomNumber(100) + 1 <= failureRateToUse) {
                        accident = true;
                    }

                    // no factory to produce, or random accident
                    if (m == null || accident) {
                        hsUpdates.append(addPP(weight, typeId, -randomLossFactor, false));
                        if (typeId == Unit.INFANTRY) {
                            industrialAccidents.append(
                                    "a cache of "
                                            + Unit.getWeightClassDesc(weight)
                                            + " "
                                            + Unit.getTypeClassDesc(typeId)
                                            + " supplies is donated to the Salvation Army.<br>");
                        } else {
                            industrialAccidents.append(
                                    "An industrial accident destroys a substantial cache of "
                                            + Unit.getWeightClassDesc(weight)
                                            + " "
                                            + Unit.getTypeClassDesc(typeId)
                                            + " components.<br>");
                        }
                    }

                    // else, make a new unit
                    else {
                        List<SUnit> newUnits = m.getMechProduced(typeId, getNewPilot(typeId));
                        for (SUnit newUnit : newUnits) {
                            LOGGER.debug("AP Unit " + newUnit.getModelName());
                            hsUpdates.append(this.addUnit(newUnit, false));
                            hsUpdates.append(
                                    addPP(weight, typeId, -(getPPCost(weight, typeId)), false));
                            /*
                             * set refresh and add to back end of the HS update.
                             * if the refresh is added in-line in the
                             * SUnitFactory, the command is sent BEFORE the
                             * final HS command, which then overwrites the
                             * correct refresh time w/ an incorrect reflesh time
                             * that reflects player activity.
                             */
                            if (!Boolean.parseBoolean(this.getConfig("UseCalculatedCosts"))) {
                                // set the refresh miniticks
                                if (m.getWeightClass() == Unit.LIGHT) {
                                    hsUpdates.append(
                                            m.addRefresh(
                                                    (Integer.parseInt(
                                                                            this.getConfig(
                                                                                    "LightRefresh"))
                                                                    * 100)
                                                            / m.getRefreshSpeed(),
                                                    false));
                                } else if (m.getWeightClass() == Unit.MEDIUM) {
                                    hsUpdates.append(
                                            m.addRefresh(
                                                    (Integer.parseInt(
                                                                            this.getConfig(
                                                                                    "MediumRefresh"))
                                                                    * 100)
                                                            / m.getRefreshSpeed(),
                                                    false));
                                } else if (m.getWeightClass() == Unit.HEAVY) {
                                    hsUpdates.append(
                                            m.addRefresh(
                                                    (Integer.parseInt(
                                                                            this.getConfig(
                                                                                    "HeavyRefresh"))
                                                                    * 100)
                                                            / m.getRefreshSpeed(),
                                                    false));
                                } else if (m.getWeightClass() == Unit.ASSAULT) {
                                    hsUpdates.append(
                                            m.addRefresh(
                                                    (Integer.parseInt(
                                                                            this.getConfig(
                                                                                    "AssaultRefresh"))
                                                                    * 100)
                                                            / m.getRefreshSpeed(),
                                                    false));
                                }
                            }

                            if (typeId == Unit.INFANTRY) {
                                // exclusive message
                                mechsProduced.append(
                                        "A militia unit ["
                                                + newUnit.getModelName()
                                                + "] from "
                                                + m.getPlanet().getName()
                                                + " activated for front line duty!<br>");
                            } else {
                                // non infantry, so use a standard build message
                                mechsProduced.append(
                                        "Technicians assembled a "
                                                + newUnit.getModelName()
                                                + " at "
                                                + m.getName()
                                                + " on "
                                                + m.getPlanet().getName()
                                                + ".<br>");
                            }
                        }
                    }
                } // end while(PP > MaxPP)
            } // end for(all 4 weight classes)
        } // end for(all 3 types)

        // now, assemble the strings
        result +=
                mechsProduced.toString()
                        + marketAdditions.toString()
                        + industrialAccidents.toString()
                        + scrapExcuses.toString();

        LOGGER.debug("show Production Count");
        if ((getShowProductionCountNext() - 1) <= 0) {
            setShowProductionCountNext(
                    (Integer.parseInt(this.getConfig("ShowComponentGainEvery"))));

            // report how many mechs of each weight class the faction can
            // produce.
            int MekComponents = getComponentsProduced(Unit.MEK);
            int VehComponents = getComponentsProduced(Unit.VEHICLE);
            int InfComponents = getComponentsProduced(Unit.INFANTRY);
            int ProtoComponents = getComponentsProduced(Unit.PROTOMEK);
            int BAComponents = getComponentsProduced(Unit.BATTLEARMOR);
            int AeroComponents = getComponentsProduced(Unit.AERO);

            DecimalFormat myFormatter = new DecimalFormat("###.##");

            result += "<br><i><b>Your factories produced enough components to make:</b></i><br>";
            if (Boolean.parseBoolean(this.getConfig("UseMek"))) {
                result +=
                        myFormatter.format(
                                        MekComponents
                                                / (Double.parseDouble(this.getConfig("LightPP"))))
                                + " Light meks<br>";
                result +=
                        myFormatter.format(
                                        MekComponents
                                                / (Double.parseDouble(this.getConfig("MediumPP"))))
                                + " Medium meks<br>";
                result +=
                        myFormatter.format(
                                        MekComponents
                                                / (Double.parseDouble(this.getConfig("HeavyPP"))))
                                + " Heavy meks<br>";
                result +=
                        myFormatter.format(
                                        MekComponents
                                                / (Double.parseDouble(this.getConfig("AssaultPP"))))
                                + " Assault meks<br>";
            }
            if (Boolean.parseBoolean(this.getConfig("UseVehicle"))) {
                result +=
                        myFormatter.format(
                                        VehComponents
                                                / (Double.parseDouble(
                                                        this.getConfig("LightVehiclePP"))))
                                + " Light vehicles<br>";
                result +=
                        myFormatter.format(
                                        VehComponents
                                                / (Double.parseDouble(
                                                        this.getConfig("MediumVehiclePP"))))
                                + " Medium vehicles<br>";
                result +=
                        myFormatter.format(
                                        VehComponents
                                                / (Double.parseDouble(
                                                        this.getConfig("HeavyVehiclePP"))))
                                + " Heavy vehicles<br>";
                result +=
                        myFormatter.format(
                                        VehComponents
                                                / (Double.parseDouble(
                                                        this.getConfig("AssaultVehiclePP"))))
                                + " Assault vehicles<br>";
            }
            if (Boolean.parseBoolean(this.getConfig("UseInfantry"))) {

                // show only light, and no weightclass if UseOnlyLight
                if (Boolean.parseBoolean(this.getConfig("UseOnlyLightInfantry"))) {
                    result +=
                            myFormatter.format(
                                            InfComponents
                                                    / (Double.parseDouble(
                                                            this.getConfig("LightInfantryPP"))))
                                    + " Infantry<br>";
                } else {
                    result +=
                            myFormatter.format(
                                            InfComponents
                                                    / (Double.parseDouble(
                                                            this.getConfig("LightInfantryPP"))))
                                    + " Light infantry<br>";
                    result +=
                            myFormatter.format(
                                            InfComponents
                                                    / (Double.parseDouble(
                                                            this.getConfig("MediumInfantryPP"))))
                                    + " Medium infantry<br>";
                    result +=
                            myFormatter.format(
                                            InfComponents
                                                    / (Double.parseDouble(
                                                            this.getConfig("HeavyInfantryPP"))))
                                    + " Heavy infantry<br>";
                    result +=
                            myFormatter.format(
                                            InfComponents
                                                    / (Double.parseDouble(
                                                            this.getConfig("AssaultInfantryPP"))))
                                    + " Assault infantry<br>";
                }
            } // end if(UseInfantry)
            if (Boolean.parseBoolean(this.getConfig("UseProtoMek"))) {
                result +=
                        myFormatter.format(
                                        ProtoComponents
                                                / (Double.parseDouble(
                                                        this.getConfig("LightProtoMekPP"))))
                                + " Light protomechs<br>";
                result +=
                        myFormatter.format(
                                        ProtoComponents
                                                / (Double.parseDouble(
                                                        this.getConfig("MediumProtoMekPP"))))
                                + " Medium protomechs<br>";
                result +=
                        myFormatter.format(
                                        ProtoComponents
                                                / (Double.parseDouble(
                                                        this.getConfig("HeavyProtoMekPP"))))
                                + " Heavy protomechs<br>";
                result +=
                        myFormatter.format(
                                        ProtoComponents
                                                / (Double.parseDouble(
                                                        this.getConfig("AssaultProtoMekPP"))))
                                + " Assault protomechs<br>";
            }
            if (Boolean.parseBoolean(this.getConfig("UseBattleArmor"))) {
                result +=
                        myFormatter.format(
                                        BAComponents
                                                / (Double.parseDouble(
                                                        this.getConfig("LightBattleArmorPP"))))
                                + " Light battle armor<br>";
                result +=
                        myFormatter.format(
                                        BAComponents
                                                / (Double.parseDouble(
                                                        this.getConfig("MediumBattleArmorPP"))))
                                + " Medium battle armor<br>";
                result +=
                        myFormatter.format(
                                        BAComponents
                                                / (Double.parseDouble(
                                                        this.getConfig("HeavyBattleArmorPP"))))
                                + " Heavy battle armor<br>";
                result +=
                        myFormatter.format(
                                        BAComponents
                                                / (Double.parseDouble(
                                                        this.getConfig("AssaultBattleArmorPP"))))
                                + " Assault battle armor<br>";
            }

            if (Boolean.parseBoolean(this.getConfig("UseAero"))) {
                result +=
                        myFormatter.format(
                                        AeroComponents
                                                / (Double.parseDouble(
                                                        this.getConfig("LightAeroPP"))))
                                + " Light aero<br>";
                result +=
                        myFormatter.format(
                                        AeroComponents
                                                / (Double.parseDouble(
                                                        this.getConfig("MediumAeroPP"))))
                                + " Medium aero<br>";
                result +=
                        myFormatter.format(
                                        AeroComponents
                                                / (Double.parseDouble(
                                                        this.getConfig("HeavyAeroPP"))))
                                + " Heavy aero<br>";
                result +=
                        myFormatter.format(
                                        AeroComponents
                                                / (Double.parseDouble(
                                                        this.getConfig("AssaultAeroPP"))))
                                + " Assault aero<br>";
            }

            LOGGER.debug("SetComponentsProduced");
            // and return the result to CampaignMain in order to have it sent to
            // the players
            setComponentsProduced(Unit.MEK, 0);
            setComponentsProduced(Unit.VEHICLE, 0);
            setComponentsProduced(Unit.INFANTRY, 0);
            setComponentsProduced(Unit.PROTOMEK, 0);
            setComponentsProduced(Unit.BATTLEARMOR, 0);
            setComponentsProduced(Unit.AERO, 0);
        } else {
            addShowProductionCountNext(-1);
        }

        LOGGER.debug("Send House Updates: ");
        LOGGER.debug("     -> " + hsUpdates.toString());
        // send house updates, if not empty
        if (hsUpdates.length() > 0) {
            CampaignMain.cm.doSendToAllOnlinePlayers(this, "HS|" + hsUpdates.toString(), false);
        }
        LOGGER.debug("returning from tick: " + getName());
        return result;
    }

    /**
     * @author V.I. Lenin aka Travis Shade
     * @param m
     * @return TODO: Refactor to reduce redundant code. Should use typename.toLowerCase() in place
     *     of explicit paths to filenames.
     */
    private String getExcuseForUnitFailure(SUnit m) {
        if (m.getType() == Unit.MEK) {
            return scrapExcuseHelper("./data/scrapmessages/mekscrapmessages.txt", m);
        } else if (m.getType() == Unit.VEHICLE) {
            return scrapExcuseHelper("./data/scrapmessages/vehiclescrapmessages.txt", m);
        } else if (m.getType() == Unit.PROTOMEK) {
            return scrapExcuseHelper("./data/scrapmessages/protoscrapmessages.txt", m);
        } else if (m.getType() == Unit.BATTLEARMOR) {
            return scrapExcuseHelper("./data/scrapmessages/bascrapmessages.txt", m);
        } else if (m.getType() == Unit.INFANTRY) {
            return scrapExcuseHelper("./data/scrapmessages/infantryscrapmessages.txt", m);
        } else if (m.getType() == Unit.AERO) {
            return scrapExcuseHelper("./data/scrapmessages/aeroscrapmessages.txt", m);
        }

        // This should never be reached :)
        return "A " + m.getModelName() + " was kidnapped by aliens from outer space";
    }

    /**
     * Helper method for SHouse.getExcuseForUnitFailure() that factors out highly redundant input
     * stream code.
     */
    private String scrapExcuseHelper(String filepath, SUnit unit) {
        try {

            // set up input buffers
            FileInputStream fis = new FileInputStream(filepath);
            BufferedReader dis = new BufferedReader(new InputStreamReader(fis));

            // pick random message, given count from line 1
            int messages = Integer.parseInt(dis.readLine());
            int id = CampaignMain.cm.getRandomNumber(messages);

            // read lines until counter reaches randomly selected message
            String scrapMessage = "";
            while (dis.ready()) {
                scrapMessage = dis.readLine();
                if (id <= 0) {
                    break;
                }
                id--;
            }

            // close buffers
            dis.close();
            fis.close();

            // replace targetted text w/ unit & pilot specific messages and
            // return.
            String scrapMessageWithPilot =
                    scrapMessage.replaceAll("PILOT", unit.getPilot().getName());
            String scrapMessageForPlayer =
                    scrapMessageWithPilot.replaceAll("UNIT", unit.getModelName());
            return scrapMessageForPlayer;

        } catch (Exception e) { // ./data/scrapmessages/ is 21 chars. strip path
            // leader and just name file w/ problems.
            LOGGER.error(
                    "A problem occured with your "
                            + filepath.substring(21, filepath.length())
                            + " file!");
            return "A " + unit.getModelName() + " was kidnapped by aliens from outer space";
        }
    }

    // VOTE AND RANKING METHODS @urgru 9/12/04
    /*
     * Need to make a few temp vectors when a faction is first created, which
     * hold ranking orders. Think about how to do this while still being
     * efficient w/i Hibernate. Looping through the entive vote vector for each
     * player to get a typecount seems too inefficient for words --- but may be
     * fine w/ SQL.
     *
     * Talk about this with Helge before implementing anything.
     */

    // PRODUCTION POINT METHODS @urgru 02/03/03
    /**
     * A method which returns the number of PP a faction has for a specified weight class
     *
     * @param weight - the weight class to return PP for
     * @return typeId - number of PP the faction has for a given weight class
     */
    public int getPP(int weight, int typeId) {
        return getComponents().get(typeId, weight).getProductionPoints();
    }

    public List<SUnitFactory> getPossibleFactoryForProduction(
            int type, int weight, boolean ignoreRefresh) {
        List<SUnitFactory> possible = new ArrayList<>();
        Iterator<SPlanet> e = planets.values().iterator();

        while (e.hasNext()) {
            SPlanet planet = e.next();
            List<SUnitFactory> v = planet.getFactoriesOfWeightClass(weight);
            for (int i = 0; i < v.size(); i++) {
                SUnitFactory MF = v.get(i);
                if (MF.canProduce(type) && (ignoreRefresh || MF.getTicksUntilRefresh() < 1)) {
                    possible.add(MF);
                }
            }
        }
        return possible;
    }

    /**
     * Method that returns a factory originally owned by this faction which is able to produce units
     * of the requested tyoe and weight. This is used during ticks and with a-specific requests
     * (RequestCommand), so that units build randomly on ticks or pursuant to a general purchase
     * request are from the faction's own tables.
     */
    public SUnitFactory getNativeFactoryForProduction(
            int type, int weight, boolean useOnlyOriginalFactories) {

        // get all possible @ weight and type and return if none exist
        List<SUnitFactory> allPossible = getPossibleFactoryForProduction(type, weight, false);
        if (allPossible.size() == 0) {
            return null;
        }

        // sort out non-faction factories and return if none exist
        List<SUnitFactory> factionPossible = new ArrayList<>();
        for (SUnitFactory currFac : allPossible) {
            if (!useOnlyOriginalFactories || currFac.getFounder().equalsIgnoreCase(getName())) {
                factionPossible.add(currFac);
            }
        }
        if (factionPossible.size() == 0) {
            return null;
        }

        // select a random factory to return
        int rand = CampaignMain.cm.getRandomNumber(factionPossible.size());
        return (factionPossible.get(rand));
    }

    /**
     * @Salient , this is for subfaction enforcement rule when player presses buy new, it pulls from
     * correct factory
     */
    public SUnitFactory getNativeAccessableFactoryForProduction(
            int type, int weight, int subFactionLvl, String Username) {

        // CampaignMain.cm.toUser("DEBUG: subfactionLvl:" + subFactionLvl , Username, true);

        // get all possible @ weight and type and return if none exist
        List<SUnitFactory> allPossible = getPossibleFactoryForProduction(type, weight, false);
        if (allPossible.size() == 0) {
            return null;
        }

        // sort out non-faction factories and return if none exist
        List<SUnitFactory> factionPossible = new ArrayList<>();
        for (SUnitFactory currFac : allPossible) {
            // CampaignMain.cm.toUser("DEBUG: All List:" + currFac.getFounder() + " AccessLvL: " +
            // currFac.getAccessLevel() , Username, true);

            if (currFac.getFounder().equalsIgnoreCase(getName())) {
                factionPossible.add(currFac);
            }
        }
        if (factionPossible.size() == 0) {
            return null;
        }

        // sort out unaccessable factories and return if none exist
        List<SUnitFactory> accessPossible = new ArrayList<>();
        for (SUnitFactory currFac : factionPossible) {
            // CampaignMain.cm.toUser("DEBUG: House List:" + currFac.getFounder() + " AccessLvL: " +
            // currFac.getAccessLevel() , Username, true);

            if (currFac.getAccessLevel() == subFactionLvl) {
                // CampaignMain.cm.toUser("DEBUG: ADDED TO ACCESS:" + currFac.getFounder() + "
                // AccessLvL: " + currFac.getAccessLevel() , Username, true);
                accessPossible.add(currFac);
            }
        }
        if (accessPossible.size() == 0) {
            // CampaignMain.cm.toUser("DEBUG: NumFactories: " + accessPossible.size() , Username,
            // true);
            return null;
        }

        // select a random factory to return
        int rand = CampaignMain.cm.getRandomNumber(accessPossible.size());
        return (accessPossible.get(rand));
    }

    public int getMaxAllowedPP(int weight, int typeId) {
        String unitAPMax = "";
        if (CampaignMain.cm.getBooleanConfig("UseAutoProdNew")) {
            unitAPMax = "APAtMax" + Unit.getWeightClassDesc(weight) + Unit.getTypeClassDesc(typeId);
        } else {
            unitAPMax = "APAtMax" + Unit.getWeightClassDesc(weight) + "Units";
        }
        int maxUnits = Integer.parseInt(this.getConfig(unitAPMax));
        return maxUnits * getPPCost(weight, typeId);
    }

    /**
     * A method which returns the PP COST of a unit. Meks and Vehicles are segregated by
     * weightclass. Infantry are flat priced accross all weight classes.
     *
     * @param weight - the weight class to be checked
     * @return int - the PP cost
     */
    public int getPPCost(int weight, int typeId) {

        int result = Integer.MAX_VALUE;
        String classtype = Unit.getWeightClassDesc(weight) + Unit.getTypeClassDesc(typeId) + "PP";

        if (typeId == Unit.MEK) {
            result = Integer.parseInt(this.getConfig(Unit.getWeightClassDesc(weight) + "PP"));
        } else {
            result = Integer.parseInt(this.getConfig(classtype));
        }

        // modify the result by the faction price modifier
        result += getHouseUnitComponentMod(typeId, weight);

        // dont allow negative component use
        result = Math.max(1, result);

        return result;
    }

    /**
     * A method which adds a specified number of PP to Stores of the given weight class. Can send
     * house status updates, but also returns cmd to be added to longer lists of changes.
     *
     * @param weight - int, the weight class to add to
     * @param typeId - int, type of of PP to add
     * @param quantity - int, number of components to add
     */
    public String addPP(int weight, int typeId, int val, boolean sendUpdate) {
        // store starting PP
        int startingPP = getPP(weight, typeId);

        try {
            // nothing to add if they have no factories.
            if (!Boolean.parseBoolean(this.getConfig("ProduceComponentsWithNoFactory"))
                    && getPossibleFactoryForProduction(typeId, weight, true).size() < 1
                    && val > 0) {
                return "";
            }

            getComponents().get(typeId, weight).addAmount(val);
        } catch (Exception ex) {
            LOGGER.error("Unable to add PP to house: ", ex);
            LOGGER.error("weight: " + weight + " type: " + typeId + " value: " + val);
            for (int i = Unit.MEK; i < Unit.AERO; i++) {
                getComponents().get(typeId, i).setProductionPoints(0);
            }
        }

        // if PP is unchanged, no need to send a real update
        if (startingPP == getPP(weight, typeId)) {
            return "";
        }

        // else, PP changed and we need to make an update string
        String hsUpdate = getHSPPChangeString(weight, typeId);
        if (sendUpdate) {
            CampaignMain.cm.doSendToAllOnlinePlayers(this, "HS|" + hsUpdate, false);
        }

        return hsUpdate;
    }

    /**
     * A method which returns a unit from the SHouse's queue. This should only be called from SHouse
     * (during ticks) or RequestDonatedCommand (during an ask). If there is no queue'd unit of the
     * given weightclass/type, a null is returned.
     *
     * <p>WARNING!! getEntity() returns a unit, which means it cannot return a HS| command string
     * like removeUnit() does. Code that makes use of getEntity will need to set up and send one
     * using getHSUnitRemovalString().
     */
    public SUnit getEntity(int weightClass, int typeId) {
        List<SUnit> unitList =
                hangar.getAll().stream()
                        .filter(unit -> unit.getType() == typeId)
                        .filter(unit -> unit.getWeightClass() == weightClass)
                        .filter(unit -> unit.getStatus() != Unit.STATUS_FORSALE)
                        .collect(Collectors.toList());

        if (unitList.isEmpty()) {
            return null;
        }
        int index = RandomUtils.getRandomNumber(unitList.size());
        SUnit unit = unitList.get(index);
        hangar.remove(unit.getId());
        return unit;
    }

    private int getNumberOfNonSaleUnits(List<SUnit> units) {
        int count = 0;

        for (SUnit unit : units) {
            if (unit.getStatus() != Unit.STATUS_FORSALE) {
                count++;
            }
        }
        return count;
    }

    /**
     * Method required for ISeller compliance. Used to distinguish between human controlled actors
     * (SPlayer class) and factions/automated actors (this).
     */
    public boolean isHuman() {
        return false;
    }

    /**
     * Method required for compliance with ISeller. 
     */
    public SUnit getUnit(int id) {
        return hangar.get(id);
    }

    /**
     * Simple method which determines whether a given SHouse (and its players) may access the market
     * to SELL units. We check this loop continuously instead of saving a value in the SHouse
     * (inefficient) b/c the config may change between checks.
     */
    public boolean maySellOnBM() {
        StringTokenizer blockedFactions = new StringTokenizer(this.getConfig("BMNoSell"), "$");
        while (blockedFactions.hasMoreTokens()) {
            if (getName().equals(blockedFactions.nextToken())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Simple method which determines whether a given SHouse (and its players) may access the market
     * to BUY units. We check this loop continuously instead of saving a value in the SHouse
     * (inefficient) b/c the config may change between checks.
     */
    public boolean mayBuyFromBM() {
        StringTokenizer blockedFactions = new StringTokenizer(this.getConfig("BMNoBuy"), "$");
        while (blockedFactions.hasMoreTokens()) {
            if (getName().equals(blockedFactions.nextToken())) {
                return false;
            }
        }
        return true;
    }

    public SPlayer getPlayer(String s) {
        String lowerName = s.toLowerCase();
        if (getReservePlayers().containsKey(lowerName)) {
            return getReservePlayers().get(lowerName);
        }
        if (getActivePlayers().containsKey(lowerName)) {
            return getActivePlayers().get(lowerName);
        }
        if (getFightingPlayers().containsKey(lowerName)) {
            return getFightingPlayers().get(lowerName);
        }

        return null;
    }

    /**
     * A method which returns the MU cost of a specified campaign unit.
     *
     * @return int - # of MU it takes to buy a unit of the given weight class
     */
    public int getPriceForUnit(int weightclass, int typeId) {
        int result = Integer.MAX_VALUE;
        String classtype =
                Unit.getWeightClassDesc(weightclass) + Unit.getTypeClassDesc(typeId) + "Price";

        if (Boolean.parseBoolean(this.getConfig("UseCalculatedCosts"))) {
            double cost = 0;
            if (typeId == Unit.MEK) {
                cost = CampaignMain.cm.getUnitCostLists().getMinCostValue(weightclass, typeId);
                cost =
                        Math.max(
                                cost,
                                getDoubleConfig(Unit.getWeightClassDesc(weightclass) + "Price"));
            } else if (typeId == Unit.VEHICLE) {
                cost = CampaignMain.cm.getUnitCostLists().getMinCostValue(weightclass, typeId);
                cost = Math.max(cost, getDoubleConfig(classtype));
            } else {
                cost = CampaignMain.cm.getUnitCostLists().getMinCostValue(Unit.LIGHT, typeId);
                cost = Math.max(cost, getDoubleConfig(classtype));
            }
            result = (int) (cost * Double.valueOf(this.getConfig("CostModifier")));
            return result;
        }

        if (typeId == Unit.MEK) {
            result =
                    Integer.parseInt(
                            this.getConfig(Unit.getWeightClassDesc(weightclass) + "Price"));
        } else {
            result = Integer.parseInt(this.getConfig(classtype));
        }

        // modify the result by the faction price modifier
        result += getHouseUnitPriceMod(typeId, weightclass);

        // dont allow negative pricing
        if (result < 0) {
            result = 0;
        }

        return result;
    } // end getPriceForUnit()

    private int getBMPriceForUnit(int weight, int type) {
        int price = getPriceForUnit(weight, type);
        double multiplier =
                CampaignMain.cm.getDoubleConfig(
                        "BMPriceMultiplier_"
                                + Unit.getWeightClassDesc(weight)
                                + Unit.getTypeClassDesc(type));
        int finalPrice = (int) (price * multiplier);
        return finalPrice;
    }

    private void parseSupportFile(String fileName, boolean addUnits) {
        File file = new File(fileName);
        if (!file.exists()) {
            return;
        }
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedReader dis = new BufferedReader(new InputStreamReader(fis));
            while (dis.ready()) {
                if (addUnits) {
                    addUnitSupported(dis.readLine(), true);
                } else {
                    removeUnitSupported(dis.readLine(), true);
                }
            }
            dis.close();
            fis.close();
        } catch (FileNotFoundException fnfe) {
            LOGGER.info("FNFE!!!!");
        } catch (IOException ioe) {
            LOGGER.info("IOE!!!");
        }
    }

    public void addUnitSupported(String fileName, boolean sendMail) {
        if (fileName.trim().length() < 1) {
            return;
        }
        fileName = fileName.trim();
        StringBuilder toReturn = new StringBuilder();
        if (houseSupportsUnit(fileName)) {
            int num = getSupportedUnits().get(fileName);
            supportedUnits.put(fileName, num + 1);
        } else {
            supportedUnits.put(fileName, 1);
            toReturn.append(fileName);
        }
        if (toReturn.length() == 0) {
            return;
        }
        CampaignMain.cm.doSendToAllOnlinePlayers(this, "PL|USU|" + "|true|" + fileName, false);
        CampaignMain.cm.doSendHouseMail(
                this, "NOTE", "The faction is now able to support the " + toReturn.toString());
    }

    public void removeUnitSupported(String fileName, boolean sendMail) {
        if (fileName.trim().length() < 1) {
            return;
        }
        fileName = fileName.trim();
        StringBuilder toReturn = new StringBuilder();
        if (houseSupportsUnit(fileName)) {
            int num = supportedUnits.get(fileName);
            if (num == 1) {
                // Remove it from the HashMap
                supportedUnits.remove(fileName);
                toReturn.append(fileName);
            } else {
                supportedUnits.put(fileName, num - 1);
            }
        } else {
            // Error. We should never get here.
            LOGGER.info(
                    "Error in House.removeUnitProduction(): trying to remove a unit that is not"
                            + " produced.");
            LOGGER.info("  --> House: " + getName() + ", Unit: " + fileName);
        }
        if (toReturn.length() == 0) {
            return;
        }
        CampaignMain.cm.doSendToAllOnlinePlayers(this, "PL|USU|" + "|false|" + fileName, false);
        CampaignMain.cm.doSendHouseMail(
                this,
                "NOTE",
                "The faction has lost the ability to support the following units: "
                        + toReturn.toString());
    }

    public ConcurrentHashMap<String, SPlanet> getPlanets() {
        return planets;
    }

    public void addPlanet(SPlanet p) {
        if (getPlanets().get(p.getName()) == null) {
            getPlanets().put(p.getName(), p);
            setBaysProvided(getBaysProvided() + p.getBaysProvided());
            setComponentProduction(getComponentProduction() + p.getComponentProduction());

            // Add unit production here
            if (CampaignMain.cm.isUsingIncreasedTechs() && p.getFactoryCount() > 0) {
                modifyUnitSupport(p, true);
            }
        }
    }

    public void removePlanet(SPlanet p) {
        if (getPlanets().get(p.getName()) != null) {
            getPlanets().remove(p.getName());
            setBaysProvided(getBaysProvided() - p.getBaysProvided());
            setComponentProduction(getComponentProduction() - p.getComponentProduction());

            // Remove unit production here
            if (CampaignMain.cm.isUsingIncreasedTechs() && p.getFactoryCount() > 0) {
                modifyUnitSupport(p, false);
            }
        }
    }

    public void transferMoney(SPlayer p, int amount) {
        if (p != null) {
            p.addMoney(amount);
            setMoney(getMoney() - amount);
        }
    }

    public String removeUnit(SUnit unitToRemove, boolean sendUpdate) {
        hangar.remove(unitToRemove.getId());

        String hsUpdate = getHSUnitRemovalString(unitToRemove);
        if (sendUpdate) {
            CampaignMain.cm.doSendToAllOnlinePlayers(this, "HS|" + hsUpdate, false);
        }

        return hsUpdate;
    }

    /**
     * Pass-though method. <code>boolean isNew</code> is unused in SHouse; however, it is needed to
     * comply with IBuyer.
     */
    public String addUnit(SUnit unit, boolean isNew, boolean sendUpdate) {
        return this.addUnit(unit, sendUpdate);
    }

    /**
     * Method which adds a unit to the house. If sendUpdate is true, all logged in house members are
     * sent an HS|AU|. AU| cmd is returned for use in bulk commands by other methods, like
     * SHouse.tick().
     */
    public String addUnit(SUnit unit, boolean sendUpdate) {
        if (Boolean.parseBoolean(this.getConfig("AllowPersonalPilotQueues"))
                && unit.isSinglePilotUnit()
                && !unit.hasVacantPilot()) {
            getPilotQueues().addPilot(unit.getType(), (SPilot) unit.getPilot());
            unit.setPilot(new SPilot(null, "Vacant", 99, 99));
        }

        if (Boolean.parseBoolean(this.getConfig("UseOnlyOneVehicleSize"))
                && unit.getType() == Unit.VEHICLE) {
            unit.setWeightClass(Unit.LIGHT);
        }

        if (hangar.get(unit.getId()) != null) {
            return "";
        }
        
        hangar.add(unit);
        String hsUpdate = this.getHSUnitAdditionString(unit);
        if (sendUpdate
                && !(this.isNewbieHouse()
                        && Boolean.parseBoolean(CampaignMain.cm.getConfig("HiddenBMUnits")))) {
            CampaignMain.cm.doSendToAllOnlinePlayers(this, "HS|" + hsUpdate, false);
        }

        return hsUpdate;
    }

    /*
     * Log a player into the faction and put him on reserve (normal) status.
     * This should be called only from CM.doLoginPlayer(). If the player is
     * signing on, the SignOn command will handle the reconnectionCheck() and
     * adjust status to fighting if necessary.
     */
    protected String doLogin(SPlayer p) {
        // lowercase the name
        String realName = p.getName();
        String lowerName = realName.toLowerCase();

        /*
         * Player has logged into their house we no longer have to worry about
         * them.
         */
        CampaignMain.cm.releaseLostSoul(p.getName());

        // test to see if the player is already in the hashes
        if (isLoggedIntoFaction(lowerName)) {
            CampaignMain.cm.toUser("CS|" + SPlayer.STATUS_RESERVE, realName, false);
            return null;
        }

        if (p.getPassword() == null) {
            if (isLeader(p.getName())) {
                removeLeader(p.getName());
            }
        } else {
            if (isLeader(p.getName())
                    && p.getPassword().getAccess()
                            < CampaignMain.cm.getIntegerConfig("factionLeaderLevel")) {
                CampaignMain.cm.updatePlayersAccessLevel(
                        p.getName(), CampaignMain.cm.getIntegerConfig("factionLeaderLevel"));
            } else if (p.getPassword().getAccess()
                            == CampaignMain.cm.getIntegerConfig("factionLeaderLevel")
                    && !isLeader(p.getName())) {
                CampaignMain.cm.updatePlayersAccessLevel(p.getName(), 2);
            }
        }

        // update the player's myHouse
        CampaignMain.cm.toUser("PL|SH|" + getName(), realName, false);

        CampaignMain.cm.toUser("PL|SSN|" + p.getSubFactionName(), realName, false);

        Date d = new Date(System.currentTimeMillis());
        LOGGER.info(d + ":" + "User Logged into House: " + realName);

        // Send the current servers MegaMek game Options
        CampaignMain.cm.toUser(
                "GO|" + CampaignMain.cm.getMegaMekOptionsToString(), realName, false);

        /*
         * Remove from all status hashes and place in reserve, in case the
         * players was somewho disconnected and not recognized while signing
         * back on. The code will later check for a running game and escalate to
         * fighting state if needed.
         */
        reservePlayers.remove(lowerName);
        activePlayers.remove(lowerName);
        fightingPlayers.remove(lowerName);

        getReservePlayers().put(lowerName, p);
        p.setLastSentStatus("");

        CampaignMain.cm.toUser("CS|" + SPlayer.STATUS_RESERVE, realName, false);

        // send player his pilot lists and exclude lists
        CampaignMain.cm.toUser(
                "PL|PPQ|" + p.getPersonalPilotQueue().toString(true), realName, false);
        CampaignMain.cm.toUser(
                "PL|AEU|" + p.getExclusionList().adminExcludeToString("$"), realName, false);
        CampaignMain.cm.toUser(
                "PL|PEU|" + p.getExclusionList().playerExcludeToString("$"), realName, false);

        /*
         * Old code used to look for a running task here, and send auto armies
         * and game options to players who had running games. Players who had
         * games were put in the fighting members hash, players who did not were
         * placed in the active hash.
         *
         * Now we use doReconnectionCheck() in the Server's SignOn cmd after the
         * login is processed. This sends any autoarmies/options and stops
         * discon threads. It also removes fighting players from active and
         * places them in fighting, as appropriate.
         *
         * In sum, we can put all players in the Reserve hash at this point, and
         * they will be properly moved afterwards when setBusyNoOpList() is run.
         */
        MWServ.getInstance().getIThread().removeImmunity(p); // logging in player
        // should NEVER be
        // immune

        // send player the MOTD
        Command c = CampaignMain.cm.getServerCommands().get("MOTD");
        c.process(new StringTokenizer("", ""), realName);

        // send the current BM and HS to the player
        CampaignMain.cm.getMarket().sendCompleteMarketStatus(p);
        CampaignMain.cm.toUser("HS|CA|0", realName, false); // clear old data
        CampaignMain.cm.toUser(getCompleteStatus(), realName, false);
        CampaignMain.cm.getPartsMarket().updatePartsBlackMarketPlayer(p);

        /*
         * Now that the player is loaded and has a fresh timestamp look for a
         * corresponding SmallPlayer.
         *
         * If the smallplayer exists, nothing needs to be done. The
         * SmallPlayer's values will all (with the exception of faction, which
         * is hardset during generation) be over written with the latest
         * SPlayerData information when the various set() calls are made during
         * SPlayer.fromString() during player load.
         *
         * Otherwise, make a new SmallPlayer with the SPlayer's info and insert
         * it into the Map. @urgru
         */
        SmallPlayer smallp = smallPlayers.get(lowerName);
        if (smallp == null) { // make a new one
            smallp =
                    new SmallPlayer(
                            p.getExperience(),
                            p.getLastOnline(),
                            p.getRating(),
                            realName,
                            p.getFluffText(),
                            this);
            smallPlayers.put(lowerName, smallp);
        }

        // Send supported units updates
        if (CampaignMain.cm.isUsingIncreasedTechs()) {
            CampaignMain.cm.toUser("PL|CSU|0", realName, false);
            StringBuilder toSend = new StringBuilder();
            toSend.append("PL|USU|");
            int num = 0;
            for (String unitName : getSupportedUnits().keySet()) {
                num = getSupportedUnits().get(unitName);
                for (; num > 0; num--) {
                    toSend.append("true|");
                    toSend.append(unitName + "|");
                }
            }
            CampaignMain.cm.toUser(toSend.toString(), realName, false);
        }

        if (isLeader(p.getName()) && CampaignMain.cm.getBooleanConfig("UsePartsRepair")) {
            Command cmd = CampaignMain.cm.getServerCommands().get("GETCOMPONENTCONVERSION");
            cmd.process(new StringTokenizer("", "#"), p.getName());
        }

        // send the player the latest data from the factionbays
        p.setLastOnline(System.currentTimeMillis()); // must be done after
        // smallplayer creation

        // Send the target system bans
        StringBuilder tsBans = new StringBuilder();
        tsBans.append("SBT|");

        for (int ban : CampaignMain.cm.getData().getBannedTargetingSystems()) {
            tsBans.append(ban);
            tsBans.append("|");
        }
        tsBans.append("|");
        CampaignMain.cm.toUser(tsBans.toString(), realName, false);

        // Send default player flags if it's an admin or mod
        if (MWServ.getInstance().isModerator(p.getName())
                || MWServ.getInstance().isAdmin(p.getName())) {
            if (!CampaignMain.cm.getDefaultPlayerFlags().isEmpty()) {
                CampaignMain.cm.toUser(
                        "PF|SDF|" + CampaignMain.cm.getDefaultPlayerFlags().export(),
                        p.getName(),
                        false);
            }
        }

        CampaignMain.cm.toUser("PF|S", p.getName(), false);
        return ("<b>[*] Logged into " + getColoredNameAsLink() + ".</b>");
    }

    /**
     * Remove a player from the house lists. Should be called only from CampaignMain's .doLogout(),
     * which sends needed status updates to all players and sets up save information.
     *
     * <p>We don't need to worry about disconnections or oddly timed logouts (eg - midgame). The
     * only time that kind of abrupt removal should be allowed is when a client closes of loses its
     * connection, which is handled by ServerWrapper.signOff().
     */
    protected void doLogout(SPlayer p) {
        // if the is already logged in, return
        String realName = p.getName();
        String lowerName = realName.toLowerCase();
        if (!isLoggedIntoFaction(lowerName)) {
            return;
        }

        // note: this removes the player from all attacker/defender lists.
        // if (p.getDutyStatus() == SPlayer.STATUS_ACTIVE)
        p.setActive(false);

        // remove from all status hashes
        reservePlayers.remove(lowerName);
        activePlayers.remove(lowerName);
        fightingPlayers.remove(lowerName);

        CampaignMain.cm.forceSavePlayer(p);
        // add info to logs
        Date d = new Date(System.currentTimeMillis());
        LOGGER.info(d + ":" + "User Logged out: " + realName);
        CampaignMain.cm.toUser("CS|" + SPlayer.STATUS_LOGGEDOUT, realName, false);
    }

    /**
     * Completely remove a player from the house. Very simple. Donate the players units, clear out
     * his votes, then nuke hims pfile.
     */
    public void removePlayer(SPlayer p, boolean donateMechs) {
        // check to make sure he's not null
        if (p == null) {
            return;
        }

        // log the player out of the house
        doLogout(p);

        removeLeader(p.getName());
        // Never send the newbie mechs back to the house bays.
        if (isNewbieHouse()) {
            donateMechs = false;
        }

        // if we're donating all units, do so
        if (donateMechs) {
            StringBuilder hsUpdates = new StringBuilder();
            boolean allowDamagedUnits =
                    CampaignMain.cm.isUsingAdvanceRepair()
                            && Boolean.parseBoolean(this.getConfig("AllowDonatingOfDamagedUnits"));
            for (SUnit currUnit : p.getUnits()) {

                boolean damaged =
                        (!UnitUtils.canStartUp(currUnit.getEntity())
                                || UnitUtils.hasArmorDamage(currUnit.getEntity())
                                || UnitUtils.hasCriticalDamage(currUnit.getEntity()));

                if ((damaged && allowDamagedUnits) || !damaged) {
                    hsUpdates.append(addUnit(currUnit, false));
                }
            }

            // if units were donated, send updates to factionmates
            if (hsUpdates.length() > 0) {
                CampaignMain.cm.doSendToAllOnlinePlayers(this, "HS|" + hsUpdates.toString(), false);
            }
        }

        /*
         * The player is moving to a new faction (or quitting). Rather than
         * letting all of his votes remain and count, strip them.
         */
        CampaignMain.cm.getVoteManager().removeAllVotesByPlayer(p);
        CampaignMain.cm.getVoteManager().removeAllVotesForPlayer(p);

        // remove small player. don't delete the pfile.
        p.getMyHouse().getSmallPlayers().remove(p.getName().toLowerCase());
    } // end removePlayer()

    /*
     * Used by RangeCommand and CheckDistCommand.
     */
    public int getDistanceTo(SPlanet p, SPlayer player) {
        // Is the faction on the planet?
        if (p.getInfluence().getInfluence(getId()) > 10) {
            return 0;
        }

        double distSq = Integer.MAX_VALUE;
        double tdist;

        Iterator<Planet> e = CampaignMain.cm.getData().getAllPlanets().iterator();
        while (e.hasNext()) {
            SPlanet pl = (SPlanet) e.next();
            // Only consider planet if we control at least 25%
            if (pl.getInfluence().getInfluence(getId()) >= 25) {
                tdist = pl.getPosition().distanceSq(p.getPosition());
                if (tdist < distSq) {
                    distSq = tdist;
                }
            }
        }
        return (int) distSq;
    }

    /**
     * Generates serialized version of SHouse to send to clients for HouseStatus tab. Complete
     * status is sent on login. Afterwards, changes are transmitted incremementally.
     */
    public String getCompleteStatus() {
        String cmdDelim = "|"; // used to separate HS| subcommands
        String internalDelim = "$"; // used to separate elements within
        // subcommands

        // first item, name
        StringBuilder result = new StringBuilder();
        result.append("HS|FN|" + getName() + cmdDelim);

        /*
         * Second, append misc. component information. Standard loop through all
         * weight classes and types.
         *
         * Structure: CC|weight$type$components$producableunits|
         */
        for (int typeId = 0; typeId < Unit.TOTALTYPES; typeId++) {
            for (int weight = Unit.LIGHT; weight <= Unit.ASSAULT; weight++) {
                result.append(getHSPPChangeString(weight, typeId));
            }
        }

        /*
         * Third block - factories. Use AF| commands to add factories to each
         * type and weight class. Similar to component loop above, but factory
         * entries contain more information.
         *
         * Loop through all worlds, check control, and send owned factories.
         *
         * Structure: AF|weight$metatype$founder$planet$name$refreshtime$ID|
         */
        for (SPlanet currPlanet : getPlanets().values()) {

            // skip unowned & contested worlds
            if (!equals(currPlanet.getOwner())) {
                continue;
            }

            for (int i = 0; i < currPlanet.getUnitFactories().size(); i++) {
                SUnitFactory currFactory = (SUnitFactory) currPlanet.getUnitFactories().get(i);
                result.append("AF" + cmdDelim); // cmd header

                result.append(currFactory.getWeightClass() + internalDelim);
                result.append(currFactory.getType() + internalDelim);

                result.append(currFactory.getFounder() + internalDelim);
                result.append(currFactory.getPlanet().getName() + internalDelim);
                result.append(currFactory.getName() + internalDelim);
                result.append(currFactory.getTicksUntilRefresh() + internalDelim);
                result.append(currFactory.getAccessLevel() + internalDelim);
                result.append(currFactory.getId() + internalDelim);
                result.append(cmdDelim);
            }
        }

        /*
         * Fourth, and final, block - units in faction bays.
         */

        if (!(this.isNewbieHouse()
                && Boolean.parseBoolean(CampaignMain.cm.getConfig("HiddenBMUnits")))) {
            for (SUnit unit : hangar.getAll()) {
                if (unit.getStatus() == Unit.STATUS_FORSALE) {
                    continue;
                }
                result.append(getHSUnitAdditionString(unit));
            }
        }
        return result.toString();
    }

    /**
     * Construct a string to send to clients if unit is added. Format is:
     * AU|weight$type$chassis$model$damage|
     */
    public String getHSUnitAdditionString(SUnit u) {
        SerializedMessage result = new SerializedMessage("$");

        // header info
        result.append("AU|" + u.getWeightClass());
        result.append(u.getType());

        // unit information (note: no pilot info included)
        Entity currE = u.getEntity();
        result.append(u.getUnitFilename());
        result.append(u.getId()); // ID used to remove units. Never shown to
        // players in GUI.

        if (!u.hasVacantPilot()) {
            result.append(u.getPilot().getGunnery());
            result.append(u.getPilot().getPiloting());
        } else {
            result.append(getBasePilotStats().getGunnery(u.getType()));
            result.append(getBasePilotStats().getPiloting(u.getType()));
        }
        // if using AR, send damage information
        if (CampaignMain.cm.isUsingAdvanceRepair()) {
            result.append(UnitUtils.unitBattleDamage(currE, true));
        }

        // finalize and return
        return result.toString() + "|";
    }

    /**
     * Construct a string to send to clients if PP changes. Format is
     * CC|weight$type$components$producableunits|
     */
    public String getHSPPChangeString(int weight, int typeId) {

        StringBuilder result = new StringBuilder();

        int costPerUnit = Math.max(1, getPPCost(weight, typeId));
        int currentPP = getPP(weight, typeId);

        result.append("CC|");
        result.append(weight + "$" + typeId + "$");
        result.append(currentPP + "$" + (currentPP / costPerUnit));

        result.append("|");
        return result.toString();
    }

    /**
     * Construct a string to send to clients if unit is removed from a house. Called by SHouse
     * internally, but also outside of SHouse as as a follow-up to SHouse.getEntity().
     */
    public String getHSUnitRemovalString(SUnit u) {

        StringBuilder result = new StringBuilder();

        // header info
        result.append("RU|");
        result.append(u.getWeightClass() + "$" + u.getType() + "$");
        result.append(u.getId());

        // fianlize and return
        result.append("|");
        return result.toString();
    }

    // Getter and Setter
    public int getMoney() {
        return money;
    }

    public String getColoredName() {
        return "<font color=\"" + getHouseColor() + "\">" + getName() + "</font>";
    }

    public String getColoredNameAsLink() {
        return "<font color=\"" + getHouseColor() + "\">" + getNameAsLink() + "</font>";
    }

    public String getColoredAbbreviation(boolean includeBrackets) {
        String toReturn = "<font color=\"" + getHouseColor() + "\">";
        if (includeBrackets) {
            toReturn += "[";
        }
        toReturn += getAbbreviation();
        if (includeBrackets) {
            toReturn += "]";
        }
        return toReturn += "</font>";
    }

    public void setMoney(int newMoney) {
        money = newMoney;
    }

    public int getComponentsProduced(int unitType) {
        if (!unitComponents.containsKey(unitType)) {
            return 0;
        }
        // else
        int component = unitComponents.get(unitType);
        return component;
    }

    public int getShowProductionCountNext() {
        return showProductionCountNext;
    }

    /**
     * @return the small player hashtable
     */
    public Map<String, SmallPlayer> getSmallPlayers() {
        synchronized (smallPlayers) {
            return smallPlayers;
        }
    }

    // Comparable
    public int compareTo(Object o) {
        SHouse h = (SHouse) o;
        if (getMoney() > h.getMoney()) {
            return 1;
        } else if (getMoney() < h.getMoney()) {
            return -1;
        }
        return getName().compareTo(h.getName());
    }

    public void addMoney(int amount) {
        setMoney(getMoney() + amount);
    }

    public void addComponentsProduced(int unitType, int amount) {
        setComponentsProduced(unitType, getComponentsProduced(unitType) + amount);
    }

    public void addShowProductionCountNext(int amount) {
        setShowProductionCountNext(getShowProductionCountNext() + amount);
    }

    /**
     * Returns all online players. Should be used sparingly.
     *
     * <p>TODO: Remove references to this method, where possible.
     */
    public Map<String, SPlayer> getAllOnlinePlayers() {
        Map<String, SPlayer> allPlayers = new HashMap<String, SPlayer>();
        allPlayers.putAll(getReservePlayers());
        allPlayers.putAll(getActivePlayers());
        allPlayers.putAll(getFightingPlayers());
        return allPlayers;
    }

    /**
     * @param baysProvided - The baysProvided to set.
     */
    public void setBaysProvided(int baysProvided) {
        this.baysProvided = baysProvided;
    }

    /**
     * @param componentProduction - The componentProduction to set.
     */
    public void setComponentProduction(int componentProduction) {
        this.componentProduction = componentProduction;
    }

    public void setComponentsProduced(int unitType, int components) {
        unitComponents.put(unitType, components);
    }

    public void setShowProductionCountNext(int i) {
        showProductionCountNext = i;
    }

    /**
     * @return Returns the initialHouseRanking.
     */
    public int getInitialHouseRanking() {
        return initialHouseRanking;
    }

    /**
     * @param initialHouseRanking - the initialHouseRanking to set.
     */
    public void setInitialHouseRanking(int initialHouseRanking) {
        this.initialHouseRanking = initialHouseRanking;
    }

    /**
     * @return Returns the MOTD.
     */
    public String getMotd() {
        return motd;
    }

    /**
     * @param motd - the MOTD to set.
     */
    public void setMotd(String motd) {
        this.motd = motd;
    }

    public void sendMessageToHouseLeaders(String msg) {
        for (String name : leaders) {
            CampaignMain.cm.toUser(msg, name);
        }
    }

    public void addLeader(String leader) {
        leaders.add(leader.toLowerCase());
    }

    public void removeLeader(String leader) {
        leaders.remove(leader.toLowerCase());
    }

    public boolean isLeader(String leader) {
        return leaders.contains(leader.toLowerCase());
    }

    public SubFaction getZeroLevelSubFaction() {
        if (getSubfactions().size() < 1) {
            return null;
        }

        for (SubFaction subfaction : getSubfactions()) {
            if (subfaction.getAccessLevel() == 0) {
                return subfaction;
            }
        }
        return null;
    }

    public void addCommonUnitSupport() {
        parseSupportFile("./campaign/factions/support/common_meks.txt", true);
        parseSupportFile("./campaign/factions/support/common_vehicles.txt", true);
        parseSupportFile("./campaign/factions/support/common_infantry.txt", true);
        parseSupportFile("./campaign/factions/support/common_battlearmor.txt", true);
        parseSupportFile("./campaign/factions/support/common_protomeks.txt", true);
        parseSupportFile("./campaign/factions/support/common_aero.txt", true);
    }

    private void modifyUnitSupport(SPlanet p, boolean addProduction) {
        if (p.getFactoryCount() > 0) {
            for (int weightClass = Unit.LIGHT; weightClass <= Unit.ASSAULT; weightClass++) {
                for (SUnitFactory uf : p.getFactoriesOfWeightClass(weightClass)) {
                    String typeString = uf.getTypeString();
                    String dirName =
                            "./campaign/factions/support/"
                                    + uf.getFounder()
                                    + "_"
                                    + uf.getSize()
                                    + "_";
                    dirName = dirName.toLowerCase();
                    if (typeString.contains("M")) {
                        parseSupportFile(dirName + "meks.txt", addProduction);
                    }
                    if (typeString.contains("V")) {
                        parseSupportFile(dirName + "vehicles.txt", addProduction);
                    }
                    if (typeString.contains("I")) {
                        parseSupportFile(dirName + "infantry.txt", addProduction);
                    }
                    if (typeString.contains("P")) {
                        parseSupportFile(dirName + "protomeks.txt", addProduction);
                    }
                    if (typeString.contains("B")) {
                        parseSupportFile(dirName + "battlearmor.txt", addProduction);
                    }
                    if (typeString.contains("A")) {
                        parseSupportFile(dirName + "aero.txt", addProduction);
                    }
                }
            }
        }
    }

    public String getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(String announcement) {
        this.announcement = announcement;
    }

    public void createNoneHouse() {
        setName("None");
        setConquerable(false);
        setHouseDefectionTo(false);
        setHouseDefectionFrom(false);
        setAbbreviation("None");
        setHouseColor(CampaignMain.cm.getConfig("DisputedPlanetColor"));
        setHousePlayerColors(CampaignMain.cm.getConfig("DisputedPlanetColor"));

        LOGGER.debug(getName());
        
        setMoney(0);
        updated();
    }

    public void addTechResearchPoint(int points) {
        setTechResearchPoints(points + getTechResearchPoints());
    }

    public void setTechResearchPoints(int points) {
        techResearchPoints = points;
    }

    public int getTechResearchPoints() {
        return techResearchPoints;
    }

    public int getTechResearchLevel() {
        return this.getTechResearchLevel(getTechLevel());
    }

    public int getTechResearchLevel(int tech) {
        int techLevel = 1;
        switch (tech) {
            case TechConstants.T_INTRO_BOXSET:
            case TechConstants.T_TW_ALL:
            case TechConstants.T_IS_TW_NON_BOX:
                techLevel = 1;
                break;
            case TechConstants.T_IS_ADVANCED:
                techLevel = 2;
                break;
            case TechConstants.T_IS_EXPERIMENTAL:
                techLevel = 3;
                break;
            case TechConstants.T_IS_UNOFFICIAL:
                techLevel = 4;
                break;
            case TechConstants.T_CLAN_TW:
                techLevel = 5;
                break;
            case TechConstants.T_CLAN_ADVANCED:
                techLevel = 6;
                break;
            case TechConstants.T_CLAN_EXPERIMENTAL:
                techLevel = 7;
                break;
            case TechConstants.T_CLAN_UNOFFICIAL:
                techLevel = 8;
                break;
            case TechConstants.T_ALL:
            case TechConstants.T_ALLOWED_ALL:
                techLevel = 9;
                break;
            default:
                techLevel = 1;
        }

        return techLevel;
    }

    public void updateHouseTechLevel() {
        switch (getTechResearchLevel()) {
            case 1:
                setTechLevel(TechConstants.T_IS_TW_ALL);
                break;
            case 2:
                setTechLevel(TechConstants.T_IS_ADVANCED);
                break;
            case 3:
                setTechLevel(TechConstants.T_IS_EXPERIMENTAL);
                break;
            case 4:
                setTechLevel(TechConstants.T_IS_UNOFFICIAL);
                break;
            case 5:
                setTechLevel(TechConstants.T_CLAN_TW);
                break;
            case 6:
                setTechLevel(TechConstants.T_CLAN_ADVANCED);
                break;
            case 7:
                setTechLevel(TechConstants.T_CLAN_EXPERIMENTAL);
                break;
            case 8:
                setTechLevel(TechConstants.T_CLAN_UNOFFICIAL);
                break;
            case 9:
                setTechLevel(TechConstants.T_ALL);
                break;
            default:
                setTechLevel(TechConstants.T_IS_TW_ALL);
                break;
        }
        techResearchPoints = 0;
    }

    public UnitComponents getUnitComponents() {
        return unitParts;
    }

    public void updatePartsCache(String part, int amount) {
        if (amount < 0) {
            getUnitComponents().remove(part, amount);
        } else {
            getUnitComponents().add(part, amount);
        }
    }

    public int getPartsAmount(String part) {
        int amount = 0;
        amount += getUnitComponents().getPartsCritCount(part);
        return amount;
    }

    public void addComponentConverter(ComponentToCritsConverter converter) {
        componentConverter.put(converter.getCritName(), converter);
    }

    public Map<String, ComponentToCritsConverter> getComponentConverter() {
        return componentConverter;
    }

    @Deprecated(since = "9.0.0", forRemoval = false)
    public String getConfig(String key) {
        return getHouseOptions().getConfig(key);
    }

    @Deprecated(since = "9.0.0", forRemoval = false)
    public boolean getBooleanConfig(String key) {
        return getHouseOptions().getBooleanConfig(key);
    }

    @Deprecated(since = "9.0.0", forRemoval = false)
    public int getIntegerConfig(String key) {
        return getHouseOptions().getIntegerConfig(key);
    }

    @Deprecated(since = "9.0.0", forRemoval = false)
    public double getDoubleConfig(String key) {
        return getHouseOptions().getDoubleConfig(key);
    }

    @Deprecated(since = "9.0.0", forRemoval = false)
    public float getFloatConfig(String key) {
        return getHouseOptions().getFloatConfig(key);
    }

    @Deprecated(since = "9.0.0", forRemoval = false)
    public long getLongConfig(String key) {
        return getHouseOptions().getLongConfig(key);
    }

    @Deprecated(since = "9.0.0", forRemoval = false)
    private void produceCrits() {
        CampaignOptions campaignOptions = CampaignData.cd.getCampaignOptions();

        if (!campaignOptions.getBooleanConfig("UsePartsRepair")) {
            return;
        }

        int year = campaignOptions.getIntegerConfig("CampaignYear");
        boolean cacheUpdate = false;

        double baseCost = campaignOptions.getDoubleConfig("BaseComponentToMoneyRatio");

        if (getComponentConverter().containsKey("All")) {
            ComponentToCritsConverter converter = getComponentConverter().get("All");
            int minCrits = converter.getMinCritLevel();
            baseCost *=
                    campaignOptions.getDoubleConfig(
                            "ComponentToPartsModifier"
                                    + SUnit.getTypeClassDesc(converter.getComponentUsedType()));
            baseCost *=
                    campaignOptions.getDoubleConfig(
                            "ComponentToPartsModifier"
                                    + SUnit.getWeightClassDesc(converter.getComponentUsedWeight()));

            for (BMEquipment eq : CampaignMain.cm.getPartsMarket().getEquipmentList().values()) {
                // do not produce something that the is allowed in the BM
                if (eq.getCost() <= 0) {
                    continue;
                }

                // do bother producing what you cannot use.
                if (!campaignOptions.getBooleanConfig("AllowCrossOverTech")) {
                    eq.getTech(year);
                    if (eq.getTechLevel() != TechConstants.T_ALL
                            && eq.getTechLevel() > getTechLevel()) {
                        continue;
                    }
                }

                if (getPartsAmount(eq.getEquipmentInternalName()) < minCrits) {
                    int critsToAdd = minCrits - getPartsAmount(eq.getEquipmentInternalName());

                    double costInComponents = eq.getCost() / baseCost;
                    double components = critsToAdd * costInComponents;

                    components = Math.ceil(components);
                    components = Math.max(1, components);
                    if (getComponents().get(
                                converter.getComponentUsedType(),
                                converter.getComponentUsedWeight()).getProductionPoints()
                            < components) {
                        continue;
                    }

                    addPP(
                            converter.getComponentUsedWeight(),
                            converter.getComponentUsedType(),
                            (int) -components,
                            false);
                    updatePartsCache(eq.getEquipmentInternalName(), critsToAdd);
                    cacheUpdate = true;
                }
            }
        } else {

            for (ComponentToCritsConverter converter : getComponentConverter().values()) {

                int minCrits = converter.getMinCritLevel();
                baseCost = campaignOptions.getDoubleConfig("BaseComponentToMoneyRatio");
                baseCost *=
                        campaignOptions.getDoubleConfig(
                                "ComponentToPartsModifier"
                                        + SUnit.getTypeClassDesc(converter.getComponentUsedType()));
                baseCost *=
                        campaignOptions.getDoubleConfig(
                                "ComponentToPartsModifier"
                                        + SUnit.getWeightClassDesc(
                                                converter.getComponentUsedWeight()));

                BMEquipment eq =
                        CampaignMain.cm
                                .getPartsMarket()
                                .getEquipmentList()
                                .get(converter.getCritName());

                if (eq == null) {
                    continue;
                }

                // do not produce something that the is allowed in the BM
                if (eq.getCost() <= 0) {
                    continue;
                }
                // do bother producing what you cannot use.
                if (!campaignOptions.getBooleanConfig("AllowCrossOverTech")) {
                    eq.getTech(year);
                    if (eq.getTechLevel() != TechConstants.T_ALL
                            && eq.getTechLevel() > getTechLevel()) {
                        continue;
                    }
                }

                if (getPartsAmount(eq.getEquipmentInternalName()) < minCrits) {
                    int critsToAdd = minCrits - getPartsAmount(eq.getEquipmentInternalName());

                    double costInComponents = eq.getCost() / baseCost;
                    double components = critsToAdd * costInComponents;

                    components = Math.ceil(components);

                    components = Math.max(1, components);

                    if (getComponents().get(
                                converter.getComponentUsedType(),
                                converter.getComponentUsedWeight()).getProductionPoints()
                            < components) {
                        continue;
                    }

                    addPP(
                            converter.getComponentUsedWeight(),
                            converter.getComponentUsedType(),
                            (int) -components,
                            false);
                    updatePartsCache(eq.getEquipmentInternalName(), critsToAdd);
                    cacheUpdate = true;
                }
            }
        }
        if (cacheUpdate) {
            CampaignMain.cm.doSendHouseMail(this, "NOTE", "The house crits have been updated");
            CampaignMain.cm.doSendToAllOnlinePlayers(this, getCompleteStatus(), false);
        }
    }

    public void calcActivityPP(Double armyWeight) {
        double cComp = getComponentProduction();
        int componentsToAdd = (int) (armyWeight * cComp);
        int refreshToAdd = (int) Math.ceil(armyWeight);

        if (getIntegerConfig("FactoryRefreshPoints") > -1) {
            // Allow Servers to refresh factories without having active players.
            refreshToAdd = getIntegerConfig("FactoryRefreshPoints");
        }

        StringBuilder hsUpdates = new StringBuilder();
        // Get income, and refresh factories
        for (SPlanet planet : getPlanets().values()) { // loop through all planets which the faction
            // has territory on
            if (equals(planet.getOwner())) {
                LOGGER.debug(
                        "Calling tick on "
                                + planet.getName()
                                + " to add "
                                + refreshToAdd
                                + " refresh");
                hsUpdates.append(planet.tick(refreshToAdd)); // call the planetary
                // tick
            }
        }

        // then add to the faction PP pools
        boolean useMekPP = getHouseOptions().getBooleanConfig("UseMek");
        boolean useVehiclePP = getHouseOptions().getBooleanConfig("UseVehicle");
        boolean useInfantryPP = getHouseOptions().getBooleanConfig("UseInfantry");
        boolean useProtoMekPP = getHouseOptions().getBooleanConfig("UseProtoMek");
        boolean useBattleArmorPP = getHouseOptions().getBooleanConfig("UseBattleArmor");
        boolean useAeroPP = getHouseOptions().getBooleanConfig("UseAero");

        for (int i = 0; i < 4; i++) { // loop through each weight class,
            // adding PP
            if (useMekPP) {
                hsUpdates.append(addPP(i, Unit.MEK, componentsToAdd, true));
                addComponentsProduced(Unit.MEK, componentsToAdd);
            }

            if (useVehiclePP) {
                hsUpdates.append(addPP(i, Unit.VEHICLE, componentsToAdd, true));
                addComponentsProduced(Unit.VEHICLE, componentsToAdd);
            }

            if (useInfantryPP) {
                if (!Boolean.parseBoolean(this.getConfig("UseOnlyLightInfantry"))
                        || i == Unit.LIGHT) {
                    hsUpdates.append(addPP(i, Unit.INFANTRY, componentsToAdd, true));
                }
                addComponentsProduced(Unit.INFANTRY, componentsToAdd);
            }

            if (useProtoMekPP) {
                hsUpdates.append(addPP(i, Unit.PROTOMEK, componentsToAdd, true));
                addComponentsProduced(Unit.PROTOMEK, componentsToAdd);
            }

            if (useBattleArmorPP) {
                hsUpdates.append(addPP(i, Unit.BATTLEARMOR, componentsToAdd, true));
                addComponentsProduced(Unit.BATTLEARMOR, componentsToAdd);
            }

            if (useAeroPP) {
                hsUpdates.append(addPP(i, Unit.AERO, componentsToAdd, false));
                addComponentsProduced(Unit.AERO, componentsToAdd);
            }
        }
        // send house updates, if not empty
        if (hsUpdates.length() > 0) {
            CampaignMain.cm.doSendToAllOnlinePlayers(this, "HS|" + hsUpdates.toString(), false);
        }
    }
} // end SHouse.java
