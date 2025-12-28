/*
 * MekWars - Copyright (C) 2025
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */

package mekwars.common.serializers;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import mekwars.common.entities.Entity;

/**
 * Serializes an Entity using only its id, saving space but forcing the Object to be sent
 * beforehand. The class type is not writen as the reader should know what class to be expecting.
 */
public abstract class IdSerializer extends Serializer<Entity> {
    private Class elementClass;

    /**
     * @param elementClass The class the {@link EntityStore} holds.
     */
    public IdSerializer(Class elementClass) {
        this.elementClass = elementClass;
    }

    public void write(Kryo kryo, Output output, Entity entity) {
        output.writeInt(entity.getId());
    }

    public Entity read(Kryo kryo, Input input, Class<? extends Entity> type) {
        return getId(input.readInt());
    }

    abstract protected Entity getId(int id);
}
