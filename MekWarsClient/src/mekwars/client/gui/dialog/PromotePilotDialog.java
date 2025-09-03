/*
 * MekWars - Copyright (C) 2008
 *
 * Original author - Torren (torren@users.sourceforge.net)
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */

package mekwars.client.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import mekwars.client.MWClient;
import mekwars.client.campaign.CUnit;
import mekwars.client.common.campaign.clientutils.GameHost;
import mekwars.common.Unit;
import mekwars.common.campaign.pilot.Pilot;
import mekwars.common.campaign.pilot.skills.PilotSkill;
import mekwars.common.util.SpringLayoutHelper;

public class PromotePilotDialog extends JFrame implements ActionListener, KeyListener {

    /**
     *
     */
    private static final long serialVersionUID = -8988175448434842033L;
    // store the client backlink for other things to use
    private MWClient mwclient = null;
    private CUnit playerUnit = null;
    private Pilot pilot = null;
    private boolean demoting = false;

    private final static String okayCommand = "Ok";
    private final static String cancelCommand = "Close";

    private String windowName = "Bulk Repair Dialog";

    // BUTTONS
    private final JButton okayButton = new JButton("Buy");
    private final JButton cancelButton = new JButton("Close");

    // STOCK DIALOUG AND PANE
    // private JDialog dialog;
    private JOptionPane pane;
    private JPanel MasterPanel = new JPanel(new SpringLayout());
    private JPanel contentPane = new JPanel();
    private JCheckBox masterCB = new JCheckBox();
    private JTextField currentExp = new JTextField();
    private JTextField expCost = new JTextField();

    public PromotePilotDialog(MWClient c, int unitID, boolean demoting) {

        // save the client
        mwclient = c;
        playerUnit = c.getPlayer().getUnit(unitID);
        this.demoting = demoting;

        windowName = "Pilot Promotion Dialog";

        addKeyListener(this);

        // Set the tooltips and actions for dialouge buttons
        okayButton.setActionCommand(okayCommand);
        okayButton.addActionListener(this);

        if (demoting) {
            okayButton.setText("Sell");
            okayButton.setToolTipText("Sell Pilot Skill.");
            okayButton.setMnemonic('S');
        } else {
            okayButton.setToolTipText("Buy Pilot Skill.");
            okayButton.setMnemonic('B');
        }

        cancelButton.setActionCommand(cancelCommand);
        cancelButton.addActionListener(this);
        cancelButton.setToolTipText("Close dialog");
        cancelButton.setDefaultCapable(true);

        loadPanel();

        // CREATE THE PANELS
        SpringLayoutHelper.setupSpringGrid(MasterPanel, 4);

        // Set the user's options
        Object[] options = { okayButton, cancelButton };

        // Create the pane containing the buttons
        pane = new JOptionPane(MasterPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, options, null);

        setTitle(windowName);

        contentPane = (JPanel) getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(pane, BorderLayout.CENTER);
        setResizable(true);
        this.setSize(new Dimension(369, 287));
        setExtendedState(Frame.NORMAL);
        addKeyListener(this);
        contentPane.addKeyListener(this);
        pane.addKeyListener(this);
        MasterPanel.addKeyListener(this);
        cancelButton.addKeyListener(this);

        pane.getRootPane().setDefaultButton(cancelButton);
        addKeyListener(this);

        this.repaint();
        setLocationRelativeTo(mwclient.getMainFrame());

        pack();
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (command.equals(okayCommand)) {
            sendPromoteCommands();
            super.dispose();
        } else if (command.equals(cancelCommand)) {
            super.dispose();
        } else if (e.getSource() instanceof JCheckBox) {
            calculateExpCost();
        }

    }

