package lychee;

import com.sun.net.httpserver.HttpExchange;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RouteTest {

    @Mock
    private RequestMatcher requestMatcher;
    @Mock
    private Handler handler;
    @Mock
    private HttpExchange exchange;

    @Mock
    private Request request;

    @Before
    public void setUp() throws Exception {
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(requestMatcher.matches(exchange)).thenReturn(true);
        when(requestMatcher.create(exchange)).thenReturn(request);
    }

    @Test
    public void test() {
        Route route = new Route(Method.GET, requestMatcher, handler);
        assertNotNull(route.getHandler());
        assertNotNull(route.getRequest(exchange));
        assertTrue(route.match(exchange));
    }

    @Test
    public void testNoMatchMethod() {
        when(exchange.getRequestMethod()).thenReturn("POST");
        Route route = new Route(Method.GET, requestMatcher, handler);
        assertFalse(route.match(exchange));
    }

    @Test
    public void testNoMatchExchange() {
        when(requestMatcher.matches(exchange)).thenReturn(false);
        Route route = new Route(Method.GET, requestMatcher, handler);
        assertFalse(route.match(exchange));
    }

}