package corwur.microbrew.neo4j;

public class NodeIdentifier {
	public final long id;

	public NodeIdentifier(String id) {
		this.id = Long.parseLong(id);
	}
	
    public long getId() {
        return id;
    }
}
