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

package mekwars.hpgnet.resolvers;

import mekwars.common.net.AbstractPacket;
import mekwars.common.net.AbstractResolver;
import mekwars.common.net.ConnectionHandler;
import mekwars.common.net.hpgnet.packets.PacketType;
import mekwars.common.net.hpgnet.packets.ServerQueryAll;
import mekwars.common.net.hpgnet.packets.ServerQueryAllResponse;
import mekwars.common.net.hpgnet.packets.ServerQueryResponse;
import mekwars.hpgnet.HPGNet;
import mekwars.hpgnet.HPGSubscriber;
import mekwars.hpgnet.HPGConnection;

public class ServerQueryAllResolver extends AbstractResolver<ServerQueryAll, HPGConnection> {
    public ServerQueryAllResolver(ConnectionHandler handler) {
        super(handler);
    }

    public void receive(ServerQueryAll message, HPGConnection connection) {
        HPGNet tracker = HPGNet.getInstance();

        var responses = tracker.getSubscribers().stream()
            .filter(HPGSubscriber::isOnline)
            .map(s -> new ServerQueryResponse(
                        s.toServerRegister(),
                        s.toServerUpdate(),
                        s.getIpAddress())
            ).toArray(ServerQueryResponse[]::new);
        connection.write(new ServerQueryAllResponse(responses));
    }

    public boolean canResolve(AbstractPacket.Type packetType) {
        return packetType.getType() == PacketType.SERVER_QUERY_ALL.getType();
    }
}
