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

import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.ResourceBundle;
import mekwars.client.common.ServerInfo;

public class ServerModel extends MWTableModel<ServerInfo> {
    private static final int COLUMN_NAME = 0;
    private static final int COLUMN_PLAYER_COUNT = 1;
    private static final int COLUMN_VERSION = 2;
    private static final int COLUMN_COUNT = 3;

    private String[] columnNames;

    public ServerModel(Locale locale) {
        super();
        ResourceBundle resourceMap = ResourceBundle.getBundle("mekwars.ServerModel", locale);
        columnNames = new String[COLUMN_COUNT];
        columnNames[COLUMN_NAME] = resourceMap.getString("name.text");
        columnNames[COLUMN_PLAYER_COUNT] = resourceMap.getString("players.text");
        columnNames[COLUMN_VERSION] = resourceMap.getString("version.text");
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (row < 0 || row >= getRowCount()) {
            return "";
        }
        ServerInfo serverInfo = getRow(row);
        
        switch (col) {
            case COLUMN_NAME:
                return serverInfo.getName();
            case COLUMN_PLAYER_COUNT:
                return serverInfo.getPlayerCount();
            case COLUMN_VERSION:
                return serverInfo.getVersion().toString();
            default:
                throw new IllegalArgumentException("Invalid column index");
        }
    }

    @Override
    public int getColumnCount() {
        return COLUMN_COUNT;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    /*
     * Sets the {@link ServerModel}'s items to newList. Ensures that every new element that exists
     * in both newList and the previous item list does not fire a
     * {@link AbstractTableModel#fireTableRowsDeleted}.
     */
    public void setItems(List<ServerInfo> newList) {
        boolean found = false;
        int index = 0;
        for (ServerInfo newListItem : newList) {
            for (int i = 0; i < getRowCount(); i++) {
                ServerInfo item = getRow(i);
                if (newListItem.compareTo(item) == 0) {
                    found = true;
                    index = i;
                    break;
                }
            }
            if (found == false) {
                addRow(index, newListItem);
            }
            found = false;
            index = 0;
        }

        found = false;
        index = 0;
        for (ListIterator<ServerInfo> iterator = getItems(); iterator.hasNext();) {
            ServerInfo item = iterator.next();

            for (int i = 0; i < newList.size(); i++) {
                ServerInfo newListItem = newList.get(i);
                if (newListItem.compareTo(item) == 0) {
                    found = true;
                    index = i;
                    break;
                }
            }
            if (found == false) {
                removeRow(index);
            }
            found = false;
            index = 0;
        }
    }
}
