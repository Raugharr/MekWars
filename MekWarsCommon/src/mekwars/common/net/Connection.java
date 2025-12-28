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
import com.esotericsoftware.kryo.io.KryoBufferOverflowException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import mekwars.common.net.packets.SystemPacketType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Generic connection that represents a connection either from a client to a server, or a server
 * to a client.
 * <p>
 * Each Connection has a heartbeart, the server will disconnect the connection if the connection
 * has not sent a {@link Ping} ({@see Client#heartbeat}) within HEARTBEAT_INTERVAL miliseconds.
 */
public class Connection implements AutoCloseable {
    public static final int HEARTBEAT_INTERVAL = 20_000;
    public static final int HEARTBEAT_TIMEOUT = HEARTBEAT_INTERVAL * 2;
    private static final Logger LOGGER = LogManager.getLogger(Connection.class);
    private static long NEXT_ID = 1;

    private long id;
    private long nextHeartbeat;
    private SocketChannel socket;
    private SelectionKey socketKey;
    private ByteBufferInput input;
    private ByteBufferOutput output;
    private ThreadLocal<Kryo> kryos;
    private PacketHeader packetState;
    private ConcurrentLinkedQueue<AbstractPacket> messageQueue;
    private CopyOnWriteArrayList<Listener> listeners;

    public Connection(ThreadLocal<Kryo> kryos) {
        int bufferSize = bufferCapacity();
        int bufferLimit = bufferLimit();

        if (bufferLimit < bufferSize) {
            bufferLimit = bufferSize;
        }
        this.kryos = kryos;
        this.input = new ByteBufferInput(bufferSize);
        this.output = new ByteBufferOutput(bufferSize, bufferLimit);
        this.listeners = new CopyOnWriteArrayList<Listener>();
        this.messageQueue = new ConcurrentLinkedQueue<AbstractPacket>();
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    /**
     * Connects to the the provided address.
     *
     * @throws IOException
     */
    public void connect(SocketChannel socketChannel, Selector selector) throws IOException {
        if (!isConnected()) {
            socketChannel.configureBlocking(false);
            this.socket = socketChannel;
            this.socketKey = socket.register(selector, SelectionKey.OP_READ);
            this.id = NEXT_ID;
            this.socketKey.attach(this);
            NEXT_ID++;
            for (Listener listener : listeners) {
                listener.connected(this);
            }
        }
    }

    /**
     * Connects to the the provided address.
     *
     * @throws IOException When the SocketChannel is not able to be opened.
     */
    public void connect(InetSocketAddress address, Selector selector) throws IOException {
        connect(SocketChannel.open(address), selector);
    }

    @Override
    public void close() {
        if (isConnected()) {
            output.close();
            input.close();
            try {
                socket.close();
            } catch (IOException exception) {
                LOGGER.error("Unable to close Connection", exception);
            }
            LOGGER.debug("Closing connection {}", getId());

            for (Listener listener : listeners) {
                listener.disconnected(this);
            }
        }
    }

    public long getId() {
        return id;
    }

    public String getIpAddress() {
        return socket.socket().getLocalAddress().getHostName();
    }

    public int getPort() {
        return socket.socket().getPort();
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    /**
     * Serializes the given packet and writes the binary representation into the Connection's
     * write buffer. Declares to the socket channel that a write is ready to be performed.
     * In order for the packet to be send to the socket, send must be invoked.
     *
     * @param packet The {@link AbstractPacket} to write to the socket.
     *
     * @throws IOException When the socket is unable to be written to.
     */
    public void write(AbstractPacket packet) throws IOException {
        if (!isConnected()) {
            throw new SocketException("Connection is closed.");
        }

        if (!messageQueue.isEmpty()) {
            messageQueue.add(packet);
            return;
        }
        synchronized (getOutput()) {
            ByteBuffer buffer = getOutput().getByteBuffer();
            int start = buffer.position();

            try {
                getOutput().setBuffer(buffer, bufferLimit());
                writeInner(packet, start);
                socketKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            } catch (KryoBufferOverflowException exception) {
                /*
                 * Go back to the last valid position, then add the packet to the queue and try
                 * again later.
                 */
                buffer.position(start);
                messageQueue.add(packet);
                LOGGER.warn(exception);
            }
        }
    }


    /**
     * Writes all data from the Connection's write buffer into the socket.
     */
    public void send() throws IOException {
        if (!isConnected()) {
            throw new SocketException("Connection is closed.");
        }

        try {
            synchronized (getOutput()) {
                ByteBuffer buffer = getOutput().getByteBuffer();

                buffer.flip();
                while (buffer.hasRemaining()) {
                    if (socket.write(getOutput().getByteBuffer()) == 0) {
                        break;
                    }
                }
                buffer.compact();
                getOutput().setBuffer(buffer, bufferLimit());
                
                /*
                 * We do not write to the socket after filling it with data. This is intentional
                 * to attempt to limit the amount of time here to prevent other Connections from
                 * not getting called.
                 */
                while (true) {
                    AbstractPacket packet = messageQueue.peek();

                    if (packet == null) {
                        break;
                    }
                    final int start = buffer.position();

                    try {
                        writeInner(packet, start);
                        messageQueue.poll();
                    } catch (KryoBufferOverflowException exception) {
                        getOutput().setPosition(start);
                        /*
                         * If the entire buffer is available but we have a buffer overflow the
                         * packet cannot fit in the buffer. If the packet is not removed from the
                         * queue this will infinately loop.
                         *
                         * If start is not 0 then its possible once send() is called again we might
                         * have enough space to serialize the packet.
                         */
                        if (start == 0) {
                            LOGGER.error("Unable to send packet, packet '{}' to big", packet.getClass().getName());
                            LOGGER.catching(exception);
                            messageQueue.poll();
                        } else {
                            LOGGER.warn(exception);
                            break;
                        }
                    }
                }

                /*
                 * We know we have nothing more to write when buffer.position() == 0 because:
                 * 1. buffer.compact() sets the position to the number of bytes it has written.
                 * 2. writeInner is not called because our messageQueue is empty. 
                 */
                if (buffer.position() == 0) {
                    /*
                     * If we had to allocate more memory than normal, reset it to the normal amount
                     * to prevent potentially having large amounts of memory allocated here.
                     */
                    if (output.getByteBuffer().capacity() == bufferCapacity()) {
                        output = new ByteBufferOutput(bufferCapacity(), bufferLimit());
                    }
                    socketKey.interestOps(SelectionKey.OP_READ);
                }
            }
        } catch (IOException exception) {
            LOGGER.error("Unable to send packet", exception);
            close();
        }
    }

    /**
     * Reads from the socket into the {@link Connection}'s read buffer. Every full packet found
     * will be deserialized into an AbstractPacket via {@link #readObject}.
     */
    public void read(ConnectionHandler handler) throws IOException {
        if (!isConnected()) {
            throw new SocketException("Connection is closed.");
        }

        try  {
            synchronized (getInput()) {
                ByteBuffer buffer = getInput().getByteBuffer();
                int bytesRead = socket.read(buffer);

                if (bytesRead == -1) {
                    close(); 
                    return;
                }

                buffer.flip();
                getInput().setBuffer(buffer);
                LOGGER.debug("reading {} bytes", buffer.limit());
                AbstractPacket packet = readObject(handler);
                while (packet != null) {
                    handler.processPacket(packet, this);
                    packet = readObject(handler);
                }
                buffer.compact();

                // Downsize the ByteBufferInput back to bufferCapacity() to save memory.
                if (buffer.position() == 0 && buffer.capacity() > bufferCapacity()) {
                    ByteBuffer newBuffer = ByteBuffer.allocateDirect(bufferCapacity());

                    /*
                     * We don't need to copy the bytes as buffer.position() == 0 tells us there is
                     * nothing more to read.
                     */
                    newBuffer.order(buffer.order());
                    input.setBuffer(newBuffer);
                }
            }
        } catch (IOException exception) {
            close();
        }
    }

    public void heartbeat() {
        nextHeartbeat = System.currentTimeMillis() + HEARTBEAT_INTERVAL;
    }

    public void serverHeartbeat() {
        nextHeartbeat = System.currentTimeMillis() + HEARTBEAT_TIMEOUT;
    }

    public long getNextHeartbeat() {
        return nextHeartbeat;
    }

    protected ByteBufferInput getInput() {
        return input;
    }

    protected ByteBufferOutput getOutput() {
        return output;
    }

    protected ConcurrentLinkedQueue<AbstractPacket> getMessageQueue() {
        return messageQueue;
    }

    protected int bufferCapacity() {
        return 1024 * 4; // 4KB.
    }

    protected int bufferLimit() {
        return 1024 * 1024 * 20; //20 MB.
    }

    /**
     * Write helper method decoupled in order to allow callee's determine how to catch the
     * {@link KryoBufferOverFlowException} if thrown.
     *
     * @param packet The packet to write.
     *
     * @param start Initial bytebuffer position.
     *
     * @throws KryoBufferOverFlowException When Output has no more space available to write to.
     */
    protected void writeInner(AbstractPacket packet, int start) throws KryoBufferOverflowException {
        output.writeInt(0); //Leave space for packet length
        output.writeInt(packet.getType().getType(), 2);
        output.writeBoolean(packet.getType().isSystemPacket());
        kryos.get().writeObject(output, packet);
        final int end = output.position();

        output.setPosition(start);
        final int length = end - start - PacketHeader.SIZE;
        // Don't count the packet type or length ints.
        output.writeInt(length);
        output.setPosition(end);
        LOGGER.debug("writing {} bytes {}", length + PacketHeader.SIZE, packet);
    }

    /**
     * Write helper method decoupled in order to allow callee's determine how to catch the
     * {@link KryoBufferOverFlowException} if thrown.
     *
     * @param packet The packet to write.
     *
     * @param start Initial bytebuffer position.
     *
     * @throws KryoBufferOverFlowException When Output has no more space available to write to.
     */
    protected void writeInner(AbstractPacket packet, int start) throws KryoBufferOverflowException {
        ByteBuffer buffer = getOutput().getByteBuffer();

        getOutput().setBuffer(buffer, bufferLimit());
        output.writeInt(0); //Leave space for packet length
        output.writeInt(packet.getType().getType(), 2);
        output.writeBoolean(packet.getType().isSystemPacket());
        kryos.get().writeObject(output, packet);
        final int end = output.position();

        output.setPosition(start);
        final int length = end - start - PacketHeader.SIZE;
        // Don't count the packet type or length ints.
        output.writeInt(length);
        output.setPosition(end);
        LOGGER.debug("writing {} bytes {}", length + PacketHeader.SIZE, packet);
    }

    /**
     * Deserializes an AbstractPacket from the socket.
     *
     * @return null if there are not enough bytes to create a PacketHeader, there are not enough
     * bytes to deserialize the {@link AbstractPacket}, or the packet type is invalid.
     * Otherwise return the serialized {@link AbstractPacket}.
     *
     * @throws IOException When the AbstractPacket cannot fit into the input buffer.
     */
    protected AbstractPacket readObject(ConnectionHandler handler) throws IOException {
        ByteBuffer buffer = getInput().getByteBuffer();

        // If we have a packetState then we know we are in the middle of reading a packet.
        if (packetState == null) {
            if (buffer.remaining() < PacketHeader.SIZE) {
                return null;
            }

            packetState = new PacketHeader(getInput());
            int totalPacketSize = packetState.getLength() + PacketHeader.SIZE;

            if (totalPacketSize > buffer.capacity()) {
                ByteBuffer newBuffer = ByteBuffer.allocateDirect(totalPacketSize);

                newBuffer.put(buffer);
                newBuffer.order(buffer.order());
                input.setBuffer(newBuffer);
                /*
                 * We don't have the entire packet if we have to resize the buffer to fit the
                 * packet.
                 */
                return null;
            }
        }

        // Check to see if we have read the entire packet.
        if (buffer.remaining() < packetState.getLength()) {
            return null;
        }

        AbstractPacket.PacketType packetType = null;
        
        try {
            if (packetState.isSystemPacket()) {
                packetType = SystemPacketType.fromInteger(packetState.getType());
            } else {
                packetType = handler.getPacketType(packetState.getType());
            }
        } catch (InvalidPacketException exception) {
            LOGGER.error("Unable to find packet type: '{}'", packetState.getType());
            LOGGER.catching(exception);
            packetState = null;
            return null;
        } 
        // Completed reading the packet reset the packetState.
        packetState = null;
        LOGGER.debug("reading packet class: {}", packetType.getPacketClass());
        return kryos.get().readObject(
          getInput(),
          packetType.getPacketClass()
        );
    }
}
