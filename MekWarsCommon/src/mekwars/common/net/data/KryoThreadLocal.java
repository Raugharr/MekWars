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

package mekwars.common.net.data;

import com.esotericsoftware.kryo.Kryo;
import mekwars.common.net.KryoUtil;

public class KryoThreadLocal extends ThreadLocal<Kryo> {
    @Override
    protected Kryo initialValue() {
        Kryo kryo = new Kryo();

        DataKryoUtil.register(kryo);
        return kryo;
    }
}
