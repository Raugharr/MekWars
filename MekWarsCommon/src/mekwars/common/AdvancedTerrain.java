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

package mekwars.common;

import java.io.IOException;
import java.util.StringTokenizer;

import mekwars.common.util.BinReader;
import mekwars.common.util.BinWriter;
import mekwars.common.util.TokenReader;
import megamek.common.planetaryconditions.Atmosphere;
import megamek.common.planetaryconditions.EMI;
import megamek.common.planetaryconditions.Fog;
import megamek.common.planetaryconditions.Light;
import megamek.common.planetaryconditions.PlanetaryConditions;
import megamek.common.planetaryconditions.Weather;
import megamek.common.planetaryconditions.Wind;
import megamek.common.planetaryconditions.WindDirection;

/**
 * Advanced Environment for planets.
 * 
 * @@author Torren (Jason Tighe) allows So's to set up each individual terrain on a planet.
 */

final public class AdvancedTerrain {
    private String displayName = "none";
    private int id = 0;
    private String name = "none";
    
    //NOTE: These fields are unused and kept to keep the xml file consistent
    private int blizzardChance = 0;
    private int blowingSandChance = 0;
    private int highWindChance = 0;

    private int lowTemp = 25;
    private int highTemp = 25;
    private double gravity = 1.0;
    private boolean vacuum = false;
    private int duskChance = 0;
    private int fullMoonChance = 0;
    private int moonlessNightChance = 0;
    private int pitchBlackChance = 0;
    private int nightTempMod = 0;
    private int minVisibility = 100;
    private int maxVisibility = 100;
    private Atmosphere atmosphere = Atmosphere.STANDARD;

    private int lightRainfallChance = 0;
    private int moderateRainfallChance = 0;
    private int heavyRainfallChance = 0;
    private int downPourChance = 0;

    private int lightSnowfallChance = 0;
    private int moderateSnowfallChance = 0;
    private int heavySnowfallChance = 0;
    private int sleetChance = 0;
    private int iceStormChance = 0;
    private int lightHailChance = 0;
    private int heavyHailChance = 0;

    private int lightWindChance = 0;
    private int moderateWindChance = 0;
    private int strongWindChance = 0;
    private int stormWindChance = 0;
    private int tornadoF13WindChance = 0;
    private int tornadoF4WindChance = 0;

    private int lightFogChance = 0;
    private int heavyFogChance = 0;

    private int emiChance = 0;

    // MegaMek Planetary Conditions
    // set up the specific conditions
    private Light lightConditions = Light.DAY;
    private Weather weatherConditions = Weather.CLEAR;
    private Wind windStrength = Wind.CALM;
    private WindDirection windDirection = WindDirection.RANDOM;
    private Wind maxWindStrength = Wind.TORNADO_F4;
    private boolean shiftingWindDirection = false;
    private boolean shiftingWindStrength = false;
    private Fog fog = Fog.FOG_NONE;
    private int temperature = 25;
    private EMI emi = EMI.EMI_NONE;
    private boolean terrainAffected = true;

    @Override
    public String toString() {
        String result = "";
        result = "$";

        if (displayName.trim().length() < 1) {
            result += "Terrain";
        } else {
            result += displayName;
        }

        result += "$";
        result += lowTemp;
        result += "$";
        result += highTemp;
        result += "$";
        result += gravity;
        result += "$";
        result += vacuum;
        result += "$";
        result += fullMoonChance;
        result += "$";
        result += nightTempMod;
        result += "$";
        result += minVisibility;
        result += "$";
        result += maxVisibility;
        result += "$";
        result += moderateRainfallChance;
        result += "$";
        result += moderateSnowfallChance;
        result += "$";
        result += heavySnowfallChance;
        result += "$";
        result += lightRainfallChance;
        result += "$";
        result += heavyRainfallChance;
        result += "$";
        result += moderateWindChance;
        result += "$";
        result += strongWindChance;
        result += "$";
        result += downPourChance;
        result += "$";
        result += lightSnowfallChance;
        result += "$";
        result += sleetChance;
        result += "$";
        result += iceStormChance;
        result += "$";
        result += lightHailChance;
        result += "$";
        result += heavyHailChance;
        result += "$";
        result += stormWindChance;
        result += "$";
        result += tornadoF13WindChance;
        result += "$";
        result += tornadoF4WindChance;
        result += "$";
        result += atmosphere;
        result += "$";
        result += lightFogChance;
        result += "$";
        result += heavyFogChance;
        result += "$";
        result += duskChance;
        result += "$";
        result += moonlessNightChance;
        result += "$";
        result += pitchBlackChance;
        result += "$";
        result += emiChance;
        result += "$";
        result += lightWindChance;

        return result;
    }

