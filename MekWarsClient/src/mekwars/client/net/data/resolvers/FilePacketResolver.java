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

package mekwars.client.net.data.resolvers;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import mekwars.client.io.FileSystem;
import mekwars.common.net.AbstractPacket;
import mekwars.common.net.AbstractResolver;
import mekwars.common.net.Connection;
import mekwars.common.net.ConnectionHandler;
import mekwars.common.net.data.packets.DataPacketType;
import mekwars.common.net.data.packets.FilePacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FilePacketResolver extends AbstractResolver<FilePacket, Connection> {

    private static final Logger LOGGER = LogManager.getLogger(FilePacketResolver.class);

    public FilePacketResolver(ConnectionHandler handler) {
        super(handler);
    }

    /**
     * Creates a file in the server's config directory for each entry in message's
     * getChecksumHashes.
     *
     * @param message The message sent to the connection.
     *
     * @param connection The connection receiving the message.
     */
    public void receive(FilePacket message, Connection connection) throws IOException {
        Path path = FileSystems.getDefault().getPath(
            FileSystem.getInstance().getConfigDir().toString(),
            message.getFilename()
        ); 

        // TODO: Test this conditional does what it should, ie prevent relative paths being
        // used to escape the data/server/ directory
        // if (path.toAbsolutePath().startsWith(FileSystem.getInstance().getConfigDir())) {
            try {
                Files.write(
                    path,
                    message.getContents(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING
                );
            } catch (IOException exception) {
                LOGGER.error("Unable to write file", exception);
                // TODO: The program cannot continue, we need to gracefully exit.
            }
        // }
        super.receive(message, connection);
    }

    public boolean canResolve(AbstractPacket.PacketType packetType) {
        return packetType.getType() == DataPacketType.FILE_PACKET.getType();
    }
}
