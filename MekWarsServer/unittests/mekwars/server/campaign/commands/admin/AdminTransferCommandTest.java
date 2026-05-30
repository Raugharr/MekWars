/*
 * MekWars - Copyright (C) 2026
 *
 * Derived from MegaMekNET (http://www.sourceforge.net/projects/megameknet)
 * Original author Helge Richter (McWizard)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 */

package mekwars.server.campaign.commands.admin;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import megamek.common.Mech;

import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.MWServ;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SPlayer;
import mekwars.server.campaign.SUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.StringTokenizer;

@ExtendWith(MockitoExtension.class)
class AdminTransferCommandTest {
    private AdminTransferCommand adminTransferCommand;

    @Mock private MWServ mwServ;

    @Mock private CampaignMain campaignMain;

    @Mock private SPlayer senderPlayer;

    @Mock private SPlayer receiverPlayer;

    @Mock private SUnit transferredUnit;

    @Mock private Mech mech;

    @BeforeEach
    public void setup() {
        adminTransferCommand = new AdminTransferCommand();
        adminTransferCommand.setExecutionLevel(IAuthenticator.ADMIN);
    }

    @Test
    public void testInsufficientAccessLevel() {
        try (MockedStatic<MWServ> mockedServ = mockStatic(MWServ.class)) {
            mockedServ.when(MWServ::getInstance).thenReturn(mwServ);
            CampaignMain.cm = campaignMain;
            when(mwServ.getUserLevel("adminUser")).thenReturn(IAuthenticator.REGISTERED);

            StringTokenizer tokenizer = new StringTokenizer("Sender#Receiver#1", "#");
            adminTransferCommand.process(tokenizer, "adminUser");

            String errorString =
                    "AM:Insufficient access level for command. Level: "
                            + IAuthenticator.REGISTERED
                            + ". Required: "
                            + IAuthenticator.ADMIN
                            + ".";
            verify(campaignMain).toUser(eq(errorString), eq("adminUser"), eq(true));
        } finally {
            CampaignMain.cm = null;
        }
    }

    @Test
    public void testImproperFormatMissingElements() {
        try (MockedStatic<MWServ> mockedServ = mockStatic(MWServ.class)) {
            mockedServ.when(MWServ::getInstance).thenReturn(mwServ);
            CampaignMain.cm = campaignMain;
            when(mwServ.getUserLevel("adminUser")).thenReturn(IAuthenticator.ADMIN);

            // Only provide 2 elements instead of 3
            StringTokenizer tokenizer = new StringTokenizer("Sender#Receiver", "#");
            adminTransferCommand.process(tokenizer, "adminUser");

            String errorString = "AM:Improper format. Try: /c admintransfer#from#to#id";
            verify(campaignMain).toUser(eq(errorString), eq("adminUser"), eq(true));
        } finally {
            CampaignMain.cm = null;
        }
    }

    @Test
    public void testSendingPlayerNotFound() {
        try (MockedStatic<MWServ> mockedServ = mockStatic(MWServ.class)) {
            mockedServ.when(MWServ::getInstance).thenReturn(mwServ);
            CampaignMain.cm = campaignMain;
            when(mwServ.getUserLevel("adminUser")).thenReturn(IAuthenticator.ADMIN);
            when(campaignMain.getPlayer("NonExistentPlayer")).thenReturn(null);

            StringTokenizer tokenizer = new StringTokenizer("NonExistentPlayer#Receiver#1", "#");
            adminTransferCommand.process(tokenizer, "adminUser");

            String errorString = "AM:Sending player could not be found. Try again.";
            verify(campaignMain).toUser(eq(errorString), eq("adminUser"), eq(true));
        } finally {
            CampaignMain.cm = null;
        }
    }

