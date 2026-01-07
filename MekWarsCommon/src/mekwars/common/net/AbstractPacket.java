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

package mekwars.common.net;

/**
 * Base class for any packet that can be serialized by Kryo and sent over the network.
 */
public abstract class AbstractPacket {
    /**
     * Enum interface that declares all packets that can be in a protocol. When creating a new
     * protocol it is convention to create an Enum class that inherits this interface that contains
     * all PacketTypes of the protocol. Additionally it is convention to define a static method
     * PacketType fromInteger(int type), where PacketType is the Enum inheriting PacketType in order to
     * convert an integer back into a PacketType. This is necessary as a {@link ConnectionHandler} needs
     * to know how to convert an integer into a PacketType ({@see ConnectionHandler#getPacketType}). 
     */
    public interface PacketType {
        int getType();

        Class<? extends AbstractPacket> getPacketClass();

        default boolean isSystemPacket() {
            return false;
        }
    }

    public abstract PacketType getType();
}
