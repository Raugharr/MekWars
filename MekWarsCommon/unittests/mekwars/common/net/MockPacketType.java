/*
 * MekWars - Copyright (C) 2025 
 * 
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet)
 * Original author Helge Richter (McWizard)
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

public enum MockPacketType implements AbstractPacket.Type {
    MOCK_PACKET(0, MockPacket.class);

    private final int type;
    private final Class<? extends AbstractPacket> klass;

    private MockPacketType(int type, Class<? extends AbstractPacket> klass) {
        this.type = type;
        this.klass = klass;
    }

    public int getType() {
        return type;
    }

    public Class<? extends AbstractPacket> getPacketClass() {
        return klass;
    }

    public static MockPacketType fromInteger(int type) throws InvalidPacketException {
        switch (type) {
            case 0: return MOCK_PACKET;
            default: throw new InvalidPacketException("Invalid PacketType");
        }
    }
}
