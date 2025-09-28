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

package mekwars.updater._9_0_0.util;

import gd.xml.ParseException;
import gd.xml.XMLParser;
import gd.xml.XMLResponder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;
import megamek.common.planetaryconditions.Atmosphere;
import megamek.common.planetaryconditions.EMI;
import megamek.common.planetaryconditions.Fog;
import megamek.common.planetaryconditions.Light;
import megamek.common.planetaryconditions.Wind;
import megamek.common.planetaryconditions.WindDirection;
import mekwars.common.AdvancedTerrain;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author mike
 */
public class XMLAdvancedTerrainDataParser implements XMLResponder {
    private static final Logger logger = LogManager.getLogger(XMLAdvancedTerrainDataParser.class);

    private String prefix;
    String Name = "";

    String lastElement = "";
    String name;
    String filename;

    int lowtemp = 25;
    int hitemp = 25;
    double gravity = 1.0;
    boolean vacuum = false;
    int nightchance = 0;
    int nightmod = 0;
    int blizzardChance = 0;
    int blowingSandChance = 0;
    int heavySnowfallChance = 0;
    int lightRainfallChance = 0;
    int heavyRainfallChance = 0;
    int moderateWindsChance = 0;
    int highWindsChance = 0;
    int downPourChance = 0;
    int duskChance = 0;
    int atmo = 0;
    int emiChance = 0;
    boolean emi = false;
    private int windStrength;
    private int windDir;
    private int tornadoF4WindChance;
    private int tornadoF13WindChance;
    private boolean effectTerrain;
    private int stormWindsChance;
    private int strongWindsChance;
    private int temp;
    private int sleetChance;
    private boolean shiftingWindStrength;
    private boolean shiftingWindDirection;
    private int pitchBlackChance;
    private int moonlessNightChance;
    private int moderateSnowFallChance;
    private int moderateRainFallChance;
    private int maxWindStrength;
    private int lightWindChance;
    private int lightSnowfallChance;
    private int lightHailChance;
    private int lightFogChance;
    private int lightConditions;
    private int iceStormChance;
    private int heavyHailChance;
    private int heavyFogChance;
    private int fogChance;

    private AdvancedTerrain planetTerrain;
    private ArrayList<AdvancedTerrain> advancedTerrains = new ArrayList();

    public XMLAdvancedTerrainDataParser(String filename) {
        this.filename = filename;
        if (!(new File(filename).exists())) {
            return;
        }
        try {
            XMLParser xp = new XMLParser();
            xp.parseXML(this);
        } catch (Exception ex) {
            logger.error("Error parsing " + filename);
            logger.error(ex);
        }
    }

    public ArrayList<AdvancedTerrain> getAdvancedTerrains() {
        return advancedTerrains;
    }

    public void recordNotationDeclaration(String name, String pubID, String sysID)
            throws ParseException {
        System.out.print(prefix + "!NOTATION: " + name);
        if (pubID != null) System.out.print("  pubID = " + pubID);
        if (sysID != null) System.out.print("  sysID = " + sysID);
        logger.info("");
    }

    public void recordEntityDeclaration(
            String name, String value, String pubID, String sysID, String notation)
            throws ParseException {
        System.out.print(prefix + "!ENTITY: " + name);
        if (value != null) System.out.print("  value = " + value);
        if (pubID != null) System.out.print("  pubID = " + pubID);
        if (sysID != null) System.out.print("  sysID = " + sysID);
        if (notation != null) System.out.print("  notation = " + notation);
        logger.info("");
    }

    public void recordElementDeclaration(String name, String content) throws ParseException {
        System.out.print(prefix + "!ELEMENT: " + name);
        logger.info("  content = " + content);
    }

    public void recordAttlistDeclaration(
            String element, String attr, boolean notation, String type, String defmod, String def)
            throws ParseException {
        System.out.print(prefix + "!ATTLIST: " + element);
        System.out.print("  attr = " + attr);
        System.out.print("  type = " + ((notation) ? "NOTATIONS " : "") + type);
        System.out.print("  def. modifier = " + defmod);
        logger.info((def == null) ? "" : "  def = " + notation);
    }

