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

package mekwars.updater._9_0_0;

import megamek.Version;
import mekwars.updater.AbstractVersionUpdater;

public class VersionUpdater extends AbstractVersionUpdater {
    Version version = new Version("9.0.0");
    FactionUpdater factionUpdater = new FactionUpdater();
    TerrainUpdater terrainUpdater = new TerrainUpdater();
    AdvancedTerrainUpdater advancedTerrainUpdater = new AdvancedTerrainUpdater();
    PlanetUpdater planetUpdater = new PlanetUpdater();

    public void update() {
        factionUpdater.process();
        terrainUpdater.process();
        advancedTerrainUpdater.process();
        planetUpdater.process();
    }

    public Version getVersion() {
        return version;
    }
}
