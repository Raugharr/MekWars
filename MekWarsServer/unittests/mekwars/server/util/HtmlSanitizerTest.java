/*
 * MekWars - Copyright (C) 2004 
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

package mekwars.server.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class HtmlSanitizerTest {

    @Test
    public void invalidHtmlTagTest() {
        String testHtml = "<foo>Hello World!</foo>";
        HtmlSanitizer htmlSanitizer = new HtmlSanitizer(false, false);
        String outHtml = htmlSanitizer.sanitize(testHtml);

        assertEquals(outHtml, "Hello World!");
    }

    @Test
    public void bodyHtmlTagInvalidTest() {
        String testHtml = "<body>Hello World!</body>";
        HtmlSanitizer htmlSanitizer = new HtmlSanitizer(false, false);
        String outHtml = htmlSanitizer.sanitize(testHtml);

        assertEquals(outHtml, "Hello World!");
    }

    @Test
    public void invalidHtmlAttributeTest() {
        String testHtml = "<div foo='bar'>Hello World!</div>";
        HtmlSanitizer htmlSanitizer = new HtmlSanitizer(false, false);
        String outHtml = htmlSanitizer.sanitize(testHtml);

        assertEquals(outHtml, "<div>Hello World!</div>");
    }

    @Test
    public void allowPlanetLinkTest() {
        String testHtml = "<planet name='Earth'>Greetings</planet>";
        HtmlSanitizer htmlSanitizer = new HtmlSanitizer(false, true);
        String outHtml = htmlSanitizer.sanitize(testHtml);

        assertEquals(outHtml, "<a href=\"JUMPTOPLANETEarth\">Greetings</a>");
    }

    @Test
    public void disallowPlanetLinkTest() {
        String testHtml = "<planet name=\"Earth\">Greetings</planet>";
        HtmlSanitizer htmlSanitizer = new HtmlSanitizer(false, false);
        String outHtml = htmlSanitizer.sanitize(testHtml);

        assertEquals(outHtml, "<planet name=\"Earth\">Greetings</planet>");
    }

    @Test
    public void allowLinkTest() {
        String testHtml = "<a href=\"http://www.foo.com\">Greetings</a>";
        HtmlSanitizer htmlSanitizer = new HtmlSanitizer(true, false);
        String outHtml = htmlSanitizer.sanitize(testHtml);

        assertEquals(outHtml, "<a href=\"http://www.foo.com\" rel=\"nofollow\">Greetings</a>");
    }

    @Test
    public void disallowLinkTest() {
        String testHtml = "<a href=\"http://www.foo.com\">Greetings</a>";
        HtmlSanitizer htmlSanitizer = new HtmlSanitizer(false, false);
        String outHtml = htmlSanitizer.sanitize(testHtml);

        assertEquals(outHtml, "<a>Greetings</a>");
    }
}
