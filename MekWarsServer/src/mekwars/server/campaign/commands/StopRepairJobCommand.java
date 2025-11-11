/*
 * MekWars - Copyright (C) 2005 
 * 
 * Original author - Torren (torren@users.sourceforge.net)
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

/*
 * Created on 10.05.2005
 *  
 */
package mekwars.server.campaign.commands;

import java.util.StringTokenizer;
import mekwars.server.MWServ;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SPlayer;
import mekwars.server.campaign.SUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Torren (Jason Tighe)
 * this parses out what the User wants reparied on thier unit and sends that data
 * to the repair thread
 */
public class StopRepairJobCommand implements Command {
    private static final Logger LOGGER = LogManager.getLogger(StopRepairJobCommand.class);

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
		
        try{
        
            int unitID = Integer.parseInt(command.nextToken());
            int location = Integer.parseInt(command.nextToken());
            int slot = Integer.parseInt(command.nextToken());
            boolean armor = Boolean.parseBoolean(command.nextToken()); 

            SPlayer player = CampaignMain.cm.getPlayer(Username);
            SUnit unit = player.getUnit(unitID);
            
            if ( !MWServ.getInstance().getRTT().isBeingRepaired(unitID,location,slot,armor) ){
                CampaignMain.cm.toUser("FSM|There is no repair order for this section at the present.",Username,false);
                return;
            }
            
            if ( MWServ.getInstance().getRTT().getState() == Thread.State.TERMINATED ){
                CampaignMain.cm.toUser("FSM|Sorry your repair order could not be processed - the repair thread terminated. Staff was notified.",Username,false);
                LOGGER.error("NOTE: Repair Thread terminated! Use the restartrepairthread command to restart the thread. If all else fails reboot!");
                return;
            }

            MWServ.getInstance().getRTT().stopRepair(unitID,location,slot,armor);
            
            CampaignMain.cm.toUser("PL|UU|"+unitID+"|"+unit.toString(true),Username,false);

        }catch(Exception ex){
            LOGGER.error("AM:Unable to Process Repair Unit Command!", ex);
        }
        
	}//end process()

  

}//end RepairUnitCommand