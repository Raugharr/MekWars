-- Migration: init_schema
-- Created: Tue May 12 11:39:44 CDT 2026

CREATE TABLE IF NOT EXISTS exclusion_list (
	id INTEGER PRIMARY KEY AUTOINCREMENT
);

CREATE TABLE IF NOT EXISTS player_excludes (
	exclusion_list_id INTEGER NOT NULL,
	player_name TEXT NOT NULL,
	FOREIGN KEY(exclusion_list_id) REFERENCES exclusion_list(id)
);

CREATE UNIQUE INDEX player_excludes_exclusion_list_id_index ON player_excludes(exclusion_list_id);

CREATE TABLE IF NOT EXISTS admin_excludes (
	exclusion_list_id INTEGER NOT NULL,
	player_name TEXT NOT NULL,
	FOREIGN KEY(exclusion_list_id) REFERENCES exclusion_list(id)
);

CREATE UNIQUE INDEX admin_excludes_eclusion_list_id_index ON admin_excludes(exclusion_list_id);

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
ALTER TABLE player ADD COLUMN exclusion_list_id INTEGER NOT NULL REFERENCES exclusion_list(id);

CREATE UNIQUE INDEX player_exclusion_list_id_index ON player(exclusion_list_id);

CREATE TABLE IF NOT EXISTS army_opponents (
    army_id INTEGER NOT NULL,
    opponent_army_id INTEGER NOT NULL,
    PRIMARY KEY (army_id, opponent_army_id),
    FOREIGN KEY (army_id) REFERENCES army(id),
    FOREIGN KEY (opponent_army_id) REFERENCES army(id)
);

ALTER TABLE house ADD COLUMN money INTEGER NOT NULL;
ALTER TABLE house ADD COLUMN bays_provided INTEGER NOT NULL;
ALTER TABLE house ADD COLUMN component_production INTEGER NOT NULL;
ALTER TABLE house ADD COLUMN show_production_count_next INTEGER NOT NULL;
ALTER TABLE house ADD COLUMN initial_house_ranking INTEGER NOT NULL;
ALTER TABLE house ADD COLUMN motd TEXT NOT NULL;
ALTER TABLE house ADD COLUMN announcement TEXT NOT NULL;
ALTER TABLE house ADD COLUMN tech_research_points INTEGER NOT NULL;
ALTER TABLE house ADD COLUMN activity_pp REAL NOT NULL;
