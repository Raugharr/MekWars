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

package mekwars.common.net;

import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;

public class PacketHeader {
    // Size of the PacketHeader in bytes.
    public static final int SIZE = 8;

    private int length;
    private int type;

    public PacketHeader(int length, int type) {
        this.length = length;
        this.type = type;
    }

    public PacketHeader(ByteBufferInput buffer) {
        this(buffer.readInt(), buffer.readInt());
    }

    public int getLength() {
        return length;
    }

    public int getType() {
        return type;
    }

    /*
     * Writes the PacketHeader and the body to buffer.
     */
    public void write(ByteBufferOutput output) {
        output.writeInt(length); //Leave space for packet length
        output.writeInt(type);
    }
}
