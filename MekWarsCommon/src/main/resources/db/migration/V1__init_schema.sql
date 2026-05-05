CREATE TABLE sync_log (
	id         INTEGER PRIMARY KEY AUTOINCREMENT,
	entity_id  INTEGER NOT NULL,
	table_name TEXT NOT NULL,
	operation  INTEGER NOT NULL,
	updated_at TEXT NOT NULL
);

CREATE UNIQUE INDEX sync_log_entity_id_table_name_index ON sync_log(entity_id, table_name);
