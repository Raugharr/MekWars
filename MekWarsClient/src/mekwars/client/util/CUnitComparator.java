/*
 *  MekWars - Copyright (C) 2004 
 * 
 *  original author - Nathan Morris (urgru@users.sourceforge.net)
 * 
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License as published by the Free 
 *  Software Foundation; either version 2 of the License, or (at your option) 
 *  any later version.
 * 
 *  This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 *  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 *  for more details.
 */
 
 package mekwars.client.util;
 
 import java.util.Comparator;

import mekwars.client.campaign.CUnit;

public class CUnitComparator implements Comparator<CUnit> {
 	/*
     * NOTE: This order must match order of
 	 * sort options in HangarSorters's SORT_CHOICES array
     */
 	public static final int HQSORT_NAME = 0;
 	public static final int HQSORT_BV = 1;
 	public static final int HQSORT_GUNNERY = 2;
 	public static final int HQSORT_ID = 3;
 	public static final int HQSORT_JUMPMP = 4;
 	public static final int HQSORT_WALKMP = 5;
 	public static final int HQSORT_PILOTKILLS = 6;
 	public static final int HQSORT_TYPE = 7;
 	public static final int HQSORT_WEIGHTCLASS = 8;
 	public static final int HQSORT_WEIGHTTONS = 9;
 	public static final int HQSORT_NONE = 10;
 	
 	private int sortOrder;
 	
 	public CUnitComparator(int sortOrder) {
 		this.sortOrder = sortOrder;
 	}
 	
    @Override
 	public int compare(CUnit left, CUnit right) {
 		switch (sortOrder) {
 			case HQSORT_NAME : //the name
				return left.getUnitFilename().compareTo(right.getUnitFilename());
 		
 			case HQSORT_BV : //self evident
 				Integer unit1BV = left.getBVForMatch();
 				Integer unit2BV = right.getBVForMatch();
 				return unit1BV.compareTo(unit2BV);
 				
 			case HQSORT_GUNNERY : //gunnery
 				Integer unit1Gunnery = left.getPilot().getGunnery();
 				Integer unit2Gunnery = right.getPilot().getGunnery();
 				return unit1Gunnery.compareTo(unit2Gunnery);
 				
 			case HQSORT_ID : //the unique unit ID
 				Integer unit1ID = left.getId();
 				Integer unit2ID = right.getId();
 				return unit1ID.compareTo(unit2ID);	
 				
 			case HQSORT_JUMPMP : //unit's jump movement
 				Integer unit1JMP = left.getEntity().getJumpMP();
 				Integer unit2JMP = right.getEntity().getJumpMP();
 				return unit1JMP.compareTo(unit2JMP);
 				
 			case HQSORT_WALKMP : //unit's jump movement
 				Integer unit1WMP = left.getEntity().getWalkMP();
 				Integer unit2WMP = right.getEntity().getWalkMP();
 				return unit1WMP.compareTo(unit2WMP);
 				
 			case HQSORT_PILOTKILLS : //Pilot's Kills
 				Integer unit1PK = left.getPilot().getKills();
 				Integer unit2PK = right.getPilot().getKills();
 				return unit1PK.compareTo(unit2PK);
 				
 			case HQSORT_TYPE : //type as in Mech, Veh, Inf, etc.
 				Integer unit1Type = left.getType();
 				Integer unit2Type = right.getType();
 				return unit1Type.compareTo(unit2Type);	
 				
 			case HQSORT_WEIGHTCLASS : //sort by general class
 				Integer unit1Class = left.getWeightClass();
 				Integer unit2Class = right.getWeightClass();
 				return unit1Class.compareTo(unit2Class);	 				
 				
 			case HQSORT_WEIGHTTONS : //sort by entity weight
 				Float unit1Mass = (float)left.getEntity().getWeight();
 				Float unit2Mass = (float)right.getEntity().getWeight();
 				return unit1Mass.compareTo(unit2Mass);
 			default :
 				return 0;
 		}
 	}
}
