package corwur.microbrew.cytoscapemenu.model;

import nl.corwur.cytoscape.neo4j.internal.neo4j.Neo4jClient;

public abstract class NodeTask {
	public enum Direction {
		IN,
		OUT,
        BIDIRECTIONAL
    }
	protected Direction direction;
	protected long id;
	protected Neo4jClient client;
    
	public NodeTask(Neo4jClient client, long id) {
		this.id = id;
		this.direction = NodeTask.Direction.BIDIRECTIONAL;
		this.client = client;
	}

}
