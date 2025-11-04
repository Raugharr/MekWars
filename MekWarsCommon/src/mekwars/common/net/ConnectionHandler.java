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
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
 * Base implementation for ConnectionHandlers. A ConnectionHandler wraps a nonblocking connection.
 * This can be any type of a connection that expects a response like a {@link Connection} or a
 * SocketChannel.
 *
 * When the ConnectionHandler recieves a packet, it will resolve it with a {@link Resolver}. 
 * 
 * {@link Connection} that expecets to recieve a response from messages. Handles processing responses from a connection though {@link AbstractResolver}.
 */
public abstract class ConnectionHandler {
    private static final Logger logger = LogManager.getLogger(ConnectionHandler.class);

    private ArrayList<AbstractResolver> resolvers;

    public ConnectionHandler() {
        resolvers = new ArrayList<AbstractResolver>();
        addResolvers();
    }

    /*
     * Determines which Resolver can process the given packet.
     */
    public void processPacket(AbstractPacket packet, Connection connection) {
        for (AbstractResolver resolver : resolvers) {
            if (resolver.canResolve(packet.getId())) {
                resolver.receive(packet, connection);
                return;
            }
        }
        logger.warn("Packet type '{}' not processed", packet.getId());
    }

    /*
     * Called when a connection can be read.
     */
    public void readConnection(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        Connection connection = (Connection) key.attachment();
        AbstractPacket packet = null;
        
        try {
            connection.read(this);
        } catch (Exception exception) {
           logger.catching(exception); 
           // Reset the buffer to try to get back to a valid state.
           connection.getInput().getByteBuffer().clear();
        }
    }

    /*
     * Called when a connection can be written to.
     */
    public void writeConnection(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        Connection connection = (Connection) key.attachment();

        connection.send();
    }

    /*
     * Called when a new connection is accepted.
     */
    public Connection createConnection(ThreadLocal<Kryo> kryo, SocketChannel socket,
            SelectionKey socketKey, int inputLen, int outputLen) {
        return new Connection(kryo, socket, socketKey, inputLen, outputLen);
    }

    public abstract Iterator<SelectionKey> select() throws IOException;
    
    /*
     * Disconnects the Client from the Server.
     */
    public abstract void close();

    public abstract void processKey(SelectionKey key) throws IOException;

    /*
     * Converts the packetType into an AbstractPacket.Type. If packetType is invalid an
     * InvalidPacketException is thrown.
     */
    public abstract AbstractPacket.Type getPacketType(int packetType) throws InvalidPacketException;

    public abstract ThreadLocal<Kryo> getKryos();

    protected void addResolver(AbstractResolver resolver) {
        resolvers.add(resolver);
    }

    /*
     * Called when the ConnectionHandler is intialized to setup all {@link Resolver}'s.
     */
    protected abstract void addResolvers();
}
