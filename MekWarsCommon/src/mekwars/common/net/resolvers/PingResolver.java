/*
 * MekWars - Copyright (C) 2025
 * 
 * Original author - Bob Eldred (spork@mekwars.org)  
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
import mekwars.common.net.packets.PacketType;
import mekwars.common.net.packets.Ping;

public class PingResolver extends AbstractResolver<Ping, Connection> {
    public PingResolver(ConnectionHandler handler) {
        super(handler);
    }

    public void receive(Ping message, Connection connection) throws IOException {
        connection.serverHeartbeat();
    }

    public boolean canResolve(AbstractPacket.Type packetType) {
        return packetType.getType() == PacketType.PING.getType();
    }
}