    public void recordDoctypeDeclaration(String name, String pubID, String sysID)
            throws ParseException {
        System.out.print(prefix + "!DOCTYPE: " + name);
        if (pubID != null) System.out.print("  pubID = " + pubID);
        if (sysID != null) System.out.print("  sysID = " + sysID);
        logger.info("");
        prefix = "";
    }

    /* DOC METHDODS */

    public void recordDocStart() {}

    public void recordDocEnd() {
        logger.info("");
        logger.info("Parsing finished without error");
    }

    @SuppressWarnings("rawtypes")
    public void recordElementStart(String name, Hashtable attr) throws ParseException {
        // logger.info(prefix+"Element: "+name);
        lastElement = name;
    }

    public void recordElementEnd(String tagName) throws ParseException {
        EMI emiValue = (emi) ? EMI.EMI : EMI.EMI_NONE;
        logger.info("Advanced Terrain READ");
        if (tagName.equals("ADVANCEDTERRAIN")) {
            planetTerrain = new AdvancedTerrain();
            planetTerrain.setAtmosphere(Atmosphere.values()[atmo]);
            planetTerrain.setDownPourChance(downPourChance);
            planetTerrain.setDuskChance(duskChance);
            planetTerrain.setEMI(emiValue);
            planetTerrain.setEMIChance(emiChance);
            planetTerrain.setFog(Fog.values()[fogChance]);
            planetTerrain.setGravity(gravity);
            planetTerrain.setHeavyfogChance(heavyFogChance);
            planetTerrain.setHeavyHailChance(heavyHailChance);
            planetTerrain.setHeavyRainfallChance(heavyRainfallChance);
            planetTerrain.setHeavySnowfallChance(heavySnowfallChance);
            planetTerrain.setHighTemp(hitemp);
            planetTerrain.setIceStormChance(iceStormChance);
            planetTerrain.setLightConditions(Light.values()[lightConditions]);
            planetTerrain.setLightFogChance(lightFogChance);
            planetTerrain.setLightHailChance(lightHailChance);
            planetTerrain.setLightRainfallChance(lightRainfallChance);
            planetTerrain.setLightSnowfallChance(lightSnowfallChance);
            planetTerrain.setLightWindChance(lightWindChance);
            planetTerrain.setLowTemp(lowtemp);
            planetTerrain.setMaxWindStrength(Wind.values()[maxWindStrength]);
            planetTerrain.setModerateRainFallChance(moderateRainFallChance);
            planetTerrain.setModerateSnowFallChance(moderateSnowFallChance);
            planetTerrain.setModerateWindChance(moderateWindsChance);
            planetTerrain.setMoonlessNightChance(moonlessNightChance);
            planetTerrain.setNightChance(nightchance);
            planetTerrain.setNightTempMod(nightmod);
            planetTerrain.setPitchBlackNightChance(pitchBlackChance);
            planetTerrain.setShiftingWindDirection(shiftingWindDirection);
            planetTerrain.setShiftingWindStrength(shiftingWindStrength);
            planetTerrain.setSleetChance(sleetChance);
            planetTerrain.setStormWindChance(stormWindsChance);
            planetTerrain.setStrongWindChance(strongWindsChance);
            planetTerrain.setTemperature(temp);
            planetTerrain.setTerrainAffected(effectTerrain);
            planetTerrain.setTornadoF13WindChance(tornadoF13WindChance);
            planetTerrain.setTornadoF4WindChance(tornadoF4WindChance);
            planetTerrain.setWindDirection(WindDirection.values()[windDir]);
            planetTerrain.setWindStrength(Wind.values()[windStrength]);
            planetTerrain.setName(name);
            planetTerrain.setDisplayName(name);
            logger.info("ADVTERRAIN: adding " + planetTerrain.getName());
            advancedTerrains.add(planetTerrain);
            name = "reset";
        }
        if (tagName.equals("ADVTERRAIN")) {}
    }

    @Override
    public InputStream getDocumentStream() throws ParseException {
        try {
            return new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            throw new ParseException("could not find the specified file: " + filename);
        }
    }

