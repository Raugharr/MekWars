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

import mekwars.common.log.LogMarkerHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface MWLogger {
    Logger LOGGER = LogManager.getLogger(MWLogger.class);

    static void mainLog(String message) {
        LOGGER.info(message);
    }

    static void modLog(String message) {
        LOGGER.info(message);
    }
    
    static void debugLog(String message) {
        LOGGER.debug(message);
    }
    
    static void ipLog(String message) {
        LOGGER.info(message);
    }
    
    static void cmdLog(String message) {
        LOGGER.error(message);
    }

    static void infoLog(String message) {
        LOGGER.info(message);
    }

    static void bmLog(String message) {
        LOGGER.info(message);
    }

    static void resultsLog(String message) {
        LOGGER.info(LogMarkerHolder.RESULTS_MARKER, message);
    }

    static void gameLog(String message) {
        LOGGER.info(LogMarkerHolder.GAME_MARKER, message);
    }

    static void testLog(String message) {
        LOGGER.info(LogMarkerHolder.TEST_MARKER, message);
    }

    static void tickLog(String message) {
        LOGGER.info(LogMarkerHolder.TICK_MARKER, message);
    }

    static void warnLog(String message) {
        LOGGER.warn(message);
    }

    static void pmLog(String message) {
        LOGGER.info(LogMarkerHolder.PM_MARKER, message);
    }

    static void mainLog(Exception e) {
        LOGGER.info("Exception: ", e);
    }
    
    static void modLog(Exception e) {
        LOGGER.info("Exception: ", e);
    }
    
    static void debugLog(Exception e) {
        LOGGER.info("Exception: ", e);
    }

    static void infoLog(Exception e) {
        LOGGER.info("Exception: ", e);
    }
}
