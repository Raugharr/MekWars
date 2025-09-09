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

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.StringTokenizer;
import mekwars.server.MWServ;
import mekwars.common.Continent;
import mekwars.common.House;
import mekwars.common.Planet;
import mekwars.common.Unit;
import mekwars.common.UnitFactory;
import mekwars.common.util.MWLogger;
import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.campaign.BuildTable;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SHouse;
import mekwars.server.campaign.SPlanet;
import mekwars.server.campaign.SUnitFactory;
import mekwars.server.campaign.commands.Command;

public class AdminSavePlanetsToXMLCommand implements Command {
    int accessLevel = IAuthenticator.ADMIN;
    String syntax = "";

    public int getExecutionLevel() {
        return accessLevel;
    }

    public void setExecutionLevel(int i) {
        accessLevel = i;
    }

    public String getSyntax() {
        return syntax;
    }

    public void process(StringTokenizer command, String Username) {

        // access level check
        int userLevel = MWServ.getInstance().getUserLevel(Username);
        if (userLevel < getExecutionLevel()) {
            CampaignMain.cm.toUser("AM:Insufficient access level for command. Level: " + userLevel + ". Required: " + accessLevel + ".", Username, true);
            return;
        }

        try {
            FileOutputStream out = new FileOutputStream("./campaign/saveplanets.xml");
            PrintStream p = new PrintStream(out);
            Collection<Planet> planets = CampaignMain.cm.getData().getAllPlanets();
            p.print(CampaignMain.cm.getXStream().toXML(planets.toArray()));
            p.close();
            out.close();
        } catch (Exception ex) {
            MWLogger.errLog(ex);
        }
        CampaignMain.cm.toUser("XML saved!", Username, true);
        CampaignMain.cm.doSendModMail("NOTE", Username + " has saved the universe to XML");
    }
}
