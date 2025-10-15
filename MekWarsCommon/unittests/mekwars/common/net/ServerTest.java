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
import java.lang.reflect.Field;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ServerTest {
    @Mock
    private ServerSocketChannel serverSocket;

    @Mock
    private ConnectionListener connectionListener;

    @Mock
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

    @Mock
    private Server server;
    private ArrayList<Connection> connections;

    @BeforeEach
    void beforeEach() {
        connections = new ArrayList<Connection>();
        Field connectionsField = ReflectionSupport
          .findFields(Server.class, f -> f.getName().equals("connections"),
            HierarchyTraversalMode.TOP_DOWN)
          .get(0);

        connectionsField.setAccessible(true);
        assertDoesNotThrow(() -> {
            connectionsField.set(server, new ArrayList<Connection>());
        });
    }

    @Test
    public void closeTest() {
        connections.add(connection);
        doCallRealMethod().when(server).close();
        assertDoesNotThrow(() -> {
            server.close();
            assertEquals(0, server.connectedClients());
        });
    }

    @Test
    public void acceptConnectionTest() {
        assertDoesNotThrow(() -> {
            doCallRealMethod().when(server).acceptConnection(any(SelectionKey.class));
            when(server.connectedClients()).thenCallRealMethod();
            when(server.getConnections()).thenCallRealMethod();
            when(key.channel()).thenReturn(serverSocketChannel);
            when(key.selector()).thenReturn(selector);
            when(serverSocketChannel.accept()).thenReturn(socketChannel);
            when(server.getKryos()).thenReturn(kryos);
            when(connection.getIpAddress()).thenReturn("localhost");

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
    }
}
