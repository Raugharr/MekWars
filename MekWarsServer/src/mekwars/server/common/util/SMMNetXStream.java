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

package mekwars.server.common.util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.core.ReferenceByIdMarshallingStrategy;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;
import mekwars.common.Continent;
import mekwars.common.Influences;
import mekwars.common.Terrain;
import mekwars.common.util.MMNetXStream;
import mekwars.server.campaign.SPlanet;
import mekwars.server.campaign.SHouse;
import mekwars.server.campaign.SUnitFactory;
import mekwars.server.campaign.converters.ContinentConverter;
import mekwars.server.campaign.converters.SPlanetConverter;
import mekwars.server.campaign.converters.SUnitFactoryConverter;
import mekwars.server.campaign.converters.SHouseConverter;
import mekwars.server.campaign.converters.TerrainConverter;
import mekwars.server.campaign.converters.InfluencesConverter;

public class SMMNetXStream extends MMNetXStream {
    @Override
    protected void setup() {
        super.setup();
        registerConverter(new ContinentConverter());
        registerConverter(new SPlanetConverter());
        registerConverter(new SHouseConverter());
        registerConverter(new SUnitFactoryConverter());
        registerConverter(new TerrainConverter());
        registerConverter(new InfluencesConverter());

        alias("continent", Continent.class);
        alias("planet", SPlanet.class);
        alias("faction", SHouse.class);
        alias("unitFactory", SUnitFactory.class);
        alias("terrain", Terrain.class);
        alias("influences", Influences.class);
    }
}
