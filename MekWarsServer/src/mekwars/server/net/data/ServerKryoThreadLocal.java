/*
 * MekWars - Copyright (C) 2025
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

package mekwars.server.net.data;

import com.esotericsoftware.kryo.Kryo;
import mekwars.common.House;
import mekwars.common.Planet;
import mekwars.common.UnitFactory;
import mekwars.common.net.data.DataKryoUtil;
import mekwars.server.campaign.NewbieHouse;
import mekwars.server.campaign.SHouse;
import mekwars.server.campaign.SPlanet;
import mekwars.server.campaign.SUnitFactory;
import mekwars.server.campaign.mercenaries.MercHouse;

public class ServerKryoThreadLocal extends ThreadLocal<Kryo> {
    @Override
    protected Kryo initialValue() {
        Kryo kryo = new Kryo();

        DataKryoUtil.register(kryo);
        kryo.register(SHouse.class, kryo.getSerializer(House.class));
        kryo.register(MercHouse.class, kryo.getSerializer(House.class));
        kryo.register(NewbieHouse.class, kryo.getSerializer(House.class));
        kryo.register(SPlanet.class, kryo.getSerializer(Planet.class));
        kryo.register(SUnitFactory.class, kryo.getSerializer(UnitFactory.class));
        return kryo;
    }
}
