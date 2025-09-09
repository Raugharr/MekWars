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

package mekwars.server.dataProvider.commands;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Date;

import mekwars.common.CampaignData;
import mekwars.common.util.BinWriter;
import mekwars.server.MWServ;
import mekwars.server.dataProvider.ServerCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Retrieve all planet information (if the data cache is lost at client side)
 * 
 * @author Imi (immanuel.scholz@gmx.de)
 */
public class ServerConfig implements ServerCommand {
    private static final Logger logger = LogManager.getLogger(ServerConfig.class);

    /**
     * @see server.dataProvider.ServerCommand#execute(java.util.Date,
     *      java.io.PrintWriter, common.CampaignData)
     */
    public void execute(Date timestamp, BinWriter out, CampaignData data)
            throws Exception {
        try {
            String campaignConfig = MWServ.getInstance().getConfigParam("CAMPAIGNCONFIG");
            FileInputStream configFile = new FileInputStream(campaignConfig);
            BufferedReader config = new BufferedReader(new InputStreamReader(configFile));
             
            while (config.ready()) {
                out.println(config.readLine(),"ConfigLine");
            }
        
            config.close();
        } catch (Exception ex) {
           logger.error("ServerConfig failed"); 
           logger.error(ex); 
        }
    }
}
