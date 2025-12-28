/*
 * MekWars - Copyright (C) 2025
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */

package mekwars.common.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer.CachedField;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.concurrent.ConcurrentHashMap;
import megamek.Version;
import megamek.common.planetaryconditions.Atmosphere;
import megamek.common.planetaryconditions.EMI;
import megamek.common.planetaryconditions.Fog;
import megamek.common.planetaryconditions.Light;
import megamek.common.planetaryconditions.PlanetaryConditions;
import megamek.common.planetaryconditions.Weather;
import megamek.common.planetaryconditions.Wind;
import megamek.common.planetaryconditions.WindDirection;
import mekwars.common.AdvancedTerrain;
import megamek.common.AmmoType;
import mekwars.common.Continent;
import mekwars.common.Influences;
import mekwars.common.House;
import mekwars.common.Planet;
import mekwars.common.PlanetEnvironment;
import mekwars.common.PlanetEnvironments;
import mekwars.common.Terrain;
import mekwars.common.UnitFactory;
import mekwars.common.util.Position;
import mekwars.common.persistence.NamedEntityStore;
import mekwars.common.util.BinReader;
import mekwars.common.universe.FactionTag;
import mekwars.common.serializers.FixedArraySerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DataSerialization {
    public static void register(Kryo kryo) {
        kryo.register(String[].class);
        kryo.register(int[].class);
        kryo.register(int[][].class);
        kryo.register(ArrayList.class);
        kryo.register(Atmosphere.class);
        kryo.register(EMI.class);
        kryo.register(Fog.class);
        kryo.register(Light.class);
        kryo.register(PlanetaryConditions.class);
        kryo.register(Weather.class);
        kryo.register(Wind.class);
        kryo.register(WindDirection.class);
        kryo.register(PlanetEnvironment.class);
        kryo.register(Terrain.class);
        kryo.register(AdvancedTerrain.class);
        kryo.register(ConcurrentHashMap.class);
        kryo.register(EnumSet.class);
        kryo.register(FactionTag.class);
        kryo.register(java.awt.Dimension.class);
        kryo.register(mekwars.common.PlanetEnvironments.class);
        kryo.register(mekwars.common.Influences.class);
        kryo.register(java.util.TreeMap.class);
        kryo.register(mekwars.common.util.Position.class);
        kryo.register(mekwars.common.Continent.class);
        kryo.register(House.class);
        kryo.register(Planet.class);
        kryo.register(UnitFactory.class);
        kryo.register(Version.class);

        FieldSerializer serializer = (FieldSerializer) kryo.getSerializer(Planet.class);
        CachedField field = serializer.getField("unitFactories");
        Serializer unitFactoriesSerializer = new FixedArraySerializer(UnitFactory.class);
        field.setSerializer(unitFactoriesSerializer);
    }
}
