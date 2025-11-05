import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

final class MD5HexerTest {

    @Test
    void testOldMD5EqualsNewMD5() throws Exception {
        var url = getClass().getResource("/gameoptionstest.xml");
        assertNotNull(url, "resource missing (put file under src/test/resources)");
        byte[] raw = Files.readAllBytes(Paths.get(url.toURI()));

        var md = MessageDigest.getInstance("MD5");
        String cleanedMD5 = toUpperCaseHex(md.digest(carriageReturnLineFeedToLineFeed(raw)));

        assertEquals("7C7F6627E11BF3BA373EEFC9E56CD16C", cleanedMD5);
    }

    /**
     * Convert byteArray to format for matching
     * @param byteArray
     * @return
     */
    private static String toUpperCaseHex(byte[] byteArray) {
        var sb = new StringBuilder(byteArray.length * 2);
        for (byte theByte : byteArray) {
            sb.append(String.format("%02X", theByte));
        }
        return sb.toString();
    }

    /**
     * Converts carriage return / line feed (CRLF) to just line feed
     * Windows uses crlf and linux (github) uses lf, so when running MD5 on the same file, they end up
     * different when read as bytes by the os.
     * @param raw - the file's bytes
     * @return
     */
    private static byte[] carriageReturnLineFeedToLineFeed(byte[] raw) {
        var s = new String(raw, StandardCharsets.UTF_8).replace("\r\n", "\n");
        return s.getBytes(StandardCharsets.UTF_8);
    }
}
