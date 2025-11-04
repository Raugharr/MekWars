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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.esotericsoftware.kryo.Kryo;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ClientTest {
    @Mock
    private Selector selector;

    @Mock
    private SocketChannel socketChannel;

    @Mock
    private SelectionKey selectionKey;

    @Mock
    private Connection connection;

    @Mock ThreadLocal<Kryo> kryos;

    @Test
    public void connectTest() {
        Client client = mock(CALLS_REAL_METHODS);

        MockedStatic<Selector> selectorClass = mockStatic(Selector.class);
        selectorClass.when(Selector::open).thenReturn(selector);

        MockedStatic<SocketChannel> socketClass = mockStatic(SocketChannel.class);
        socketClass.when(() -> SocketChannel.open(any(InetSocketAddress.class)))
            .thenReturn(socketChannel);

        assertDoesNotThrow(() -> {
            when(socketChannel.register(any(Selector.class), anyInt())).thenReturn(selectionKey);
            InetSocketAddress address = new InetSocketAddress("localhost", 1234);

            when(connection.getIpAddress()).thenReturn("localhost");
            when(connection.getPort()).thenReturn(1234);
            when(client.getKryos()).thenReturn(kryos);
            when(client.createConnection(
                any(ThreadLocal.class),
                any(SocketChannel.class),
                any(SelectionKey.class),
                anyInt(),
                anyInt()
            )).thenReturn(connection);

            client.connect(address);
            verify(client).createConnection(
                any(ThreadLocal.class),
                eq(socketChannel),
                eq(selectionKey),
                anyInt(),
                anyInt()
            );
            verify(selectionKey).attach(eq(connection));
        });
    }
}
