/*
 * MekWars - Copyright (C) 2025
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

package mekwars.client.gui.icons;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import mekwars.client.campaign.CArmy;
import mekwars.client.campaign.CUnit;
import mekwars.client.gui.icons.unitstatus.UnitStatus;

public class StatusIconsTable implements Icon {
    private int rows;
    private int columns;
    private int columnHeight;
    private int columnWidth;
    private UnitStatus[] status;
    private UnitStatusIcon[] statusIconImage;
    private int activeStatusCount;
    private CUnit unit;
    private CArmy army;
    private ResourceBundle resourceBundle;

    public StatusIconsTable(ResourceBundle bundle, int columns, int columnWidth, int columnHeight,
            UnitStatus... status) {

        this.status = status;
        this.columnWidth = columnWidth;
        this.columnHeight = columnHeight;
        this.columns = columns;
        this.rows = countRows(status.length);
        this.statusIconImage = new UnitStatusIcon[status.length];
        this.resourceBundle = bundle;
        
        clear();
    }

    public StatusIconsTable(CUnit unit, CArmy army, ResourceBundle bundle, int columns,
            int columnWidth, int columnHeight, UnitStatus... status) {

        this(bundle, columns, columnWidth, columnHeight, status);
        this.unit = unit;
        this.army = army;
    }

    @Override
    public int getIconWidth() {
        if (activeStatusCount == 0 || unit == null) {
            return 0;
        }
        return countRows(activeStatusCount) * columnWidth;
    }

    @Override
    public int getIconHeight() {
        if (activeStatusCount == 0 || unit == null) {
            return 0;
        }
        return Math.min(activeStatusCount, columns) * columnHeight;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        int currX = x;
        int currY = y;
        int count = 0;

        if (unit == null) {
            return;
        }

        for (int i = 0; i < statusIconImage.length; ++i) {
            ImageIcon imageIcon = statusIconImage[i];

            if (imageIcon != null) {
                imageIcon.paintIcon(c, g, currX, currY);
                count++;
               if (count % columns == 0) {
                    currY = y;
                    currX += columnWidth;
                    count = 0;
                } else {
                    currY += columnHeight;
                }
            }
        }
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public int getColumnWidth() {
        return columnWidth;
    }

    public int getColumnHeight() {
        return columnHeight;
    }

    public void setUnit(CUnit unit, CArmy army) {
        this.unit = unit;
        this.army = army;
        calculateStatus();
    }

    public void clear() {
        for (int i = 0; i < statusIconImage.length; ++i) {
            statusIconImage[i] = null;
        }
        activeStatusCount = 0;
    }

    /**
     * Calculate which {@link ImageIcon} if any to display for each status in the StatusIconsTable.
     */
    public void calculateStatus() {
        activeStatusCount = 0;

        if (unit == null) {
            return;
        }

        for (int i = 0; i < status.length; ++i) {
            statusIconImage[i] = status[i].apply(unit, army);

            if (statusIconImage[i] != null) {
                activeStatusCount++;
            }
        }
    }

    public String getToolTipText(MouseEvent e, Rectangle bounds) {
        if (activeStatusCount == 0) {
            return null;
        }
        Point location = bounds.getLocation();
        Point point = new Point(e.getPoint());
        point.translate(
            -(int) Math.floor(location.getX()),
            -(int) Math.floor(location.getY())
        );
        int row = (int) Math.floor(point.getX()) / columnWidth;
        int column = (int) Math.floor(point.getY()) / columnHeight;

        if (row * rows + column >= statusIconImage.length) {
            return null;
        }
        int count = row * rows + column + 1;
        UnitStatusIcon statusIcon = null;

        for (int i = 0; i < statusIconImage.length && count > 0; ++i) {
            if (statusIconImage[i] != null) {
                statusIcon = statusIconImage[i];
                --count;
            }
        }
        return statusIcon.getToolTipText(resourceBundle);
    }

    protected int countRows(int count) {
        return Math.max((count / columns) + (count % columns > 0 ? 1 : 0), 1);
    }
}
