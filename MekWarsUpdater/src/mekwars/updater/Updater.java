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

package mekwars.updater;

import megamek.Version;
import mekwars.server.campaign.CampaignMain;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Updater {
    private static final Logger logger = LogManager.getLogger(Updater.class);

    public static void main(String[] args) {

        Options options = new Options();
        Option versionCommand = new Option("v", "version", true, "Version to migrate from");
        versionCommand.setRequired(true);
        options.addOption(versionCommand);
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        new CampaignMain("./data/campaignconfig.txt");

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
        }

        Version version = new Version(cmd.getOptionValue("version"));
        VersionUpdaterPicker updatePicker = new VersionUpdaterPicker();
        
        if (!updatePicker.migrate(version)) {
            System.out.println("Invalid version. Unable to upgrade");
            System.exit(0);
        }
    }
}
