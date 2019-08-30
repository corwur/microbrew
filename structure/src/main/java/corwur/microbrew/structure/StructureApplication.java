package corwur.microbrew.structure;

import corwur.microbrew.structure.model.GeneIdentifier;
import corwur.microbrew.structure.model.GeneIndex;
import lychee.Context;
import lychee.Lychee;
import lychee.LycheeException;
import lychee.MediaType;
import lychee.ResponseWriter;
import lychee.Server;

import java.io.IOException;

public class StructureApplication {
    public static void main(String[] args) throws IOException, LycheeException {

        ApplicationConfiguration applicationConfiguration = ApplicationConfiguration.load("application.properties");
        Server server = new Server(applicationConfiguration.getPort());
        Context context = server.addContext("/gene", (ResponseWriter) new GsonResponseWriter(), MediaType.APPLICATION_JSON);
        GeneStructureRepository geneStructureRepository = new GeneStructureRepository(applicationConfiguration);

        context.get(Lychee.regex("/gene$"), ((request, response) -> {
            var geneIdentfier = request.get("id").map(GeneIdentifier::new).orElseThrow(IllegalArgumentException::new);
            var distance = request.get("distance").map(Integer::parseInt).orElse(1);
            try {
                var genes = geneStructureRepository.getAllGenesWithinDistance(geneIdentfier, distance);
                response.ok(genes);
            } catch (ApplicationException e) {
                throw new IllegalStateException(e);
            }
        }));

        context.get(Lychee.regex("/gene/organisms$"), ((request, response) -> {
            var geneIdentfier = request.get("id").map(GeneIdentifier::new).orElseThrow(IllegalArgumentException::new);
            try {
                var genes = geneStructureRepository.getGenesToOrganisms(geneIdentfier);
                response.ok(genes);
            } catch (ApplicationException e) {
                throw new IllegalStateException(e);
            }
        }));

        context.get(Lychee.regex("/gene/search$"), ((request, response) -> {
            int limit = request.get("limit").map(Integer::parseInt).orElse(25);
            long offset = request.get("offset").map(Long::parseLong).orElse(0l);
            String search = request.get("query").orElse(".*");
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
