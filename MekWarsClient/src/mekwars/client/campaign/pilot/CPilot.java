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

package mekwars.client.campaign.pilot;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import mekwars.client.campaign.CUnit;
import mekwars.common.House;
import mekwars.common.MegaMekPilotOption;
import mekwars.common.Unit;
import mekwars.common.campaign.pilot.Pilot;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pilot")
public class CPilot extends Pilot {
    @OneToOne(mappedBy = "pilot")
    private CUnit unit;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CMegaMekPilotOption> megamekOptions = new ArrayList<>();

    public CPilot(House house, String name, int gunnery, int piloting) {
        super(house, name, gunnery, piloting);
    }

    public CUnit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = (CUnit) unit;
    }

    @Override
    public void addMegamekOption(String name, boolean value) {
        CMegaMekPilotOption option = new CMegaMekPilotOption(name, value);
        option.setPilot(this);
        megamekOptions.add(option);
    }

    @Override
    public void addMegamekOption(MegaMekPilotOption option) {
        megamekOptions.add((CMegaMekPilotOption) option);
    }

    /**
     * @return Returns the megamekOptions.
     */
    @Override
    public List<CMegaMekPilotOption> getMegamekOptions() {
        return megamekOptions;
    }
}
