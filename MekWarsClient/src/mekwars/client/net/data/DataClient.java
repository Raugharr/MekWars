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

package mekwars.client.net.data;

import com.esotericsoftware.kryo.Kryo;
import mekwars.client.MWClient;
import mekwars.client.net.data.resolvers.FilePacketResolver;
import mekwars.client.net.data.resolvers.ServerConnectResponseResolver;
import mekwars.client.net.data.resolvers.UpdateEntitiesResolver;
import mekwars.common.net.AbstractPacket;
import mekwars.common.net.Client;
import mekwars.common.net.Connection;
import mekwars.common.net.InvalidPacketException;
import mekwars.common.net.data.KryoThreadLocal;
import mekwars.common.net.data.packets.DataPacketType;

public class DataClient extends Client {
    private static KryoThreadLocal kryos = new KryoThreadLocal();

    private MWClient mwClient;

    public DataClient(MWClient mwClient) {
        super();
        this.mwClient = mwClient;
    }

    public AbstractPacket.PacketType getPacketType(int packetType) throws InvalidPacketException {
        return DataPacketType.fromInteger(packetType);
    }

    public ThreadLocal<Kryo> getKryos() {
        return kryos;
    }

    public MWClient getMWClient() {
        return mwClient;
    }

    public Connection createConnection(ThreadLocal<Kryo> kryo) {
        return new ClientDataConnection(getMWClient(), kryo);
    }

    protected void addResolvers() {
        addResolver(new UpdateEntitiesResolver(this));
        addResolver(new ServerConnectResponseResolver(this));
        addResolver(new FilePacketResolver(this));
    }
}
