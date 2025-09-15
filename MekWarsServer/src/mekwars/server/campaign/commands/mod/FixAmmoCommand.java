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

package mekwars.server.campaign.commands.mod;

import java.util.List;
import java.util.StringTokenizer;

import mekwars.common.util.StringUtils;
import megamek.common.AmmoType;
import megamek.common.Entity;
import megamek.common.equipment.AmmoMounted;
import mekwars.server.MWServ;
import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SPlayer;
import mekwars.server.campaign.SUnit;
import mekwars.server.campaign.commands.Command;

/**
 * @author Helge Richter
 */
public class FixAmmoCommand implements Command {
	
	int accessLevel = IAuthenticator.MODERATOR;
	String syntax = "Player Name#Unit ID";
	public int getExecutionLevel(){return accessLevel;}
	public void setExecutionLevel(int i) {accessLevel = i;}
	public String getSyntax() { return syntax;}
	
	public void process(StringTokenizer command,String Username) {
		
		//access level check
		int userLevel = MWServ.getInstance().getUserLevel(Username);
		if(userLevel < getExecutionLevel()) {
			CampaignMain.cm.toUser("AM:Insufficient access level for command. Level: " + userLevel + ". Required: " + accessLevel + ".",Username,true);
			return;
		}
		
		String targetName;
		SPlayer target = null;
		int unitID;
		
		try {
			targetName = command.nextToken();
			unitID = Integer.parseInt(command.nextToken());
		}catch (Exception ex) {
			CampaignMain.cm.toUser("AM:Invalid Syntax: /FixAmmo Player#UnitID", Username);
			return;
		}
		
		target = CampaignMain.cm.getPlayer(targetName);
		
		if (target == null) {
			CampaignMain.cm.toUser("AM:Target player could not be found. Try again.", Username, true);
			return;
		}
		
		//non null target, so attempt the scrap
		SUnit m = target.getUnit(unitID);
		
		//break out if the player doesn't have a unit with that id
		if (m == null) {
			CampaignMain.cm.toUser("AM:Target player doesn't have a unit with ID# " + unitID + ".", Username, true);
			return;
		}
		
		fixAmmo(m);
		//tell the player the unit has been fixed
		CampaignMain.cm.toUser(targetName + "'s " + m.getModelName() + "'s ammo has been fixed.", Username, true);
		CampaignMain.cm.toUser(Username + " has fixed the ammo on your " + m.getModelName() + " (ID#" + m.getId() + ")", targetName, true);
		CampaignMain.cm.doSendModMail("NOTE",Username + " fixed the ammo on "+StringUtils.aOrAn(m.getModelName(),true) + " belonging to " + targetName);
        target.setSave();
        target.checkAndUpdateArmies(m);
        CampaignMain.cm.toUser("PL|UU|"+m.getId()+"|"+m.toString(true),targetName,false);
	}
	/**
	 * If the units ammo is different from the baseline entity the ammo is fixed.
	 * @param unit
	 */
	private void fixAmmo(SUnit unit) {
		Entity baseLine = null;
		Entity en = unit.getEntity();
		
		baseLine = SUnit.loadMech(unit.getUnitFilename());
		
		List<AmmoMounted> baseLineAmmo = baseLine.getAmmo();
		List<AmmoMounted> currentAmmo = en.getAmmo();
		
		if ( baseLineAmmo.size() < 1)
			unit.getEntity().getAmmo().clear();
		
		for ( int pos = 0; pos < baseLineAmmo.size(); pos++) {
			AmmoType baseAmmo = (AmmoType)baseLineAmmo.get(pos).getType();
			AmmoType ammo = (AmmoType)currentAmmo.get(pos).getType();
			
			if ( !ammo.getInternalName().startsWith(baseAmmo.getInternalName()) ) {
				unit.getEntity().getAmmo().set(pos, baseLineAmmo.get(pos));
			}
		}
	}

}//end ModFullRepairCommand
