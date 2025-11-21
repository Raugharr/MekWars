/*
 * MekWars - Copyright (C) 2004
 *
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megamek)
 * Original author Helge Richter (McWizard)
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

package mekwars.client.cmd;

import java.util.StringTokenizer;

import mekwars.client.MWClient;
import mekwars.client.gui.dialog.RepodSelectorDialog;
import megamek.client.ui.swing.UnitLoadingDialog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @@author jtighe
 */
public class RUD extends Command {
    private static final Logger LOGGER = LogManager.getLogger(RUD.class);
    /**
     * @param client
     */
    public RUD(MWClient mwclient) {
        super(mwclient);
    }

    /**
     * @see client.cmd.Command#execute(java.lang.String)
     */
    @Override
    public void execute(String input) {
        StringTokenizer ST = decode(input);

        try {

            String unitId = ST.nextToken();

            if (!ST.hasMoreTokens()) {
                String toUser = "CH|CLIENT: Your faction has no repod options for Unit "
                        + unitId + ".";
                mwclient.doParseDataInput(toUser);
            } else {
                String chassieList = ST.nextToken();
                UnitLoadingDialog unitLoadingDialog = new UnitLoadingDialog(
                        mwclient.getGUIClient().getMainFrame());
                RepodSelectorDialog repodSelector = new RepodSelectorDialog(
                        mwclient.getGUIClient().getMainFrame(), unitLoadingDialog, mwclient,
                        chassieList, unitId);
                Thread.sleep(125);
                new Thread(repodSelector).start();
            }

        } catch (Exception ex) {
            LOGGER.error("Exception: ", ex);
            LOGGER.error("Unable to run Repod Dialog");
        }
    }
}
