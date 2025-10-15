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

import java.util.UUID;
import mekwars.common.net.AbstractPacket;

/*
 * Contains information that should be periodically sent to the HPGNet server.
 */
public class ServerUpdate extends AbstractPacket {
    private UUID uid;
    private int playersOnline;
    private int gamesInProgress;
    private int gamesComplete;

    public ServerUpdate() {}

    public ServerUpdate(UUID uid, int playersOnline, int gamesInProgress, int gamesComplete) {
        this.uid = uid;
        this.playersOnline = playersOnline;
        this.gamesInProgress = gamesInProgress;
        this.gamesComplete = gamesComplete;
    }

    public PacketType getId() {
        return PacketType.SERVER_UPDATE;
    }

    public UUID getUid() {
        return uid;
    }

    public int getPlayersOnline() {
        return playersOnline;
    }

    public int getGamesInProgress() {
        return gamesInProgress;
    }

    public int getGamesComplete() {
        return gamesComplete;
    }
}
