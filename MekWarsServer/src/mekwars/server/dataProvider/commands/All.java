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

import mekwars.common.CampaignData;
import mekwars.common.util.BinWriter;
import mekwars.common.util.HibernateUtil;
import mekwars.server.dataProvider.ServerCommand;

import java.util.Date;

/**
 * Retrieve all planet information (if the data cache is lost at client side)
 *
 * @author Imi (immanuel.scholz@gmx.de)
 */
public class All implements ServerCommand {

    /**
     * @see server.dataProvider.ServerCommand#execute(java.util.Date, java.io.PrintWriter,
     *     common.CampaignData)
     */
    public void execute(Date timestamp, BinWriter out, CampaignData data) throws Exception {
        // HibernateUtil.getInstance().inSession(session -> {try { data.binOut(out); } catch(Exception e){e.printStackTrace();}});
        data.binOut(out);
    }
}