    public void binIn(BinReader in) throws IOException {
        displayName = in.readLine("displayName");
        name = displayName;
        lowTemp = in.readInt("lowTemp");
        highTemp = in.readInt("highTemp");
        gravity = in.readDouble("gravity");
        vacuum = in.readBoolean("vacuum");
        fullMoonChance = in.readInt("nightChance");
        nightTempMod = in.readInt("nightTempMod");
        minVisibility = in.readInt("minvisibility");
        maxVisibility = in.readInt("maxvisibility");
        moderateRainfallChance = in.readInt("moderateRainfallChance");
        moderateSnowfallChance = in.readInt("moderateSnowfallChance");
        heavySnowfallChance = in.readInt("heavySnowfallChance");
        lightRainfallChance = in.readInt("lightRainfallChance");
        heavyRainfallChance = in.readInt("heavyRainfallChance");
        lightWindChance = in.readInt("lightWindChance");
        moderateWindChance = in.readInt("moderateWindChance");
        strongWindChance = in.readInt("strongWindChance");
        downPourChance = in.readInt("downPourChance");
        lightSnowfallChance = in.readInt("lightSnowfallChance");
        sleetChance = in.readInt("sleetChance");
        iceStormChance = in.readInt("iceStormChance");
        lightHailChance = in.readInt("lightHailChance");
        heavyHailChance = in.readInt("heavyHailChance");
        stormWindChance = in.readInt("stormWindChance");
        tornadoF13WindChance = in.readInt("tornadoF13WindChance");
        tornadoF4WindChance = in.readInt("tornadoF4WindChance");
        atmosphere = Atmosphere.values()[in.readInt("atmosphere")];
        lightFogChance = in.readInt("lightFogChance");
        heavyFogChance = in.readInt("heavyFogChance");
        duskChance = in.readInt("duskChance");
        moonlessNightChance = in.readInt("moonlessNightChance");
        pitchBlackChance = in.readInt("pitchBlackChance");
        emiChance = in.readInt("emiChance");
    }

    public void binOut(BinWriter out) throws IOException {

        out.println(displayName, "displayName");
        out.println(lowTemp, "lowTemp");
        out.println(highTemp, "highTemp");
        out.println(gravity, "gravity");
        out.println(vacuum, "vacuum");
        out.println(fullMoonChance, "nightChance");
        out.println(nightTempMod, "nightTempMod");
        out.println(minVisibility, "minvisibility");
        out.println(maxVisibility, "maxvisibility");
        out.println(moderateRainfallChance, "moderateRainfallChance");
        out.println(moderateSnowfallChance, "moderateSnowfallChance");
        out.println(heavySnowfallChance, "heavySnowfallChance");
        out.println(lightRainfallChance, "lightRainfallChance");
        out.println(heavyRainfallChance, "heavyRainfallChance");
        out.println(lightWindChance, "lightWindChance");
        out.println(moderateWindChance, "moderateWindChance");
        out.println(strongWindChance, "strongWindChance");
        out.println(downPourChance, "downPourChance");
        out.println(lightSnowfallChance, "lightSnowfallChance");
        out.println(sleetChance, "sleetChance");
        out.println(iceStormChance, "iceStormChance");
        out.println(lightHailChance, "lightHailChance");
        out.println(heavyHailChance, "heavyHailChance");
        out.println(stormWindChance, "stormWindChance");
        out.println(tornadoF13WindChance, "tornadoF13WindChance");
        out.println(tornadoF4WindChance, "tornadoF4WindChance");
        out.println(atmosphere.ordinal(), "atmosphere");
        out.println(lightFogChance, "lightFogChance");
        out.println(heavyFogChance, "heavyFogChance");
        out.println(duskChance, "duskChance");
        out.println(moonlessNightChance, "moonlessNightChance");
        out.println(pitchBlackChance, "pitchBlackChance");
        out.println(emiChance, "emiChance");
    }

