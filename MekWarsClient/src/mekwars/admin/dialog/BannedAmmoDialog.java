/*
 * MekWars - Copyright (C) 2004
 *
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet)
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

package mekwars.admin.dialog;

import megamek.common.AmmoType;

import mekwars.client.MWClient;
import mekwars.client.common.campaign.clientutils.GameHost;
import mekwars.common.House;
import mekwars.common.entities.BannedAmmo;
import mekwars.common.util.SpringLayoutHelper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SpringLayout;

public final class BannedAmmoDialog implements ActionListener {
    private static final Logger LOGGER = LogManager.getLogger(BannedAmmoDialog.class);

    // store the client backlink for other things to use
    private MWClient mwclient = null;
    private House house = null;

    private static final String okayCommand = "Add";
    private static final String cancelCommand = "Close";

    private String windowName = "Server Banned Ammo Editor";
    private ArrayList<JCheckBox> cBoxArrayList = new ArrayList<JCheckBox>();

    // BUTTONS
    private final JButton okayButton = new JButton("Save");
    private final JButton cancelButton = new JButton("Close");

    // STOCK DIALOUG AND PANE
    private JDialog dialog;
    private JOptionPane pane;

    JTabbedPane ConfigPane = new JTabbedPane();

    public BannedAmmoDialog(MWClient c, House house) {

        // save the client
        this.mwclient = c;
        this.house = house;

        // stored values.

        // Set the tooltips and actions for dialouge buttons
        okayButton.setActionCommand(okayCommand);
        cancelButton.setActionCommand(cancelCommand);

        okayButton.addActionListener(this);
        cancelButton.addActionListener(this);
        okayButton.setToolTipText("Save");
        cancelButton.setToolTipText("Exit without saving changes");

        // CREATE THE PANELS
        JPanel banPanel = new JPanel(); // player name, etc

        /*
         * Format the Reward Points panel. Spring layout.
         */
        banPanel.setLayout(new BoxLayout(banPanel, BoxLayout.Y_AXIS));

        JPanel ammoPanel = new JPanel(new SpringLayout());

        mwclient.loadBannedAmmo();

        Set<String> munitions = BannedAmmo.getAllMunitions();
        for (String munitionName : munitions) {
            // String munitionName = munitionNames.nextElement();
            JCheckBox cBox = new JCheckBox();
            cBox.setText(munitionName);
            cBox.setSelected(checkAmmoBan(munitionName));
            ammoPanel.add(cBox);
            cBoxArrayList.add(cBox);
        }

        SpringLayoutHelper.setupSpringGrid(ammoPanel, 2);

        banPanel.add(ammoPanel);

        // Set the user's options
        Object[] options = {okayButton, cancelButton};

        // Create the pane containing the buttons
        pane =
                new JOptionPane(
                        banPanel,
                        JOptionPane.PLAIN_MESSAGE,
                        JOptionPane.DEFAULT_OPTION,
                        null,
                        options,
                        null);

        if (house != null) windowName = this.house.getName() + " Banned Ammo Dialog";
        // Create the main dialog and set the default button
        dialog = pane.createDialog(ammoPanel, windowName);
        dialog.getRootPane().setDefaultButton(cancelButton);

        // Show the dialog and get the user's input
        dialog.setModal(true);
        dialog.pack();
        dialog.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (command.equals(okayCommand)) {
            for (JCheckBox tempBox : cBoxArrayList) {
                AmmoType.Munitions munitionType = BannedAmmo.getMunitionByName(tempBox.getText());
                BannedAmmo bannedAmmo =
                        mwclient.getData().getBannedAmmoStore().get(munitionType, house);

                if (tempBox.isSelected() != (bannedAmmo != null)) {
                    if (house == null) {
                        mwclient.sendChat(
                                GameHost.CAMPAIGN_PREFIX
                                        + "c adminsetserverammoban#"
                                        + munitionType.ordinal());
                    } else {
                        mwclient.sendChat(
                                GameHost.CAMPAIGN_PREFIX
                                        + "c adminsethouseammoban#"
                                        + house.getName()
                                        + "#"
                                        + munitionType.ordinal());
                    }
                }
            }
            dialog.dispose();
        } else if (command.equals(cancelCommand)) {
            dialog.dispose();
        }
    }

    /**
     * @return false if the ammo is banned by the player's house or the server, otherwise true.
     */
    public boolean checkAmmoBan(String ammo) {
        try {
            AmmoType.Munitions munitionType = BannedAmmo.getMunitionByName(ammo);
            return mwclient.getData().getBannedAmmoStore().get(munitionType, house) != null;
        } catch (Exception ex) {
            LOGGER.error("Unable to find ammo " + ammo);
            return false;
        }
    }
} // end BannedAmmoDialog.java
