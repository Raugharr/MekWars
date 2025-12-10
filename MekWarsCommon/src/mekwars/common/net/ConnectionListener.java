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

package mekwars.common.net;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
 * Runnable that continuously checks if a {@link ConnectionHandler} has any incomming connections,
 * calling {@link ConnectionHandler#processKey} when one is received. 
 */
public class ConnectionListener implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger(ConnectionListener.class);

    private AtomicBoolean shouldRun;
    private ConnectionHandler connectionHandler;

    public ConnectionListener(ConnectionHandler connectionHandler) {
        this.shouldRun = new AtomicBoolean(true);
        this.connectionHandler = connectionHandler;
    }

    @Override
    public void run() {
        while (shouldRun.get()) {
            try {
                Iterator<SelectionKey> iterator = null;

                try {
                    connectionHandler.heartbeat();
                    iterator = connectionHandler.select();
                } catch (IOException exception) {
                    connectionHandler.close();
                    LOGGER.error("Unable to run ConnectionListener", exception);
                    return;
                }

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();

                    iterator.remove();
                    try {
                        connectionHandler.processKey(key);
                    } catch (IOException exception) {
                        Connection connection = (Connection) key.attachment();
                        connection.close();
                        LOGGER.error("Unable to process key", exception);
                    }
                }
            } catch (Exception exception) {
                LOGGER.error("Unable to run ConnectionListener", exception);
            }
        }
    }

    /*
     * Gracefully shuts down the ConnectionListener.
     */
    public void shouldStop() {
        shouldRun.set(false);
    }
}
