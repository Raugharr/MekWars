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
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import mekwars.common.net.packets.PacketType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private ArrayList<Listener> listeners;

    public Connection(ThreadLocal<Kryo> kryos, int inputLen, int outputLen) {
        this.kryos = kryos;
        this.input = new ByteBufferInput(inputLen);
        this.output = new ByteBufferOutput(outputLen);
        this.listeners = new ArrayList<Listener>();
    }

    public Connection(ThreadLocal<Kryo> kryos, SocketChannel socket, SelectionKey socketKey,
            int inputLen, int outputLen) {
        this(kryos, inputLen, outputLen);
        this.socket = socket;
        this.socketKey = socketKey;
    }

    public boolean isConnected() {
        return socket.isConnected();
    }

    /*
     * Connects to the the provided address.
     * @throws IOException
     */
    public void connect(InetSocketAddress address) throws IOException {
        if (!isConnected()) {
            socket = SocketChannel.open(address);
        }
    }

    public void close() {
        if (isConnected()) {
            output.close();
            input.close();
            try {
                socket.close();
            } catch (IOException exception) {
                LOGGER.error("Unable to close Connection", exception);
            }

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

    public ByteBufferInput getInput() {
        return input;
    }

    public ByteBufferOutput getOutput() {
        return output;
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    /*
     * Serializes the given packet and writes the binary representation into the Connection's
     * write buffer. Declares to the socket channel that a write is ready to be performed.
     * In order for the packet to be send to the socket, send must be invoked.
     */
    public void write(AbstractPacket packet) throws IOException {
        if (!isConnected()) {
            throw new SocketException("Connection is closed.");
        }
        synchronized (getOutput()) {
            ByteBuffer buffer = getOutput().getByteBuffer();

            getOutput().setBuffer(buffer);
            final int start = buffer.position();
            output.writeInt(0); //Leave space for packet length
            output.writeInt(packet.getId().getType(), 2);
            output.writeBoolean(packet.isSystemPacket());
            kryos.get().writeObject(output, packet);
            final int end = buffer.position();
            LOGGER.debug("writing {} bytes {}", buffer.position(), packet);

            buffer.position(start);
            // Don't count the packet type or length ints.
            output.writeInt(end - start - PacketHeader.SIZE);
            buffer.position(end);
            socketKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }
        if (socketKey.isWritable()) {
            send();
        }
    }


    /*
     * Writes all data from the Connection's write buffer into the socket.
     */
    public void send() throws IOException {
        if (!isConnected()) {
            throw new SocketException("Connection is closed.");
        }

        try {
            synchronized (getOutput()) {
                ByteBuffer buffer = getOutput().getByteBuffer();

                socketKey.interestOps(SelectionKey.OP_READ);
                buffer.flip();
                while (buffer.hasRemaining()) {
                    if (socket.write(getOutput().getByteBuffer()) == 0) {
                        socketKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        break;
                    }
                }
                buffer.compact();
            }
        } catch (IOException exception) {
            LOGGER.error("Unable to send packet", exception);
            close();
        }
    }

    /*
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

                getInput().setBuffer(buffer);
                buffer.flip();
                LOGGER.debug("reading {} bytes", buffer.limit());
                AbstractPacket packet = readObject(handler);
                while (packet != null) {
                    handler.processPacket(packet, this);
                    if (buffer.remaining() <= PacketHeader.SIZE) {
                        break;
                    }
                    packet = readObject(handler);
                }
                buffer.compact();
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

    public void onConnect() {
        id = NEXT_ID;
        NEXT_ID++;
        for (Listener listener : listeners) {
            listener.connected(this);
        }
    }

    /*
     * Deserializes an AbstractPacket from the socket.
     * @return null if there are not enough bytes to create a PacketHeader, there are not enough
     * bytes to deserialize the {@link AbstractPacket}, or the packet type is invalid.
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

            if (packetState.getLength() > buffer.capacity()) {
                int length = packetState.getLength();
                packetState = null;
                throw new IOException("Packet length '" + length + "' exceeds buffer capacity '" + buffer.capacity() + "'");
            }
        }

        if (buffer.remaining() < packetState.getLength()) {
            return null;
        }

        AbstractPacket.Type packetType = null;
        
        try {
            if (packetState.isSystemPacket()) {
                packetType = PacketType.fromInteger(packetState.getType());
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
