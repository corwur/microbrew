package corwur.microbrew.structure;

import corwur.microbrew.lychee.neo4j.Neo4jConfiguration;
import lychee.ApplicationConfiguration;
import lychee.ApplicationException;
import corwur.microbrew.lychee.neo4j.GsonResponseWriter;
import corwur.microbrew.structure.model.GeneIdentifier;
import corwur.microbrew.structure.model.GeneIndex;
import lychee.Context;
import lychee.Lychee;
import lychee.LycheeException;
import lychee.MediaType;
import lychee.ResponseWriter;
import lychee.Server;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StructureApplication {

    private static final Logger LOGGER = Logger.getLogger(StructureApplication.class.getName());
    private static final String OPT_CONFIG = "config";
    private static final String APPLICATION_PROPERTIES = "application.properties";

    public static void main(String[] args)  {

        LOGGER.info("Starting structure app");
        try {
            Options options = createOptions();
            CommandLine cmd = parseOptions( args, options);
            if(cmd == null) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "java -jar <jarfile> ", options );
                return;
            }
            StructureConfiguration configuration = getConfiguration(cmd);

            LOGGER.log(Level.INFO, "Starting http server on port {0,number,#}", configuration.getPort());
            Server server = new Server(configuration.getPort());

            LOGGER.log(Level.INFO, "NEO4J Server uri is {0}", configuration.getNeo4jUri());
            GeneStructureRepository geneStructureRepository = new GeneStructureRepository(configuration);
            createServerContext(server, geneStructureRepository);

            server.start();
        } catch (ParseException|IOException|LycheeException e) {
            LOGGER.log(Level.SEVERE, "An exception occurred", e);
        }
    }

    private static void createServerContext(Server server, GeneStructureRepository geneStructureRepository) throws LycheeException {
        Context context = server.addContext("/gene", (ResponseWriter) new GsonResponseWriter(), MediaType.APPLICATION_JSON);
        context.get(Lychee.regex("/gene$"), ((request, response) -> {
            var geneIdentfier = request.get("id").map(GeneIdentifier::new).orElseThrow(IllegalArgumentException::new);
            var distance = request.get("distance").map(Integer::parseInt).orElse(1);
            try {
                var genes = geneStructureRepository.getAllGenesWithinDistance(geneIdentfier, distance);
                response.ok(genes);
            } catch (ApplicationException e) {
                LOGGER.log(Level.SEVERE, "Error handling request", e);
                throw new IllegalStateException(e);
            }
        }));

        context.get(Lychee.regex("/gene/organisms$"), ((request, response) -> {
            var geneIdentfier = request.get("id").map(GeneIdentifier::new).orElseThrow(IllegalArgumentException::new);
            try {
                var genes = geneStructureRepository.getGenesToOrganisms(geneIdentfier);
                response.ok(genes);
            } catch (ApplicationException e) {
                LOGGER.log(Level.SEVERE, "Error handling request", e);
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
                LOGGER.log(Level.SEVERE, "Error handling request", e);
                throw new IllegalStateException(e);
            }
        }));
    }


    private static StructureConfiguration getConfiguration(CommandLine cmd) throws IOException {
        if(cmd.hasOption(OPT_CONFIG)) {
            new StructureConfiguration(ApplicationConfiguration.load(new File(cmd.getOptionValue(OPT_CONFIG))));
        }
        return new StructureConfiguration(ApplicationConfiguration.load(APPLICATION_PROPERTIES));
    }

    private static Options createOptions() {
        Options options = new Options();
        options.addOption(OPT_CONFIG, true,"location of application properties file");
        return options;
    }

    private static CommandLine parseOptions(String[] args, Options options) throws ParseException {
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            return cmd;
        } catch (UnrecognizedOptionException e) {
            return null;
        }
    }
}
