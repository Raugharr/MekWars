/*
 * MekWars - Copyright (C) 2006
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

package mekwars.server.campaign.commands.admin;

import java.util.StringTokenizer;
import mekwars.common.util.MWLogger;
import mekwars.server.MWServ;
import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.commands.Command;
import mekwars.server.campaign.util.scheduler.MWScheduler;
import mekwars.server.util.MWPasswd;


/**
 * Moving the Shutdown command from MWServ into the normal command structure.
 *
 * Syntax  /c Shutdown
 */
public class ShutdownCommand implements Command {
	
	int accessLevel = IAuthenticator.ADMIN;
	String syntax = "";
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
		MWScheduler.getInstance().shutdown();
		
        CampaignMain.cm.getMarket().removeAllListings();
		CampaignMain.cm.toFile();
		CampaignMain.cm.forceSavePlayers(Username);
        CampaignMain.cm.saveBannedAmmo();
        CampaignMain.cm.getDefaultPlayerFlags().save();
        CampaignMain.cm.toUser("AM:You halted the server. Have a nice day.", Username,true);
        MWLogger.infoLog(Username + " halted the server. Have a nice day!");
        MWServ.getInstance().addToNewsFeed("Server halted!", "Server News", "");
        MWServ.getInstance().postToDiscord("Server halted!");
        try {
            MWPasswd.save();
        } catch(Exception ex) {
            MWLogger.errLog("Unable to save passwords before shutdown!");
            MWLogger.errLog(ex);
        }
        
        System.exit(0);
	}
}
