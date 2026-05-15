-- Migration: init_schema
-- Created: Tue May 12 11:40:21 CDT 2026

ALTER TABLE player ADD COLUMN house TEXT NOT NULL;
ALTER TABLE player ADD COLUMN bays INTEGER NOT NULL;
ALTER TABLE player ADD COLUMN free_bays INTEGER NOT NULL;
ALTER TABLE player ADD COLUMN hanger_penalty INTEGER NOT NULL;
ALTER TABLE player ADD COLUMN hangar_purchase_penalties TEXT NOT NULL;
ALTER TABLE player ADD COLUMN repair_location INTEGER NOT NULL;
ALTER TABLE player ADD COLUMN repair_tech_type INTEGER NOT NULL;
ALTER TABLE player ADD COLUMN repair_retries INTEGER NOT NULL;
ALTER TABLE player ADD COLUMN conventional_mines_allowed INTEGER NOT NULL;
ALTER TABLE player ADD COLUMN vibra_mines_allowed INTEGER NOT NULL;
