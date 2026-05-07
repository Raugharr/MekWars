/*
 * MekWars - Copyright (C) 2026
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

package mekwars.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import mekwars.common.util.Position;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Random;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for Planet business logic, specifically testing the getPlanetOwner method which
 * determines which house has the highest influence on a planet.
 */
@ExtendWith(MockitoExtension.class)
class PlanetTest {
    @Mock private Position position;

    @Mock private Influences influence;

    @Test
    public void testId() {
        Planet testPlanet = new Planet("TestPlanet1", position, influence);

        assertEquals(EntityStore.UNSET_ID, testPlanet.getId());
    }

    @Nested
    public class ContinentsTest {
        private Continent[] continents;
        private Terrain[] terrains;
        private AdvancedTerrain[] advancedTerrains;

        @BeforeEach
        void setup() {
            terrains =
                    new Terrain[] {
                        Mockito.mock(Terrain.class),
                        Mockito.mock(Terrain.class),
                        Mockito.mock(Terrain.class)
                    };
            advancedTerrains =
                    new AdvancedTerrain[] {
                        Mockito.mock(AdvancedTerrain.class),
                        Mockito.mock(AdvancedTerrain.class),
                        Mockito.mock(AdvancedTerrain.class)
                    };
            continents =
                    new Continent[] {
                        new Continent(70, terrains[0], advancedTerrains[0]),
                        new Continent(20, terrains[1], advancedTerrains[1]),
                        new Continent(10, terrains[2], advancedTerrains[2])
                    };
        }

        @Test
        public void testHasContinents_empty() {
            Planet planet = new Planet("TestPlanet", position, influence);

            assertFalse(planet.hasContinents());
        }

        @Test
        public void testGetContinents_empty() {
            Planet planet = new Planet("TestPlanet", position, influence);

            List<Continent> continents = planet.getContinents();
            assertEquals(0, continents.size());
        }

        @Test
        public void testAddContinent() {
            Planet planet = new Planet("TestPlanet", position, influence);

            continents[0].setID(10);
            planet.addContinent(continents[0]);

            assertEquals(1, planet.getContinents().size());
            assertTrue(planet.hasContinents());
            assertEquals(continents[0].getID(), planet.getContinents().get(0).getID());
        }

        @Test
        public void testAddMultipleContinents() {
            Planet planet = new Planet("TestPlanet", position, influence);

            planet.addContinent(continents[0]);
            planet.addContinent(continents[1]);
            planet.addContinent(continents[2]);

            assertEquals(3, planet.getContinents().size());
            assertTrue(planet.hasContinents());
            assertEquals(3, planet.getContinents().size());
        }

        @Test
        public void testRemoveContinent() {
            when(terrains[0].getName()).thenReturn("foo");
            when(terrains[1].getName()).thenReturn("foo");
            when(terrains[2].getName()).thenReturn("Desert");

            Planet planet = new Planet("TestPlanet", position, influence);
            planet.addContinent(continents[0]);
            planet.addContinent(continents[1]);
            planet.addContinent(continents[2]);

            planet.removeContinent("Desert");

            assertEquals(2, planet.getContinents().size());
        }

        @Test
        public void testRemoveAllContinents() {
            Planet planet = new Planet("TestPlanet", position, influence);
            planet.addContinent(continents[0]);
            planet.addContinent(continents[1]);

            planet.removeAllContinents();

            assertEquals(0, planet.getContinents().size());
            assertFalse(planet.hasContinents());
            assertTrue(planet.getContinents().isEmpty());
        }

        @Test
        public void testGetTotalEnvironmentProbabilities() {
            Planet planet = new Planet("TestPlanet", position, influence);
            planet.addContinent(continents[0]);
            planet.addContinent(continents[1]);

            int totalProbability = planet.getTotalEnvironmentProbabilities();

            assertEquals(90, totalProbability);
        }

        @Test
        public void testGetBiggestContinent() {
            Planet planet = new Planet("TestPlanet", position, influence);

            planet.addContinent(continents[0]);
            planet.addContinent(continents[1]);
            planet.addContinent(continents[2]);

            Continent biggest = planet.getBiggestContinent();

            assertEquals(70, biggest.getSize());
        }

        @Test
        public void testGetBiggestContinent_single() {
            Planet planet = new Planet("TestPlanet", position, influence);
            planet.addContinent(continents[0]);

            Continent biggest = planet.getBiggestContinent();

            assertEquals(70, biggest.getSize());
        }

        @Test
        public void testGetBiggestContinent_empty() {
            Planet planet = new Planet("TestPlanet", position, influence);

            assertNull(planet.getBiggestContinent());
        }

        @Test
        public void testGetRandomContinent_weighted() {
            Planet planet = new Planet("TestPlanet", position, influence);
            planet.addContinent(continents[0]);
            planet.addContinent(continents[1]);
            planet.addContinent(continents[2]);

            Random random = new Random(42);

            Continent selected = planet.getRandomContinent(random);

            assertTrue(selected.equals(continents[0]));
        }

        @Test
        public void testGetRandomContinent_empty() {
            Planet planet = new Planet("TestPlanet", position, influence);

            Random random = new Random(42);
            Continent selected = planet.getRandomContinent(random);

            assertNull(selected.getEnvironment());
            assertEquals(0, selected.getSize());
        }

        @Test
        public void testRemoveContinent_nonexistent() {
            when(terrains[0].getName()).thenReturn("foo");
            when(terrains[1].getName()).thenReturn("foo");

            Planet planet = new Planet("TestPlanet", position, influence);
            planet.addContinent(continents[0]);
            planet.addContinent(continents[1]);

            planet.removeContinent("NonExistent");

            assertEquals(2, planet.getContinents().size());
        }
    }
}
