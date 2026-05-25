/*
 * MekWars - Copyright (C) 2026
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

package mekwars.common.campaign;

import mekwars.common.Unit;
import mekwars.common.campaign.pilot.Pilot;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PersonalPilotQueues {
    private static final int WEIGHT_CLASS_COUNT = Unit.ASSAULT + 1;

    private List<Deque<Pilot>> mekPilots = new ArrayList<Deque<Pilot>>();
    private List<Deque<Pilot>> protoPilots = new ArrayList<Deque<Pilot>>();
    private List<Deque<Pilot>> aeroPilots = new ArrayList<Deque<Pilot>>();

    /**
     * Simple no-paramater constructor that creates the list-holding vectors and populates the
     * weightclasses. LIGHTONLY values for infantry and vehicles are not checked, and Lists are
     * created for all types/weightclasses. This ensures that a null is never returned by a
     * getPilotQueue() call.
     */
    public PersonalPilotQueues() {
        flushQueue();
    }

    /**
     * Method that returns a particular class/size queue. Used throughout the client code to fecth
     * queue, which are then iterated in order to draw menus, dialog boxes, etc.
     *
     * <p>Because these queues are always created in the constructor, they will never be null, even
     * if a LIGHTONLY option for vehs or infantry is enabled.
     */
    public Deque<Pilot> getPilotQueue(int unitType, int weightClass) {
        return this.getUnitTypeQueue(unitType).get(weightClass);
    }

    /** Obliterate all queued pilots. Whatever calls this should send a PL|PPQ to the player. */
    public synchronized void flushQueue() {
        mekPilots.clear();
        protoPilots.clear();
        aeroPilots.clear();
        for (int i = Unit.LIGHT; i <= Unit.ASSAULT; i++) {
            mekPilots.add(i, new ArrayDeque<Pilot>());
            protoPilots.add(i, new ArrayDeque<Pilot>());
            aeroPilots.add(i, new ArrayDeque<Pilot>());
        }
    }

    /**
     * Rather than if/else'ing meks and protos throughout the other methods of the class, use a
     * private get method which returns mek or proto as needed and then work on the vector without
     * regard to type.
     */
    protected List<Deque<Pilot>> getUnitTypeQueue(int typeToGet) {
        if (typeToGet == Unit.PROTOMEK) return protoPilots;
        if (typeToGet == Unit.AERO) return aeroPilots;
        return mekPilots;
    }

    private List<Deque<Pilot>> createWeightClassQueues() {
        return IntStream.range(0, WEIGHT_CLASS_COUNT)
                .mapToObj(i -> new ArrayDeque<Pilot>())
                .collect(Collectors.toList());
    }
}
