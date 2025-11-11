package mekwars.common;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import megamek.common.Entity;
import megamek.common.Game;
import megamek.common.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class GameWrapper implements GameInterface {
    private static final Logger LOGGER = LogManager.getLogger(GameWrapper.class);

    private final Game game;
    
    public GameWrapper(Game game) {
        this.game = game;
    }

    @Override
    public Enumeration<Entity> getDevastatedEntities() {
        return game.getDevastatedEntities();
    }

    @Override
    public Enumeration<Entity> getGraveyardEntities() {
        return game.getGraveyardEntities();
    }

    @Override
    public Iterator<Entity> getEntities() {
        return game.getEntities();
    }

    @Override
    public Enumeration<Entity> getRetreatedEntities() {
        return game.getRetreatedEntities();
    }

    @Override
    public List<String> getWinners() {
        ArrayList<String> result = new ArrayList<String>();
        
        //TODO: Winners sometimes coming up empty. Let's see why
        Enumeration<Player> en = game.getPlayers();

        LOGGER.error("  :: game.getPlayers(): {}", en.toString());
        LOGGER.error("  :: VictoryTeam: {}", game.getVictoryTeam());
        
        while (en.hasMoreElements()){
            final Player player = en.nextElement();
            LOGGER.error("  :: ==> Player: {} :: Team: {}", player.getName().trim(), player.getTeam());
            
            if (player.getTeam() == game.getVictoryTeam()){
                result.add(player.getName().trim());
            }
        }
        return result;
    }

    @Override
    public boolean hasWinner() {
        return game.getVictoryTeam() != Player.TEAM_NONE;
    }
}
