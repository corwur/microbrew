package lychee;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class ServerTest {

    @Test
    public void test() throws IOException, LycheeException {
        Server server = new Server(0);
        assertNotNull(server.addContext("/context-1"));
        ResponseWriter writer = new DefaultResponseWriter();
        MediaType mediaType = MediaType.APPLICATION_JSON;
        assertNotNull(server.addContext("/context-2", writer, mediaType));
    }

}