    public AdvancedTerrain(String s) {
        StringTokenizer command = new StringTokenizer(s, "$");

        setDisplayName(TokenReader.readString(command));
        setLowTemp(TokenReader.readInt(command));
        setHighTemp(TokenReader.readInt(command));
        setGravity(TokenReader.readDouble(command));
        setVacuum(TokenReader.readBoolean(command));
        setNightChance(TokenReader.readInt(command));
        setNightTempMod(TokenReader.readInt(command));
        setMinVisibility(TokenReader.readInt(command));
        setMaxVisibility(TokenReader.readInt(command));
        setModerateRainFallChance(TokenReader.readInt(command));
        setModerateSnowFallChance(TokenReader.readInt(command));
        setHeavySnowfallChance(TokenReader.readInt(command));
        setLightRainfallChance(TokenReader.readInt(command));
        setHeavyRainfallChance(TokenReader.readInt(command));
        setModerateWindChance(TokenReader.readInt(command));
        setStrongWindChance(TokenReader.readInt(command));
        setDownPourChance(TokenReader.readInt(command));
        setLightSnowfallChance(TokenReader.readInt(command));
        setSleetChance(TokenReader.readInt(command));
        setIceStormChance(TokenReader.readInt(command));
        setLightHailChance(TokenReader.readInt(command));
        setHeavyHailChance(TokenReader.readInt(command));
        setStormWindChance(TokenReader.readInt(command));
        setTornadoF13WindChance(TokenReader.readInt(command));
        setTornadoF4WindChance(TokenReader.readInt(command));
        setAtmosphere(Atmosphere.values()[TokenReader.readInt(command)]);
        setLightFogChance(TokenReader.readInt(command));
        setHeavyfogChance(TokenReader.readInt(command));
        setDuskChance(TokenReader.readInt(command));
        setMoonlessNightChance(TokenReader.readInt(command));
        setPitchBlackNightChance(TokenReader.readInt(command));
        setEMIChance(TokenReader.readInt(command));
        setLightWindChance(TokenReader.readInt(command));

        // MegaMek Planetary Conditions this should always be last
        setLightConditions(Light.values()[TokenReader.readInt(command)]);
        setWeatherConditions(Weather.values()[TokenReader.readInt(command)]);
        setWindStrength(Wind.values()[TokenReader.readInt(command)]);
        setWindDirection(WindDirection.values()[TokenReader.readInt(command)]);
        setShiftingWindDirection(TokenReader.readBoolean(command));
        setShiftingWindStrength(TokenReader.readBoolean(command));
        setFog(Fog.values()[TokenReader.readInt(command)]);
        setTemperature(TokenReader.readInt(command));
        setEMI(EMI.values()[TokenReader.readInt(command)]);
        setTerrainAffected(TokenReader.readBoolean(command));
        setMaxWindStrength(Wind.values()[TokenReader.readInt(command)]);

    }

    public AdvancedTerrain() {
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String name) {
        displayName = name;
        this.name = name; 
    }

    @Deprecated
    public boolean isVacuum() {
        return vacuum;
    }

