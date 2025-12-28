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

package mekwars.common.net.data.packets;

import java.util.concurrent.ConcurrentHashMap;
import megamek.Version;
import mekwars.common.net.AbstractPacket;

/**
 * Responsible for initializing the connection between the {@link DataClient} and
 * {@link DataServer}
 */
public class ServerConnect extends AbstractPacket {
    private Version version;
    // Key: Filename, Value: checksum
    private ConcurrentHashMap<String, byte[]> checksumHash = new ConcurrentHashMap<String, byte[]>();
    private boolean isDedicated;

    /**
     * No argument constructor for serilization purposes only.
     */
    public ServerConnect() {}

    public ServerConnect(Version version, ConcurrentHashMap<String, byte[]> checksumHash,
            boolean isDedicated) {

        this.checksumHash = checksumHash;
        this.version = version;
        this.isDedicated = isDedicated;
    }

    /**
     * Returns the provided checksum for the filename.
     *
     * @param filename The Filename to retrieve the checksum for.
     *
     * @return The checksum for the given filename.
     */
    public byte[] getChecksum(String filename) {
        return checksumHash.get(filename);
    }

    /**
     * The {@link Version} of the connecting client.
     */
    public Version getVersion() {
        return version;
    }

    /**
     * If the connection for a dedicated server.
     */
    public boolean isDedicated() {
        return isDedicated;
    }

    /**
     * The {@link DataPacketType type} of packet this is.
     *
     * @return The {@link DataPacketType} of this class.
     */
    @Override
    public DataPacketType getType() {
        return DataPacketType.SERVER_CONNECT;
    }
}
