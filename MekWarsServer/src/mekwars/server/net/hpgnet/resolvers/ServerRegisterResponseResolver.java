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

package mekwars.server.net.hpgnet.resolvers;

import java.util.Properties;
import java.util.UUID;
import mekwars.common.net.AbstractPacket;
import mekwars.common.net.AbstractResolver;
import mekwars.common.net.Connection;
import mekwars.common.net.hpgnet.packets.PacketType;
import mekwars.common.net.hpgnet.packets.ServerRegisterResponse;
import mekwars.server.MWServ;
import mekwars.server.net.hpgnet.HPGSubscribedClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerRegisterResponseResolver extends AbstractResolver<ServerRegisterResponse, Connection> {
    private static final Logger logger = LogManager.getLogger(ServerRegisterResponseResolver.class);

    public ServerRegisterResponseResolver(HPGSubscribedClient client) {
        super(client);
    }

    public void receive(ServerRegisterResponse message, Connection connection) {
        if (getHandler().getHpgId() != null) {
            logger.warn("Already assigned an HPG id");
            return;
        }

        logger.info("setting HpgId to: '{}'", message.getUid());
        getHandler().setHpgId(message.getUid());
        Properties config = MWServ.getInstance().getCampaign().getCampaignOptions().getConfig();
        config.setProperty("TrackerUUID", getHandler().getHpgId().toString());
        MWServ.getInstance().saveConfigs();
    }

    public boolean canResolve(AbstractPacket.Type packetType) {
        return packetType.getType() == PacketType.SERVER_REGISTER_RESPONSE.getType();
    }

    @Override
    public HPGSubscribedClient getHandler() {
        return (HPGSubscribedClient) super.getHandler();
    }
}
