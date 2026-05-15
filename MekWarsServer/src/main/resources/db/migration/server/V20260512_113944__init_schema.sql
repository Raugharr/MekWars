-- Migration: init_schema
-- Created: Tue May 12 11:39:44 CDT 2026

 	
ALTER TABLE player ADD COLUMN fluff_text TEXT;
ALTER TABLE player ADD COLUMN last_isp TEXT;
ALTER TABLE player ADD COLUMN xp_until_reward INTEGER NOT NULL;
ALTER TABLE player ADD COLUMN xp_until_flu INTEGER NOT NULL;
ALTER TABLE player ADD COLUMN group_allowance INTEGER NOT NULL;
ALTER TABLE player ADD COLUMN bays_owned INTEGER NOT NULL;
ALTER TABLE player ADD COLUMN last_online INTEGER;
ALTER TABLE player ADD COLUMN multi_campaign_status INTEGER NOT NULL;
ALTER TABLE player ADD COLUMN discord_id TEXT;
ALTER TABLE player ADD COLUMN scraps_this_tick INTEGER NOT NULL;
ALTER TABLE player ADD COLUMN donations_this_tick INTEGER NOT NULL;
ALTER TABLE player ADD COLUMN weighted_army_number INTEGER NOT NULL;
ALTER TABLE player ADD COLUMN last_time_command_sent INTEGER NOT NULL;
ALTER TABLE player ADD COLUMN last_attack_from_reserve INTEGER NOT NULL;
ALTER TABLE player ADD COLUMN active_since INTEGER NOT NULL;
ALTER TABLE player ADD COLUMN attack_restriction_until INTEGER NOT NULL;
ALTER TABLE player ADD COLUMN selling_to_id INTEGER NOT NULL;
ALTER TABLE player ADD COLUMN client_version TEXT NOT NULL;
ALTER TABLE player ADD COLUMN password TEXT NOT NULL;
ALTER TABLE player ADD COLUMN user_validated INTEGER NOT NULL;
ALTER TABLE player ADD COLUMN last_promoted INTEGER NOT NULL;
ALTER TABLE player ADD COLUMN leech_count INTEGER NOT NULL;
ALTER TABLE player ADD COLUMN status INTEGER NOT NULL;
