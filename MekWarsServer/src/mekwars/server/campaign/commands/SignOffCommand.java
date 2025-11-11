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

package mekwars.server.campaign.commands;

import java.util.StringTokenizer;
import mekwars.server.MWServ;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Moving the SignOff command from MWServ into the normal command structure.
 *
 * Syntax  /c SignOff
 */
public class SignOffCommand implements Command {
    private static final Logger LOGGER = LogManager.getLogger(SignOffCommand.class);

	public int getExecutionLevel(){return 0;}
	public void setExecutionLevel(int i) {}
	String syntax = "";
	public String getSyntax() { return syntax;}

	public void process(StringTokenizer command, String userName) {

        LOGGER.error("{} has sent signoff command", userName);
        MWServ.getInstance().clientLogout(userName);
	}
}