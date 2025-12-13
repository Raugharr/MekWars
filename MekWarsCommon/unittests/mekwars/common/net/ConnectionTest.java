/*
 * MekWars - Copyright (C) 2025 
 * 
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet)
 * Original author Helge Richter (McWizard)
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ConnectionTest {
    @Mock
    private SocketChannel channel;

    @Mock
    private SelectionKey key;

    private ThreadLocal<Kryo> kryos;
    private Connection connection;

    @BeforeEach
    void init() {
        kryos = ThreadLocal.withInitial(() -> {
            Kryo kryo = new Kryo();
        
            kryo.register(MockPacket.class);
            return kryo;
        });
        connection = spy(new Connection(kryos, channel, key, 1024, 1024));
    }

    @Test
    public void sendWhenNotConnectedTest() {
        when(channel.isConnected()).thenReturn(false);
        assertThrows(SocketException.class, () -> {
            connection.send();
        });
    }

    @Nested
    public class WriteOnlyTest {
        @Test
        public void writePacketTest() {
            when(key.isWritable()).thenReturn(false);
            when(channel.isConnected()).thenReturn(true);
            assertDoesNotThrow(() -> {
                connection.write(new MockPacket());
            });

            ByteBuffer byteBuffer = connection.getOutput().getByteBuffer();

            assertEquals(PacketHeader.SIZE + 3, byteBuffer.position());
            // byteBuffer.flip();
            // // assertEquals(, byteBuffer.getShort());
            // assertEquals(3, byteBuffer.getInt());
            // assertEquals(MockPacketType.MOCK_PACKET.getType(), byteBuffer.getInt());
            // assertEquals(0, byteBuffer.get());
        }

        @Test
        public void sendFailsTest() {
            when(key.isWritable()).thenReturn(false);
            when(channel.isConnected()).thenReturn(true);

            assertDoesNotThrow(() -> {
                connection.write(new MockPacket());
                when(channel.write(any(ByteBuffer.class))).thenThrow(new ClosedChannelException());
                connection.send();
            });

            verify(connection).close();
        }

        @Test
        public void writeThenReadTest() {
            when(key.isWritable()).thenReturn(false);
            when(channel.isConnected()).thenReturn(true);
            assertDoesNotThrow(() -> {
                connection.write(new MockPacket());
            });

            verify(key).interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            assertDoesNotThrow(() -> {
                verify(connection, never()).send();
            });
        }

        @Test
        public void writeAndReadTest() {
            when(key.isWritable()).thenReturn(true);
            when(channel.isConnected()).thenReturn(true);
            assertDoesNotThrow(() -> {
                connection.write(new MockPacket());
            });

            verify(key).interestOps(SelectionKey.OP_READ);
            assertDoesNotThrow(() -> {
                verify(connection).send();
            });
        }
    }
    
    @Nested
    public class ReadOnlyTest {
        @Mock
        private ConnectionHandler handler;

        private MockPacket packet;
        private ByteBufferInput input;
        private ByteBufferOutput output; 
        private PacketHeader header;

        @BeforeEach
        void setup() {
            packet = new MockPacket();
            input = connection.getInput();
            output = new ByteBufferOutput(input.getByteBuffer()); 
            header = new PacketHeader(3, (short) packet.getId().getType(), false);
        }

        @Test
        public void readObjectTest() {
            ByteBuffer buffer = output.getByteBuffer();
            output.setBuffer(buffer);
            header.write(output);
            kryos.get().writeObject(output, new MockPacket());
            buffer.flip();
            input.setBuffer(buffer);
            assertEquals(PacketHeader.SIZE + 3, connection.getInput().getByteBuffer().limit());

            assertDoesNotThrow(() -> {
                when(handler.getPacketType(any(Integer.class)))
                    .thenReturn(MockPacketType.MOCK_PACKET);

                AbstractPacket readPacket = connection.readObject(handler);
                assertInstanceOf(MockPacket.class, readPacket);

                readPacket = connection.readObject(handler);
                assertNull(readPacket);
            });
        }

        @Test
        public void noPacketHeaderTest() {
            ByteBuffer buffer = output.getByteBuffer();
            output.setBuffer(buffer);
            buffer.flip();
            input.setBuffer(buffer);
            assertDoesNotThrow(() -> {
                assertNull(connection.readObject(handler));
            });
        }

        @Test
        public void noPacketBodyTest() {
            ByteBuffer buffer = output.getByteBuffer();
            output.setBuffer(buffer);
            header.write(output);
            buffer.flip();
            input.setBuffer(buffer);
            assertEquals(PacketHeader.SIZE, connection.getInput().getByteBuffer().limit());

            assertDoesNotThrow(() -> {
                assertNull(connection.readObject(handler));
            });
        }

        @Test
        public void readClosedSocketTest() {
            ByteBuffer buffer = output.getByteBuffer();
            output.setBuffer(buffer);

            when(channel.isConnected()).thenReturn(true);
            assertDoesNotThrow(() -> {
                when(channel.read(buffer)).thenThrow(ClosedChannelException.class);
                connection.read(handler);
                verify(connection).close();
            });
        }
    }
}
