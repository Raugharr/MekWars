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
 * Tells the HPGNet server about a new subscriber ad provides information that only needs to be
 * sent once.
 */
public class ServerRegister extends AbstractPacket {
    private String name;
    private int serverPort;
    private int dataPort;
    private String link;
    private String version;
    private String description;

    public ServerRegister() {}

    public ServerRegister(String name, int serverPort, int dataPort, String link, String version,
            String description) {
        this.name = name;
        this.serverPort = serverPort;
        this.dataPort = dataPort;
        this.link = name;
        this.version = version;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getDataPort() {
        return dataPort;
    }

    public String getLink() {
        return link;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public PacketType getId() {
        return PacketType.SERVER_REGISTER;
    }
}
