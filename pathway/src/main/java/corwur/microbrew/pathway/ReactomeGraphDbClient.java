package corwur.microbrew.pathway;
import org.neo4j.graphdb.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ReactomeGraphDbClient {

    private final GraphDatabaseService gb;

    public ReactomeGraphDbClient(GraphDatabaseService gb) {
        this.gb = gb;
    }
    public List<Map<String, Object>> runQuery(final String query) throws IOException {
        try(Transaction tx = gb.beginTx()) {
            var resutlt = gb.execute(query);
            return resutlt.stream().map(this::data).collect(Collectors.toList());
        } catch (QueryExecutionException e) {
            throw new IOException(e);
        }
    }

    private Map<String, Object> data(Map<String, Object> data) {
        return data.entrySet().stream().map(this::entry).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map.Entry<String, Object> entry(Map.Entry<String, Object> entry) {
        if( entry.getValue() instanceof Node) {
            Node node = (Node) entry.getValue();
            return Map.entry(entry.getKey(), asMap(node));
        }
        if(entry instanceof Relationship) {
            Relationship relationship = (Relationship) entry.getValue();
            return Map.entry(entry.getKey(), asMap(relationship));
        }
        if(entry.getValue() instanceof Map) {
            return Map.entry( entry.getKey(), data((Map<String, Object>) entry.getValue()));
        }
        return Map.entry(entry.getKey(), entry.getValue().toString());
    }

    private Map<String, Object> asMap(Relationship relationship) {
        Map<String, Object> relationMap = new HashMap<>();
        relationMap.put("id", relationship.getId());
        relationMap.put("from", relationship.getStartNodeId());
        relationMap.put("id", relationship.getEndNodeId());
        relationMap.put("properties", relationship.getAllProperties());
        return relationMap;
    }

    private Map<String,Object> asMap(Node node) {
        Map<String, Object> nodeMap = new HashMap<>();
        List<String> labels = StreamSupport.stream(node.getLabels().spliterator(), false).map(Label::name).collect(Collectors.toList());
        nodeMap.put("labels", labels);
        nodeMap.put("id", node.getId());
        nodeMap.put("properties", node.getAllProperties());
        return nodeMap;
    }
}
