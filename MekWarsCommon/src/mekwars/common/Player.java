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
 * Created on 8/25/2004
 *
 */

package mekwars.common;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

import mekwars.common.campaign.PersonalPilotQueues;
import mekwars.common.composition.IHasUnits;
import mekwars.common.flags.PlayerFlags;
import mekwars.common.util.UnitComponents;
import mekwars.common.util.UnitUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

@MappedSuperclass
public abstract class Player implements IHasUnits {
    private static final Logger LOGGER = LogManager.getLogger(Player.class);
    private static final double INITIAL_RATING = 1600;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name = "";
    private String logo = "";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_id", nullable = false)
    private House myHouse = null;

    private int money = 0;
    private int experience = 0;
    // @salient - changed from 50 to 0, starting flu can be set in SO faction.
    private int influence = 0;
    private int technicians = 0; // @urgru 7/17/04
    private int currentTechPayment = -1; // num Cbills owed to techs after games
    private int teamNumber = -1;
    private int rewardPoints = 0;

    private double rating = INITIAL_RATING;
    private boolean isInvisible = false; // Evil command for Big brother err admins.
    private boolean autoReorderParts = false;
    private UnitComponents unitComponents = new UnitComponents();
    private SubFaction subfaction = null;

    protected int totalTechs[] = new int[UnitUtils.TECH_TYPES];
    protected int availableTechs[] = new int[UnitUtils.TECH_TYPES];
    @Transient protected PlayerFlags flags = new PlayerFlags();
    // This is only going to be set for staff
    @Transient protected PlayerFlags defaultPlayerFlags = new PlayerFlags();
    // A counter for how many meks a player is allowed to create in freebuild
    protected int mekTokens = 0;
    @Column(name = "hanger_bv")
    protected int bvTracker = 0; // used to track hangar BV in mini campaigns

