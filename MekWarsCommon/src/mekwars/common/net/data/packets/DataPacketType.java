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
import mekwars.common.net.InvalidPacketException;

public enum DataPacketType implements AbstractPacket.PacketType {
    SERVER_CONNECT(0, ServerConnect.class),
    SERVER_CONNECT_RESPONSE(1, ServerConnectResponse.class),
    UPDATE_ENTITIES(2, UpdateEntities.class),
    FILE_WRITE_REQUEST(3, FileWriteRequest.class),
    FILE_PACKET(4, FilePacket.class);

    private final int type;
    private final Class<? extends AbstractPacket> klass;

    private DataPacketType(int type, Class<? extends AbstractPacket> klass) {
        this.type = type;
        this.klass = klass;
    }

    public int getType() {
        return type;
    }

    public Class<? extends AbstractPacket> getPacketClass() {
        return klass;
    }

    public static DataPacketType fromInteger(int type) throws InvalidPacketException {
        switch (type) {
            case 0: return SERVER_CONNECT;
            case 1: return SERVER_CONNECT_RESPONSE;
            case 2: return UPDATE_ENTITIES;
            case 3: return FILE_WRITE_REQUEST;
            case 4: return FILE_PACKET;
            default: throw new InvalidPacketException("Invalid PacketType");
        }
    }
}
