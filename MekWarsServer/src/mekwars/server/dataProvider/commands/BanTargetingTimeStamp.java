/*
 * MekWars - Copyright (C) 2004 
 * 
 * Original Author: nmorris (urgru@users.sourceforge.net)
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

package mekwars.server.dataProvider.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Date;

import mekwars.common.CampaignData;
import mekwars.common.util.BinWriter;
import mekwars.common.util.MWLogger;
import mekwars.server.dataProvider.ServerCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Retrieve the MD5 of the current campaignconfig file.
 */
public class BanTargetingTimeStamp implements ServerCommand {
    private static final Logger LOGGER = LogManager.getLogger(BanTargetingTimeStamp.class);

    public void execute(Date timestamp, BinWriter out, CampaignData data) throws Exception {
    	
        String bannedAmmoTimestamp = "-1";
        File bannedAmmo = new File("./campaign/bantargeting.dat");
        
        if (bannedAmmo.exists()) {
            
            try {
                FileInputStream in = new FileInputStream(bannedAmmo);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                bannedAmmoTimestamp = br.readLine();
                br.close();
                in.close();
                
            } catch (Exception e) {
                LOGGER.info("Error reading first line from bantargeting.dat");
            }
        } 
        
        else
            LOGGER.info("bantargeting.dat didn't exist. returning ficticious timestamp to requesting client.");
        
        out.println(bannedAmmoTimestamp, "BanTargetingTimeStamp");
    }

}
