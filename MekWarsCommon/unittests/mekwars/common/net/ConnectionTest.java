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
import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import org.junit.jupiter.api.AfterEach;
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

    @Mock
    private Selector selector;

    private ThreadLocal<Kryo> kryos;
    private Connection connection;

    @BeforeEach
    void init() throws Exception {
        kryos = ThreadLocal.withInitial(() -> {
            Kryo kryo = new Kryo();
        
            kryo.register(MockPacket.class);
            return kryo;
        });
        when(channel.register(
            selector,
            SelectionKey.OP_READ
        )).thenReturn(key);
        connection = spy(new Connection(kryos));
        connection.connect(channel, selector);
    }

    @AfterEach
    void quit() throws Exception {
        if (connection.isConnected()) {
            connection.close();
        }
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
            when(channel.isConnected()).thenReturn(true);
            assertDoesNotThrow(() -> {
                connection.write(new MockPacket());
            });

            ByteBuffer byteBuffer = connection.getOutput().getByteBuffer();

            assertEquals(PacketHeader.SIZE + 3, byteBuffer.position());
        }

        @Test
        public void sendFailsTest() {
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
            when(channel.isConnected()).thenReturn(true);
            assertDoesNotThrow(() -> {
                connection.write(new MockPacket());
            });

            verify(key).interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            assertDoesNotThrow(() -> {
                verify(connection, never()).send();
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
            header = new PacketHeader(3, (short) packet.getType().getType(), false);
        }

        @Nested
        public class SmallBufferTest {
            @BeforeEach
            void init() throws Exception {
                // Make the buffer force to resize to fit a single packet.
                header = new PacketHeader(10, (short) packet.getType().getType(), false);
            }

            @Test
            public void enlargeBufferTest() throws Exception {
                output.setBuffer(ByteBuffer.allocateDirect(PacketHeader.SIZE));
                ByteBuffer buffer = output.getByteBuffer();

                header.write(output);

                buffer.flip();
                input.setBuffer(buffer);

                AbstractPacket readPacket = connection.readObject(handler);
                assertEquals(PacketHeader.SIZE + 10, connection.getInput().getByteBuffer().capacity());
                assertNull(readPacket);
            }

            @Test
            public void shrinkBufferTest() throws Exception {
                doReturn(null).when(connection).readObject(eq(handler));
                when(channel.isConnected()).thenReturn(true);
                input.setBuffer(ByteBuffer.allocateDirect(connection.bufferCapacity() + 1));

                connection.read(handler);
                assertEquals(connection.bufferCapacity(), connection.getInput().getByteBuffer().capacity());
            }
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

    @Nested
    public class SmallBufferTest {
        @BeforeEach
        void init() throws Exception {
            connection.getOutput().setBuffer(ByteBuffer.allocateDirect(PacketHeader.SIZE + 3));
            connection.getInput().setBuffer(ByteBuffer.allocateDirect(PacketHeader.SIZE + 3));
        }

        @Test
        public void resizeOutputBufferTest() throws IOException {
            when(connection.isConnected()).thenReturn(true);

            connection.write(new MockPacket());
            assertEquals(PacketHeader.SIZE + 3, connection.getOutput().position());

            connection.write(new MockPacket());
            assertEquals((PacketHeader.SIZE + 3) * 2, connection.getOutput().position());
        }

        @Test
        public void queueMessageTest() throws IOException {
            when(connection.bufferLimit())
                .thenReturn(PacketHeader.SIZE + 3);
            when(connection.isConnected()).thenReturn(true);
            connection.write(new MockPacket());
            when(channel.write(any(ByteBuffer.class)))
                .thenAnswer(input -> {
                   ByteBuffer byteBuffer = input.getArgument(0); 

                   int written = 0;
                   while (byteBuffer.position() < byteBuffer.limit()) {
                       written += 1;
                       byteBuffer.get();
                   }
                   return written;
                });

            assertEquals(PacketHeader.SIZE + 3, connection.getOutput().position());
            verify(key).interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);

            connection.write(new MockPacket());
            assertEquals(PacketHeader.SIZE + 3, connection.getOutput().position());
            assertEquals(1, connection.getMessageQueue().size());
            verify(key).interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);

            connection.write(new MockPacket());
            assertEquals(PacketHeader.SIZE + 3, connection.getOutput().position());
            assertEquals(2, connection.getMessageQueue().size());
            verify(key).interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);

            connection.send();
            assertEquals(10, connection.getOutput().position());
            assertEquals(1, connection.getMessageQueue().size());

            connection.send();
            assertEquals(10, connection.getOutput().position());
            assertEquals(0, connection.getMessageQueue().size());

            connection.send();
            assertEquals(0, connection.getOutput().position());
            assertEquals(0, connection.getMessageQueue().size());
            verify(key).interestOps(SelectionKey.OP_READ);
        }
    }
}
