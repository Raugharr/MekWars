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

package mekwars.common.net.data.packets;

import megamek.Version;
import mekwars.common.net.AbstractPacket;

/**
 * The {@link DataServer DataServer's} response to a {@link ServerConnect} packet. The checksumHash
 * contains every file the {@link DataClient} either does not have or has an out of date version.
 */
public class ServerConnectResponse extends AbstractPacket {
    // List of files that need to be requested.
    private String[] filenames;
    /*
     * NOTE: We currently do nothing with the version as the MWChat server checks if the versions
     * match.
     */
    private Version version;

    /**
     * A zero argument constructor for serialization.
     */
    public ServerConnectResponse() {}

    public ServerConnectResponse(String[] filenames, Version version) {
      this.filenames = filenames;
      this.version = version;
    }

    public String[] getFilenames() {
        return filenames;
    }

    public Version getVersion() {
        return version;
    }

    @Override
    public DataPacketType getType() {
        return DataPacketType.SERVER_CONNECT_RESPONSE;
    }
}
