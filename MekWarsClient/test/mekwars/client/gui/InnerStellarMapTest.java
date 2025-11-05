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
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(value = MockitoExtension.class)
public class InnerStellarMapTest {

    @Mock
    private InnerStellarMap innerStellarMap;

    @Test
    void loadsConfigFromResourceFile() throws Exception {
        Path dir = Files.createTempDirectory("mapconf-test");
        Path target = dir.resolve("mapconf.xml");

        try (InputStream in = getClass().getResourceAsStream("/mapconf.xml")) {
            assertNotNull(in, "Test resource mapconf.xml not found in /test/resources/");
            Files.copy(in, target);
        }

        when(innerStellarMap.loadMapConf(anyString())).thenCallRealMethod();
        InnerStellarMapConfig conf = innerStellarMap.loadMapConf(dir.toString());
        boolean[] expected = {true, false, true, false, false, false, true, true};

        assertNotNull(conf, "Config should not be null");
        assertNotNull(conf.getDisplay(), "Display array should not be null");
        assertEquals(8, conf.getDisplay().length, "Display length mismatch — verify displayStr in test");
        assertArrayEquals(expected, conf.getDisplay());
    }
}

