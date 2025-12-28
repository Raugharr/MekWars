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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import megamek.Version;
import mekwars.common.net.AbstractPacket;

public class FilePacket extends AbstractPacket {
    private String filename;
    private byte[] contents;

    /**
     * A zero argument constructor for serialization.
     */
    public FilePacket() { }

    public FilePacket(String filename, byte[] contents) {
        this.filename = filename;
        this.contents = contents;
    }

    public String getFilename() {
        return filename;
    }

    public byte[] getContents() {
        return contents;
    }

    @Override
    public DataPacketType getType() {
        return DataPacketType.FILE_PACKET;
    }
}
