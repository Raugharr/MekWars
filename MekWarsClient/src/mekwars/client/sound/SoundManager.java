/*
 * MekWars - Copyright (C) 2004
 *
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet) Original author Helge Richter (McWizard)
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */

package mekwars.client.sound;

import mekwars.client.GUIClientConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SoundManager {
    private static final Logger LOGGER = LogManager.getLogger(SoundManager.class);

    private boolean soundMuted = false;
    private GUIClientConfig config;
    
    public SoundManager(GUIClientConfig config) {
        this.config = config;
        setSoundMuted(config.isParam("DISABLEALLSOUND"));
    }

    public void doPlaySound(String filename) {
        doPlaySound(filename, true);
    }

    // This can happen quite often, since no check is made if the config option
    // is set
    public void doPlaySound(String filename, boolean inThread) {
        if (soundMuted) {
            return;
        }

        try {
            if (inThread) {
                AePlayWave player = new AePlayWave(filename);
                player.start();
            } else {
                AePlayWave.AePlayWaveNonThreaded(filename);
            }
        } catch (Exception ex) {
            LOGGER.catching(ex);
        }
    }

    public void setSoundMuted(boolean b) {
        soundMuted = b;

        // see if the setting should be saved
        if (b != config.isParam("DISABLEALLSOUND")) {
            if (b == false) {
                config.setParam("DISABLEALLSOUND", "false");
            } else {
                config.setParam("DISABLEALLSOUND", "true");
            }
            config.saveConfig();
        }
    }

    public boolean isMuted() {
        return soundMuted;
    }
}
