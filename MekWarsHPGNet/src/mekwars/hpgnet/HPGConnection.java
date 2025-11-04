/*
 * MekWars - Copyright (C) 2018
 * 
 * Original author - Bob Eldred (spork@mekwars.org)  
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

package mekwars.hpgnet;

import com.esotericsoftware.kryo.Kryo;
import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import mekwars.common.net.Connection;
import mekwars.common.net.hpgnet.packets.ServerRegister;
import mekwars.common.net.hpgnet.packets.ServerUpdate;

public class HPGConnection extends Connection {
    private HPGSubscriber subscriber;

    public HPGConnection(ThreadLocal<Kryo> kryos, int inputLen, int outputLen) {
        super(kryos, inputLen, outputLen);
    }

    public HPGConnection(ThreadLocal<Kryo> kryos, SocketChannel socket,
            SelectionKey socketKey, int inputLen, int outputLen) {
        super(kryos, socket, socketKey, inputLen, outputLen);
    }

    public ServerRegister getServerRegister() {
        if (subscriber == null) {
            return null;
        }

        return subscriber.toServerRegister();
    }

    public ServerUpdate getServerUpdate() {
        if (subscriber == null) {
            return null;
        }
        return subscriber.toServerUpdate();
    }
}
