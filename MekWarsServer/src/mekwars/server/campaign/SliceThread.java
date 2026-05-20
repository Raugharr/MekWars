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

package mekwars.server.campaign;

import mekwars.common.CampaignData;
import mekwars.common.util.HibernateUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

/**
 * @author urgru A barebones timing thread which calls slices in CampaignMain.
 *     <p>Created in CM as follows: SThread = new SliceThread(this,
 *     Integer.parseInt(getConfig("SliceTime"))); SThread.start();//it slices, it dices, it chops!
 */
public class SliceThread extends Thread {
    private static final Logger LOGGER = LogManager.getLogger(SliceThread.class);

    CampaignMain myCampaign;
    long until;
    int Duration;
    int sliceid = 0;
    int lastHouseId = 0;

    public SliceThread(CampaignMain main, int Duration) {
        super("slicethread");
        this.Duration = Duration; // set length when thread is spun
        myCampaign = main;
    }

    public int getSliceID() {
        return sliceid;
    }

    public void extendedWait(int time) {
        until = System.currentTimeMillis() + time;
        try {
            this.wait(time);
        } catch (Exception ex) {
            LOGGER.error("Exception: ", ex);
        }
    } // end ExtendedWait(time)

    public long getRemainingSleepTime() {
        return Math.max(0, until - System.currentTimeMillis());
    }

    @Override
    public synchronized void run() {
        try {
            int sleepTime = Duration;
            while (true) {
                this.extendedWait(sleepTime);
                final long startTime = System.currentTimeMillis();
                sliceid++;
                HibernateUtil.inTransaction(session -> runSession(session, startTime));
                sleepTime = (int) (Duration - (System.currentTimeMillis() - startTime));
                sleepTime = Math.max(100, sleepTime);
            }
        } catch (Exception ex) {
            LOGGER.error("Exception: ", ex);
        }
    }

    private void runSession(Session session, long startTime) {
        try {
            myCampaign.slice(getSliceID());

            if (CampaignData.cd.getCampaignOptions().getBooleanConfig("ProcessHouseTicksAtSlice")) {
                long endTime = startTime + Duration / 2;
                while (endTime > System.currentTimeMillis()) {
                    if (lastHouseId > CampaignMain.cm.getData().getAllHouses().size()) {
                        lastHouseId = 0;
                    }
                    SHouse house = CampaignMain.cm.getHouseById(lastHouseId);
                    long onlinePlayers =
                            session.createQuery(
                                            "SELECT COUNT(p) SPlayer p LEFT JOIN SHouse h ON"
                                                + " :houseId == p.house_id",
                                            Long.class)
                                    .setParameter("houseId", house.getId())
                                    .getSingleResult();
                    if (house != null && onlinePlayers > 0) {
                        CampaignMain.cm.getHouseById(lastHouseId).tick(true, sliceid);
                        lastHouseId++;
                    } else {
                        lastHouseId++;
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Exception: ", ex);
            myCampaign.doSendToAllOnlinePlayers("Slice skipped. Errors occured", true);
        }
    }
}
