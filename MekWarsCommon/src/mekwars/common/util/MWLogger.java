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
package mekwars.common.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface MWLogger {
    Logger LOGGER = LogManager.getLogger(MWLogger.class);

    static void mainLog(String message) {
        LOGGER.info(message);
    }
}
