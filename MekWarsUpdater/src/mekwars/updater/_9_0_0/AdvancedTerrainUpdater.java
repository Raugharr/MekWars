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

package mekwars.updater._9_0_0;

import com.thoughtworks.xstream.XStream;
import java.util.ArrayList;
import mekwars.common.AdvancedTerrain;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.common.util.SMMNetXStream;
import mekwars.updater.Updater;
import mekwars.updater.FileUpdater;
import mekwars.updater._9_0_0.util.XMLAdvancedTerrainDataParser;

public class AdvancedTerrainUpdater extends FileUpdater<AdvancedTerrain> {

    public AdvancedTerrainUpdater() {
        super("./data/advterr.xml", "./data/advancedTerrain.xml");
    }

    protected ArrayList<AdvancedTerrain> deserialize() {
        ArrayList<AdvancedTerrain> terrains = new XMLAdvancedTerrainDataParser(getInFilename())
            .getAdvancedTerrains();

        for (AdvancedTerrain terrain : terrains) {
            CampaignMain.cm.getData().addAdvancedTerrain(terrain);
        }
        return terrains;
    }

    protected String serialize(ArrayList<AdvancedTerrain> elements) {
        XStream xstream = CampaignMain.cm.getXStream(); 
        
        return xstream.toXML(elements.toArray(new AdvancedTerrain[elements.size()]));
    }
}
