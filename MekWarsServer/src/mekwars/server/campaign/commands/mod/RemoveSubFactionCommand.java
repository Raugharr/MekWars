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

package mekwars.server.campaign.commands.mod;

import java.util.StringTokenizer;
import mekwars.server.MWServ;
import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SHouse;
import mekwars.server.campaign.SPlayer;
import mekwars.server.campaign.commands.Command;

/**
 * Syntax  /RemoveSubFaction SubFactionName#FactionName 
 */

public class RemoveSubFactionCommand implements Command {
	
	int accessLevel = IAuthenticator.MODERATOR;
	String syntax = "Faction name#Sub Faction Name";
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
        
		String factionName = "";
		String subFactionName = "";
		SPlayer player = CampaignMain.cm.getPlayer(Username);
		
		try{
			subFactionName = command.nextToken();
			if ( command.hasMoreTokens() && MWServ.getInstance().isModerator(Username) )
				factionName = command.nextToken();
			else
				factionName = player.getMyHouse().getName();
		}catch(Exception ex){
			CampaignMain.cm.toUser("AM:Invalid syntax: /RemoveSubFaction SubFactionName#[FactionName]", Username);
			return;
		}
		
		SHouse faction = CampaignMain.cm.getHouseFromPartialString(factionName,Username);
		
		if ( faction == null )
			return;
		

		faction.getSubFactionList().remove(subFactionName);
		
		faction.updated();
		
		CampaignMain.cm.doSendModMail("NOTE", Username +" has removed subfaction "+subFactionName+" for faction "+faction.getName());
		CampaignMain.cm.toUser("AM:You have removed subfaction "+subFactionName+" for faction "+faction.getName(), Username);
	}
}