CREATE TABLE IF NOT EXISTS planet_environments (
	id INTEGER PRIMARY KEY AUTOINCREMENT
);

CREATE TABLE IF NOT EXISTS planet (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	x REAL NOT NULL,
	y REAL NOT NULL,
	dtype TEXT NOT NULL,
	planet_environments_id INTEGER,
	updated_at INTEGER NOT NULL,
	-- created_at TEXT NOT NULL,
	name TEXT NOT NULL,
	description TEXT NOT NULL DEFAULT "",
	bays_provided INTEGER NOT NULL DEFAULT 0,
	conquerable INTEGER NOT NULL DEFAULT 1,
	component_production INTEGER NOT NULL DEFAULT 0,
	min_planet_ownership INTEGER NOT NULL DEFAULT -1,
	homeworld INTEGER NOT NULL DEFAULT 0,
	original_owner TEXT NOT NULL,
	conquest_points INTEGER NOT NULL DEFAULT 100,
	FOREIGN KEY(planet_environments_id) REFERENCES planets_environments(id)
);

CREATE UNIQUE INDEX planet_name_index ON planet(name);
CREATE UNIQUE INDEX planet_planet_environments_id_index ON planet(planet_environments_id);

CREATE TABLE IF NOT EXISTS planet_influence (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	-- updated_at TEXT NOT NULL,
	-- created_at TEXT NOT NULL,
	influence INTEGER NOT NULL,
	planet_id INTEGER NOT NULL,
	house_id INTEGER NOT NULL,

	FOREIGN KEY(planet_id) REFERENCES planet(id),
	FOREIGN KEY(house_id) REFERENCES house(id)
);

CREATE UNIQUE INDEX planet_influence_planet_id_house_id_index ON planet_influence(planet_id, house_id);

CREATE TABLE IF NOT EXISTS planet_flag (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	-- updated_at TEXT NOT NULL,
	-- created_at TEXT NOT NULL,
	name TEXT NOT NULL,
	value TEXT NOT NULL,
	planet_id INTEGER NOT NULL,

	FOREIGN KEY(planet_id) REFERENCES planet(id)
);

CREATE UNIQUE INDEX planet_flag_planet_id_index ON planet_flag(planet_id);

