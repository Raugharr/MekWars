package mekwars.server.campaign;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CampaignMainTest {

    /**
     * Ideally the working dir for unit tests is the test root, not the project root. We're capturing the current
     * state of the program in these tests, then, since the deployed artifacts also mirror the repo structure.
     */
    @Test
    public void testConstructorFilesExist() {
        assertTrue(Files.exists(Path.of("./campaign/")));
        assertTrue(Files.exists(Path.of("./campaign/players/")));
        assertTrue(Files.exists(Path.of("./data/campaignconfig.txt")));
        new CampaignMain("./data/campaignconfig.txt");
    }

}
