/*
 * MekWars - Copyright (C) 2004 
 * 
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megamek)
 * Original author Helge Richter (McWizard)
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

package mekwars.client.cmd;

import java.nio.file.Path;
import java.util.StringTokenizer;

import mekwars.client.MWClient;
import mekwars.client.GUIClient;
import mekwars.client.campaign.CPlayer;
import mekwars.client.campaign.CUnit;
import mekwars.client.gui.dialog.AdvancedRepairDialog;
import mekwars.client.io.FileSystem;
import mekwars.common.CampaignData;
import mekwars.common.House;
import mekwars.common.SubFaction;
import mekwars.common.campaign.pilot.Pilot;
import mekwars.common.util.TokenReader;
import mekwars.common.util.UnitUtils;
import mekwars.client.common.campaign.clientutils.GameHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Imi (immanuel.scholz@gmx.de)
 */

public class PL extends Command {
    private static final Logger LOGGER = LogManager.getLogger(PL.class);
    /**
     * @param client
     */
    public PL(MWClient mwclient) {
        super(mwclient);
    }

    /**
     * @see client.cmd.Command#execute(java.lang.String)
     */
    @Override
    public void execute(String input) {
        StringTokenizer st = decode(input);

        String cmd = TokenReader.readString(st);
        CPlayer player = mwclient.getPlayer();

        if (!st.hasMoreTokens()) {
            return;
        }

        if (cmd.equals("FCU")) {
            mwclient.updateClient();
            return;
        }

        if (cmd.equals("RA")) {
            player.removeArmy(TokenReader.readInt(st));
            mwclient.getGUIClient().getMainFrame().updateAttackMenu();
        } else if (cmd.equals("LA")) {
            player.playerLockArmy(TokenReader.readInt(st));
        } else if (cmd.equals("ULA")) {
            player.playerUnlockArmy(TokenReader.readInt(st));
        } else if (cmd.equals("TAD")) {
            player.toggleArmyDisabled(TokenReader.readInt(st));
        } else if (cmd.equals("SAD")) {
            player.setArmyData(TokenReader.readString(st));
            mwclient.getGUIClient().getMainFrame().updateAttackMenu();
        } else if (cmd.equals("SABV")) {
            player.setArmyBV(TokenReader.readString(st));
        } else if (cmd.equals("AAU")) {
            player.addArmyUnit(TokenReader.readString(st));
        } else if (cmd.equals("RAU")) {
            player.removeArmyUnit(TokenReader.readString(st));
            mwclient.getGUIClient().refreshGUI(GUIClient.REFRESH_HQPANEL);
        } else if (cmd.equals("HD")) {
            player.setHangarData(TokenReader.readString(st));
        } else if (cmd.equals("RU")) {
            player.removeUnit(TokenReader.readInt(st));
        } else if (cmd.equals("SE")) {
            player.setExperience(TokenReader.readInt(st));
        } else if (cmd.equals("SM")) {
            player.setMoney(TokenReader.readInt(st));
        } else if (cmd.equals("UMT")) {
            player.setMekTokens(TokenReader.readInt(st)); //@Salient
        } else if (cmd.equals("SB")) {
            player.setBays(TokenReader.readInt(st));
        } else if (cmd.equals("SF")) {
            player.setFreeBays(TokenReader.readInt(st));
        } else if (cmd.equals("SI")) {
            player.setInfluence(TokenReader.readInt(st));
        } else if (cmd.equals("SR")) {
            player.setRating(TokenReader.readDouble(st));
        } else if (cmd.equals("SRP")) {
            player.setRewardPoints(TokenReader.readInt(st));
        } else if (cmd.equals("SH")) {
            setHouse(st, player);
        } else if (cmd.equals("ST")) {
            player.setTechnicians(TokenReader.readInt(st));
        } else if (cmd.equals("SSN")) {
            String subfactionName = TokenReader.readString(st);
            SubFaction subfaction = player.getMyHouse().getSubfaction(subfactionName);

            player.setSubfaction(subfaction);
        } else if (cmd.equals("AAA")) {
            mwclient.getCampaign().setAutoArmy(st);// give it the whole tokenizer
        } else if (cmd.equals("AAM")) {
            player.setMines(st);// give it the whole tokenizer
        } else if (cmd.equals("GEA")) {
            mwclient.getCampaign().setAutoGunEmplacements(st);// give it the whole tokenizer
        } else if (cmd.equals("SUS")) {
            player.setUnitStatus(TokenReader.readString(st));
        } else if (cmd.equals("RNA")) {
            player.setArmyName(TokenReader.readString(st));
        } else if (cmd.equals("SAB")) {
            player.setArmyLimit(TokenReader.readString(st));
        } else if (cmd.equals("SAL")) {
            player.setArmyLock(TokenReader.readString(st));
        } else if (cmd.equals("UU")) {
            player.updateUnitData(st);
        } else if (cmd.equals("UUMG")) {
            player.updateUnitMachineGuns(st);
        } else if (cmd.equals("BMW")) { // play a sound someone won the bm.
            if (mwclient.getConfig().isParam("ENABLEBMSOUND")) {
                mwclient.getSoundManager().doPlaySound(mwclient.getConfig().getParam("SOUNDONBMWIN"));
            }
        } else if (cmd.equals("PPQ")) {
            player.getPersonalPilotQueue().fromString(TokenReader.readString(st));
        } else if (cmd.equals("PEU")) {
            player.setPlayerExcludes(TokenReader.readString(st), "$");
            mwclient.getGUIClient().getMainFrame().getMainPanel().getUserListPanel().repaint();
        } else if (cmd.equals("AEU")) {
            player.setAdminExcludes(TokenReader.readString(st), "$");
            mwclient.getGUIClient().getMainFrame().getMainPanel().getUserListPanel().repaint();
        } else if (cmd.equals("RPU")) {
            player.repositionArmyUnit(TokenReader.readString(st));
        } else if (cmd.equals("UOE")) {
            player.updateOperations(TokenReader.readString(st));
            mwclient.getGUIClient().getMainFrame().updateAttackMenu();
        } else if (cmd.equals("UTT")) {
            player.updateTotalTechs(TokenReader.readString(st));
        } else if (cmd.equals("UAT")) {
            player.updateAvailableTechs(TokenReader.readString(st));
        } else if (cmd.equals("GBB")) {
            mwclient.getConnector().closeConnection();
        } else if (cmd.equals("UB")) {
            mwclient.setUsingBots(TokenReader.readBoolean(st));
        } else if (cmd.equals("BOST")) {
            mwclient.setBotsOnSameTeam(TokenReader.readBoolean(st));
        } else if (cmd.equals("SHFF")) {
            player.setHouseFightingFor(TokenReader.readString(st));
        } else if (cmd.equals("SUL")) {// Players Unit Logo
            player.setLogo(TokenReader.readString(st));
            mwclient.getGUIClient().refreshGUI(GUIClient.REFRESH_PLAYERPANEL);
        } else if (cmd.equals("AP2PPQ")) {
            player.getPersonalPilotQueue().addPilot(st);
        } else if (cmd.equals("RPPPQ")) {
            player.getPersonalPilotQueue().removePilot(st);
        } else if (cmd.equals("RSOD")) {
            mwclient.retrieveOpData("short", TokenReader.readString(st));
        } else if (cmd.equals("UCP")) {
            mwclient.updateParam(st);
        } else if (cmd.equals("SOFL")) {
            mwclient.setServerOpFlags(st);
        } else if (cmd.equals("SAOFS")) {
            player.setArmyOpForceSize(TokenReader.readString(st));
        } else if (cmd.equals("FC")) {
            setFactionConfigs(TokenReader.readString(st));
        } else if (cmd.equals("UPBM")) {
            mwclient.updatePartsBlackMarket(TokenReader.readString(st), Integer.parseInt(mwclient.getServerConfigs("CampaignYear")));
        } else if (cmd.equals("UPPC")) {
            mwclient.updatePlayerPartsCache(TokenReader.readString(st));
        } else if (cmd.equals("RPPC")) {
            mwclient.getPlayer().getUnitComponents().fromString(st);
        } else if (cmd.equals("STN")) {
            mwclient.getPlayer().setTeamNumber(TokenReader.readInt(st));
        } else if (cmd.equals("VUI")) {
            StringTokenizer data = new StringTokenizer(TokenReader.readString(st), "#");
            String filename = TokenReader.readString(data);
            int BV = TokenReader.readInt(data);
            int gunnery = TokenReader.readInt(data);
            int piloting = TokenReader.readInt(data);
            String damage = "";

            if (data.hasMoreElements()) {
                damage = TokenReader.readString(data);
            }

            mwclient.getGUIClient().getMainFrame().getMainPanel().getHSPanel().showInfoWindow(filename, BV, gunnery, piloting, damage);
        } else if (cmd.equals("VURD")) {
            StringTokenizer data = new StringTokenizer(TokenReader.readString(st), "#");
            String filename = TokenReader.readString(data);
            String damage = TokenReader.readString(data);
            CUnit unit = new CUnit();

            unit.setUnitFilename(filename);
            unit.createEntity();
            unit.setPilot(new Pilot(player.getMyHouse(), "Jeeves", 4, 5));
            UnitUtils.applyBattleDamage(unit.getEntity(), damage, true);
            new AdvancedRepairDialog(mwclient, unit, unit.getEntity(), false);
        } else if (cmd.equals("CPPC")) {
            mwclient.getPlayer().getUnitComponents().clear();
        } else if (cmd.equals("UDAO")) {
            mwclient.updateOpData(true);
            if (!mwclient.isDedicated()) {
                mwclient.getGUIClient().getMainFrame().updateAttackMenu();
            }
        } else if (cmd.equals("RMF")) {
            mwclient.retrieveMul(TokenReader.readString(st));
        } else if (cmd.equals("SMFD")) {
            mwclient.getGUIClient().getMainFrame().showMulFileList(TokenReader.readString(st));
        } else if (cmd.equals("CAFM")) {
            mwclient.getGUIClient().getMainFrame().createArmyFromMul(TokenReader.readString(st));
        } else if (cmd.equals("USU")) {
            // Update Supported Units
            while (st.hasMoreTokens()) {
                boolean addSupport = TokenReader.readBoolean(st);
                String unitName = TokenReader.readString(st);
                if (unitName != null) {
                    if (addSupport) {
                        player.getMyHouse().addUnitSupported(unitName);
                    } else {
                        player.getMyHouse().removeUnitSupported(unitName);
                    }
                }
            }
            LOGGER.info(player.getMyHouse().getSupportedUnits().toString());
        } else if (cmd.equals("CSU")) {
            // clear supported units
            LOGGER.info("Clearing Supported Units");
            player.getMyHouse().supportedUnits.clear();
            player.getMyHouse().setNonFactionUnitsCostMore(Boolean.parseBoolean(mwclient.getServerConfigs("UseNonFactionUnitsIncreasedTechs")));
        } else if (cmd.equals("SMA")) {
            mwclient.getCampaign().setMULCreatedArmy(st);
        } else if (cmd.equals("ANH")) {
            mwclient.createNewHouse(st);
        } else if (cmd.equals("RPF")) {
            int id = TokenReader.readInt(st);
            mwclient.getData().removeHouse(id);
        } else if (cmd.equals("UDT")) {
            mwclient.getGUIClient().addToChat(TokenReader.readString(st), mwclient.getConfig().getIntParam("USERDEFINDMESSAGETAB"));
        } else if (cmd.equals("CCC")) {
            mwclient.getCampaign().setComponentConverter(st.nextToken());
        } else if (cmd.equals("SUD")) {
            try {
                StringBuilder userData = new StringBuilder(GameHost.CAMPAIGN_PREFIX + "c sendclientdata#");
                String clientMD5 = mwclient.createFilenameChecksum("./MekWarsClient.jar");
                String mmMD5 = mwclient.createFilenameChecksum("./MegaMek.jar");
                userData.append(mwclient.getClass().getProtectionDomain().getCodeSource().getLocation().toURI() + "#");
                userData.append(clientMD5 + "#");
                userData.append(mmMD5 + "#");

                String[] userDataSet =
                    { "user.name", "user.language", "user.country", "user.timezone", "os.name", "os.arch", "os.version", "java.version" };

                for (int pos = 0; pos < userDataSet.length; pos++) {
                    String property = System.getProperty(userDataSet[pos], "Unknown");
                    userData.append(property);
                    userData.append("#");
                }
                mwclient.sendChat(userData.toString());
            } catch (Exception ex) {
            }
        } else if (cmd.equals("ROP")) {
            mwclient.getPlayer().setAutoReorder(TokenReader.readBoolean(st));
        } else if (cmd.equals("SHP")) {
            player.parseHangarPenaltyString(TokenReader.readString(st));
            mwclient.getGUIClient().getMainFrame().getMainPanel().getHSPanel().updateDisplay();
        } else if (cmd.equals("STS")) {
            int unitID = TokenReader.readInt(st);
            int targetType = TokenReader.readInt(st);
            //LOGGER.error("Setting Targeting for Unit " + unitID + " to " + targetType);
            player.getUnit(unitID).setTargetSystem(targetType);
            mwclient.doParseDataInput("CH|AM: Targeting for unit " + unitID + " set to " + player.getUnit(unitID).getTargetSystemTypeDesc());
        } else {
            return;
        }

        mwclient.getGUIClient().refreshGUI(GUIClient.REFRESH_HQPANEL);
        mwclient.getGUIClient().refreshGUI(GUIClient.REFRESH_PLAYERPANEL);
        mwclient.getGUIClient().refreshGUI(GUIClient.REFRESH_BMPANEL);
    }

