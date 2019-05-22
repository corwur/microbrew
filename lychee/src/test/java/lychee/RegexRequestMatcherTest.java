package lychee;

import com.sun.net.httpserver.HttpExchange;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RegexRequestMatcherTest {

    @Mock
    private HttpExchange exchange;

    @Test
    public void test() throws URISyntaxException {

        when(exchange.getRequestURI()).thenReturn(new URI("http://org.lychee/regex"));

        RegexRequestMatcher regexRequestMatcher = new RegexRequestMatcher("/regex");
        assertTrue(regexRequestMatcher.matches(exchange));
        assertNotNull(regexRequestMatcher.create(exchange));

    }

}