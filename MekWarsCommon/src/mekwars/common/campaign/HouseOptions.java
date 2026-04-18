/*
 * MekWars - Copyright (C) 2026
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

import mekwars.common.CampaignData;
import mekwars.common.MekWarsConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

public class HouseOptions extends MekWarsConfig {
    private static final Logger LOGGER = LogManager.getLogger(HouseOptions.class);

    public HouseOptions(Path path) {
        super(path);
        try {
            load();
        // We want to eat the IOException here as this configuration is optional.
        } catch(IOException exception) {
            LOGGER.debug("Unable to find faction config {}", path, exception);
        }
    }

    @Override
    public String getConfig(String key) {
        if (getConfig() == null || getConfig().getProperty(key) == null) {
            return CampaignData.cd.getCampaignOptions().getConfig(key);
        }
        return getConfig().getProperty(key).trim();
    }
}
