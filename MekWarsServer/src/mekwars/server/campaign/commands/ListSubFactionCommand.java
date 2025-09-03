/*
 * MekWars - Copyright (C) 2007
 * 
 * Original author - Jason Tighe (torren@users.sourceforge.net)
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

package mekwars.server.campaign.commands;

import java.util.StringTokenizer;

import mekwars.common.House;
import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SHouse;
import mekwars.server.campaign.SPlayer;


/**
 * Syntax  /ListSubFaction FactionName 
 */

public class ListSubFactionCommand implements Command {
	
	int accessLevel = IAuthenticator.MODERATOR;
	public int getExecutionLevel(){return accessLevel;}
	public void setExecutionLevel(int i) {accessLevel = i;}
	String syntax = "";
	public String getSyntax() { return syntax;}

	
	public void process(StringTokenizer command,String Username) {
		
		if (accessLevel != 0) {
			int userLevel = CampaignMain.cm.getServer().getUserLevel(Username);
			if(userLevel < getExecutionLevel()) {
				CampaignMain.cm.toUser("AM:Insufficient access level for command. Level: " + userLevel + ". Required: " + accessLevel + ".",Username,true);
				return;
			}
		}
        
		String factionName = "";
		SPlayer player = CampaignMain.cm.getPlayer(Username);
		
		try{
			if ( command.hasMoreTokens() && CampaignMain.cm.getServer().isModerator(Username) )
				factionName = command.nextToken();
			else
				factionName = player.getMyHouse().getName();
		}catch(Exception ex){
			factionName = player.getMyHouse().getName();
		}
		
		if ( factionName.equalsIgnoreCase("all") ){

			for ( House faction : CampaignMain.cm.getData().getAllHouses() ){
				StringBuffer result = new StringBuffer("SM|Subfaction list for faction ");
				result.append(faction.getName());
				for (String subFactionName : faction.getSubFactionList().keySet() ){
					result.append("<BR>");
					result.append(subFactionName);
				}
				
				CampaignMain.cm.toUser(result.toString(), Username,false);
				result.setLength(0);
			}
			return;
		}
		
		SHouse faction = CampaignMain.cm.getHouseFromPartialString(factionName,Username);
		
		if ( faction == null )
			return;

		StringBuffer result = new StringBuffer("SM|Subfaction list for faction ");
		result.append(faction.getName());
		for (String subFactionName : faction.getSubFactionList().keySet() ){
			result.append("<BR>");
			result.append(subFactionName);
		}
		
		CampaignMain.cm.toUser(result.toString(), Username,false);
	}
}