package corwur.microbrew.cytoscapemenu.model;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import nl.corwur.cytoscape.neo4j.internal.graph.Graph;
import nl.corwur.cytoscape.neo4j.internal.neo4j.CypherQuery;
import nl.corwur.cytoscape.neo4j.internal.neo4j.Neo4jClient;
import nl.corwur.cytoscape.neo4j.internal.neo4j.Neo4jClientException;


public class ExpandNodeTask extends NodeTask {
	
    private String edge;
    private String node;
    
	public ExpandNodeTask(Neo4jClient client, long id) {
		super(client, id);
		this.id = id;
		this.edge = null;
		this.node = null;
		this.direction = ExpandNodeTask.Direction.BIDIRECTIONAL;
		this.client = client;
	}
	
	public void setNode(String node) {
		this.node = node;
	}

	public void setEdge(String edge) {
		this.edge = edge;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	
	public Graph expand() throws InterruptedException, ExecutionException {
		String directionLeft = this.direction == Direction.IN ? "<" : "";
		String directionRight = this.direction == Direction.OUT ? ">" : "";
		
		String query;
		if (this.edge == null && this.node == null) {
			query = "match p=(n)" + directionLeft +  "-[r]-" + directionRight + "() where ID(n) = " + id +" return p"; 
		}
		else if (this.node == null){
			query = "match p=(n)" + directionLeft +  "-[:"+this.edge+"]-" + directionRight + "() where ID(n) = " + id +" return p";
		}
		else {
			query = "match p=(n)" + directionLeft +  "-[r]-" + directionRight + "(:" + this.node + ") where ID(n) = " + id +" return p"; 
		}
		CypherQuery cypherQuery = CypherQuery.builder().query(query).build();
		
        CompletableFuture<Graph> result = CompletableFuture.supplyAsync(() -> getGraph(cypherQuery));

        if (result.isCompletedExceptionally()) {
            throw new IllegalStateException("Error executing cypher query");
        }

        Graph graph = result.get();

        return graph;
    }


    private Graph getGraph(CypherQuery query) {
        try {
            return client.getGraph(query);
        } catch (Neo4jClientException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

	public void setDirection(String direction) {
    	if (direction == "BIDIRECTIONAL")
    		setDirection(Direction.BIDIRECTIONAL);
    	if (direction == "IN")
    		setDirection(Direction.IN);
    	if (direction == "OUT")
    		setDirection(Direction.OUT);
	}
}
