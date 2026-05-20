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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.StringTokenizer;

import mekwars.common.CampaignData;
import mekwars.common.Player;
import mekwars.common.SubFaction;
import mekwars.common.Unit;
import mekwars.common.campaign.pilot.Pilot;
import mekwars.common.campaign.pilot.skills.PilotSkill;
import mekwars.common.composition.HasUnits;
import mekwars.common.flags.PlayerFlags;
import mekwars.common.util.TokenReader;
import mekwars.common.util.UnitUtils;
import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.MWServ;
import mekwars.server.campaign.market2.IBuyer;
import mekwars.server.campaign.market2.ISeller;
import mekwars.server.campaign.mercenaries.ContractInfo;
import mekwars.server.campaign.mercenaries.MercHouse;
import mekwars.server.campaign.pilot.SPilot;
import mekwars.server.campaign.util.ExclusionList;
import mekwars.server.campaign.util.OpponentListHelper;
import mekwars.server.campaign.util.SerializedMessage;
import mekwars.server.campaign.util.scheduler.UserActivityComponentsJob;
import mekwars.server.campaign.util.scheduler.UserActivityInfluenceJob;
import mekwars.server.util.MWPasswdRecord;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.processing.CheckHQL;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.StringTokenizer;

/**
 * A class representing a Player DOCU is not finished
 *
 * @author Helge Richter (McWizard)
 * @author Bob Eldred (Spork)
 * @version 2016.10.06
 *     <p>Modifications: - Moved slice flu generation to a Quartz task
 */
@NamedQueries({
    @NamedQuery(
            name = "SPlayer.findAllInHouse",
            query = "FROM SPlayer WHERE myHouse.id = :houseId"),
    @NamedQuery(
            name = "SPlayer.findPlayersInStatus",
            query = "FROM SPlayer WHERE AND status = :status"),
    @NamedQuery(
            name = "SPlayer.getAllLoggedIn",
            query = "FROM SPlayer WHERE status != " + SPlayer.STATUS_LOGGEDOUT),
    @NamedQuery(name = "SPlayer.findByName", query = "FROM SPlayer WHERE name = :name")
})
@FilterDef(
        name = "FilterByOnline",
        parameters = @ParamDef(name = "status", type = Integer.class),
        defaultCondition = "status != " + SPlayer.STATUS_LOGGEDOUT)
@Filter(name = "FilterByOnline")
@CheckHQL
@Entity
@Table(name = "player")
public class SPlayer extends Player<SUnit> implements Comparable<Object>, IBuyer, ISeller {
    private static final Logger LOGGER = LogManager.getLogger(SPlayer.class);

    // STATIC VARIABLES
    // STATUS_DISCONNECTED, which is used by the client, is 0
    public static final int STATUS_LOGGEDOUT = 1;
    public static final int STATUS_RESERVE = 2;
    public static final int STATUS_ACTIVE = 3;
    public static final int STATUS_FIGHTING = 4;

    // @salient Mini Campaign Phases
    private static final String RESTOCK_MC = "restockmc"; // @salient for minicampaigns
    private static final String ACTIVE_MC = "activemc"; // @salient for minicampaigns

    // DATA VARIABLES (SAVED. Most have gets and sets.)
    private String fluffText = "";

    @Column(name = "last_isp")
    private String lastISP = "";

    private int xpTillReward =
            0; // counter until next RP injection triggered by XP gains, see XPRollOverCap in server
    // options
    private int xpTillFlu =
            0; // @ Salient , same as above. counter until next flu injection triggered by XP gains.
    private int groupAllowance = 0;
    private int baysOwned = 0;

    private long lastOnline = 0;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private List<SArmy> armies = new ArrayList<>();

