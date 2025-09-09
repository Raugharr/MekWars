package mekwars.server.campaign.commands;

import java.util.StringTokenizer;
import mekwars.server.MWServ;
import mekwars.server.campaign.CampaignMain;
import mekwars.server.campaign.SArmy;
import mekwars.server.campaign.SPlayer;

public class PlayerUnlockArmyCommand implements Command {
	int accessLevel = 0;
	String syntax = "";
	public int getExecutionLevel(){return accessLevel;}
	public void setExecutionLevel(int i) {accessLevel = i;}
	public String getSyntax() { return syntax;}

	public void process(StringTokenizer command,String Username) {
		
		if (accessLevel != 0) {
			int userLevel = MWServ.getInstance().getUserLevel(Username);
			if(userLevel < getExecutionLevel()) {
				CampaignMain.cm.toUser("AM:Insufficient access level for command. Level: " + userLevel + ". Required: " + accessLevel + ".",Username,true);
				return;
			}
		}
		
		SPlayer p = CampaignMain.cm.getPlayer(Username);
		if (p == null) {
			CampaignMain.cm.toUser("AM:Null Player while renaming army. Report This!.",Username,true);
			return;
		}
		int aid = -1;
		try {
			aid = Integer.parseInt((String)command.nextElement());
		} catch (Exception e) {
			CampaignMain.cm.toUser("AM:Improper format. Try: /c playerunlockarmy#ID",Username,true);
			return;
		}
		SArmy army = p.getArmy(aid);
		if (army == null) {
			CampaignMain.cm.toUser("AM:Could not find an Army #" + aid + ".",Username,true);
			return;
		}
		army.setPlayerLock(aid, false);
		CampaignMain.cm.toUser("AM:Army " + army.getID() + " unlocked.",Username,true);
	}
}
