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
import java.util.ArrayList;

/**
 * Serializes an ArrayList as a "FixedArray" that is an array that contains no null references,
 * and every element is serialized as the same class.
 */
public class FixedArraySerializer extends Serializer<ArrayList> {
    private Serializer elementSerializer;
    private Class elementClass;

    public FixedArraySerializer(Class elementClass) {
        this.elementSerializer = null;
        this.elementClass = elementClass;
    }

    public FixedArraySerializer(Class elementClass, Serializer elementSerializer) {
        this.elementSerializer = elementSerializer;
        this.elementClass = elementClass;
    }

    public void write(Kryo kryo, Output output, ArrayList collection) {
        output.writeInt(collection.size());
        if (elementSerializer != null) {
            for (Object element : collection) {
                kryo.writeObject(output, element, elementSerializer);
            }
        } else {
            for (Object element : collection) {
                kryo.writeObject(output, element);
            }
        }
    }

    public Serializer getElementSerializer() {
        return elementSerializer;
    }

    public Class getElementClass() {
        return elementClass;
    }

    public ArrayList read(Kryo kryo, Input input, Class<? extends ArrayList> type) {
        ArrayList collection = new ArrayList();
        Serializer elementSerializer = this.elementSerializer;

        int length = input.readInt();
        if (elementSerializer != null) {
            for (int i = 0; i < length; ++i) {
                collection.add(kryo.readObject(input, elementClass, elementSerializer));
            }
        } else {
            for (int i = 0; i < length; ++i) {
                collection.add(kryo.readObject(input, elementClass));
            }
        }
        return collection;
    }
}
