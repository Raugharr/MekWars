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

import java.io.IOException;

/*
 * Base class that is capable of resolving a packet recieved by a {@link ConnectionHandler}
 */
public abstract class AbstractResolver<S extends AbstractPacket, T extends Connection> {
    private ConnectionHandler handler;

    public AbstractResolver(ConnectionHandler handler) {
        this.handler = handler;
    }

    /*
     * Determines if this Reosolver can resolve the given packet type.
     * @return true if the Resolver can resolve the packetType otherwise, false.
     */
    public abstract boolean canResolve(AbstractPacket.Type packetType);

    public ConnectionHandler getHandler() {
        return handler;
    }

    /*
     * Called when the {@link ConnectionHandler} has recieved a packet that can be resolved by this
     * class.
     */
    protected abstract void receive(S message, T connection) throws IOException;
}
