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

package mekwars.common.net;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
 * Represents a socket that has been connected to a remote address.
 */
public abstract class Client extends ConnectionHandler {
    private static final int BUFFER_SIZE = 4096;
    private static final int JOIN_WAIT_TIME = 100;
    private static final int SELECT_WAIT = 1000;
    private static final Logger logger = LogManager.getLogger(Client.class);

    private Selector selector;
    private Connection connection;
    private ConnectionListener connectionListener;
    private Thread connectionThread;
    
    public Client() {
        super();
    }

    /*
     * Attemps to connect to the given address.
     * If the connection is successful, a thread is created to listen to all messages.
     */
    public void connect(InetSocketAddress address) throws IOException {
        selector = Selector.open();
        SocketChannel socket = SocketChannel.open(address);

        socket.configureBlocking(false);
        SelectionKey clientKey = socket.register(selector, SelectionKey.OP_READ);
        connection = createConnection(getKryos(), socket, clientKey, BUFFER_SIZE, BUFFER_SIZE);
        clientKey.attach(connection);
        logger.info("Connected to to {}:{}", connection.getIpAddress(), connection.getPort());

        connectionListener = new ConnectionListener(this, address);
        connectionThread = new Thread(connectionListener, "Client Listener");
        connectionThread.start();
    }

    public void close() {
        if (selector != null) {
            try {
                selector.close();
            } catch (IOException exception) {
                logger.catching(exception); 
            }
            selector = null;
        }

        if (connection != null) {
            connection.close();
            connection = null;
        }

        if (connectionListener != null) {
            connectionListener.shouldStop();
            try {
                connectionThread.join(JOIN_WAIT_TIME);
            } catch (InterruptedException exception) {
                logger.catching(exception);
            }
        }
    }

    public void connectConnection(SelectionKey key) throws IOException {
        key.interestOps(key.interestOps() & ~SelectionKey.OP_CONNECT);
        logger.info("Connected to to {}:{}", connection.getIpAddress(), connection.getPort());
    }

    public Iterator<SelectionKey> select() throws IOException {
        selector.select(SELECT_WAIT);
        return selector.selectedKeys().iterator();
    }

    public void processKey(SelectionKey key) throws IOException {
        if (!key.isValid()) {
            return;
        } 

        if (key.isConnectable()) {
            connectConnection(key);
        } else if (key.isReadable()) {
            readConnection(key);
        } else if (key.isWritable()) {
            writeConnection(key);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean isConnected() {
        return (connection != null && connection.isConnected());
    }
}
