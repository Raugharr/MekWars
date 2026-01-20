package mekwars.common.campaign.clientutils;

import java.util.Properties;
import megamek.Version;
import megamek.common.event.GameCFREvent;

public interface IGameHost {
    void setStatus(int newStatus);

    boolean isAdmin();

    boolean isMod();

    String getUsername();
    
    void gameClientFeedbackRquest(GameCFREvent arg0);

    String getServerConfigs(String key);

    Properties getServerConfigs();

    void loadSavegame();

    String getConfigParam(String param);
}
