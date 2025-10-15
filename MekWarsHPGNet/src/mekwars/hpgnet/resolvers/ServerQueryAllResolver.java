/*
 * MekWars - Copyright (C) 2018
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
import mekwars.common.net.hpgnet.packets.ServerRegister;
import mekwars.common.net.hpgnet.packets.ServerUpdate;
import mekwars.hpgnet.HPGNet;
import mekwars.hpgnet.HPGSubscriber;
import mekwars.hpgnet.HPGConnection;

public class ServerQueryAllResolver extends AbstractResolver<ServerQueryAll, HPGConnection> {
    public ServerQueryAllResolver(ConnectionHandler handler) {
        super(handler);
    }

    public void receive(ServerQueryAll message, HPGConnection connection) {
        HPGNet tracker = HPGNet.getInstance();
        ServerQueryResponse responseArray[] = new ServerQueryResponse[tracker.getSubscribers().size()];
        int index = 0;

        for (HPGSubscriber subscriber : tracker.getSubscribers()) {
            ServerRegister serverRegister = subscriber.toServerRegister();
            ServerUpdate serverUpdate = subscriber.toServerUpdate();
            responseArray[index] = new ServerQueryResponse(serverRegister, serverUpdate);
            index += 1;
        }
        connection.write(new ServerQueryAllResponse(responseArray));
    }

    public boolean canResolve(AbstractPacket.Type packetType) {
        return packetType.getType() == PacketType.SERVER_QUERY_ALL.getType();
    }
}
