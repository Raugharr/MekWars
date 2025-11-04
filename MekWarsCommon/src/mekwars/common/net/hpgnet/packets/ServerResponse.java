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

/*
 * Contains information that should be periodically sent to the HPGNet server.
 */
public class ServerResponse extends AbstractPacket {
    public enum Status {
        OK,
        NOT_OK,
        BAD_TRACKER_ID;
    }

    private Status status;

    public ServerResponse() {}

    public ServerResponse(Status status) {
        this.status = status;
    }

    public PacketType getId() {
        return PacketType.SERVER_RESPONSE;
    }

    public Status getStatus() {
        return status;
    }
}
