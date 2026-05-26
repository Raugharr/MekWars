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

import mekwars.common.entities.MWEntity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

public class NamedMWEntityStore<T extends MWEntity> extends MWEntityStore<T> {
    private static final Logger LOGGER = LogManager.getLogger(NamedMWEntityStore.class);

    protected SortedMap<String, Integer> entityNames =
            Collections.synchronizedSortedMap(new TreeMap());

    /**
     * Adds an MWEntity to the MWEntityStore.
     *
     * @param entity The {@link MWEntity} to add to the {@link MWEntityStore}.
     */
    public void put(T entity) {
        LOGGER.info("Adding {}: '{}'", entity.getClass().getSimpleName(), entity.getName());
        if (entity.getId() == UNSET_ID) {
            entity.setId(nextId());
        }

        entities.put(entity.getId(), entity);
        String entityName = entity.getName().toLowerCase();
        if (entityNames.containsKey(entityName)) {
            LOGGER.error("MWEntity {} already exists", entityName);
            throw new IllegalArgumentException();
        }
        entityNames.put(entityName, entity.getId());
    }

    /**
     * Gets an entity by their name.
     *
     * @param name The name of the {@link MWEntity} to retrieve.
     * @return The {@link MWEntity} of the given name.
     */
    public T getByName(String name) {
        if (name == null) {
            return null;
        }

        Integer id = entityNames.get(name.toLowerCase());

        if (id == null) {
            LOGGER.warn("Unable to find '{}'", name.toLowerCase());
            return null;
        }
        return get(id);
    }

    /**
     * Removes an entity by their id.
     *
     * @param id The id of the {@link MWEntity} to remove.
     * @return The {@link MWEntity} that is associated with the given id.
     */
    public T remove(int id) {
        T entity = get(id);

        if (entity == null) {
            return null;
        }
        entityNames.remove(entity.getName().toLowerCase());
        return entities.remove(id);
    }

    /** Removes all entities from the MWEntityStore. */
    public void clear() {
        super.clear();
        entityNames.clear();
    }
}
