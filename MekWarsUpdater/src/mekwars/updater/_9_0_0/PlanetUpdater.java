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
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SPlanet;
import mekwars.updater.FileUpdater;
import mekwars.updater._9_0_0.util.XMLPlanetDataParser;

public class PlanetUpdater extends FileUpdater<SPlanet> {
    public PlanetUpdater() {
        super("./data/planets.xml");
    }

    protected ArrayList<SPlanet> deserialize() {
        return new XMLPlanetDataParser(getInFilename()).getPlanets();
    }

    protected String serialize(ArrayList<SPlanet> elements) {
        XStream xstream = CampaignMain.cm.getXStream();
        SPlanet[] elementList = elements.toArray(new SPlanet[elements.size()]);

        return xstream.toXML(elementList);
    }
}
