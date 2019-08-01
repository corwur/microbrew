package corwur.microbrew.neo4j.tasks;

import java.util.ArrayList;

import corwur.microbrew.neo4j.tasks.NodeTask.Direction;

public class MenuItem {
	private final ArrayList<Direction> directions = new ArrayList<>();
	private final String label;
	
	public MenuItem(String label) {
		this.label = label.replaceAll("[`]+", "");
	}
	
	public void addDirection(Direction direction) {
		this.directions.add(direction);
	}
	
	public String getLabel() {
		return label;
	}
	
	public ArrayList<Direction> getDirections(){
		return directions;
	}
}
