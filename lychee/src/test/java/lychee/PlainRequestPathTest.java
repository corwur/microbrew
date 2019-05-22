package lychee;

import org.junit.Test;

import static org.junit.Assert.*;

public class PlainRequestPathTest {

    @Test
    public void test() {
        PlainRequestPath plainRequestPath = new PlainRequestPath("path");
        assertEquals("path", plainRequestPath.getPath());
        assertFalse(plainRequestPath.containsKey("key"));
        assertNull(plainRequestPath.get(0));
        assertNull(plainRequestPath.get("key"));

    }

}