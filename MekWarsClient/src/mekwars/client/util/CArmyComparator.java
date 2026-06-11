/*
 *  MekWars - Copyright (C) 2007 
 * 
 *  original author - Nathan Morris (urgru@users.sourceforge.net)
 *  Change by Jason Tighe (torren@users.sourceforge.net)
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
 *  
 *  Sort used for Armies
 */
 
 package mekwars.client.util;
 
 import java.util.Comparator;

import mekwars.client.campaign.CArmy;

 public class CArmyComparator implements Comparator<CArmy> {
 	/*
     * NOTE: This order must match order of
 	 * sort options in ArmySorters's SORT_CHOICES array
     */
 	public static final int ARMYSORT_NAME = 0;
 	public static final int ARMYSORT_BV = 1;
 	public static final int ARMYSORT_ID = 2;
 	public static final int ARMYSORT_TONNAGE = 3;
 	public static final int ARMYSORT_AVGMPWALK = 4;
 	public static final int ARMYSORT_AVGMPJUMP = 5;
 	public static final int ARMYSORT_UNITS = 6;
 	public static final int ARMYSORT_NONE = 7;
 	
 	private int sortOrder;
 	
 	public CArmyComparator(int sortOrder) {
 		this.sortOrder = sortOrder;
 	}
 	
 	public int compare(CArmy left, CArmy right) {
 		switch (sortOrder) {
 		
 			case ARMYSORT_NAME : //the name
				return left.getName().compareTo(right.getName());
 		
 			case ARMYSORT_BV : //self evident
 				Integer army1BV = left.getBV();
 				Integer army2BV = right.getBV();
 				return army1BV.compareTo(army2BV);
 				
 			case ARMYSORT_ID : //the unique unit ID
 				Integer army1ID = left.getId();
 				Integer army2ID = right.getId();
 				return army1ID.compareTo(army2ID);	

 			case ARMYSORT_TONNAGE: //Total tonnage of the army
 				Double army1Ton = left.getTotalTonnage();
 				Double army2Ton = right.getTotalTonnage();
 				return army1Ton.compareTo(army2Ton);

 			case ARMYSORT_AVGMPWALK : //average walk MP for the army
 				Double army1MP = left.getAverageWalk();
 				Double army2MP = right.getAverageWalk();
 				return army1MP.compareTo(army2MP);	

 			case ARMYSORT_AVGMPJUMP : //average jump mp of the army
 				Double army1JP = left.getAverageJump();
 				Double army2JP = right.getAverageJump();
 				return army1JP.compareTo(army2JP);	

 			case ARMYSORT_UNITS:
 				Integer army1Size = left.getUnits().size();
 				Integer army2Size = right.getUnits().size();
 				return army1Size.compareTo(army2Size);
 				
 			default :
 				return 0;
 		}
 	}
}
