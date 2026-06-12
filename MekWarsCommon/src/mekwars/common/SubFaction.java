/*
 * MekWars - Copyright (C) 2007 
 * 
 * Original author - jtighe (torren@users.sourceforge.net)
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
package mekwars.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostPersist;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

@Entity
public class SubFaction {
    private static final Logger LOGGER = LogManager.getLogger(SubFaction.class);

    private String name;

    @OneToMany
    @JoinColumn(name = "subfaction_id")
    private List<Player> players;

    @ManyToOne
    @JoinColumn(name = "house_id")
    private House owner;
    private int accessLevel = 0;
    private int minElo = 0;
    private int minExp = 0;

    @ElementCollection
    @CollectionTable(name = "subfaction_settings", joinColumns = @JoinColumn(name = "subfaction_id"))
    @MapKeyColumn(name = "key")
    @Column(name = "value")
	private Map<String, String> settings;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

	public SubFaction() {
	}
	
	public SubFaction(String name){
        this();
        this.name = name;
	}

	public SubFaction(String name, int accessLevel){
        this(name);
        this.accessLevel = accessLevel;
	}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public House getOwner() {
        return owner;
    }

    public void setOwner(House owner) {
        this.owner = owner;
    }

    public int getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(int accessLevel) {
        this.accessLevel = accessLevel;
    }

    public int getMinElo() {
        return minElo;
    }

    public void setMinElo(int minElo) {
        this.minElo = minElo;
    }

    public int getMinExp() {
        return minExp;
    }

    public void setMinExp(int minExp) {
        this.minExp = minExp;
    }
	
    @PostPersist
	public void getDefault(){
		for (int type = 0; type < Unit.MAXBUILD; type++) {
		    for (int weight = 0; weight <= Unit.ASSAULT; weight++) {
                String poststring = Unit.getWeightClassDesc(weight) + Unit.getTypeClassDesc(type); 

                settings.put("CanBuyNew" + poststring, "true");
                settings.put("CanBuyUsed" + poststring, "true");
		    }
		}
	}
	
	public String getConfig(String key) {
        switch (key) {
            case "Name":
                return name;
            case "AccessLevel":
                return String.valueOf(accessLevel);
            case "MinELO":
                return String.valueOf(minElo);
            case "MinExp":
                return String.valueOf(minExp);
        }

        String value = settings.get(key);
		if (value == null) {
            LOGGER.error("Unable to find subfaction config: {}", key);
			return "-1";
		}
		return value;
	}
	
	public void setConfig(String key, String value) {
        switch (key) {
            case "Name":
                setName(value);
            case "AccessLevel":
                setAccessLevel(Integer.parseInt(value));
            case "MinELO":
                setMinElo(Integer.parseInt(value));
            case "MinExp":
                setMinExp(Integer.parseInt(value));
            default:
                settings.put(key, value);
        }
	}
	
	@Override
    public String toString() {
		StringBuilder result = new StringBuilder();
		
		if (settings.isEmpty()) {
			return "# #";
        }

        appendSetting(result, "Name", name);
        appendSetting(result, "AccessLevel", String.valueOf(accessLevel));
        appendSetting(result, "MinELO", String.valueOf(minElo));
        appendSetting(result, "MinExp", String.valueOf(minExp));
        settings.forEach((key, value) -> appendSetting(result, key, value));
		return result.toString();
	}

	public void fromString(String settings) {
		StringTokenizer propertyList = new StringTokenizer(settings,"#");
		
		while (propertyList.hasMoreElements()) {
			String key = propertyList.nextToken();
			
			if (!propertyList.hasMoreElements()) {
				return;
            }
			
			String value = propertyList.nextToken();
			setConfig(key, value);
		}
	}

    private void appendSetting(StringBuilder sb, String key, String value) {
        sb.append(key).append("#").append(value).append("#");
    }
}