CREATE TABLE IF NOT EXISTS planet_environment (
	id							INTEGER PRIMARY KEY AUTOINCREMENT,
	terrain_id					INTEGER, -- This should be uncommented sometime in this PR? NOT NULL,
	-- updated_at					TEXT	NOT NULL,
	-- created_at					TEXT	NOT NULL,
	name						TEXT	NOT NULL DEFAULT '',

	crater_probability			INTEGER NOT NULL DEFAULT 0,
	crater_minimum				INTEGER NOT NULL DEFAULT 0,
	crater_maximum				INTEGER NOT NULL DEFAULT 0,
	crater_min_radius			INTEGER NOT NULL DEFAULT 0,
	crater_max_radius			INTEGER NOT NULL DEFAULT 0,

	hillyness					INTEGER NOT NULL DEFAULT 100,
	hill_elevation_range		INTEGER NOT NULL DEFAULT 3,
	hill_invert_probability		INTEGER NOT NULL DEFAULT 0,

	water_min_spots				INTEGER NOT NULL DEFAULT 3,
	water_max_spots				INTEGER NOT NULL DEFAULT 8,
	water_min_hexes				INTEGER NOT NULL DEFAULT 2,
	water_max_hexes				INTEGER NOT NULL DEFAULT 10,
	water_deep_probability		INTEGER NOT NULL DEFAULT 20,

	forest_min_spots            INTEGER NOT NULL DEFAULT 4,
	forest_max_spots            INTEGER NOT NULL DEFAULT 8,
	forest_min_hexes            INTEGER NOT NULL DEFAULT 2,
	forest_max_hexes            INTEGER NOT NULL DEFAULT 6,
	forest_heavy_probability    INTEGER NOT NULL DEFAULT 20,
	forest_ultra_probability    INTEGER NOT NULL DEFAULT 0,

	rough_min_spots				INTEGER NOT NULL DEFAULT 0,
	rough_max_spots				INTEGER NOT NULL DEFAULT 5,
	rough_min_hexes				INTEGER NOT NULL DEFAULT 1,
	rough_max_hexes				INTEGER NOT NULL DEFAULT 2,
	rough_ultra_probability		INTEGER NOT NULL DEFAULT 0,

	swamp_min_spots				INTEGER NOT NULL DEFAULT 0,
	swamp_max_spots				INTEGER NOT NULL DEFAULT 0,
	swamp_min_hexes				INTEGER NOT NULL DEFAULT 0,
	swamp_max_hexes				INTEGER NOT NULL DEFAULT 0,

	pavement_min_spots			INTEGER NOT NULL DEFAULT 0,
	pavement_max_spots			INTEGER NOT NULL DEFAULT 0,
	pavement_min_hexes			INTEGER NOT NULL DEFAULT 0,
	pavement_max_hexes			INTEGER NOT NULL DEFAULT 0,

	ice_min_spots				INTEGER NOT NULL DEFAULT 0,
	ice_max_spots				INTEGER NOT NULL DEFAULT 0,
	ice_min_hexes				INTEGER NOT NULL DEFAULT 0,
	ice_max_hexes				INTEGER NOT NULL DEFAULT 0,

	rubble_min_spots			INTEGER NOT NULL DEFAULT 0,
	rubble_max_spots			INTEGER NOT NULL DEFAULT 0,
	rubble_min_hexes			INTEGER NOT NULL DEFAULT 0,
	rubble_max_hexes			INTEGER NOT NULL DEFAULT 0,
	rubble_ultra_probability	INTEGER NOT NULL DEFAULT 0,

	fortified_min_spots			INTEGER NOT NULL DEFAULT 0,
	fortified_max_spots			INTEGER NOT NULL DEFAULT 0,
	fortified_min_hexes			INTEGER NOT NULL DEFAULT 0,
	fortified_max_hexes			INTEGER NOT NULL DEFAULT 0,

	sand_min_spots				INTEGER NOT NULL DEFAULT 0,
	sand_max_spots				INTEGER NOT NULL DEFAULT 0,
	sand_min_hexes				INTEGER NOT NULL DEFAULT 0,
	sand_max_hexes				INTEGER NOT NULL DEFAULT 0,

	planted_field_min_spots		INTEGER NOT NULL DEFAULT 0,
	planted_field_max_spots		INTEGER NOT NULL DEFAULT 0,
	planted_field_min_hexes		INTEGER NOT NULL DEFAULT 0,
	planted_field_max_hexes		INTEGER NOT NULL DEFAULT 0,

	min_buildings				INTEGER NOT NULL DEFAULT 0,
	max_buildings				INTEGER NOT NULL DEFAULT 0,
	min_cf						INTEGER NOT NULL DEFAULT 0,
	max_cf						INTEGER NOT NULL DEFAULT 0,
	min_floors					INTEGER NOT NULL DEFAULT 0,
	max_floors					INTEGER NOT NULL DEFAULT 0,
	city_density				INTEGER NOT NULL DEFAULT 50,
	city_type					TEXT    NOT NULL DEFAULT 'NONE',
	roads						INTEGER NOT NULL DEFAULT 4,
	town_size					INTEGER NOT NULL DEFAULT 0,

	fx_mod						INTEGER NOT NULL DEFAULT 0,
	forest_fire_probability		INTEGER NOT NULL DEFAULT 0,
	freeze_probability			INTEGER NOT NULL DEFAULT 0,
	flood_probability			INTEGER NOT NULL DEFAULT 0,
	drought_probability			INTEGER NOT NULL DEFAULT 0,
	theme						TEXT    NOT NULL DEFAULT '',

	mount_peaks					INTEGER NOT NULL DEFAULT 0,
	mount_width_min				INTEGER NOT NULL DEFAULT 0,
	mount_width_max				INTEGER NOT NULL DEFAULT 0,
	mount_height_min			INTEGER NOT NULL DEFAULT 0,
	mount_height_max			INTEGER NOT NULL DEFAULT 0,
	mount_style					INTEGER NOT NULL DEFAULT 0,

	road_probability            INTEGER NOT NULL DEFAULT 25,
	river_probability           INTEGER NOT NULL DEFAULT 25,
	algorithm                   INTEGER NOT NULL DEFAULT 0,
	cliff_probability           INTEGER NOT NULL DEFAULT 0,
	invert_negative_terrain     INTEGER NOT NULL DEFAULT 0,
	environment_probability     INTEGER NOT NULL DEFAULT 1,

	static_map_name				TEXT    NOT NULL DEFAULT 'surprise',
	x_size						INTEGER NOT NULL DEFAULT -1,
	y_size						INTEGER NOT NULL DEFAULT -1,
	static_map					INTEGER NOT NULL DEFAULT 0,
	x_board_size				INTEGER NOT NULL DEFAULT -1,
	y_board_size				INTEGER NOT NULL DEFAULT -1
);

