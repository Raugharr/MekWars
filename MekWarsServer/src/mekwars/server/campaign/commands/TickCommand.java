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

package mekwars.server.campaign.commands;

import java.util.StringTokenizer;
import mekwars.server.MWServ;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.util.scheduler.TickJob;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TickCommand implements Command {
	
    private static final Logger logger = LogManager.getLogger(TickCommand.class);
	int accessLevel = 0;
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
		
        try {
            long remaining = TickJob.millisecondsUntilNextFire();
            long remainingMinutes = (remaining / 60);
            long remainingSeconds = (remaining % 60);
            CampaignMain.cm.toUser("AM:The next Tick [" + (TickJob.getTickID() + 1) + "] will occur in " + remainingMinutes + " minutes and " + remainingSeconds + " seconds.", Username, true);
        } catch (Exception exception) {
            logger.catching(exception);
        }
	}
}
