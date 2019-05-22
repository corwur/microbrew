package lychee;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.*;


public class RegexRequestPathTest {

    Pattern pattern = Pattern.compile("^(?<path>\\w*)$");


    @Test
    public void test() {

        RegexRequestPath regexRequestPath = new RegexRequestPath("path", pattern);
        assertEquals("path", regexRequestPath.getPath());
        assertTrue(regexRequestPath.containsKey("path"));
        assertEquals("path", regexRequestPath.get("path"));
        assertEquals("path", regexRequestPath.get(0));
        assertEquals("path", regexRequestPath.get(1));
    }

    @Test(expected = IndexOutOfBoundsException.class )
    public void testInvalidIndex() {
        RegexRequestPath regexRequestPath = new RegexRequestPath("path", pattern);
        regexRequestPath.get(3);
    }

    @Test(expected = IllegalArgumentException.class )
    public void testInvalidKey() {
        RegexRequestPath regexRequestPath = new RegexRequestPath("path", pattern);
        regexRequestPath.get("something");
    }

    @Test(expected = IllegalStateException.class )
    public void testNoMatchIndex() {
        RegexRequestPath regexRequestPath = new RegexRequestPath("----", pattern);
        regexRequestPath.get(0);
    }

    @Test(expected = IllegalStateException.class )
    public void testNoMatchKey() {
        RegexRequestPath regexRequestPath = new RegexRequestPath("----", pattern);
        regexRequestPath.get("path");
    }


    @Test
    public void testNoMatchContains() {
        RegexRequestPath regexRequestPath = new RegexRequestPath("----", pattern);
        assertFalse(regexRequestPath.containsKey("path"));
    }




}