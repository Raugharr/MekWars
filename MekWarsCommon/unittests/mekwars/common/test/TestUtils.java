/*
 * MekWars - Copyright (C) 2025 
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

package mekwars.common.test;

import java.lang.reflect.Field;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;

public class TestUtils {
    public static void setField(Class<?> classType, Object object, String fieldName, Object value) {
        Field field = ReflectionSupport
          .findFields(classType, f -> fieldName.equals(f.getName()),
            HierarchyTraversalMode.TOP_DOWN)
          .get(0);

        field.setAccessible(true);
        try {
            field.set(object, value);
        } catch (IllegalAccessException exception) {
            System.out.println(exception);
        }
    }

    public static Object getField(Class<?> classType, Object object, String fieldName) {
        Field field = ReflectionSupport
          .findFields(classType, f -> fieldName.equals(f.getName()),
            HierarchyTraversalMode.TOP_DOWN)
          .get(0);

        field.setAccessible(true);
        try {
            return field.get(object);
        } catch (IllegalAccessException exception) {
            System.out.println(exception);
            return null;
        }
    }
}
