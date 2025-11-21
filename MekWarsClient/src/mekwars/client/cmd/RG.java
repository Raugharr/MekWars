/*
 * MekWars - Copyright (C) 2004 
 * 
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megamek)
 * Original author Helge Richter (McWizard)
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

package mekwars.client.cmd;

import mekwars.client.MWClient;
import mekwars.client.GUIClient;

/**
 * @author Salient 
 * RG = Refresh GUI
 * just a command that makes the mwclient refresh
 * I'm sure there was some preexisting way of doing this
 * but I looked, and got tired of looking
 * So...
 */
public class RG extends Command {

	/**
	 * @param client
	 */
	public RG(MWClient mwclient) {
		super(mwclient);
	}

	/**
	 * @see client.cmd.Command#execute(java.lang.String)
	 */
	@Override
	public void execute(String input) {
		mwclient.getGUIClient().refreshGUI(GUIClient.REFRESH_HQPANEL);
		mwclient.getGUIClient().refreshGUI(GUIClient.REFRESH_PLAYERPANEL);
		mwclient.getGUIClient().refreshGUI(GUIClient.REFRESH_BMPANEL);
		mwclient.getGUIClient().refreshGUI(GUIClient.REFRESH_STATUS);
		mwclient.getGUIClient().refreshGUI(GUIClient.REFRESH_BATTLETABLE);
		mwclient.getGUIClient().refreshGUI(GUIClient.REFRESH_USERLIST);
	}
}
