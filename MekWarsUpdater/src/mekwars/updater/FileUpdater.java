/*
 * MekWars - Copyright (C) 2004 
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

package mekwars.updater;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class FileUpdater<T> {
    private static final Logger logger = LogManager.getLogger(FileUpdater.class);
    private static final String DATA_DIR = "data/";

    private String inFilename;
    private String outFilename;
    
    public FileUpdater(String filename) {
        this.inFilename = filename;
        this.outFilename = filename;
    }

    public FileUpdater(String inFilename, String outFilename) {
        this.inFilename = inFilename;
        this.outFilename = outFilename;
    }

    public void process() {
        logger.info("Beginning to update file '{}'", getInFilename());
        try {
            createBackup();
            ArrayList<T> elements = deserialize();
            String xml = serialize(elements);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(getOutFilename()))) {
                writer.write(xml);
            }
        } catch (IOException e) {
            logger.error("Unable to update file '{}': '{}'", getInFilename(), e.getMessage());
            logger.error(e);
        }
        logger.info("Finished updating file '{}'", getInFilename());
    }

    protected void createBackup() throws IOException {
        File backup = new File(getBackupFilename());

        if (!backup.createNewFile()) {
            throw new IOException("backup for " + getInFilename() + " already exists");
        }

        Files.copy(
            Paths.get(getInFilename()),
            Paths.get(getBackupFilename()),
            StandardCopyOption.REPLACE_EXISTING
        );
    }

    protected abstract ArrayList<T> deserialize();

    protected abstract String serialize(ArrayList<T> elements);

    public String getInFilename() {
        return inFilename;
    }

    public String getOutFilename() {
        return outFilename;
    }

    public String getBackupFilename() {
        return getInFilename() + ".bak";
    }
}
