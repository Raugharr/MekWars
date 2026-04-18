/*
 * MekWars - Copyright (C) 2004
 *
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet)
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */

package mekwars.common.campaign;

import mekwars.common.MekWarsConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CampaignOptions extends MekWarsConfig {
    private static final Logger LOGGER = LogManager.getLogger(CampaignOptions.class);

    private DefaultCampaignOptions defaultOptions;

    public CampaignOptions(Path path) {
        super(path);
        defaultOptions = new DefaultCampaignOptions();
        defaultOptions.createDefaults();
        load();
        // save the config file so any missed defaults are included.
        save();
    }

    public boolean getBooleanConfig(String key) {
        try {
            return Boolean.parseBoolean(getConfig(key));
        } catch (Exception ex) {
            return false;
        }
    }

    public int getIntegerConfig(String key) {
        try {
            return Integer.parseInt(getConfig(key));
        } catch (Exception ex) {
            return -1;
        }
    }

    public long getLongConfig(String key) {
        try {
            return Long.parseLong(getConfig(key));
        } catch (Exception ex) {
            return -1;
        }
    }

    public double getDoubleConfig(String key) {
        try {
            return Double.parseDouble(getConfig(key));
        } catch (Exception ex) {
            return -1;
        }
    }

    public float getFloatConfig(String key) {
        try {
            return Float.parseFloat(getConfig(key));
        } catch (Exception ex) {
            return -1;
        }
    }

    public void load() {
        // load all of the defaults
        getConfig().putAll(defaultOptions.getDefaults());
        try {
            getConfig().load(Files.newInputStream(getPath()));

            // Right here, we're going to try to prune old cruft from the configs
            // Over the course of many years, as config options change, crap never
            // gets removed from campaignconfig.txt.  We're seeing this very badly on
            // MMNet, and probably other servers are, as well.
            ArrayList<String> keysToRemove = new ArrayList<String>();
            for (Object key : getConfig().keySet()) {
                if (!defaultOptions.getDefaults().keySet().contains(key)
                        && !((String) key).endsWith("RewardPointMultiplier")) {
                    LOGGER.error(
                            "Key "
                                    + (String) key
                                    + " does not exist in DefaultServerConfig. Pruning from"
                                    + " configs.");
                    keysToRemove.add((String) key);
                }
            }

            for (String key : keysToRemove) {
                getConfig().remove(key);
            }
            onLoad();
        } catch (Exception ex) {
            LOGGER.error("Problems with loading campaign config", ex);
        }
    }

    @Override
    public String getConfig(String key) {
        if (getConfig().getProperty(key) == null) {
            if (defaultOptions.getDefaults().getProperty(key) == null) {
                LOGGER.error("You're missing the config variable: " + key + " in campaignconfig!");
                return "-1";
            }
            return defaultOptions.getDefaults().getProperty(key).trim();
        }
        return getConfig().getProperty(key).trim();
    }

    public void setProperty(String name, String value) {
        getConfig().setProperty(name, value);
    }

    /** Deprecated, use getConfig() instead. */
    @Deprecated(since = "9.0.0", forRemoval = false)
    public Properties getProperties() {
        return getConfig();
    }

    public DefaultCampaignOptions getDefaultOptions() {
        return defaultOptions;
    }
}
