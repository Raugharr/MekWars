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

package mekwars.client.gui.dialog;

import mekwars.client.MWClient;
import mekwars.client.common.campaign.clientutils.GameHost;
import mekwars.client.io.FileSystem;
import mekwars.common.House;
import mekwars.common.campaign.pilot.skills.PilotSkill;
import mekwars.common.util.SpringLayoutHelper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

public final class TraitDialog implements ActionListener, KeyListener {
    private static class SkillEntry {
        private int skillId;
        private JLabel label;
        private JTextField field;

        private SkillEntry(int skillId, String name, boolean editable) {
            this.skillId = skillId;
            this.label = new JLabel(name + ":", SwingConstants.TRAILING);
            this.field = new JTextField(3);
            this.field.setEditable(editable);
            String toolTipText =
                    "<html>"
                            + name
                            + "<br>Modifies the chance for a pilot to receive this"
                            + " skill</html>";
            getField().setToolTipText(toolTipText);
        }

        public int getSkillId() {
            return skillId;
        }

        public JLabel getLabel() {
            return label;
        }

        public JTextField getField() {
            return field;
        }
    }

    private static final Logger LOGGER = LogManager.getLogger(TraitDialog.class);
    private final List<SkillEntry> skillEntries;

    // store the client backlink for other things to use
    private MWClient mwclient = null;

    private static final String okayCommand = "Add";
    private static final String cancelCommand = "Close";
    private static final String removeCommand = "Remove";
    private static final String traitCommand = "Trait";
    private static final String factionCommand = "Faction";

    private String windowName = "Trait Editor";

    private static final String delimiter = "*";

    // BUTTONS
    private final JButton okayButton = new JButton("Add");
    private final JButton cancelButton = new JButton("Close");
    private final JButton removeButton = new JButton("Remove");

    // TEXT FIELDS
    // tab names
    private final JLabel factionLabel = new JLabel("Faction:", SwingConstants.TRAILING);
    private final JLabel traitLabel = new JLabel("Trait:", SwingConstants.TRAILING);

    private JComboBox<String> factionComboBox = new JComboBox<String>();
    private JComboBox<String> traitComboBox = new JComboBox<String>();

    // STOCK DIALOUG AND PANE
    private JDialog dialog;
    private JOptionPane pane;

