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

import java.util.ArrayList;
import java.util.List;

/*
 * Resolver that notifies all listeners of a message when recieved.
 */
public abstract class CallbackResolver<S extends AbstractPacket, T extends Connection>
    extends AbstractResolver<S, T> {

    private List<CallbackResolverListener> listeners;

    public CallbackResolver(ConnectionHandler handler) {
        super(handler);
        listeners = new ArrayList<CallbackResolverListener>();
    }

    protected void receive(S message, T connection) {
        for (CallbackResolverListener listener : listeners) {
            listener.resolverUpdate(message, connection);
        }
    }

    public void addListener(CallbackResolverListener listener) {
        listeners.add(listener);
    }

    public void removeListener(CallbackResolverListener listener) {
        listeners.remove(listener);
    }

    public void clearListeners() {
        listeners.clear();
    }
}