    private void loadPanel() {

        pilot = playerUnit.getPilot();

        Dimension dim = new Dimension(30, 10);

        MasterPanel.add(new JLabel("Current Exp", SwingConstants.TRAILING));
        currentExp.setEditable(false);
        currentExp.setText(Integer.toString(pilot.getExperience()));
        currentExp.setMinimumSize(dim);
        currentExp.setPreferredSize(dim);
        currentExp.setMaximumSize(dim);
        MasterPanel.add(currentExp);

        MasterPanel.add(new JLabel("Exp Cost", SwingConstants.TRAILING));
        expCost.setEditable(false);
        expCost.setMinimumSize(dim);
        expCost.setPreferredSize(dim);
        expCost.setMaximumSize(dim);
        MasterPanel.add(expCost);

        if (pilot.getGunnery() > Integer.parseInt(mwclient.getServerConfigs("BestGunnerySkill")) && !demoting) {
            masterCB = new JCheckBox("Gunnery " + (pilot.getGunnery() - 1));
            masterCB.setName("gunnery");
            masterCB.addActionListener(this);
            MasterPanel.add(masterCB);
        }

        if (pilot.getPiloting() > Integer.parseInt(mwclient.getServerConfigs("BestPilotingSkill")) && !demoting) {
            masterCB = new JCheckBox("Piloting " + (pilot.getPiloting() - 1));
            masterCB.setName("piloting");
            masterCB.addActionListener(this);
            MasterPanel.add(masterCB);
        }

        int maxSkills = Integer.parseInt(mwclient.getServerConfigs("MaxPilotUpgrades"));
        if (maxSkills > -1 && pilot.getSkills().size() >= maxSkills && !demoting) {
            return;
        }

        if (demoting) {
            if (Integer.parseInt(mwclient.getServerConfigs("chanceforATfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && (pilot.getSkills().has(PilotSkill.AstechSkillID))) {
                masterCB = new JCheckBox("Astech");
                masterCB.setName("chanceforATfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforDMfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && pilot.getSkills().has(PilotSkill.DodgeManeuverSkillID)) {
                masterCB = new JCheckBox("Dodge Maneuver");
                masterCB.setName("chanceforDMfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforMSfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && pilot.getSkills().has(PilotSkill.MeleeSpecialistSkillID)) {
                masterCB = new JCheckBox("Melee Specialist");
                masterCB.setName("chanceforMSfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforPRfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && pilot.getSkills().has(PilotSkill.PainResistanceSkillID)) {
                masterCB = new JCheckBox("Pain Resistance");
                masterCB.setName("chanceforPRfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforSVfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && pilot.getSkills().has(PilotSkill.SurvivalistSkillID)) {
                masterCB = new JCheckBox("Survivalist");
                masterCB.setName("chanceforSVfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforIMfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && pilot.getSkills().has(PilotSkill.IronManSkillID)) {
                masterCB = new JCheckBox("Iron Man");
                masterCB.setName("chanceforIMfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforEDfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && (pilot.getSkills().has(PilotSkill.EdgeSkillID))) {
                masterCB = new JCheckBox("Edge");
                masterCB.setName("chanceforEDfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforMAfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && pilot.getSkills().has(PilotSkill.ManeuveringAceSkillID)) {
                masterCB = new JCheckBox("Maneuvering Ace");
                masterCB.setName("chanceforMAfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforNAPfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && pilot.getSkills().has(PilotSkill.NaturalAptitudePilotingSkillID)) {
                masterCB = new JCheckBox("Natural Aptitude Piloting");
                masterCB.setName("chanceforNAPfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforNAGfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && pilot.getSkills().has(PilotSkill.NaturalAptitudeGunnerySkillID)) {
                masterCB = new JCheckBox("Natural Aptitude Gunnery");
                masterCB.setName("chanceforNAGfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforWSfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && pilot.getSkills().has(PilotSkill.WeaponSpecialistSkillID)) {
                masterCB = new JCheckBox("Weapon Specialist");
                masterCB.setName("chanceforWSfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforTGfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && pilot.getSkills().has(PilotSkill.TacticalGeniusSkillID)) {
                masterCB = new JCheckBox("Tactical Genius");
                masterCB.setName("chanceforTGfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforGMfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && pilot.getSkills().has(PilotSkill.GunneryMissileSkillID)) {
                masterCB = new JCheckBox("Gunnery Missile");
                masterCB.addActionListener(this);
                masterCB.setName("chanceforGMfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforGBfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && pilot.getSkills().has(PilotSkill.GunneryBallisticSkillID)) {
                masterCB = new JCheckBox("Gunnery Ballistic");
                masterCB.setName("chanceforGBfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforGLfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && pilot.getSkills().has(PilotSkill.GunneryLaserSkillID)) {
                masterCB = new JCheckBox("Gunnery Laser");
                masterCB.setName("chanceforGLfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforTNfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && pilot.getSkills().has(PilotSkill.TraitID)) {
                masterCB = new JCheckBox("Trait");
                masterCB.setName("chanceforTNfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforEIfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && pilot.getSkills().has(PilotSkill.EnhancedInterfaceID)) {
                masterCB = new JCheckBox("Enhanced Interface");
                masterCB.setName("chanceforEIfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforGTfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && pilot.getSkills().has(PilotSkill.GiftedID)) {
                masterCB = new JCheckBox("Gifted");
                masterCB.setName("chanceforGTfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforQSfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && pilot.getSkills().has(PilotSkill.QuickStudyID)) {
                masterCB = new JCheckBox("Quick Study");
                masterCB.setName("chanceforQSfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforMTfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && pilot.getSkills().has(PilotSkill.MedTechID)) {
                masterCB = new JCheckBox("Med Tech");
                masterCB.setName("chanceforMTfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforVDNIfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && pilot.getSkills().has(PilotSkill.VDNIID)) {
                masterCB = new JCheckBox("VDNI");
                masterCB.setName("chanceforVDNIfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforBVDNIfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && pilot.getSkills().has(PilotSkill.BufferedVDNIID)) {
                masterCB = new JCheckBox("Buffered VDNI");
                masterCB.setName("chanceforBVDNIfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforPSfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && pilot.getSkills().has(PilotSkill.PainShuntID)) {
                masterCB = new JCheckBox("Pain Shunt");
                masterCB.setName("chanceforPSfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

        } else {

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforATfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && (!pilot.getSkills().has(PilotSkill.AstechSkillID) || pilot.getSkills().getPilotSkill(PilotSkill.AstechSkillID).getLevel() < 2)) {
                masterCB = new JCheckBox("Astech");
                masterCB.setName("chanceforATfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforDMfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && !pilot.getSkills().has(PilotSkill.DodgeManeuverSkillID)) {
                masterCB = new JCheckBox("Dodge Maneuver");
                masterCB.setName("chanceforDMfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforMSfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && !pilot.getSkills().has(PilotSkill.MeleeSpecialistSkillID)) {
                masterCB = new JCheckBox("Melee Specialist");
                masterCB.setName("chanceforMSfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforPRfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && !pilot.getSkills().has(PilotSkill.PainResistanceSkillID)) {
                masterCB = new JCheckBox("Pain Resistance");
                masterCB.setName("chanceforPRfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforSVfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && !pilot.getSkills().has(PilotSkill.SurvivalistSkillID)) {
                masterCB = new JCheckBox("Survivalist");
                masterCB.setName("chanceforSVfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforIMfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && !pilot.getSkills().has(PilotSkill.IronManSkillID)) {
                masterCB = new JCheckBox("Iron Man");
                masterCB.setName("chanceforIMfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforEDfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && (!pilot.getSkills().has(PilotSkill.EdgeSkillID) || pilot.getSkills().getPilotSkill(PilotSkill.EdgeSkillID).getLevel() < Integer.parseInt(mwclient.getServerConfigs("MaxEdgeChanges")))) {
                masterCB = new JCheckBox("Edge");
                masterCB.setName("chanceforEDfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforMAfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && !pilot.getSkills().has(PilotSkill.ManeuveringAceSkillID)) {
                masterCB = new JCheckBox("Maneuvering Ace");
                masterCB.setName("chanceforMAfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforNAPfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && !pilot.getSkills().has(PilotSkill.NaturalAptitudePilotingSkillID)) {
                masterCB = new JCheckBox("Natural Aptitude Piloting");
                masterCB.setName("chanceforNAPfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforNAGfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && !pilot.getSkills().has(PilotSkill.NaturalAptitudeGunnerySkillID)) {
                masterCB = new JCheckBox("Natural Aptitude Gunnery");
                masterCB.setName("chanceforNAGfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforWSfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && !pilot.getSkills().has(PilotSkill.WeaponSpecialistSkillID)) {
                masterCB = new JCheckBox("Weapon Specialist");
                masterCB.setName("chanceforWSfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforTGfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && !pilot.getSkills().has(PilotSkill.TacticalGeniusSkillID)) {
                masterCB = new JCheckBox("Tactical Genius");
                masterCB.setName("chanceforTGfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforGMfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && !pilot.getSkills().has(PilotSkill.GunneryMissileSkillID)) {
                masterCB = new JCheckBox("Gunnery Missile");
                masterCB.addActionListener(this);
                masterCB.setName("chanceforGMfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforGBfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && !pilot.getSkills().has(PilotSkill.GunneryBallisticSkillID)) {
                masterCB = new JCheckBox("Gunnery Ballistic");
                masterCB.setName("chanceforGBfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforGLfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && !pilot.getSkills().has(PilotSkill.GunneryLaserSkillID)) {
                masterCB = new JCheckBox("Gunnery Laser");
                masterCB.setName("chanceforGLfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforTNfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && !pilot.getSkills().has(PilotSkill.TraitID)) {
                masterCB = new JCheckBox("Trait");
                masterCB.setName("chanceforTNfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforEIfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && !pilot.getSkills().has(PilotSkill.EnhancedInterfaceID)) {
                masterCB = new JCheckBox("Enhanced Interface");
                masterCB.setName("chanceforEIfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforGTfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && !pilot.getSkills().has(PilotSkill.GiftedID)) {
                masterCB = new JCheckBox("Gifted");
                masterCB.setName("chanceforGTfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforQSfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && !pilot.getSkills().has(PilotSkill.QuickStudyID)) {
                masterCB = new JCheckBox("Quick Study");
                masterCB.setName("chanceforQSfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforMTfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && !pilot.getSkills().has(PilotSkill.MedTechID)) {
                masterCB = new JCheckBox("Med Tech");
                masterCB.setName("chanceforMTfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforVDNIfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && !pilot.getSkills().has(PilotSkill.VDNIID)) {
                masterCB = new JCheckBox("VDNI");
                masterCB.setName("chanceforVDNIfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforBVDNIfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && !pilot.getSkills().has(PilotSkill.BufferedVDNIID)) {
                masterCB = new JCheckBox("Buffered VDNI");
                masterCB.setName("chanceforBVDNIfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }

            if (Integer.parseInt(mwclient.getServerConfigs("chanceforPSfor" + Unit.getTypeClassDesc(playerUnit.getType()))) > 0 && !pilot.getSkills().has(PilotSkill.PainShuntID)) {
                masterCB = new JCheckBox("Pain Shunt");
                masterCB.setName("chanceforPSfor" + Unit.getTypeClassDesc(playerUnit.getType()));
                masterCB.addActionListener(this);
                MasterPanel.add(masterCB);

            }
        }
    }

    public void keyPressed(KeyEvent arg0) {
    }

    public void keyReleased(KeyEvent arg0) {
    }

    public void keyTyped(KeyEvent arg0) {

        if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
            super.dispose();
        } else if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
            sendPromoteCommands();
        }

    }

    private void calculateExpCost() {

        int cost = 0;
        int gun = pilot.getGunnery();
        int piloting = pilot.getPiloting();
        if (pilot.getSkills().has(PilotSkill.NaturalAptitudeGunnerySkillID)) {
            gun++;
        }

        if (pilot.getSkills().has(PilotSkill.NaturalAptitudePilotingSkillID)) {
            piloting++;
        }

        for (Object object : MasterPanel.getComponents()) {
            int pilotCost = 0;
            if (object instanceof JCheckBox) {
                JCheckBox checkBox = (JCheckBox) object;

                if (checkBox.isSelected()) {

                    if (checkBox.getName().equalsIgnoreCase("gunnery") || checkBox.getName().equalsIgnoreCase("piloting")) {
                        int totalSkill = Math.min(9, gun + piloting);
                        pilotCost = Integer.parseInt(mwclient.getServerConfigs("BaseRollToLevel"));
                        pilotCost *= Integer.parseInt(mwclient.getServerConfigs("MultiplierPerPreviousLevel"));
                        pilotCost *= 10 - totalSkill;

                    } else if (checkBox.getName().startsWith("chanceforATfor")) {
                        if (pilot.getSkills().has(PilotSkill.AstechSkillID)) {
                            int level = pilot.getSkills().getPilotSkill(PilotSkill.AstechSkillID).getLevel();

                            pilotCost = Integer.parseInt(mwclient.getServerConfigs(checkBox.getName()));
                            pilotCost *= level + 2;
                        } else {
                            pilotCost = Integer.parseInt(mwclient.getServerConfigs(checkBox.getName()));
                        }
                    } else if (checkBox.getName().startsWith("chanceforEDfor")) {
                        if (pilot.getSkills().has(PilotSkill.EdgeSkillID)) {
                            int level = pilot.getSkills().getPilotSkill(PilotSkill.EdgeSkillID).getLevel();

                            pilotCost = Integer.parseInt(mwclient.getServerConfigs(checkBox.getName()));
                            pilotCost *= level + 1;
                        } else {
                            pilotCost = Integer.parseInt(mwclient.getServerConfigs(checkBox.getName()));
                        }
                    } else {
                        pilotCost = Integer.parseInt(mwclient.getServerConfigs(checkBox.getName()));
                    }

                    if (demoting) {
                        pilotCost = (int) Math.round(((double) pilotCost) * Double.parseDouble(mwclient.getServerConfigs("PilotUpgradeSellBackPercent")));
                    }
                    cost += pilotCost;
                }
            }
        }

        if (pilot.getSkills().has(PilotSkill.GiftedID) && !demoting) {
            cost *= (1 - Double.parseDouble(mwclient.getServerConfigs("GiftedPercent")));
        }

        expCost.setText(Integer.toString(cost));
    }

    private void sendPromoteCommands() {

        String baseCommand = GameHost.CAMPAIGN_PREFIX + "c promotepilot#" + playerUnit.getId() + "#";

        if (demoting) {
            baseCommand = GameHost.CAMPAIGN_PREFIX + "c demotepilot#" + playerUnit.getId() + "#";
        }

        for (Object object : MasterPanel.getComponents()) {
            if (object instanceof JCheckBox) {
                JCheckBox checkBox = (JCheckBox) object;

                if (checkBox.isSelected()) {
                    String cmd = checkBox.getName();

                    if (cmd.startsWith("chancefor")) {
                        int startPos = "chancefor".length();
                        cmd = cmd.substring(startPos, cmd.indexOf("for", startPos));
                    }
                    mwclient.sendChat(baseCommand + cmd);
                }
            }
        }

    }
}// end BulkRepairDialog.java
