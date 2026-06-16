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

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderBy;

import mekwars.common.Unit;
import mekwars.common.util.TokenReader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Interface for storing {@link Component components} based on a {@link Unit unit's} weight and
 * type.
 */
@Embeddable
public class ComponentList implements Iterable<Component> {
    private final int TYPE_SIZE = Unit.AERO - Unit.MEK + 1;
    private final int WEIGHT_SIZE = Unit.ASSAULT - Unit.LIGHT + 1;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "house_components", joinColumns = @JoinColumn(name = "house_id"))
    @OrderBy("unitType ASC, unitWeight ASC")
    private List<Component> components = new ArrayList<>();

    public class ComponentListIterator implements Iterator<Component> {
        private int unitType = 0;
        private int unitWeight = 0;
        private ComponentList componentList;

        public ComponentListIterator(ComponentList componentList) {
            this.componentList = componentList;
        }

        @Override
        public boolean hasNext() {
            return unitType < TYPE_SIZE && unitWeight < WEIGHT_SIZE;
        }

        @Override
        public Component next() {
            Component nextComponent = componentList.get(unitType, unitWeight);

            ++unitWeight;
            if (unitWeight >= WEIGHT_SIZE) {
                ++unitType;
                unitWeight = 0;
            }
            return nextComponent;
        }
    }

    public ComponentList() {}

    public void initialize() {
        for (int type = 0; type < TYPE_SIZE; ++type) {
            for (int weight = 0; weight < WEIGHT_SIZE; ++weight) {
                components.add(new Component(type, weight));
            }
        }
    }

    /**
     * @see java.lang.Iterable
     */
    @Override
    public ComponentListIterator iterator() {
        return new ComponentListIterator(this);
    }

    /**
     * Returns the component that has the given type and weight.
     *
     * @return The Component that has the given type and weight.
     * @throws IndexOutOfBoundsException When unitType or unitWeight is invalid.
     */
    public Component get(int unitType, int unitWeight) {
        if (unitType < 0 || unitType >= TYPE_SIZE) {
            throw new IndexOutOfBoundsException("Invalid unitType: " + unitType);
        }

        if (unitWeight < 0 || unitWeight >= WEIGHT_SIZE) {
            throw new IndexOutOfBoundsException("Invalid unitWeight " + unitWeight);
        }
        return components.get(getIndex(unitType, unitWeight));
    }

    public void fromString(StringTokenizer tokenizer) {
        for (int type = 0; type < TYPE_SIZE; ++type) {
            for (int weight = 0; weight < WEIGHT_SIZE; ++weight) {
                get(type, weight).setProductionPoints(TokenReader.readInt(tokenizer));
            }
        }
    }

    private int getIndex(int unitType, int unitWeight) {
        return unitType * WEIGHT_SIZE + unitWeight;
    }
}
