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

package mekwars.common.io;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractFileSystem {
    public static final String FILE_NAME_CAMPAIGN_CONFIG = "campaignconfig.txt";
    public static final String FILE_NAME_BAN_AMMO = "banammo.dat";
    public static final String FILE_NAME_BAN_TARGETING = "bantargeting.dat";
    public static final String FILE_NAME_OP_LIST = "OpList.txt";

    protected static final String DIRECTORY_NAME_DATA = "data/";
    protected static final Path DIRECTORY_DATA = FileSystems.getDefault()
        .getPath(DIRECTORY_NAME_DATA);

    private ConcurrentHashMap<String, FileChecksum> checksumHash = new ConcurrentHashMap<String, FileChecksum>();

    /**
     * Creates all directories needed by the FileSystem.
     *
     * @throws IOException When unable to create a directory
     */
    public void createDirectories() throws IOException {
        for (Path directory : getDirectories()) {
            Files.createDirectories(directory);
        }
    }

    /**
     * Returns the path to the banammo.dat file.
     *
     * @return The path to the banammo.dat file.
     */
    public Path getBanAmmo() {
        return FileSystems.getDefault().getPath(getConfigDir().toString(), FILE_NAME_BAN_AMMO);
    }

    /**
     * Returns the path to the bantargeting.dat file.
     *
     * @return The path to the bantargeting.dat file.
     */
    public Path getBanTargeting() {
        return FileSystems.getDefault().getPath(getConfigDir().toString(), FILE_NAME_BAN_TARGETING);
    }

    /**
     * Returns the path to the OpList.txt file.
     *
     * @return The path to the OpList.txt file.
     */
    public Path getOpList() {
        return FileSystems.getDefault().getPath(getConfigDir().toString(), FILE_NAME_OP_LIST);
    }

    /**
     * Returns the path to the campaignconfig.txt file.
     *
     * @return The path to the campaignconfig.txt file.
     */
    public Path getCampaignConfig() {
        return FileSystems.getDefault().getPath(getConfigDir().toString(), FILE_NAME_CAMPAIGN_CONFIG);
    }

    /**
     * Returns The path to the directory that contains all shared configurations.
     *
     * @return The path to the directory that contains all shared configurations.
     */
    public abstract Path getConfigDir();

    /*
     * Returns all directories used.
     *
     * @return All directories used.
     */
    public abstract Path[] getDirectories();

    /**
     * Calculates the filepath for every config file that is shared between the server and client.
     *
     * @return A list of the filepaths for every synched config file.
     */
    public Path[] getConfigFiles() {
        Path[] configFiles = new Path[3];

        configFiles[0] = getBanAmmo();
        configFiles[1] = getOpList();
        configFiles[2] = getCampaignConfig();
        return configFiles;
    }

    /**
     * Returns the {@link Path} of the data directory.
     *
     * @return The {@link Path} of the data directory.
     */
    public static Path getDataDir() {
        return DIRECTORY_DATA;
    }

    public ConcurrentHashMap<String, FileChecksum> getChecksumFiles() {
        return checksumHash;
    }

    /**
     * Reads all files that do not have a checksum and generate a checksum for them.
     *
     * @throws IOException If there is an error in reading one of the files.
     */
    public void calculateChecksums() throws IOException {
        checksumHash.clear();
        for (Path path : getConfigFiles()) {
            if (getFile(path.getFileName().toString()) == null && Files.exists(path)) {
                FileChecksum fileChecksum = new FileChecksum(path);
                checksumHash.put(fileChecksum.getFilename(), fileChecksum);
            }
        }
    }

    /**
     * Returns the given {@Link FileChecksum} for the given filename.
     *
     * @param filename The filename of the {@link FileChecksum}
     */
    public FileChecksum getFile(String filename) {
        return checksumHash.get(filename);
    }

    /**
     * Remove the checksum for the given filename.
     *
     * @param filename The file's checksum to remove.
     */
    public void clearChecksum(String filename) {
        checksumHash.remove(filename);
    }

    /**
     * Add a file checksum.
     *
     * @param filename The filename of the {@link FileChecksum}.
     * @param content The content of the {@link FileChecksum}.
     */
    public void addFile(String filename, byte[] content) throws IOException {
        checksumHash.put(filename, new FileChecksum(filename, content));
    }
}
