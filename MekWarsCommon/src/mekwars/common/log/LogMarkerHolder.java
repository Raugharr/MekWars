package mekwars.common.log;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

/**
 * A holder for log markers
 */
public class LogMarkerHolder {
    public static final Marker RESULTS_MARKER = MarkerManager.getMarker("resultsLog");
    public static final Marker GAME_MARKER = MarkerManager.getMarker("gameLog");
    public static final Marker TEST_MARKER = MarkerManager.getMarker("testLog");
    public static final Marker TICK_MARKER = MarkerManager.getMarker("tickLog");
    public static final Marker PM_MARKER = MarkerManager.getMarker("pmLog");
}
