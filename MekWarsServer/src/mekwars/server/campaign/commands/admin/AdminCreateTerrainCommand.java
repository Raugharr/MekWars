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

import java.util.StringTokenizer;
import mekwars.server.MWServ;
import mekwars.common.Continent;
import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SPlanet;
import mekwars.server.campaign.commands.Command;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class AdminCreateTerrainCommand implements Command {
    private static final Logger LOGGER = LogManager.getLogger(AdminCreateTerrainCommand.class);

	int accessLevel = IAuthenticator.ADMIN;
	String syntax = "Planet Name#TerrainType#AdvancedTerrain#Chance";
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
		
		try{
			SPlanet p = CampaignMain.cm.getPlanetFromPartialString(command.nextToken(),Username);
			String terraintype = command.nextToken();
			String advTerrainType = command.nextToken();
			int chance = Integer.parseInt(command.nextToken());
			
			if (p == null) {
				CampaignMain.cm.toUser("Planet not found:",Username,true);
				return;
			}
			
			Continent cont = new Continent(chance, CampaignMain.cm.getData().getTerrainByName(terraintype), CampaignMain.cm.getData().getAdvancedTerrainByName(advTerrainType));
			p.getEnvironments().add(cont);
			p.updated();
			
			//server.MWLogger.modLog(Username + " added terrain to " + p.getName() + " (" + terraintype + ").");
			CampaignMain.cm.toUser("Terrain added to " + p.getName() + "(" + terraintype + "-" + advTerrainType + ").",Username,true);
			CampaignMain.cm.doSendModMail("NOTE",Username + " added terrain to planet " + p.getName() + "(" + terraintype + "-" + advTerrainType + ").");
		}
		
		catch (Exception ex){
			LOGGER.error("Exception: ", ex);
		}
	}
}