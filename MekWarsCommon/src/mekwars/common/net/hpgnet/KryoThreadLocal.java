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

package mekwars.common.net.hpgnet;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import java.util.UUID;
import mekwars.common.net.hpgnet.packets.Ping;
import mekwars.common.net.hpgnet.packets.ServerQueryAll;
import mekwars.common.net.hpgnet.packets.ServerQueryAllResponse;
import mekwars.common.net.hpgnet.packets.ServerQueryResponse;
import mekwars.common.net.hpgnet.packets.ServerRegister;
import mekwars.common.net.hpgnet.packets.ServerRegisterResponse;
import mekwars.common.net.hpgnet.packets.ServerResponse;
import mekwars.common.net.hpgnet.packets.ServerUpdate;

public class KryoThreadLocal extends ThreadLocal<Kryo> {
    protected Kryo initialValue() {
        Kryo kryo = new Kryo();

        kryo.register(UUID.class, new DefaultSerializers.UUIDSerializer());
        kryo.register(Ping.class);
        kryo.register(ServerQueryAll.class);
        kryo.register(ServerQueryAllResponse.class);
        kryo.register(ServerQueryResponse.class);
        kryo.register(ServerRegister.class);
        kryo.register(ServerRegisterResponse.class);
        kryo.register(ServerUpdate.class);
        kryo.register(ServerResponse.class);
        kryo.register(ServerResponse.Status.class);
        return kryo;
    }
}
