package lychee;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ResponseTest {

    @Mock
    private HttpExchange exchange;

    @Mock
    private ResponseWriter writer;

    @Mock
    private Headers headers;

    @Before
    public void setUp() throws Exception {
        when(exchange.getResponseBody()).thenReturn(new ByteArrayOutputStream());
        when(exchange.getResponseHeaders()).thenReturn(headers);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                ByteArrayOutputStream out = invocationOnMock.getArgument(0);
                String value = invocationOnMock.getArgument(1);
                out.writeBytes(value.getBytes());
                return null;
            }
        }).when(writer).write(any(ByteArrayOutputStream.class), anyString());

    }

    @Test
    public void ok() throws IOException {
        Response response = new Response(exchange, writer, MediaType.APPLICATION_JSON);
        response.ok("ok");
        verify(writer).write(any(ByteArrayOutputStream.class), eq("ok"));
        verify(headers).set("Content-Type", "application/json");
        verify(exchange).sendResponseHeaders(200, 2L);
    }

    @Test
    public void notFound() throws IOException {
        Response response = new Response(exchange, writer, MediaType.APPLICATION_JSON);
        response.notFound("not found");
        verify(exchange).sendResponseHeaders(404, 9L);
    }

    @Test
    public void internalServerError() throws IOException {
        Response response = new Response(exchange, writer, MediaType.APPLICATION_JSON);
        response.internalServerError("internal server error");
        verify(exchange).sendResponseHeaders(500, 21L);
    }

    @Test
    public void forbidden() throws IOException {
        Response response = new Response(exchange, writer, MediaType.APPLICATION_JSON);
        response.forbidden("forbidden");
        verify(exchange).sendResponseHeaders(403, 9L);
    }

    @Test
    public void badRequest() throws IOException {
        Response response = new Response(exchange, writer, MediaType.APPLICATION_JSON);
        response.badRequest("bad request");
        verify(exchange).sendResponseHeaders(400, 11L);
    }

    @Test
    public void unauthorized() throws IOException {
        Response response = new Response(exchange, writer, MediaType.APPLICATION_JSON);
        response.unauthorized("unauthorized");
        verify(exchange).sendResponseHeaders(401, 12L);
    }

}