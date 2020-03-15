package corwur.microbrew.cytoscapemenu.model;

import java.util.HashMap;
import java.util.Map;

import corwur.microbrew.cytoscapemenu.model.MenuItem;
import corwur.microbrew.cytoscapemenu.model.NodeTask;
import org.neo4j.driver.internal.value.StringValue;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;

import nl.corwur.cytoscape.neo4j.internal.neo4j.CypherQuery;
import nl.corwur.cytoscape.neo4j.internal.neo4j.Neo4jClient;
import nl.corwur.cytoscape.neo4j.internal.neo4j.Neo4jClientException;

public class MenuEdgesTask extends NodeTask {

	private final Map<String, MenuItem> menu = new HashMap<>();
	
	public MenuEdgesTask(Neo4jClient client, long id) {
		super(client, id);
	}
	
	public void addMenuItemsEdges(Record record) {
		StringValue result = (StringValue) record.get("r");
		String edgeLabel = result.asString();
		MenuItem items;
		if (this.menu.containsKey(edgeLabel)) {
			items = this.menu.get(edgeLabel);
		}
		else {
			items = new MenuItem(edgeLabel);
		}
		items.addDirection(this.direction);
		this.menu.put(edgeLabel, items);

	}

	public void createMenuItem() throws Neo4jClientException {
		direction = Direction.IN;
		String query = "match (n)<-[r]-() where ID(n) = " + this.id + " return distinct type(r) as r";
		CypherQuery cypherQuery = CypherQuery.builder().query(query).build();
		StatementResult result = this.client.getResults(cypherQuery);
		result.forEachRemaining(this::addMenuItemsEdges);

		this.direction = Direction.OUT;
		query = "match (n)-[r]->() where ID(n) = " + this.id + " return distinct type(r) as r";
		cypherQuery = CypherQuery.builder().query(query).build();
		result = this.client.getResults(cypherQuery);
		result.forEachRemaining(this::addMenuItemsEdges);

	}
	
	public Map<String, MenuItem> getMenu() {
		return menu;
	}
	

}