    public Player() {
        Arrays.fill(totalTechs, 0);
        Arrays.fill(availableTechs, 0);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public House getMyHouse() {
        return myHouse;
    }

    public void setMyHouse(House house) {
        this.myHouse = house;
    }

    public String getLogoTag() {
        return "<img height=\"140\" width=\"130\" src =\"" + logo + "\">";
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        this.name = name;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int tmoney) {
        money = tmoney;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getExperience() {
        return experience;
    }

    public boolean isClan() {
        return getMyHouse().isClan();
    }

    public void setTotalTechs(int slot, int techs) {
        if (slot < 0 || slot >= UnitUtils.TECH_TYPES) {
            return;
        }
        totalTechs[slot] = techs;
    }

    public void setAvailableTechs(int slot, int techs) {
        if (slot < 0 || slot >= UnitUtils.TECH_TYPES) {
            return;
        }
        availableTechs[slot] = techs;
    }

    public int getTotalTech(int slot) {
        return totalTechs[slot];
    }

    public int[] getTotalTechs() {
        return totalTechs;
    }

    public int getAvailableTech(int slot) {
        return availableTechs[slot];
    }

    public int[] getAvailableTechs() {
        return availableTechs;
    }


    public int getRewardPoints() {
        return rewardPoints;
    }

    public void setRewardPoints(int rewardPoints) {
        int xpRewardCap = CampaignData.cd.getCampaignOptions().getIntegerConfig("XPRewardCap");

        if (xpRewardCap != -1) {
            rewardPoints = Math.max(0, Math.min(xpRewardCap, rewardPoints));
        } else {
            rewardPoints = Math.max(0, rewardPoints);
        }

        this.rewardPoints = rewardPoints;
    }

    public void addRewardPoints(int toAdd) {
        setRewardPoints(getRewardPoints() + toAdd);
    }

    /**
     * A method which returns a players influence
     *
     * @return int - influence amount
     */
    public int getInfluence() {
        return influence;
    }

    public void setInfluence(int influence) {
        int influenceCeiling =
                CampaignData.cd.getCampaignOptions().getIntegerConfig("InfluenceCeiling");

        if (influenceCeiling != -1) {
            this.influence = Math.max(0, Math.min(influenceCeiling, influence));
        } else {
            this.influence = Math.max(0, influence);
        }
    }

    /**
     * A method to add a specified amount of influence
     *
     * @param i - amount of influence to add
     */
    public void addInfluence(int influence) {
        setInfluence(getInfluence() + influence);
    }

    /**
     * @return bvTracker value
     */
    public int getBVTracker() {
        return bvTracker;
    }

    /**
     * @param set the bvTracker value
     */
    public void setBVTracker(int bvtracker) {
        bvTracker = bvtracker;
    }

    /**
     * @return the mekTokens
     */
    public int getMekTokens() {
        return mekTokens;
    }

    /**
     * @param mekTokens the mekTokens to set
     */
    public void setMekTokens(int mekTokens) {
        this.mekTokens = mekTokens;
    }

    /**
     * @return current post-task payment to technicians, in Cbills
     */
    public int getCurrentTechPayment() {
        return currentTechPayment;
    }

    /**
     * @param i post-task payment to set, in Cbills
     */
    public void setCurrentTechPayment(int techPayment) {
        currentTechPayment = techPayment;
    }

    /**
     * @return the number of technicians the player has
     */
    public int getTechnicians() {
        return technicians;
    } // end getTechnicians()

    /**
     * @param int to set technicians to.
     */
    public void setTechnicians(int technicians) {
        int maxTechs = CampaignData.cd.getCampaignOptions().getIntegerConfig("MaxTechsToHire");

        if (maxTechs != -1) {
            this.technicians = Math.max(0, Math.min(maxTechs, technicians));
        } else {
            this.technicians = Math.max(0, technicians);
        }

        // clear the tech payment any time a new number of techs is set
        currentTechPayment = -1;
    }

    /**
     * @param the number of technicians to add (subtract) from the player's total
     *     <p>NOTE: sub-zero cases are checked in setTechs(). no check here.
     */
    public void addTechnicians(int technicians) {
        this.setTechnicians(this.technicians + technicians);
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    /**
     * Sets that a player now has the invis flag. of course players with access levels >= this
     * player will still beable to see them.
     *
     * @param invis
     */
    public void setInvisible(boolean invis) {
        isInvisible = invis;
    }

    /**
     * Does the player have the invisible flag.
     *
     * @return true/false.
     */
    public boolean isInvisible() {
        return isInvisible;
    }

    /**
     * Returns players team number
     *
     * @return
     */
    public int getTeamNumber() {
        return teamNumber;
    }

    /**
     * Set Players team number for the current op.
     *
     * @param team
     */
    public void setTeamNumber(int team) {
        this.teamNumber = team;
    }

    /**
     * Sets if the player wants to reorder parts.
     *
     * @param reorder
     */
    public void setAutoReorder(boolean reorder) {
        this.autoReorderParts = reorder;
    }

    /**
     * Returns if the player has auto reorder parts turned on.
     *
     * @return
     */
    public boolean getAutoReorder() {
        return this.autoReorderParts;
    }

    public UnitComponents getUnitComponents() {
        return unitComponents;
    }

    public void setSubfaction(SubFaction subfaction) {
        this.subfaction = subfaction;
    }

    public SubFaction getSubfaction() {
        if (subfaction == null) {
            return new SubFaction();
        }
        return subfaction;
    }

    public int getSubFactionAccess() {
        if (getSubfaction() == null) {
            return 0;
        }

        return getSubfaction().getAccessLevel();
    }

    public String getSubFactionName() {
        if (getSubfaction() == null) {
            return "";
        }

        return getSubfaction().getName();
    }

    /**
     * Sets a player flag
     *
     * @param name - the name of the flag to set
     * @param value - true or false (String value)
     */
    public void setFlagStatus(String name, String value) {
        setFlagStatus(name, Boolean.parseBoolean(value));
    }

    /**
     * Sets a player flag
     *
     * @param name - the name of the flag to set
     * @param value - true or false (boolean value)
     */
    public void setFlagStatus(String name, boolean value) {
        flags.setFlag(name, value);
    }

    /**
     * Returns the value of a flag
     *
     * @param name - the name of the flag to get
     * @return true or false (boolean value)
     */
    public boolean getFlagStatus(String name) {
        return flags.getFlagStatus(name);
    }

    /**
     * Loads the set of server-defined players flags from a string. This should probably only be
     * called during player logon, and then each flag can be set individually
     *
     * @param data
     */
    public void loadFlags(String data) {
        flags.loadDefaults(data);
    }

    /**
     * Exports the string of flags read by loadFlags
     *
     * @return flag data
     */
    public String exportFlags() {
        return flags.export();
    }

    /**
     * @return the defaultPlayerFlags
     */
    public PlayerFlags getDefaultPlayerFlags() {
        return defaultPlayerFlags;
    }

    /**
     * @return the playerFlags
     */
    public PlayerFlags getFlags() {
        return flags;
    }

    public PersonalPilotQueues getPersonalPilotQueue() {
        return null;
    }

    /**
     * A method to determine if the player is over the unit limit and the server is configured to
     * use sliding hangar cost increases
     */
    public boolean hasHangarPenalty(int uType, int uWeight) {
        // Always false if we're not using the sliding limits
        if (!getMyHouse().getHouseOptions().getBooleanConfig("UseSlidingHangarLimits")) {
            return false;
        }

        int limit = getMyHouse().getHouseOptions().getUnitLimit(uType, uWeight);

        // Always false if the particular limit is not checked
        if (limit < 0) {
            return false;
        }

        int numUnits = countUnits(uType, uWeight);
        // False if we're below the limit
        return limit < numUnits;
    }

    public int calculateHangarPenalty(int typeId, int weightclass) {
        if (!hasHangarPenalty(typeId, weightclass)) {
            return 0;
        }

        int penalty = 0;
        int limit = getMyHouse().getHouseOptions().getUnitLimit(typeId, weightclass);
        int numUnits = countUnits(typeId, weightclass);

        if (numUnits <= limit) {
            return 0;
        }

        int penaltyUnits = numUnits - limit;
        double slidingHangerLimitModifier =
                getMyHouse().getHouseOptions().getDoubleConfig("SlidingHangarLimitModifier");

        penalty = (int) Math.pow(penaltyUnits, slidingHangerLimitModifier);

        return penalty;
    }

    public int calculateTotalHangarPenalty() {
        int penalty = 0;

        for (int type = Unit.MEK; type < Unit.MAXBUILD; type++) {
            for (int weight = Unit.LIGHT; weight <= Unit.ASSAULT; weight++) {
                penalty += calculateHangarPenalty(type, weight);
            }
        }
        return penalty;
    }

    /**
     * This method does all the math to figure out how much the retainer fee, maintenance cost,
     * whathaveyou is for the current number of technicians. The number itself is useful in some
     * cases (let people know what they will have to pay after hiring a new tech, for example), and
     * thus separated from the actual payment. For now, we have only one payment calculation
     * mechanism -- additive costing, whereby each tech costs as much as the last, plus a constant
     * kicker. A cap to this cost can be configured; however, it must be a multiple of the per-tech
     * additive (eg, if the additive is .04, 1.20 would be a valid cap, but 1.30 wouldn't).
     *
     * @urgru 7/26/04
     */
    protected void doPayTechniciansMath() {
        int techs = getTechnicians();

        // don't even waste time on 0 cases. Just return.
        if (techs <= 0) {
            setCurrentTechPayment(0);
            return;
        }

        // starts as a double, gets cast back to an int for return.
        float amountToPay = 0;

        // load config variables needed to do the math ...
        float additive = getMyHouse().getHouseOptions().getFloatConfig("AdditivePerTech");
        float ceiling = getMyHouse().getHouseOptions().getFloatConfig("AdditiveCostCeiling");

        if (additive <= 0) {
            LOGGER.error(
                    "Unable to calculate technicians pay, AdditivePerTech must be above 0, but is"
                            + " {}",
                    additive);
            setCurrentTechPayment(0);
            return;
        }

        if (ceiling <= 0) {
            LOGGER.error(
                    "Unable to calculate technicians pay, AdditiveCostCeiling must be above 0, but"
                            + " is {}",
                    ceiling);
            setCurrentTechPayment(0);
            return;
        }

        /*
         * divide the ceiling by the addiive. techs past this number are all
         * charged at the ceiling rate. Example: (With 1.20 and .04, the result
         * is 30. Every additional tech (31, 32, etc.) is paid at the ceiling
         * wage.
         */
        int techCeiling = (int) (ceiling / additive);
        if (techs > techCeiling) {
            int techsPastCeiling = techs - techCeiling;
            amountToPay += ceiling * techsPastCeiling;
        } // end if(some techs are paid @ ceiling price)

        /*
         * Add up the number of times the non-ceiling techs were incremented,
         * then figure out their total cost. In cases where the ceiling is
         * passed, the flat fee techs are handled above, so only techs up to
         * that ceiling need to have the additive math done. If the ceiling isnt
         * reached, just use the number of techToPay from the param.
         */
        int techsUsingAdditive = 0;
        if (techs > techCeiling) {
            techsUsingAdditive = techCeiling;
        } else {
            techsUsingAdditive = techs;
        }

        /*
         * Faster to just to a for loop to determine the number of times the
         * additive was made (1 + 2 + 3 + 4, and so on) with ints, and THEN
         * multiply by the double additive than do alot of floating point math
         * by for-in through and multiplying by the additive each time.
         */
        int totalAdditions = 0;
        for (int i = 1; i <= techsUsingAdditive; i++) {
            totalAdditions += i;
        }

        // now figure out the final amount to pay ...
        amountToPay += totalAdditions * additive;

        // Add penalty if the player is over a sliding limit

        for (int typeId = Unit.MEK; typeId < Unit.MAXBUILD; typeId++) {
            for (int weightclass = Unit.LIGHT; weightclass <= Unit.ASSAULT; weightclass++) {
                if (hasHangarPenalty(typeId, weightclass)) {
                    int costPenalty = calculateHangarPenalty(typeId, weightclass);
                    amountToPay += costPenalty;
                }
            }
        }
        /*
         * now return the amount in INT form since we don't support fractional
         * money. also, set the currentTechPayment, to avoid doing this math
         * again if possible.
         */
        setCurrentTechPayment(Math.max(0, Math.round(amountToPay)));
    }
}
