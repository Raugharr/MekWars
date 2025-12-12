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
import java.nio.file.Files;
import java.nio.file.Path;
import mekwars.common.util.IOUtil;

public class FileChecksum {
    private String filename;
    private byte[] checksum;
    private byte[] content;

    public FileChecksum(Path path) throws IOException {
        byte[] content = Files.readAllBytes(path);

        this.filename = path.getFileName().toString();
        this.content = content;
        this.checksum = IOUtil.getSHA256(content);
    }

    public FileChecksum(String filename, byte[] content) throws IOException {
        this.filename = filename;
        this.content = content;
        this.checksum = IOUtil.getSHA256(content);
    }

    public String getFilename() {
        return filename;
    }

    public byte[] getContent() {
        return content;
    }

    public byte[] getChecksum() {
        return checksum;
    }
}
