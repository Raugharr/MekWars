/*
 * MekWars - Copyright (C) 2004 
 * 
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet)
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

/*
 * Created:       03/25/05
 * Last refactor: 01/12/06
 */
package mekwars.server.campaign;

import java.io.Serializable;
import java.util.Deque;
import java.util.Iterator;
import java.util.StringTokenizer;

import mekwars.common.Unit;
import mekwars.common.campaign.pilot.Pilot;
import mekwars.common.campaign.PersonalPilotQueues;
import mekwars.common.util.TokenReader;
import mekwars.server.campaign.pilot.SPilot;
import mekwars.server.campaign.util.SerializedMessage;

/**
 * @author Torren (Jason Tighe) Server-side holder of Personal Pilot Queue information. The queue is a collection of pilots, managed by a player, which may be moved between eligible units (restricted by type and weightclass).
 */

public class SPersonalPilotQueues extends PersonalPilotQueues implements Serializable {
    /**
     * Used if the pilot type has not been set for the pilot yet.
     * 
     * @param p
     * @param type
     * @param weightClass
     */
    public void addPilot(Pilot p, int type, int weightClass) {
        p.setUnitType(type);
        addPilot(p, weightClass);
    }
    
    /**
     * Add a pilot to the queue. Many different events can trigger an addition, including game resolution, sale via market, the hiring/purchase of a new pilot, and more. The type of unit that the pilot may use is embedded within the Pilot/SPilot that is passed as a param; however, the weight class is not and must be set here.
     * 
     * @param p -
     *            the actual pilot to add.
     * @param weight -
     *            weightclass of unit the pilot may use
     */
    public void addPilot(Pilot p, int weightClass) {
        /*
         * On the off chance a VACANT pilot is somehow added to the player's queue, kill it off.
         */
        if (p.getName().trim().equalsIgnoreCase("Vacant")) {
            p = null;// some how a bad pilot go through the checks.
            return;
        }
        // add the pilot to the correct weightclass list.
        this.getUnitTypeQueue(p.getUnitType()).get(weightClass).add(p);
    }

    /**
     * Check the specified queue's size and send a warning to the named player if they've exceeded the queue cap. Have to do this in a stand alone method b/c the PPQ has no knowledge of it's owning player and must be sent a name.
     */
    public void checkQueueAndWarn(String playerName, int unitType, int weightClass) {
        int size = this.getPilotQueue(unitType, weightClass).size();
        if (size > CampaignMain.cm.getIntegerConfig("MaxAllowedPilotsInQueueToBuyFromHouse"))
            CampaignMain.cm.toUser("WARNING: You have more " + Unit.getWeightClassDesc(weightClass) + " " + Unit.getTypeClassDesc(unitType) + " pilots than allowed. HQ will randomly reassign some of them, if you do not.", playerName);
    }

