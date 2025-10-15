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

package mekwars.server.net.hpgnet.resolvers;

import java.io.IOException;
import mekwars.common.net.AbstractPacket;
import mekwars.common.net.AbstractResolver;
import mekwars.common.net.Connection;
import mekwars.common.net.InvalidPacketException;
import mekwars.common.net.hpgnet.packets.PacketType;
import mekwars.common.net.hpgnet.packets.ServerResponse;
import mekwars.server.net.hpgnet.HPGSubscribedClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerResponseResolver extends AbstractResolver<ServerResponse, Connection> {
    private static final Logger logger = LogManager.getLogger(ServerResponseResolver.class);

    public ServerResponseResolver(HPGSubscribedClient client) {
        super(client);
    }

    public void receive(ServerResponse message, Connection connection) {
        if (message.getStatus() != ServerResponse.Status.OK) {
            if (message.getStatus() == ServerResponse.Status.BAD_TRACKER_ID) {
                logger.warn("Invalid TrackerId, Requesting new TrackerId.");
                getHandler().setHpgId(null);
                connection.write(getHandler().getServerRegister());
            } else {
                logger.error("Error sending message to tracker");
            }
        }
    }

    public boolean canResolve(AbstractPacket.Type packetType) {
        return packetType.getType() == PacketType.SERVER_RESPONSE.getType();
    }

    @Override
    public HPGSubscribedClient getHandler() {
        return (HPGSubscribedClient) super.getHandler();
    }
}
