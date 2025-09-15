/*
 * MekWars - Copyright (C) 2005
 * 
 * Original author - nmorris (urgru@users.sourceforge.net)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 */

package mekwars.server.campaign.commands.admin;

import java.util.StringTokenizer;
import java.util.Vector;
import mekwars.server.MWServ;
import mekwars.common.util.MWLogger;
import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.commands.Command;


public class AddTraitCommand implements Command {
	
	int accessLevel = IAuthenticator.ADMIN;
	String syntax = "Faction#TraitName#Skill$Skill$Skill$Skill";
	public int getExecutionLevel(){return accessLevel;}
	public void setExecutionLevel(int i) {accessLevel = i;}
	public String getSyntax() { return syntax;}
	
	public void process(StringTokenizer command,String Username) {
		
		if (accessLevel != 0) {
			int userLevel = MWServ.getInstance().getUserLevel(Username);
			if(userLevel < getExecutionLevel()) {
				CampaignMain.cm.toUser("AM:Insufficient access level for command. Level: " + userLevel + ". Required: " + accessLevel + ".",Username,true);
				return;
			}
		}

		//Syntax AddTrait Faction#TraitName#SkillList($)
		
		
	    String faction="common";
		String traitName="none";
		String skillList="";
		String confirmString ="";
		
		try{
		    faction = command.nextToken();
			traitName = command.nextToken();
			skillList = command.nextToken();
			confirmString = command.nextToken();
		}catch (Exception ex){
		    MWLogger.errLog(ex);
		}		

		//MWLogger.errLog("faction: "+faction+" Trait: "+traitName+" skills: "+skillList+" Confirm: "+confirmString);
		
		if ( !confirmString.equals("CONFIRM") )
		    return;
		
		Vector<String> traits = CampaignMain.cm.getFactionTraits(faction.toLowerCase());
		
		for ( int pos = 0; pos < traits.size(); pos++ ){
		    StringTokenizer traitToken = new StringTokenizer(traits.elementAt(pos),"*");
		    if ( traitName.equalsIgnoreCase(traitToken.nextToken())){
		        traits.removeElementAt(pos);
		        traits.add(traitName+"*"+skillList);
		 		CampaignMain.cm.toUser("Trait "+ traitName+" has been added with skills "+skillList+".",Username,true);
				CampaignMain.cm.doSendModMail("NOTE",Username + " has added trait "+ traitName+" with skills "+skillList+".");
				CampaignMain.cm.saveFactionTraits(faction,traits);
				return;
		    }
		}
		traits.add(traitName+"*"+skillList);
		CampaignMain.cm.saveFactionTraits(faction,traits);
		CampaignMain.cm.toUser("Trait "+ traitName+" has been added with skills "+skillList+".",Username,true);
		CampaignMain.cm.doSendModMail("NOTE",Username + " has added trait "+ traitName+" with skills "+skillList+".");
	}
	
}