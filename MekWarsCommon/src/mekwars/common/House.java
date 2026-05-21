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

import megamek.common.TechConstants;

import mekwars.common.entities.MWEntity;
import mekwars.common.persistence.EntityStore;
import mekwars.common.util.BinReader;
import mekwars.common.util.BinWriter;
import mekwars.common.util.HTMLConverter;
import mekwars.common.util.TokenReader;
import mekwars.common.universe.FactionTag;
import mekwars.common.campaign.BasePilotStats;
import megamek.common.TechConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Entity;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Id;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Helge Richter
 * 
 */
@Entity
public class House implements MWEntity {
    public static final int RED_VALUE = 0;
    public static final int GREEN_VALUE = 1;
    public static final int BLUE_VALUE = 2;

    private String name = "none";
    private String logo = "";
    private String factionFluFile = "Common";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ElementCollection
    @CollectionTable(name = "house_price_mods", joinColumns = @JoinColumn(name = "house_id"))
    @SQLRestriction("mod_category = 'PRICE'")
    private int factionUnitPriceMod[][] = new int[Unit.MAXBUILD][4]; // [Type][Weight]

    @ElementCollection
    @CollectionTable(name = "house_price_mods", joinColumns = @JoinColumn(name = "house_id"))
    @SQLRestriction("mod_category = 'INFLUENCE'")
    private int factionUnitFluMod[][] = new int[Unit.MAXBUILD][4]; // [Type][Weight]

    @ElementCollection
    @CollectionTable(name = "house_price_mods", joinColumns = @JoinColumn(name = "house_id"))
    @SQLRestriction("mod_category = 'COMPONENT'")
    private int factionUnitComponentMod[][] = new int[Unit.MAXBUILD][4]; // [Type][Weight]

    private String factionColor = "#000000";
    private String abbreviation = "";
    private String factionPlayerColors = "#000000";
    private BasePilotStats basePilotStats;

    private boolean conquerable = true;

    private int techLevel = TechConstants.T_ALLOWED_ALL;
    private boolean allowDefectionsFrom = true;
    private boolean allowDefectionsTo = true;

    private List<SubFaction> subfactions = new CopyOnWriteArrayList<SubFaction>();
    public ConcurrentHashMap<String, Integer> supportedUnits = new ConcurrentHashMap<String, Integer>();
    public float usedMekBayMultiplier;
    private boolean nonFactionUnitsCostMore = false;
    // NOTE: Once MekWars uses MegaMek version 50.04 this should use megamek.common.universe.FactionTag.
    private Set<FactionTag> tags = EnumSet.noneOf(FactionTag.class);

    /**
     * @return Returns the myAbbreviation.
     */
    public String getAbbreviation() {
        return abbreviation;
    }

    public BasePilotStats getBasePilotStats() {
        return basePilotStats;
    }

    /**
     * @param myAbbreviation
     *            The myAbbreviation to set.
     */
    public void setAbbreviation(String myAbbreviation) {
        abbreviation = myAbbreviation;
    }

    /**
     * @return Returns the conquerable.
     */
    public boolean isConquerable() {
        return conquerable;
    }

    /**
     * @param conquerable
     *            The conquerable to set.
     */
    public void setConquerable(boolean conquerable) {
        this.conquerable = conquerable;
    }

    /**
     * @return Returns the factionColor.
     */
    public String getHouseColor() {
        return factionColor;
    }

    /**
     * @param factionColor
     *            The factionColor to set.
     */
    public void setHouseColor(String factionColor) {
        this.factionColor = factionColor;
    }

    /**
     * @return Returns the logo.
     */
    public String getLogo() {
        return logo;
    }

    /**
     * @param logo
     *            The logo to set.
     */
    public void setLogo(String logo) {
        this.logo = logo;
    }

    /**
     * @return Returns the logo.
     */
    public String getHouseFluFile() {
        return factionFluFile;
    }

