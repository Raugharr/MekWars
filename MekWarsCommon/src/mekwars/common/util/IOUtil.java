/*
 * MekWars - Copyright (C) 2025
 *
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet) Original author Helge Richter (McWizard)
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */

package mekwars.common.util;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IOUtil {
    private static final Logger LOGGER = LogManager.getLogger(IOUtil.class);

    /**
     * Calculates the SHA256 message digest of the buffer.
     *
     * @return The SHA256 message digest.
     */
    public static byte[] getSHA256(byte[] buffer) throws IOException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            return messageDigest.digest(buffer);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Unable to get SHA256", e);
            return null;
        }
    }
}
