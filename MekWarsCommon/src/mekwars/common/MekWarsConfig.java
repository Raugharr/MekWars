/*
 * MekWars - Copyright (C) 2026
 *
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet)
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */

package mekwars.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Properties;

public abstract class MekWarsConfig {
    private static final Logger LOGGER = LogManager.getLogger(MekWarsConfig.class);
    private static final int TYPE_SIZE = Unit.AERO + 1;
    private static final int WEIGHT_SIZE = Unit.ASSAULT + 1;

    private int[][] unitLimits = new int[TYPE_SIZE][WEIGHT_SIZE];
    private boolean[][] bmLimits = new boolean[TYPE_SIZE][WEIGHT_SIZE];
    private Properties config = new Properties();
    private Path path;

    public MekWarsConfig(Path path) {
        this.path = path;
    }

    public boolean getBooleanConfig(String key) {
        try {
            return Boolean.parseBoolean(this.getConfig(key));
        } catch (Exception ex) {
            return false;
        }
    }

    public int getIntegerConfig(String key) {
        try {
            return Integer.parseInt(this.getConfig(key));
        } catch (Exception ex) {
            return -1;
        }
    }

    public double getDoubleConfig(String key) {
        try {
            return Double.parseDouble(this.getConfig(key));
        } catch (Exception ex) {
            return -1;
        }
    }

    public float getFloatConfig(String key) {
        try {
            return Float.parseFloat(this.getConfig(key));
        } catch (Exception ex) {
            return -1;
        }
    }

    public long getLongConfig(String key) {
        try {
            return Long.parseLong(this.getConfig(key));
        } catch (Exception ex) {
            return -1;
        }
    }

    public abstract String getConfig(String key);

    protected Properties getConfig() {
        return config;
    }

    /**
     * A method that returns the hangar limit for a given weight/type of unit
     *
     * @param unitType
     * @param unitWeightClass
     * @return -1 if it is unlimited or a malformed request, the limit otherwise
     */
    public int getUnitLimit(int unitType, int unitWeight) {
        if (unitType < 0 || unitType >= TYPE_SIZE || unitWeight < 0 || unitWeight >= WEIGHT_SIZE) {
            LOGGER.error("Invalid get unit limit query: type={} weight={}", unitType, unitWeight);
            return -1;
        }
        return unitLimits[unitType][unitWeight];
    }

    public boolean canBuyFromBM(int unitType, int unitWeight) {
        if (unitType < 0 || unitType >= TYPE_SIZE || unitWeight < 0 || unitWeight >= WEIGHT_SIZE) {
            LOGGER.error("Invalid BM limit query: type={} weight={}", unitType, unitWeight);
            return false;
        }
        return bmLimits[unitType][unitWeight];
    }

    public void setCanBuyFromBM(int unitType, int unitWeight, boolean canBuy) {
        if (unitType < 0 || unitType >= TYPE_SIZE || unitWeight < 0 || unitWeight >= WEIGHT_SIZE) {
            LOGGER.error("Invalid BM set limit query: type={} weight={}", unitType, unitWeight);
            return;
        }

        bmLimits[unitType][unitWeight] = canBuy;
    }

    public void save() {
        long currentTime = System.currentTimeMillis();

        /* NOTE: Some places expect the first 11 characters of a configuration to be #TIMESTAMP=
         * followed by a timestamp, House configurations expect a TIMESTAMP property,
         * here we do both.
         */
        setProperty("TIMESTAMP", Long.toString(currentTime));
        try (OutputStream os = Files.newOutputStream(path);
                BufferedOutputStream bos = new BufferedOutputStream(os);
                PrintStream printStream = new PrintStream(bos, false, StandardCharsets.UTF_8)) {
            printStream.println("#Timestamp=" + currentTime);
            getConfig().store(printStream, "Server Config");
            printStream.flush();
        } catch (IOException exception) {
            LOGGER.error("Unable to save config to {}", path, exception);
        }
    }

    public void clear() {
        config.clear();
    }

    public Enumeration<?> propertyNames() {
        return config.propertyNames();
    }

    public void setProperty(String key, String value) {
        config.setProperty(key, value);
    }

    public void load() throws IOException {
        getConfig().load(Files.newInputStream(path));
        onLoad();
    }

    public Path getPath() {
        return path;
    }

    protected void onLoad() {
        populateBMLimits();
        populateUnitLimits();
    }

