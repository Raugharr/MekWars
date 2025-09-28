package mekwars.server.campaign.commands.mod;

import java.util.StringTokenizer;
import mekwars.common.CampaignData;
import mekwars.common.House;
import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.MWServ;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SHouse;
import mekwars.server.campaign.SPlayer;
import mekwars.server.campaign.commands.Command;

public class AnnounceCommand implements Command {
    int accessLevel = IAuthenticator.MODERATOR;
    String syntax =
            "Set for Faction: /announce [FactionName]#Message ('clear' to unset)<br>Set for all Factions: /announce All#Message ('clear' to unset)";

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

    @Override
    public void process(StringTokenizer command, String Username) {
        if (accessLevel != 0) {
            int userLevel = MWServ.getInstance().getUserLevel(Username);
            if (userLevel < getExecutionLevel()) {
                CampaignMain.cm.toUser(
                        "AM:Insufficient access level for command. Level: "
                                + userLevel
                                + ". Required: "
                                + accessLevel
                                + ".",
                        Username,
                        true);
                return;
            }
        }
        if (!command.hasMoreTokens()) {
            CampaignMain.cm.toUser("Invalid Sytax: <br>" + getSyntax(), Username, true);
            return;
        }

        String scope = command.nextToken();
        if (scope.equalsIgnoreCase("all")) {
            // Set for all factions
            String announcement = "";
            try {
                // there may be #'s in HTML. Use all tokens and restore #'s.
                announcement = command.nextToken();
                while (command.hasMoreTokens()) announcement += "#" + command.nextToken();
            } catch (Exception e) {
                CampaignMain.cm.toUser("AM:nvalid Syntax: <br> " + getSyntax(), Username, true);
                return;
            }
            SPlayer p = CampaignMain.cm.getPlayer(Username);
            if (announcement.trim().equals("") || announcement.trim().equalsIgnoreCase("clear")) {
                announcement = "";
            }
            for (House h : CampaignData.cd.getAllHouses()) {
                SHouse house = (SHouse) h;
                house.setAnnouncement(announcement);
            }
            CampaignMain.cm.toUser("Announcement set for all factions.", Username, true);

        } else {
            // Set for a single faction
            SHouse h = (SHouse) CampaignMain.cm.getData().getHouseByName(scope);
            if (h == null) {
                CampaignMain.cm.toUser("Invalid Syntax: <br> " + getSyntax(), Username, true);
            }
            String announcement = "";
            try {
                // there may be #'s in HTML. Use all tokens and restore #'s.
                announcement = command.nextToken();
                while (command.hasMoreTokens()) announcement += "#" + command.nextToken();
            } catch (Exception e) {
                CampaignMain.cm.toUser("AM:nvalid Syntax: <br> " + getSyntax(), Username, true);
                return;
            }
            SPlayer p = CampaignMain.cm.getPlayer(Username);
            if (announcement.trim().equals("") || announcement.trim().equalsIgnoreCase("clear")) {
                p.getMyHouse().setAnnouncement("");
                CampaignMain.cm.toUser("AM:" + scope + " announcement cleared.", Username, true);
                return;
            }
            p.getMyHouse().setAnnouncement(announcement + "<p> -- Set by " + p.getName());
            CampaignMain.cm.toUser("AM:MOTD set. Use /c motd to review.", Username, true);
        }
    }
}
