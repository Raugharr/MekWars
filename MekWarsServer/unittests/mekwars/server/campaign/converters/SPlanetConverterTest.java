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

package mekwars.server.campaign.converters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.TreeMap;
import mekwars.common.Influences;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SPlanet;
import mekwars.server.common.util.SMMNetXStream;
import org.junit.jupiter.api.Test;

public class SPlanetConverterTest {
    @Test
    public void serialize() {
        CampaignMain campaignMain = new CampaignMain();
        Influences influences = new Influences();
        String planetName = "TestPlanet";
        int xcoord = 7;
        int ycoord = 13;
        int componentProduction = 51;
        boolean isHomeworld = true;
        String originalOwner = "Foobar";
        TreeMap<String, String> operationFlags = new TreeMap<String, String>();

        operationFlags.put("Foo", "Bar");
        SPlanet planet = new SPlanet(planetName, influences, componentProduction, xcoord, ycoord);
        planet.setHomeWorld(isHomeworld);
        planet.setOriginalOwner(originalOwner);
        planet.setPlanetFlags(operationFlags);

        SMMNetXStream xstream = new SMMNetXStream();
        String xml = xstream.toXML(planet);

        SPlanet newPlanet = (SPlanet) xstream.fromXML(xml);

        assertEquals(newPlanet.getName(), planetName);
        assertEquals(newPlanet.getCompProduction(), componentProduction);
        assertEquals(newPlanet.getPosition().x, xcoord);
        assertEquals(newPlanet.getPosition().y, ycoord);
        assertEquals(newPlanet.isHomeWorld(), isHomeworld);
        assertEquals(newPlanet.getOriginalOwner(), originalOwner);

        assertEquals(newPlanet.getPlanetFlags().size(), 1);
        assertEquals(newPlanet.getPlanetFlags().get("Foo"), "Bar");
    }
}