    @Override
    public void recordCharData(String charData) {
        logger.info(prefix + charData);
        if (!charData.equalsIgnoreCase("")) {
            logger.info(lastElement + " --> " + charData);
        } else {
            lastElement = "";
        }

        if (lastElement.equalsIgnoreCase("NAME")) {
            name = charData;
            logger.info(name);
        } else if (lastElement.equalsIgnoreCase("lowtemp")) {
            lowtemp = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("hitemp")) {
            hitemp = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("gravity")) {
            gravity = Double.parseDouble(charData);
        } else if (lastElement.equalsIgnoreCase("vacuum")) {
            vacuum = Boolean.parseBoolean(charData);
        } else if (lastElement.equalsIgnoreCase("nightchance")) {
            nightchance = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("nightmod")) {
            nightmod = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("blizzardchance")) {
            blizzardChance = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("blowingsandchance")) {
            blowingSandChance = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("heavysnowfallchance")) {
            heavySnowfallChance = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("lightRainfallChance")) {
            lightRainfallChance = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("heavyRainfallChance")) {
            heavyRainfallChance = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("moderateWindsChance")) {
            moderateWindsChance = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("highWindsChance")) {
            highWindsChance = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("downPourChance")) {
            downPourChance = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("duskChance")) {
            duskChance = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("atmosphere")) {
            atmo = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("emiChance")) {
            emiChance = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("emi")) {
            emi = Boolean.parseBoolean(charData);
        } else if (lastElement.equalsIgnoreCase("windStrength")) {
            windStrength = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("windDir")) {
            windDir = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("tornadoF4WindChance")) {
            tornadoF4WindChance = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("tornadoF13WindChance")) {
            tornadoF13WindChance = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("effectTerrain")) {
            effectTerrain = Boolean.parseBoolean(charData);
        } else if (lastElement.equalsIgnoreCase("stormWindsChance")) {
            stormWindsChance = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("strongWindsChance")) {
            strongWindsChance = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("temp")) {
            temp = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("sleetchance")) {
            sleetChance = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("shiftingwindstrength")) {
            shiftingWindStrength = Boolean.parseBoolean(charData);
        } else if (lastElement.equalsIgnoreCase("shiftingWindDirection")) {
            shiftingWindDirection = Boolean.parseBoolean(charData);
        } else if (lastElement.equalsIgnoreCase("pitchBlackChance")) {
            pitchBlackChance = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("moonlessNightChance")) {
            moonlessNightChance = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("moderateSnowFallChance")) {
            moderateSnowFallChance = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("moderateRainFallChance")) {
            moderateRainFallChance = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("maxWindsStrength")) {
            maxWindStrength = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("lightWindChance")) {
            lightWindChance = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("ligthSnowfallChance")) {
            lightSnowfallChance = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("lightHailChance")) {
            lightHailChance = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("lightFogChance")) {
            lightFogChance = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("lightConditions")) {
            lightConditions = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("iceStormChance")) {
            iceStormChance = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("HeavyHailChance")) {
            heavyHailChance = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("HeavyFogChance")) {
            heavyFogChance = Integer.parseInt(charData);
        } else if (lastElement.equalsIgnoreCase("fogChance")) {
            fogChance = Integer.parseInt(charData);
        }
    }

    @Override
    public void recordComment(String comment) {
        logger.info(prefix + "*Comment: " + comment);
    }

    @Override
    public void recordPI(String name, String pValue) {
        logger.info(prefix + "*" + name + " PI: " + pValue);
    }

    public InputStream resolveDTDEntity(String name, String pubID, String sysID)
            throws ParseException {
        return resolveExternalEntity(name, pubID, sysID);
    }

    public static String newLineToBR(String data) {
        StringTokenizer tokened = new StringTokenizer(data, "\n");
        String result = new String();
        while (tokened.hasMoreElements()) {
            result += tokened.nextElement();
            if (tokened.hasMoreElements()) {
                result += "<BR>";
            }
        }
        return result;
    }

    @Override
    public InputStream resolveExternalEntity(String name, String pubID, String sysID)
            throws ParseException {
        if (sysID != null) {
            File f = new File((new File(filename)).getParent(), sysID);
            try {
                return new FileInputStream(f);
            } catch (FileNotFoundException e) {
                throw new ParseException("file not found (" + f + ")");
            }
        }
        // else
        return null;
    }
}
