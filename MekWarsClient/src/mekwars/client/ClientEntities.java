/*
 * MekWars - Copyright (C) 2026
 *
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet)
 * Original author Helge Richter (McWizard)
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

package mekwars.client;

import mekwars.common.CommonEntities;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ClientEntities {
    public static final List<Class<?>> ALL =
            Stream.concat(
                            CommonEntities.ALL.stream(),
                            Stream.of(
                                    mekwars.common.PlanetEnvironment.class,
                                    mekwars.common.Terrain.class,
                                    mekwars.common.AdvancedTerrain.class,
                                    mekwars.common.Continent.class,
                                    mekwars.common.Planet.class,
                                    mekwars.common.UnitFactory.class))
                    .collect(Collectors.toList());
}
