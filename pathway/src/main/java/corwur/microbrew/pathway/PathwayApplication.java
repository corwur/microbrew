package corwur.microbrew.pathway;


import java.io.File;
import java.io.IOException;

import lychee.*;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class PathwayApplication {
    public static void main(String[] args) throws IOException, LycheeException {

        PathwayConfiguration pathwayConfiguration = PathwayConfiguration.load("pathway.properties");
        GraphDatabaseFactory graphDbFactory = new GraphDatabaseFactory();
        GraphDatabaseService graphDb = graphDbFactory.newEmbeddedDatabase(
                new File(pathwayConfiguration.getReactomeNeo4jDirectory()));

        ReactomeGraphDbClient reactomeGraphDbClient = new ReactomeGraphDbClient(graphDb);

        Server server = new Server(pathwayConfiguration.getPort());
        Context context = server.addContext("/query", new GsonResponseWriter(), MediaType.APPLICATION_JSON);

        context.post(Lychee.regex("/query"), (req, res) -> {
            var result = reactomeGraphDbClient.runQuery(req.getBody());
            res.ok(result);
        });

        server.start();
    }
}
