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

package mekwars.common.net.packets;

import mekwars.common.net.AbstractPacket;
import mekwars.common.net.InvalidPacketException;

public enum SystemPacketType implements AbstractPacket.PacketType {
    PING(0, Ping.class),
    CLOSE_CONNECTION(1, CloseConnection.class);

    private final int type;
    private final Class<? extends AbstractPacket> klass;

    SystemPacketType(int type, Class<? extends AbstractPacket> klass) {
        this.type = type;
        this.klass = klass;
    }

    public int getType() {
        return type;
    }

    public Class<? extends AbstractPacket> getPacketClass() {
        return klass;
    }

    public boolean isSystemPacket() {
        return true;
    }

    public static SystemPacketType fromInteger(int type) throws InvalidPacketException {
        switch (type) {
            case 0: return PING;
            case 1: return CLOSE_CONNECTION;
            default: throw new InvalidPacketException("Invalid PacketType");
        }
    }
}
