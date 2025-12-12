/*
 * MekWars - Copyright (C) 2025
 * 
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet)
 * Original author Helge Richter (McWizard)
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

package mekwars.server.io;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import mekwars.common.io.AbstractFileSystem;

public class FileSystem extends AbstractFileSystem {
    protected static final Path DIRECTORY_OPERATIONS_PLANETS = FileSystems.getDefault()
        .getPath(DIRECTORY_NAME_DATA, "planets.xml");

    protected static final Path DIRECTORY_DATA_TERRAIN = FileSystems.getDefault()
        .getPath(DIRECTORY_NAME_DATA, "terrain.xml");

    protected static final Path DIRECTORY_DATA_ADVANCED_TERRAIN = FileSystems.getDefault()
        .getPath(DIRECTORY_NAME_DATA, "advancedTerrain.xml");

    protected static final Path DIRECTORY_DATA_SUPPORT_UNITS = FileSystems.getDefault()
        .getPath(DIRECTORY_NAME_DATA, "supportunits.txt");

    private static final String DIRECTORY_NAME_OPERATIONS = DIRECTORY_NAME_DATA + "operations/";
    private static final Path DIRECTORY_OPERATIONS = FileSystems.getDefault()
        .getPath(DIRECTORY_NAME_OPERATIONS);

    protected static final String DIRECTORY_NAME_OPERATIONS_SHORT = DIRECTORY_NAME_OPERATIONS
        + "short/";

    protected static final Path DIRECTORY_OPERATIONS_SHORT = FileSystems.getDefault()
        .getPath(DIRECTORY_NAME_OPERATIONS_SHORT);

    protected static final String DIRECTORY_NAME_OPERATIONS_LONG = DIRECTORY_NAME_OPERATIONS
        + "long/";

    protected static final Path DIRECTORY_OPERATIONS_LONG = FileSystems.getDefault()
        .getPath(DIRECTORY_NAME_OPERATIONS_LONG);

    protected static final String DIRECTORY_NAME_OPERATIONS_MODIFIERS = DIRECTORY_NAME_OPERATIONS
        + "modifiers/";

    protected static final Path DIRECTORY_OPERATIONS_MODIFIERS = FileSystems.getDefault()
        .getPath(DIRECTORY_NAME_OPERATIONS_MODIFIERS);

    protected static final String DIRECTORY_NAME_CAMPAIGN = "campaign/";

    protected static final Path DIRECTORY_CAMPAIGN = FileSystems.getDefault()
        .getPath(DIRECTORY_NAME_CAMPAIGN);

    protected static final Path DIRECTORY_CAMPAIGN_MECH_STAT = FileSystems.getDefault()
        .getPath(DIRECTORY_NAME_CAMPAIGN, "mechstat.dat");

    protected static final String DIRECTORY_NAME_CAMPAIGN_PLAYERS = DIRECTORY_NAME_CAMPAIGN
        + "players/";

    protected static final Path DIRECTORY_CAMPAIGN_PLAYERS = FileSystems.getDefault()
        .getPath(DIRECTORY_NAME_CAMPAIGN_PLAYERS);

    protected static final String DIRECTORY_NAME_CAMPAIGN_PLANETS = DIRECTORY_NAME_CAMPAIGN
        + "planets/";

    protected static final Path DIRECTORY_CAMPAIGN_PLANETS = FileSystems.getDefault()
        .getPath(DIRECTORY_NAME_CAMPAIGN_PLANETS);

    protected static final String DIRECTORY_NAME_CAMPAIGN_FACTIONS = DIRECTORY_NAME_CAMPAIGN
        + "factions/";

    protected static final Path DIRECTORY_CAMPAIGN_FACTIONS = FileSystems.getDefault()
        .getPath(DIRECTORY_NAME_CAMPAIGN_FACTIONS);

    protected static final String DIRECTORY_NAME_CAMPAIGN_COST_MODIFIERS = DIRECTORY_NAME_CAMPAIGN
        + "costmodifiers/";

    protected static final Path DIRECTORY_CAMPAIGN_COST_MODIFIERS = FileSystems.getDefault()
        .getPath(DIRECTORY_NAME_CAMPAIGN_COST_MODIFIERS);

    protected static final String DIRECTORY_NAME_CAMPAIGN_BACKUP = DIRECTORY_NAME_CAMPAIGN
        + "backup/";

    protected static final Path DIRECTORY_CAMPAIGN_BACKUP = FileSystems.getDefault()
        .getPath(DIRECTORY_NAME_CAMPAIGN_BACKUP);


    private static final Path[] DIRECTORIES = new Path[] {
        DIRECTORY_OPERATIONS,
        DIRECTORY_OPERATIONS_SHORT,
        DIRECTORY_OPERATIONS_LONG,
        DIRECTORY_OPERATIONS_MODIFIERS,
        DIRECTORY_CAMPAIGN,
        DIRECTORY_CAMPAIGN_PLAYERS,
        DIRECTORY_CAMPAIGN_PLANETS,
        DIRECTORY_CAMPAIGN_FACTIONS,
        DIRECTORY_CAMPAIGN_COST_MODIFIERS,
        DIRECTORY_CAMPAIGN_BACKUP
    };

    private static class LazyHolder {
        private static final FileSystem INSTANCE = new FileSystem();
    }

    public static FileSystem getInstance() {
        return LazyHolder.INSTANCE;
    }

    public static Path getTerrain() {
        return DIRECTORY_DATA_TERRAIN;
    }

    public static Path getAdvancedTerrain() {
        return DIRECTORY_DATA_ADVANCED_TERRAIN;
    }

    public static Path getSupportUnits() {
        return DIRECTORY_DATA_SUPPORT_UNITS;
    }

    public static Path getOperationsDir() {
        return DIRECTORY_OPERATIONS;
    }

    public Path getOpList() {
        return FileSystems.getDefault().getPath(getOperationsDir().toString(), FILE_NAME_OP_LIST);
    }

    public static Path getOperationsShortDir() {
        return DIRECTORY_OPERATIONS_SHORT;
    }

    public static Path getOperationsModifiersDir() {
        return DIRECTORY_OPERATIONS_MODIFIERS;
    }

    public static Path getOperationsLongDir() {
        return DIRECTORY_OPERATIONS_LONG;
    }

    public Path getCampaignConfig() {
        return FileSystems.getDefault().getPath(
            getConfigDir().toString(),
            FILE_NAME_CAMPAIGN_CONFIG
        );
    }

    public Path getBanAmmo() {
        return FileSystems.getDefault().getPath(getCampaignDir().toString(), FILE_NAME_BAN_AMMO);
    }

    public Path getCampaignDir() {
        return DIRECTORY_CAMPAIGN;
    }

    public Path getMechStat() {
        return DIRECTORY_CAMPAIGN_MECH_STAT;
    }

    public Path getPlayersDir() {
        return DIRECTORY_CAMPAIGN_PLAYERS;
    }

    public Path getPlanetsDir() {
        return DIRECTORY_CAMPAIGN_PLANETS;
    }

    public Path getFactionsDir() {
        return DIRECTORY_CAMPAIGN_FACTIONS;
    }

    public Path getCostModifiersDir() {
        return DIRECTORY_CAMPAIGN_COST_MODIFIERS;
    }

    public Path getBackupDir() {
        return DIRECTORY_CAMPAIGN_BACKUP;
    }

    public Path getConfigDir() {
        return DIRECTORY_DATA;
    }

    public Path[] getDirectories() {
        return DIRECTORIES;
    }
}
