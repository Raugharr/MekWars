/*
 * MekWars - Copyright (C) 2025
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

package mekwars.server.campaign.util.scheduler;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.FileWriter;
import java.io.IOException;
import mekwars.server.MWServ;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.util.Statistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SimpleTrigger;

public class TickJob implements Job {
    private static final Logger logger = LogManager.getLogger(TickJob.class);
    private static int tickId = 0;
    public static final String JOB_IDENTITY = "TickJob";
    public static final String JOB_GROUP = "TickGroup";
    public static final String TRIGGER_IDENTITY = "TickTrigger";
    public static final int TICK_TIME = 15;
    private static final int NEWS_TICK_INTERVAL = 8;
    private static final int TIME_IN_MILISEC = TICK_TIME * 1000 * 60;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        CampaignMain campaign = MWServ.getInstance().getCampaign();

        logger.info("Tick (" + tickId + ") Started");
        try {
            campaign.tick(true, tickId);
        } catch (Exception ex) {
            logger.catching(ex);
            campaign.doSendToAllOnlinePlayers("Tick skipped. Errors occured", true);
        }
        
        try {
            campaign.toFile();
        } catch (Exception ex) {
            campaign.doSendToAllOnlinePlayers("Warning! AutoSave failed!", true);
        }
        
        if (this.tickId % NEWS_TICK_INTERVAL == 0) {
            MWServ.getInstance().addToNewsFeed("Faction Rankings", "Server News", Statistics.getReadableHouseRanking(false));
            if (CampaignMain.cm.getBooleanConfig("DiscordEnable")) {
                MWServ.getInstance().postToDiscord(Statistics.getReadableHouseRanking(false));
            }
            
            try {
                FileWriter out = new FileWriter(campaign.getConfig("HouseRankPath"), true); // opened in APPEND mode; will be controlled by config setting
                out.write(Statistics.getReadableHouseRanking(false)); // dump actual SHouse Ranking data to a permanent file
                out.write("\n");
                out.close();
            } catch (IOException e) {
                logger.catching(e);
            }
        }
        logger.info("Tick (" + tickId + ") Finished");
        campaign.doSendToAllOnlinePlayers("CC|NT|" + TIME_IN_MILISEC + "|" + true, false);
    }

     public static void submit() {
        JobDetail job = newJob(TickJob.class)
            .withIdentity(JOB_IDENTITY, JOB_GROUP)
            .build();
        SimpleTrigger trigger = newTrigger()
            .withIdentity(TRIGGER_IDENTITY, JOB_GROUP)
            .withSchedule(simpleSchedule()
                .withIntervalInMinutes(TICK_TIME)
                .repeatForever())
            .build();

        MWScheduler.getInstance().scheduleJob(job, trigger);
    }

    public static long millisecondsUntilNextFire() throws Exception {
        return MWScheduler.millisecondsUntilNextFire(TRIGGER_IDENTITY, JOB_GROUP);
    }

    public static int getTickID() {
        return tickId;
    }
}
