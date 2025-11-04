/*
 * MekWars - Copyright (C) 2018
 * 
 * Original author - Bob Eldred (spork@mekwars.org)  
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

package mekwars.hpgnet;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A class to deserialize HPGSubscriber objects.  We need a custom deserializer because
 * JSON doesn't know how to deal with the EvictingQueues used in the object
 *
 * @author Spork
 * @version 1.0
 * 
 */

public class HPGSubscriberDeserializer implements JsonDeserializer<HPGSubscriber> {
    private static final Logger logger = LogManager.getLogger(HPGSubscriberDeserializer.class);

    @Override
    public HPGSubscriber deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        final JsonObject obj = json.getAsJsonObject();
        final String name = (obj.get("name") == null || obj.get("name").isJsonNull()) ? "" : obj.get("name").getAsString();
        final String url = (obj.get("url") == null || obj.get("url").isJsonNull()) ? "" : obj.get("url").getAsString();
        final String desc = (obj.get("description") == null || obj.get("description").isJsonNull()) ? "" : obj.get("description").getAsString();
        final int maxPlayers = obj.get("maxPlayers") == null ? 0 : obj.get("maxPlayers").getAsInt();
        final int maxGames = obj.get("maxGames") == null ? 0 : obj.get("maxGames").getAsInt();
        final int port = obj.get("port") == null ? 0 : obj.get("port").getAsInt();
        final String ipAddress = (obj.get("ipAddress") == null || obj.get("ipAddress").isJsonNull()) ? "" : obj.get("ipAddress").getAsString();
        final String domain = (obj.get("domain") == null || obj.get("domain").isJsonNull()) ? "" : obj.get("domain").getAsString();
        final String version = (obj.get("MWVersion") == null || obj.get("MWVersion").isJsonNull()) ? "" : obj.get("MWVersion").getAsString();
        final String uuid = (obj.get("uuid") == null || obj.get("uuid").isJsonNull()) ? "" : obj.get("uuid").getAsString();
        final String password = (obj.get("password") == null || obj.get("password").isJsonNull()) ? "" : obj.get("password").getAsString();
        final boolean isLegacy = obj.get("isLegacy") == null ? true : obj.get("isLegacy").getAsBoolean();
        final String lastUpdated = (obj.get("lastUpdated") == null || obj.get("lastUpdated").isJsonNull()) ? null : obj.get("lastUpdated").getAsString();
        final int totalGames = obj.get("totalGames") == null ? 0 : obj.get("totalGames").getAsInt();
        final int serverPort = obj.get("serverPort") == null ? 0 : obj.get("serverPort").getAsInt();
        final int dataPort = obj.get("dataPort") == null ? 0 : obj.get("dataPort").getAsInt();
        
        
        final HPGSubscriber sub = new HPGSubscriber();
        sub.setName(name);
        sub.setUrl(url);
        sub.setDescription(desc);
        sub.setPort(port);
        sub.setIpAddress(ipAddress);
        sub.setDomain(domain);
        sub.setMWVersion(version);
        sub.setMaxPlayers(maxPlayers);
        sub.setMaxGames(maxGames);
        sub.setTotalGames(totalGames);
        sub.setServerPort(serverPort);
        sub.setDataPort(dataPort);
        
        sub.setLegacy(isLegacy);
        if (isLegacy) {
            sub.setUuid(uuid);
            sub.setPassword(null);
        } else {
            sub.setUuid(uuid);
            sub.setPassword(password);
        }
        
        if (obj.get("historicalPlayers") != null) {
            final JsonArray historicalPlayersArray = obj.get("historicalPlayers").getAsJsonArray();
            for (int i = 0; i < historicalPlayersArray.size(); i++) {
                final JsonElement e = historicalPlayersArray.get(i);
                sub.addHistoricalPlayersElement(e.getAsInt());
            }
        }
        
        if (obj.get("historicalGames") != null) {
            final JsonArray historicalGamesArray = obj.get("historicalGames").getAsJsonArray();
            for (int i = 0; i < historicalGamesArray.size(); i++) {
                final JsonElement e = historicalGamesArray.get(i);
                sub.addHistoricalGamesElement(e.getAsInt());
            }
        }
        
        SimpleDateFormat format = new SimpleDateFormat("MMM d, yyyy, h:mm:ss a");
        Date date = null;
        try {
            if(lastUpdated != null) {
                date = format.parse(lastUpdated);
            } else {
                date = new Date();
            }
        } catch (ParseException e) {
            logger.catching(e);
        }
        sub.setLastUpdated(date);
        
        return sub;
    }
}
