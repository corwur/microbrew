package corwur.microbrew.neo4j;

import java.io.IOException;


import corwur.microbrew.neo4j.tasks.ExpandNodeTask;
import corwur.microbrew.neo4j.tasks.MenuEdgesTask;
import corwur.microbrew.neo4j.tasks.MenuLabelTask;
import corwur.microbrew.neo4j.tasks.NodeTask.Direction;
import lychee.Context;
import lychee.Lychee;
import lychee.LycheeException;
import lychee.MediaType;
import lychee.Server;
import nl.corwur.cytoscape.neo4j.internal.neo4j.ConnectionParameter;
import nl.corwur.cytoscape.neo4j.internal.neo4j.Neo4jClient;

public class Neo4jApplication {

	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Neo4jClient.class);
	
	
	public static void main(String[] args) throws IOException, LycheeException {
        ApplicationConfiguration applicationConfiguration = ApplicationConfiguration.load("application.properties");
        Server server = new Server(applicationConfiguration.getPort());
        Context context = server.addContext("/", new GsonResponseWriter(), MediaType.APPLICATION_JSON);
        Neo4jClient neo4jClient = new Neo4jClient();
        neo4jClient.connect(new ConnectionParameter(applicationConfiguration.getNeo4jUri(), applicationConfiguration.getNeo4jUser(), applicationConfiguration.getNeo4jPassword().toCharArray()));
        
        context.get(Lychee.regex("/node/expand/(?<nodeId>\\w*)$"), ((request, response) -> {
        	var nodeIdentifier = request.get("nodeId").map(NodeIdentifier::new).orElseThrow(IllegalArgumentException::new);
        	ExpandNodeTask expandNodeTask = new ExpandNodeTask(neo4jClient, nodeIdentifier.getId());
            try {
				response.ok(expandNodeTask.expand());
			} catch (Exception e) {
				response.internalServerError("Error executing expand node task");
			}
        }));
        context.get(Lychee.regex("/node/menu/labels/(?<nodeId>\\w*)$"), ((request, response) -> {
        	var nodeIdentifier = request.get("nodeId").map(NodeIdentifier::new).orElseThrow(IllegalArgumentException::new);
        	MenuLabelTask menuLabelTask = new MenuLabelTask(neo4jClient, nodeIdentifier.getId());
            try {
            	menuLabelTask.createMenuItem();
				response.ok(menuLabelTask.getMenu());
			} catch (Exception e) {
				response.internalServerError("Error executing expand menu label task");
			}
        }));
        context.get(Lychee.regex("/node/menu/edges/(?<nodeId>\\w*)$"), ((request, response) -> {
        	var nodeIdentifier = request.get("nodeId").map(NodeIdentifier::new).orElseThrow(IllegalArgumentException::new);
        	MenuEdgesTask menuEdgesTask = new MenuEdgesTask(neo4jClient, nodeIdentifier.getId());
            try {
            	menuEdgesTask.createMenuItem();
				response.ok(menuEdgesTask.getMenu());
			} catch (Exception e) {
				response.internalServerError("Error executing expand menu edge task");
			}
        }));

        context.get(Lychee.regex("/node/expand/node/(?<nodeId>\\w*)$"), ((request, response) -> {
        	var nodeIdentifier = request.get("nodeId").map(NodeIdentifier::new).orElseThrow(IllegalArgumentException::new);
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

        context.get(Lychee.regex("/node/expand/edge/(?<nodeId>\\w*)$"), ((request, response) -> {
        	var nodeIdentifier = request.get("nodeId").map(NodeIdentifier::new).orElseThrow(IllegalArgumentException::new);
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


        server.start();
    }

}
