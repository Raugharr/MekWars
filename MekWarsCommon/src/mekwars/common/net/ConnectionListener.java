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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
 * Runnable that continuously checks if a {@link ConnectionHandler} has any incomming connections,
 * calling {@link ConnectionHandler#processKey} when one is received. 
 */
public class ConnectionListener implements Runnable {
    private static final Logger logger = LogManager.getLogger(ConnectionListener.class);

    private AtomicBoolean shouldRun;
    private InetSocketAddress address;
    private ConnectionHandler connectionHandler;

    public ConnectionListener(ConnectionHandler connectionHandler, InetSocketAddress address) {
        this.shouldRun = new AtomicBoolean(true);
        this.address = address;
        this.connectionHandler = connectionHandler;
    }

    public void run() {
        while (shouldRun.get()) {
            Iterator<SelectionKey> iterator = null;

            try {
                iterator = connectionHandler.select();
            } catch (Exception exception) {
                logger.catching(exception);
            }

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();

                iterator.remove();
                try {
                    connectionHandler.processKey(key);
                } catch (Exception exception) {
                    logger.catching(exception);
                }
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