    @Deprecated
    public void setVacuum(boolean vacuum) {
        this.vacuum = vacuum;
    }

    public int getLowTemp() {
        return lowTemp;
    }

    public void setLowTemp(int temp) {
        lowTemp = temp;
    }

    public int getHighTemp() {
        return highTemp;
    }

    public void setHighTemp(int temp) {
        highTemp = temp;
    }

    public double getGravity() {
        return gravity;
    }

    public void setGravity(double grav) {
        gravity = grav;
    }

    public int getDuskChance() {
        return duskChance;
    }

    public void setDuskChance(int chance) {
        duskChance = chance;
    }

    public int getNightChance() {
        return fullMoonChance;
    }

    public void setNightChance(int chance) {
        fullMoonChance = chance;
    }

    public int getMoonlessNightChance() {
        return moonlessNightChance;
    }

    public void setMoonlessNightChance(int chance) {
        moonlessNightChance = chance;
    }

    public int getPitchBlackNightChance() {
        return pitchBlackChance;
    }

    public void setPitchBlackNightChance(int chance) {
        pitchBlackChance = chance;
    }

    public int getNightTempMod() {
        return nightTempMod;
    }

    public void setNightTempMod(int mod) {
        nightTempMod = mod;
    }

    @Deprecated
    public int getMinVisibility() {
        return minVisibility;
    }

    @Deprecated
    public int getMaxVisibility() {
        return maxVisibility;
    }

    @Deprecated
    public void setMinVisibility(int minVisibility) {
        this.minVisibility = minVisibility;
    }

    @Deprecated
    public void setMaxVisibility(int maxVisibility) {
        this.maxVisibility = maxVisibility;
    }

    public void setModerateSnowFallChance(int chance) {
        moderateSnowfallChance = chance;
    }

    public int getModerateSnowFallChance() {
        return moderateSnowfallChance;
    }

    public void setModerateRainFallChance(int chance) {
        moderateRainfallChance = chance;
    }

    public int getModerateRainFallChance() {
        return moderateRainfallChance;
    }

    public void setHeavySnowfallChance(int chance) {
        heavySnowfallChance = chance;
    }

    public int getHeavySnowfallChance() {
        return heavySnowfallChance;
    }

    public void setLightRainfallChance(int chance) {
        lightRainfallChance = chance;
    }

    public int getLightRainfallChance() {
        return lightRainfallChance;
    }

    public void setHeavyRainfallChance(int chance) {
        heavyRainfallChance = chance;
    }

    public int getHeavyRainfallChance() {
        return heavyRainfallChance;
    }

    public void setModerateWindChance(int chance) {
        moderateWindChance = chance;
    }

    public int getModerateWindChance() {
        return moderateWindChance;
    }

    public void setStrongWindChance(int chance) {
        strongWindChance = chance;
    }

    public int getStrongWindChance() {
        return strongWindChance;
    }

    public void setStormWindChance(int chance) {
        stormWindChance = chance;
    }

    public int getStormWindChance() {
        return stormWindChance;
    }

    public void setLightWindChance(int chance) {
        lightWindChance = chance;
    }

    public int getLightWindChance() {
        return lightWindChance;
    }

    public void setTornadoF13WindChance(int chance) {
        tornadoF13WindChance = chance;
    }

    public int getTornadoF13WindChance() {
        return tornadoF13WindChance;
    }

    public void setTornadoF4WindChance(int chance) {
        tornadoF4WindChance = chance;
    }

    public int getTornadoF4WindChance() {
        return tornadoF4WindChance;
    }

    public void setDownPourChance(int chance) {
        downPourChance = chance;
    }

    public int getDownPourChance() {
        return downPourChance;
    }

    public void setLightSnowfallChance(int chance) {
        lightSnowfallChance = chance;
    }

    public int getLightSnowfallChance() {
        return lightSnowfallChance;
    }