    /** A method to fill the unitLimits array */
    private void populateUnitLimits() {
        unitLimits[Unit.MEK][Unit.LIGHT] = getIntegerConfig("MaxHangarLightMek");
        unitLimits[Unit.MEK][Unit.MEDIUM] = getIntegerConfig("MaxHangarMediumMek");
        unitLimits[Unit.MEK][Unit.HEAVY] = getIntegerConfig("MaxHangarHeavyMek");
        unitLimits[Unit.MEK][Unit.ASSAULT] = getIntegerConfig("MaxHangarAssaultMek");

        unitLimits[Unit.VEHICLE][Unit.LIGHT] = getIntegerConfig("MaxHangarLightVehicle");
        unitLimits[Unit.VEHICLE][Unit.MEDIUM] = getIntegerConfig("MaxHangarMediumVehicle");
        unitLimits[Unit.VEHICLE][Unit.HEAVY] = getIntegerConfig("MaxHangarHeavyVehicle");
        unitLimits[Unit.VEHICLE][Unit.ASSAULT] = getIntegerConfig("MaxHangarAssaultVehicle");

        unitLimits[Unit.INFANTRY][Unit.LIGHT] = getIntegerConfig("MaxHangarLightInfantry");
        unitLimits[Unit.INFANTRY][Unit.MEDIUM] = getIntegerConfig("MaxHangarMediumInfantry");
        unitLimits[Unit.INFANTRY][Unit.HEAVY] = getIntegerConfig("MaxHangarHeavyInfantry");
        unitLimits[Unit.INFANTRY][Unit.ASSAULT] = getIntegerConfig("MaxHangarAssaultInfantry");

        unitLimits[Unit.PROTOMEK][Unit.LIGHT] = getIntegerConfig("MaxHangarLightProtoMek");
        unitLimits[Unit.PROTOMEK][Unit.MEDIUM] = getIntegerConfig("MaxHangarMediumProtoMek");
        unitLimits[Unit.PROTOMEK][Unit.HEAVY] = getIntegerConfig("MaxHangarHeavyProtoMek");
        unitLimits[Unit.PROTOMEK][Unit.ASSAULT] = getIntegerConfig("MaxHangarAssaultProtoMek");

        unitLimits[Unit.BATTLEARMOR][Unit.LIGHT] = getIntegerConfig("MaxHangarLightBattleArmor");
        unitLimits[Unit.BATTLEARMOR][Unit.MEDIUM] = getIntegerConfig("MaxHangarMediumBattleArmor");
        unitLimits[Unit.BATTLEARMOR][Unit.HEAVY] = getIntegerConfig("MaxHangarHeavyBattleArmor");
        unitLimits[Unit.BATTLEARMOR][Unit.ASSAULT] =
                getIntegerConfig("MaxHangarAssaultBattleArmor");

        unitLimits[Unit.AERO][Unit.LIGHT] = getIntegerConfig("MaxHangarLightAero");
        unitLimits[Unit.AERO][Unit.MEDIUM] = getIntegerConfig("MaxHangarMediumAero");
        unitLimits[Unit.AERO][Unit.HEAVY] = getIntegerConfig("MaxHangarHeavyAero");
        unitLimits[Unit.AERO][Unit.ASSAULT] = getIntegerConfig("MaxHangarAssaultAero");
    }

    private void populateBMLimits() {
        bmLimits[Unit.MEK][Unit.LIGHT] = getBooleanConfig("CanBuyBMLightMeks");
        bmLimits[Unit.MEK][Unit.MEDIUM] = getBooleanConfig("CanBuyBMMediumMeks");
        bmLimits[Unit.MEK][Unit.HEAVY] = getBooleanConfig("CanBuyBMHeavyMeks");
        bmLimits[Unit.MEK][Unit.ASSAULT] = getBooleanConfig("CanBuyBMAssaultMeks");

        bmLimits[Unit.VEHICLE][Unit.LIGHT] = getBooleanConfig("CanBuyBMLightVehicles");
        bmLimits[Unit.VEHICLE][Unit.MEDIUM] = getBooleanConfig("CanBuyBMMediumVehicles");
        bmLimits[Unit.VEHICLE][Unit.HEAVY] = getBooleanConfig("CanBuyBMHeavyVehicles");
        bmLimits[Unit.VEHICLE][Unit.ASSAULT] = getBooleanConfig("CanBuyBMAssaultVehicles");

        bmLimits[Unit.INFANTRY][Unit.LIGHT] = getBooleanConfig("CanBuyBMLightInfantry");
        bmLimits[Unit.INFANTRY][Unit.MEDIUM] = getBooleanConfig("CanBuyBMMediumInfantry");
        bmLimits[Unit.INFANTRY][Unit.HEAVY] = getBooleanConfig("CanBuyBMHeavyInfantry");
        bmLimits[Unit.INFANTRY][Unit.ASSAULT] = getBooleanConfig("CanBuyBMAssaultInfantry");

        bmLimits[Unit.BATTLEARMOR][Unit.LIGHT] = getBooleanConfig("CanBuyBMLightBA");
        bmLimits[Unit.BATTLEARMOR][Unit.MEDIUM] = getBooleanConfig("CanBuyBMMediumBA");
        bmLimits[Unit.BATTLEARMOR][Unit.HEAVY] = getBooleanConfig("CanBuyBMHeavyBA");
        bmLimits[Unit.BATTLEARMOR][Unit.ASSAULT] = getBooleanConfig("CanBuyBMAssaultBA");

        bmLimits[Unit.PROTOMEK][Unit.LIGHT] = getBooleanConfig("CanBuyBMLightProtomeks");
        bmLimits[Unit.PROTOMEK][Unit.MEDIUM] = getBooleanConfig("CanBuyBMMediumProtomeks");
        bmLimits[Unit.PROTOMEK][Unit.HEAVY] = getBooleanConfig("CanBuyBMHeavyProtomeks");
        bmLimits[Unit.PROTOMEK][Unit.ASSAULT] = getBooleanConfig("CanBuyBMAssaultProtomeks");

        bmLimits[Unit.AERO][Unit.LIGHT] = getBooleanConfig("CanBuyBMLightAero");
        bmLimits[Unit.AERO][Unit.MEDIUM] = getBooleanConfig("CanBuyBMMediumAero");
        bmLimits[Unit.AERO][Unit.HEAVY] = getBooleanConfig("CanBuyBMHeavyAero");
        bmLimits[Unit.AERO][Unit.ASSAULT] = getBooleanConfig("CanBuyBMAssaultAero");
    }
}
