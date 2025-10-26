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
package mekwars.server.util.discord;

import mekwars.common.util.MWLogger;
import mekwars.server.campaign.CampaignMain;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Provides integration with a Discord webhook.  Status messages and
 * Operation outcome can be sent to the webhook.
 * @author Spork
 */
public class DiscordMessageHandler {
    private String webhookAddress;
    private HttpClient httpClient;

    public DiscordMessageHandler(String webhookAddress, HttpClient httpClient) {
        this.webhookAddress = webhookAddress;
        this.httpClient = httpClient;
    }

    public DiscordMessageHandler() {
        webhookAddress = CampaignMain.cm.getBooleanConfig("DiscordEnable")
                ? CampaignMain.cm.getConfig("DiscordWebHookAddress")
                : null;

        httpClient = HttpClient.newBuilder()
                .version(Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * Post a message to the webhook
     * @param message the message to send
     */
    public void post(String message) {
        if (addressValid()) {
            String body = "content=" + URLEncoder.encode(message, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder(URI.create(webhookAddress))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .POST(BodyPublishers.ofString(body))
                    .build();
            try {
                HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
                if (response.statusCode() >= 400) {
                    MWLogger.errLog("Discord returned HTTP " + response.statusCode()
                            + " body=" + response.body());
                }
            } catch (IOException | InterruptedException e) {
                MWLogger.errLog(e);
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * webhookAddress is expected to be non-null and non-empty. Further validation might be reasonable if this
     * functionality is critical.
     * @return webhook validity
     */
    private boolean addressValid() {
        return webhookAddress != null && !webhookAddress.trim().isEmpty();
    }
}
