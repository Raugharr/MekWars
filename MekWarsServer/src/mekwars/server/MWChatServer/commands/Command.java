/*
 * MekWars - Copyright (C) 2004 
 * 
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet),
 * which based its code on classes from NFCChat, a GPL chat client/server.
 * Original NFC code can be found @ http://nfcchat.sourceforge.net
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
package mekwars.server.MWChatServer.commands;

import mekwars.common.comm.TransportCodec;
import mekwars.server.ServerWrapper;
import mekwars.server.MWChatServer.MWChatClient;
import mekwars.server.MWChatServer.MWChatServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Command extends CommandBase implements ICommands {
    private static final Logger LOGGER = LogManager.getLogger(Command.class);

    /**
     * @return true if this message should be distributed to other clients
     */
    public boolean process(MWChatClient client, String[] args) {
        try {
            ((ServerWrapper) client.getServer()).processCommand(client.getUserId(), TransportCodec.unescape(args[1]));
        } catch (Exception e) {
            LOGGER.error("Exception: ", e);
            LOGGER.error("Not supposed to happen");
        }
        return false;
    }

    public void processDistributed(String client, String origin, String[] args, MWChatServer server) {

    }
}
