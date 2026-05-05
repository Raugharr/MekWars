/*
 * MekWars - Copyright (C) 2026
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 */

package mekwars.common.sync;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;

import java.time.ZonedDateTime;

public class SyncLogListener
        implements PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener {

    @Override
    public void onPostInsert(PostInsertEvent event) {
        if (event.getEntity() instanceof SyncEntity) {
            SyncEntity entity = (SyncEntity) event.getEntity();

            writeSyncLog(
                    event.getSession(),
                    entity.getId(),
                    event.getEntity().getClass().getSimpleName(),
                    SyncEntity.Action.INSERT.getValue());
        }
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        if (event.getEntity() instanceof SyncEntity) {
            SyncEntity entity = (SyncEntity) event.getEntity();

            writeSyncLog(
                    event.getSession(),
                    entity.getId(),
                    event.getEntity().getClass().getSimpleName(),
                    SyncEntity.Action.UPDATE.getValue());
        }
    }

    @Override
    public void onPostDelete(PostDeleteEvent event) {
        if (event.getEntity() instanceof SyncEntity) {
            SyncEntity entity = (SyncEntity) event.getEntity();

            writeSyncLog(
                    event.getSession(),
                    entity.getId(),
                    event.getEntity().getClass().getSimpleName(),
                    SyncEntity.Action.DELETE.getValue());
        }
    }

    @Override
    public boolean requiresPostCommitHandling(EntityPersister persister) {
        return false;
    }

    private void writeSyncLog(
            SharedSessionContractImplementor session, int id, String table, int op) {
        String statement =
                "INSERT INTO sync_log (entity_id, table_name, operation, updated_at) "
                        + "VALUES (?, ?, ?, ?) "
                        + "ON CONFLICT(entity_id, table_name) DO UPDATE SET "
                        + "id = excluded.id, "
                        + "operation = excluded.operation, "
                        + "updated_at = excluded.updated_at";
        session.doWork(
                conn -> {
                    try (var ps = conn.prepareStatement(statement)) {
                        ps.setInt(1, id);
                        ps.setInt(2, op);
                        ps.setString(3, ZonedDateTime.now().toString());
                        ps.executeUpdate();
                    }
                });
    }
}
