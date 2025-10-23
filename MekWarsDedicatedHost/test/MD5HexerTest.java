import mekwars.dedicatedhost.protocol.MD5Hexer;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MD5HexerTest {

    private String digestUtilsMD5 = "FB19537F278C9669CD9510A174D49354";

    @Test
    public void testOldMD5EqualsNewMD5() throws IOException, NoSuchAlgorithmException {
        File file = new File("test/resources/gameoptions.xml");
        String md5HexerString = MD5Hexer.md5Hex(file);
        assertEquals(digestUtilsMD5, md5HexerString);
    }

}
