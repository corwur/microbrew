package lychee;

import org.junit.Test;

import static org.junit.Assert.*;

public class KeyValuePairTest {

    @Test
    public void test() {
        var keyValuePair = KeyValuePair.parse("a%20=b%20");
        assertEquals("a%20", keyValuePair.getKey());
        assertEquals("b%20", keyValuePair.getValue());
        var decodedKeyValuePair = keyValuePair.decode();
        assertNotNull(decodedKeyValuePair);
        assertEquals("a ", decodedKeyValuePair.getKey());
        assertEquals("b ", decodedKeyValuePair.getValue());

    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmpty() {
        KeyValuePair.parse("");
    }

    @Test(expected = NullPointerException.class)
    public void testNull() {
        KeyValuePair.parse(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid() {
        KeyValuePair.parse("abc");
    }
}