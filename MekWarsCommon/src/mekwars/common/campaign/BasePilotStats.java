/*
 * MekWars - Copyright (C) 2026
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

package mekwars.common.campaign;

import mekwars.common.House;
import mekwars.common.Unit;
import mekwars.common.util.BinReader;
import mekwars.common.util.BinWriter;

import jakarta.persistence.Column;
import jakarta.persistence.Converter;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Convert;
import mekwars.common.campaign.converters.IntegerListConverter;
import mekwars.common.campaign.converters.StringListConverter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pilot_stats")
public class BasePilotStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "house_id")
    private House house;

    @Column(name = "gunnery_json")
    @Convert(converter = IntegerListConverter.class)
    private List<Integer> gunnery = new ArrayList<>();

    @Column(name = "piloting_json")
    @Convert(converter = IntegerListConverter.class)
    private List<Integer> piloting = new ArrayList<>();

    @Column(name = "skills_json")
    @Convert(converter = StringListConverter.class)
    private List<String> pilotSkills = new ArrayList<>();

    public BasePilotStats() {
        for (int pos = 0; pos < Unit.MAXBUILD; pos++) {
            gunnery.add(4);
            piloting.add(5);
            pilotSkills.add(" ");
        }
    }

    public BasePilotStats(BinReader in) throws IOException {
        this();
        gunnery.set(0, in.readInt("baseGunner"));
        piloting.set(0, in.readInt("basePilot"));
        for (int pos = 0; pos < Unit.MAXBUILD; pos++) {
            pilotSkills.set(pos, in.readLine("factionBasePilotSkill"));
        }
    }

    public void binOut(BinWriter out) throws IOException {
        out.println(getGunnery(Unit.MEK), "baseGunner");
        out.println(getPiloting(Unit.MEK), "basePilot");
        for (int pos = 0; pos < Unit.MAXBUILD; pos++) {
            out.println(pilotSkills.get(pos), "factionBasePilotSkill");
        }
    }

    public int getGunnery() {
        return getGunnery(Unit.MEK);
    }

    public int getGunnery(Integer type) {
        return gunnery.get(type);
    }

    public void setGunnery(Integer gunnerySkill, int type) {
        gunnery.set(type, gunnerySkill);
    }

    public int getPiloting() {
        return piloting.get(Unit.MEK);
    }

    public int getPiloting(Integer type) {
        return piloting.get(type);
    }

    public void setPiloting(Integer pilotingSkill, int type) {
        piloting.set(type, pilotingSkill);
    }

    public String getSkills(Integer type) {
        return pilotSkills.get(type);
    }

    public void setSkills(String skills, Integer type) {
        pilotSkills.set(type, skills);
    }

    public House getHouse() {
        return house;
    }

    public void setHouse(House house) {
        this.house = house;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
