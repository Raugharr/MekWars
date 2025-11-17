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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.swing.table.AbstractTableModel;

public abstract class MWTableModel<T> extends AbstractTableModel {
    private List<T> items = new ArrayList<T>();

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (items.isEmpty()) {
            return Object.class;
        }
        return getValueAt(0, columnIndex).getClass();
    }

    @Override
    public int getRowCount() {
        return items.size();
    }

    public void addItem(T item) {
        items.add(item);
    }

    public void clearItems() {
        items.clear();
    }

    public void removeRow(int index) {
        items.remove(index);
        fireTableRowsDeleted(index, index);
    }

    public void addRow(int index, T item) {
        items.add(index, item);
        fireTableRowsInserted(index, index);
    }

    public T getRow(int index) {
        return items.get(index);
    }

    public ListIterator<T> getItems() {
        return items.listIterator();
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }
}
