import mekwars.dedicatedhost.protocol.MD5Hexer;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MD5HexerTest {

    private String digestUtilsMD5 = "4AB4494CDB7F872285069E8E237B6881";

    @Test
    public void testOldMD5EqualsNewMD5() throws IOException, NoSuchAlgorithmException {
        File file = new File("test/resources/gameoptionstest.xml");
        String md5HexerString = MD5Hexer.md5Hex(file);
        assertEquals(digestUtilsMD5, md5HexerString);
    }

}
