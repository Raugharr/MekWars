/*
 * MekWars - Copyright (C) 2004
 *
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet)
 * Original author Helge Richter (McWizard)
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
 * Created on 24.03.2004
 *
 */
package mekwars.common.campaign.pilot;

import jakarta.persistence.Entity;
import jakarta.persistence.Embedded;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;

import mekwars.common.House;
import mekwars.common.MegaMekPilotOption;
import mekwars.common.Unit;
import mekwars.common.campaign.pilot.skills.PilotSkill;
import mekwars.common.campaign.pilot.skills.PilotSkills;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Helge Richter
 */
@Entity
public class Pilot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id = -1;

    private int gunnery = 4;
    private int piloting = 5;
    private String name = "John Doe";
    private int experience = 0;
    private int hits = 0;
    @OneToMany
    private List<MegaMekPilotOption> megamekOptions = new ArrayList<MegaMekPilotOption>();
    private String weapon = "Default"; // for Weapon Specialist skill

    @ManyToOne
    @JoinColumn(name = "house_id")
    private House house;

    @Transient
    private Unit unit;

    private String trait = null;
    boolean edgeWhenTac = true;
    boolean edgeWhenKo = true;
    boolean edgeWhenHeadhit = true;
    boolean edgeWhenExplosion = true;

    /** List of skills this pilot has obtained. */
    @Embedded
    private PilotSkills skills = new PilotSkills();

    private double bvMod = 0.0;
    private int bayModifier = 0;
    private int kills = 0;
    private int unitType = 0; // set the units type good for checking stuff

    public Pilot(House house, String name, int gunnery, int piloting) {
        setHouse(house);
        setName(name);
        setGunnery(gunnery);
        setPiloting(piloting);
    }

    /** Used for serialization */
    public Pilot() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return Returns the gunnery.
     */
    public int getGunnery() {
        return gunnery;
    }

    /**
     * @param gunnery The gunnery to set.
     */
    public void setGunnery(int gunnery) {
        this.gunnery = gunnery;
    }

    public String getSkillString(boolean abbreviated) {
        return getSkillString(abbreviated, "");
    }

    public String getSkillString(boolean abbreviated, String houseSkills) {
        StringBuilder result = new StringBuilder();
        Iterator<PilotSkill> i = getSkills().getSkillIterator();

        if (!i.hasNext()) return "";

        while (i.hasNext()) {
            PilotSkill skill = (PilotSkill) i.next();
            // Do not list house skills for pilots
            if (houseSkills.indexOf(skill.getName()) >= 0) continue;

            String lvl = "";
            if (skill.getLevel() != -1) lvl += skill.getLevel();
            if (abbreviated) result.append(skill.getAbbreviation() + lvl);
            else {
                if (skill.getName().equalsIgnoreCase("Weapon Specialist"))
                    result.append(skill.getName().trim() + " " + this.getWeapon());
                else if (skill.getName().equalsIgnoreCase("Trait")) {
                    result.append(getTraitName());
                } else if (lvl.length() > 0) result.append(skill.getName().trim() + " " + lvl);
                else result.append(skill.getName().trim());
            }
            if (i.hasNext()) {
                result.append(",");
                if (!abbreviated) {
                    result.append(" ");
                }
            }
        }

        if (result.toString().trim().endsWith(",")) result.deleteCharAt(result.lastIndexOf(","));
        return result.toString().trim();
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
     * @return Returns the piloting.
     */
    public int getPiloting() {
        return piloting;
    }

    /**
     * @param piloting The piloting to set.
     */
    public void setPiloting(int piloting) {
        this.piloting = piloting;
    }

    /**
     * @return Returns the hits.
     */
    public int getHits() {
        return hits;
    }

    /**
     * @param hits The hits to set.
     */
    public void setHits(int hits) {
        this.hits = hits;
    }

    /**
     * @return Returns the experience.
     */
    public int getExperience() {
        return experience;
    }

    /**
     * @param experience The experience to set.
     */
    public void setExperience(int experience) {
        this.experience = experience;
    }

    public void addMegamekOption(MegaMekPilotOption op) {
        megamekOptions.add(op);
        op.setPilot(this);
    }

    /**
     * @return Returns the bvMod.
     */
    public double getBVMod() {
        return bvMod;
    }

    /**
     * @param bvMod The bvMod to set.
     */
    public void setBvMod(double bvMod) {
        this.bvMod = bvMod;
    }

    /**
     * @return Returns the bayModifier.
     */
    public int getBayModifier() {
        return bayModifier;
    }

    /**
     * @param bayModifier The bayModifier to set.
     */
    public void setBayModifier(int bayModifier) {
        this.bayModifier = bayModifier;
    }

    /**
     * @return Returns the skills.
     */
    public PilotSkills getSkills() {
        return skills;
    }

    /**
     * @return Returns the megamekOptions.
     */
    public List<MegaMekPilotOption> getMegamekOptions() {
        return megamekOptions;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kill) {
        kills = kill;
    }

    public void addKill(int kill) {
        setKills(getKills() + kill);
    }

    public void setWeapon(String weapon) {
        this.weapon = weapon;
    }

    public String getWeapon() {
        return this.weapon;
    }

    public void setUnitType(int type) {
        this.unitType = type;
    }

    public int getUnitType() {
        return this.unitType;
    }

    public House getHouse() {
        return house;
    }

    public void setHouse(House house) {
        this.house = house;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public String getTraitName() {
        return trait;
    }

    public void setTraitName(String trait) {
        this.trait = trait;
    }

    public int getPilotId() {
        return this.id;
    }

    public void setPilotId(int id) {
        this.id = id;
    }

    public boolean getTac() {
        return edgeWhenTac;
    }

    public boolean getKO() {
        return edgeWhenKo;
    }

    public boolean getHeadHit() {
        return edgeWhenHeadhit;
    }

    public boolean getExplosion() {
        return edgeWhenExplosion;
    }

    public void setTac(boolean value) {
        edgeWhenTac = value;
    }

    public void setKO(boolean value) {
        edgeWhenKo = value;
    }

    public void setHeadHit(boolean value) {
        edgeWhenHeadhit = value;
    }

    public void setExplosion(boolean value) {
        edgeWhenExplosion = value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Pilot pilot = (Pilot) other;
        return getId() == pilot.getId();
    }

    @Override
    public int hashCode() {
        return Integer.valueOf(id).hashCode();
    }
}
