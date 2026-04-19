/*
 * MekWars - Copyright (C) 2026
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

package mekwars.common.campaign;

import megamek.common.CriticalSlot;
import megamek.common.Entity;
import megamek.common.Mech;
import megamek.common.Mounted;
import megamek.common.WeaponType;

import mekwars.common.CampaignData;
import mekwars.common.House;
import mekwars.common.util.UnitUtils;

import java.util.List;

public final class UnitRepairCostCalculator {
    private UnitRepairCostCalculator() {}

    @FunctionalInterface
    interface GetArmor<T> {
        int execute(Entity unit, int location);
    }

    public static double getArmorCost(Entity unit, int location) {
        if (CampaignData.cd.getCampaignOptions().getBooleanConfig("UsePartsRepair")) {
            return 0;
        }

        String armorCost = "CostPoint" + UnitUtils.getArmorShortName(unit, location);
        return CampaignData.cd.getCampaignOptions().getDoubleConfig(armorCost);
    }

    /**
     * TODO: This should probably replace getArmorCost but we aren't sure as we are still
     * refactoring.
     *
     * @param armorLocation Must be LOC_FRONT_ARMOR, LOC_REAR_ARMOR, or LOC_INTERNAL_ARMOR
     */
    public static double getFullArmorCost(
            Entity unit,
            int location,
            int armorLocation,
            double techCost,
            int techWorkMod,
            GetArmor externalArmor,
            GetArmor internalArmor) {
        double cost = getArmorCost(unit, location);
        int pointsToRepair = 0;

        if (externalArmor.execute(unit, location) > internalArmor.execute(unit, location)) {
            // remove the repairing armor so we can get the real
            // cost.
            UnitUtils.removeArmorRepair(unit, armorLocation, location);
            pointsToRepair =
                    internalArmor.execute(unit, location) - externalArmor.execute(unit, location);
            // Add the repairing armor flag back on.
            UnitUtils.setArmorRepair(unit, armorLocation, location);
        } else {
            pointsToRepair =
                    internalArmor.execute(unit, location) - externalArmor.execute(unit, location);
        }
        return Math.max(1, getCost(cost, pointsToRepair, techCost, techWorkMod));
    }

    public static double getStructureCost(Entity unit) {
        if (CampaignData.cd.getCampaignOptions().getBooleanConfig("UsePartsRepair")) {
            return 0;
        }

        String armorCost = "CostPoint" + UnitUtils.getInternalShortName(unit) + "IS";
        return CampaignData.cd.getCampaignOptions().getDoubleConfig(armorCost);
    }

    /**
     * Calculates the cost of repairing broken equipment of the same type by technician and the
     * technicians repair roll modifier.
     *
     * @param cost The cost of the equipment
     * @param amount How much of the equipment to repair
     * @param techCost The cost of the technician repairing the equipment {@see TechRepairCost}
     *     configurations.
     * @param techWorkMod The modifier roll for the technician performing this repair.
     */
    public static double getCost(double cost, double amount, double techCost, double techWorkMod) {
        return (cost * amount) + techCost * Math.abs(techWorkMod) + techCost;
    }

    public static double getCritCost(Entity unit, CriticalSlot crit) {
        double cost = 0;

        if (CampaignData.cd.getCampaignOptions().getBooleanConfig("UsePartsRepair")) {
            return 0;
        }

        if (crit == null) {
            return 0;
        }

        if (crit.isBreached() && !crit.isDamaged()) {
            return 0;
        }

        CampaignOptions campaignOptions = CampaignData.cd.getCampaignOptions();

        if (UnitUtils.isEngineCrit(crit)) {
            cost = campaignOptions.getDoubleConfig("EngineCritRepairCost");
        } else if (crit.getType() == CriticalSlot.TYPE_SYSTEM) {
            if (crit.isMissing()) {
                cost = campaignOptions.getDoubleConfig("SystemCritReplaceCost");
            } else {
                cost = campaignOptions.getDoubleConfig("SystemCritRepairCost");
            }
        } else {
            Mounted mounted = crit.getMount();

            if (mounted.getType() instanceof WeaponType) {
                WeaponType weapon = (WeaponType) mounted.getType();
                if (weapon.hasFlag(WeaponType.F_ENERGY)) {
                    if (crit.isMissing()) {
                        cost = campaignOptions.getDoubleConfig("EnergyWeaponCritReplaceCost");
                    } else {
                        cost = campaignOptions.getDoubleConfig("EnergyWeaponCritRepairCost");
                    }
                } else if (weapon.hasFlag(WeaponType.F_BALLISTIC)) {
                    if (crit.isMissing()) {
                        cost = campaignOptions.getDoubleConfig("BallisticCritReplaceCost");
                    } else {
                        cost = campaignOptions.getDoubleConfig("BallisticCritRepairCost");
                    }
                } else if (weapon.hasFlag(WeaponType.F_MISSILE)) {
                    if (crit.isMissing()) {
                        cost = campaignOptions.getDoubleConfig("MissileCritReplaceCost");
                    } else {
                        cost = campaignOptions.getDoubleConfig("MissileCritRepairCost");
                    }
                } else // use the misc eq costs.
                if (crit.isMissing()) {
                    cost = campaignOptions.getDoubleConfig("EquipmentCritReplaceCost");
                } else {
                    cost = campaignOptions.getDoubleConfig("EquipmentCritRepairCost");
                }
            } else // use the misc eq costs.
            if (crit.isMissing()) {
                cost = campaignOptions.getDoubleConfig("EquipmentCritReplaceCost");
            } else {
                cost = campaignOptions.getDoubleConfig("EquipmentCritRepairCost");
            }
        }
        return Math.max(1, cost);
    }

    public static int getTotalRepairCosts(
            Entity unit, List<Integer> techs, List<Integer> rolls, int pilotLevel, House house) {
        double cost = 0;
        double totalArmorCost = 0;
        double internalCost = 0;
        double systemsCost = 0;
        double equipmentCost = 0;
        double weaponsCost = 0;
        double engineCost = 0;

        int techType = techs.get(UnitUtils.ARMOR);
        int baseRoll = rolls.get(UnitUtils.ARMOR);

        double pointsToRepair = 0;
        double armorCost = 0;
        double techCost = 0;
        double techWorkMod = 0;

        if (techType != UnitUtils.TECH_PILOT) {
            techCost =
                    CampaignData.cd
                            .getCampaignOptions()
                            .getIntegerConfig(
                                    UnitUtils.techDescription(techType) + "TechRepairCost");
            techWorkMod =
                    UnitUtils.getTechRoll(
                                    unit,
                                    0,
                                    UnitUtils.LOC_FRONT_ARMOR,
                                    techType,
                                    true,
                                    house.getTechLevel())
                            - baseRoll;
        } else {
            techType = pilotLevel;
        }

        techWorkMod = Math.max(techWorkMod, 0);

        for (int location = 0; location < unit.locations(); location++) {
            if (unit.getArmor(location) < unit.getOArmor(location)) {
                pointsToRepair += unit.getOArmor(location) - unit.getArmor(location);
                armorCost = getArmorCost(unit, location);
                totalArmorCost += getCost(armorCost, pointsToRepair, techCost, techWorkMod);
            }

            if (unit.hasRearArmor(location)) {
                pointsToRepair += unit.getOArmor(location, true) - unit.getArmor(location, true);
                armorCost = getArmorCost(unit, location);
                totalArmorCost += getCost(armorCost, pointsToRepair, techCost, techWorkMod);
            }
        }

        // Base on what they assigned as the base roll we increase the payout so
        // that it covers the chances of failures. not the greatest but better
        // then nothing.
        totalArmorCost *= payOutIncreaseBasedOnRoll(baseRoll);
        totalArmorCost = Math.max(0, totalArmorCost);

        techType = techs.get(UnitUtils.INTERNAL);
        baseRoll = rolls.get(UnitUtils.INTERNAL);
        pointsToRepair = 0;
        armorCost = getStructureCost(unit);
        techCost = 0;
        techWorkMod = 0;

        if (techType != UnitUtils.TECH_PILOT) {
            techCost =
                    CampaignData.cd
                            .getCampaignOptions()
                            .getIntegerConfig(
                                    UnitUtils.techDescription(techType) + "TechRepairCost");
        }

        for (int location = 0; location < unit.locations(); location++) {
            if (unit.getInternal(location) < unit.getOInternal(location)) {
                if (techType != UnitUtils.TECH_PILOT) {
                    techWorkMod =
                            UnitUtils.getTechRoll(
                                            unit,
                                            location,
                                            UnitUtils.LOC_INTERNAL_ARMOR,
                                            techType,
                                            true,
                                            house.getTechLevel())
                                    - baseRoll;
                }

                techWorkMod = Math.max(techWorkMod, 0);
                pointsToRepair = unit.getOInternal(location) - unit.getInternal(location);
                internalCost += getCost(armorCost, pointsToRepair, techCost, techWorkMod);
            }
        }

        // Base on what they assigned as the base roll we increase the payout so
        // that it covers the chances of failures. not the greatest but better
        // then nothing.
        internalCost *= payOutIncreaseBasedOnRoll(baseRoll);
        internalCost = Math.max(0, internalCost);

        techType = techs.get(UnitUtils.SYSTEMS);
        baseRoll = rolls.get(UnitUtils.SYSTEMS);
        pointsToRepair = 0;
        double critCost = 0;
        techCost = 0;
        techWorkMod = 0;

        if (techType != UnitUtils.TECH_PILOT) {
            techCost =
                    CampaignData.cd
                            .getCampaignOptions()
                            .getIntegerConfig(
                                    UnitUtils.techDescription(techType) + "TechRepairCost");
        }

        for (int location = 0; location < unit.locations(); location++) {
            for (int slot = 0; slot < unit.getNumberOfCriticals(location); slot++) {
                CriticalSlot cs = unit.getCritical(location, slot);
                if (cs == null) {
                    continue;
                }
                if (!cs.isBreached() && !cs.isDamaged()) {
                    continue;
                }
                if (cs.getType() == CriticalSlot.TYPE_SYSTEM
                        && cs.getIndex() != Mech.SYSTEM_ENGINE) {
                    if (techType != UnitUtils.TECH_PILOT) {
                        techWorkMod =
                                UnitUtils.getTechRoll(
                                                unit,
                                                location,
                                                slot,
                                                techType,
                                                true,
                                                house.getTechLevel())
                                        - baseRoll;
                    }

                    critCost = getCritCost(unit, cs);
                    techWorkMod = Math.max(techWorkMod, 0);
                    pointsToRepair = UnitUtils.getNumberOfCrits(unit, cs);
                    critCost += techCost;
                    systemsCost += getCost(critCost, pointsToRepair, techCost, techWorkMod);

                    // move the slot ahead if the Crit is more then 1 in size.
                    slot += pointsToRepair - 1;
                }
            }
        }

        // Base on what they assigned as the base roll we increase the payout so
        // that it covers the chances of failures. not the greatest but better
        // then nothing.
        systemsCost *= payOutIncreaseBasedOnRoll(baseRoll);
        systemsCost = Math.max(0, systemsCost);

        techType = techs.get(UnitUtils.WEAPONS);
        baseRoll = rolls.get(UnitUtils.WEAPONS);
        pointsToRepair = 0;
        critCost = 0;
        techCost = 0;
        techWorkMod = 0;

        if (techType != UnitUtils.TECH_PILOT) {
            techCost =
                    CampaignData.cd
                            .getCampaignOptions()
                            .getIntegerConfig(
                                    UnitUtils.techDescription(techType) + "TechRepairCost");
        }

        for (int location = 0; location < unit.locations(); location++) {
            for (int slot = 0; slot < unit.getNumberOfCriticals(location); slot++) {
                CriticalSlot cs = unit.getCritical(location, slot);
                if (cs == null) {
                    continue;
                }
                if (!cs.isBreached() && !cs.isDamaged()) {
                    continue;
                }
                if (cs.getType() == CriticalSlot.TYPE_EQUIPMENT) {
                    Mounted mounted = cs.getMount();

                    if (mounted.getType() instanceof WeaponType) {
                        if (techType != UnitUtils.TECH_PILOT) {
                            techWorkMod =
                                    UnitUtils.getTechRoll(
                                                    unit,
                                                    location,
                                                    slot,
                                                    techType,
                                                    true,
                                                    house.getTechLevel())
                                            - baseRoll;
                        }

                        critCost = getCritCost(unit, cs);
                        techWorkMod = Math.max(techWorkMod, 0);
                        pointsToRepair = UnitUtils.getNumberOfCrits(unit, cs);
                        critCost += techCost;
                        weaponsCost += getCost(critCost, pointsToRepair, techCost, techWorkMod);

                        // move the slot ahead if the Crit is more then 1 in
                        // size.
                        slot += pointsToRepair - 1;
                    }
                }
            }
        }

        // Base on what they assigned as the base roll we increase the payout so
        // that it covers the chances of failures. not the greatest but better
        // then nothing.
        weaponsCost *= payOutIncreaseBasedOnRoll(baseRoll);
        weaponsCost = Math.max(0, weaponsCost);

        techType = techs.get(UnitUtils.EQUIPMENT);
        baseRoll = rolls.get(UnitUtils.EQUIPMENT);
        pointsToRepair = 0;
        critCost = 0;
        techCost = 0;
        techWorkMod = 0;

        if (techType != UnitUtils.TECH_PILOT) {
            techCost =
                    CampaignData.cd
                            .getCampaignOptions()
                            .getIntegerConfig(
                                    UnitUtils.techDescription(techType) + "TechRepairCost");
        }

        for (int location = 0; location < unit.locations(); location++) {
            for (int slot = 0; slot < unit.getNumberOfCriticals(location); slot++) {
                CriticalSlot cs = unit.getCritical(location, slot);
                if (cs == null) {
                    continue;
                }
                if (!cs.isBreached() && !cs.isDamaged()) {
                    continue;
                }
                if (cs.getType() == CriticalSlot.TYPE_EQUIPMENT) {
                    Mounted mounted = cs.getMount();

                    if (!(mounted.getType() instanceof WeaponType)) {
                        if (techType != UnitUtils.TECH_PILOT) {
                            techWorkMod =
                                    UnitUtils.getTechRoll(
                                                    unit,
                                                    location,
                                                    slot,
                                                    techType,
                                                    true,
                                                    house.getTechLevel())
                                            - baseRoll;
                        }

                        critCost = getCritCost(unit, cs);
                        techWorkMod = Math.max(techWorkMod, 0);
                        pointsToRepair = UnitUtils.getNumberOfCrits(unit, cs);
                        critCost += techCost;
                        equipmentCost += getCost(critCost, pointsToRepair, techCost, techWorkMod);
                        // move the slot ahead if the Crit is more then 1 in
                        // size.
                        slot += pointsToRepair - 1;
                    }
                }
            }
        }

        // Base on what they assigned as the base roll we increase the payout so
        // that it covers the chances of failures. not the greatest but better
        // then nothing.
        equipmentCost *= payOutIncreaseBasedOnRoll(baseRoll);
        equipmentCost = Math.max(0, equipmentCost);

        techType = techs.get(UnitUtils.ENGINES);
        baseRoll = rolls.get(UnitUtils.ENGINES);
        pointsToRepair = 0;
        critCost = 0;
        techCost = 0;
        techWorkMod = 0;

        boolean found = false;
        int location = 0, slot = 0;
        CriticalSlot cs = null;

        if (techType != UnitUtils.TECH_PILOT) {
            techCost =
                    CampaignData.cd
                            .getCampaignOptions()
                            .getIntegerConfig(
                                    UnitUtils.techDescription(techType) + "TechRepairCost");
        }

        for (int x = UnitUtils.LOC_CT; x <= UnitUtils.LOC_LT; x++) {
            for (int y = 0; y < unit.getNumberOfCriticals(x); y++) {
                cs = unit.getCritical(x, y);

                if (cs == null) {
                    continue;
                }

                if (!cs.isDamaged() && !cs.isBreached()) {
                    continue;
                }

                if (!UnitUtils.isEngineCrit(cs)) {
                    continue;
                }

                location = x;
                slot = y;
                found = true;
                break;
            }
            if (found) {
                break;
            }
        }

        if (techType != UnitUtils.TECH_PILOT) {
            techWorkMod =
                    UnitUtils.getTechRoll(
                                    unit, location, slot, techType, true, house.getTechLevel())
                            - baseRoll;
        }

        critCost = getCritCost(unit, cs);
        techWorkMod = Math.max(techWorkMod, 0);
        pointsToRepair = UnitUtils.getNumberOfCrits(unit, cs);
        engineCost += getCost(critCost, pointsToRepair, techCost, techWorkMod);

        // Base on what they assigned as the base roll we increase the payout so
        // that it covers the chances of failures. not the greatest but better
        // then nothing.
        engineCost *= payOutIncreaseBasedOnRoll(baseRoll);
        engineCost = Math.max(0, engineCost);

        if (!found) {
            engineCost = 0;
        }

        cost =
                totalArmorCost
                        + engineCost
                        + systemsCost
                        + internalCost
                        + weaponsCost
                        + equipmentCost;
        return (int) cost;
    }

    public static int getRepairCost(
            Entity unit,
            int critLocation,
            int critSlot,
            int techType,
            boolean armor,
            int techWorkMod) {
        return getRepairCost(unit, critLocation, critSlot, techType, armor, techWorkMod, false);
    }

    public static int getRepairCost(
            Entity unit,
            int critLocation,
            int critSlot,
            int techType,
            boolean armor,
            int techWorkMod,
            boolean salvage) {
        CampaignOptions campaignOptions = CampaignData.cd.getCampaignOptions();
        double totalCost = 1;
        double techCost = 0;
        double cost = 1;
        int totalCrits = 1;
        int year = campaignOptions.getIntegerConfig("CampaignYear");

        if (techType < UnitUtils.TECH_PILOT) {
            techCost =
                    campaignOptions.getIntegerConfig(
                            UnitUtils.techDescription(techType) + "TechRepairCost");
        }

        if (campaignOptions.getBooleanConfig("UseRealRepairCosts")) {
            double realCost = UnitUtils.getPartCost(unit, critLocation, critSlot, armor, year);
            if (campaignOptions.getBooleanConfig("UsePartsRepair")) {
                realCost = 0;
            }

            double costMod = campaignOptions.getDoubleConfig("RealRepairCostMod");
            // modify the cost
            if (costMod > 0) {
                realCost *= costMod;
            }

            cost += (techCost * Math.abs(techWorkMod)) + realCost;
        } else {
            if (armor) {
                if (critSlot == UnitUtils.LOC_FRONT_ARMOR) {
                    cost =
                            getFullArmorCost(
                                    unit,
                                    critLocation,
                                    UnitUtils.LOC_FRONT_ARMOR,
                                    techCost,
                                    techWorkMod,
                                    (fUnit, fLocation) -> fUnit.getArmor(fLocation),
                                    (fUnit, fLocation) -> fUnit.getOArmor(fLocation));
                } else if (critSlot == UnitUtils.LOC_REAR_ARMOR) {
                    // tell the repair command its using rear external armor
                    // Need to move this above the getArmorCost because it's
                    // sending back index to get the loc.
                    // 07 Sept 2011 - Cord Awtry
                    if (critLocation >= UnitUtils.LOC_CTR) {
                        critLocation -= 7;
                    }
                    cost =
                            getFullArmorCost(
                                    unit,
                                    critLocation,
                                    UnitUtils.LOC_REAR_ARMOR,
                                    techCost,
                                    techWorkMod,
                                    (fUnit, fLocation) -> fUnit.getArmor(fLocation, true),
                                    (fUnit, fLocation) -> fUnit.getOArmor(fLocation, true));
                } else {
                    cost =
                            getFullArmorCost(
                                    unit,
                                    critLocation,
                                    UnitUtils.LOC_INTERNAL_ARMOR,
                                    techCost,
                                    techWorkMod,
                                    (fUnit, fLocation) -> fUnit.getInternal(fLocation),
                                    (fUnit, fLocation) -> fUnit.getInternal(fLocation));
                }
            } else {
                CriticalSlot cs = unit.getCritical(critLocation, critSlot);
                if (salvage) {
                    totalCrits =
                            UnitUtils.getNumberOfCrits(unit, cs)
                                    - UnitUtils.getNumberOfDamagedCrits(
                                            unit, critSlot, critLocation, armor);
                } else {
                    totalCrits =
                            UnitUtils.getNumberOfDamagedCrits(unit, critSlot, critLocation, armor);
                }
                cost = getCritCost(unit, cs);
                totalCost = (int) (totalCrits * cost);
                totalCost += (int) (totalCrits * techCost);
                totalCost += techCost;
                totalCost += techCost * Math.abs(techWorkMod);
                cost = Math.max(1, totalCost);
            }
        }

        if (campaignOptions.getBooleanConfig("AllowCritRepairsForRewards")
                && techType == UnitUtils.TECH_REWARD_POINTS) {
            cost = totalCrits * campaignOptions.getDoubleConfig("RewardPointsForCritRepair");
            cost = Math.max(Math.ceil(cost), 1);
        }

        return (int) cost;
    }

    public static double payOutIncreaseBasedOnRoll(int roll) {
        final double[] payout = {
            1.0, 1.0, 1.0, 1.03, 1.09, 1.20, 1.38, 1.72, 2.40, 3.60, 5.92, 12.0, 36.0
        };
        int finalRoll = Math.min(Math.max(roll, 0), 12);
        return payout[finalRoll];
    }
}