    /**
     * @param logo
     *            The logo to set.
     */
    public void setHouseFluFile(String factionFlu) {
        this.factionFluFile = factionFlu;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    public String getNameAsLink() {
        return "<a href=\"MEKWARS/c faction#" + name + "\">" + name + "</a>";
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the id.
     */
    public int getId() {
        return id;
    }

    public House() {
        setId(EntityStore.UNSET_ID);
        basePilotStats = new BasePilotStats();
    }

    public House(StringTokenizer st) {
        setId(TokenReader.readInt(st));
        setName(TokenReader.readString(st));
        setLogo(TokenReader.readString(st));
        getBasePilotStats().setGunnery(TokenReader.readInt(st), Unit.MEK);
        getBasePilotStats().setPiloting(TokenReader.readInt(st), Unit.MEK);
        setHouseColor(TokenReader.readString(st));
        setHousePlayerColors(TokenReader.readString(st));
        setAbbreviation(TokenReader.readString(st));
        setConquerable(TokenReader.readBoolean(st));
        setTechLevel(TokenReader.readInt(st));
        setHouseDefectionFrom(TokenReader.readBoolean(st));
        setHouseDefectionTo(TokenReader.readBoolean(st));
        setUsedMekBayMultiplier(TokenReader.readFloat(st));
    }

    /**
     * Write itself to an binary stream.
     */
    public void binOut(BinWriter out) throws IOException {
        out.println(id, "id");
        out.println(name, "name");
        out.println(logo, "logo");
        basePilotStats.binOut(out);
        out.println(factionColor, "factionColor");

        out.println(factionPlayerColors, "factionPlayerColor");

        out.println(abbreviation, "abbreviation");
        out.println(conquerable, "conquerable");

        for (int type = 0; type < Unit.MAXBUILD; type++)
            for (int weight = 0; weight < 4; weight++)
                out.println(this.getHouseUnitComponentMod(type, weight), "componentMod" + type + weight);
        for (int type = 0; type < Unit.MAXBUILD; type++)
            for (int weight = 0; weight < 4; weight++)
                out.println(this.getHouseUnitPriceMod(type, weight), "priceMod" + type + weight);
        for (int type = 0; type < Unit.MAXBUILD; type++)
            for (int weight = 0; weight < 4; weight++)
                out.println(this.getHouseUnitFluMod(type, weight), "fluMod" + type + weight);

        out.println(this.getTechLevel(), "techLevel");
        out.println(this.getHouseDefectionFrom(), "defectFrom");
        out.println(this.getHouseDefectionTo(), "defectTo");
        out.println(this.getUsedMekBayMultiplier(), "usedMekBayMultiplier");

        out.println(this.tags.size(), "tagsize");
        for(FactionTag tag : tags) {
            out.println(tag.ordinal(), "value");
        }
        out.println(this.subfactions.size(), "subfactionsize");

        for (SubFaction subFaction : this.subfactions) {
            out.println(subFaction.getConfig("Name"), "SubFactionName");
            out.println(subFaction.getConfig("AccessLevel"), "SubFactionAccessLevel");
            for (int type = 0; type < Unit.MAXBUILD; type++ ){
                for ( int weight = 0; weight <= Unit.ASSAULT; weight++){
                    String setting = "CanBuyNew"+Unit.getWeightClassDesc(weight)+Unit.getTypeClassDesc(type);
                    out.println(subFaction.getConfig(setting), setting);
                    setting = "CanBuyUsed"+Unit.getWeightClassDesc(weight)+Unit.getTypeClassDesc(type);
                    out.println(subFaction.getConfig(setting), setting);
                }
            }
            out.println(subFaction.getConfig("MinELO"), "SubFactionMinELO");
            out.println(subFaction.getConfig("MinExp"), "SubFactionMinExp");
        }
    }

    /**
     * Read itself from a stream.
     */
    public House(BinReader in) throws IOException {
        id = in.readInt("id");
        name = HTMLConverter.br2cr(in.readLine("name"));
        logo = HTMLConverter.br2cr(in.readLine("logo"));
        basePilotStats = new BasePilotStats(in);
        factionColor = in.readLine("factionColor");

        factionPlayerColors = in.readLine("factionPlayerColor");

        abbreviation = in.readLine("abbreviation");
        conquerable = in.readBoolean("conquerable");

        for (int type = 0; type < Unit.MAXBUILD; type++) {
            for (int weight = 0; weight < 4; weight++) {
                this.setHouseUnitComponentMod(type, weight, in.readInt("componentMod" + type + weight));
            }
        }

        for (int type = 0; type < Unit.MAXBUILD; type++) {
            for (int weight = 0; weight < 4; weight++) {
                this.setHouseUnitPriceMod(type, weight, in.readInt("priceMod" + type + weight));
            }
        }

        for (int type = 0; type < Unit.MAXBUILD; type++) {
            for (int weight = 0; weight < 4; weight++) {
                this.setHouseUnitFluMod(type, weight, in.readInt("fluMod" + type + weight));
            }
        }

        this.setTechLevel(in.readInt("techLevel"));
        this.setHouseDefectionFrom(in.readBoolean("defectFrom"));
        this.setHouseDefectionTo(in.readBoolean("defectTo"));
        this.setUsedMekBayMultiplier((float) in.readDouble("usedMekBayMultiplier"));


        int tagSize = in.readInt("tagsize");
        for (; tagSize > 0; tagSize--) {
            int value = in.readInt("value");
            FactionTag tag = FactionTag.values()[value];
            this.tags.add(tag); 
        }

        int size = in.readInt("subfactionsize");

        this.subfactions.clear();
        for (; size > 0; size--) {
            SubFaction subFaction = new SubFaction(in.readLine("SubFactionName"));
            subFaction.setConfig("AccessLevel", in.readLine("SubFactionAccessLevel"));
            for (int type = 0; type < Unit.MAXBUILD; type++ ){
                for ( int weight = 0; weight <= Unit.ASSAULT; weight++){
                    String setting = "CanBuyNew"+Unit.getWeightClassDesc(weight)+Unit.getTypeClassDesc(type);
                    subFaction.setConfig(setting, in.readLine(setting));
                    setting = "CanBuyUsed"+Unit.getWeightClassDesc(weight)+Unit.getTypeClassDesc(type);
                    subFaction.setConfig(setting, in.readLine(setting));
                }
            }
            subFaction.setConfig("MinELO", in.readLine("SubFactionMinELO"));
            subFaction.setConfig("MinExp", in.readLine("SubFactionMinExp"));
            this.subfactions.add(subFaction);
        }

    }

    /**
     * @TODO This is only a hack and should ONLY be used by experienced
     *       personnel!
     * @param id
     *            The id to set.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @get the unit price mod for a faction
     */
    public int getHouseUnitPriceMod(int type, int weight) {
        return this.factionUnitPriceMod[type][weight];
    }

    /**
     * sets the unit price mod for a faction
     */
    public void setHouseUnitPriceMod(int type, int weight, int mod) {
        this.factionUnitPriceMod[type][weight] = mod;
    }

    /**
     * @get the unit price mod for a faction
     */
    public int getHouseUnitFluMod(int type, int weight) {
        return this.factionUnitFluMod[type][weight];
    }

    /**
     * sets the unit price mod for a faction
     */
    public void setHouseUnitFluMod(int type, int weight, int mod) {
        this.factionUnitFluMod[type][weight] = mod;
    }

    /**
     * gets the unit component mod for a faction
     */
    public int getHouseUnitComponentMod(int type, int weight) {
        return factionUnitComponentMod[type][weight];
    }

    /**
     * sets the unit component mod for a faction.
     */
    public void setHouseUnitComponentMod(int type, int weight, int mod) {
        this.factionUnitComponentMod[type][weight] = mod;
    }

    public void setHousePlayerColors(String factionPlayerColor) {
        if (factionPlayerColor.startsWith("#"))
            this.factionPlayerColors = factionPlayerColor;
        else
            this.factionPlayerColors = "#" + factionPlayerColor;
    }

    public String getHousePlayerColor() {
        return this.factionPlayerColors;
    }

    public void setTechLevel(int level) {
        if (level < TechConstants.T_INTRO_BOXSET)
            this.techLevel = TechConstants.T_ALL;
        else
            this.techLevel = level;
    }

    public int getTechLevel() {
        return this.techLevel;
    }

    public boolean getHouseDefectionFrom() {
        return allowDefectionsFrom;
    }

    public void setHouseDefectionFrom(boolean defection) {
        allowDefectionsFrom = defection;
    }

    public boolean getHouseDefectionTo() {
        return allowDefectionsTo;
    }

    public void setHouseDefectionTo(boolean defection) {
        allowDefectionsTo = defection;
    }

    public void setUsedMekBayMultiplier(float mult) {
        this.usedMekBayMultiplier = mult;
    }

    public float getUsedMekBayMultiplier() {
        return this.usedMekBayMultiplier;
    }

    public List<SubFaction> getSubfactions() {
        return Collections.unmodifiableList(subfactions);
    }

    public SubFaction getSubfaction(String name) {
        for (SubFaction subfaction : subfactions) {
            if(subfaction.getName().equals(name)) {
                return subfaction;
            }
        }
        return null;
    }

    public void addSubfaction(SubFaction subfaction) {
        if (getSubfaction(subfaction.getName()) == null) {
            subfactions.add(subfaction);
        }
    }

    public boolean removeSubfaction(String name) {
        Iterator<SubFaction> iterator = subfactions.iterator();

        while (iterator.hasNext()) {
            SubFaction subfaction = iterator.next();

            if(subfaction.getName().equals(name)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    public boolean houseSupportsUnit(String fileName) {
        if (fileName.indexOf(".") > 0)
            fileName = fileName.substring(0, fileName.indexOf("."));
        return supportedUnits.containsKey(fileName);
    }

    public ConcurrentHashMap<String, Integer> getSupportedUnits() {
        return supportedUnits;
    }

    public void addUnitSupported(String fileName) {
        if (fileName.trim().length() < 1)
            return;
        fileName = fileName.trim();
        if (houseSupportsUnit(fileName)) {
            int num = getSupportedUnits().get(fileName);
            supportedUnits.put(fileName, num + 1);
        } else {
            supportedUnits.put(fileName, 1);
        }
    }

    public void removeUnitSupported(String fileName) {
        if (fileName.trim().length() < 1)
            return;
        fileName = fileName.trim();
        if (houseSupportsUnit(fileName)) {
            int num = supportedUnits.get(fileName);
            if (num == 1) {
                // Remove it from the HashMap
                supportedUnits.remove(fileName);
            } else {
                supportedUnits.put(fileName, num - 1);
            }
        } else {
            // Error. We should never get here.
            // Fix the logging here. How to determine if it's being called from
            // CHouse or SHouse?
            // MWServ.mwlog.mainLog("Error in House.removeUnitProduction():
            // trying to remove a unit that is not produced.");
            // MWServ.mwlog.mainLog(" --> House: " + getName() + ", Unit: " +
            // fileName);
        }
    }

    public boolean getNonFactionUnitsCostMore() {
        return nonFactionUnitsCostMore;
    }

    public void setNonFactionUnitsCostMore(boolean answer) {
        nonFactionUnitsCostMore = answer;
    }

    public String addNewHouse() {
        StringBuilder result = new StringBuilder();

        result.append(id);
        result.append("|");
        
        result.append(name);
        result.append("|");
        
        if (logo.trim().length() < 1)
            result.append(" ");
        else
            result.append(logo);
        result.append("|");
        result.append(getBasePilotStats().getGunnery(Unit.MEK));
        result.append("|");
        result.append(getBasePilotStats().getPiloting(Unit.MEK));
        result.append("|");
        result.append(factionColor);
        result.append("|");
        result.append(factionPlayerColors);
        result.append("|");
        result.append(abbreviation);
        result.append("|");
        result.append(conquerable);
        result.append("|");
        result.append(this.getTechLevel());
        result.append("|");
        result.append(this.getHouseDefectionFrom());
        result.append("|");
        result.append(this.getHouseDefectionTo());
        result.append("|");
        result.append(this.getUsedMekBayMultiplier());
        result.append("|");

        return result.toString();
    }

    public void setTags(Set<FactionTag> tags) {
        this.tags = tags;
    }

    public Set<FactionTag>getTags() {
        return tags;
    }

    public boolean is(FactionTag tag) {
        return tags.contains(tag);
    }

    public boolean isInnerSphere() {
        return is(FactionTag.IS);
    }

    public boolean isClan() {
        return is(FactionTag.CLAN);
    }

    public boolean equals(House house) {
        return (house != null) && (getId() == house.getId());
    }

    /**
     * FIXME: On the client side there is no faction config. If the faction has any configurations
     * they will be placed ontop of any existing configuration in the CampaignOptions.
     */
    public MekWarsConfig getHouseOptions() {
        return CampaignData.cd.getHouseOptions(getName());
    }

    /**
     * A method which returns the influence cost of a specified campaign mech.
     * 
     * @return int - # of PP it takes to buy a mech of the given units weight
     *         class
     */
    public int getInfluenceForUnit(int weightClass, int type_id) {
        int result = Integer.MAX_VALUE;
        String classtype = Unit.getWeightClassDesc(weightClass) + Unit.getTypeClassDesc(type_id) + "Inf";

        if (type_id == Unit.MEK) {
            result = getHouseOptions().getIntegerConfig(Unit.getWeightClassDesc(weightClass) + "Inf");
        } else {
            result = getHouseOptions().getIntegerConfig(classtype);
        }

        // modify the result by the faction price modifier
        result += getHouseUnitFluMod(type_id, weightClass);
        return Math.max(result, 0);
    }
}
