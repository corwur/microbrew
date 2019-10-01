package corwur.microbrew.cytoscapemenu;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import corwur.microbrew.lychee.neo4j.Neo4jConfiguration;
import corwur.microbrew.lychee.neo4j.GsonResponseWriter;
import corwur.microbrew.lychee.neo4j.NodeIdentifier;
import lychee.ApplicationConfiguration;
import lychee.Context;
import lychee.Lychee;
import lychee.LycheeException;
import lychee.MediaType;
import lychee.Server;
import nl.corwur.cytoscape.neo4j.internal.neo4j.ConnectionParameter;
import nl.corwur.cytoscape.neo4j.internal.neo4j.Neo4jClient;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PatternOptionBuilder;
import org.apache.commons.cli.UnrecognizedOptionException;

public class CytoscapeMenuApplication {

    public static final String OPT_CONFIG = "config";
    public static final String APPLICATION_PROPERTIES = "application.properties";
    private static Logger LOGGER = Logger.getLogger(CytoscapeMenuApplication.class.getName());

    public static void main(String[] args) {

        try {
            LOGGER.info("Cytoscape menu");
            Options options = createOptions();
            CommandLine cmd = parseOptions(args, options );
            if(cmd == null) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "java -jar <jarfile> ", options );
                return;
            }
            CytoscapeMenuConfiguration configuration = getConfiguration(cmd);

            LOGGER.info("Starting http server on port: " + configuration.getPort());
            Server server = new Server(configuration.getPort());
            Context context = server.addContext("/", new GsonResponseWriter(), MediaType.APPLICATION_JSON);

            LOGGER.info("Connecting to NEO4J server: " + configuration.getNeo4jUri());
            Neo4jClient neo4jClient = new Neo4jClient();
            neo4jClient.connect(new ConnectionParameter(configuration.getNeo4jUri(), configuration.getNeo4jUser(), configuration.getNeo4jPassword().toCharArray()));

            createServerContext(context, neo4jClient);
            server.start();
        } catch (ParseException|IOException|LycheeException e) {
            LOGGER.log(Level.SEVERE, "An exception occured", e);
        }
    }

    private static void createServerContext(Context context, Neo4jClient neo4jClient) {
        context.get(Lychee.regex("/node/expand$"), ((request, response) -> {
            var nodeIdentifier = request.get("id").map(NodeIdentifier::new).orElseThrow(IllegalArgumentException::new);
            ExpandNodeTask expandNodeTask = new ExpandNodeTask(neo4jClient, nodeIdentifier.getId());
            try {
                response.ok(expandNodeTask.expand());
            } catch (Exception e) {
                response.internalServerError("Error executing expand node task");
            }
        }));
        context.get(Lychee.regex("/node/menu/labels$"), ((request, response) -> {
            var nodeIdentifier = request.get("id").map(NodeIdentifier::new).orElseThrow(IllegalArgumentException::new);
            MenuLabelTask menuLabelTask = new MenuLabelTask(neo4jClient, nodeIdentifier.getId());
            try {
                menuLabelTask.createMenuItem();
                response.ok(menuLabelTask.getMenu());
            } catch (Exception e) {
                response.internalServerError("Error executing expand menu label task");
            }
        }));
        context.get(Lychee.regex("/node/menu/edges$"), ((request, response) -> {
            var nodeIdentifier = request.get("id").map(NodeIdentifier::new).orElseThrow(IllegalArgumentException::new);
            MenuEdgesTask menuEdgesTask = new MenuEdgesTask(neo4jClient, nodeIdentifier.getId());
            try {
                menuEdgesTask.createMenuItem();
                response.ok(menuEdgesTask.getMenu());
            } catch (Exception e) {
                response.internalServerError("Error executing expand menu edge task");
            }
        }));

        context.get(Lychee.regex("/node/expand/node$"), ((request, response) -> {
            var nodeIdentifier = request.get("id").map(NodeIdentifier::new).orElseThrow(IllegalArgumentException::new);
            var label = request.get("label").map(String::new).orElseThrow(IllegalArgumentException::new);
            var direction = request.get("direction").map(String::new).orElseThrow(IllegalArgumentException::new);
            ExpandNodeTask expandNodeTask = new ExpandNodeTask(neo4jClient, nodeIdentifier.getId());
            expandNodeTask.setNode(label);
            expandNodeTask.setDirection(direction);

            try {
                response.ok(expandNodeTask.expand());
            } catch (Exception e) {
                response.internalServerError("Error executing expand menu label task");
            }
        }));

        context.get(Lychee.regex("/node/expand/edge$"), ((request, response) -> {
            var nodeIdentifier = request.get("id").map(NodeIdentifier::new).orElseThrow(IllegalArgumentException::new);
            var label = request.get("label").map(String::new).orElseThrow(IllegalArgumentException::new);
            var direction = request.get("direction").map(String::new).orElseThrow(IllegalArgumentException::new);
            ExpandNodeTask expandNodeTask = new ExpandNodeTask(neo4jClient, nodeIdentifier.getId());
            expandNodeTask.setEdge(label);
            expandNodeTask.setDirection(direction);
            try {
                response.ok(expandNodeTask.expand());
            } catch (Exception e) {
                response.internalServerError("Error executing expand menu label task");
            }
        }));
    }

    private static CytoscapeMenuConfiguration getConfiguration(CommandLine cmd) throws IOException {
        if(cmd.hasOption(OPT_CONFIG)) {
            new CytoscapeMenuConfiguration(ApplicationConfiguration.load(new File(cmd.getOptionValue(OPT_CONFIG))));
        }
        return new CytoscapeMenuConfiguration(ApplicationConfiguration.load(APPLICATION_PROPERTIES));
    }


    private static Options createOptions() {
        Options options = new Options();
        options.addOption(OPT_CONFIG, true, "location of application properties file");
        return options;
    }

    private static CommandLine parseOptions(String[] args, Options options) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            return cmd;
        } catch (UnrecognizedOptionException e) {
            return null;
        }
    }

}
