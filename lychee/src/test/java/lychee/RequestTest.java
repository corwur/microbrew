package lychee;

import com.sun.net.httpserver.HttpExchange;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RequestTest {

    @Mock
    private HttpExchange httpExchange;

    @Mock
    private RequestPath requestPath;

    @Before
    public void setUp() throws Exception {
        when(httpExchange.getRequestURI()).thenReturn(new URI("http://org.lychee?key=value1&key=value2"));
        when(httpExchange.getRequestBody()).thenReturn(new ByteArrayInputStream("body".getBytes()));
        when(requestPath.containsKey("path")).thenReturn(true);
        when(requestPath.get("path")).thenReturn("path");
    }

    @Test
    public void test() throws IOException {
        Request request = Request.create(httpExchange, requestPath);
        assertEquals("value1", request.get("key").get());
        assertEquals(Arrays.asList("value1","value2"), request.getAll("key"));
        assertEquals("path", request.get("path").get());
        assertTrue(request.get("some").isEmpty());
        assertEquals("body", request.getBody());
    }
}