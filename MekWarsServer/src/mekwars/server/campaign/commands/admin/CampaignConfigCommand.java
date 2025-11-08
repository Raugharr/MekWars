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

package mekwars.server.campaign.commands.admin;

import java.io.FileInputStream;
import java.util.StringTokenizer;
import mekwars.server.MWServ;
import mekwars.common.util.MWLogger;
import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.commands.Command;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CampaignConfigCommand implements Command {
    private static final Logger LOGGER = LogManager.getLogger(CampaignConfigCommand.class);

	int accessLevel = IAuthenticator.ADMIN;
	String syntax = "";
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
		
		try {//Try to read the config file
			CampaignMain.cm.getCampaignOptions().getConfig().load(new FileInputStream(MWServ.getInstance().getConfigParam("CAMPAIGNCONFIG")));
		} catch (Exception ex) {
			LOGGER.error("Exception: ", ex);
			CampaignMain.cm.toUser("Failed to read campaign config.",Username,true);
		}	
		CampaignMain.cm.toUser("Campaign config reread!",Username,true);
		
	}
}
