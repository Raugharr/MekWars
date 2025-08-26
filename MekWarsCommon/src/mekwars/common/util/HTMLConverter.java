/*
 * Project Kerensky - Copyright (C) 2019 Bob Eldred (spork@mekwars.org)
 *
 * Part of the MekWars project
 *
 * Derived from MegaMekNET (http://www.megamek.info)
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

package common.util;

import java.util.StringTokenizer;


/**
 * Helper class for various HTML convertations
 * @author Imi (immanuel.scholz@gmx.de)
 */
public class HTMLConverter {

    /**
     * Convert all CR/LF into html-br-tags
     * @param str The string to be converted
     * @return The converted string
     */
    public static String cr2br(String str) {
        StringTokenizer tokened = new StringTokenizer(str, "\n");
        StringBuilder result = new StringBuilder();
        while (tokened.hasMoreTokens()) {
            result.append(tokened.nextToken());
            if (tokened.hasMoreTokens())
                result.append("<br>");
        }
        return result.toString();
    }

    /**
     * Convert all html-br-tags into CR/LF
     * @param str The string to be converted
     * @return The converted string
     */
    public static String br2cr(String str) {
        StringTokenizer tokened = new StringTokenizer(str, "<");
        String result = "";
        String temp = "";
        while (tokened.hasMoreTokens()) {
            temp += tokened.nextToken();
            if (temp.length() > 3 && temp.startsWith("BR>"))
                result += temp.substring(3);
            else
                result += temp;
            if (tokened.hasMoreTokens()) result += "\n";
        }
        return result;
    }
}

