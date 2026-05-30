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

@ExtendWith(MockitoExtension.class)
class ArmyTest {
    private Army<Unit> army;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private Player player;

    @BeforeEach
    public void setup() {
        army = new Army<>(player);
        army.setId(1);
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
    public void testGetUnitById() {
        Unit unit = Mockito.mock(Unit.class);

        Mockito.when(unit.getId()).thenReturn(1);
        army.addUnit(unit);

        assertNotNull(army.getUnit(1));
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
