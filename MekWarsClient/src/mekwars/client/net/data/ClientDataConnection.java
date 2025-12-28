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

package mekwars.client.net.data;

import com.esotericsoftware.kryo.Kryo;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import megamek.Version;
import mekwars.client.MWClient;
import mekwars.client.io.FileSystem;
import mekwars.common.io.FileChecksum;
import mekwars.common.net.Connection;
import mekwars.common.net.Listener;
import mekwars.common.net.data.DataConnection;
import mekwars.common.net.data.packets.FileWriteRequest;
import mekwars.common.net.data.packets.ServerConnect;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Connects to the MekWars server and is responsible for requesting entities or files from the
 * server. An entity is any game object like a {@link Terrain} or {@link Planet} the client needs
 * in order to play the game properly. If the game state changes this conenction is responsible for
 * recieving and handling that information. 
 */
public class ClientDataConnection extends DataConnection {
    private static final Logger LOGGER = LogManager.getLogger(DataConnection.class);
    private static final Listener ON_CONNECT_LISTENER = new Listener() {
        @Override
        public void connected(Connection connection) {
            ClientDataConnection dataConnection = (ClientDataConnection) connection;

            ConcurrentHashMap<String, byte[]> checksumHash = new ConcurrentHashMap<String, byte[]>();
            ConcurrentHashMap<String, FileChecksum> fileChecksumHash = FileSystem.getInstance().getChecksumFiles();

            for (String filename : fileChecksumHash.keySet()) {
                checksumHash.put(filename, fileChecksumHash.get(filename).getChecksum());
            }

            try {
                dataConnection.writeServerConnect(
                    MWClient.CLIENT_VERSION,
                    checksumHash,
                    dataConnection.getMWClient().isDedicated()
                );
            } catch (IOException exception) {
                LOGGER.error("Unable to write ServerManifestRequest", exception);
            }
        }
    };

    private MWClient mwClient;

    public ClientDataConnection(MWClient mwClient, ThreadLocal<Kryo> kryos) {
        super(kryos);
        this.mwClient = mwClient;
        addListener(ON_CONNECT_LISTENER);
    }

    public MWClient getMWClient() {
        return mwClient;
    }

    /**
     * Sends the server a ServerConnect packet.
     *
     * @param version The version of the client.
     *
     * @param fileChecksums A hash containing the filename as the key and a checksum of the values
     * as the value.
     *
     * @param isDedicated If the connection is for a dedicated server.
     *
     * @throws IOException When the connection is unable to send the packet to the server.
     */
    public void writeServerConnect(Version version, ConcurrentHashMap<String, byte[]> fileChecksums,
            boolean isDedicated) throws IOException {
       write(new ServerConnect(version, fileChecksums, isDedicated)); 
    }

    public void requestFile(String filename) throws IOException {
        write(new FileWriteRequest(filename));
    }
}
