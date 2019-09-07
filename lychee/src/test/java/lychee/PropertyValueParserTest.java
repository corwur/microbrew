package lychee;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class PropertyValueParserTest {

    @Test
    public void parse() {
        var env = new HashMap<String, String>();
        env.put("BAR", "bar");
        env.put("FOO", "foo");
        var propertyValueParser = new PropertyValueParser(env);
        assertEquals("default", propertyValueParser.parse("${VAR:#{default}}"));
        assertEquals("foo", propertyValueParser.parse("${FOO:#{default}}"));
        assertEquals("var1-var2", propertyValueParser.parse("${VAR1:#{var1}}-${VAR2:#{var2}}"));
        assertEquals("", propertyValueParser.parse("${NOTFOUND}"));
        assertEquals("bar-foo", propertyValueParser.parse("${BAR}-${FOO}"));
        assertEquals("-bar-foo-", propertyValueParser.parse("-${BAR}-${FOO}-"));
        assertEquals("-bar-foo", propertyValueParser.parse("-${BAR}-${FOO}"));
        assertEquals("bar-foo-", propertyValueParser.parse("${BAR}-${FOO}-"));
        assertEquals("barfoo", propertyValueParser.parse("${BAR}${FOO}"));
        assertEquals("-bar", propertyValueParser.parse("-${BAR}"));
        assertEquals("foo-", propertyValueParser.parse("${FOO}-"));
        assertEquals("foo", propertyValueParser.parse("${FOO}"));
        assertEquals("abc", propertyValueParser.parse("abc"));
        assertEquals("", propertyValueParser.parse(""));
        assertEquals("abc", propertyValueParser.parse("abc"));
        assertEquals("abc", propertyValueParser.parse("abc"));
    }


}