    private SPersonalPilotQueues personalPilotQueue = new SPersonalPilotQueues();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "exclusion_list_id")
    private ExclusionList exclusionList = new ExclusionList();

    // SEMI-PERMANENT VARIABLES. Not saved to String.

    // @salient , I foresee mini campaigns becoming ever more complex
    // this section will contain strings to be saved together as a
    // serialized message embedded into the player save.
    private String phaseMC = ACTIVE_MC;

    // Same goes for discord Info for use by bot
    @Column(name = "discord_id")
    private String discordID = ""; // @salient will be set by DiscordInfo

    private int scrapsThisTick = 0;
    private int donationsThisTick = 0;

    private double weightedArmyNumber = -1;

    private long lastTimeCommandSent = 0;
    private long lastAttackFromReserve = 0;
    private long activeSince = 0;
    private long attackRestrictionUntil = 0;

    private String sellingto = "";
    private Version clientVersion; // version gets sent by the player and
    // set

    @Transient private MWPasswdRecord password = null;

    private boolean userValidated = false;

    @Transient boolean isLoading = false; // Player was getting saved multiple times
    // during loading. Just seemed silly. Adding this
    // back in, as saving during load is causing DB
    // issues.

    private long lastPromoted = 0;

    public volatile int leechCount = 0;

    private int status = STATUS_LOGGEDOUT;

    // CONSTRUCTORS
    /**
     * Stock constructor. Note that an SPlayer is data-less unless/until fromString() or some sets
     * are called. SPlayers are created in only two places - CampaignMain's load method and the
     * EnrollCommand.
     */
    public SPlayer() {
        setMyHouse(
                CampaignData.cd.getHouseByName(
                        CampaignData.cd.getCampaignOptions().getConfig("NewbieHouseName")));
    }

    /* TODO: This is unwanted but necessary. We need to cast here because we get compile errors for
     * two reasons
     * 1. CampaignData.cm.getHouse returns a House but is used by both the client and server so it
     *   cannot be modified.
     * 2. SHouse has a legacy getHouseConfig method that points to
     *   CampaignData.cm.getCampaignOptions()
     */
    public SHouse getMyHouse() {
        return (SHouse) super.getMyHouse();
    }

    /**
     * @see IHasUnits#getUnits
     */
    @Override
    public List<SUnit> getUnits() {
        return (List<SUnit>) Collections.unmodifiableList(units.getAll());
    }

    /**
     * @see IHasUnits#getUnit(int)
     */
    @Override
    public SUnit getUnit(int id) {
        return units.get(id);
    }

    /**
     * @see IHasUnits#addUnit(Unit, int)
     */
    @Override
    public void addUnit(int position, Unit unit) {
        unit.setOwner(this);
        units.add(position, (SUnit) unit);
    }

    /**
     * @see IHasUnits#addUnit(Unit)
     */
    @Override
    public void addUnit(Unit unit) {
        unit.setOwner(this);
        units.add((SUnit) unit);
    }

    /**
     * @see IHasUnits#removeUnit(int)
     */
    @Override
    public boolean removeUnit(int id) {
        SUnit unit = units.get(id);
        
        if (unit != null) {
            unit.setOwner(null);
            units.remove(id);
            return true;
        }
        return false;
    }

    /**
     * @see IHasUnits#getUnitCount()
     */
    @Override
    public int getUnitCount() {
        return units.count();
    }

    /**
     * @see IHasUnits#countUnits(int, int)
     */
    @Override
    public int countUnits(int type, int weightClass) {
        return units.count(type, weightClass);
    }

    /**
     * @see IHasUnits#clear()
     */
    @Override
    public void clearUnits() {
        units.clear();
    }

    /**
     * Save player file immediatly.
     */
    public void setSave() {
        if (!isLoading) {
            CampaignMain.cm.forceSavePlayer(this);
        }
    }

    // PUBLIC METHODS
    /** Override the standard Object.equals(), compare two instances of a player by name only. */
    @Override
    public boolean equals(Object o) {

        SPlayer p = null;
        try {
            p = (SPlayer) o;
        } catch (ClassCastException e) {
            return false;
        }

        if (p == null) {
            return false;
        }

        return getId() == p.getId();
        // if (p.getName().equals(getName())) {
        //     return true;
        // }

        // // else
        // return false;
    }

    public boolean hasSameIP(SPlayer otherPlayer) {
        try {
            String p1 = MWServ.getInstance().getIP(getName()).toString();
            String p2 = MWServ.getInstance().getIP(otherPlayer.getName()).toString();

            return p1.equals(p2);
        } catch (Exception e) {
            LOGGER.error("Exception while checking players' IPs", e);
        }
        return false;
    }

    /**
     * A Method that returns a rounded ELO rating for this player. Used to send truncated doubles to
     * the userlist.
     *
     * @return the rounded rating
     */
    public double getRatingRounded() {
        BigDecimal bd = new BigDecimal(getRating());
        bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Determine whether or not a player can use a unit of a given weight class. This is used to
     * prevent new players from buying heavier/larger units and sucking a house dry.
     *
     * @param - weight class to check.
     */
    public boolean mayUse(int weightClass) {
        // @Salient adding this in for Gunny
        if (weightClass == Unit.LIGHT) {
            if (Integer.parseInt(getMyHouse().getConfig("MinEXPforLight")) > getExperience()) {
                return false;
            }
        }
        if (weightClass == Unit.MEDIUM) {
            if (Integer.parseInt(getMyHouse().getConfig("MinEXPforMedium")) > getExperience()) {
                return false;
            }
        }
        if (weightClass == Unit.HEAVY) {
            if (Integer.parseInt(getMyHouse().getConfig("MinEXPforHeavy")) > getExperience()) {
                return false;
            }
        }
        if (weightClass == Unit.ASSAULT) {
            if (Integer.parseInt(getMyHouse().getConfig("MinEXPforAssault")) > getExperience()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Add a unit to the player. Pass-though to addUnit(SUnit,boolean,boolean). This version should
     * be called in almost all situations.
     */
    public void addUnit(SUnit m, boolean isNew) {
        this.addUnit(m, isNew, true);
    }

    /**
     * Add a unit to the player. If the unit is new, make it immune to maintenance scraps. Nearly
     * all calls should send updates to a client; however, in some instances (ex: when giving units
     * to a SOL player), bandwidth is saved by doing a single PS| at the end of a series of adds.
     */
    public String addUnit(SUnit m, boolean isNew, boolean sendUpdates) {
        if (isNew) {
            long immunityTime = Long.parseLong(getMyHouse().getConfig("ImmunityTime")) * 1000;
            m.setPassesMaintainanceUntil(System.currentTimeMillis() + immunityTime * 2);
        }

        // clear any scrap allowance
        m.setScrappableFor(-1);

        /*
         * OK if there's room, unmaintained if not. This also strips any
         * FOR_SALE from units purchased via the market.
         */
        if (getFreeBays()
                < (CampaignMain.cm.isUsingIncreasedTechs()
                        ? SUnit.getHangarSpaceRequired(
                                m,
                                getMyHouse().houseSupportsUnit(m.getUnitFilename()),
                                getMyHouse())
                        : SUnit.getHangarSpaceRequired(m, getMyHouse()))) {
            m.setUnmaintainedStatus();
        } else {
            m.setStatus(Unit.STATUS_OK);
        }

        // strip illegal ammo
        SUnit.checkAmmoForUnit(m, getMyHouse());

        m.setPosId(getFreeID());
        synchronized (getUnits()) {
            addUnit(m);
        }

        /*
         * Send PL|HD. Client-side reading of HD adds units to the hangar
         * instead of clearing/replacing the hangar, so we can send just this
         * one, if we like. Send status update to the client (status determined
         * above), along with total and free bay/tech info.
         */
        if (sendUpdates) {
            CampaignMain.cm.toUser("PL|HD|" + m.toString(true), getName(), false);
            CampaignMain.cm.toUser("PL|SUS|" + m.getId() + "#" + m.getStatus(), getName(), false);
            CampaignMain.cm.toUser("PL|SB|" + getTotalMekBays(), getName(), false);
            CampaignMain.cm.toUser("PL|SF|" + getFreeBays(), getName(), false);
        }

        // make sure to save the player, with his fancy new unit ...
        setSave();

        String penaltyString = buildHangarPenaltyString();
        CampaignMain.cm.toUser("PL|SHP|" + penaltyString, getName(), false);

        // LOGGER.debug("Checking Anti-Air");
        // m.isAntiAir();

        return ""; // dummy string returned to comply with IBuyer
    }

    /**
     * ISeller-compliant .removeUnit(). Simply get the unit ID and pass to
     * normal SPlayer.removeUnit(int,bool). Use the (int,boolean) version of
     * remove unit whenever possible in order to intelligently pass select the
     * army update option. ISeller assumes true and sends updates to all armies.
     *
     * @urgru 1.2.06
     */
    public String removeUnit(SUnit unitToRemove, boolean sendHouseStatusUpdate) {
        this.removeUnit(unitToRemove.getId(), true);
        String penaltyString = buildHangarPenaltyString();
        CampaignMain.cm.toUser("PL|SHP|" + penaltyString, getName(), false);
        return ""; // dummy stirng returned for IBuyer
    }

    /**
     * Remove the Unit with ID unitId from the player. Ops are checked by discrete commands (ie -
     * SellUnit), unchecked by large blocks of code which force a check on their own (ie -
     * ShortResolver).
     *
     * @param unitId the ID of the unit to remove
     */
    public void removeUnit(int unitId, boolean sendArmyUpdate) {
        SUnit Mech = null;

        removeUnit(unitId);
        for (SArmy currA : getArmies()) {
            if (currA.getUnitPosition(unitId) > -1) {
                currA.removeUnit(unitId);
                if (sendArmyUpdate) {
                    CampaignMain.cm.toUser("PL|SAD|" + currA.toString(true, "%"), getName(), false);
                    CampaignMain.cm.getOpsManager().checkOperations(currA, true); // update
                    // legal
                    // ops
                }
            }
        } // end for(all armies)

        CampaignMain.cm.toUser("PL|RU|" + unitId, getName(), false);
        CampaignMain.cm.toUser("PL|SB|" + getTotalMekBays(), getName(), false);
        CampaignMain.cm.toUser("PL|SF|" + getFreeBays(), getName(), false);
        setSave(); // save on remove (adminstrip, etc)
    }

    /**
     * Method which determines the number ot free bays/techs a player has. Simple loop through the
     * hangar.
     *
     * @return number of free bays/techs
     */
    public int getFreeBays() {
        int free = getTotalMekBays();
        int totalProtos = 0;
        boolean advanceRep = CampaignMain.cm.isUsingAdvanceRepair();

        /*
         * Loop through all units. Those with STATUS_OK and STATUS_FORSALE take
         * up space. Units with STATUS_UNMAINTAINED and STATUS_DESTROYED don't
         * require techs. Protos get special point-based handling. They're
         * counted and passed off to this.getTechRequiredForProtos(), which
         * determines exactly how many techs are needed for any ProtoMek
         * grouping.  Christmas gifts are excluded from cost.
         */
        for (SUnit currU : getUnits()) {

            if (((currU.getStatus() == Unit.STATUS_OK)
                            || (currU.getStatus() == Unit.STATUS_FORSALE))
                    && (!currU.isChristmasUnit())) {
                if (CampaignMain.cm.isUsingIncreasedTechs()) {
                    free -=
                            SUnit.getHangarSpaceRequired(
                                    currU,
                                    getMyHouse().houseSupportsUnit(currU.getUnitFilename()),
                                    getMyHouse());
                } else {
                    free -= SUnit.getHangarSpaceRequired(currU, getMyHouse());
                }

                // proto counting
                if ((currU.getEntity() instanceof Protomech) && !advanceRep) {
                    if (!currU.getPilot().getSkills().has(PilotSkill.AstechSkillID)) {
                        totalProtos++;
                    }
                } else if (currU.getEntity() instanceof Protomech) {
                    totalProtos++;
                }
            }
        } // end while(more unit data)

        /*
         * Adjust for proto points.
         */
        if (totalProtos > 0) {
            int techRatio = Integer.parseInt(CampaignMain.cm.getConfig("TechsToProtoPointRatio"));
            double ppoints = totalProtos / 5.0; // 5 protos in a point
            int ptechs = (int) (ppoints * techRatio);

            if (ptechs < 1) {
                ptechs = 1;
            }
            free -= ptechs;
        }

        return free;
    }

    /**
     * This can be calcualted in one of three "standard" ways: 1) House bays + techs 2) House bays +
     * experience 3) House bays + techs + experience Or, two additional ways if using Advanced
     * Repair: 4) House Bays + bays owned by player 5) House bays + bays owned by player +
     * experience
     *
     * @return the total amount of bays this player has
     */
    public int getTotalMekBays() { // return bay/support number
        int numBays = 0; // amount to return

        boolean usesXP = getMyHouse().getBooleanConfig("UseExperience");
        boolean usesTechs = getMyHouse().getBooleanConfig("UseTechnicians");
        boolean usesAdvanceRepairs = CampaignMain.cm.isUsingAdvanceRepair();

        if (usesAdvanceRepairs) {
            usesTechs = false;
        }

        // include the basic bays. flat amount for mercs/SOL, warehouse # for
        // GreatHouses
        int BASE_BAYS = getMyHouse().getBaysProvided();
        numBays += BASE_BAYS;

        /*
         * Make sure all non-merc players meet a minimum free bay standard.
         * Useful for small factions on large servers (Marians, etc) and
         * factions which lose a large number of their warehouse worlds,
         * dropping fresh-from-SOL players to an unacceptably low # of bays.
         * Don't give these to mercenaries.
         */
        if (!getMyHouse().isMercHouse()) {
            int minBays = getMyHouse().getIntegerConfig("MinimumHouseBays");
            if (numBays < minBays) {
                numBays = minBays;
            }
        } // end if(non-merc)

        // then add the bays from XP, if the config says to...
        if (usesXP) {
            int experienceForBay = getMyHouse().getIntegerConfig("ExperienceForBay");
            // check for stupid settings to avoid division by 0
            if (experienceForBay != 0) {
                int maxBaysFromXP = getMyHouse().getIntegerConfig("MaxBaysFromEXP");
                int expBays = (getExperience() / experienceForBay);
                if (expBays > maxBaysFromXP) {
                    expBays = maxBaysFromXP;
                }
                numBays += expBays;
            } else {
                LOGGER.error(
                        "0 is invalid setting for EXP for Bay Setting when using xp for bays!");
            }
        }

        // and now add the bays from techs if config'ed...
        if (usesTechs) {
            numBays += getTechnicians();
        }

        // now add bays if you are using advanced repairs
        if (usesAdvanceRepairs) {
            numBays += baysOwned;
        }

        return numBays;
    } // end TotalMechBays()

    /**
     * Should be called only after an attempt to pay techs comes up short. At present, only used by
     * ShortResolver. Other times techs are paid (eg - TransferCommand) shortfalls stop the player
     * from acting. Does all the dirty work of lowering the number of technicians and setting units
     * as unmaintained.
     *
     * @param amountofShortFall - the amount owed to techs which can't be paid. used to determine
     *     how many walk off / quit.
     * @return numLost - the number of techs or bays lost.
     */
    public int doFireUnpaidTechnicians(float amountOfShortFall) {

        // String toReturn = "";

        // layoffs all around! well, at least some. so reset the
        // currentTechPayment
        setCurrentTechPayment(-1);

        // load config variables needed to do the calculations
        float additive = Float.parseFloat(getMyHouse().getConfig("AdditivePerTech"));
        float ceiling = Float.parseFloat(getMyHouse().getConfig("AdditiveCostCeiling"));

        int currentTechs = getTechnicians(); // current number of techs
        int techCeiling = (int) (ceiling / additive); // the ceiling

        /*
         * Start by getting rid of the most expensive techs (those at the
         * ceiling). Loop until the player is able to afford the bill, or all
         * techs above the ceiling have been dismissed.
         */
        while ((amountOfShortFall > 0) && (currentTechs > techCeiling)) {
            currentTechs = currentTechs - 1;
            amountOfShortFall -= ceiling;
        }

        /*
         * Now start getting rid of the less expensive techs. Each tech costs
         * his # times his additive amount. Loop until theyre all gone, or the
         * bill can be paid.
         */
        while (amountOfShortFall > 0) {

            // fire a tech and reduce shortfall by his cost
            float costOfCurrentTech = currentTechs * additive;
            currentTechs = currentTechs - 1;
            amountOfShortFall -= costOfCurrentTech;

            // catch zero techs, just in case there IS rounding funkiness
            if (currentTechs == 0) {
                amountOfShortFall = 0;
            }
        }

        int numberOfTechsFired = getTechnicians() - currentTechs;
        addTechnicians(-numberOfTechsFired);

        return numberOfTechsFired;
    }

    /**
     * Method that returns the current cost of hiring a new technician, after adjustment for XP,
     * etc. Used by HireTechsCommand, Requests and SetMaintainedCommand.
     */
    public int getTechHiringFee() {
        // get the starting tech cost
        int techCost = Integer.parseInt(CampaignMain.cm.getConfig("BaseTechCost"));

        /*
         * Check to see if tech hiring costs should be decreased with
         * experience. If they should be, load the amount of XP for each
         * reduction, and the pricing floor. Loop through the XP amount reducing
         * cost until the floor is reached, or there isnt enough XP to reduce
         * price further.
         */
        boolean decreaseWithXP = Boolean.parseBoolean(getMyHouse().getConfig("DecreasingTechCost"));
        if (decreaseWithXP) {
            // if it decreases, see how much
            int xpToDecrease = Integer.parseInt(getMyHouse().getConfig("XPForDecrease"));
            int minTechCost = Integer.parseInt(getMyHouse().getConfig("MinimumTechCost"));

            int numDecreases = (int) Math.floor(getExperience() / xpToDecrease);
            techCost -= numDecreases;

            if (techCost < minTechCost) {
                techCost = minTechCost;
            }

            // catch error, in case server is misconfigured
            if (techCost < 0) {
                techCost = 0;
            }
        }
        return techCost;
    }

    /**
     * A method which is called to randomly set some units as unmaintained when support levels go
     * negative. Continues until support number is positive again, or all units are unsupported
     * (catches odd problems with units on the black market -- not an expecially graceful solution;
     * however, the alternative is allowing units on the BM to be scrapped mid-auction).
     *
     * @urgru 8/2/04
     */
    public int setRandomUnmaintained() {

        // holder.
        int numUnmaintained = 0;

        // filter out units which are already unmaintained, for_sale or
        // destroyed
        ArrayList<SUnit> okUnitsData = new ArrayList<>();
        for (SUnit currU : getUnits()) {
            if (currU.getStatus() == Unit.STATUS_OK) {
                okUnitsData.add(currU);
            }
        }

        while (getFreeBays() < 0) {

            if (okUnitsData.size() == 0) {
                return numUnmaintained;
            }

            // passed the catch. unmaintain some units.
            int rnd = CampaignMain.cm.getRandomNumber(okUnitsData.size()); // generate
            // a
            // RND
            SUnit unit = okUnitsData.get(rnd); // get unit @ rnd location
            unit.setUnmaintainedStatus(); // make it unmaintained
            numUnmaintained++;
            CampaignMain.cm.toUser(
                    "PL|UU|" + unit.getId() + "|" + unit.toString(true), getName(), false);
            okUnitsData.remove(rnd); // and remove it from the vector
        } // end while(no free bays)

        setSave();
        return numUnmaintained;
    } // end setRandomUnmaintained

    /**
     * Loop through the units and perform maintainance. Check status and adjust maintainance level
     * accordingly. This is called during slices. Check to ses if units are maintained -- if so,
     * improve maintainance levels. If not, roll a random. If its greater than the maintainance
     * level, scrap the unit. If unit should be scrapped, or just have its mainainance level
     * reduced. Note that units on the BM arent included in the maintainance loop. It should be
     * impossible to add an unmaintained unit to the BM, but just in case, they're excluded
     * (STATUS_FORSALE is ignored). This prevents off BM nulls.
     */
    public void doMaintainance() {
        if (CampaignMain.cm.isUsingAdvanceRepair()) {
            return;
        }
        int increase = Integer.parseInt(getMyHouse().getConfig("MaintainanceIncrease"));
        int decrease = Integer.parseInt(getMyHouse().getConfig("MaintainanceDecrease"));

        ArrayList<SUnit> unitsToDestroy = new ArrayList<SUnit>();
        for (SUnit currUnit : getUnits()) { // loops through all units

            // if the unit is maintained, boost its level
            if (currUnit.getStatus() == Unit.STATUS_OK) {
                currUnit.addToMaintainanceLevel(increase);
            } else if (currUnit.getStatus() == Unit.STATUS_UNMAINTAINED) {
                int rnd = CampaignMain.cm.getRandomNumber(100) + 1;

                // immediately after a game, only decrement. don't scrap.
                long currTime = System.currentTimeMillis();
                if (MWServ.getInstance().getIThread().isImmune(this)
                        || (currUnit.getPassesMaintainanceUntil() > currTime)) {
                    currUnit.addToMaintainanceLevel(-decrease);
                } else if (rnd <= currUnit.getMaintainanceLevel()) {
                    currUnit.addToMaintainanceLevel(-decrease);
                }

                // unmaintained and failed scrap check. blow 'er up.
                else {

                    if (getMyHouse().isNewbieHouse()) {
                        CampaignMain.cm.toUser(
                                "Your "
                                        + currUnit.getModelName()
                                        + " is badly maintained and failed a survival roll. In a"
                                        + " normal faction, failing these rolls <b>destroys</b> the"
                                        + " unit. In the training faction you simply get this"
                                        + " warning. Take heed.",
                                getName(),
                                true);
                        return;
                    } // break out if trying to scrap a SOL mech

                    // if scrapping costs bills, subtract the appropriate
                    // amount.
                    int mechscrapprice =
                            Math.round(
                                    getMyHouse()
                                                    .getPriceForUnit(
                                                            currUnit.getWeightClass(),
                                                            currUnit.getType())
                                            * Float.parseFloat(
                                                    getMyHouse().getConfig("ScrapCostMultiplier")));
                    if (getMoney() < mechscrapprice) {
                        mechscrapprice = getMoney();
                    }
                    if (mechscrapprice > 0) {
                        addMoney(-mechscrapprice);
                    }

                    // remove all flu, even if scrapping is free
                    int flutolose = getInfluence();
                    addInfluence(-flutolose);

                    String toSend =
                            "Lack of maintainance has forced your techs to scrap "
                                    + currUnit.getPilot().getName()
                                    + "'s "
                                    + currUnit.getModelName()
                                    + " for parts. HQ is displeased (";
                    if (mechscrapprice > 0) {
                        toSend +=
                                CampaignMain.cm.moneyOrFluMessage(
                                                true, false, -mechscrapprice, true)
                                        + ", ";
                    }
                    toSend +=
                            CampaignMain.cm.moneyOrFluMessage(false, false, -flutolose, true)
                                    + ").";
                    CampaignMain.cm.toUser(toSend, getName(), true);

                    getMyHouse().addDispossessedPilot(currUnit, false);
                    unitsToDestroy.add(currUnit); // actually removing now
                    // would cause conc mod
                    // error
                } // end else(failed scrap check)
            } // end else if(isnt maintained)
        } // end for(all elements)

        /*
         * remove those units which were destroyed. no need to send updates b/c
         * unmaintained units can't be in armies.
         */
        for (SUnit destroyedU : unitsToDestroy) {
            this.removeUnit(destroyedU.getId(), false);
        }
    } // end doMaintainance()

    /**
     * Method which checks to see if a player owns an unmaintained unit. Called from Request,
     * RequestDonated, Transfer and other commands. Hacky direct access of SUnitData, but
     * constructing an SUnit when we have direct access to the status and no intent to change it is
     * a bit wasteful.
     *
     * @return boolean indicating owndership of an unmaintained unit.
     */
    public boolean hasUnmaintainedUnit() {

        for (SUnit currU : getUnits()) {
            if (currU.getStatus() == Unit.STATUS_UNMAINTAINED) {
                return true;
            }
        }

        // no unmaintained unit found.
        return false;
    }

    /**
     * Transition a player from reserve to active, or vice versa. See in-line comments for more
     * detail.
     *
     * @param newStatus - true to activate, false to deac.
     */
    public void setActive(boolean newStatus) {
        // lower case the getName() only once
        String lowerName = getName().toLowerCase();

        // de-activating
        if (!newStatus) {

            activeSince = 0; // deactivating. make a 0.
            setLastOnline(System.currentTimeMillis());

            /*
             * Player is being moved to ianctive status. This means he is no
             * longer an eligible attack target. Need to remove his oplists and
             * clear his entries on other players oplists.
             */
            OpponentListHelper olh = new OpponentListHelper(this);
            olh.execute(OpponentListHelper.MODE_REMOVE);
            olh.sendInfoToOpponents("left the front lines and may no longer be attacked");

            /*
             * The player also needs to be removed as a possible defender from
             * all outstanding operations. Loop through the ops, removing him
             * from their defender/chicken trees. It's safe to assume that any
             * deactivation in the face of attack deserves a penalty, so call
             * the punishing shutdown. NOTE: The chicken threads call
             * setActive(false) in order to turn off someone who has been
             * leeched. This means that the thread is calling its own
             * doPenalty() methods indirectly here ... but also lets the first
             * thread to hit the leach ceiling turn off any other attacks
             * against the player.
             */
            CampaignMain.cm
                    .getOpsManager()
                    .removePlayerFromAllPossibleDefenderLists(getName(), true);

            /*
             * Remove the player from all attacker lists. It is presumed that a
             * player who is fighting could never finish the Deactivate command.
             * If a player has gotten this far, his attacks must be in WAITING
             * status, so we remove the player from the games and cancel if they
             * hit 0 attackers.
             */
            CampaignMain.cm.getOpsManager().removePlayerFromAllAttackerLists(this, null, true);

            setStatus(STATUS_RESERVE);

            // NOTE: Deactivation does NOT call IThread.removeImmunity(). This
            // lets SOL reset units.
            // We remove immunity when someone re-activates instead.
        }

        // activating
        else {

            // activating. set current timestamp and clear immunity.
            activeSince = System.currentTimeMillis();
            MWServ.getInstance().getIThread().removeImmunity(this);

            /*
             * Player is activating. His armies are all acceptable, and his
             * status has changed. Broadcast his army values to other players
             * and construct opponent vectors for the newly activated armies.
             * [NOTE: actual checks moved into a helper class so they can be run
             * as a player logs in w/ a running game and after games as well].
             */
            OpponentListHelper olh = new OpponentListHelper(this);
            olh.execute(OpponentListHelper.MODE_ADD);
            olh.sendInfoToOpponents("is headed to the front lines. You may attack it with ");

            // Update the persisted status
            setStatus(STATUS_ACTIVE);
        }
    }

    /**
     * Standard active/fighting rotation. Use setFighting(bool,bool) to move a player to reserve
     * from fighting after an AFR game, and this method for everything else.
     */
    public void setFighting(boolean newStatus) {
        this.setFighting(newStatus, false);
    }

    /**
     * Transition a player between fighting and active status.
     *
     * @param name
     */
    public void setFighting(boolean newStatus, boolean toReserve) {

        // lower case the name only once
        String lowerName = getName().toLowerCase();

        // switch to fighting
        if (newStatus) {
            setStatus(STATUS_FIGHTING);

            // send status update to the user
            CampaignMain.cm.toUser("CS|" + STATUS_FIGHTING, getName(), false);

            /*
             * Player is being moved to busy status. This means he is no longer
             * an eligible attack target. Need to remove his oplists and clear
             * his entries on other players oplists. Note that this has no
             * effect on players who are being set as Busy immediately after
             * logging in because they disconnected mid-game since they have
             * empty op lists.
             */
            OpponentListHelper olh = new OpponentListHelper(this);
            olh.execute(OpponentListHelper.MODE_REMOVE);
            olh.sendInfoToOpponents(" entered combat and may no longer be attacked");
        }

        // de-fight from AFR. Move to reserve.
        else if (toReserve) {
            activeSince = 0;
            setStatus(STATUS_RESERVE);
            UserActivityComponentsJob.stop(getName());
            UserActivityInfluenceJob.stop(getName());
        } else {
            setStatus(STATUS_ACTIVE);

            /*
             * If player was STATUS_FIGHTING and is being moved back into
             * STATUS_ACTIVE, either - a game was cancelled; or - a game was
             * finished. If we're dealing with a finished game, let the
             * ImmunityThread handle OpponentList issues. If a cancel, there
             * will not be any immunity and updates should be sent to all
             * players immediately.
             */
            if (!MWServ.getInstance().getIThread().isImmune(this)) {
                OpponentListHelper olh = new OpponentListHelper(this);
                olh.execute(OpponentListHelper.MODE_ADD);
                olh.sendInfoToOpponents(" halted combat operations and returned to its post. You may attack it with ");
            }
        }
    } // end setFighting(boolean b)

    /**
     * Method which sets a player to fighting without triggering Oplist construction. DO NOT USE
     * THIS METHOD. It is a special activation/business sequence that is used only when a player is
     * returning to the server and already involved in a game and should only be called from
     * ShortOperation. All standard activations and ALL deactivations should be dealt with via
     * SPlayer.setActive(boolean), which sets up opponent lists, informs potential attackers, etc.
     */
    public void setFightingNoOppList() {
        // no immunity from immediate activation
        MWServ.getInstance().getIThread().removeImmunity(this);

        // mark this as the time-of-activation
        activeSince = System.currentTimeMillis();

        setStatus(STATUS_FIGHTING);
        CampaignMain.cm.toUser("CS|" + SPlayer.STATUS_FIGHTING, getName(), false);
    }

    /**
     * Method that determines the weighted number or armies a player has active. Each army gives an
     * initial weight of 1. Weight for an army is reduced if its BV +/- MaxBVDifference (from
     * campaign configuration) overlaps another armies BV, falls below MinCount or rises above
     * MaxCount. In short, only the portions of an army which may be *uniquely* targetted by
     * opposing forces with the Min/Max range count fully. The weight is automatically reduced by
     * the level of overlap, and server operators may declare additional overlap penalties. Example:
     * Player A has Armies of 3000 and 3050 BV. MaxBVDifference is 150, and an OverlapPenalty of .20
     * is set in campaignconfig.txt - Starting weight is 2 for two armies, - Raw amount of overlap
     * is (150-(3050-3000 = 50))/150 = .67 - Weight after raw overlap adjustment is 2.0 - 0.67 =
     * 1.33 - OverlapPenalty is applied (1.33 - .20 = 1.13) In this case, the final weighted number
     * of armies is 1.37.
     *
     * @return int the weighted army number
     * @author urgru 10/27/04
     */
    public double getWeightedArmyNumber() {
        // only get the weight if it hasnt been calculated already.
        if (weightedArmyNumber <= 0) {

            ArrayList<SArmy> orderedArmies = new ArrayList<>();

            LOGGER.debug("Start getWeightedArmyNumber for " + getName());
            int MinCount = getMyHouse().getIntegerConfig("MinCountForTick");
            int MaxCount = getMyHouse().getIntegerConfig("MaxCountForTick");
            int MaxFlatDiff = 1;
            int legalOps = 0;
            double MaxPercentDiff = 0.0;

            for (SArmy currentArmy : getArmies()) {
                // only count armies within the defined Min/Max range
                int forceBV = currentArmy.getOperationsBV(null);
                if (forceBV <= MinCount) {
                    continue;
                }

                if (forceBV >= MaxCount) {
                    continue;
                }

                // Don't count the army if it's disabled
                if (currentArmy.isDisabled()) {
                    continue;
                }

                // if they army is only set up for ops that SO's do not deem
                // legal for component production then the player doesnt get
                // anything.
                boolean fLegalOp = false;
                for (String Opname : currentArmy.getLegalOperations()) {
                    if (!CampaignMain.cm
                            .getOpsManager()
                            .getOperation(Opname)
                            .getBooleanValue("DoesNotCountForPP")) {
                        fLegalOp = true;
                        MaxFlatDiff +=
                                Math.max(
                                        0,
                                        CampaignMain.cm
                                                .getOpsManager()
                                                .getOperation(Opname)
                                                .getIntValue("MaxBVDifference"));
                        MaxPercentDiff +=
                                Math.max(
                                        0,
                                        CampaignMain.cm
                                                .getOpsManager()
                                                .getOperation(Opname)
                                                .getIntValue("MaxBVPercent"));
                        legalOps++;
                    }
                }

                // Army does is not used in a PP legal op.
                if (!fLegalOp) {
                    continue;
                }

                /*
                 * Sort the armies into BV order, least to greatest. Take an
                 * enumeration of all armies. 1st is added to orderedArmies by
                 * default. Additional armies are compared to previously sorted
                 * BVs and inserted in front of the first element which has a
                 * higher value. If currentForce is larger than previously
                 * sorted armies it is appended to end of the vector
                 */

                // if empty, add the first force by default
                if (orderedArmies.size() == 0) {
                    orderedArmies.add(currentArmy);
                } else { // size > 0
                    Iterator<SArmy> f = orderedArmies.iterator();
                    int forceNumber = 0; // number of current army
                    boolean forceSorted = false;

                    while (f.hasNext() && !forceSorted) {
                        if (currentArmy.getOperationsBV(null) < (f.next()).getOperationsBV(null)) {
                            orderedArmies.add(forceNumber, currentArmy);
                            forceSorted = true;
                        } else {
                            forceNumber++;
                        }
                    } // end while(more elements to compare to)

                    if (!forceSorted) {
                        orderedArmies.add(currentArmy);
                    }
                } // end else (not first)
            } // end for(each army)

            /*
             * Determine overlap of lances, now that they have been ordered.
             * Reduce payout modifier if forces cover similar value ranges. Only
             * do this if there are actually ordered armies!
             */
            if (legalOps != 0) {
                MaxFlatDiff /= legalOps;
                MaxPercentDiff /= legalOps;
            }
            weightedArmyNumber = orderedArmies.size();

            double weightMod = Math.max(0, getMyHouse().getDoubleConfig("BaseCountForProduction"));
            weightedArmyNumber *= weightMod;

            if (weightedArmyNumber > 0) {
                Iterator<SArmy> e = orderedArmies.iterator();
                SArmy currentArmy = e.next(); // get first army
                int currentBV = currentArmy.getOperationsBV(null);

                // holder for whichever is greater - flat diff or percent
                double currentMaxDiff = 0;

                /*
                 * compare first force to floor. get first army, determine
                 * percent and flat difference, then test against the BV-edge.
                 */
                double caPercentDiff = currentBV * MaxPercentDiff;
                if (MaxFlatDiff >= caPercentDiff) {
                    currentMaxDiff = MaxFlatDiff;
                } else {
                    currentMaxDiff = caPercentDiff;
                }

                if (currentBV - MinCount < currentMaxDiff) {
                    weightedArmyNumber -= getMyHouse().getDoubleConfig("FloorPenalty");
                    int overlap = currentBV - MinCount;
                    weightedArmyNumber -= (currentMaxDiff - overlap) / currentMaxDiff;
                }

                /*
                 * compare intermediate forces to each other...
                 */
                SArmy nextArmy = null; // for use in loop
                int nextBV = 0; // for use in loop
                while (e.hasNext()) { // loop through remaining forces
                    // get the next army, and its BV
                    nextArmy = e.next();
                    nextBV = nextArmy.getOperationsBV(null);

                    /*
                     * test whether flat or percent BV difference is larger for
                     * these two armies. compare based on larger window.
                     */
                    if (MaxPercentDiff <= 0) {
                        currentMaxDiff = MaxFlatDiff;
                    } else {
                        if (currentBV > nextBV) {
                            caPercentDiff = currentBV * MaxPercentDiff;
                        } else {
                            caPercentDiff = nextBV * MaxPercentDiff;
                        }

                        if (MaxFlatDiff >= caPercentDiff) {
                            currentMaxDiff = MaxFlatDiff;
                        } else {
                            currentMaxDiff = caPercentDiff;
                        }
                    }

                    if (nextBV - currentBV < currentMaxDiff) {
                        weightedArmyNumber -= getMyHouse().getDoubleConfig("OverlapPenalty");
                        int overlap = nextBV - currentBV;
                        weightedArmyNumber -= (currentMaxDiff - overlap) / currentMaxDiff;
                    }
                    currentArmy = nextArmy; // set up for the next iteration
                    currentBV = nextBV; // set up for the next iteration
                } // end while(more elements)

                /*
                 * compare last force to ceiling
                 */
                caPercentDiff = currentBV * MaxPercentDiff;
                if (MaxFlatDiff >= caPercentDiff) {
                    currentMaxDiff = MaxFlatDiff;
                } else {
                    currentMaxDiff = caPercentDiff;
                }
                if (MaxCount - currentBV < currentMaxDiff) {
                    weightedArmyNumber -= getMyHouse().getDoubleConfig("CeilingPenalty");
                    int overlap = MaxCount - currentBV;
                    weightedArmyNumber -= (currentMaxDiff - overlap) / currentMaxDiff;
                }

                /*
                 * Remove armies which cannot attack from the weighting AFTER
                 * overlap checks in order to discourage any abusive stacking.
                 */
                for (SArmy currA : orderedArmies) {
                    if (currA.getLegalOperations().size() <= 0) {
                        weightedArmyNumber -= weightMod;
                    }
                }

                // make sure at least 1 is returned, in case penalties create <1
                // cases.
                if (weightedArmyNumber < 0) {
                    weightedArmyNumber = weightMod;
                }
            } // end if(armies were ordered)
        } // end if (weighted <= 0)
        LOGGER.debug("End getWeightedArmyNumber for " + getName());
        return weightedArmyNumber;
    }

    /**
     * A method which resets the weightedArmyNumber to -1, forcing a recalculation next time the
     * above method (getWeightedArmyNumber) is called. Should be triggered by anything which changes
     * army BV or army numbers - game resolution and EXM, etc.
     *
     * @urgru 11/12/04
     */
    public void resetWeightedArmyNumber() {
        weightedArmyNumber = -1;
    }

    public void reset(String confirm) {
        if (!confirm.equals("CONFIRM")) {
            return;
        }

        armies.clear();
        clearUnits();
        setMoney(0);
        exclusionList.getAdminExcludes().clear();
        exclusionList.getPlayerExcludes().clear();
        setExperience(0);
        baysOwned = 0;
        availableTechs = new int[UnitUtils.TECH_TYPES];
        totalTechs = new int[UnitUtils.TECH_TYPES];
        setTechnicians(0);
        fluffText = " ";
        setRewardPoints(0);
        groupAllowance = 0;
        setInfluence(0);
        setMyHouse(CampaignData.cd.getHouseByName(getMyHouse().getConfig("NewbieHouseName")));
        setLogo(" ");
        personalPilotQueue.flushQueue();
        xpTillReward = 0;
        xpTillFlu = 0;
        setMekTokens(0);
        sellingto = " ";
        weightedArmyNumber = 0;
        setSave();
    }

    /**
     * Add money to a player. Money is always modified relative to a previous amount
     * (this.fromString is an expetion, but sets the value directly), so there is no need for a
     * public SPlayer.setMoney() method.
     */
    public void addMoney(int i) {
        // holder, amount to store.
        int moneyToSet = getMoney() + i;

        // don't let SOL exceed cap, or anyone have negative cash
        int maxNewbieCbills = getMyHouse().getIntegerConfig("MaxSOLCBills");
        if (getMyHouse().isNewbieHouse() && (moneyToSet > maxNewbieCbills)) {
            moneyToSet = maxNewbieCbills;
        }
        if (moneyToSet < 0) {
            moneyToSet = 0;
        }

        // change the value and send an update
        setMoney(moneyToSet);
        CampaignMain.cm.toUser("PL|SM|" + getMoney(), getName(), false);
        setSave();
    }

    /**
     * @ Salient for free build, mek tokens iterate up to the server limit. Updates CPlayer.
     */
    public void addMekTokens(int i) {
        int tokenToSet = this.getMekTokens() + i;
        this.setMekTokens(tokenToSet);
        CampaignMain.cm.toUser(
                "PL|UMT|" + tokenToSet, getName(), false); // UMT: Update Mek Token on cplayer
        setSave();
    }

    public void setPassword(MWPasswdRecord pass) {
        if (pass == null) {
            try {
                throw new Exception();
            } catch (Exception ex) {
                LOGGER.error("Exception: ", ex);
            }
        }
        password = pass;
        setSave();
    }

    public MWPasswdRecord getPassword() {
        return password;
    }

    /**
     * Method required for ISeller compliance. Used to distinguish between human controlled actors
     * (this class) and factions/automated actors (SHouse).
     */
    public boolean isHuman() {
        return true;
    }

    /**
     * Method which determines which house a player is actually fighting for. Used to display
     * contracting house, instead of real faction, for mercenaries.
     */
    public SHouse getHouseFightingFor() {
        return getMyHouse().getHouseFightingFor(this);
    }

    /**
     * Set the player's faction. Should only be used by Defect, ForcedDefect and Enroll commands.
     */
    public void setMyHouse(SHouse h) {
        super.setMyHouse(h);
        setSave();
    }

    /**
     * A Method to get the current duty status of a player. Options are, from lowest to highest,
     * STATUS_LOGGEDOUT, STATUS_RESERVE, STATUS_ACTIVE, and STATUS_FIGHTING. This method returns the
     * persisted status value.
     */
    public int getDutyStatus() {
        return status;
    }

    /**
     * Determines the weighted number of votes a player can cast. Draws a flat config out of
     * campaignconfig.txt to use as a base number. Additonal votes may be assigned as a player gains
     * XP, up to a configurable ceiling. Used by the various vote cmds to block overvoting, etc.
     *
     * @return int representing total # of votes player is allowed to cast.
     */
    public int getNumberOfVotesAllowed() {

        int voteTotal = Integer.parseInt(getMyHouse().getConfig("StartingVotes"));
        int xpForVote = Integer.parseInt(getMyHouse().getConfig("XPForAdditionalVote"));
        int maxVotes = Integer.parseInt(getMyHouse().getConfig("MaximumVotes"));

        voteTotal += (int) Math.floor(getExperience() / xpForVote);
        if (voteTotal > maxVotes) {
            voteTotal = maxVotes;
        }

        return voteTotal;
    }

    /**
     * Strip the player's units. They disappear forever and are NOT given to the player's house.
     *
     * @param sendStatus - boolean. if true, send the player's status downstream. should usually be
     *     true. false when called from NewbieHouse, which send status on its own after granting new
     *     units.
     */
    public void stripOfAllUnits(boolean sendStatus) {
        clearUnits();
        armies.clear();

        if (sendStatus) {
            CampaignMain.cm.toUser("PS|" + this.toString(true), getName(), false);
        }

        setSave();
        CampaignMain.cm.toUser("PL|SHP|" + buildHangarPenaltyString(), getName(), false);
    }

    // EXPERIENCE SET/ADD/GET Methods
    /**
     * Add experience to the player. Boolean param is used to prevent RP gain from mod/admin XP
     * additions.
     *
     * @param i - amount of RP to add
     * @param modAdded - true if added from a mod/admin command
     */
    public void addExperience(int i, boolean modAdded) {
        // change xp
        setExperience(getExperience() + i);

        // check floor
        if (getExperience() < 0) {
            setExperience(0);
        }

        // check SOL cap
        if (getMyHouse().isNewbieHouse()
                && (getExperience() > getMyHouse().getIntegerConfig("MaxSOLExp"))) {
            setExperience(getMyHouse().getIntegerConfig("MaxSOLExp"));
        }

        // update client & all userlists
        CampaignMain.cm.toUser("PL|SE|" + getExperience(), getName(), false);
        CampaignMain.cm.doSendToAllOnlinePlayers(
                "PI|EX|" + getName() + "|" + getExperience(), false);

        // update corresponding small player.
        SmallPlayer smallp = getMyHouse().getSmallPlayers().get(getName().toLowerCase());
        if (smallp != null) {
            smallp.setExperience(getExperience());
        }

        // check and send mek bay numbers
        CampaignMain.cm.toUser("PL|SB|" + getTotalMekBays(), getName(), false);
        CampaignMain.cm.toUser("PL|SF|" + getFreeBays(), getName(), false);

        // check reward, if not mod added. never reduce rollover counter.
        if (!modAdded && (i > 0)) {
            int currentXP = xpTillReward + i;
            int rollOver = getMyHouse().getIntegerConfig("XPRollOverCap");

            // if XP is over rollover point, reduce until below again
            if ((currentXP >= rollOver) && (rollOver > 0)) {
                int rpToAdd = 0;

                while (currentXP >= rollOver) {
                    currentXP -= rollOver;
                    rpToAdd++;
                }

                addRewardPoints(rpToAdd);

                // reset the counter
                setXpTillReward(currentXP);

                // set up and send upe rp link
                String toSend =
                        "You earned "
                                + rpToAdd
                                + " experience "
                                + CampaignMain.cm.getConfig("RPShortName");
                toSend +=
                        "[<a href=\"MWUSERP\">Use "
                                + CampaignMain.cm.getConfig("RPShortName")
                                + "</a>]";
                CampaignMain.cm.toUser(toSend, getName(), true);
            } else {
                setXpTillReward(currentXP);
            }
        }

        // @salient
        if (!modAdded && (i > 0)) {
            int currentXP = xpTillFlu + i;
            int rollOver = getMyHouse().getIntegerConfig("FluXPRollOverCap");

            // if XP is over rollover point, reduce until below again
            if ((currentXP >= rollOver) && (rollOver > 0)) {
                int fluToAdd = 0;

                while (currentXP >= rollOver) {
                    currentXP -= rollOver;
                    fluToAdd++;
                }

                addInfluence(fluToAdd);

                // reset the counter
                setXpTillFlu(currentXP);

                String toSend =
                        "You earned "
                                + fluToAdd
                                + CampaignMain.cm.getConfig("FluShortName")
                                + " by gaining xp!";
                CampaignMain.cm.toUser(toSend, getName(), true);

            } else {
                setXpTillFlu(currentXP);
            }
        }
        setSave();
    }

    // SPECIAL USE METHODS (PRIVATE OR PUBLIC&STATIC)
    /**
     * Determine the total BV of all units owned by the player. This is used by the welfare checks
     * to see whether a players units can form an army of sufficient BV. Note that for_sale units
     * are included in the BV total, because skipping them would allow players to list a unit, get
     * welfare units, and then delist the sales unit in order. Freebies is something we want to
     * avoid, because people are evil and cheat.
     *
     * @author Jason Tighe.
     * @return the total bv of the player's units.
     */
    public int getHangarBV() {
        int bv = 0;
        for (SUnit currU : getUnits()) {
            bv += currU.getBVForMatch();
        }
        return bv;
    }

    // @salient - do the same as above but also some other BV calcs.
    public int getHangarBVforMC() {
        int bv = 0;
        boolean removeLockedBV = getMyHouse().getBooleanConfig("LockedUnits_RemoveBV");
        boolean ignoreAeroBV = getMyHouse().getBooleanConfig("IgnoreAeroBV");

        for (SUnit currU : getUnits()) {
            if (removeLockedBV) { // do not add BV of units that are locked.
                if (currU.isLocked() == false) { // if unit is locked, ignore it
                    if (ignoreAeroBV && currU.getType() == 5) // ignore aero units
                    continue;
                    else bv += currU.getBVForMatch();
                }
            } else { // add up all unit bv
                if (ignoreAeroBV && currU.getType() == 5) // ignore aero units
                continue;
                else bv += currU.getBVForMatch();
            }
        }
        return bv;
    }

    /**
     * Simple private method which returns the next available free position ID (hangar location).
     * While this seems pointless, and probably is, the hangar ID is used by the client for all
     * kinds of things and we're stuck with it until someone takes the time to weed it out
     * completely.
     */
    private int getFreeID() {
        int id = 0;
        boolean found = false;
        while (!found) {
            found = true;
            for (int i = 0; i < getUnits().size(); i++) {
                if (getUnits().get(i).getPosId() == id) {
                    found = false;
                    id++;
                }
            }
        }
        return id;
    }

    public int getFreeArmyId() {
        int i = 0;
        boolean free = false;
        while (!free) {
            free = true;
            for (int j = 0; j < getArmies().size(); j++) {
                if (getArmies().get(j).getId() == i) {
                    free = false;
                    i++;
                }
            }
        }

        return i;
    }

    // METHODS TO CHECK/COMMENT
    /**
     * @author Jason Tighe aka Torren
     * @return if the player is eligible for welfare light meks from faction bays. due to lack of
     *     mechs in bay and they are all light
     */
    public boolean mayAcquireWelfareUnits() {

        if ((getHangarBV() < getMyHouse().getIntegerConfig("WelfareTotalUnitBVCeiling"))
                && (getMoney() < getMyHouse().getIntegerConfig("WelfareCeiling"))) {
            return true;
        }

        // else
        return false;
    }

    // MINI CAMPAIGN CODE
    /**
     * @author Salient
     * @return if enabled, this method will initiate the Restock Phase (currency injection) if
     *     hangar is below a certain threshold. This occurs AFTER a match.
     */
    public void checkHangarRestockMC() {
        boolean enabledMC = getMyHouse().getBooleanConfig("Enable_MiniCampaign");
        boolean lockUnits = getMyHouse().getBooleanConfig("LockUnits");

        // adding this in before method exit, since i want to be able to allow
        // unit locking while mini campaign is disabled yes locked units is not
        int lockedLimit = getMyHouse().getIntegerConfig("UnlockUnits_Percentage");
        if (!enabledMC && lockUnits && lockedLimit != -1) {
            if (percentLockedUnitsMC() >= lockedLimit) {
                unlockAllUnitsMC();
                setSave();
            }
        }

        if (!enabledMC) {
            toSelf("AM: Mini Campaigns are disabled on the server!");
            return;
        }

        // debug
        LOGGER.error(getName() + "'s BV: " + getHangarBVforMC());

        // set states and cache configs
        boolean restock = false;
        boolean minBVRestock = false;
        boolean percentRestock = false;
        boolean unitRestock = false;

        // tempted to make these global variables...
        int minBVLimit = getMyHouse().getIntegerConfig("MinBV_HangarRestock");
        int percentBVLimit = getMyHouse().getIntegerConfig("Percent_HangarRestock");
        int minUnitLimit = getMinUnitResetMC();

        int restockCB = getMyHouse().getIntegerConfig("RestockCB_Injection");
        int restockRP = getMyHouse().getIntegerConfig("RestockRP_Injection");
        int restockFLU = getMyHouse().getIntegerConfig("RestockFLU_Injection");
        int restockMT = getMyHouse().getIntegerConfig("RestockMT_Injection");

        // check if we should restock
        if (minBVLimit != -1 && getHangarBVforMC() < minBVLimit) {
            restock = true;
            minBVRestock = true;

            LOGGER.info("{} has gone under BV limit and a restock should occur", getName());
        }

        if (percentBVLimit != -1 && getHangarBVforMC() < getBVResetPointMC()) {
            restock = true;
            percentRestock = true;
            setBVTracker(
                    0); // return this to default zero. on activation, it will be set to new value.

            LOGGER.info("{} has gone under % BV limit and a restock should occur", getName());
        }

        if (minUnitLimit != -1 && getUnitCountMC() < minUnitLimit) {
            restock = true;
            unitRestock = true;

            LOGGER.info(getName() + " has gone under Unit limit and a restock should occur");
        }

        if (!restock && !minBVRestock && minBVLimit != -1) {
            toSelf(
                    "AM: Your hangar is at "
                            + getHangarBVforMC()
                            + "BV. When you drop below "
                            + minBVLimit
                            + "BV your mini campaign will restart");
        }

        if (!restock && !percentRestock && percentBVLimit != -1) {
            toSelf(
                    "AM: Your hangar is at "
                            + getHangarBVforMC()
                            + "BV. When you drop below "
                            + getBVResetPointMC()
                            + "BV your mini campaign will restart");
        }

        if (!restock && !unitRestock && minUnitLimit != -1) {
            toSelf(
                    "AM: Your hangar is at "
                            + getUnitCountMC()
                            + "Units. When you drop below "
                            + minUnitLimit
                            + "Units your mini campaign will restart");
        }

        // if too many of the players units are locked to continue, unlock all units
        if (!restock && lockUnits && lockedLimit != -1) { // do only if feature enabled
            if (percentLockedUnitsMC() >= lockedLimit) {
                unlockAllUnitsMC(); // sets save now
                // setSave();
            }
        }

        if (!restock) {
            setSave(); // needed since shortresolver handles unit locking
            // though i have to imagine it also saves in shortresolver somewhere...
            return;
        }

        if (restock) { // the way it's set up, may not need to clear currency since it should be
            // clear already.
            if (restockRP != -1) {
                addRewardPoints(-getRewardPoints()); // clear before reset
                addRewardPoints(restockRP);
                toSelf(
                        "AM: You have received "
                                + getRewardPoints()
                                + " "
                                + CampaignMain.cm.getConfig("RPLongName")
                                + ". Restock your forces before continuing.");
            }

            if (restockFLU != -1) {
                addInfluence(-getInfluence()); // clear before reset
                addInfluence(restockFLU);
                toSelf(
                        "AM: You have received "
                                + getInfluence()
                                + " "
                                + CampaignMain.cm.getConfig("FluLongName")
                                + ". Restock your forces before continuing.");
            }

            if (restockMT != -1) {
                addMekTokens(-getMekTokens()); // clear
                addMekTokens(getMekTokenLimit()); // have to go to limit to clear to 0, counts up
                addMekTokens(-restockMT); // subtract since it counts up
                toSelf(
                        "AM: You have received "
                                + getRemainingMekTokens()
                                + " free mek tokens. Restock your forces before continuing.");
            }

            if (restockCB != -1) {
                addMoney(-getMoney()); // clear
                addMoney(restockCB);
                toSelf(
                        "AM: You have received "
                                + getMoney()
                                + " "
                                + CampaignMain.cm.getConfig("MoneyLongName")
                                + ". Restock your forces before continuing.");
            }

            setPhaseRestockMC();
            unlockAllUnitsMC(); // sets save
            addRewardsMC(); // adds currencies not involved with injection/restocking
        }

        return;
    }

    /**
     * @author Salient
     * @return checks if a player can go active for the next cycle in his/her mini campaign
     */
    public boolean canActivateForMiniCampaign() {
        if (!getMyHouse().getBooleanConfig("Enable_MiniCampaign")) {
            toSelf("AM: Mini Campaigns are disabled on the server!");
            return false;
        }

        int minBVLimit = getMyHouse().getIntegerConfig("MinBV_HangarRestock");
        int percentBVLimit = getMyHouse().getIntegerConfig("Percent_HangarRestock");
        int minUnitLimit = getMinUnitResetMC();

        int restockCB = getMyHouse().getIntegerConfig("RestockCB_Injection");
        int restockRP = getMyHouse().getIntegerConfig("RestockRP_Injection");
        int restockFLU = getMyHouse().getIntegerConfig("RestockFLU_Injection");
        int restockMT = getMyHouse().getIntegerConfig("RestockMT_Injection");

        float percentCB = getMyHouse().getIntegerConfig("RestockCB_LeewayPercentage") / 100.0f;
        float percentRP = getMyHouse().getIntegerConfig("RestockRP_LeewayPercentage") / 100.0f;
        float percentFLU = getMyHouse().getIntegerConfig("RestockFLU_LeewayPercentage") / 100.0f;
        float percentMT = getMyHouse().getIntegerConfig("RestockMT_LeewayPercentage") / 100.0f;

        int leewayCB = (int) (restockCB * percentCB);
        int leewayRP = (int) (restockRP * percentRP);
        int leewayFLU = (int) (restockFLU * percentFLU);
        int leewayMT = (int) (restockMT * percentMT);

        boolean requireUnitsAtLimit = getMyHouse().getBooleanConfig("AtUnitLimitsMC");
        boolean requireUnitsAtOrOverLimit = getMyHouse().getBooleanConfig("AtOrOverUnitLimitsMC");

        // boolean canActivate = true;

        // check if hangar BV has increased (maybe via salvage? or trades?), if so update to new
        // value.
        if (percentBVLimit != -1 && getHangarBVforMC() > getBVTracker()) {
            setBVTracker(getHangarBVforMC());
            LOGGER.info(getName() + "'s BV reset point set to " + getBVResetPointMC() + " BV");
        }

        if (isPhaseRestockMC()) {
            if (minBVLimit != -1 && getHangarBVforMC() < minBVLimit) {
                toSelf(
                        "AM: To go active you must raise your hangar BV! You have "
                                + getHangarBVforMC()
                                + " and need at least "
                                + minBVLimit
                                + " to go active!");
                return false;
            }

            if (minUnitLimit != -1 && getUnitCountMC() < minUnitLimit) {
                toSelf(
                        "AM: To go active you must raise your hangar Unit Count! You have "
                                + getUnitCountMC()
                                + " and need at least "
                                + minUnitLimit
                                + " to go active!");
                return false;
            }

            if (restockCB != -1 && leewayCB > 0f && getMoney() > leewayCB) {
                toSelf(
                        "AM: You have too many "
                                + CampaignMain.cm.getCurrencyName("money", false)
                                + " to go active!");
                return false;
            }

            if (restockRP != -1 && leewayRP > 0f && getRewardPoints() > leewayRP) {
                toSelf(
                        "AM: You have too many "
                                + CampaignMain.cm.getCurrencyName("rp", false)
                                + " to go active!");
                return false;
            }

            if (restockFLU != -1 && leewayFLU > 0f && getInfluence() > leewayFLU) {
                toSelf(
                        "AM: You have too much "
                                + CampaignMain.cm.getCurrencyName("flu", false)
                                + " to go active!");
                return false;
            }

            if (restockMT != -1 && leewayMT > 0f && getRemainingMekTokens() > leewayMT) {
                toSelf("AM: You must use up more of your free meks to go active!");
                return false;
            }

            if (requireUnitsAtOrOverLimit
                    && isPhaseRestockMC()
                    && isAtOrOverUnitLimits() == false) {
                toSelf(
                        "AM: You must reach or exceed the limit for each unit type/weight before "
                                + "restarting your mini campaign!");
                return false;
            } else if (requireUnitsAtLimit && isPhaseRestockMC() && isAtUnitLimits() == false) {
                toSelf(
                        "AM: You must reach the limit for each unit type/weight before restarting"
                                + " your mini campaign!");
                return false;
            }

            // At this point we assume that the player can activate and leave restock state.
            removeInjectedCurrencyMC(restockCB, restockRP, restockFLU, restockMT);

            if (percentBVLimit != -1) {
                setBVTracker(getHangarBVforMC()); // set new hangar BV for tracking
                LOGGER.info(getName() + "'s BV reset point set to " + getBVResetPointMC() + " BV");
            }
            setPhaseActiveMC();
            setSave();
        }

        reportStatusMC();
        return true;
    }

    // @salient will be used here and in a command.
    public void reportStatusMC() {
        int minBVLimit = getMyHouse().getIntegerConfig("MinBV_HangarRestock");
        int percentBVLimit = getMyHouse().getIntegerConfig("Percent_HangarRestock");
        int minUnitLimit = getMinUnitResetMC();

        if (percentBVLimit != -1) {
            toSelf("AM: Current Hangar BV: " + getHangarBVforMC());
            toSelf(
                    "AM: Next mini campaign cycle will begin when your hangar BV falls below "
                            + getBVResetPointMC());
        }

        if (minUnitLimit != -1) {
            toSelf("AM: Current Unit Count: " + getUnitCountMC());
            toSelf(
                    "AM: Next mini campaign cycle will if your Unit Count falls below "
                            + minUnitLimit);
        }

        if (minBVLimit != -1) {
            toSelf("AM: Current Hangar BV: " + getHangarBVforMC());
            toSelf(
                    "AM: Next mini campaign cycle will begin when your hangar BV falls below "
                            + minBVLimit);
        }
    }

    // -- MC DATA SAVE/LOAD --
    private String saveStatusMC() {
        SerializedMessage result = new SerializedMessage("&");
        result.append(phaseMC);
        return result.toString();
    }

    private void loadStatusMC(String data) {
        StringTokenizer st = new StringTokenizer(data, "&");
        if (st.hasMoreTokens()) phaseMC = TokenReader.readString(st);
        else LOGGER.error("loadStatusMC failed! no token available for phaseMC");
    }

    private boolean isPhaseRestockMC() {
        if (phaseMC.equalsIgnoreCase(RESTOCK_MC)) return true;
        else return false;
    }

    private void setPhaseActiveMC() {
        phaseMC = ACTIVE_MC;
    }

    private void setPhaseRestockMC() {
        phaseMC = RESTOCK_MC;
    }

    // @salient - made a new command called RG (refresh gui) not really sure it works tbh..
    public void refreshGUI() {
        CampaignMain.cm.toUser("RG|" + " ", getName(), false);
    }

    // @salient
    public void unlockAllUnitsMC() {
        if (!getMyHouse().getBooleanConfig("LockUnits")) {
            return;
        }

        for (SUnit aUnit : getUnits()) {
            aUnit.setLocked(false);
        }

        CampaignMain.cm.toUser("PS|" + this.toString(true), getName(), false);
        setSave();
        CampaignMain.cm.toUser("PL|SHP|" + buildHangarPenaltyString(), getName(), false);

        // refreshGUI();
        toSelf("AM: Units have been unlocked!");
    }

    // doesnt work, dunno why... might work, just didn't work in shortresolver?
    //    //@salient
    //    public void removeLockedUnitsFromArmiesMC()
    //    {
    //      if(!getMyHouse().getBooleanConfig("LockUnits"))
    //          return;
    //
    //      getLockedArmy();
    //        for (SArmy army : getArmies())
    //        {
    //          for (Unit aUnit : army.getUnits())
    //          {
    //              if(aUnit.isLocked())
    //                  army.removeUnit(aUnit.getId());
    //          }
    //        }
    //
    //      refreshGUI();
    //      toSelf("AM: Locked Units Removed From Army!");
    //    }

    /**
     * @author Salient adds rewards to player at end of mini campaign cycle
     */
    private void addRewardsMC() {
        int rewardBays = this.getMyHouse().getIntegerConfig("MC_Reward_BAYS");
        int rewardTechs = this.getMyHouse().getIntegerConfig("MC_Reward_TECHS");
        int rewardXP = this.getMyHouse().getIntegerConfig("MC_Reward_XP");
        int rewardRP = this.getMyHouse().getIntegerConfig("MC_Reward_RP");
        int rewardFLU = this.getMyHouse().getIntegerConfig("MC_Reward_FLU");
        int rewardCB = this.getMyHouse().getIntegerConfig("MC_Reward_CB");
        int rewardMT = this.getMyHouse().getIntegerConfig("MC_Reward_MT");

        this.addBays(rewardBays);
        this.addTechnicians(rewardTechs);
        this.addExperience(rewardXP, false);
        this.addRewardPoints(rewardRP);
        this.addInfluence(rewardFLU);
        this.addMoney(rewardCB);
        this.addMekTokens(-rewardMT); // counts up to limit
    }

    // @salient - returns the percent of players units that are locked.
    public int percentLockedUnitsMC() {
        int numLocked = 0;

        for (SUnit aUnit : getUnits()) {
            if (aUnit.isLocked()) numLocked++;
        }

        float result = getUnitCount() / numLocked;

        return (int) (result * 100);
    }

    // @salient
    private void removeInjectedCurrencyMC(
            int restockCB, int restockRP, int restockFLU, int restockMT) {
        if (hasMoney() && restockCB != -1) {
            addMoney(-getMoney());
        }

        if (hasRP() && restockRP != -1) {
            addRewardPoints(-getRewardPoints());
        }

        if (hasFlu() && restockFLU != -1) {
            addInfluence(-getInfluence());
        }

        if (hasMT() && restockMT != -1) {
            addMekTokens(-getMekTokens()); // clear
            addMekTokens(getMekTokenLimit()); // have to go to limit to clear to 0, counts up
        }
    }

    // @salient - using a percentage set by SO, this returns the BV at which point the mini campaign
    // will end
    private int getBVResetPointMC() {
        float percent = getMyHouse().getIntegerConfig("Percent_HangarRestock") / 100.0f;
        int resetPt = (int) (getBVTracker() * percent);
        return resetPt;
    }

    // @salient - using a value set by SO, this returns the Unit count at which point the mini
    // campaign will end
    private int getMinUnitResetMC() {
        int resetPt = getMyHouse().getIntegerConfig("Unit_HangarRestock");
        return resetPt;
    }

    // -- DISCORD BOT DATA SAVE/LOAD --
    private String saveDiscordInfo() {
        SerializedMessage result = new SerializedMessage("&");
        result.append(discordID);
        return result.toString();
    }

    private void loadDiscordInfo(String data) {
        StringTokenizer st = new StringTokenizer(data, "&");
        if (st.hasMoreTokens()) {
            discordID = TokenReader.readString(st);
        } else LOGGER.debug("loadDiscordInfo failed! no token available!");
    }

    // @salient
    public void removeCurrency() {
        addMoney(-getMoney());
        addInfluence(-getInfluence());
        addRewardPoints(-getRewardPoints());

        addMekTokens(-getMekTokens()); // clear
        addMekTokens(getMekTokenLimit()); // have to go to limit to clear to 0, counts up
    }

    // @salient
    public boolean hasCurrency() {
        if (getMoney() != 0
                || getInfluence() != 0
                || getRewardPoints() != 0
                || getRemainingMekTokens() != 0) return true;
        else return false;
    }

    // @salient
    public boolean hasMoney() {
        if (getMoney() != 0) return true;
        else return false;
    }

    // @salient
    public boolean hasFlu() {
        if (getInfluence() != 0) return true;
        else return false;
    }

    // @salient
    public boolean hasRP() {
        if (getRewardPoints() != 0) return true;
        else return false;
    }

    // @salient
    public boolean hasMT() {
        if (getRemainingMekTokens() != 0) return true;
        else return false;
    }

    // @salient
    public int getRemainingMekTokens() {
        int limit = getMyHouse().getIntegerConfig("FreeBuild_Limit");

        return limit - getMekTokens(); // mek tokens count up to limit
    }

    // @salient
    public int getMekTokenLimit() {
        int limit = Integer.parseInt(getMyHouse().getConfig("FreeBuild_Limit"));

        return limit; // mek tokens count up to limit
    }

    /**
     * @return current post-game payment to technicians, in Cbills
     */
    @Override
    public int getCurrentTechPayment() {
        // recalculate if -1
        if (super.getCurrentTechPayment() < 0) {
            doPayTechniciansMath();
        }

        return super.getCurrentTechPayment();
    }

    @Override
    public void setCurrentTechPayment(int techPayment) {
        super.setCurrentTechPayment(techPayment);
        setSave();
    }

    /**
     * @return the number of technicians the player has
     */
    @Override
    public int getTechnicians() {
        if (CampaignMain.cm.isUsingAdvanceRepair()) {
            return getBaysOwned();
        }
        return super.getTechnicians();
    }

    public String totalTechsToString() {
        StringBuilder result = new StringBuilder();

        for (Integer tech : getTotalTechs()) {
            result.append(tech + "%");
        }

        return result.toString();
    }

    public String availableTechsToString() {
        StringBuilder result = new StringBuilder();

        for (Integer tech : getAvailableTechs()) {
            result.append(tech + "%");
        }

        return result.toString();
    }

    public void addAvailableTechs(int type, int number) {
        setAvailableTechs(type, getTotalTech(type) + number);
    }

    public void setAvailableTechs(int type, int number) {
        if (type < 0 || type >= UnitUtils.TECH_TYPES) {
            return;
        }

        synchronized (availableTechs) {
            availableTechs[type] = number;
        }

        CampaignMain.cm.toUser("PL|UAT|" + availableTechsToString(), getName(), false);
    }

    public void addTotalTechs(int type, int number) {
        setTotalTechs(type, getTotalTech(type) + number);
    }

    public void setTotalTechs(int type, int number) {
        if (type < 0 || type >= UnitUtils.TECH_TYPES) {
            return;
        }

        synchronized (totalTechs) {
            totalTechs[type] = number;
        }
        CampaignMain.cm.toUser("PL|UTT|" + totalTechsToString(), getName(), false);
    }

    public void updateAvailableTechs(String data) {
        try {
            StringTokenizer techs = new StringTokenizer(data, "%");
            int techType = UnitUtils.TECH_GREEN;
            while (techs.hasMoreTokens()) {
                setAvailableTechs(techType, Integer.parseInt(techs.nextToken()));
                techType++;
            }
        } catch (Exception ex) {
            LOGGER.error("Unable to update available techs", ex);
        }
    }

    public void updateTotalTechs(String data) {
        try {
            StringTokenizer techs = new StringTokenizer(data, "%");
            int techType = UnitUtils.TECH_GREEN;

            while (techs.hasMoreTokens()) {
                setTotalTechs(techType, TokenReader.readInt(techs));
                techType++;
            }
        } catch (Exception ex) {
            LOGGER.error("Unable to update total techs", ex);
        }
    }

    public int getBaysOwned() {
        return baysOwned;
    }

    public void setBaysOwned(int bays) {
        int maxBays = 0;

        if (getMyHouse() != null) {
            maxBays = Integer.parseInt(getMyHouse().getConfig("MaxBaysToBuy"));
        } else {
            maxBays = CampaignMain.cm.getIntegerConfig("MaxBaysToBuy");
        }

        if (maxBays != -1) {
            baysOwned = Math.min(maxBays, bays);
        } else {
            baysOwned = bays;
        }
    }

    public void addBays(int bays) {
        setBaysOwned(baysOwned + bays);
    }

    public String getLastISP() {
        return lastISP;
    }

    public void setLastISP(String isp) {
        lastISP = isp;
    }

    /**
     * @param t - int to set technicians to.
     */
    @Override
    public void setTechnicians(int t) {
        super.setTechnicians(t);
        CampaignMain.cm.toUser("PL|ST|" + t, getName(), false);
        CampaignMain.cm.toUser("PL|SB|" + getTotalMekBays(), getName(), false);
        CampaignMain.cm.toUser("PL|SF|" + getFreeBays(), getName(), false);
        setSave();
    }

    /**
     * @param t - the number of technicians to add (subtract) from the player's total sub-zero cases
     *     are checked in setTechs(). no check here.
     */
    @Override
    public void addTechnicians(int t) {
        if (CampaignMain.cm.isUsingAdvanceRepair()) {
            addBays(t);
        } else {
            setTechnicians(getTechnicians() + t);
        }
    }

    public String getColoredName() {
        return "<font color=\""
                + getHouseFightingFor().getHouseColor()
                + "\">"
                + getName()
                + "</font>";
    }

    public String getColoredNameBold() { // @salient
        return "<font color=\""
                + getHouseFightingFor().getHouseColor()
                + "\"><b>"
                + getName()
                + "</b></font>";
    }

    public void setName(String name) {
        super.setName(name);
        setSave();
    }

    // @salient
    public String getDiscordID() {
        return discordID;
    }

    // @salient
    public void setDiscordID(String _discordID) {
        if (_discordID == null || _discordID == "") {
            toSelf("AM:You must enter a discord ID to set!");
            return;
        }
        discordID = _discordID;
        setSave();
    }

    // @salient- compare client quirks with server
    // lol while this works, realized the way i'm doing things
    // makes this check meaningless... what needs to be checked is the hosts xmls, not the client
    // quirks
    // which are already set by the server anyway....
    //    public boolean checkAllQuirkInfoForActivation(String data)
    //    {
    //      StringTokenizer st = new StringTokenizer(data,"*");
    //      int debugCounter = 0;
    //
    //        while(st.hasMoreTokens())
    //        {
    //          SUnit currU = this.getUnit(TokenReader.readInt(st));
    //          String quirks = QuirkHandler.getInstance().returnQuirkList(currU);
    //          if(quirks.equalsIgnoreCase(TokenReader.readString(st)))
    //          {
    //              if(debugCounter < 10)
    //              {
    //                  debugCounter++;
    //                  //LOGGER.debug(currU.getVerboseModelName()+quirks+" MATCHED");
    //              }
    //              continue;
    //          }
    //          else
    //              return false;
    //        }
    //
    //        return true;
    //    }

    public SArmy getArmy(int id) {
        for (SArmy currA : armies) {
            if (currA.getId() == id) {
                return currA;
            }
        }
        return null;
    }

    public List<SArmy> getArmies() {
        return armies;
    }

    public void removeArmy(int armyID) {
        Iterator<SArmy> i = armies.iterator();
        while (i.hasNext()) {
            SArmy currA = i.next();
            if (currA.getId() == armyID) {
                i.remove();
                break;
            }
        }
        CampaignMain.cm.toUser("PL|RA|" + armyID, getName(), false);
    }

    public void setArmies(ArrayList<SArmy> v) {
        armies = v;
        setSave();
    }

    // @salient - includes SO check to count only unlocked units LockedUnits_DecrementUnitCount
    private int getUnitCountMC() {
        if (getMyHouse().getBooleanConfig("LockedUnits_DecrementUnitCount")) {
            int count = 0;
            for (SUnit aUnit : getUnits()) {
                if (aUnit.isLocked() == false) count++;
            }
            return count;
        } else {
            return getUnits().size();
        }
    }

    // Comparable
    @Override
    public int compareTo(SPlayer player) {
        if (getRating() > player.getRating()) {
            return 1;
        } else if (getRating() < player.getRating()) {
            return -1;
        }
        return player.getName().compareTo(getName());
    }

    public int getScrapsThisTick() {
        return scrapsThisTick;
    }

    public void addScrapThisTick() {
        scrapsThisTick += 1;
    }

    public void setScrapsThisTick(int scraps) {
        scrapsThisTick = scraps;
    }

    public int getDonationsThisTick() {
        return donationsThisTick;
    }

    public void addDonationThisTick() {
        donationsThisTick += 1;
    }

    public void setDonatonsThisTick(int donations) {
        donationsThisTick = donations;
    }

    public Date getLastOnlineDate() {
        return new Date(lastOnline);
    }

    public long getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(long l) {
        lastOnline = l;
        SmallPlayer smallp = null;
        if (getMyHouse().getSmallPlayers().containsKey(getName().toLowerCase())) {
            // update the corresponding small player.
            smallp = getMyHouse().getSmallPlayers().get(getName().toLowerCase());
        } else {
            smallp =
                    new SmallPlayer(
                            getExperience(),
                            lastOnline,
                            getRating(),
                            getName(),
                            getFluffText(),
                            getMyHouse());
            getMyHouse().getSmallPlayers().put(getName().toLowerCase(), smallp);
        }

        smallp.setLastOnline(lastOnline);
    }

    public long getAttackRestrictionUntil() {
        return attackRestrictionUntil;
    }

    public void setAttackRestrictionUntil(long l) {
        attackRestrictionUntil = l;
    }

    public void setRating(double d) {
        super.setRating(d);

        // update the corresponding small player.
        SmallPlayer smallp = getMyHouse().getSmallPlayers().get(getName().toLowerCase());
        smallp.setRating(getRating());

        // if sharing ratings, send to clients
        if (!Boolean.parseBoolean(getMyHouse().getConfig("HideELO"))) {
            double rounded = getRatingRounded();
            CampaignMain.cm.toUser("PL|SR|" + rounded, getName(), false);
            CampaignMain.cm.doSendToAllOnlinePlayers("PI|RA|" + getName() + "|" + rounded, false);
        }

        setSave();
    }

    public String getFluffText() {
        if ((fluffText.length() > 0) && !fluffText.equals(" ") && !fluffText.equals("0")) {
            return fluffText;
        }
        return "";
    }

    public void setFluffText(String s) {
        fluffText = s;

        // update the corresponding small player.
        SmallPlayer smallp = getMyHouse().getSmallPlayers().get(getName().toLowerCase());
        smallp.setFluffText(fluffText);

        setSave();
    }

    /**
     * @return Returns the activeSince.
     */
    public long getActiveSince() {
        return activeSince;
    }

    public int getAmountOfTimesUnitExistsInArmies(int unitID) {
        int result = 0;
        List<SArmy> v = getArmies();
        for (int i = 0; i < v.size(); i++) {
            SArmy a = v.get(i);
            if (a.getUnit(unitID) != null) {
                result++;
            }
        }
        return result;
    }

    public void checkAndUpdateArmies(SUnit unit) {

        for (SArmy army : armies) {
            if (army.isUnitInArmy(unit)) {
                army.setBV(0);
                CampaignMain.cm.toUser(
                        "PL|SABV|" + army.getId() + "#" + army.getBV(), getName(), false);
            }
        }
    } // end checkAndUpdateArmies

    public int getGroupAllowance() {
        return groupAllowance;
    }

    public void setGroupAllowance(int i) {
        groupAllowance = i;
    }

    // set the current amount of reward points a player has.
    public void setRewardPoints(int rewardPoints) {
        super.setRewardPoints(rewardPoints);
        CampaignMain.cm.toUser("PL|SRP|" + rewardPoints, getName(), false);
        setSave();
    }

    public void setInfluence(int influence) {
        super.setInfluence(influence);
        CampaignMain.cm.toUser("PL|SI|" + getInfluence(), getName(), false);
        setSave();
    }

    // sets counter to next RP injection triggered by XP gains.
    public void setXpTillReward(int xp) {
        xpTillReward = xp;
        setSave();
    }

    public int getXpTillReward() {
        return xpTillReward;
    }

    // @salient sets counter to next flu injection triggered by XP gains.
    public void setXpTillFlu(int xp) {
        xpTillFlu = xp;
        setSave();
    }

    public int getXpTillFlu() {
        return xpTillFlu;
    }

    public void setPlayerSellingto(String selling) {
        sellingto = selling;
    }

    public String getPlayerSellingto() {
        return sellingto;
    }

    public void setPlayerClientVersion(Version version) {
        clientVersion = version;
    }

    public Version getPlayerClientVersion() {
        return clientVersion;
    }

    @Override
    public SPersonalPilotQueues getPersonalPilotQueue() {
        return personalPilotQueue;
    }

    public ExclusionList getExclusionList() {
        return exclusionList;
    }

    public void setLastTimeCommandSent(long l) {
        lastTimeCommandSent = l;
    }

    public long getLastTimeCommandSent() {
        return lastTimeCommandSent;
    }

    public void setLastAttackFromReserve(long time) {
        lastAttackFromReserve = time;
    }

    public long getLastAttackFromReserve() {
        return lastAttackFromReserve;
    }

    public boolean hasRepairingUnits() {
        return hasRepairingUnits(true);
    }

    /**
     * Method which returns a boolean indicating whether any units in all the armies or any units
     * period are being repaired.
     *
     * @param inArmy - if false, check all units. true, check units in armies.
     */
    public boolean hasRepairingUnits(boolean inArmy) {

        // if not using advanced repair don't spend the time checking.
        if (!CampaignMain.cm.isUsingAdvanceRepair()) {
            return false;
        }

        // only check for a repairing unit that is currently in an army
        if (inArmy) {

            for (SArmy army : armies) {
                for (Unit currU : army.getUnits()) {
                    // Needs to be done units are stripped of entity in the army
                    // Might be best to add that to Unit as well have to think
                    // about that --Torren.
                    // TODO: See comment above.
                    SUnit unit = getUnit(currU.getId());
                    if (UnitUtils.isRepairing(unit.getEntity())) {
                        return true;
                    }
                }
            } // end For
        } else { // check for any repairing units the player owns

            for (SUnit currU : getUnits()) {
                if (UnitUtils.isRepairing(currU.getEntity())) {
                    return true;
                }
            }
        } // end else

        return false;
    } // end hasRepairingUnits

    /**
     * Used for Advanced Repair cannot repair a unit that is in combat.
     *
     * @param unitID
     * @return
     */
    public boolean isUnitInLockedArmy(int unitID) {

        if (getUnit(unitID) == null) {
            return false;
        }

        // check all armies
        for (SArmy army : getArmies()) {
            if (!army.isLocked()) {
                continue;
            }
            if (army.getUnit(unitID) != null) {
                return true;
            }
        }

        return false;
    } // end isUnitInLockedArmy

    /** if damage transfers is allowed then pilots to heal while off line. */
    public void healAllPilots() {
        try {
            if (!Boolean.parseBoolean(getMyHouse().getConfig("AllowPilotDamageToTransfer"))) {
                return;
            }
            Long timeGone = System.currentTimeMillis() - lastOnline; // timeGone
            // /=60000;
            int tickTime = CampaignMain.cm.getIntegerConfig("TickTime");
            if (timeGone > tickTime) {
                healAllPilots((int) (timeGone / tickTime));
            }
        } catch (Exception ex) {
            LOGGER.error("Exception: ", ex);
        }
    }

    public void healAllPilots(int numberOfHeals) {
        if (!Boolean.parseBoolean(getMyHouse().getConfig("AllowPilotDamageToTransfer"))) {
            return;
        }
        int health =
                Integer.parseInt(getMyHouse().getConfig("PilotAmountHealedPerTick"))
                        * numberOfHeals;
        int medtechHeal =
                Integer.parseInt(getMyHouse().getConfig("MedTechAmountHealedPerTick"))
                        * numberOfHeals;

        if (Boolean.parseBoolean(getMyHouse().getConfig("AllowPersonalPilotQueues"))) {
            int typeList[] = {Unit.MEK, Unit.PROTOMEK, Unit.AERO};
            for (int type : typeList) {
                for (int weight = 0; weight <= Unit.ASSAULT; weight++) {
                    Queue<Pilot> list = personalPilotQueue.getPilotQueue(type, weight);
                    for (Pilot pilot : list) {
                        if (pilot.getHits() <= 0) {
                            continue;
                        }
                        int hits = pilot.getHits();

                        hits -= health;
                        if (pilot.getSkills().has(PilotSkill.MedTechID)) {
                            hits -= medtechHeal;
                        }

                        pilot.setHits(Math.max(0, hits));
                    } // end For each
                } // end for weight
            } // end for type
        }

        for (SUnit unit : getUnits()) {
            Pilot pilot = unit.getPilot();

            if (pilot.getHits() <= 0) {
                continue;
            }
            int hits = pilot.getHits();

            hits -= health;
            if (pilot.getSkills().has(PilotSkill.MedTechID)) {
                hits -= medtechHeal;
            }

            pilot.setHits(Math.max(0, hits));
        } // end for each
    } // end healAllPilots

    public void healPilots() {
        if (!Boolean.parseBoolean(getMyHouse().getConfig("AllowPilotDamageToTransfer"))) {
            return;
        }
        int health = Integer.parseInt(getMyHouse().getConfig("PilotAmountHealedPerTick"));
        int medtechHeal = Integer.parseInt(getMyHouse().getConfig("MedTechAmountHealedPerTick"));

        if (Boolean.parseBoolean(getMyHouse().getConfig("AllowPersonalPilotQueues"))) {
            int typeList[] = {Unit.MEK, Unit.PROTOMEK, Unit.AERO};
            for (int type : typeList) {
                for (int weight = 0; weight <= Unit.ASSAULT; weight++) {
                    Queue<Pilot> list = personalPilotQueue.getPilotQueue(type, weight);
                    for (Pilot pilot : list) {
                        if (pilot.getHits() <= 0) {
                            continue;
                        }
                        int hits = pilot.getHits();

                        hits -= health;
                        if (pilot.getSkills().has(PilotSkill.MedTechID)) {
                            hits -= medtechHeal;
                        }

                        pilot.setHits(Math.max(0, hits));
                    } // end For each
                } // end for weight
            } // end for type
        } else {
            for (SUnit unit : getUnits()) {
                Pilot pilot = unit.getPilot();

                if (pilot.getHits() <= 0) {
                    continue;
                }
                int hits = pilot.getHits();

                hits -= health;
                if (pilot.getSkills().has(PilotSkill.MedTechID)) {
                    hits -= medtechHeal;
                }

                pilot.setHits(Math.max(0, hits));
            } // end for each
        } // end else
    } // end healPilots

    // STATUS DISPLAY METHODS
    /*
     * These would normally be under the PUBLIC METHODS heading; however,
     * they're important (and long) enough to justify their own heading.
     */
    /**
     * Complete human readable status of a player. Absolutely must be maintained and properly
     * updated at all times. /c mystatus is the best/only way to accurately confirm a client's data
     * representation vs. the player's state according to the server.
     */
    public String getReadableStatus(boolean adminStatus) {
        DecimalFormat myFormatter = new DecimalFormat("####.##");
        StringBuilder s =
                new StringBuilder(
                        "<br><b>Status for: "
                                + getColoredName()
                                + " ("
                                + getMyHouse().getColoredName());

        if (getSubFactionName().trim().length() > 0) {
            s.append("::");
            s.append(getSubFactionName());
        }

        s.append(")</b><br>");

        // if being checked by an admin, show his activity status.
        if (adminStatus) {
            s.append("Activity Status: ");
            if (getDutyStatus() == STATUS_FIGHTING) {
                s.append("fighting<br>");
            } else if (getDutyStatus() == STATUS_ACTIVE) {
                s.append("active<br>");
            } else {
                s.append("inactive<br>");
            }

            if (getGroupAllowance() > 0) {
                s.append("IP Group Allowance: " + getGroupAllowance() + "<br>");
            }
        }

        s.append(
                "  "
                        + CampaignMain.cm.moneyOrFluMessage(true, false, getMoney())
                        + " //  "
                        + CampaignMain.cm.moneyOrFluMessage(false, false, getInfluence())
                        + " // "
                        + getExperience()
                        + " Experience<br>");

        // advanced repair
        if (CampaignMain.cm.isUsingAdvanceRepair()) {
            s.append(
                    "Technicians (Green/Reg/Vet/Elite): "
                            + getTotalTech(UnitUtils.TECH_GREEN)
                            + "/"
                            + getTotalTech(UnitUtils.TECH_REG)
                            + "/"
                            + getTotalTech(UnitUtils.TECH_VET)
                            + "/"
                            + getTotalTech(UnitUtils.TECH_ELITE)
                            + "<br>");
            s.append(
                    "Idle Techs (Green/Reg/Vet/Elite):  "
                            + getAvailableTech(UnitUtils.TECH_GREEN)
                            + "/"
                            + getAvailableTech(UnitUtils.TECH_REG)
                            + "/"
                            + getAvailableTech(UnitUtils.TECH_VET)
                            + "/"
                            + getAvailableTech(UnitUtils.TECH_ELITE)
                            + "<br>");
            s.append("Bays: " + getFreeBays() + "/" + getTotalMekBays() + "<br>");
            s.append(
                    "Leased Bays: "
                            + getBaysOwned()
                            + " (Cost: "
                            + CampaignMain.cm.moneyOrFluMessage(
                                    true, false, getCurrentTechPayment())
                            + "/Game)<br>");
        }

        // normal techs
        else {
            s.append(
                    "Technicians (Idle/Total): "
                            + getFreeBays()
                            + "/"
                            + getTotalMekBays()
                            + "<br>");
            s.append(
                    "Paid Technicians: "
                            + getTechnicians()
                            + " (Cost: "
                            + CampaignMain.cm.moneyOrFluMessage(
                                    true, false, getCurrentTechPayment())
                            + "/Game)<br>");
        }

        // give the players some basic vote info. should use /c myvotes to get
        // full vote info
        if (Boolean.parseBoolean(getMyHouse().getConfig("VotingEnabled"))) {
            int votesCast = CampaignMain.cm.getVoteManager().getAllVotesBy(this).size();
            int votesAllowed = getNumberOfVotesAllowed();
            if (votesAllowed == votesCast) {
                s.append("Votes: All votes cast (" + votesCast + "/" + votesAllowed + ").<br>");
            } else {
                s.append(
                        "Votes: "
                                + votesCast
                                + " votes cast. "
                                + votesAllowed
                                + " votes allowed. ("
                                + votesCast
                                + "/"
                                + votesAllowed
                                + ").<br>");
            }
        } // end if(voting is allowed)

        if (!Boolean.parseBoolean(getMyHouse().getConfig("HideELO")) && !adminStatus) {
            s.append("Rating: " + myFormatter.format(getRating()) + "<br>");
        }
        if (Boolean.parseBoolean(getMyHouse().getConfig("ShowReward"))) {
            s.append(
                    "Current "
                            + CampaignMain.cm.getConfig("RPLongName")
                            + ": "
                            + getRewardPoints()
                            + " (Maximum  of "
                            + Integer.parseInt(getMyHouse().getConfig("XPRewardCap"))
                            + ")<br>");
        }

        // if merc show their status.
        if (getMyHouse().isMercHouse()) {
            s.append("<br>" + getReadableMercStatus());
        }

        s.append("<br>");

        if ((Integer.parseInt(getMyHouse().getConfig("NoPlayListSize")) > 0)
                || (exclusionList.getAdminExcludes().size() > 0)) {

            // player no-play
            s.append("<b>No-Play List:</b> ");
            if (exclusionList.getPlayerExcludes().isEmpty()) {
                s.append("empty");
            } else {
                s.append(String.join(", ", exclusionList.getPlayerExcludes()));
            }
            s.append("<br>");

            // admin no-plays
            s.append("<b>No-Play (Admin):</b> ");
            if (exclusionList.getAdminExcludes().isEmpty()) {
                s.append("empty");
            } else {
                s.append(String.join(", ", exclusionList.getAdminExcludes()));
            }
            s.append("<br><br>");
        }

        s.append("<b>Current Armies:<br></b>");

        if (armies.size() == 0) {
            s.append("(No armies constructed)<br>");
        } else {
            // proceed to list lances and hangar contents
            for (SArmy currA : armies) {
                if (adminStatus) {
                    s.append(currA.getDescription(true, true, false) + "<br>");
                } else {
                    s.append(currA.getDescription(true, true, true) + "<br>");
                }
            }
        }

        s.append("<br><b>Contents of Hangar:</b><br>");
        for (SUnit currU : getUnits()) {

            if (currU.getStatus() == Unit.STATUS_FORSALE) {
                continue;
            }

            if (adminStatus) {
                s.append(currU.getDescription(false) + "<br>");
            } else {
                s.append(currU.getDescription(true) + "<br>");
            }
        }

        // Get info for units the player is selling on Market2
        StringBuilder saleUnits = new StringBuilder();
        for (SUnit currU : getUnits()) {
            if (currU.getStatus() != Unit.STATUS_FORSALE) {
                continue;
            }

            if (adminStatus) {
                saleUnits.append(currU.getDescription(false) + "<br>");
            } else {
                saleUnits.append(currU.getDescription(true) + "<br>");
            }
        }

        // only include sale heading if units are actually on market
        if (saleUnits.length() > 0) {
            s.append("<br><b>Units on Market:</b><br>");
            s.append(saleUnits);
        }

        s.append("<br>");

        // Return the player's PlayerFlags
        s.append("<b>Player Flags</b><br>");
        PlayerFlags pFlags = this.getFlags();
        for (String flag : pFlags.getFlagNames()) {
            s.append(flag + ": " + Boolean.toString(pFlags.getFlagStatus(flag)) + "<br>");
        }

        return s.toString();
    }

    /**
     * Method that returns a human readable string containing special info pertinent to mercenaries,
     * such an employer and contract terms.
     */
    public String getReadableMercStatus() {
        String s = "";
        if (getMyHouse().isMercHouse()) { // if a merc
            s = "Mercenary information for " + getName() + ": <br>"; // list name
            s +=
                    "Currently fighting for: "
                            + (((MercHouse) getMyHouse()).getHouseFightingFor(this)).getName()
                            + "<br>"; // list
            // employing
            // faction
            ContractInfo contract = (((MercHouse) getMyHouse()).getContractInfo(this));
            if (contract != null) {
                s += contract.getInfo(this);
            } else {
                s += "Contract Status: Currently avaliable for hire <br>";
            }
            s += "<br>";
        }
        return s;
    }

    // TOSTRING AND FROMSTRING METHODS
    // @salient - "Seems like instead of a boolean, this should be two separate methods.
    //            One for client. one for server."
    /*
     * These would normally be under the "methods" heading; however, they're so
     * huge (and important) that they get a separate block.
     */
    public String toString(boolean toClient) {
        SerializedMessage result = new SerializedMessage("~");
        result.append("CP");
        result.append(getName());
        result.append(getMoney());
        result.append(getExperience());
        result.append(getUnits().size());
        if (!getUnits().isEmpty()) {
            synchronized (getUnits()) {
                for (SUnit currU : getUnits()) {
                    currU.getPilot().setHouse(getMyHouse());
                    result.append(currU.toString(toClient));
                }
            }
        }
        result.append(armies.size());
        for (int i = 0; i < armies.size(); i++) {
            result.append(armies.get(i).toString(toClient, "%"));
        }
        if (!toClient) {
            if (getMyHouse() != null) {
                result.append(getMyHouse().getName());
            } else {
                result.append(CampaignMain.cm.getConfig("NewbieHouseName"));
            }
            result.append(lastOnline);
        }
        result.append(getTotalMekBays());
        result.append(getFreeBays());

        if (toClient) {
            if (Boolean.parseBoolean(getMyHouse().getConfig("HideELO"))) {
                result.append("0");
            } else {
                result.append(getRatingRounded());
            }
        } else {
            result.append(getRating());
        }
        result.append(getInfluence());
        if (!toClient) {
            result.append(fluffText + " ");
            /*
             * In older code, player-prefered game options were saved here. This
             * feature has been eliminated. Because of terrible coding (using
             * the standard ~ delimiter instead of an inner delimiter like $),
             * we can't eliminate the read in without endangering older saves.
             * We'll just save a 0 for now. Sometime in the future, this space
             * can be reclaimed. @urgru 12.28.05
             */
            // @salient - i'm going to reclaim it then for fluXProllover :)
            result.append(getXpTillFlu());
        }

        if (CampaignMain.cm.isUsingAdvanceRepair()) {
            result.append(getBaysOwned());
        } else {
            result.append(getTechnicians()); // used when saving to houses.dat
        }
        // above is used when sending to client bad hack but needed for now
        result.append(getRewardPoints()); // saving current reward points
        /*
         * In older code, player's price modifier (mezzo) was saved here. This
         * feature has been eliminated, and the spaces can be reclaimed. @urgru
         * 9.30.06
         */
        // @salient reclaiming this for mini campaign data
        if (!toClient) {
            result.append(saveStatusMC());
        }

        result.append(getMekTokens());

        result.append(getMyHouse().getName() + " ");
        if (toClient) {
            result.append(getHouseFightingFor().getName() + " ");
            if (getLogo().length() == 0) {
                result.append(getMyHouse().getLogo() + " ");
            } else {
                result.append(getLogo() + " ");
            }
        } else {
            result.append(xpTillReward);
            result.append(getBVTracker()); // @Salient for mini campaigns
            result.append(getPersonalPilotQueue().toString(toClient));
            result.append(getExclusionList().adminExcludeToString("$"));
            result.append(getExclusionList().playerExcludeToString("$"));

            if (CampaignMain.cm.isUsingAdvanceRepair()) {
                result.append(totalTechsToString());
                result.append(availableTechsToString());
                result.append(baysOwned);
            } else {
                result.append(" ");
                result.append(" ");
                result.append(getTechnicians());
            }
            if (getLogo().trim().length() == 0) {
                result.append(getMyHouse().getLogo() + " ");
            } else {
                result.append(getLogo() + " ");
            }
            result.append(getLastAttackFromReserve());
            result.append(getGroupAllowance());
            if (lastISP.length() < 1) {
                result.append(" ");
            } else {
                result.append(lastISP);
            }
        }
        result.append(isInvisible());
        if (!toClient) {
            result.append(groupAllowance);
            if (password != null) {
                result.append(password.getAccess());
                result.append(password.getPasswd());
                result.append(password.getTime());
            } else {
                result.append("0");
                result.append(" ");
                result.append("0");
            }
        }
        result.append(getUnitComponents().toString("|"));
        result.append(getAutoReorder());
        if (!toClient) {
            result.append(getTeamNumber());
            if (getSubFactionName().trim().length() < 1) {
                result.append(" ");
            } else {
                result.append(getSubFactionName());
            }
            result.append(getLastPromoted());
        }
        result.append(
                exportFlags().length() > 1
                        ? exportFlags()
                        : CampaignMain.cm.getDefaultPlayerFlags().export());
        result.append(saveDiscordInfo()); // @salient adding new field to save
        return result.getMessage();
    }

    /**
     * @author jtighe
     * @param s - string from a pfile Used for sperate pfiles with faction name stuck on the end.
     */
    public void fromString(String s) {
        if (s == null) {
            throw new NullPointerException("SPlayer fromString(s) is null");
        }

        isLoading = true;
        try {
            armies.clear();

            s = s.substring(3);
            StringTokenizer ST = new StringTokenizer(s, "~");
            setName(TokenReader.readString(ST));

            /*
             * name is set before the exclusion list is un-strung in
             * SPlayer.fromString(). Use this opportunity to set it in the
             * ExclusionList so strip/error messages can be sent back to the
             * player properly. Uber-Hacky, but functional.
             *
             * @urgru 4.2.05
             */
            exclusionList.setOwner(this);

            setMoney(TokenReader.readInt(ST));
            setExperience(TokenReader.readInt(ST));

            int numofarmies = 0;
            int numofUnits = TokenReader.readInt(ST);
            clearUnits();

            for (int i = 0; i < numofUnits; i++) {
                SUnit m = new SUnit();
                m.fromString((String) ST.nextElement());
                addUnit(m);
                CampaignMain.cm.toUser("PL|HD|" + m.toString(true), getName(), false);
            }

            numofarmies = (Integer.parseInt((String) ST.nextElement()));
            for (int i = 0; i < numofarmies; i++) {
                SArmy a = new SArmy(this);
                a.fromString((String) ST.nextElement(), "%", this);
                if (armies.size() < a.getId()) {
                    armies.add(a);
                } else {
                    armies.add(a.getId(), a);
                }
                CampaignMain.cm.toUser("PL|SAD|" + a.toString(true, "%"), getName(), false);
            }

            setMyHouse(CampaignMain.cm.getHouseFromPartialString(TokenReader.readString(ST), null));

            lastOnline = TokenReader.readLong(ST);
            // Just read it. It's not necessary to use it on the server..
            // It's useful for the client
            TokenReader.readString(ST); // Number of Bays
            TokenReader.readString(ST); // Number of Free Bays

            super.setRating(TokenReader.readDouble(ST));
            super.setInfluence(TokenReader.readInt(ST));

            fluffText = TokenReader.readString(ST).trim();

            // @Salient i've reclaimed unused token for xp till flu injection
            setXpTillFlu(TokenReader.readInt(ST));

            if (CampaignMain.cm.isUsingAdvanceRepair()) {
                int greenTechs = TokenReader.readInt(ST);
                int regTechs = greenTechs / 5;
                greenTechs -= regTechs;
                updateAvailableTechs(greenTechs + "%" + regTechs + "%0%0%");

                totalTechs = Arrays.copyOf(availableTechs, totalTechs.length);
                // give them some bays
                setBaysOwned(greenTechs + regTechs);
            } else {
                // technicians = TokenReader.readInt(ST);
                int te = TokenReader.readInt(ST);
                int mt = CampaignMain.cm.getIntegerConfig("MaxTechsToHire");
                setTechnicians((mt != -1) ? Math.min(te, mt) : te);
            }

            setRewardPoints(TokenReader.readInt(ST));

            // @salient reclaimed token for mini campaign data
            loadStatusMC(TokenReader.readString(ST));

            setMekTokens(TokenReader.readInt(ST));

            setMyHouse(CampaignData.cd.getHouseByName(TokenReader.readString(ST)));

            if (getMyHouse() == null) {
                setMyHouse(
                        CampaignData.cd.getHouseByName(
                                CampaignData.cd.getCampaignOptions().getConfig("NewbieHouseName")));
            }

            setXpTillReward(TokenReader.readInt(ST));

            setBVTracker(TokenReader.readInt(ST)); // @Salient for mini campaigns
            // TokenReader.readString(ST);

            getPersonalPilotQueue().fromString(TokenReader.readString(ST), "$");

            getExclusionList().adminExcludeFromString(TokenReader.readString(ST), "$");

            getExclusionList().playerExcludeFromString(TokenReader.readString(ST), "$");

            {
                try {
                    if (CampaignMain.cm.isUsingAdvanceRepair()) {
                        updateTotalTechs(TokenReader.readString(ST));

                        updateAvailableTechs(TokenReader.readString(ST));

                        setBaysOwned(TokenReader.readInt(ST));
                    } // get rid of the 3 blanks
                    else {
                        TokenReader.readString(ST);

                        TokenReader.readString(ST);
                        // allow servers to go back and forth using Bays as
                        // techs since bays are what techs are.
                        {
                            if (getTechnicians() <= 0) {
                                setTechnicians(TokenReader.readInt(ST));
                            } else {
                                TokenReader.readString(ST);
                            }
                        }
                    }
                } // Had alot of problems with advanced repair so lets just
                // use this.
                catch (Exception ex) {
                }
            } // get rid of the 2 blanks

            setLogo(TokenReader.readString(ST));

            // Stupid error with player logo if its blank it doesn't save
            // anything
            // and gets skipped.
            // Thats been fixed but for all the PFiles out there with the defect
            // this will allow them to
            // Still Load.
            try {
                setLastAttackFromReserve(TokenReader.readLong(ST));
                setGroupAllowance(TokenReader.readInt(ST));
                setLastISP(TokenReader.readString(ST));
                setInvisible(TokenReader.readBoolean(ST));
                setGroupAllowance(TokenReader.readInt(ST));
            } catch (Exception ex) {
            }

            try {
                int access = TokenReader.readInt(ST);
                String passwd = TokenReader.readString(ST);
                long time = TokenReader.readLong(ST);

                if (passwd.trim().length() > 2) {
                    setPassword(new MWPasswdRecord(getName(), access, passwd, time, ""));
                }
            } catch (Exception ex) {
                // Issue with password loading just stop now.
                isLoading = false;
                return;
            }
            if (CampaignMain.cm.getBooleanConfig("UsePartsRepair")) {
                getUnitComponents().fromString(TokenReader.readString(ST), "|");
            } else {
                TokenReader.readString(ST);
            }

            setAutoReorder(TokenReader.readBoolean(ST));

            setTeamNumber(TokenReader.readInt(ST));
            setSubfaction(getMyHouse().getSubfaction(TokenReader.readString(ST)));
            lastPromoted = TokenReader.readLong(ST);

            loadFlags(CampaignMain.cm.getDefaultPlayerFlags().export());

            String flagString = TokenReader.readString(ST);
            if (flagString.length() > 1) flags.loadPersonal(flagString);

            if (ST.hasMoreTokens()) {
                // @salient... i sure hope this doesnt break anything
                String discordData = TokenReader.readString(ST);
                if (CampaignMain.cm.getBooleanConfig("Enable_BotPlayerInfo")) {
                    loadDiscordInfo(discordData);
                }
            }

            if ((password != null) && (password.getPasswd().trim().length() <= 2)) {
                password.setAccess(IAuthenticator.GUEST);
            }

            CampaignMain.cm.toUser("PL|SB|" + getTotalMekBays(), getName(), false);
            CampaignMain.cm.toUser("PL|SF|" + getFreeBays(), getName(), false);
            if (CampaignMain.cm.isUsingAdvanceRepair()) {

                if (!this.hasRepairingUnits()) {
                    CampaignMain.cm.toUser("PL|UTT|" + totalTechsToString(), getName(), false);
                    CampaignMain.cm.toUser("PL|UAT|" + totalTechsToString(), getName(), false);
                    updateAvailableTechs(totalTechsToString()); // make
                    // sure
                    // techs
                    // are
                    // in
                    // synch
                } else {
                    CampaignMain.cm.toUser("PL|UTT|" + totalTechsToString(), getName(), false);
                    CampaignMain.cm.toUser("PL|UAT|" + availableTechsToString(), getName(), false);
                }
            }

            healAllPilots();

            /*
             * Check all units for bad ammo or illegal/mis-set vacant pilots.
             * This was being done at the same time as the units are unstrung,
             * but caused a null b/c fixAmmo() uses .getMyHouse()(), which is null at
             * that point in the unstring. If the units are changed as a result
             * of the checks, a PL|UU is sent, as well as a PL|SAD for each army
             * that includes the unit.
             */
            for (SUnit currU : getUnits()) {
                fixPilot(currU);
            }
        } catch (Exception ex) {
            LOGGER.error("Exception: ", ex);
        } finally {
            isLoading = false;
        }
    }

    /**
     * Issue with vacant pilots getting placed in !Mek and !Proto Units This fixes it. Will also be
     * helpful if future bugs cause vacant pilots.
     *
     * @param unit
     */
    private void fixPilot(SUnit unit) {
        if (!unit.hasVacantPilot()) {
            return;
        }

        if (Boolean.parseBoolean(getMyHouse().getConfig("AllowPersonalPilotQueues"))
                && unit.isSinglePilotUnit()) {
            return;
        }

        // set a new pilot
        SPilot pilot = getMyHouse().getNewPilot(unit.getType());
        unit.setPilot(pilot);

        // send an update to the player
        CampaignMain.cm.toUser(
                "PL|UU|" + unit.getId() + " |" + unit.toString(true), getName(), false);

        // correct the BV of any army which contains the unit
        for (SArmy currA : armies) {
            if (currA.getUnit(unit.getId()) != null) {
                currA.setBV(0);
                CampaignMain.cm.toUser("PL|SAD|" + currA.toString(true, "%"), getName(), false);
                CampaignMain.cm.getOpsManager().checkOperations(currA, true); // update
                // legal
                // operations
            }
        }
    }

    public int getPartsAmount(String part) {
        int amount = 0;
        amount += getHouseFightingFor().getPartsAmount(part);
        amount += getUnitComponents().getPartsCritCount(part);
        return amount;
    }

    public void updatePartsCache(String part, int amount) {

        if (amount < 0) {
            int playerAmount = getUnitComponents().getPartsCritCount(part);

            if (playerAmount >= Math.abs(amount)) {
                getUnitComponents().remove(part, amount);
            } else {
                amount += playerAmount;
                getHouseFightingFor().updatePartsCache(part, amount);
                getUnitComponents().remove(part, playerAmount);
                amount = -playerAmount;
            }
        } else {
            getUnitComponents().add(part, amount);
        }
        CampaignMain.cm.toUser("PL|UPPC|" + part + "#" + amount, getName(), false);
    }

    public SArmy getLockedArmy() {
        for (SArmy army : getArmies()) {
            if (!army.isLocked()) {
                continue;
            }
            return army;
        }

        return null;
    }

    @Override
    public void setTeamNumber(int team) {
        super.setTeamNumber(team);
        setSave();
    }

    @Override
    public void setSubfaction(SubFaction subfaction) {
        super.setSubfaction(subfaction);
        if (getSubFactionAccess() != 0) {
            setLastPromoted(System.currentTimeMillis());
        }
        setSave();
    }

    public boolean playerIsLoading() {
        return isLoading;
    }

    public boolean canBePromoted() {
        if (getMyHouse().getSubfactions().size() < 1) {
            return false;
        }

        int days = getMyHouse().getIntegerConfig("daysbetweenpromotions");
        long day = 1000 * 60 * 60 * 24;

        try {
            long daysSinceLastPromoted = (System.currentTimeMillis() - getLastPromoted()) / day;

            // They've been promoted in the last number of days so they are not
            // eligible for a check.
            if (daysSinceLastPromoted < days) {
                return false;
            }
        } catch (Exception ex) {
            LOGGER.error("Exception: ", ex);
            return false;
        }
        return true;
    }

    public void checkForPromotion() {
        if (CampaignMain.cm.getBooleanConfig("Disable_Promote_Subfaction")) { // @salient
            return;
        }

        if (!canBePromoted()) {
            return;
        }
        int currentAccessLevel = getSubFactionAccess();

        for (SubFaction subFaction : getMyHouse().getSubfactions()) {
            if ((currentAccessLevel < Integer.parseInt(subFaction.getConfig("AccessLevel"))) && (getRating() >= Integer.parseInt(subFaction.getConfig("MinELO"))) && (getExperience() >= Integer.parseInt(subFaction.getConfig("MinExp")))) {
                CampaignMain.cm.toUser("You are eligible for a promotion to subFaction " + subFaction.getConfig("Name") + ". <a href=\"MEKWARS/c RequestSubFactionPromotion#" + subFaction.getConfig("Name") + "\">Click here to request promotion.</a>", getName());
            }
        }
    }

    public void checkForDemotion() {
        if (CampaignMain.cm.getBooleanConfig("Disable_Demote_Subfaction")) { // @salient
            return;
        }

        SubFaction subfaction = getSubfaction();

        int access = getSubFactionAccess();
        int elo = Integer.parseInt(subfaction.getConfig("MinELO"));
        int exp = Integer.parseInt(subfaction.getConfig("MinExp"));

        // can go any lower
        if (access < 1) {
            return;
        }

        // Auto Promotes and Demotes no need to inform anyone
        if (CampaignMain.cm.getBooleanConfig("autoPromoteSubFaction")) {
        	SubFaction newSF = null;
        	for (SubFaction subFaction : getMyHouse().getSubfactions()) {
        		if ((access > Integer.parseInt(subFaction.getConfig("AccessLevel"))) && (getRating() >= Integer.parseInt(subFaction.getConfig("MinELO"))) && (getExperience() >= Integer.parseInt(subFaction.getConfig("MinExp")))) {
        			if (newSF == null) {
        				newSF = subFaction;
        			} else if (Integer.parseInt(subFaction.getConfig("AccessLevel")) > Integer.parseInt(newSF.getConfig("AccessLevel"))) {
        				newSF = subFaction;
        			}
        		}
        	}

            if (newSF == null) {
                return; // Nothing to demote him to
            }
            SubFaction subFaction = newSF;
            String subFactionName = subFaction.getConfig("Name");
            setSubfaction(subFaction);
            CampaignMain.cm.toUser("PL|SSN|" + subFactionName, getName(), false);
            CampaignMain.cm.doSendToAllOnlinePlayers(
                    "PI|FT|" + getName() + "|" + getFluffText(), false);
            CampaignMain.cm.toUser("HS|CA|0", getName(), false); // clear
            // old
            // data
            CampaignMain.cm.toUser(getMyHouse().getCompleteStatus(), getName(), false);
            for (SArmy army : getArmies()) {
                CampaignMain.cm.getOpsManager().checkOperations(army, true);
            }

            CampaignMain.cm.toUser(
                    "AM:You have been demoted to SubFaction " + subFactionName + ".", getName());
            CampaignMain.cm.doSendHouseMail(
                    getMyHouse(),
                    "NOTE",
                    getName()
                            + " has been demoted to subfaction "
                            + subFactionName
                            + " by the Faction Leadership!");
            return;
        }

        if (((elo > getRating()) || (exp > getExperience()))
                && !CampaignMain.cm.getBooleanConfig("disableDemotionNotification")
                && !CampaignMain.cm.getBooleanConfig("autoPromoteSubFaction")) {
            StringBuilder message = new StringBuilder(getName());
            message.append(" no longer meets the eligbility requirements for subfaction ");
            message.append(getSubFactionName());
            message.append(". He is eligible for the following:<br>");
            for (SubFaction subFaction : getMyHouse().getSubfactions()) {
                if ((access > Integer.parseInt(subFaction.getConfig("AccessLevel"))) && (getRating() >= Integer.parseInt(subFaction.getConfig("MinELO"))) && (getExperience() >= Integer.parseInt(subFaction.getConfig("MinExp")))) {
                    message.append(subFaction.getName());
                    message.append(". <a href=\"MEKWARS/c demoteplayer#");
                    message.append(getName());
                    message.append("#");
                    message.append(subFaction.getConfig("Name"));
                    message.append("\">Click here to demote.</a><br>");
                }
            }

            message.append("None");
            message.append(". <a href=\"MEKWARS/c demoteplayer#");
            message.append(getName());
            message.append("#");
            message.append("None");
            message.append("\">Click here to demote.</a><br>");

            getMyHouse().sendMessageToHouseLeaders(message.toString());
        }
    }

    public long getLastPromoted() {
        return lastPromoted;
    }

    public void setLastPromoted(long promotedTime) {
        lastPromoted = promotedTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * A player may only have 1 army locked at a time. This will lock that army and unlock any
     * others Passing an armyId of -1 will unlock all armies.
     *
     * @param armyId
     */
    public void lockArmy(int armyId) {

        for (SArmy army : getArmies()) {

            if (army.getId() == armyId) {
                army.setLocked(true);
                CampaignMain.cm.toUser("PL|SAL|" + armyId + "#" + true, getName(), false);
            } else if (army.isLocked()) {
                army.setLocked(false);
                CampaignMain.cm.toUser("PL|SAL|" + army.getId() + "#" + false, getName(), false);
            }
        }
    }

    public void setUserValidated(boolean validated) {
        userValidated = validated;
    }

    public boolean isValidated() {
        return userValidated;
    }

    /**
     * A method to determine if a player is above or below the hangar limits for units, based on
     * type and weight.
     *
     * @param uType - type of unit (Unit.MEK, Unit.VEHICLE, etc.)
     * @param uWeightClass - weightclass of unit (Unit.LIGHT, Unit.MEDIUM, etc)
     * @return true if the player is below the limit, false if he's at or above
     */
    public boolean hasRoomForUnit(int uType, int uWeightClass) {
        if ((uType < 0) || (uType > Unit.AERO)) {
            LOGGER.error("Invalid uType in SPlayer.hasRoomForUnit: " + uType);
            return false;
        }
        if ((uWeightClass < 0) || (uWeightClass > Unit.ASSAULT)) {
            LOGGER.error("Invalid uWeightClass in SPlayer.hasRoomForUnit: " + uWeightClass);
            return false;
        }
        int limit = CampaignData.cd.getCampaignOptions().getUnitLimit(uType, uWeightClass);

        if (limit < 0) {
            // Unlimited
            return true;
        }
        int inHangar = countUnits(uType, uWeightClass);

        return (inHangar < limit)
                || ((inHangar >= limit)
                        && Boolean.parseBoolean(getMyHouse().getConfig("UseSlidingHangarLimits")));
    }

    /**
     * A method to determine if any of the unit limits have been exceeded
     *
     * @return true if any limits are exceeded, false otherwise
     */
    public boolean isOverAnyUnitLimits() {
        for (int t = Unit.MEK; t <= Unit.AERO; t++) {
            for (int w = Unit.LIGHT; w <= Unit.ASSAULT; w++) {
                int limit = CampaignData.cd.getCampaignOptions().getUnitLimit(t, w);
                int inHangar = countUnits(t, w);
                if ((limit != -1) && (inHangar > limit)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @author Salient A method to determine if player is at the unit limits
     * @return true if at all limits, false otherwise
     */
    public boolean isAtUnitLimits() {
        boolean result = false;
        boolean dontCountAero = CampaignMain.cm.getBooleanConfig("IgnoreAeroUnitLimit");
        int uType = Unit.AERO;

        if (dontCountAero) {
            uType = Unit.BATTLEARMOR;
        }

        for (int t = Unit.MEK; t <= uType; t++) {
            for (int w = Unit.LIGHT; w <= Unit.ASSAULT; w++) {
                int limit = CampaignData.cd.getCampaignOptions().getUnitLimit(t, w);
                int inHangar = countUnits(t, w);

                if (limit != -1) {
                    return inHangar == limit;
                }
            }
        }
        return result;
    }

    /**
     * @author Salient A method to determine if player is at or over the unit limits
     * @return true if at or over all limits, false otherwise
     */
    public boolean isAtOrOverUnitLimits() {
        boolean result = false;
        boolean dontCountAero =
                CampaignData.cd.getCampaignOptions().getBooleanConfig("IgnoreAeroUnitLimit");
        int uType = Unit.AERO;

        if (dontCountAero) {
            uType = Unit.BATTLEARMOR;
        }

        for (int t = Unit.MEK; t <= uType; t++) {
            for (int w = Unit.LIGHT; w <= Unit.ASSAULT; w++) {
                int limit = CampaignData.cd.getCampaignOptions().getUnitLimit(t, w);
                int inHangar = countUnits(t, w);

                if (limit != -1) {
                    return inHangar >= limit;
                }
            }
        }
        return result;
    }

    /**
     * A method to determine if the player will be over the unit limit after purchasing a new unit
     * if the server is configured to use sliding hangar cost increases
     */
    public boolean willHaveHangarPenalty(int uType, int uWeight) {
        // Always false if we're not using the sliding limits
        if (!Boolean.parseBoolean(getMyHouse().getConfig("UseSlidingHangarLimits"))) {
            return false;
        }

        int limit = CampaignData.cd.getCampaignOptions().getUnitLimit(uType, uWeight);

        // Always false if the particular limit is not checked
        if (limit < 0) {
            return false;
        }

        // Need to add one, since we're checking what it will be after a purchase
        int numUnits = countUnits(uType, uWeight) + 1;
        // False if we're below the limit
        if (limit >= numUnits) {
            return false;
        }

        return true;
    }

    /**
     * Calculates and returns the string to be sent to the client to set both the maintenance
     * penalty and the purchase price penalty for each unit type and weight.
     *
     * @return
     */
    public String buildHangarPenaltyString() {
        StringBuilder toReturn = new StringBuilder();

        toReturn.append(Integer.toString(calculateTotalHangarPenalty()));

        for (int type = Unit.MEK; type < Unit.MAXBUILD; type++) {
            for (int weight = Unit.LIGHT; weight <= Unit.ASSAULT; weight++) {
                toReturn.append(
                        "*"
                                + Integer.toString(
                                        calculateHangarPenaltyForNextPurchase(type, weight)));
            }
        }

        return toReturn.toString();
    }

    public int calculateHangarPenaltyForNextPurchase(int type, int weight) {
        int penalty = 0;

        int limit = CampaignData.cd.getCampaignOptions().getUnitLimit(type, weight);
        int numUnits = countUnits(type, weight) + 1;

        if ((limit == -1) || (numUnits <= limit)) {
            return 0;
        }

        int penaltyUnits = numUnits - limit;

        penalty =
                (int)
                        (Math.pow(
                                penaltyUnits,
                                Double.parseDouble(
                                        getMyHouse().getConfig("SlidingHangarLimitModifier"))));
        return penalty;
    }

    // @salient
    public boolean hasUnusedMekTokens() {
        if (getMekTokens() < Integer.parseInt(getMyHouse().getConfig("FreeBuild_Limit"))) {
            return true;
        }

        return false;
    }

    // @salient send msg to self
    public void toSelf(String msg) {
        CampaignMain.cm.toUser(msg, getName(), true);
    }
}
