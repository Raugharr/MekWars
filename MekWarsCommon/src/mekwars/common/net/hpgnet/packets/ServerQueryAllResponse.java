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
 * The response body to a ServerQueryAllPacket containing all subscribers to a HPGNet server.
 */
public class ServerQueryAllResponse extends AbstractPacket {
    private ServerQueryResponse[] queryResponses;

    public ServerQueryAllResponse() {}

    public ServerQueryAllResponse(ServerQueryResponse[] queryResponses) {
        this.queryResponses = queryResponses;
    }

    public PacketType getId() {
        return PacketType.SERVER_QUERY_ALL_RESPONSE;
    }

    public ServerQueryResponse[] getQueryResponses() {
        return queryResponses;
    }
}
