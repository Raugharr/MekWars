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

package mekwars.common.net.data;

import com.esotericsoftware.kryo.Kryo;
import java.io.IOException;
import mekwars.common.net.Connection;
import mekwars.common.net.data.packets.FileWriteRequest;

public class DataConnection extends Connection {
    public DataConnection(ThreadLocal<Kryo> kryos) {
        super(kryos);
    }

    public void sendFile(String filename) throws IOException {
        if (!validFilename(filename)) {
            throw new IOException();
        }
        writeFileWriteRequest(filename);
    }

    public boolean validFilename(String filename) {
        return true;
    }

    public void writeFileWriteRequest(String filename) throws IOException {
        write(new FileWriteRequest(filename));
    }
}
