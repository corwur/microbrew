package corwur.microbrew.structure;
import corwur.microbrew.neo4j.ApplicationConfiguration;
import corwur.microbrew.neo4j.ApplicationException;
import corwur.microbrew.neo4j.GsonResponseWriter;
import corwur.microbrew.structure.model.GeneIdentifier;
import corwur.microbrew.structure.model.GeneIndex;
import lychee.*;

import java.io.IOException;

public class StructureApplication {
    public static void main(String[] args) throws IOException, LycheeException {

        ApplicationConfiguration applicationConfiguration = ApplicationConfiguration.load("application.properties");
        Server server = new Server(applicationConfiguration.getPort());
        Context context = server.addContext("/gene", new GsonResponseWriter(), MediaType.APPLICATION_JSON);
        GeneStructureRepository geneStructureRepository = new GeneStructureRepository(applicationConfiguration);

        context.get(Lychee.regex("/gene/(?<geneId>\\w*)$"), ((request, response) -> {
            var geneIdentfier = request.get("geneId").map(GeneIdentifier::new).orElseThrow(IllegalArgumentException::new);
            var distance = request.get("distance").map(Integer::parseInt).orElse(1);
            try {
                var genes = geneStructureRepository.getAllGenesWithinDistance(geneIdentfier, distance);
                response.ok(genes);
            } catch (ApplicationException e) {
                throw new IllegalStateException(e);
            }
        }));

        context.get(Lychee.regex("/gene$"), ((request, response) -> {
            int limit = request.get("limit").map(Integer::parseInt).orElse(25);
            long offset = request.get("offset").map(Long::parseLong).orElse(0l);
            String search = request.get("search").orElse(".*");
            GeneIndex geneIndex = null;
            try {
                geneIndex = geneStructureRepository.getGeneIndex(search, limit, offset);
                response.ok(geneIndex);
            } catch (ApplicationException e) {
                throw new IllegalStateException(e);
            }
        }));
        server.start();
    }

}
