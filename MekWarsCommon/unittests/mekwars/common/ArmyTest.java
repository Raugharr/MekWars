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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

@ExtendWith(MockitoExtension.class)
class ArmyTest {
    private Army army;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private Player player;

    @BeforeEach
    public void setup() throws Exception {
        army = Mockito.mock(Army.class, Mockito.CALLS_REAL_METHODS);
        army.setId(1);

        // Initialize transient fields that aren't set by constructor in mocked instances
        Field c3NetworkField = Army.class.getDeclaredField("c3Network");
        c3NetworkField.setAccessible(true);
        c3NetworkField.set(army, new HashMap<>());

        Field commandersField = Army.class.getDeclaredField("commanders");
        commandersField.setAccessible(true);
        commandersField.set(army, new ArrayList<>());
    }

    @Test
    public void testAddCommander() {
        army.addCommander(1);

        assertTrue(army.isCommander(1));
        assertEquals(1, army.getCommanders().size());
        assertTrue(army.getCommanders().contains(1));
    }

    @Test
    public void testAddSameCommanderTwice() {
        army.addCommander(1);
        army.addCommander(1);

        // Should not add duplicates
        assertEquals(1, army.getCommanders().size());
        assertTrue(army.getCommanders().contains(1));
    }

    @Test
    public void testRemoveNonExistentCommander() {
        army.addCommander(1);

        army.removeCommander(99);

        assertEquals(1, army.getCommanders().size());
        assertTrue(army.isCommander(1));
        assertFalse(army.isCommander(99));
    }

    @Test
    public void testRemoveExistingCommanderByIdValue() {
        army.addCommander(5);

        army.removeCommander(5);

        assertEquals(0, army.getCommanders().size());
    }

    @Test
    public void testSetUnitAsCommander() {
        Unit unit1 = Mockito.mock(Unit.class);
        Mockito.when(unit1.getId()).thenReturn(1);
        Unit unit2 = Mockito.mock(Unit.class);

        army.addUnit(unit1);
        army.addUnit(unit2);

        army.addCommander(unit1.getId());

        assertTrue(army.isCommander(unit1.getId()));
        assertEquals(1, army.getCommanders().size());
    }

    @Test
    public void testRemoveUnitAsCommander() {
        Unit unit1 = Mockito.mock(Unit.class);
        Mockito.when(unit1.getId()).thenReturn(1);

        army.addUnit(unit1);
        army.addCommander(unit1.getId());

        army.removeCommander(unit1.getId());

        assertFalse(army.isCommander(unit1.getId()));
    }
}
