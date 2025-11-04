package mekwars.client.gui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(value = MockitoExtension.class)
public class InnerStellarMapTest {

    @Mock
    private InnerStellarMap innerStellarMap;

    @Test
    void loadsConfigFromResourceFile() throws Exception {
        // Create temp directory
        Path dir = Files.createTempDirectory("mapconf-test");

        // Path to destination mapconf.xml in temp dir
        Path target = dir.resolve("mapconf.xml");

        // Copy existing test resource to temp dir
        try (InputStream in = getClass().getResourceAsStream("/mapconf.xml")) {
            assertNotNull(in, "Test resource mapconf.xml not found in /test/resources/");
            Files.copy(in, target);
        }

        when(innerStellarMap.loadMapConf(anyString())).thenCallRealMethod();
        InnerStellarMapConfig conf = innerStellarMap.loadMapConf(dir.toString());

        assertNotNull(conf, "Config should not be null");
        assertNotNull(conf.getDisplay(), "Display array should not be null");
        assertEquals(8, conf.getDisplay().length, "Display length mismatch — verify displayStr in test");
    }
}