    @Test
    public void testReceivingPlayerNotFound() {
        try (MockedStatic<MWServ> mockedServ = mockStatic(MWServ.class)) {
            mockedServ.when(MWServ::getInstance).thenReturn(mwServ);
            CampaignMain.cm = campaignMain;
            when(mwServ.getUserLevel("adminUser")).thenReturn(IAuthenticator.ADMIN);
            when(campaignMain.getPlayer("Sender")).thenReturn(senderPlayer);
            when(campaignMain.getPlayer("NonExistentReceiver")).thenReturn(null);

            StringTokenizer tokenizer = new StringTokenizer("Sender#NonExistentReceiver#1", "#");
            adminTransferCommand.process(tokenizer, "adminUser");

            String errorString = "AM:Receiving player could not be found. Try again.";
            verify(campaignMain).toUser(eq(errorString), eq("adminUser"), eq(true));
        } finally {
            CampaignMain.cm = null;
        }
    }

    @Test
    public void testUnitNotFoundForSender() {
        try (MockedStatic<MWServ> mockedServ = mockStatic(MWServ.class)) {
            mockedServ.when(MWServ::getInstance).thenReturn(mwServ);
            CampaignMain.cm = campaignMain;
            when(mwServ.getUserLevel("adminUser")).thenReturn(IAuthenticator.ADMIN);
            when(campaignMain.getPlayer("Sender")).thenReturn(senderPlayer);
            when(campaignMain.getPlayer("Receiver")).thenReturn(receiverPlayer);
            when(senderPlayer.getUnit(999)).thenReturn(null);

            StringTokenizer tokenizer = new StringTokenizer("Sender#Receiver#999", "#");
            adminTransferCommand.process(tokenizer, "adminUser");

            String errorString = "AM:Sender doesn't have a unit with ID# " + 999 + ".";
            verify(campaignMain).toUser(eq(errorString), eq("adminUser"), eq(true));
        } finally {
            CampaignMain.cm = null;
        }
    }

    @Test
    public void testSuccessfulTransfer() {
        try (MockedStatic<MWServ> mockedServ = mockStatic(MWServ.class)) {
            mockedServ.when(MWServ::getInstance).thenReturn(mwServ);
            CampaignMain.cm = campaignMain;
            when(mwServ.getUserLevel("adminUser")).thenReturn(IAuthenticator.ADMIN);
            when(campaignMain.getPlayer("Sender")).thenReturn(senderPlayer);
            when(campaignMain.getPlayer("Receiver")).thenReturn(receiverPlayer);
            when(senderPlayer.getUnit(1)).thenReturn(transferredUnit);
            when(transferredUnit.getId()).thenReturn(1);
            when(transferredUnit.getModelName()).thenReturn("Warhammer WTH-4L");
            when(receiverPlayer.getName()).thenReturn("Receiver");

            StringTokenizer tokenizer = new StringTokenizer("Sender#Receiver#1", "#");
            adminTransferCommand.process(tokenizer, "adminUser");

            verify(campaignMain).toUser(anyString(), eq("adminUser"), eq(true));
            verify(campaignMain).toUser(anyString(), eq("Sender"), eq(true));
            verify(campaignMain).toUser(anyString(), eq("Receiver"), eq(true));
            verify(campaignMain).doSendModMail(anyString(), anyString());
            verify(senderPlayer).removeUnit(1, true);
            verify(receiverPlayer).addUnit(transferredUnit, true);
        } finally {
            CampaignMain.cm = null;
        }
    }

    @Test
    public void testSyntaxReturnsCorrectFormat() {
        String expectedSyntax = "Sending Player#Receiving Player#Unit ID";
        assert adminTransferCommand.getSyntax().equals(expectedSyntax);
    }

    @Test
    public void testGetExecutionLevelReturnsAdmin() {
        assert adminTransferCommand.getExecutionLevel() == IAuthenticator.ADMIN;
    }

    @Test
    public void testSetExecutionLevel() {
        int newLevel = IAuthenticator.MODERATOR;
        adminTransferCommand.setExecutionLevel(newLevel);
        assert adminTransferCommand.getExecutionLevel() == newLevel;
    }
}
