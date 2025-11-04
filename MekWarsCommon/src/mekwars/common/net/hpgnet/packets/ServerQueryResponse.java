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
 * The reponse body for a single subscriber of the HPGNet server.
 */
public class ServerQueryResponse extends AbstractPacket {
    private ServerRegister serverRegister;
    private ServerUpdate serverUpdate;

    public ServerQueryResponse() {}

    public ServerQueryResponse(ServerRegister serverRegister, ServerUpdate serverUpdate) {
        this.serverRegister = serverRegister;
        this.serverUpdate = serverUpdate; 
    }

    public PacketType getId() {
        return PacketType.SERVER_QUERY_RESPONSE;
    }

    public ServerRegister getServerRegister() {
        return serverRegister;
    }

    public ServerUpdate getServerUpdate() {
        return serverUpdate;
    }
}
