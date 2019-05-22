package lychee;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class Server {

    private final int port;
    private final HttpServer server;
    private final Map<String, Context> contextMap;

    public Server(int port) throws IOException {
        this.port = port;
        this.contextMap = new HashMap<>();
        server = HttpServer.create(new InetSocketAddress(port),0);
    }

    public Context addContext(String contextPath) throws LycheeException {
        return addContext(contextPath, new DefaultResponseWriter(), MediaType.PLAIN_TEXT);
    }

    public Context addContext(String contextPath, ResponseWriter responseWriter, MediaType mediaType) throws LycheeException {
        if(contextMap.containsKey(contextPath)) {
            throw new LycheeException();
        }
        Context context = new Context(server.createContext(contextPath), new ContextHandler((exchange) -> new Response(exchange, responseWriter, mediaType)));
        contextMap.put(contextPath, context);
        return context;
    }

    public void start() {
        server.setExecutor(null);
        server.start();
    }

    public void stop () {
        server.stop(0);
    }

}