    public void setFactionConfigs(String data) {
        if (data.startsWith("DONE#DONE")) {
            mwclient.setWaiting(false);
            return;
        }

        StringTokenizer ST = new StringTokenizer(data, CPlayer.DELIMITER);
        while (ST.hasMoreTokens()) {
            String key = TokenReader.readString(ST);
            String value = TokenReader.readString(ST);

            mwclient.getServerConfigs().setProperty(key, value);
        }
        mwclient.setWaiting(false);
    }

    private void setHouse(StringTokenizer st, CPlayer player) {
        House playerHouse = player.getMyHouse();
        player.setHouse(TokenReader.readString(st));
        /*
         * Get the faction configs before starting anything else. I could pause
         * the client and wait for the configs but I'll let it go. --Torren
         */
        mwclient.sendChat(GameHost.CAMPAIGN_PREFIX + "c getfactionconfigs#0" + mwclient.getServerConfigs("TIMESTAMP"));
        /**
         * FIXME: This is a hack. Currently the house config files only exist on the server and are
         * then appended to the campaignconfig.txt above. In order to make the server and client
         * both use the House's config file we create a dummy config below that will have no
         * parameters and pass through to the campaignconfig.txt. Later this hack will be removed
         * when house config files exist properly on the client side.
         */
        if (CampaignData.cd.getHouseOptions(playerHouse.getName()) == null) {
            Path configPath = FileSystem.getInstance().getFactionConfigPath(playerHouse.getName());

            CampaignData.cd.loadHouseOptions(configPath, playerHouse);
        }


        /*
         * Now that we have a house set, we can check for BM access properly. Do
         * the BM buy and sell button checks.
         */
        if (mwclient.getGUIClient().getMainFrame().getMainPanel().getBMPanel() != null) {
            mwclient.getGUIClient().getMainFrame().getMainPanel().getBMPanel().checkFactionAccess();
        }

        /*
         * Same thing for the HQ. We have a house, so we can rebuild the button
         * bar w/ or w/o a reset button, as appropriate.
         */
        if (mwclient.getGUIClient().getMainFrame().getMainPanel().getHQPanel() != null) {
            mwclient.getGUIClient().getMainFrame().getMainPanel().getHQPanel().reinitialize();
        }
    }
}