CREATE TABLE IF NOT EXISTS advanced_terrain (
	id                        INTEGER PRIMARY KEY AUTOINCREMENT,
	display_name              TEXT    NOT NULL DEFAULT 'none',
	name                      TEXT    NOT NULL DEFAULT 'none',

	low_temp                  INTEGER NOT NULL DEFAULT 25,
	high_temp                 INTEGER NOT NULL DEFAULT 25,
	gravity                   REAL    NOT NULL DEFAULT 1.0,
	vacuum                    INTEGER NOT NULL DEFAULT 0,
	dusk_chance               INTEGER NOT NULL DEFAULT 0,
	full_moon_chance          INTEGER NOT NULL DEFAULT 0,
	moonless_night_chance     INTEGER NOT NULL DEFAULT 0,
	pitch_black_chance        INTEGER NOT NULL DEFAULT 0,
	night_temp_mod            INTEGER NOT NULL DEFAULT 0,
	min_visibility            INTEGER NOT NULL DEFAULT 100,
	max_visibility            INTEGER NOT NULL DEFAULT 100,
	atmosphere                TEXT    NOT NULL DEFAULT 'STANDARD',

	light_rainfall_chance     INTEGER NOT NULL DEFAULT 0,
	moderate_rainfall_chance  INTEGER NOT NULL DEFAULT 0,
	heavy_rainfall_chance     INTEGER NOT NULL DEFAULT 0,
	down_pour_chance          INTEGER NOT NULL DEFAULT 0,

	light_snowfall_chance     INTEGER NOT NULL DEFAULT 0,
	moderate_snowfall_chance  INTEGER NOT NULL DEFAULT 0,
	heavy_snowfall_chance     INTEGER NOT NULL DEFAULT 0,
	sleet_chance              INTEGER NOT NULL DEFAULT 0,
	ice_storm_chance          INTEGER NOT NULL DEFAULT 0,
	light_hail_chance         INTEGER NOT NULL DEFAULT 0,
	heavy_hail_chance         INTEGER NOT NULL DEFAULT 0,

	light_wind_chance         INTEGER NOT NULL DEFAULT 0,
	moderate_wind_chance      INTEGER NOT NULL DEFAULT 0,
	strong_wind_chance        INTEGER NOT NULL DEFAULT 0,
	storm_wind_chance         INTEGER NOT NULL DEFAULT 0,
	tornado_f13_wind_chance   INTEGER NOT NULL DEFAULT 0,
	tornado_f4_wind_chance    INTEGER NOT NULL DEFAULT 0,

	light_fog_chance          INTEGER NOT NULL DEFAULT 0,
	heavy_fog_chance          INTEGER NOT NULL DEFAULT 0,

	emi_chance                INTEGER NOT NULL DEFAULT 0,

	light_conditions          TEXT    NOT NULL DEFAULT 'DAY',
	weather_conditions        TEXT    NOT NULL DEFAULT 'CLEAR',
	wind_strength             TEXT    NOT NULL DEFAULT 'CALM',
	wind_direction            TEXT    NOT NULL DEFAULT 'RANDOM',
	max_wind_strength         TEXT    NOT NULL DEFAULT 'TORNADO_F4',
	shifting_wind_direction   INTEGER NOT NULL DEFAULT 0,
	shifting_wind_strength    INTEGER NOT NULL DEFAULT 0,
	fog                       TEXT    NOT NULL DEFAULT 'FOG_NONE',
	temperature               INTEGER NOT NULL DEFAULT 25,
	emi                       TEXT    NOT NULL DEFAULT 'EMI_NONE',
	terrain_affected          INTEGER NOT NULL DEFAULT 1
);

CREATE UNIQUE INDEX planet_environment_name_index ON planet_environment(name);
CREATE UNIQUE INDEX planet_environment_terrain_id_index ON planet_environment(terrain_id);

CREATE TABLE IF NOT EXISTS terrain (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	name TEXT NOT NULL
	-- updated_at TEXT NOT NULL,
	-- created_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS continent (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	planet_id INTEGER,
	terrain_id INTEGER NOT NULL,
	advanced_terrain_id INTEGER NOT NULL,
	-- updated_at TEXT NOT NULL,
	-- created_at TEXT NOT NULL,
	FOREIGN KEY(planet_id) REFERENCES planet(id),
	FOREIGN KEY(terrain_id) REFERENCES terrain(id),
	FOREIGN KEY(advanced_terrain_id) REFERENCES advanced_terrain(id)
);

CREATE INDEX continent_planet_id_index ON continent(planet_id);
CREATE INDEX continent_terrain_id_index ON continent(terrain_id);
CREATE INDEX continent_advanced_terrain_id_index ON continent(advanced_terrain_id);

CREATE TABLE IF NOT EXISTS unit_factory (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	dtype TEXT NOT NULL,
	name TEXT NOT NULL,
	size TEXT NOT NULL,
	founder TEXT NOT NULL,
	ticks_until_refresh INTEGER NOT NULL,
	refresh_speed INTEGER NOT NULL,
	type INTEGER NOT NULL,
	-- factory_id TEXT NOT NULL,
	access_level INTEGER NOT NULL,
	build_table_folder TEXT NOT NULL,
	factory_locked INTEGER NOT NULL,
	planet_id INTEGER DEFAULT NULL, -- should be commented eventually NOT NULL,
	FOREIGN KEY(planet_id) REFERENCES planet(id)
);

CREATE INDEX unit_factory_planet_id_index ON unit_factory(planet_id);
