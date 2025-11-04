/*
 * MekWars - Copyright (C) 2025
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

package mekwars.client.common;

import mekwars.common.net.hpgnet.packets.ServerQueryResponse;

public class ServerInfo implements Comparable {
    private String name;
    private int playerCount;
    private String version;
    private String hostname;
    private int serverPort;
    private int dataPort;

    public ServerInfo(String name, int playerCount, String version, String hostname, int serverPort,
            int dataPort) {

        this.name = name;
        this.playerCount = playerCount;
        this.version = version;
        this.hostname = hostname;
        this.serverPort = serverPort;
        this.dataPort = dataPort;
    }

    public ServerInfo(ServerQueryResponse response) {
        this(response.getServerRegister().getName(),
            response.getServerUpdate().getPlayersOnline(),
            response.getServerRegister().getVersion(),
            response.getHostname(),
            response.getServerRegister().getServerPort(),
            response.getServerRegister().getDataPort()
        );
    }

    public String getName() {
        return name;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public String getVersion() {
        return version;
    }

    public String getHostname() {
        return hostname;
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getDataPort() {
        return dataPort;
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(Object otherObject) {    
        return compareTo(otherObject) == 0;
    }

    @Override
    public int compareTo(Object otherObject) {
        ServerInfo other = (ServerInfo) otherObject;

        return getName().compareTo(other.getName());
    }
}
