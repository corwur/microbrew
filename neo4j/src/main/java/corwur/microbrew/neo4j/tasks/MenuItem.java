package corwur.microbrew.neo4j.tasks;

import java.util.ArrayList;

import corwur.microbrew.neo4j.tasks.NodeTask.Direction;

public class MenuItem {
	private final ArrayList<Direction> directions = new ArrayList<>();
	private final String nodeLabel;
	
	public MenuItem(String nodeLabel) {
		this.nodeLabel = nodeLabel;
	}
	
	public void addDirection(Direction direction) {
		this.directions.add(direction);
	}
	
	public String getNodeLabel() {
		return nodeLabel;
	}
	
	public ArrayList<Direction> getDirections(){
		return directions;
	}
}
