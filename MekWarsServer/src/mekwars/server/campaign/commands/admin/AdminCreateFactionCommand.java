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

/*
 * Created on 14.04.2004
 *
 */
package mekwars.server.campaign.commands.admin;

import java.util.StringTokenizer;
import mekwars.server.MWServ;
import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SHouse;
import mekwars.server.campaign.SPlanet;
import mekwars.common.Influences;
import mekwars.server.campaign.commands.Command;
import java.util.HashMap;
import java.util.Random;


/**
 * @author Helge Richter
 */
public class  AdminCreateFactionCommand  implements Command {
    int accessLevel = IAuthenticator.ADMIN;
    String syntax = "name#color(hex)#basegunner#basePilot#Abbreviation";
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
            String name = command.nextToken();
            String color = command.nextToken();
            int baseGunner = Integer.parseInt(command.nextToken());
            int basePilot = Integer.parseInt(command.nextToken());
            String Abb = command.nextToken();
            
            SHouse newfaction = new SHouse(name, "#" + color, baseGunner, basePilot, Abb); 
            newfaction.updated();
            
            CampaignMain.cm.addHouse(newfaction);
            CampaignMain.cm.doSendToAllOnlinePlayers("PL|ANH|" + newfaction.addNewHouse(), false);
            CampaignMain.cm.toUser("Faction created!",Username,true);
            CampaignMain.cm.doSendModMail("NOTE",Username + " has created faction " + newfaction.getName());
            
            // Create a home planet for the new faction if single-player factions are allowed
            if (CampaignMain.cm.getBooleanConfig("AllowSinglePlayerFactions")) {
                createHomePlanet(newfaction);
            }
        }
        catch(Exception ex){
            CampaignMain.cm.toUser("Invalid Syntax: /AdminCreateFaction Name#Color(hex)#BaseGunner#BasePilot#Abberviation",Username,true);
            return;
        }
    }
    
    /**
     * Creates a home planet for the newly created faction
     * @param faction The faction to create the planet for
     */
    private void createHomePlanet(SHouse faction) {
        try {
            // Generate random coordinates within a reasonable range
            Random random = new Random();
            double x = 100 + random.nextDouble() * 800; // Random x between 100 and 900
            double y = 100 + random.nextDouble() * 600; // Random y between 100 and 700
            
            // Create influence map with 100% control for the new faction
            HashMap<Integer, Integer> influenceMap = new HashMap<>();
            influenceMap.put(faction.getId(), 100);
            
            // Create the planet with the faction's name as the planet name
            String planetName = faction.getName() + " Prime";
            SPlanet planet = new SPlanet(planetName, new Influences(influenceMap), 5, x, y);
            
            // Set planet properties
            planet.setBaysProvided(10); // Provide some starting bays
            planet.setConquestPoints(100); // Full control points
            planet.setOriginalOwner(faction.getName());
            
            // Add the planet to the campaign and set the owner
            CampaignMain.cm.addPlanet(planet);
            planet.setOwner(null, faction, true);
            planet.updated();
            
            // Log the creation
            CampaignMain.cm.doSendModMail("NOTE", "Created home planet " + planetName + 
                " for faction " + faction.getName() + " at coordinates (" + x + ", " + y + ")");
            
        } catch (Exception e) {
            CampaignMain.cm.toUser("AM:Failed to create home planet: " + e.getMessage(), "SYSTEM", true);
            e.printStackTrace();
        }
    }
}
