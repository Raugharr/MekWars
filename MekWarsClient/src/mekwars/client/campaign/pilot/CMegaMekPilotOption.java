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

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import mekwars.common.MegaMekPilotOption;
import mekwars.common.campaign.pilot.Pilot;

@Entity
@Table(name = "megamek_pilot_option")
public class CMegaMekPilotOption extends MegaMekPilotOption {

    @ManyToOne private CPilot pilot;

    public CMegaMekPilotOption() {}

    public CMegaMekPilotOption(String name, boolean value) {
        super(name, value);
    }

    @Override
    public CPilot getPilot() {
        return pilot;
    }

    @Override
    public void setPilot(Pilot pilot) {
        this.pilot = (CPilot) pilot;
    }
}
