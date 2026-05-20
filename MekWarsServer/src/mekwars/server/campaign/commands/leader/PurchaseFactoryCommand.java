/*
 * MekWars - Copyright (C) 2007
 *
 * Original author - jtighe (torren@users.sourceforge.net)
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 */

package mekwars.server.campaign.commands.leader;

import megamek.common.TechConstants;

import mekwars.common.UnitFactory;
import mekwars.common.util.HibernateUtil;
import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.MWServ;
import mekwars.server.campaign.BuildTable;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SHouse;
import mekwars.server.campaign.SPlanet;
import mekwars.server.campaign.SPlayer;
import mekwars.server.campaign.SUnit;
import mekwars.server.campaign.SUnitFactory;
import mekwars.server.campaign.commands.Command;

import java.io.File;
import java.util.StringTokenizer;
import java.util.List;

public class PurchaseFactoryCommand implements Command {
    // Starting out at mod level this can be lowered as needed
    int accessLevel = IAuthenticator.MODERATOR;

    public int getExecutionLevel() {
        return accessLevel;
    }

    public void setExecutionLevel(int i) {
        accessLevel = i;
    }

    String syntax = "Factory Name#Type#Weight#Planet";

    public String getSyntax() {
        return syntax;
    }

    public void process(StringTokenizer command, String username) {

        if (accessLevel != 0) {
            int userLevel = MWServ.getInstance().getUserLevel(username);
            if (userLevel < getExecutionLevel()) {
                CampaignMain.cm.toUser(
                        "AM:Insufficient access level for command. Level: "
                                + userLevel
                                + ". Required: "
                                + accessLevel
                                + ".",
                        username,
                        true);
                return;
            }
        }

        SPlayer player = CampaignMain.cm.getPlayer(username);
        SPlanet planet;
        SHouse house;
        double cost = 0.0;
        double flu = 0.0;
        int type = SUnit.MEK;
        int weight = SUnit.LIGHT;
        String name = "";
        int buildType = UnitFactory.BUILDMEK;

        name = command.nextToken();
        type = Integer.parseInt(command.nextToken());
        weight = Integer.parseInt(command.nextToken());
        planet = CampaignMain.cm.getPlanetFromPartialString(command.nextToken(), null);
        house = player.getMyHouse();

        if (planet == null) {
            CampaignMain.cm.toUser("Unable to find planet.", username);
            return;
        }

        if (house.isNewbieHouse()) {
            CampaignMain.cm.toUser(
                    CampaignMain.cm.getConfig("NewbieHouseName")
                            + " cannot purchase new factories!",
                    username);
            return;
        }

        if (type == SUnit.BATTLEARMOR && house.getTechLevel() < TechConstants.T_IS_TW_ALL) {
            CampaignMain.cm.toUser(
                    "Your factions tech level is not high enough to purchase Battle Armor"
                            + " factories",
                    username);
            return;
        }

        if (type == SUnit.PROTOMEK && house.getTechLevel() < TechConstants.T_CLAN_TW) {
            CampaignMain.cm.toUser(
                    "Your factions tech level is not high enough to purchase ProtoMek factories",
                    username);
            return;
        }

        if (!planet.isOwner(house.getId())) {
            CampaignMain.cm.toUser("You do not own " + planet.getName(), username);
            return;
        }

        String buildTable =
                BuildTable.getFileName(
                        "Common", SUnit.getWeightClassDesc(weight), BuildTable.STANDARD, type);

        if (!new File(buildTable).exists()) {
            CampaignMain.cm.toUser("Sorry but That type of factory cannot be built.", username);
            return;
        }

        switch (type) {
            case SUnit.MEK:
                buildType = UnitFactory.BUILDMEK;
                break;
            case SUnit.INFANTRY:
                buildType = UnitFactory.BUILDINFANTRY;
                break;
            case SUnit.VEHICLE:
                buildType = UnitFactory.BUILDVEHICLES;
                break;
            case SUnit.BATTLEARMOR:
                buildType = UnitFactory.BUILDBATTLEARMOR;
                break;
            case SUnit.PROTOMEK:
                buildType = UnitFactory.BUILDPROTOMECHS;
                break;
        }

        cost = CampaignMain.cm.getDoubleConfig("NewFactoryBaseCost");
        cost *=
                CampaignMain.cm.getDoubleConfig(
                        "NewFactoryCostModifier" + SUnit.getWeightClassDesc(weight));
        cost *=
                CampaignMain.cm.getDoubleConfig(
                        "NewFactoryCostModifier" + SUnit.getTypeClassDesc(type));

        cost = Math.round(cost);

        flu = CampaignMain.cm.getDoubleConfig("NewFactoryBaseFlu");
        flu *=
                CampaignMain.cm.getDoubleConfig(
                        "NewFactoryFluModifier" + SUnit.getWeightClassDesc(weight));
        flu *=
                CampaignMain.cm.getDoubleConfig(
                        "NewFactoryFluModifier" + SUnit.getTypeClassDesc(type));

        flu = Math.round(flu);

        if (player.getMoney() < cost) {
            CampaignMain.cm.toUser(
                    "AM:You need "
                            + CampaignMain.cm.moneyOrFluMessage(true, true, (int) cost)
                            + " to purchase a factory.",
                    username);
            return;
        }

        if (player.getInfluence() < flu) {
            CampaignMain.cm.toUser(
                    "AM:You need "
                            + CampaignMain.cm.moneyOrFluMessage(false, true, (int) flu)
                            + " to purchase a factory.",
                    username);
            return;
        }

        cost = Math.max(0, cost);
        flu = Math.max(0, flu);

        player.addMoney((int) -cost);
        player.addInfluence((int) -flu);

        SUnitFactory factory =
                new SUnitFactory(
                        name,
                        planet,
                        SUnit.getWeightClassDesc(weight),
                        house.getName(),
                        0,
                        CampaignMain.cm.getIntegerConfig("BaseFactoryRefreshRate"),
                        buildType,
                        BuildTable.STANDARD,
                        0);
        factory.setPlanet(planet);
        planet.setOwner(house);
        
        house.updated();
        planet.updated();

        CampaignMain.cm.toUser(
                "AM:You have purchased a factory, "
                        + name
                        + ", on planet "
                        + planet.getName()
                        + " for "
                        + CampaignMain.cm.moneyOrFluMessage(true, true, (int) cost)
                        + " and "
                        + CampaignMain.cm.moneyOrFluMessage(false, true, (int) flu),
                username,
                true);
        CampaignMain.cm.doSendHouseMail(
                house,
                "NOTE",
                username
                        + " has purchased a factory, "
                        + name
                        + ", on planet "
                        + planet.getName()
                        + ".");
    }
} // end RequestSubFactionPromotionCommand class