    public void setSleetChance(int chance) {
        sleetChance = chance;
    }

    public int getSleetChance() {
        return sleetChance;
    }

    public void setIceStormChance(int chance) {
        iceStormChance = chance;
    }

    public int getIceStormChance() {
        return iceStormChance;
    }

    public void setLightHailChance(int chance) {
        lightHailChance = chance;
    }

    public int getLightHailChance() {
        return lightHailChance;
    }

    public void setHeavyHailChance(int chance) {
        heavyHailChance = chance;
    }

    public int getHeavyHailChance() {
        return heavyHailChance;
    }

    public AdvancedTerrain clone() {
        AdvancedTerrain clone = new AdvancedTerrain();
        clone.setAtmosphere(atmosphere);
        clone.setDisplayName(displayName);
        clone.setDownPourChance(downPourChance);
        clone.setDuskChance(duskChance);
        clone.setEMI(emi);
        clone.setFog(fog);
        clone.setGravity(gravity);
        clone.setHeavyfogChance(heavyFogChance);
        clone.setHeavyHailChance(heavyHailChance);
        clone.setHeavyRainfallChance(heavyRainfallChance);
        clone.setHeavySnowfallChance(heavySnowfallChance);
        clone.setHighTemp(highTemp);
        clone.setIceStormChance(iceStormChance);
        clone.setLightConditions(lightConditions);
        clone.setLightFogChance(lightFogChance);
        clone.setLightHailChance(lightHailChance);
        clone.setLightRainfallChance(lightRainfallChance);
        clone.setLightSnowfallChance(lightSnowfallChance);
        clone.setLightWindChance(lightWindChance);
        clone.setLowTemp(lowTemp);
        clone.setMaxWindStrength(maxWindStrength);
        clone.setModerateRainFallChance(moderateRainfallChance);
        clone.setModerateSnowFallChance(moderateSnowfallChance);
        clone.setModerateWindChance(moderateWindChance);
        clone.setMoonlessNightChance(moonlessNightChance);
        clone.setName(name);
        clone.setNightChance(fullMoonChance);
        clone.setNightTempMod(nightTempMod);
        clone.setPitchBlackNightChance(pitchBlackChance);
        clone.setShiftingWindDirection(shiftingWindDirection);
        clone.setShiftingWindStrength(shiftingWindStrength);
        clone.setSleetChance(sleetChance);
        clone.setStormWindChance(stormWindChance);
        clone.setStrongWindChance(strongWindChance);
        clone.setTemperature(temperature);
        clone.setTerrainAffected(terrainAffected);
        clone.setTornadoF13WindChance(tornadoF13WindChance);
        clone.setTornadoF4WindChance(tornadoF4WindChance);
        clone.setWeatherConditions(weatherConditions);
        clone.setWindDirection(windDirection);
        clone.setWindStrength(windStrength);
       
        return clone;
    }

    public void setLightConditions(Light light) {
        lightConditions = light;
    }

    public Light getLightConditions() {
        return lightConditions;
    }

    public void setWeatherConditions(Weather weather) {
        weatherConditions = weather;
    }

    public Weather getWeatherConditions() {
        return weatherConditions;
    }

    public void setWindStrength(Wind wind) {
        windStrength = wind;
    }

    public Wind getWindStrength() {
        return windStrength;
    }

    public void setWindDirection(WindDirection dir) {
        windDirection = dir;
    }

    public WindDirection getWindDirection() {
        return windDirection;
    }

    public void setShiftingWindDirection(boolean shift) {
        shiftingWindDirection = shift;
    }

    public boolean hasShifitingWindDirection() {
        return shiftingWindDirection;
    }

    public void setShiftingWindStrength(boolean strength) {
        shiftingWindStrength = strength;
    }

    public boolean hasShifitingWindStrength() {
        return shiftingWindStrength;
    }

