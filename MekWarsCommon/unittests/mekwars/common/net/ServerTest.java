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
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.PriorityQueue;
import mekwars.common.test.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ServerTest {
    @Mock
    private ServerSocketChannel serverSocket;

    @Mock
    private ConnectionListener connectionListener;

    private Connection connection;

    @Mock
    private SelectionKey key;

    @Mock
    private Selector selector;

    @Mock
    private ThreadLocal<Kryo> kryos;

    @Mock
    private SelectionKey clientSelectionKey;

    @Mock
    private ServerSocketChannel serverSocketChannel;

    @Mock
    private SocketChannel socketChannel;

    @Spy
    private Server server;

    @BeforeEach
    void beforeEach() {
        connection = spy(new Connection(kryos, 1024, 1024));
    }

    @Test
    public void closeTest() {
        ArrayList<Connection> connections = new ArrayList<Connection>();
        connections.add(connection);
        TestUtils.setField(Server.class, server, "connections", connections);
        doNothing().when(connection).close();
        assertDoesNotThrow(() -> {
            assertEquals(1, server.connectedClients());
            server.close();
            assertEquals(0, server.connectedClients());
        });
    }

    @Test
    public void acceptConnectionTest() {
        MockedStatic<SocketChannel> socketClass = mockStatic(SocketChannel.class);
        socketClass.when(() -> SocketChannel.open(any(InetSocketAddress.class)))
            .thenAnswer((address) -> {
                return socketChannel;
            });

        assertDoesNotThrow(() -> {
            doCallRealMethod().when(server).acceptConnection(any(SelectionKey.class));
            when(key.channel()).thenReturn(serverSocketChannel);
            when(key.selector()).thenReturn(selector);
            when(serverSocketChannel.accept()).thenReturn(socketChannel);
            when(server.getKryos()).thenReturn(kryos);
            doReturn("localhost").when(connection).getIpAddress();

            when(socketChannel.register(any(Selector.class), anyInt()))
                .thenReturn(clientSelectionKey);

            when(server.createConnection(
                    any(ThreadLocal.class),
                    any(SocketChannel.class),
                    any(SelectionKey.class),
                    anyInt(),
                    anyInt()
            )).thenReturn(connection);
            server.acceptConnection(key);
        });

        assertEquals(1, server.connectedClients());
        assertEquals(connection, server.getConnections().next());
        verify(connection).serverHeartbeat();
        verify(connection).onConnect();
        socketClass.close();
    }

    @Nested
    public class ProcessHeartbeatsTest {
        private ArrayList<Connection> connections = new ArrayList<Connection>();

        @BeforeEach
        void beforeEach() {
            connections.add(connection);
            TestUtils.setField(Server.class, server, "connections", connections);
            PriorityQueue<Connection> connectionTimeoutQueue = (PriorityQueue<Connection>)
                TestUtils.getField(
                    Server.class,
                    server,
                    "connectionTimeoutQueue"
                );
            connectionTimeoutQueue.add(connection);
        }

        @Test
        public void heartbeatStaleTest() {
            doReturn(true).when(connection).isConnected();
            doNothing().when(connection).close();
            assertDoesNotThrow(() -> server.heartbeat());
            verify(connection).close();
        }

        @Test
        public void heartbeatFreshTest() {
            connection.serverHeartbeat();
            assertDoesNotThrow(() -> server.heartbeat());
            verify(server, never()).close();
        }
    }
}
