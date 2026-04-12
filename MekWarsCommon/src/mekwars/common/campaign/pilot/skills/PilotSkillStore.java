/*
 * MekWars - Copyright (C) 2026
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
package mekwars.common.campaign.pilot.skills;

import mekwars.common.CampaignData;
import mekwars.common.campaign.pilot.Pilot;
import mekwars.common.entities.FactionTrait;
import mekwars.common.entities.factiontrait.Trait;
import mekwars.common.io.file.FactionTraitFile;
import mekwars.common.util.RandomUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PilotSkillStore {
    private static final Logger LOGGER = LogManager.getLogger(PilotSkills.class);

    private static Map<Integer, PilotSkill> pilotSkills = new HashMap<>();

    public static void initializePilotSkills() {
        // PilotSkills
        pilotSkills.put(
                PilotSkill.DodgeManeuverSkillID,
                (new DodgeManeuverSkill(PilotSkill.DodgeManeuverSkillID)));
        pilotSkills.put(
                PilotSkill.ManeuveringAceSkillID,
                (new ManeuveringAceSkill(PilotSkill.ManeuveringAceSkillID)));
        pilotSkills.put(
                PilotSkill.MeleeSpecialistSkillID,
                (new MeleeSpecialistSkill(PilotSkill.MeleeSpecialistSkillID)));
        pilotSkills.put(
                PilotSkill.PainResistanceSkillID,
                (new PainResistanceSkill(PilotSkill.PainResistanceSkillID)));
        pilotSkills.put(PilotSkill.AstechSkillID, (new AstechSkill(PilotSkill.AstechSkillID)));
        pilotSkills.put(
                PilotSkill.NaturalAptitudeGunnerySkillID,
                (new NaturalAptitudeGunnerySkill(PilotSkill.NaturalAptitudeGunnerySkillID)));
        pilotSkills.put(
                PilotSkill.NaturalAptitudePilotingSkillID,
                (new NaturalAptitudePilotingSkill(PilotSkill.NaturalAptitudePilotingSkillID)));
        pilotSkills.put(PilotSkill.IronManSkillID, (new IronManSkill(PilotSkill.IronManSkillID)));
        pilotSkills.put(
                PilotSkill.GunneryBallisticSkillID,
                (new GunneryBallisticSkill(PilotSkill.GunneryBallisticSkillID)));
        pilotSkills.put(
                PilotSkill.GunneryLaserSkillID,
                (new GunneryLaserSkill(PilotSkill.GunneryLaserSkillID)));
        pilotSkills.put(
                PilotSkill.GunneryMissileSkillID,
                (new GunneryMissileSkill(PilotSkill.GunneryMissileSkillID)));
        pilotSkills.put(
                PilotSkill.TacticalGeniusSkillID,
                (new TacticalGeniusSkill(PilotSkill.TacticalGeniusSkillID)));
        pilotSkills.put(
                PilotSkill.WeaponSpecialistSkillID,
                (new WeaponSpecialistSkill(PilotSkill.WeaponSpecialistSkillID)));
        pilotSkills.put(
                PilotSkill.SurvivalistSkillID,
                (new SurvivalistSkill(PilotSkill.SurvivalistSkillID)));
        pilotSkills.put(PilotSkill.TraitID, (new TraitSkill(PilotSkill.TraitID)));
        pilotSkills.put(
                PilotSkill.EnhancedInterfaceID,
                (new EnhancedInterfaceSkill(PilotSkill.EnhancedInterfaceID)));
        pilotSkills.put(PilotSkill.QuickStudyID, (new QuickStudySkill(PilotSkill.QuickStudyID)));
        pilotSkills.put(PilotSkill.GiftedID, (new GiftedSkill(PilotSkill.GiftedID)));
        pilotSkills.put(PilotSkill.MedTechID, (new MedTechSkill(PilotSkill.MedTechID)));
        pilotSkills.put(PilotSkill.EdgeSkillID, (new EdgeSkill(PilotSkill.EdgeSkillID)));
        pilotSkills.put(
                PilotSkill.ClanPilotTraingID,
                (new ClanPilotTrainingSkill(PilotSkill.ClanPilotTraingID)));
        pilotSkills.put(PilotSkill.VDNIID, (new VDNI(PilotSkill.VDNIID)));
        pilotSkills.put(PilotSkill.BufferedVDNIID, (new BufferedVDNI(PilotSkill.BufferedVDNIID)));
        pilotSkills.put(PilotSkill.PainShuntID, (new PainShunt(PilotSkill.PainShuntID)));
    }

    /**
     * Randomly selects a PilotSkill that is valid for the provided unitType. If the pilot has a
     * traitName its modifiers are included in the PilotSkill weights.
     *
     * @param pilot The pilot to generate the {@link PilotSkill} for.
     * @param unitType The unit type to get a random skill for.
     */
    public static PilotSkill getRandomSkill(Pilot pilot, int unitType) {
        if (pilot == null) {
            return null;
        }

        // PilotSkill's id, weight
        Map<Integer, Integer> skillTable = new HashMap<>();
        // Add the weight of all traits in the pilots getTraitName(), if any.
        if (pilot.getSkills().has(PilotSkill.TraitID)) {
            FactionTraitFile factionTraitFile =
                    CampaignData.cd.getFactionTraitFileByHouse(pilot.getHouse().getName());

            if (factionTraitFile != null) {
                FactionTrait traits = factionTraitFile.getFactionTraitByName(pilot.getTraitName());

                if (traits != null) {
                    for (Trait trait : traits.getTraits()) {
                        skillTable.put(trait.getPilotSkill().getId(), trait.getModifier());
                    }
                }
            }
        }

        // Add the weight of all skills.
        for (PilotSkill skill : pilotSkills.values()) {
            int chance = skill.getChance(unitType, pilot);
            Integer weight = skillTable.get(skill.getId());

            if (weight == null) {
                skillTable.put(skill.getId(), chance);
            } else {
                skillTable.put(skill.getId(), chance + weight);
            }
        }

        List<Map.Entry<Integer, Integer>> skillList =
                skillTable.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue())
                        .collect(Collectors.toList());
        Integer pilotSkillId = null;

        if (!skillList.isEmpty()) {
            pilotSkillId = RandomUtils.getRandomItem(skillList);
        }

        return (pilotSkillId != null) ? pilotSkills.get(pilotSkillId) : null;
    }

    /** Create a skill from a string, ignoring case. Used by CreateUnitCommand. */
    public static PilotSkill getPilotSkill(String skill) {
        for (PilotSkill pSkill : pilotSkills.values()) {
            if (pSkill.getName().equalsIgnoreCase(skill)
                    || pSkill.getAbbreviation().equalsIgnoreCase(skill)) {
                return pSkill;
            }
        }
        return null;
    }

    /** Get a pilot skill by ID number. Used to unstring Pilots in pfiles. */
    public static PilotSkill getPilotSkill(int id) {
        return pilotSkills.get(id);
    }
}
