/*
 * MekWars - Copyright (C) 2016
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 */

package mekwars.server.campaign.util.scheduler;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.IOException;
import java.net.InetSocketAddress;
import mekwars.server.MWServ;
import mekwars.server.net.hpgnet.HPGSubscribedClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerKey;

import com.esotericsoftware.kryo.io.Output;
import java.io.*;

public class TrackerUpdateJob implements Job {
    private static final Logger logger = LogManager.getLogger(TrackerUpdateJob.class);

    public static String JOB_IDENTITY = "TrackerJob";
    public static String JOB_GROUP = "TrackerGroup";
    public static String TRIGGER_IDENTITY = "TrackerTrigger";

    public void execute(JobExecutionContext context) throws JobExecutionException {
        HPGSubscribedClient client = MWServ.getInstance().getHpgClient();

        if (client.isConnected() == false) {
            String trackerAddress = MWServ.getInstance().getCampaign().getCampaignOptions()
                .getConfig("TrackerAddress");

            try {
                InetSocketAddress address = new InetSocketAddress(
                    trackerAddress,
                    HPGSubscribedClient.TRACKER_PORT
                );
                MWServ.getInstance().getHpgClient().connect(address);
            } catch (IOException exception) {
                logger.catching(exception);
            }
        }

        try {
            if (client.getHpgId() == null) {
                client.getConnection().write(client.getServerRegister());
            } else {
                client.getConnection().write(client.getServerUpdate());
            }
        } catch (IOException exception) {
            logger.error("Unable to connect to HPGTracker");
        }
    }

    public static void submit() {
        JobDetail job = newJob(TrackerUpdateJob.class)
            .withIdentity(JOB_IDENTITY, JOB_GROUP)
            .build();
        SimpleTrigger trigger = newTrigger()
            .withIdentity(TRIGGER_IDENTITY, JOB_GROUP)
            .withSchedule(simpleSchedule()
                .withIntervalInMinutes(10)
                .repeatForever())
            .build();

        MWScheduler.getInstance().scheduleJob(job, trigger);
    }
}
