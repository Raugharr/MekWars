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

package mekwars.client.gui;

import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import megamek.common.Entity;
import megamek.common.MechView;
import megamek.common.TechConstants;

public class MWUnitDisplay extends JPanel {
    private Entity entity;
    private ResourceBundle resourceMap;

    public MWUnitDisplay(Entity entity) {
        super();
        this.entity = entity;
        resourceMap = ResourceBundle.getBundle("mekwars.UnitViewDisplay", Locale.US);
        try {
            initComponents();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    protected void initComponents() {
        // FIXME: Use SuiteOptions for selecting the Locale when added.
        setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = null;

        JPanel generalInfo = new JPanel();
        generalInfo.setLayout(new GridBagLayout());
        generalInfo.setBorder(BorderFactory.createTitledBorder(entity.getShortNameRaw()));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        add(generalInfo, gridBagConstraints);

        String techBaseValue = TechConstants.getLevelDisplayableName(entity.getTechLevel());
        String battleValue = Integer.toString(entity.calculateBattleValue(true, true));
        addRow(0, "techBase", techBaseValue, generalInfo);
        addRow(1, "tonnage", Double.toString(entity.getWeight()), generalInfo);
        addRow(2, "battleValue", battleValue, generalInfo);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.weightx = 0.0;

        MechView mechView = new MechView(entity, false, true);
        JTextPane mechReadout = new JTextPane();
        mechReadout.setContentType("text/html");
        mechReadout.setEditable(false);
        mechReadout.setFont(Font.decode("Monospaced-Plan-12"));
        mechReadout.setText(
                "<div style='font: 12pt monospaced'>"
                        + mechView.getMechReadoutBasic()
                        + "<br>"
                        + mechView.getMechReadoutLoadout()
                        + "</div>");
        String technicalReadoutResource = resourceMap.getString("technicalReadoutLabel.text");
        mechReadout.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(technicalReadoutResource),
                        BorderFactory.createEmptyBorder(0, 2, 2, 2)));
        add(mechReadout, gridBagConstraints);
        // FIXME: Check for EnableQuirks config and add quirks.
    }

    protected void addRow(int gridy, String name, String value) {
        addRow(gridy, name, value, this);
    }

    protected void addRow(int gridy, String name, String value, Container parent) {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        JLabel label = new JLabel();
        label.setName(name + "Label");
        label.setText(resourceMap.getString(name + "Label.text"));
        parent.add(label, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        JLabel text = new JLabel();
        text.setName(name + "Text");
        text.setText(value);
        parent.add(text, gridBagConstraints);
    }

    // @Override
    // public Dimension getPreferredScrollableViewportSize() {
    //     return super.getPreferredSize();
    // }

    // @Override
    // public int getScrollableUnitIncrement(final Rectangle visibleRect, final int orrientation,
    //         final int direction) {
    //     return 16;
    // }

    // @Override int getScrollableBlockIncrement(final Rectangle visible, final int orientation,
    //     final int direction) {
    //     return (SwingConstants.VERTICAL == orientation) ? visible.height : visible.width;
    // }

    // @Override boolean getScrollableTracksViewportWidth() {
    //     return true;
    // }

    // @Override boolean getScrollableTracksViewportHeight() {
    //     return false;
    // }
}
