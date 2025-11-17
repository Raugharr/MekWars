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

package mekwars.client.net.hpgnet.resolvers;

import mekwars.common.net.AbstractPacket;
import mekwars.common.net.CallbackResolver;
import mekwars.common.net.Connection;
import mekwars.common.net.ConnectionHandler;
import mekwars.common.net.hpgnet.packets.PacketType;
import mekwars.common.net.hpgnet.packets.ServerQueryAllResponse;

public class ServerQueryAllResponseResolver
    extends CallbackResolver<ServerQueryAllResponse, Connection> {

    public ServerQueryAllResponseResolver(ConnectionHandler handler) {
        super(handler);
    }

    public boolean canResolve(AbstractPacket.Type packetType) {
        return packetType.getType() == PacketType.SERVER_QUERY_ALL_RESPONSE.getType();
    }
}