    /**
     * Remove a given pilot from the player's personal queue (as defined by type, weight and position in the LList) and return him to the calling class.
     */
    public Pilot getPilot(int unitType, int weightClass, int position) {
        try {
            Deque<Pilot> list = this.getUnitTypeQueue(unitType).get(weightClass);
            Iterator<Pilot> iterator = list.iterator();
            if (position >= list.size()) {
                return null;
            }
            for (int i = 0; i < position; i++) {
                iterator.next();
            }
            Pilot pilot = iterator.next();
            iterator.remove();
            return pilot;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Convert the pilot queue information into a data string. Although toClient doesn't change this string-out directly, it is needed by SPilot's toString equivalent (SPilot.toFileFormat()). WARNING: This format MAY NOT BE CHANGED. Any restructuring of this data would break servers' player saves.
     * 
     * @return - a data string.
     */
    public String toString(boolean toClient) {
        SerializedMessage result = new SerializedMessage("$");

        // meks first
        for (int weightClass = Unit.LIGHT; weightClass <= Unit.ASSAULT; weightClass++) {
            Deque<Pilot> currList = this.getPilotQueue(Unit.MEK, weightClass);
            result.append(currList.size());
            for (Pilot pilot : currList) {
                result.append(((SPilot) pilot).toFileFormat("#", toClient));
            }
        }

        // protos second
        for (int weightClass = Unit.LIGHT; weightClass <= Unit.ASSAULT; weightClass++) {
            Deque<Pilot> currList = this.getPilotQueue(Unit.PROTOMEK, weightClass);
            result.append(currList.size());
            for (Pilot pilot : currList) {
                result.append(((SPilot) pilot).toFileFormat("#", toClient));
            }
        }

        // aeros third
        for (int weightClass = Unit.LIGHT; weightClass <= Unit.ASSAULT; weightClass++) {
            Deque<Pilot> currList = this.getPilotQueue(Unit.AERO, weightClass);
            result.append(currList.size());
            for (Pilot pilot : currList) {
                result.append(((SPilot) pilot).toFileFormat("#", toClient));
            }
        }

        return result.toString();

        /*
         * OLD SAVE STYLE PRESERVED FOR OUTPUT FORMATTING REFERENCE
         */
        /*
         * for (int type = 0; type <= ppProto; type++){ for ( int weight = 0; weight <= SUnit.ASSAULT; weight++){ Queue list = getPilotQueue(type,weight); result.append(list.size()); result.append("$"); for ( int count = 0; count < list.size(); count++ ){ result.append(((SPilot)list.get(count)).toFileFormat("#",toClient)); result.append("$"); } } } return result.toString();
         */
    }

    public void fromString(String buffer, String delimiter) {
        StringTokenizer mainTokenizer = new StringTokenizer(buffer, delimiter);
        int capSize = CampaignMain.cm.getIntegerConfig("MaxAllowedPilotsInQueueToBuyFromHouse");

        // loop once to read in meks (light -> assault lists)
        for (int weightClass = Unit.LIGHT; weightClass <= Unit.ASSAULT; weightClass++) {
            int listSize = TokenReader.readInt(mainTokenizer);
            for (int count = 0; count < listSize; count++) {
                SPilot filePilot = new SPilot();
                filePilot.fromFileFormat(TokenReader.readString(mainTokenizer), "#");
                this.addPilot(filePilot, Unit.MEK, weightClass);
            }
            while (this.getPilotQueue(Unit.MEK, weightClass).size() > capSize) {
                this.getPilot(Unit.MEK, weightClass, CampaignMain.cm.getRandomNumber(this.getPilotQueue(Unit.MEK, weightClass).size()));
            }
        }

        // a second loop will read in protos (light -> assault lists)
        for (int weightClass = Unit.LIGHT; weightClass <= Unit.ASSAULT; weightClass++) {
            int listSize = TokenReader.readInt(mainTokenizer);
            for (int count = 0; count < listSize; count++) {
                SPilot filePilot = new SPilot();
                filePilot.fromFileFormat(TokenReader.readString(mainTokenizer), "#");
                this.addPilot(filePilot, Unit.PROTOMEK, weightClass);
            }
            while (this.getPilotQueue(Unit.PROTOMEK, weightClass).size() > capSize) {
                this.getPilot(Unit.PROTOMEK, weightClass, CampaignMain.cm.getRandomNumber(this.getPilotQueue(Unit.PROTOMEK, weightClass).size()));
            }
        }

        // a third loop will read in aeros (light -> assault lists)
        for (int weightClass = Unit.LIGHT; weightClass <= Unit.ASSAULT; weightClass++) {
            int listSize = TokenReader.readInt(mainTokenizer);
            for (int count = 0; count < listSize; count++) {
                SPilot filePilot = new SPilot();
                filePilot.fromFileFormat(TokenReader.readString(mainTokenizer), "#");
                this.addPilot(filePilot, Unit.AERO, weightClass);
            }
            while (this.getPilotQueue(Unit.AERO, weightClass).size() > capSize) {
                this.getPilot(Unit.AERO, weightClass, CampaignMain.cm.getRandomNumber(this.getPilotQueue(Unit.AERO, weightClass).size()));
            }
        }
    /*
     * OLD PASRING PRESERVED FOR REFERENCE
     */
    /*
     * StringTokenizer ST = new StringTokenizer(buffer,delimiter); for (int type = 0; type <= ppProto; type++ ){ for ( int weight = 0; weight <= SUnit.ASSAULT; weight++ ){ int size = Integer.parseInt(ST.nextToken()); for( int count = 0 ; count < size; count++ ){ SPilot pilot = new SPilot(); pilot.fromFileFormat(ST.nextToken(),"#"); this.addPilot(type,weight,pilot); } } }
     */
    }

}// end SPersonalPilotQueues.java
