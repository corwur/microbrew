package lychee;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContextTest {

    @Mock
    private HttpContext httpContext;
    @Mock
    private ContextHandler contextHandler;
    @Mock
    private RequestMatcher requestMatcher;
    @Mock
    private Handler handler;
    @Mock
    private HttpExchange exchange;

    @Before
    public void setUp() throws Exception {
        when(requestMatcher.matches(any())).thenReturn(true);
    }

    @Test
    public void get() {
        when(exchange.getRequestMethod()).thenReturn("GET");
        var context = new Context(httpContext, contextHandler);
        context.get(requestMatcher, handler);
        verify(httpContext).setHandler(contextHandler);
        verifyRoute();
    }

    @Test
    public void post() {
        when(exchange.getRequestMethod()).thenReturn("POST");
        var context = new Context(httpContext, contextHandler);
        context.post(requestMatcher, handler);
        verify(httpContext).setHandler(contextHandler);
        verifyRoute();
    }

    @Test
    public void put() {
        when(exchange.getRequestMethod()).thenReturn("PUT");
        var context = new Context(httpContext, contextHandler);
        context.put(requestMatcher, handler);
        verify(httpContext).setHandler(contextHandler);
        verifyRoute();
    }

    @Test
    public void delete() {
        when(exchange.getRequestMethod()).thenReturn("DELETE");
        var context = new Context(httpContext, contextHandler);
        context.delete(requestMatcher, handler);
        verify(httpContext).setHandler(contextHandler);
        verifyRoute();
    }

    private void verifyRoute() {
        ArgumentCaptor<Route> captor = ArgumentCaptor.forClass(Route.class);
        verify(contextHandler).addRoute(captor.capture());
        assertTrue(captor.getValue().match(exchange));
    }

}