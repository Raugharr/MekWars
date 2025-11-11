/*
 * MekWars - Copyright (C) 2005 
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
import mekwars.server.dataProvider.ServerCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Command which calcaulates and returns the 
 * timestamp of the current OpList.txt file.
 */
public class OpListTimestamp implements ServerCommand {
    private static final Logger LOGGER = LogManager.getLogger(OpListTimestamp.class);

    public void execute(Date timestamp, BinWriter out, CampaignData data) throws Exception {
    	
    	String oplistTimestamp = "-1";
        File opList = new File("./data/operations/OpList.txt");
    	if (opList.exists()) {
    		
    		try {
				FileInputStream in = new FileInputStream("./data/operations/OpList.txt");
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String tempTime = br.readLine();
				br.close();
				in.close();
				
				oplistTimestamp = tempTime.substring(11);//remove "#Timestamp="
			} catch (Exception e) {
				LOGGER.info("Error reading first line from OpList.txt");
			}
    	
    	}//end if(oplist exists)
    		
    	else
    		LOGGER.info("OpList.txt didn't exist. Returning falsified timestamp to requesting client.");
       
        out.println(oplistTimestamp, "OpListTimestamp");
        out.flush();
    }
}
