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

package mekwars.common.net.data;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer.CachedField;
import mekwars.common.House;
import mekwars.common.Planet;
import mekwars.common.net.KryoUtil;
import mekwars.common.net.data.packets.FilePacket;
import mekwars.common.net.data.packets.FileWriteRequest;
import mekwars.common.net.data.packets.ServerConnect;
import mekwars.common.net.data.packets.ServerConnectResponse;
import mekwars.common.net.data.packets.UpdateEntities;
import mekwars.common.serializers.FixedArraySerializer;
import mekwars.common.util.DataSerialization;

public class DataKryoUtil {
    public static void register(Kryo kryo) {
        KryoUtil.register(kryo);
        DataSerialization.register(kryo);
        kryo.register(byte[].class);
        kryo.register(ServerConnect.class);
        kryo.register(ServerConnectResponse.class);
        kryo.register(UpdateEntities.class);
        kryo.register(FileWriteRequest.class);
        kryo.register(FilePacket.class);

        FieldSerializer serializer = (FieldSerializer) kryo.getSerializer(UpdateEntities.class);
        CachedField field = serializer.getField("houses");
        Serializer housesSerializer = new FixedArraySerializer(House.class);
        field.setSerializer(housesSerializer);

        field = serializer.getField("planets");
        Serializer planetsSerializer = new FixedArraySerializer(Planet.class);
        field.setSerializer(planetsSerializer);
    }
}
