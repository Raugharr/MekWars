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
    public static final int SIZE = 7;

    private int length;
    private short type;
    private boolean systemPacket;

    public PacketHeader(int length, short type, boolean systemPacket) {
        this.length = length;
        this.type = type;
        this.systemPacket = systemPacket;
    }

    public PacketHeader(ByteBufferInput buffer) {
        this((int) buffer.readInt(), (short) buffer.readInt(2), buffer.readInt(1) != 0);
    }

    public int getLength() {
        return length;
    }

    public short getType() {
        return type;
    }
    
    public boolean isSystemPacket() {
        return systemPacket;
    }

    /*
     * Writes the PacketHeader and the body to buffer.
     */
    public void write(ByteBufferOutput output) {
        output.writeInt(length); //Leave space for packet length
        output.writeInt(type, 2);
        output.writeBoolean(systemPacket);
    }
}
