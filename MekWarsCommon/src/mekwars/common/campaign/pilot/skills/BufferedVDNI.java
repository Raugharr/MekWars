/*
 * MekWars - Copyright (C) 2008
 *
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
package mekwars.common.campaign.pilot.skills;

import mekwars.common.MegaMekPilotOption;
import mekwars.common.campaign.pilot.Pilot;

public class BufferedVDNI extends PilotSkill {
    public BufferedVDNI(int id) {
        super(id, "Buffered VDNI", "BVDNI");
        setDescription("MD Buffered VDNI");
    }

    @Override
    public void modifyPilot(Pilot p) {
        p.addMegamekOption(new MegaMekPilotOption("bvdni", true));
        // p.setBvMod(p.getBVMod() + 0.01);
    }
}
