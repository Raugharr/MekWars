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

package mekwars.server.net.data.resolvers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import mekwars.common.io.FileChecksum;
import mekwars.common.net.AbstractPacket;
import mekwars.common.net.AbstractResolver;
import mekwars.common.net.Connection;
import mekwars.common.net.ConnectionHandler;
import mekwars.common.net.data.packets.DataPacketType;
import mekwars.common.net.data.packets.ServerConnect;
import mekwars.common.net.data.packets.ServerConnectResponse;
import mekwars.common.net.data.packets.UpdateEntities;
import mekwars.server.MWServ;
import mekwars.server.io.FileSystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerConnectResolver extends AbstractResolver<ServerConnect, Connection> {
    private static final Logger LOGGER = LogManager.getLogger(ServerConnectResolver.class);

    public ServerConnectResolver(ConnectionHandler handler) {
        super(handler);
    }

    public void receive(ServerConnect message, Connection connection) throws IOException {
        ConcurrentHashMap<String, FileChecksum> checksumHash = FileSystem.getInstance().getChecksumFiles();
        ArrayList<String> outdatedFiles = new ArrayList<String>();

        for (String filename : checksumHash.keySet()) {
            FileChecksum fileChecksum = FileSystem.getInstance().getFile(filename);
            FileChecksum serverFileChecksum = checksumHash.get(filename);
            byte[] messageFileContents = message.getChecksum(filename);

            if (serverFileChecksum == null) {
                LOGGER.warn("Unable to compare checksums for '{}', file not found", filename);
                continue;
            }
            byte[] serverFileContents = serverFileChecksum.getChecksum();
            
            LOGGER.info("Checksum for file '{}', client {}, server {}", filename, messageFileContents, serverFileContents);
            if (messageFileContents == null || !messageFileContents.equals(serverFileContents)) {
                outdatedFiles.add(fileChecksum.getFilename());
           } 
        }
        try {
            String[] filenames = new String[outdatedFiles.size()];

            outdatedFiles.toArray(filenames);
            connection.write(new ServerConnectResponse(filenames, MWServ.SERVER_VERSION));
            if (!message.isDedicated()) {
                connection.write(new UpdateEntities(MWServ.getInstance().getCampaign().getData()));
            }
        } catch (IOException exception) {
           LOGGER.error("Unable to write response", exception); 
        }
        super.receive(message, connection);
    }

    public boolean canResolve(AbstractPacket.PacketType packetType) {
        return packetType.getType() == DataPacketType.SERVER_CONNECT.getType();
    }
}
