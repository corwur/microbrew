package lychee;

import com.sun.net.httpserver.HttpExchange;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ContextHandlerTest {

    @Mock
    private ResponseFactory responseFactory;

    @Mock
    private Response response;

    @Mock
    private HttpExchange httpExchange;

    private ContextHandler contextHandler;

    @Before
    public void setUp() throws URISyntaxException {
        when(httpExchange.getRequestURI()).thenReturn(new URI("http://lychee.org"));
        contextHandler = new ContextHandler(responseFactory);
        when(responseFactory.create(httpExchange)).thenReturn(response);
    }

    @Test
    public void handle() throws IOException {
        var route = Mockito.mock(Route.class);
        when(route.match(httpExchange)).thenReturn(true);
        contextHandler.addRoute(route);
        contextHandler.handle(httpExchange);
        Mockito.verify(route).getRequest(httpExchange);
        Mockito.verify(route).getHandler();
    }

    @Test
    public void handleDefaultNotFound() throws IOException {
        contextHandler.handle(httpExchange);
        Mockito.verify(response).notFound("Not found");
    }

    @Test
    public void handleDefaultException() throws IOException {
        var defaultHandler = Mockito.mock(Handler.class);
        doThrow(new RuntimeException("message")).when(defaultHandler).handle(any(), any());
        contextHandler.setDefaultHandler(defaultHandler);
        contextHandler.handle(httpExchange);
        Mockito.verify(response).internalServerError("message");
    }

    @Test
    public void setDefaultHandler() throws IOException {
        var defaultHandler = Mockito.mock(Handler.class);
        contextHandler.setDefaultHandler(defaultHandler);
        contextHandler.handle(httpExchange);
        Mockito.verify(defaultHandler).handle(any(Request.class), any(Response.class));
    }

    @Test
    public void setExceptionHandler() throws IOException {
        var defaultHandler = Mockito.mock(Handler.class);
        var exceptionHandler = Mockito.mock(ExceptionHandler.class);
        doThrow(new RuntimeException()).when(defaultHandler).handle(any(), any());
        contextHandler.setExceptionHandler(exceptionHandler);
        contextHandler.setDefaultHandler(defaultHandler);
        contextHandler.handle(httpExchange);
        Mockito.verify(defaultHandler).handle(any(), any());
        Mockito.verify(exceptionHandler).handle(any(RuntimeException.class), any(), any());
    }
}