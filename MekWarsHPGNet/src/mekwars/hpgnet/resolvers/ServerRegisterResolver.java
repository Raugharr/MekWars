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
import java.util.UUID;
import mekwars.common.net.AbstractPacket;
import mekwars.common.net.AbstractResolver;
import mekwars.common.net.ConnectionHandler;
import mekwars.common.net.hpgnet.packets.PacketType;
import mekwars.common.net.hpgnet.packets.ServerRegister;
import mekwars.common.net.hpgnet.packets.ServerRegisterResponse;
import mekwars.hpgnet.HPGNet;
import mekwars.hpgnet.HPGSubscriber;
import mekwars.hpgnet.HPGConnection;

public class ServerRegisterResolver extends AbstractResolver<ServerRegister, HPGConnection> {
    public ServerRegisterResolver(ConnectionHandler handler) {
        super(handler);
    }

    public void receive(ServerRegister message, HPGConnection connection) throws IOException {
        HPGNet tracker = HPGNet.getInstance();
        String ipAddress = connection.getIpAddress();

        // Check for duplicate servers.
        for (HPGSubscriber subscriber : tracker.getSubscribers()) {
            if (subscriber.equals(ipAddress)) {
                connection.write(new ServerRegisterResponse(UUID.fromString(subscriber.getUuid())));
            }
        }
        HPGSubscriber subscriber = new HPGSubscriber(UUID.randomUUID());

        connection.setSubscriber(subscriber);
        subscriber.setTracker(tracker);
        subscriber.setName(message.getName());
        subscriber.setUrl(message.getLink());
        subscriber.setMWVersion(message.getVersion());
        subscriber.setDescription(message.getDescription());
        subscriber.setLegacy(true);
        subscriber.setIpAddress(ipAddress);
        subscriber.setServerPort(message.getServerPort());
        subscriber.setDataPort(message.getDataPort());
        tracker.registerNewSubscriber(subscriber);
        
        subscriber.setLastUpdated(new Date());
        subscriber.calculateThreatLevel();
        subscriber.generateHTMLString();
        connection.write(new ServerRegisterResponse(UUID.fromString(subscriber.getUuid())));
    }

    public boolean canResolve(AbstractPacket.Type packetType) {
        return packetType.getType() == PacketType.SERVER_REGISTER.getType();
    }
}
