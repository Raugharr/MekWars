package mekwars.server.campaign.commands.admin;

import mekwars.common.CampaignData;
import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.MWServ;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.commands.Command;
import mekwars.server.campaign.util.ChristmasHandler;

import java.util.StringTokenizer;

/**
 * A command to start the Christmas Season
 *
 * @author Spork
 * @version 2016.10.26
 */
public class StartChristmasCommand implements Command {

    int accessLevel = IAuthenticator.ADMIN;
    String syntax = "";

    @Override
    public void process(StringTokenizer command, String username) {
        // access level check
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

        ChristmasHandler.getInstance().startChristmas();
        CampaignData.cd.getCampaignOptions().setProperty("Christmas_ManuallyStarted", "true");

        CampaignMain.cm.doSendModMail(
                "SERVER", "Happy Holidays! " + username + " started the Christmas season.");
        CampaignMain.cm.doSendToAllOnlinePlayers(
                "AM: The Christmas season is upon us.  Happy Holidays!", true);
    }

    @Override
    public int getExecutionLevel() {
        return accessLevel;
    }

    @Override
    public void setExecutionLevel(int i) {
        accessLevel = i;
    }

    @Override
    public String getSyntax() {
        return syntax;
    }
}
