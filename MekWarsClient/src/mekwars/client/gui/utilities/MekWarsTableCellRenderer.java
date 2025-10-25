/*
 * MekWars - Copyright (C) 2025
 *
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet) Original author Helge Richter (McWizard)
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */

package mekwars.client.gui.utilities;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

public class MekWarsTableCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
        setupTigerStripes(this, table, row);
        return this;
    }

    public static void setupTigerStripes(Component component, JTable table, int row) {
        Color background = table.getBackground();

        if (row % 2 != 0) {
            Color alternateColor = UIManager.getColor("Table.alternateRowColor");
            if (alternateColor == null) {
                alternateColor = UIManager.getColor("controlHighlight");
            }
            if (alternateColor != null) {
                background = alternateColor;
            }
        }
        component.setForeground(table.getForeground());
        component.setBackground(background);
    }
}
