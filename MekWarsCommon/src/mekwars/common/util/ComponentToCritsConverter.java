/*
 * MekWars - Copyright (C) 2008 
 * 
 * Original author - Torren (torren@users.sourceforge.net)
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

/**
 * 
 * @author Torren (Jason Tighe) 3.9.08 
 * 
 */

package mekwars.common.util;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import mekwars.common.Unit;

@Entity
public class ComponentToCritsConverter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int minCriticalLevel = 10;
    private int componentUsedType = Unit.MEK;
    private int componentUsedWeight = Unit.LIGHT;
    private String criticalName = "All";

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * 
     * @param int level
     */
    public void setMinCriticalLevel(int level) {
        this.minCriticalLevel = level;
    }
    
    /**
     * 
     * @return int
     */
    public int getMinCriticalLevel() {
        return this.minCriticalLevel;
    }
    
    /**
     * 
     * @param int type
     */
    public void setComponentUsedType(int type) {
        this.componentUsedType = type;
    }

    /**
     * 
     * @return int
     */
    public int getComponentUsedType() {
        return this.componentUsedType;
    }
    
    /**
     * 
     * @param int weight
     */
    public void setComponentUsedWeight(int weight) {
        this.componentUsedWeight = weight;
    }
    
    /**
     * 
     * @return int
     */
    public int getComponentUsedWeight() {
        return this.componentUsedWeight;
    }
    
    /**
     * 
     * @param String crit
     */
    public void setCriticalName(String crit) {
        this.criticalName = crit;
    }
    
    /**
     * 
     * @return string
     */
    public String getCriticalName() {
        return this.criticalName;
    }

    public String toString() {
        return this.toString("|");
    }
    
    public String toString(String token) {
        StringBuffer results = new StringBuffer();
        
        results.append(criticalName);
        results.append(" ");
        results.append(token);
        results.append(minCriticalLevel);
        results.append(token);
        results.append(componentUsedType);
        results.append(token);
        results.append(componentUsedWeight);
        results.append(token);
        
        return results.toString();
    }
    
}
