/*
 * MekWars - Copyright (C) 2004 
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

package mekwars.common.net.hpgnet.packets;

import mekwars.common.net.AbstractPacket;
import mekwars.common.net.InvalidPacketException;

public enum PacketType implements AbstractPacket.Type {
    SERVER_REGISTER(0, ServerRegister.class),
    SERVER_UPDATE(1, ServerUpdate.class),
    SERVER_QUERY_ALL(2, ServerQueryAll.class),
    SERVER_QUERY_ALL_RESPONSE(3, ServerQueryAllResponse.class),
    SERVER_QUERY_RESPONSE(4, ServerQueryResponse.class),
    SERVER_RESPONSE(5, ServerResponse.class),
    SERVER_REGISTER_RESPONSE(6, ServerRegisterResponse.class),
    PING(7, Ping.class);

    private final int type;
    private final Class<? extends AbstractPacket> klass;

    private PacketType(int type, Class<? extends AbstractPacket> klass) {
        this.type = type;
        this.klass = klass;
    }

    public int getType() {
        return type;
    }

    public Class<? extends AbstractPacket> getPacketClass() {
        return klass;
    }

    public static PacketType fromInteger(int type) throws InvalidPacketException {
        switch (type) {
            case 0: return SERVER_REGISTER;
            case 1: return SERVER_UPDATE;
            case 2: return SERVER_QUERY_ALL;
            case 3: return SERVER_QUERY_ALL_RESPONSE;
            case 4: return SERVER_QUERY_RESPONSE;
            case 5: return SERVER_RESPONSE;
            case 6: return SERVER_REGISTER_RESPONSE;
            case 7: return PING;
            default: throw new InvalidPacketException("Invalid PacketType");
        }
    }
}