    public String toStringPlanetaryConditions() {
        StringBuilder results = new StringBuilder();

        results.append(toString());
        results.append("$");
        results.append(lightConditions);
        results.append("$");
        results.append(weatherConditions);
        results.append("$");
        results.append(windStrength);
        results.append("$");
        results.append(windDirection);
        results.append("$");
        results.append(shiftingWindDirection);
        results.append("$");
        results.append(shiftingWindStrength);
        results.append("$");
        results.append(fog);
        results.append("$");
        results.append(temperature);
        results.append("$");
        results.append(emi);
        results.append("$");
        results.append(terrainAffected);
        results.append("$");
        results.append(maxWindStrength);

        return results.toString();
    }

    public boolean isTerrainAffected() {
        return terrainAffected;
    }

    public void setTerrainAffected(boolean terrain) {
        terrainAffected = terrain;
    }

    public EMI hasEMI() {
        return emi;
    }

    public void setEMI(EMI emi) {
        this.emi = emi;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temp) {
        temperature = temp;
    }

    public Fog getFog() {
        return fog;
    }

    public void setFog(Fog fog) {
        this.fog = fog;
    }

    public Atmosphere getAtmosphere() {
        return atmosphere;
    }

    public void setAtmosphere(Atmosphere atmosphere) {
        this.atmosphere = atmosphere;
    }

    public int getLightFogChance() {
        return lightFogChance;
    }

    public void setLightFogChance(int chance) {
        lightFogChance = chance;
    }

    public int getHeavyFogChance() {
        return heavyFogChance;
    }

    public void setHeavyfogChance(int chance) {
        heavyFogChance = chance;
    }

    public int getEMIChance() {
        return emiChance;
    }

    public void setEMIChance(int chance) {
        emiChance = chance;
    }

    public void setMaxWindStrength(Wind wind) {
        maxWindStrength = wind;
    }

    public Wind getMaxWindStrength() {
        return maxWindStrength;
    }

    public int getId() {
        return id;
    }

    public void setId(int unusedTerrainID) {
        id = unusedTerrainID;        
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        displayName = name;
        this.name = name;
    }

    public String toImageDescription() {
         StringBuilder results = new StringBuilder();
        
        results.append("<table><TR>");
        results.append("<TD>");
        results.append("lightConditions");
        results.append("</TD><TD>");
        results.append("weatherConditions");
        results.append("</TD><TD>");
        results.append("windStrength");
        results.append("</TD><TD>");
        results.append("windDirection");
        results.append("</TD><TD>");
        results.append("shiftingWindDirection");
        results.append("</TD><TD>");
        results.append("shiftingWindStrength");
        results.append("</TD><TD>");
        results.append("fog");
        results.append("</TD><TD>");
        results.append("temperature");
        results.append("</TD><TD>");
        results.append("emi");
        results.append("</TD><TD>");
        results.append("terrainAffected");
        results.append("</TD><TD>");
        results.append("maxWindStrength");        
        results.append("</TD></TR><TR><TD>");
        results.append(lightConditions.toString());
        results.append("</TD><TD>");
        results.append(weatherConditions.toString());
        results.append("</TD><TD>");
        results.append(windStrength.toString());
        results.append("</TD><TD>");
        results.append(windDirection);
        results.append("</TD><TD>");
        results.append(shiftingWindDirection);
        results.append("</TD><TD>");
        results.append(shiftingWindStrength);
        results.append("</TD><TD>");
        results.append(fog.toString());
        results.append("</TD><TD>");
        results.append(temperature);
        results.append("</TD><TD>");
        results.append(emi);
        results.append("</TD><TD>");
        results.append(terrainAffected);
        results.append("</TD><TD>");
        results.append(maxWindStrength.toString());
        results.append("</TR><table>");
        
        
        
        return results.toString();

    }

