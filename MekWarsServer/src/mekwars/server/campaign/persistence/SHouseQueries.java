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

package mekwars.server.campaign.persistence;

import mekwars.common.util.ComponentToCritsConverter;
import mekwars.server.campaign.SPlayer;

import org.hibernate.Session;
import org.hibernate.annotations.processing.HQL;

import java.util.List;

public interface SHouseQueries {
    Session getSession();

    @HQL("FROM ComponentToCritsConverter WHERE criticalName = \"ALL\"")
    ComponentToCritsConverter getDefaultComponentToCritsConverter();

    @HQL("FROM SPlayer p WHERE p.myHouse.id = :houseId and p.name = :name")
    List<SPlayer> findPlayerInHouse(int houseId, String name);
}
