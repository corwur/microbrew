package corwur.microbrew.pathway;


import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class PathwayApplication {
    public static void main(String[] args) throws IOException {
        PathwayConfiguration pathwayConfiguration = PathwayConfiguration.load("pathway.properties");
        HttpServer server = HttpServer.create(new InetSocketAddress(pathwayConfiguration.getPort() ),0);
        ReactomeClientProvider reactomeClientProvider = new ReactomeClientProvider(pathwayConfiguration);
        server.createContext("/query", new CypherHandler(reactomeClientProvider));
        server.setExecutor(null);
        server.start();
    }
}
