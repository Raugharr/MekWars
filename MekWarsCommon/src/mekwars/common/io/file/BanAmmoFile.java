/*
 * MekWars - Copyright (C) 2026
 *
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet) Original author Helge Richter (McWizard)
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */

package mekwars.common.io.file;

import megamek.common.AmmoType;

import mekwars.common.CampaignData;
import mekwars.common.House;
import mekwars.common.entities.BannedAmmo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;

public class BanAmmoFile {
    private static final Logger LOGGER = LogManager.getLogger(BanAmmoFile.class);

    private Path path;

    public BanAmmoFile(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public void load(CampaignData campaignData) {
        try {
            List<String> banAmmoLines = Files.readAllLines(getPath());

            for (String line : banAmmoLines.subList(1, banAmmoLines.size())) {
                loadLine(line, campaignData);
            }
        } catch (FileNotFoundException fne) {
            LOGGER.info("No banned ammo data found.");
        } catch (Exception ex) {
            LOGGER.error("Unable to read " + path.getFileName(), ex);
        }
    }

    public void loadLine(String line, CampaignData campaignData) {
        try {
            StringTokenizer st = new StringTokenizer(line, "#");
            String houseName = (String) st.nextElement();
            House faction = null;

            if (!"server".equalsIgnoreCase(houseName)) {
                faction = campaignData.getHouseByName(houseName);
            }
            while (st.hasMoreElements()) {
                int munitionValue = Integer.parseInt(st.nextToken());
                AmmoType.Munitions munition = BannedAmmo.getMunitionByNumber(munitionValue);

                if (munition != null) {
                    campaignData.getBannedAmmoStore().add(munition, faction);
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Error loading banned ammo.", ex);
        }
    }

    public void save(long timestamp, CampaignData campaignData) {
        try {
            // output streams
            OutputStream out = Files.newOutputStream(getPath());
            PrintStream p = new PrintStream(out);

            // timestamp
            p.println(timestamp);

            Map<Optional<House>, List<BannedAmmo>> bannedAmmoByHouse =
                campaignData.getBannedAmmoStore().groupByHouse();

            for (Map.Entry<Optional<House>, List<BannedAmmo>> entry : bannedAmmoByHouse.entrySet()) {
                Optional<House> house = entry.getKey();

                if (house.isEmpty()) {
                    p.print("server#");
                } else {
                    p.print(house.get().getName() + "#");
                }

                for (BannedAmmo bannedAmmo : entry.getValue()) {
                    p.print(bannedAmmo.getId());
                    p.print("#");
                }
                p.println();
            }

            // close streams
            p.close();
            out.close();
        } catch (Exception ex) {
            LOGGER.error("Error saving banned ammo.", ex);
        }
    }
}
