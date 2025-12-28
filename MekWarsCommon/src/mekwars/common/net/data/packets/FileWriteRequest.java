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

import mekwars.common.net.AbstractPacket;

/**
 * Initialize a request to start sending data for the specified file.
 * After this request has been made FileData packets should be used to send the entirety of the
 * file until it has been either successfully sent, or there is an error.
 */
public class FileWriteRequest extends AbstractPacket {
    private String filename;

    /**
     * No argument constructor for serialization
     */
    public FileWriteRequest() {}
        
    public FileWriteRequest(String filename) {
        this.filename = filename; 
    }

    public String getFilename() {
        return filename;
    }

    @Override
    public DataPacketType getType() {
        return DataPacketType.FILE_WRITE_REQUEST;
    }
}
