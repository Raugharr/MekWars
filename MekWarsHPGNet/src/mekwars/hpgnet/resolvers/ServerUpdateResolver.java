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

import java.io.IOException;
import java.util.Date;
import mekwars.common.net.AbstractPacket;
import mekwars.common.net.AbstractResolver;
import mekwars.common.net.ConnectionHandler;
import mekwars.common.net.hpgnet.packets.PacketType;
import mekwars.common.net.hpgnet.packets.ServerResponse;
import mekwars.common.net.hpgnet.packets.ServerUpdate;
import mekwars.hpgnet.HPGNet;
import mekwars.hpgnet.HPGSubscriber;
import mekwars.hpgnet.HPGConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerUpdateResolver extends AbstractResolver<ServerUpdate, HPGConnection> {
    private static final Logger logger = LogManager.getLogger(ServerUpdateResolver.class);

    public ServerUpdateResolver(ConnectionHandler handler) {
        super(handler);
    }

    public void receive(ServerUpdate message, HPGConnection connection) throws IOException {
        HPGNet tracker = HPGNet.getInstance();
        synchronized (tracker.getSubscribers()) {
            HPGSubscriber subscriber = tracker.getSubscriber(message.getUid());
        
            if (subscriber == null) {
                connection.write(new ServerResponse(ServerResponse.Status.BAD_TRACKER_ID));
                logger.error("Subscriber '{}' does not exist", message.getUid());
                return;
            }
            
            connection.setSubscriber(subscriber);
            subscriber.update(message);
            subscriber.setLastUpdated(new Date());
            tracker.save(subscriber);
        }
        connection.write(new ServerResponse(ServerResponse.Status.OK));
    }

    public boolean canResolve(AbstractPacket.Type packetType) {
        return packetType.getType() == PacketType.SERVER_UPDATE.getType();
    }
}