    public String WeatherForcast()
    {
        Light worstLight = Light.DAY;
        float worstLightProb = 0;
        Light likelyLight = Light.DAY;
        float lightProb = 0;
        Weather worstWeather = Weather.CLEAR;
        float worstWeatherProb = 0;
        Weather likelyWeather = Weather.CLEAR;
        float weatherProb = 0;
        Wind worstWind = Wind.CALM;
        float worstWindProb = 0;
        Wind likelyWind = Wind.CALM;
        float windProb = 0;
        
        StringBuilder results = new StringBuilder();
        
        //find the worst light conditions and the most likely conditions (other than day)
        if (duskChance > 0) {
            likelyLight = worstLight = Light.DUSK;
            lightProb = worstLightProb = duskChance;
        }
        if (fullMoonChance > 0) {
            if (fullMoonChance > lightProb) {
                likelyLight = Light.FULL_MOON;
                lightProb = fullMoonChance;
            }
            worstLight = Light.FULL_MOON;
            worstLightProb = fullMoonChance;
        }
        if (moonlessNightChance > 0) {
            if (moonlessNightChance > lightProb)  {
                likelyLight = Light.MOONLESS;
                lightProb = moonlessNightChance;
            }
            worstLight = Light.MOONLESS;
            worstLightProb = moonlessNightChance;
        }
        if (pitchBlackChance > 0) {
            if (pitchBlackChance > lightProb) {
                likelyLight = Light.PITCH_BLACK;
                lightProb = pitchBlackChance;
            }
            worstLight = Light.PITCH_BLACK;
            worstLightProb = pitchBlackChance;
        }

        results.append("likely / worst <br>");
        results.append("Light:");
        if (lightProb > 0){
            results.append(lightProb/10);
            results.append("% ");
            results.append(likelyLight.toString());
            results.append(" / ");
            results.append(worstLightProb / 10);
            results.append("% ");
            results.append(worstLight.toString());
            results.append("<br>");            
        } else {
            results.append("100% Daylight");            
            results.append("<br>");                        
        }
            
        if (lightRainfallChance > 0) {
            likelyWeather = worstWeather = Weather.LIGHT_RAIN;
            weatherProb = worstWeatherProb = lightRainfallChance;
        }
        if (lightSnowfallChance > 0) {
            if (lightSnowfallChance > weatherProb) {
                likelyWeather= Weather.LIGHT_SNOW;
                weatherProb = lightSnowfallChance;
            }
            worstWeather = Weather.LIGHT_SNOW;
            worstWeatherProb = lightSnowfallChance;
        }    
        if (moderateRainfallChance > 0) {
            if (moderateRainfallChance > weatherProb) {
                likelyWeather = Weather.MOD_RAIN;
                weatherProb = moderateRainfallChance;
            }
            worstWeather = Weather.MOD_RAIN;
            worstWeatherProb = moderateRainfallChance;
        }    
        if (moderateSnowfallChance > 0) {
            if (moderateSnowfallChance > weatherProb) {
                likelyWeather = Weather.MOD_SNOW;
                weatherProb = moderateSnowfallChance;
            }
            worstWeather = Weather.MOD_SNOW;
            worstWeatherProb = moderateSnowfallChance;
        }    
        if (heavyRainfallChance > 0) {
            if (heavyRainfallChance > weatherProb) {
                likelyWeather = Weather.HEAVY_RAIN;
                weatherProb = heavyRainfallChance;
            }
            worstWeather = Weather.HEAVY_RAIN;
            worstWeatherProb = heavyRainfallChance;
        }    
        if (heavySnowfallChance > 0) {
            if (heavySnowfallChance > weatherProb) {
                likelyWeather = Weather.HEAVY_SNOW;
                weatherProb = heavySnowfallChance;
            }
            worstWeather = Weather.HEAVY_SNOW;
            worstWeatherProb = heavySnowfallChance;
        }    
        if (downPourChance > 0) {
            if (downPourChance > weatherProb) {
                likelyWeather = Weather.DOWNPOUR;
                weatherProb = downPourChance;
            }
            worstWeather = Weather.DOWNPOUR;
            worstWeatherProb = downPourChance;
        }    

        results.append("Weather:");
        if (weatherProb > 0) {
            results.append(weatherProb / 10);
            results.append("% ");
            results.append(likelyWeather.toString());
            results.append(" / ");
            results.append(worstWeatherProb / 10);
            results.append("% ");
            results.append(worstWeather.toString());
            if (lightHailChance > 0 || heavyHailChance > 0)
                results.append(" (hail)");                            
            if (sleetChance > 0)
                results.append(" (sleet)");        
            if (iceStormChance > 0)
                results.append(" (ice storm)");        
            results.append("<br>");                                    
        } else {
            results.append("100% CLEAR");            
            results.append("<br>");                        
        }
        
        if (lightWindChance > 0) {
            likelyWind = worstWind = Wind.LIGHT_GALE;
            windProb = worstWindProb = lightWindChance;
        }
        if (moderateWindChance > 0) {
            if (moderateWindChance > weatherProb) {
                likelyWind = Wind.MOD_GALE;
                windProb = moderateWindChance;
            }
            worstWind = Wind.MOD_GALE;
            worstWindProb = moderateWindChance;
        }    
        if (strongWindChance > 0) {
            if (strongWindChance > weatherProb) {
                likelyWind = Wind.STRONG_GALE;
                windProb = strongWindChance;
            }
            worstWind = Wind.STRONG_GALE;
            worstWindProb = strongWindChance;
        }    
        if (stormWindChance > 0) {
            if (stormWindChance > weatherProb) {
                likelyWind = Wind.STORM;
                windProb = stormWindChance;
            }
            worstWind = Wind.STORM;
            worstWindProb = stormWindChance;
        }    
        if (tornadoF13WindChance > 0) {
            if (tornadoF13WindChance > weatherProb) {
                likelyWind = Wind.TORNADO_F1_TO_F3;
                windProb = tornadoF13WindChance;
            }
            worstWind = Wind.TORNADO_F1_TO_F3;
            worstWindProb = tornadoF13WindChance;
        }    
        if (tornadoF4WindChance > 0) {
            if (tornadoF4WindChance > weatherProb) {
                likelyWind = Wind.TORNADO_F4;
                windProb = tornadoF4WindChance;
            }
            worstWind = Wind.TORNADO_F4;
            worstWindProb = tornadoF4WindChance;
        }    

        results.append("Wind:");
        if (windProb > 0) {
            results.append(windProb / 10);
            results.append("% ");
            results.append(likelyWind.toString());
            results.append(" / ");
            results.append(worstWindProb / 10);
            results.append("% ");
            results.append(worstWind.toString());
            results.append("<br>");            
        } else {
            results.append("100% Calm");            
            results.append("<br>");                        
        }
        
        
        if (lightFogChance > 0 || heavyFogChance > 0) {
            results.append("Fog:");
            results.append((float) Math.max(lightFogChance, heavyFogChance) / 10);
            results.append("% ");
        }
        results.append("<br>");                        
        
        
        return results.toString();
    }

    public String getHumanReadableWeather() {
        StringBuilder results = new StringBuilder();
        int adverse = 0;
        
        results.append(lightConditions.toString());
        results.append("/" + weatherConditions.toString());
        results.append("/" + windStrength.toString());
        results.append("/" + fog.toString());
        results.append("/" + atmosphere.toString());
        results.append("/");
        results.append(gravity);
        if (lightConditions != Light.DAY) adverse++;
        if (weatherConditions != Weather.CLEAR) adverse++;
        if (windStrength != Wind.CALM) adverse++;
        if (fog != Fog.FOG_NONE) adverse++;
        if (atmosphere != Atmosphere.STANDARD) adverse++;
        if (gravity != 1.0) adverse++;
        
        results.append("/" + adverse);
        
        return results.toString();
    }
}
