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

package mekwars.common.net.resolvers;

import java.io.IOException;
import mekwars.common.net.AbstractPacket;
import mekwars.common.net.AbstractResolver;
import mekwars.common.net.Connection;
import mekwars.common.net.ConnectionHandler;
import mekwars.common.net.packets.CloseConnection;
import mekwars.common.net.packets.SystemPacketType;

public class CloseConnectionResolver extends AbstractResolver<CloseConnection, Connection> {
    public CloseConnectionResolver(ConnectionHandler handler) {
        super(handler);
    }

    public void receive(CloseConnection message, Connection connection) throws IOException {
        connection.close();
    }

    public boolean canResolve(AbstractPacket.PacketType packetType) {
        return packetType.isSystemPacket() && packetType.getType() == SystemPacketType.PING.getType();
    }
}
