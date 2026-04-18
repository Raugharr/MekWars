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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CampaignOptions {
    private static final Logger LOGGER = LogManager.getLogger(CampaignOptions.class);

    private DefaultCampaignOptions defaultOptions;
    private Properties config = new Properties();

    public CampaignOptions(String configFile) {
        defaultOptions = new DefaultCampaignOptions();
        defaultOptions.createDefaults();
        try {
            config.putAll(defaultOptions.getDefaults()); // load all of the defaults
            config.load(new FileInputStream(configFile));

            // Right here, we're going to try to prune old cruft from the configs
            // Over the course of many years, as config options change, crap never
            // gets removed from campaignconfig.txt.  We're seeing this very badly on
            // MMNet, and probably other servers are, as well.
            ArrayList<String> keysToRemove = new ArrayList<String>();
            for (Object key : config.keySet()) {
                if (!defaultOptions.getDefaults().keySet().contains(key) && !((String)key).endsWith("RewardPointMultiplier")) {
                    LOGGER.error("Key " + (String)key + " does not exist in DefaultServerConfig. Pruning from configs.");
                    keysToRemove.add((String) key);
                }
            }

            for (String key : keysToRemove) {
                config.remove(key);
            }
            saveConfigureFile(config, configFile);
        } catch (Exception ex) {
            LOGGER.error("Problems with loading campaign config");
            LOGGER.error("Exception: ", ex);
            defaultOptions.createConfig(configFile);
            try {
                config.load(new FileInputStream(configFile));
            } catch (Exception ex1) {
                LOGGER.error("Problems with loading campaing config from defaults");
                LOGGER.error(ex1);
                System.exit(1);
            }

            // added
            defaultOptions.createConfig(configFile); // save the cofig file so any missed defaults are
        }
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

    public String getConfig(String key) {
        if (config.getProperty(key) == null) {
            if (defaultOptions.getDefaults().getProperty(key) == null) {
                LOGGER.error("You're missing the config variable: " + key + " in campaignconfig!");
                return "-1";
            }
            return defaultOptions.getDefaults().getProperty(key).trim();
        }
        return config.getProperty(key).trim();
    }

    public void setProperty(String name, String value) {
        config.setProperty(name, value);
    }

    public Properties getProperties() {
        return config;
    }

    public void saveConfigureFile(Properties config, String fileName) {
        try {
            PrintStream ps = new PrintStream(new FileOutputStream(fileName));
            ps.println("#Timestamp=" + System.currentTimeMillis());
            config.store(ps, "Server Config");
            ps.close();
        } catch (FileNotFoundException fe) {
            LOGGER.error(fileName + " not found");
        } catch (Exception ex) {
            LOGGER.error("Exception: ", ex);
        }
    }

    public DefaultCampaignOptions getDefaultOptions() {
        return defaultOptions;
    }
}
