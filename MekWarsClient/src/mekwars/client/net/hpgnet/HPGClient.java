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

package mekwars.client.net.hpgnet;

import com.esotericsoftware.kryo.Kryo;
import mekwars.client.MWClient;
import mekwars.client.net.hpgnet.resolvers.ServerQueryAllResponseResolver;
import mekwars.common.net.AbstractPacket;
import mekwars.common.net.CallbackResolverListener;
import mekwars.common.net.Client;
import mekwars.common.net.InvalidPacketException;
import mekwars.common.net.hpgnet.KryoThreadLocal;
import mekwars.common.net.hpgnet.packets.PacketType;

public class HPGClient extends Client {
    public static final int TRACKER_PORT = 13731;
    private static KryoThreadLocal kryos = new KryoThreadLocal();

    private MWClient mwClient;
    private ServerQueryAllResponseResolver serverQueryAllResponseResolver;

    public HPGClient(MWClient mwClient) {
        this.mwClient = mwClient;
    }

    protected void addResolvers() {
        serverQueryAllResponseResolver = new ServerQueryAllResponseResolver(this);
        addResolver(serverQueryAllResponseResolver);
    }

    public ThreadLocal<Kryo> getKryos() {
        return kryos;
    }

    public MWClient getMWClient() {
        return mwClient;
    }

    public AbstractPacket.Type getPacketType(int packetType) throws InvalidPacketException {
        return PacketType.fromInteger(packetType);
    }

    public void registerServerQueryAllResponse(CallbackResolverListener listener) {
        serverQueryAllResponseResolver.addListener(listener);
    }

    public void unregisterServerQueryAllResponse(CallbackResolverListener listener) {
        serverQueryAllResponseResolver.removeListener(listener);
    }
}
