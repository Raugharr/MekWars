/*
 * MekWars - Copyright (C) 2026
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

package mekwars.common.campaign;

import jakarta.persistence.Embeddable;

/**
 * Tracks the number of production points a {@link House} has for a specific {@link Unit unit's}
 * weight and type.
 */
@Embeddable
public class Component {
    private int unitType;
    private int unitWeight;
    private int productionPoints = 0;

    public Component(int unitType, int unitWeight) {
        this.unitType = unitType;
        this.unitWeight = unitWeight;
    }

    public int getUnitType() {
        return unitType;
    }

    public int getUnitWeight() {
        return unitWeight;
    }

    public int getProductionPoints() {
        return productionPoints;
    }

    public void setProductionPoints(int productionPoints) {
        this.productionPoints = productionPoints;
    }

    public void addAmount(int delta) {
        this.productionPoints += delta;
    }
}
