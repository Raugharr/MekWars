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

import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Server extends ConnectionHandler {
    private static final Logger logger = LogManager.getLogger(Server.class);
    private static final int BUFFER_SIZE = 4096;
    private static final int JOIN_WAIT_TIME = 100;
    private static final int SELECT_WAIT = 1000;

    private InetSocketAddress address;
    private ServerSocketChannel serverSocket;
    private Selector selector;
    private ConnectionListener connectionListener;
    private Thread serverThread;
    private ArrayList<Connection> connections;
    private ArrayList<Listener> listeners;
    private Listener dispatchListener = new Listener() {
        public void connect(Connection connection) {
            connections.add(connection);
            for (Listener listener : Server.this.listeners) {
                listener.connected(connection);
            }
        }

        public void disconnect(Connection connection) {
            connections.remove(connection);
            for (Listener listener : Server.this.listeners) {
                listener.disconnected(connection);
            }
        }
    };

    public Server() {
        super();
        this.connections = new ArrayList<Connection>();
    }

    /*
     * Attemps to connect to the given address.
     * If the connection is successful, a thread is created to listen to all messages.
     */
    public void bind(InetSocketAddress address) throws IOException {
        selector = Selector.open();
        serverSocket = selector.provider().openServerSocketChannel();
        serverSocket.bind(address);
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        logger.info("Binding to '{}'", address.toString());
    }

    /*
     * Starts to listen for connections on a thread using a {@link ConnectionThread}.
     */
    public void start(InetSocketAddress address) {
        try {
            bind(address);
        } catch (IOException exception) {
            logger.catching(exception);
            return;
        }
        connectionListener = new ConnectionListener(this, address);
        serverThread = new Thread(connectionListener, "Server Listener");
        serverThread.start();
    }

    /*
     * Close the socket and all related connections.
     */
    public void close() {
        for (Connection connection : connections) {
            connection.close();
        } 
        connections.clear();

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException exception) {
                logger.catching(exception); 
            }
            serverSocket = null;
        } 

        if (connectionListener != null) {
            connectionListener.shouldStop();
            connectionListener = null;
            try {
                serverThread.join(JOIN_WAIT_TIME);
            } catch (InterruptedException exception) {
                logger.catching(exception);
            }
        }
    }

    public Iterator<SelectionKey> select() throws IOException {
        selector.select(SELECT_WAIT);
        return selector.selectedKeys().iterator();
    }

    /*
     * Creates a new connection tracked by the Server.
     */
    public void acceptConnection(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);

        SelectionKey clientKey = channel.register(key.selector(), SelectionKey.OP_READ);
        Connection connection = createConnection(getKryos(), channel, clientKey,
                BUFFER_SIZE, BUFFER_SIZE);
        clientKey.attach(connection);
        connection.addListener(dispatchListener);
        connections.add(connection);
        logger.info("Accepting connection from '{}'", connection.getIpAddress());
    }

    public void processKey(SelectionKey key) throws IOException {
        if (!key.isValid()) {
            return;
        } 

        if (key.isAcceptable()) {
            acceptConnection(key);
        } else if (key.isReadable()) {
            readConnection(key);
        } else if (key.isWritable()) {
            writeConnection(key);
        }
    }

    public void writeAll(AbstractPacket packet) throws IOException {
        for (Connection connection : connections) {
            connection.write(packet);
        }
    }

    public ListIterator<Connection> getConnections() {
        return connections.listIterator();
    }

    public int connectedClients() {
        return connections.size();
    }
}
