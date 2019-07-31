package corwur.microbrew.neo4j.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.driver.internal.value.ListValue;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;

import nl.corwur.cytoscape.neo4j.internal.neo4j.CypherQuery;
import nl.corwur.cytoscape.neo4j.internal.neo4j.Neo4jClient;
import nl.corwur.cytoscape.neo4j.internal.neo4j.Neo4jClientException;

public class MenuLabelTask extends NodeTask{
	
	private long id;
	private Neo4jClient client;
	private final Map<String, MenuItem> menu = new HashMap<>();
	
	
	public MenuLabelTask(Neo4jClient client, long id) {
		super(client,id);
		this.client = client;
		this.id = id;
	}

	public void addMenuItemsNodes(Record record) {
		ListValue result = (ListValue) record.get("r");
		ArrayList<String> nodeLabels = new ArrayList<String>();
		result.asList().forEach(v -> nodeLabels.add("`" + (String) v + "`"));
		String nodeLabel = String.join(":", nodeLabels);
		MenuItem items;
		if (this.menu.containsKey(nodeLabel)) {
			items = this.menu.get(nodeLabel);
		}
		else {
			items = new MenuItem(nodeLabel);
		}
		items.addDirection(this.direction);
		this.menu.put(nodeLabel, items);

	}

	public void createMenuItem() throws Neo4jClientException {
		direction = Direction.IN;
		String query = "match (n)<-[]-(r) where ID(n) = " + this.id + " return distinct labels(r) as r";
		CypherQuery cypherQuery = CypherQuery.builder().query(query).build();
		StatementResult result = this.client.getResults(cypherQuery);
		result.forEachRemaining(this::addMenuItemsNodes);

		this.direction = Direction.OUT;
		query = "match (n)-[]->(r) where ID(n) = " + this.id + " return distinct labels(r) as r";
		cypherQuery = CypherQuery.builder().query(query).build();
		result = this.client.getResults(cypherQuery);
		result.forEachRemaining(this::addMenuItemsNodes);

	}
	
	public Map<String, MenuItem> getMenu() {
		return menu;
	}

}
