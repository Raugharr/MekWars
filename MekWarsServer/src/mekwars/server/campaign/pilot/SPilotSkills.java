package mekwars.server.campaign.pilot;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import mekwars.common.campaign.pilot.skills.PilotSkill;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.pilot.skills.AstechSkill;
import mekwars.server.campaign.pilot.skills.BufferedVDNI;
import mekwars.server.campaign.pilot.skills.ClanPilotTrainingSkill;
import mekwars.server.campaign.pilot.skills.DodgeManeuverSkill;
import mekwars.server.campaign.pilot.skills.EdgeSkill;
import mekwars.server.campaign.pilot.skills.EnhancedInterfaceSkill;
import mekwars.server.campaign.pilot.skills.GiftedSkill;
import mekwars.server.campaign.pilot.skills.GunneryBallisticSkill;
import mekwars.server.campaign.pilot.skills.GunneryLaserSkill;
import mekwars.server.campaign.pilot.skills.GunneryMissileSkill;
import mekwars.server.campaign.pilot.skills.IronManSkill;
import mekwars.server.campaign.pilot.skills.ManeuveringAceSkill;
import mekwars.server.campaign.pilot.skills.MedTechSkill;
import mekwars.server.campaign.pilot.skills.MeleeSpecialistSkill;
import mekwars.server.campaign.pilot.skills.NaturalAptitudeGunnerySkill;
import mekwars.server.campaign.pilot.skills.NaturalAptitudePilotingSkill;
import mekwars.server.campaign.pilot.skills.PainResistanceSkill;
import mekwars.server.campaign.pilot.skills.PainShunt;
import mekwars.server.campaign.pilot.skills.QuickStudySkill;
import mekwars.server.campaign.pilot.skills.SPilotSkill;
import mekwars.server.campaign.pilot.skills.SurvivalistSkill;
import mekwars.server.campaign.pilot.skills.TacticalGeniusSkill;
import mekwars.server.campaign.pilot.skills.TraitSkill;
import mekwars.server.campaign.pilot.skills.VDNI;
import mekwars.server.campaign.pilot.skills.WeaponSpecialistSkill;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SPilotSkills {
    private static final Logger LOGGER = LogManager.getLogger(SPilotSkills.class);

    private static Hashtable<Integer, SPilotSkill> pilotSkills = new Hashtable<>();

	 public static void initializePilotSkills() {
	        // PilotSkills
	        pilotSkills.put(PilotSkill.DodgeManeuverSkillID, (new DodgeManeuverSkill(PilotSkill.DodgeManeuverSkillID)));
	        pilotSkills.put(PilotSkill.ManeuveringAceSkillID, (new ManeuveringAceSkill(PilotSkill.ManeuveringAceSkillID)));
	        pilotSkills.put(PilotSkill.MeleeSpecialistSkillID, (new MeleeSpecialistSkill(PilotSkill.MeleeSpecialistSkillID)));
	        pilotSkills.put(PilotSkill.PainResistanceSkillID, (new PainResistanceSkill(PilotSkill.PainResistanceSkillID)));
	        pilotSkills.put(PilotSkill.AstechSkillID, (new AstechSkill(PilotSkill.AstechSkillID)));
	        pilotSkills.put(PilotSkill.NaturalAptitudeGunnerySkillID, (new NaturalAptitudeGunnerySkill(PilotSkill.NaturalAptitudeGunnerySkillID)));
	        pilotSkills.put(PilotSkill.NaturalAptitudePilotingSkillID, (new NaturalAptitudePilotingSkill(PilotSkill.NaturalAptitudePilotingSkillID)));
	        pilotSkills.put(PilotSkill.IronManSkillID, (new IronManSkill(PilotSkill.IronManSkillID)));
	        pilotSkills.put(PilotSkill.GunneryBallisticSkillID, (new GunneryBallisticSkill(PilotSkill.GunneryBallisticSkillID)));
	        pilotSkills.put(PilotSkill.GunneryLaserSkillID, (new GunneryLaserSkill(PilotSkill.GunneryLaserSkillID)));
	        pilotSkills.put(PilotSkill.GunneryMissileSkillID, (new GunneryMissileSkill(PilotSkill.GunneryMissileSkillID)));
	        pilotSkills.put(PilotSkill.TacticalGeniusSkillID, (new TacticalGeniusSkill(PilotSkill.TacticalGeniusSkillID)));
	        pilotSkills.put(PilotSkill.WeaponSpecialistSkillID, (new WeaponSpecialistSkill(PilotSkill.WeaponSpecialistSkillID)));
	        pilotSkills.put(PilotSkill.SurvivalistSkillID, (new SurvivalistSkill(PilotSkill.SurvivalistSkillID)));
	        pilotSkills.put(PilotSkill.TraitID, (new TraitSkill(PilotSkill.TraitID)));
	        pilotSkills.put(PilotSkill.EnhancedInterfaceID, (new EnhancedInterfaceSkill(PilotSkill.EnhancedInterfaceID)));
	        pilotSkills.put(PilotSkill.QuickStudyID, (new QuickStudySkill(PilotSkill.QuickStudyID)));
	        pilotSkills.put(PilotSkill.GiftedID, (new GiftedSkill(PilotSkill.GiftedID)));
	        pilotSkills.put(PilotSkill.MedTechID, (new MedTechSkill(PilotSkill.MedTechID)));
	        pilotSkills.put(PilotSkill.EdgeSkillID, (new EdgeSkill(PilotSkill.EdgeSkillID)));
	        pilotSkills.put(PilotSkill.ClanPilotTraingID, (new ClanPilotTrainingSkill(PilotSkill.ClanPilotTraingID)));
	        pilotSkills.put(PilotSkill.VDNIID, (new VDNI(PilotSkill.VDNIID)));
	        pilotSkills.put(PilotSkill.BufferedVDNIID, (new BufferedVDNI(PilotSkill.BufferedVDNIID)));
	        pilotSkills.put(PilotSkill.PainShuntID, (new PainShunt(PilotSkill.PainShuntID)));
	    }
	 
	    public static SPilotSkill getRandomSkill(SPilot p, int unitType) {
	        int total = 0;

	        Iterator<SPilotSkill> it = pilotSkills.values().iterator();
	        Hashtable<Integer, Integer> skilltable = new Hashtable<Integer, Integer>();
	        if (p.getSkills().has(PilotSkill.TraitID)) {
	            // SPilotSkill skill =
	            // (SPilotSkill)p.getSkills().getPilotSkill(SPilotSkill.TraitID);
	            String trait = p.getTraitName();
	            if (trait.indexOf("*") > -1) {
	                trait = trait.substring(0, trait.indexOf("*"));
	            }
	            Vector<String> traitsList = CampaignMain.cm.getFactionTraits(p.getCurrentFaction());
	            traitsList.trimToSize();
	            for (String traitNames : traitsList) {
	                StringTokenizer traitName = new StringTokenizer(traitNames, "*");
	                String traitString = traitName.nextToken();
	                if (traitString.equalsIgnoreCase(trait)) {
	                    while (traitName.hasMoreElements()) {
	                        int traitid = Integer.parseInt(traitName.nextToken());
	                        int traitMod = Integer.parseInt(traitName.nextToken());
	                        skilltable.put(traitid, traitMod);
	                    }
	                }
	            }
	        }

	        // check for trait mods and add them
	        while (it.hasNext()) {
	            SPilotSkill skill = it.next();
	            total += skill.getChance(unitType, p);
	        }

	        if (total == 0) {
	            return null;
	        }
	        /*
	         * int rnd = 1; if (total > 1) rnd = getR().nextInt(total) + 1;
	         */
	        it = pilotSkills.values().iterator();
	        Vector<SPilotSkill> skillBuilder = new Vector<SPilotSkill>(total, 1);

	        try {
	            while (it.hasNext()) {
	                SPilotSkill skill = it.next();
	                int chance = skill.getChance(unitType, p);
	                if (skilltable.get(skill.getId()) != null) {
	                    chance += skilltable.get(skill.getId());
	                }

	                for (int pos = 0; pos < chance; pos++) {
	                    skillBuilder.add(skill);
	                }
	                skillBuilder.trimToSize();
	                /*
	                 * //LOGGER.error("Pilot: "+p.getName()+" Skill:
	                 * "+skill.getName()+" Rnd "+rnd+ " chance: "+chance); if ( rnd
	                 * <= chance ) return skill; //else rnd -=
	                 * skill.getChance(unitType,p);
	                 */
	            }

	            return skillBuilder.elementAt(CampaignMain.cm.getRandomNumber(skillBuilder.size()));
	        } catch (Exception ex) {
	            LOGGER.error("Problems during skill earning! Skill Table Size = " + skillBuilder.size() + " total = " + total);
	            return null;
	        }
	    }

	    /**
	     * Create a skill from a string. Used by CreateUnitCommand.
	     */
	    public static SPilotSkill getPilotSkill(String skill) {

	        for (SPilotSkill pSkill : pilotSkills.values()) {
	            if (pSkill.getName().equalsIgnoreCase(skill) || pSkill.getAbbreviation().equalsIgnoreCase(skill)) {
	                return pSkill;
	            }
	        }

	        return null;
	    }

	    /**
	     * Get a pilot skill by ID number. Used to unstring SPilots in pfiles.
	     */
	    public static SPilotSkill getPilotSkill(int id) {
	        return pilotSkills.get(id);
	    }
}
