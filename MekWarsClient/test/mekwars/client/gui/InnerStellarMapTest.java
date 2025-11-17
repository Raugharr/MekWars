/*
 * MekWars - Copyright (C) 2025
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

package mekwars.client.gui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(value = MockitoExtension.class)
public class InnerStellarMapTest {

    @Mock
    private InnerStellarMap innerStellarMap;

    @Test
    void loadsConfigFromResourceFile() throws Exception {
        Path dir = Files.createTempDirectory("mapconf-test");
        Path target = dir.resolve("mapconf.xml");

        try (InputStream in = getClass().getResourceAsStream("/mapconf.xml")) {
            assertNotNull(in, "Test resource mapconf.xml not found in /test/resources/");
            Files.copy(in, target);
        }

        when(innerStellarMap.loadMapConf(anyString())).thenCallRealMethod();
        InnerStellarMapConfig conf = innerStellarMap.loadMapConf(dir.toString());
        boolean[] expected = {true, false, true, false, false, false, true, true};

        assertNotNull(conf, "Config should not be null");
        assertNotNull(conf.getDisplay(), "Display array should not be null");
        assertEquals(8, conf.getDisplay().length, "Display length mismatch — verify displayStr in test");
        assertArrayEquals(expected, conf.getDisplay());
    }
}

