/*
 * MekWars - Copyright (C) 2025
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

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import mekwars.client.gui.utilities.MWTableCellRenderer;

public class MWTable extends JTable {
    @Override
    public TableCellRenderer getDefaultRenderer(Class<?> columnClass) {
        return new MWTableCellRenderer();
    }
}
