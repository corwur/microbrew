package lychee;

import org.junit.Test;

import static org.junit.Assert.*;

public class LycheeTest {

    @Test
    public void regex() {
        var requestMatcher = Lychee.regex(".*");
        assertNotNull(requestMatcher);
    }
}