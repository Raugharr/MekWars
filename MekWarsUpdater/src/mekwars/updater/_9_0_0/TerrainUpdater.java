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
import mekwars.common.Terrain;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.common.util.SMMNetXStream;
import mekwars.updater.Updater;
import mekwars.updater.FileUpdater;
import mekwars.updater._9_0_0.util.XMLTerrainDataParser;

public class TerrainUpdater extends FileUpdater<Terrain> {

    public TerrainUpdater() {
        super("./data/terrain.xml");
    }

    protected ArrayList<Terrain> deserialize() {
        ArrayList<Terrain> terrains = new XMLTerrainDataParser(getInFilename()).getTerrain();

        for (Terrain terrain : terrains) {
            CampaignMain.cm.getData().addTerrain(terrain);
        }
        return terrains;
    }

    protected String serialize(ArrayList<Terrain> elements) {
        XStream xstream = CampaignMain.cm.getXStream(); 
        
        return xstream.toXML(elements.toArray(new Terrain[elements.size()]));
    }
}
