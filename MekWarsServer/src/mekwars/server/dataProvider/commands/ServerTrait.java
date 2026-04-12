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

package mekwars.server.dataProvider.commands;

import mekwars.common.CampaignData;
import mekwars.common.House;
import mekwars.common.util.BinWriter;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.dataProvider.ServerCommand;
import mekwars.common.io.file.FactionTraitFile;
import mekwars.common.entities.FactionTrait;

import java.util.Date;

/**
 * @author Imi (immanuel.scholz@gmx.de)
 */
public class ServerTrait implements ServerCommand {
    /**
     * @see server.dataProvider.ServerCommand#execute(java.util.Date, java.io.PrintWriter,
     *     common.CampaignData)
     */
    public void execute(Date timestamp, BinWriter out, CampaignData data) throws Exception {
        String factionName = "common";
        FactionTraitFile factionTraitFile = CampaignMain.cm.getFactionTraitFileByHouse(factionName);
        StringBuilder builder = new StringBuilder();

        out.println(factionName, "TraitLine");
        out.println(factionTraitFile.getFactionTraits().size(), "TraitLine");
            for (FactionTrait factionTrait : factionTraitFile.getFactionTraits()) {
                factionTrait.serialize(builder);
                out.println(builder.toString(), "TraitLine");
                builder.setLength(0);
        }

        for (House house : CampaignMain.cm.getData().getAllHouses()) {
            factionTraitFile = CampaignMain.cm.getFactionTraitFileByHouse(house.getName());

            out.println(factionName, "TraitLine");
            out.println(factionTraitFile.getFactionTraits().size(), "TraitLine");
            for (FactionTrait factionTrait : factionTraitFile.getFactionTraits()) {
                factionTrait.serialize(builder);
                out.println(builder.toString(), "TraitLine");
                builder.setLength(0);
            }
        }
    }
}