    public TraitDialog(MWClient c, boolean player) {
        // save the client
        this.mwclient = c;
        this.skillEntries = createSkillEntries(!player);

        // COMBO BOXES
        TreeSet<String> names = new TreeSet<String>();
        names.add("Common"); // start with the common faction
        for (House factions : mwclient.getData().getAllHouses()) {
            names.add(factions.getName());
        }

        factionComboBox = new JComboBox<String>(names.toArray(new String[names.size()]));
        traitComboBox.setEditable(!player);

        // stored values.

        // Set the tooltips and actions for dialouge buttons
        okayButton.setActionCommand(okayCommand);
        cancelButton.setActionCommand(cancelCommand);
        factionComboBox.setActionCommand(factionCommand);
        traitComboBox.setActionCommand(traitCommand);

        okayButton.addActionListener(this);
        cancelButton.addActionListener(this);
        removeButton.addActionListener(this);
        okayButton.setToolTipText("Save Trait");
        if (player) {
            cancelButton.setToolTipText("Exit");
            windowName = "Trait Viewer";
        } else {
            cancelButton.setToolTipText("Exit without saving changes");
        }
        removeButton.setToolTipText("Delete Trait");
        traitComboBox.addActionListener(this);
        factionComboBox.addActionListener(this);

        okayButton.setVisible(!player);
        removeButton.setVisible(!player);

        // CREATE THE PANELS
        JPanel traitsPanel = new JPanel(); // player name, etc

        /*
         * Format the Reward Points panel. Spring layout.
         */
        traitsPanel.setLayout(new BoxLayout(traitsPanel, BoxLayout.Y_AXIS));

        JPanel skillPanel = new JPanel(new SpringLayout());

        JPanel comboPanel = new JPanel(new SpringLayout());

        comboPanel.add(factionLabel);
        factionComboBox.setToolTipText("Select a faction");
        comboPanel.add(factionComboBox);

        comboPanel.add(traitLabel);
        if (player) traitComboBox.setToolTipText("Select a trait.");
        else traitComboBox.setToolTipText("Select a trait or enter a new one.");
        comboPanel.add(traitComboBox);

        for (SkillEntry skillEntry : skillEntries) {
            skillPanel.add(skillEntry.getLabel());
            skillPanel.add(skillEntry.getField());
        }

        // run the spring layout
        SpringLayoutHelper.setupSpringGrid(comboPanel, 2);
        SpringLayoutHelper.setupSpringGrid(skillPanel, 8);

        traitsPanel.add(comboPanel);
        traitsPanel.add(skillPanel);

        JPanel mainPanel = new JPanel();

        // Set the user's options
        Object[] options = {okayButton, removeButton, cancelButton};

        // Create the pane containing the buttons
        pane =
                new JOptionPane(
                        traitsPanel,
                        JOptionPane.PLAIN_MESSAGE,
                        JOptionPane.DEFAULT_OPTION,
                        null,
                        options,
                        null);

        // Create the main dialog and set the default button
        dialog = pane.createDialog(mainPanel, windowName);
        dialog.getRootPane().setDefaultButton(cancelButton);

        mwclient.loadServerTraitFiles();

        factionComboBox.setSelectedIndex(0);
        dialog.setLocationRelativeTo(mwclient.getGUIClient().getMainFrame());
        // Show the dialog and get the user's input
        dialog.setModal(true);
        dialog.pack();
        dialog.setVisible(true);

        if (pane.getValue() == okayButton) {

        } else dialog.dispose();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        String faction = (String) factionComboBox.getSelectedItem();
        String selectedTrait = (String) traitComboBox.getSelectedItem();

        if (e.getComponent().equals(factionComboBox)) {
            loadFactionTraits(faction);
        } else if (selectedTrait != null) {
            populateTraits(faction, selectedTrait.trim());
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {}

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (command.equals(okayCommand)) {
            String faction = (String) factionComboBox.getSelectedItem();
            String trait = ((String) traitComboBox.getSelectedItem()).trim();
            if (trait.trim().length() < 1) {
                JOptionPane.showMessageDialog(null, "You did not enter a trait name!");
                return;
            }

            String result = getResults(faction, trait);
            mwclient.sendChat(GameHost.CAMPAIGN_PREFIX + "c addtrait#" + result);
            mwclient.loadServerTraitFiles();
            loadFactionTraits(faction);
        } else if (command.equals(cancelCommand)) {
            pane.setValue(cancelButton);
            dialog.dispose();
        } else if (command.equals(removeCommand)) {
            String faction = (String) factionComboBox.getSelectedItem();
            String trait = ((String) traitComboBox.getSelectedItem()).trim();

            if (trait.length() < 1) {
                JOptionPane.showMessageDialog(
                        null, "You have to select a trait before you can remove it!");
                return;
            }
            int choice =
                    JOptionPane.showConfirmDialog(
                            null,
                            "Are you sure you want to remove this trait?",
                            "Remove it?",
                            JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.OK_OPTION) {
                mwclient.sendChat(
                        GameHost.CAMPAIGN_PREFIX
                                + "c removetrait#"
                                + faction
                                + "#"
                                + trait
                                + "#CONFIRM");
                mwclient.loadServerTraitFiles();
                loadFactionTraits(faction);
            }
        } else if (command.equals(factionCommand)) {
            String selection = (String) factionComboBox.getSelectedItem();
            loadFactionTraits(selection);
        } else if (command.equals(traitCommand)) {
            String faction = (String) factionComboBox.getSelectedItem();
            if (traitComboBox.getSelectedItem() == null) return;
            String trait = ((String) traitComboBox.getSelectedItem()).trim();
            populateTraits(faction, trait);
        }
    }

    private void loadFactionTraits(String faction) {
        if (traitComboBox.getItemCount() > 0) {
            traitComboBox.removeAllItems();
        }
        try {
            Path path = FileSystem.getInstance().getFactionTraitNamesPath(faction);

            if (!Files.exists(path)) {
                return;
            }
            List<String> lines = Files.readAllLines(path);

            for (String line : lines) {
                StringTokenizer traitName = new StringTokenizer(line, delimiter);
                traitComboBox.addItem(traitName.nextToken());
            }
            if (traitComboBox.getItemCount() > 0) {
                traitComboBox.setSelectedIndex(0);
            }
            traitComboBox.revalidate();
        } catch (IOException exception) {
            LOGGER.error("Unable to load faction traits", exception);
        }
    }

    private void populateTraits(String faction, String trait) {
        try {
            List<String> lines =
                    Files.readAllLines(FileSystem.getInstance().getFactionTraitNamesPath(faction));

            for (SkillEntry skillEntry : skillEntries) {
                skillEntry.getField().setText("0");
            }
            for (String line : lines) {
                StringTokenizer traitNames = new StringTokenizer(line, delimiter);
                String traitName = traitNames.nextToken();

                if (traitName.equalsIgnoreCase(trait)) {
                    while (traitNames.hasMoreTokens()) {
                        int traitID = Integer.parseInt(traitNames.nextToken());
                        String traitMod = traitNames.nextToken();

                        for (SkillEntry skillEntry : skillEntries) {
                            if (traitID == skillEntry.getSkillId()) {
                                skillEntry.getField().setText(traitMod);
                            }
                        }
                    }
                }
            }
        } catch (IOException exception) {
            LOGGER.error("Unable to load faction traits", exception);
        }
    }

    public String getResults(String faction, String trait) {
        StringBuilder result = new StringBuilder(faction).append("#").append(trait).append("#");

        for (SkillEntry skillEntry : skillEntries) {
            try {
                int value = Integer.parseInt(skillEntry.getField().getText().trim());

                if (value != 0) {
                    result.append(skillEntry.getSkillId())
                            .append(delimiter)
                            .append(skillEntry.getField().getText())
                            .append(delimiter);
                }
            } catch (NumberFormatException exception) {
                LOGGER.error("Invalid skill value {}", skillEntry.getField().getText());
            }
        }

        result.append("#CONFIRM");
        return result.toString();
    }

    private List<SkillEntry> createSkillEntries(boolean editable) {
        return List.of(
                new SkillEntry(PilotSkill.GunneryLaserSkillID, "Gunnery Laser", editable),
                new SkillEntry(PilotSkill.GunneryBallisticSkillID, "Gunnery Balistic", editable),
                new SkillEntry(PilotSkill.GunneryMissileSkillID, "Gunnery Missile", editable),
                new SkillEntry(PilotSkill.AstechSkillID, "Astech", editable),
                new SkillEntry(PilotSkill.TacticalGeniusSkillID, "Tactical Genius", editable),
                new SkillEntry(PilotSkill.WeaponSpecialistSkillID, "Weapon Specialist", editable),
                new SkillEntry(PilotSkill.MeleeSpecialistSkillID, "Melee Specialist", editable),
                new SkillEntry(PilotSkill.DodgeManeuverSkillID, "Dodge Maneuver", editable),
                new SkillEntry(PilotSkill.IronManSkillID, "Iron Man", editable),
                new SkillEntry(PilotSkill.ManeuveringAceSkillID, "Maneuvering Ace", editable),
                new SkillEntry(
                        PilotSkill.NaturalAptitudeGunnerySkillID,
                        "Natural Aptitude Gunnery",
                        editable),
                new SkillEntry(
                        PilotSkill.NaturalAptitudePilotingSkillID,
                        "Natural Aptitude Piloting",
                        editable),
                new SkillEntry(PilotSkill.PainResistanceSkillID, "Pain Resistance", editable),
                new SkillEntry(PilotSkill.SurvivalistSkillID, "Survival", editable),
                new SkillEntry(PilotSkill.EnhancedInterfaceID, "Enhanced Interface", editable),
                new SkillEntry(PilotSkill.QuickStudyID, "Quick Study", editable),
                new SkillEntry(PilotSkill.GiftedID, "Gifted Study", editable),
                new SkillEntry(PilotSkill.MedTechID, "Medtech", editable));
    }
}
