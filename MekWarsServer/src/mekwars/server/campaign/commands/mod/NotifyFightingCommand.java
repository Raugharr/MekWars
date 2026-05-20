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

import mekwars.server.MWServ;
import mekwars.common.House;
import mekwars.common.util.HibernateUtil;
import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SHouse;
import mekwars.server.campaign.SPlayer;
import mekwars.server.campaign.commands.Command;

import java.util.List;
import java.util.StringTokenizer;

public class NotifyFightingCommand implements Command {
	int accessLevel = IAuthenticator.MODERATOR;
	String syntax = "Message";
	public int getExecutionLevel(){return accessLevel;}
	public void setExecutionLevel(int i) {accessLevel = i;}
	public String getSyntax() { return syntax;}
	
	public void process(StringTokenizer command,String username) {
		
		//access level check
		int userLevel = MWServ.getInstance().getUserLevel(username);
		if(userLevel < getExecutionLevel()) {
			CampaignMain.cm.toUser("AM:Insufficient access level for command. Level: " + userLevel + ". Required: " + accessLevel + ".",username,true);
			return;
		}
		
		//load the message
		String Message = (String)command.nextElement();

		//send to all fighters from all houses
		for (House h : CampaignMain.cm.getData().getAllHouses()) {
			SHouse currH = (SHouse)h;
			List<SPlayer> fightingPlayers = HibernateUtil.fromTransaction(session ->
				session.createQuery("FROM SPlayer WHERE myHouse.id = :houseId AND status = :status", SPlayer.class)
					.setParameter("houseId", currH.getId())
					.setParameter("status", SPlayer.STATUS_FIGHTING)
					.getResultList()
			);
			for (SPlayer p : fightingPlayers) {
				CampaignMain.cm.toUser("PM|SERVER|" + Message, p.getName(), false);
			}
		}

		CampaignMain.cm.doSendModMail("NOTE",username + " sent a message to all fighting players: " + Message);
		CampaignMain.cm.toUser("Message sent to all fighting players: " + Message,username,true);
			
	}//end process()
}//end notifyfightingcommand.java
