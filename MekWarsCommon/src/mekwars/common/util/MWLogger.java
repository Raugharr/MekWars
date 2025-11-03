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
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public interface MWLogger {
    Logger LOGGER = LogManager.getLogger(MWLogger.class);

    Marker RESULTS_MARKER = MarkerManager.getMarker("resultsLog");
    Marker GAME_MARKER = MarkerManager.getMarker("gameLog");
    Marker TEST_MARKER = MarkerManager.getMarker("testLog");
    Marker TICK_MARKER = MarkerManager.getMarker("tickLog");
    Marker PM_MARKER = MarkerManager.getMarker("pmLog");

    static void errLog(String message) {
        LOGGER.error(message);
    }
    
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
        LOGGER.info(RESULTS_MARKER, message);
    }

    static void gameLog(String message) {
        LOGGER.info(GAME_MARKER, message);
    }

    static void testLog(String message) {
        LOGGER.info(TEST_MARKER, message);
    }

    static void tickLog(String message) {
        LOGGER.info(TICK_MARKER, message);
    }

    static void warnLog(String message) {
        LOGGER.warn(message);
    }

    static void pmLog(String message) {
        LOGGER.info(PM_MARKER, message);
    }

    // Can't have a method like this here without a rework, note it is unused anyway
    static void factionLog(String factionName, String message) {
        final Marker faction = MarkerManager.getMarker(factionName);
        LOGGER.info(faction, message);
    }

    static void errLog(Exception e) {
        LOGGER.error("Exception: ", e);
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
    
    static void ipLog(Exception e) {
        LOGGER.info("Exception: ", e);
    }
    
    static void cmdLog(Exception e) {
        LOGGER.info("Exception: ", e);
    }
    
    static void infoLog(Exception e) {
        LOGGER.info("Exception: ", e);
    }

    static void bmLog(Exception e) {
        LOGGER.info("Exception: ", e);
    }

    static void resultsLog(Exception e) {
        LOGGER.info(RESULTS_MARKER, "Exception: ", e);
    }

    static void gameLog(Exception e) {
        LOGGER.info(GAME_MARKER, "Exception: ", e);
    }

    static void testLog(Exception e) {
        LOGGER.info(e);
    }
    
    static void tickLog(Exception e) {
        LOGGER.info(e);
    }
    
    static void warnLog(Exception e) {
        LOGGER.warn(e);
    }
    
    static void pmLog(Exception e) {
        LOGGER.info(e);
    }

    // Can't have a method like this here without a rework, note it is unused anyway
    static void factionLog(String factionName, Exception e) {
        final Marker factiondebug = MarkerManager.getMarker(factionName);
        LOGGER.debug(factiondebug, e.getMessage());
    }
}
