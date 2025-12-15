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

package mekwars.common.persistence;

import java.util.Collection;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;
import mekwars.common.entities.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityStore<T extends Entity> {
    private static final Logger LOGGER = LogManager.getLogger(EntityStore.class);

    public static final int UNSET_ID = -1;

    protected SortedMap<Integer, T> entities = Collections.synchronizedSortedMap(new TreeMap());

    /**
     * Adds an Entity to the EntityStore.
     *
     * @param entity The {@link Entity} to add to the {@link EntityStore}.
     */
    public void put(T entity) {
        LOGGER.info("Adding {}: '{}'", entity.getClass().getSimpleName(), entity.getName());

        if (entity.getId() == UNSET_ID) {
            entity.setId(nextId());
        }
        entities.put(entity.getId(), entity);
    }

    /**
     * Gets an entity by their id.
     *
     * @param id The id of the {@link Entity} to retrieve.
     *
     * @return The {@link Entity} of the given id.
     */
    public T get(int id) {
        return entities.get(id);
    }

    /**
     * Removes an entity by their id.
     *
     * @param id The id of the {@link Entity} to remove.
     *
     * @return The {@link Entity} that is associated with the given id.
     */
    public T remove(int id) {
        return entities.remove(id);
    }

    /**
     * Removes all entities from the EntityStore.
     */
    public void clear() {
        entities.clear();
    }

    /**
     * Returns a list of all stores entities.
     *
     * @return A Collection of all {@link Entity entities}.
     */
    public Collection<T> values() {
        return entities.values();
    }

    /**
     * Returns the number of entities in the EntityStore. If the Entity store contains more than
     * Integer.MAX_VALUE elements, returns Integer.MAX_VALUE.
     *
     * @return The number of {@link Entity entities} in this {@link EntityStore}
     */
    public int size() {
        return entities.size();
    }

    /**
     * Returns the next available id free for an entity.
     *
     * @return The next available id free for an {@link Entity}.
     */
    protected int nextId() {
        return entities.keySet().stream().max(Integer::compare).orElse(0) + 1;
    }
}
