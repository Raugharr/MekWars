/*
 * MekWars - Copyright (C) 2025
 *
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet) Original author Helge Richter (McWizard)
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */

package mekwars.server.net.data;

import com.esotericsoftware.kryo.Kryo;
import mekwars.common.net.AbstractPacket;
import mekwars.common.net.Connection;
import mekwars.common.net.InvalidPacketException;
import mekwars.common.net.Server;
import mekwars.common.net.data.DataConnection;
import mekwars.common.net.data.packets.DataPacketType;
import mekwars.server.net.data.resolvers.FileWriteRequestResolver;
import mekwars.server.net.data.resolvers.ServerConnectResolver;

public class DataServer extends Server {
    private ServerKryoThreadLocal kryos;

    public DataServer() {
        kryos = new ServerKryoThreadLocal();
    }

    public ThreadLocal<Kryo> getKryos() {
        return kryos;
    }

    @Override
    public AbstractPacket.PacketType getPacketType(int packetType) throws InvalidPacketException {
        return DataPacketType.fromInteger(packetType);
    }

    public Connection createConnection(ThreadLocal<Kryo> kryo) {
        return new DataConnection(kryo);
    }

    protected void addResolvers() {
        super.addResolvers();
        addResolver(new ServerConnectResolver(this));
        addResolver(new FileWriteRequestResolver(this));
    }
}
