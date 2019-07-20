package corwur.microbrew.neo4j;

import java.io.IOException;

import lychee.Context;
import lychee.Lychee;
import lychee.LycheeException;
import lychee.MediaType;
import lychee.Server;

public class Neo4jApplication {

	public static void main(String[] args) throws IOException, LycheeException {
        ApplicationConfiguration applicationConfiguration = ApplicationConfiguration.load("application.properties");
        Server server = new Server(applicationConfiguration.getPort());
        Context context = server.addContext("/cytoscape", new GsonResponseWriter(), MediaType.APPLICATION_JSON);

        context.get(Lychee.regex("/gene/(?<geneId>\\w*)$"), ((request, response) -> {
            var distance = request.get("distance").map(Integer::parseInt).orElse(1);
            response.ok("200 ;-)");
        }));

        context.get(Lychee.regex("/gene$"), ((request, response) -> {
            int limit = request.get("limit").map(Integer::parseInt).orElse(25);
            long offset = request.get("offset").map(Long::parseLong).orElse(0l);
            String search = request.get("search").orElse(".*");
            response.ok("200 ;-)");
        }));
        server.start();
    }

}
