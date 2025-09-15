package mekwars.server.campaign.commands.admin;

import java.util.StringTokenizer;
import mekwars.server.MWServ;
import mekwars.server.MWChatServer.auth.IAuthenticator;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.commands.Command;
import mekwars.server.campaign.util.ChristmasHandler;
/**
 * Ends the Christmas season
 * <p>
 * Ends the Christmas season, doing some cleanup, including deleting the list of 
 * units handed out
 * 
 * @author Spork
 * @version 2016.10.26
 */
public class EndChristmasCommand implements Command {

	int accessLevel = IAuthenticator.ADMIN;
	String syntax = "";
	
	@Override
	public void process(StringTokenizer command, String Username) {
		//access level check
		int userLevel = MWServ.getInstance().getUserLevel(Username);
		if(userLevel < getExecutionLevel()) {
			CampaignMain.cm.toUser("AM:Insufficient access level for command. Level: " + userLevel + ". Required: " + accessLevel + ".",Username,true);
			return;
		}
		ChristmasHandler.getInstance().endChristmas();
		CampaignMain.cm.doSendModMail("SERVER", Username + " ended the Christmas season.");
		CampaignMain.cm.doSendToAllOnlinePlayers("AM: The Christmas season has officially ended.", true);
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
