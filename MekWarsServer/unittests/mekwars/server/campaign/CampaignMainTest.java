package mekwars.server.campaign;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mekwars.common.util.HibernateUtil;
import mekwars.server.ServerEntities;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CampaignMainTest {
    @BeforeAll
    static void setup() throws IOException {
        Files.createDirectories(Path.of("campaign", "players"));
        Path cfg = Path.of("data", "campaignconfig.txt");
        Files.createDirectories(cfg.getParent());
        if (!Files.exists(cfg)) {
            Files.writeString(cfg, "");
        }
    }

    /**
     * Ideally the working dir for unit tests is the test root, not the project root. We're
     * capturing the current state of the program in these tests, then, since the deployed artifacts
     * also mirror the repo structure.
     */
    @Test
    void testConstructorFilesExist() {
        assertTrue(Files.exists(Path.of("campaign")), "campaign/ should exist");
        assertTrue(Files.exists(Path.of("campaign", "players")), "campaign/players/ should exist");
        assertTrue(
                Files.exists(Path.of("data", "campaignconfig.txt")),
                "data/campaignconfig.txt should exist");
        new CampaignMain();
    }

    @Test
    void testDoLoginPlayer() {
        CampaignMain campaignMain = new CampaignMain();

        HibernateUtil.buildSessionFactory(ServerEntities.ALL);
        campaignMain.doLoginPlayer("TestPlayer");
        HibernateUtil.inTransaction(
                session -> {
                    SPlayer player =
                            session.createQuery("FROM SPlayer WHERE name = :name", SPlayer.class)
                                    .setParameter("name", "TestPlayer")
                                    .getSingleResult();
                    assertEquals(SPlayer.STATUS_RESERVE, player.getStatus());
                });
    }
}
