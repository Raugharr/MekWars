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

package mekwars.client.io;

import java.io.IOException;
import java.lang.IllegalArgumentException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import mekwars.common.campaign.clientutils.IClientConfig;
import mekwars.common.util.IOUtil;
import mekwars.common.io.FileChecksum;
import mekwars.common.io.AbstractFileSystem;

public class FileSystem extends AbstractFileSystem {
    private Path configDir;
    private Path campaignConfig;

    private static final String DIRECTORY_NAME_SERVERS = DIRECTORY_NAME_DATA + "servers/";
    private static final Path DIRECTORY_SERVERS = FileSystems.getDefault()
        .getPath(DIRECTORY_NAME_SERVERS);

    public static final String FILE_NAME_DATA_LAST_UPDATED = "dataLastUpdated.dat";
    private static final Path FILE_DATA_LAST_UPDATED = FileSystems.getDefault()
        .getPath(FILE_NAME_DATA_LAST_UPDATED);

    private static final Path[] DIRECTORIES = new Path[] {
        DIRECTORY_DATA,
        DIRECTORY_SERVERS
    };

    private static class LazyHolder {
        private static final FileSystem INSTANCE = new FileSystem();
    }

    public static FileSystem getInstance() {
        return LazyHolder.INSTANCE;
    }

    public static Path getServersDir() {
        return DIRECTORY_SERVERS;
    }

    public Path getDataLastUpdated() {
        return FileSystems.getDefault().getPath(getConfigDir().toString(), FILE_NAME_DATA_LAST_UPDATED);
    }

    /**
     * Sets the config directory to configDir.
     */
    public void setConfigDir(String configDir) {
        this.configDir = FileSystems.getDefault().getPath(configDir);
        this.campaignConfig = FileSystems.getDefault().getPath(configDir, FILE_NAME_CAMPAIGN_CONFIG);
    }

    /**
     * Uses the IClientConfig parameters to generate a valid config directory.
     *
     * @param config The configuration that contains the SERVERIP and SERVERPORT properties to set
     * the config directory.
     *
     * @throws IllegalArgumentException When config does not have the SERVERIP or SERVERPORT config
     * values
     */
    public void setConfigDir(IClientConfig config) throws Exception {
        String serverIp = config.getParam("SERVERIP");
        String serverPort = config.getParam("SERVERPORT");

        if (serverIp == null) {
            throw new IllegalArgumentException("SERVERIP config value is empty");
        }
        if (serverPort == null) {
            throw new IllegalArgumentException("SERVERPORT config value is empty");
        }

        String configDirName = DIRECTORY_NAME_SERVERS + serverIp + "." + serverPort;
        this.configDir = FileSystems.getDefault().getPath(configDirName);
        this.campaignConfig = FileSystems.getDefault().getPath(
            configDir.getFileName().toString(),
            config.getParam("CAMPAIGNCONFIG")
        );
    }

    public Path getConfigDir() {
        return configDir;
    }

    public Path getCampaignConfig() {
        return campaignConfig;
    }

    public Path[] getDirectories() {
        return DIRECTORIES;
    }
}
