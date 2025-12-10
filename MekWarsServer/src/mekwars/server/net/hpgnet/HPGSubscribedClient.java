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

package mekwars.server.net.hpgnet;

import com.esotericsoftware.kryo.Kryo;
import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.UUID;
import mekwars.common.net.AbstractPacket;
import mekwars.common.net.Client;
import mekwars.common.net.InvalidPacketException;
import mekwars.common.net.hpgnet.KryoThreadLocal;
import mekwars.common.net.hpgnet.packets.PacketType;
import mekwars.common.net.hpgnet.packets.ServerRegister;
import mekwars.common.net.hpgnet.packets.ServerUpdate;
import mekwars.server.MWServ;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.net.hpgnet.resolvers.ServerRegisterResponseResolver;
import mekwars.server.net.hpgnet.resolvers.ServerResponseResolver;

public class HPGSubscribedClient extends Client {
    public static final int TRACKER_PORT = 13731;
    private static KryoThreadLocal kryos = new KryoThreadLocal();

    private UUID hpgId;
    private int serverPort;
    private int dataPort;
    private String name;
    private String link;
    private String description;

    public HPGSubscribedClient(MWServ server, UUID uuid) {
        super();
        this.hpgId = uuid;
        this.name = server.getCampaign().getConfig("ServerName");
        this.link = server.getCampaign().getConfig("TrackerLink");
        this.description = server.getCampaign().getConfig("TrackerDesc");
        this.serverPort = Integer.parseInt(server.getConfigParam("SERVERPORT"));
        this.dataPort = Integer.parseInt(server.getConfigParam("DATAPORT"));
    }

    public UUID getHpgId() {
        return hpgId;
    }

    public void setHpgId(UUID hpgId) {
        this.hpgId = hpgId;
    }

    public ServerRegister getServerRegister() {
        MWServ server = MWServ.getInstance();

        return new ServerRegister(
            name,
            serverPort,
            dataPort,
            link,
            MWServ.SERVER_VERSION.toString(),
            description
        );
    }

    public ServerUpdate getServerUpdate() {
        MWServ server = MWServ.getInstance();
        int playersOnline = server.userCount(false);
        int gamesInProgress = 0;
        int gamesCompleted = 0;
        
        CampaignMain campaign = server.getCampaign();
        if (campaign != null) {
            gamesInProgress = campaign.getOpsManager().getRunningOps().size();
            gamesCompleted = campaign.getGamesCompleted();
            campaign.setGamesCompleted(0);
        }
        
        return new ServerUpdate(
            hpgId,
            playersOnline,
            gamesInProgress,
            gamesCompleted
        );
    }

    public AbstractPacket.Type getPacketType(int packetType) throws InvalidPacketException {
        return PacketType.fromInteger(packetType);
    }

    public ThreadLocal<Kryo> getKryos() {
        return kryos;
    }

    protected void addResolvers() {
		super.addResolvers();
        addResolver(new ServerResponseResolver(this));
        addResolver(new ServerRegisterResponseResolver(this));
    }
}
