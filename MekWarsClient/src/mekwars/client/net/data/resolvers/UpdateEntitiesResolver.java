/*
 * MekWars - Copyright (C) 2025
 *
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet) Original author Helge Richter (McWizard)
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */

package mekwars.client.net.data.resolvers;

import java.io.IOException;
import mekwars.client.net.data.DataClient;
import mekwars.common.net.AbstractPacket;
import mekwars.common.net.AbstractResolver;
import mekwars.common.net.Connection;
import mekwars.common.net.ConnectionHandler;
import mekwars.common.net.data.packets.DataPacketType;
import mekwars.common.net.data.packets.UpdateEntities;

public class UpdateEntitiesResolver extends AbstractResolver<UpdateEntities, Connection> {
    public UpdateEntitiesResolver(ConnectionHandler handler) {
        super(handler);
    }

    /**
     * Updates the {@link MWClient MWClient's} {@link CampaignData} with the recieved entities.
     */
    public void receive(UpdateEntities message, Connection connection) throws IOException {
        DataClient client = (DataClient) getHandler();

        message.update(client.getMWClient().getData());
        super.receive(message, connection);
    }

    public boolean canResolve(AbstractPacket.PacketType packetType) {
        return packetType.getType() == DataPacketType.UPDATE_ENTITIES.getType();
    }
}
