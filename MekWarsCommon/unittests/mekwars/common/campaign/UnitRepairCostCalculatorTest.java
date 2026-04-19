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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import megamek.common.CriticalSlot;
import megamek.common.Entity;
import megamek.common.Mech;
import megamek.common.Mounted;
import megamek.common.equipment.ArmorType;
import megamek.common.weapons.Weapon;

import mekwars.common.CampaignData;
import mekwars.common.House;
import mekwars.common.util.UnitUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class UnitRepairCostCalculatorTest {
    @Mock private CampaignData campaignData;

    @Mock private CampaignOptions campaignOptions;

    @Mock private Entity entity;

    @Mock private House house;

    private List<Integer> techs = Collections.nCopies(7, UnitUtils.TECH_REG);
    private List<Integer> rolls = Collections.nCopies(7, 8);

    @BeforeEach
    public void setup() {
        CampaignData.cd = campaignData;

        when(campaignOptions.getIntegerConfig("RegTechRepairCost")).thenReturn(2);
        when(campaignOptions.getBooleanConfig("UsePartsRepair")).thenReturn(false);
        when(campaignData.getCampaignOptions()).thenReturn(campaignOptions);
    }

    @Nested
    public class BasicTest {
        // Mock undamaged unit
        @BeforeEach
        public void setup() {
            when(entity.locations()).thenReturn(1);
            when(entity.getArmor(0)).thenReturn(100);
            when(entity.getOArmor(0)).thenReturn(100);
            when(entity.hasRearArmor(0)).thenReturn(false);
            when(entity.getInternal(0)).thenReturn(50);
            when(entity.getOInternal(0)).thenReturn(50);
            when(entity.getNumberOfCriticals(0)).thenReturn(0);
        }

        @Test
        public void testNoDamage_ZeroCost() {
            int repairCost =
                    UnitRepairCostCalculator.getTotalRepairCosts(entity, techs, rolls, 4, house);

            assertEquals(0, repairCost);
        }
    }

    @Nested
    public class FrontArmorCostTest {
        @BeforeEach
        public void setup() {
            when(entity.locations()).thenReturn(1);
            when(entity.getArmor(0)).thenReturn(90);
            when(entity.getOArmor(0)).thenReturn(100);
            when(entity.hasRearArmor(0)).thenReturn(false);
            when(entity.getInternal(0)).thenReturn(50);
            when(entity.getOInternal(0)).thenReturn(50);
            when(entity.getNumberOfCriticals(0)).thenReturn(0);
            when(campaignOptions.getDoubleConfig("CostPointStandard")).thenReturn(1.0);
        }

        @Test
        public void testArmorRepair_BaseCost() {
            int repairCost =
                    UnitRepairCostCalculator.getTotalRepairCosts(entity, techs, rolls, 4, house);

            /**
             * Armor missing: 10 Armor cost: 1 RegTechRepairCost: 2 payOutIncreaseBasedOnRoll: 2.4
             * (Armor missing + RegTechRepairCost) * payOutIncreaseBasedOnRoll = 28
             */
            assertEquals(28, repairCost);
        }
    }

    @Nested
    public class EngineRepairTest {
        @Mock private CriticalSlot criticalSlot;

        @BeforeEach
        public void setup() {
            when(entity.locations()).thenReturn(1);
            when(entity.getArmor(0)).thenReturn(100);
            when(entity.getOArmor(0)).thenReturn(100);
            when(entity.hasRearArmor(0)).thenReturn(false);
            when(entity.getInternal(0)).thenReturn(50);
            when(entity.getOInternal(0)).thenReturn(50);
            when(entity.getNumberOfCriticals(0)).thenReturn(0);
            when(entity.getNumberOfCriticals(UnitUtils.LOC_CT)).thenReturn(1);
            when(entity.getCritical(UnitUtils.LOC_CT, 0)).thenReturn(criticalSlot);
            when(campaignOptions.getDoubleConfig("EngineCritRepairCost")).thenReturn(150.0);
            when(campaignOptions.getDoubleConfig("CostPointStandardIS")).thenReturn(0.8);
            when(criticalSlot.getType()).thenReturn(CriticalSlot.TYPE_SYSTEM);
            when(criticalSlot.getIndex()).thenReturn(Mech.SYSTEM_ENGINE);
            when(criticalSlot.isBreached()).thenReturn(true);
            when(criticalSlot.isDamaged()).thenReturn(true);
        }

        @Test
        public void testEngineRepair_BaseCost() {
            int repairCost =
                    UnitRepairCostCalculator.getTotalRepairCosts(entity, techs, rolls, 4, house);

            /**
             * engineCost: 150 amount: 6 techCost: 2 payOutIncreaseBasedOnRoll: 2.4 (engineCost *
             * amount) + techCost) * payOutIncreaseBasedOnRoll = 2164
             */
            assertEquals(2164, repairCost);
        }
    }

    @Nested
    public class SystemsRepairTest {
        @Mock private CriticalSlot criticalSlot;

        @BeforeEach
        public void setup() {
            when(entity.locations()).thenReturn(1);
            when(entity.getArmor(0)).thenReturn(100);
            when(entity.getOArmor(0)).thenReturn(100);
            when(entity.hasRearArmor(0)).thenReturn(false);
            when(entity.getInternal(0)).thenReturn(50);
            when(entity.getOInternal(0)).thenReturn(50);
            when(entity.getNumberOfCriticals(0)).thenReturn(1);
            when(entity.getCritical(0, 0)).thenReturn(criticalSlot);
            when(campaignOptions.getDoubleConfig("SystemCritReplaceCost")).thenReturn(24.0);
            when(campaignOptions.getDoubleConfig("CostPointStandardIS")).thenReturn(0.8);
            when(criticalSlot.getType()).thenReturn(CriticalSlot.TYPE_SYSTEM);
            when(criticalSlot.getIndex()).thenReturn(Mech.ACTUATOR_UPPER_ARM);
            when(criticalSlot.isBreached()).thenReturn(false);
            when(criticalSlot.isDamaged()).thenReturn(true);
            when(criticalSlot.isMissing()).thenReturn(true);
        }

        @Test
        public void testSystemsRepair_BaseCost() {
            int repairCost =
                    UnitRepairCostCalculator.getTotalRepairCosts(entity, techs, rolls, 4, house);

            /**
             * CritCost: 1 + techCost = 3 techCost: 2 payOutIncreaseBasedOnRoll: 2.4 (CritCost +
             * techCost) * payOutIncreaseBasedOnRoll = 12
             */
            assertEquals(67, repairCost);
        }
    }

    @Nested
    public class InternalRepairTest {
        @BeforeEach
        public void setup() {
            when(entity.locations()).thenReturn(1);
            when(entity.getArmor(0)).thenReturn(100);
            when(entity.getOArmor(0)).thenReturn(100);
            when(entity.hasRearArmor(0)).thenReturn(false);
            when(entity.getInternal(0)).thenReturn(40);
            when(entity.getOInternal(0)).thenReturn(50);
            when(entity.getNumberOfCriticals(0)).thenReturn(0);
            when(campaignOptions.getDoubleConfig("CostPointStandardIS")).thenReturn(1.0);
        }

        @Test
        public void testInternalArmorRepair_BaseCost() {
            int repairCost =
                    UnitRepairCostCalculator.getTotalRepairCosts(entity, techs, rolls, 4, house);

            /**
             * Armor missing: 10 Armor cost: 1 RegTechRepairCost: 2 payOutIncreaseBasedOnRoll: 2.4
             * (Armor missing + RegTechRepairCost) * payOutIncreaseBasedOnRoll = 28
             */
            assertEquals(28, repairCost);
        }
    }

    @Nested
    public class WeaponsRepairTest {
        @Mock private CriticalSlot criticalSlot;
        @Mock private Mounted mounted;
        @Mock private Weapon weapon;

        @BeforeEach
        public void setup() {
            when(entity.locations()).thenReturn(1);
            when(entity.getArmor(0)).thenReturn(100);
            when(entity.getOArmor(0)).thenReturn(100);
            when(entity.hasRearArmor(0)).thenReturn(false);
            when(entity.getInternal(0)).thenReturn(50);
            when(entity.getOInternal(0)).thenReturn(50);
            when(entity.getNumberOfCriticals(0)).thenReturn(1);
            when(entity.getCritical(0, 0)).thenReturn(criticalSlot);
            when(campaignOptions.getDoubleConfig("EquipmentCritRepairCost")).thenReturn(12.0);
            when(campaignOptions.getDoubleConfig("CostPointStandardIS")).thenReturn(0.8);
            when(criticalSlot.getMount()).thenReturn(mounted);
            when(criticalSlot.getType()).thenReturn(CriticalSlot.TYPE_EQUIPMENT);
            when(criticalSlot.isBreached()).thenReturn(true);
            when(criticalSlot.isDamaged()).thenReturn(true);
            when(mounted.getType()).thenReturn(weapon);
        }

        @Test
        public void testWeaponRepair_BaseCost() {
            int repairCost =
                    UnitRepairCostCalculator.getTotalRepairCosts(entity, techs, rolls, 4, house);

            /**
             * engineCost: 150 amount: 6 techCost: 2 payOutIncreaseBasedOnRoll: 2.4 (engineCost *
             * amount) + techCost) * payOutIncreaseBasedOnRoll = 2164
             */
            assertEquals(38, repairCost);
        }
    }

    @Nested
    public class EquipmentRepairTest {
        @Mock private CriticalSlot criticalSlot;
        @Mock private Mounted mounted;
        @Mock private ArmorType armor;

        @BeforeEach
        public void setup() {
            when(entity.locations()).thenReturn(1);
            when(entity.getArmor(0)).thenReturn(100);
            when(entity.getOArmor(0)).thenReturn(100);
            when(entity.hasRearArmor(0)).thenReturn(false);
            when(entity.getInternal(0)).thenReturn(50);
            when(entity.getOInternal(0)).thenReturn(50);
            when(entity.getNumberOfCriticals(0)).thenReturn(1);
            when(entity.getCritical(0, 0)).thenReturn(criticalSlot);
            when(campaignOptions.getDoubleConfig("EquipmentCritReplaceCost")).thenReturn(12.0);
            when(campaignOptions.getDoubleConfig("CostPointStandardIS")).thenReturn(0.8);
            when(criticalSlot.getMount()).thenReturn(mounted);
            when(criticalSlot.getType()).thenReturn(CriticalSlot.TYPE_EQUIPMENT);
            when(criticalSlot.isBreached()).thenReturn(true);
            when(criticalSlot.isDamaged()).thenReturn(true);
            when(criticalSlot.isMissing()).thenReturn(true);
            when(mounted.getType()).thenReturn(armor);
        }

        @Test
        public void testEquipmentRepair_BaseCost() {
            int repairCost =
                    UnitRepairCostCalculator.getTotalRepairCosts(entity, techs, rolls, 4, house);

            /**
             * engineCost: 150 amount: 6 techCost: 2 payOutIncreaseBasedOnRoll: 2.4 (engineCost *
             * amount) + techCost) * payOutIncreaseBasedOnRoll = 2164
             */
            assertEquals(38, repairCost);
        }
    }

    @Nested
    public class GetRepairCostTest {
        @BeforeEach
        public void setup() {
            when(campaignOptions.getIntegerConfig("CampaignYear")).thenReturn(3025);
            when(campaignOptions.getBooleanConfig("UseRealRepairCosts")).thenReturn(false);
            when(campaignOptions.getBooleanConfig("AllowCritRepairsForRewards")).thenReturn(false);
        }

        @Nested
        public class FrontArmorRepair {
            @BeforeEach
            public void setup() {
                when(entity.getArmor(0)).thenReturn(90);
                when(entity.getOArmor(0)).thenReturn(100);
                when(campaignOptions.getDoubleConfig("CostPointStandard")).thenReturn(1.0);
            }

            @Test
            public void testFrontArmorRepair_BaseCost() {
                int repairCost =
                        UnitRepairCostCalculator.getRepairCost(
                                entity,
                                0,
                                UnitUtils.LOC_FRONT_ARMOR,
                                UnitUtils.TECH_REG,
                                true,
                                0,
                                false);
                assertEquals(12, repairCost);
            }
        }

        @Nested
        public class RearArmorRepair {
            @BeforeEach
            public void setup() {
                when(entity.getArmor(0, true)).thenReturn(90);
                when(entity.getOArmor(0, true)).thenReturn(100);
                when(campaignOptions.getDoubleConfig("CostPointStandard")).thenReturn(1.0);
            }

            @Test
            public void testRearArmorRepair_BaseCost() {
                int repairCost =
                        UnitRepairCostCalculator.getRepairCost(
                                entity,
                                0,
                                UnitUtils.LOC_REAR_ARMOR,
                                UnitUtils.TECH_REG,
                                true,
                                0,
                                false);

                /**
                 * Armor missing: 10 Armor cost: 1 RegTechRepairCost: 2 payOutIncreaseBasedOnRoll:
                 * 2.4 (Armor missing + RegTechRepairCost) * payOutIncreaseBasedOnRoll = 28
                 */
                assertEquals(12, repairCost);
            }
        }

        @Nested
        public class CriticalSlotRepairTest {
            @Mock private CriticalSlot criticalSlot;

            @BeforeEach
            public void setup() {
                when(entity.getCritical(0, 0)).thenReturn(criticalSlot);
                when(campaignOptions.getDoubleConfig("SystemCritReplaceCost")).thenReturn(1.0);
                when(criticalSlot.getType()).thenReturn(CriticalSlot.TYPE_SYSTEM);
                when(criticalSlot.getIndex()).thenReturn(Mech.ACTUATOR_UPPER_ARM);
                when(criticalSlot.isBreached()).thenReturn(false);
                when(criticalSlot.isDamaged()).thenReturn(true);
                when(criticalSlot.isMissing()).thenReturn(true);
            }

            @Test
            public void testCriticalSlotRepair_BaseCost() {
                int repairCost =
                        UnitRepairCostCalculator.getRepairCost(
                                entity, 0, 0, UnitUtils.TECH_REG, false, 0, false);

                /**
                 * CritCost: 1 + techCost = 3 techCost: 2 payOutIncreaseBasedOnRoll: 2.4 (CritCost +
                 * techCost) * payOutIncreaseBasedOnRoll = 12
                 */
                assertEquals(5, repairCost);
            }
        }
    }
}
