/*
 * MekWars - Copyright (C) 2025
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

package mekwars.hpgnet;

import com.esotericsoftware.kryo.Kryo;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import mekwars.common.net.AbstractPacket;
import mekwars.common.net.Connection;
import mekwars.common.net.InvalidPacketException;
import mekwars.common.net.Server;
import mekwars.common.net.hpgnet.KryoThreadLocal;
import mekwars.common.net.hpgnet.packets.PacketType;
import mekwars.hpgnet.resolvers.ServerQueryAllResolver;
import mekwars.hpgnet.resolvers.ServerRegisterResolver;
import mekwars.hpgnet.resolvers.ServerUpdateResolver;

public class HPGServer extends Server {
    private KryoThreadLocal kryos;

    public HPGServer() {
        kryos = new KryoThreadLocal();
    }

    public ThreadLocal<Kryo> getKryos() {
        return kryos;
    }

    @Override
    public Connection createConnection(ThreadLocal<Kryo> kryos, SocketChannel socket,
            SelectionKey socketKey, int inputLen, int outputLen) {
        return new HPGConnection(kryos, socket, socketKey, inputLen, outputLen);
    }

    @Override
    public AbstractPacket.Type getPacketType(int packetType) throws InvalidPacketException {
        return PacketType.fromInteger(packetType);
    }

    protected void addResolvers() {
        addResolver(new ServerUpdateResolver(this));
        addResolver(new ServerRegisterResolver(this));
        addResolver(new ServerQueryAllResolver(this));
    }
}
