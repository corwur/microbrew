package corwur.microbrew.structure;

import corwur.microbrew.neo4j.ApplicationConfiguration;
import corwur.microbrew.neo4j.ApplicationException;
import corwur.microbrew.neo4j.CypherClient;
import corwur.microbrew.structure.model.*;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Relationship;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GeneStructureRepository {

    private static final String GET_ALL_GENES = "MATCH (g:gene) WHERE g.name =~ $search RETURN g SKIP $offset LIMIT $limit";
    private static final String GET_GENE_COUNT = "MATCH (g:gene) WHERE g.name =~ $search RETURN COUNT(g) AS gene_count";
    private static final String GET_GENE = "MATCH (g:gene) WHERE g.name=$geneId RETURN g";
    private static final String GET_GENE_ON = "MATCH (g:gene)-[r:on]-(s:sequence) WHERE g.name=$geneId RETURN g,r,s";
    private static final String GET_ALL_GENES_WITHIN_DISTANCE = "MATCH (n:gene)-[r:backbone*..%d]-(g:gene) WHERE n.name=$geneId RETURN DISTINCT g";
    private static final String GET_ORGANISMS = "MATCH (g:gene)-[r:order]->(t:gene) WHERE g.name IN $genes WITH r.in AS r_in MATCH (o:organism) WHERE o.name=r_in RETURN DISTINCT o";
    private static final String GET_ALL_SEQUENCES = "MATCH (g:gene)-[r:on]->(s:sequence) WHERE g.name IN $genes RETURN g, r, s ORDER BY s.organisme, s.name";
    private static final String GET_ORDER_LINKS = "MATCH (g:gene)-[r:order]->(t:gene) WHERE g.name IN $genes and t.name in $genes RETURN g,r,t";
    private static final String GET_BACKBONE_LINKS = "MATCH (g:gene)-[r:backbone]->(t:gene) WHERE g.name IN $genes and t.name in $genes RETURN g,r,t";

    private String uri;
    private String user;
    private String password;

    public GeneStructureRepository(ApplicationConfiguration applicationConfiguration) {
        this.uri = applicationConfiguration.getNeo4jUri();
        this.user = applicationConfiguration.getNeo4jUser();
        this.password = applicationConfiguration.getNeo4jPassword();
    }

    public GeneIndex getGeneIndex(String search, int limit, long offset) throws ApplicationException {
        try (CypherClient cypherClient = new CypherClient(uri, user, password)) {
            var genes = getAllGenes(search, limit, offset, cypherClient);
            var geneCount = getGeneCount(search, cypherClient);
            return new GeneIndex(genes, geneCount);
        } catch (IOException e) {
            throw new ApplicationException();
        }
    }

    public GeneStructure getAllGenesWithinDistance(GeneIdentifier geneIdentifier, int distance) throws ApplicationException {
        try (CypherClient cypherClient = new CypherClient(uri, user, password)) {
            var root = getGeneById(geneIdentifier, cypherClient).orElseThrow(ApplicationException::new);
            var genes = getGenes(geneIdentifier, distance, cypherClient);
            genes.put(root.name, root);
            var organisms = getOrganisms(genes, cypherClient);
            var sequences = getSequences(genes, cypherClient);
            var order = getOrderLinks(genes, cypherClient);
            var backbone = getBackboneLinks(genes, cypherClient);

            for (Gene gene : genes.values()) {
                var on = getOnLinks(gene.name, cypherClient);
                gene.on.addAll(on);
            }

            return new GeneStructure(
                    genes.values(),
                    organisms.values(),
                    sequences.values(),
                    order,
                    backbone
            );
        } catch (IOException e) {
            throw new ApplicationException();
        }
    }

    private List<String> getAllGenes(String search, int limit, long offset, CypherClient cypherClient) throws IOException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("limit", limit);
        parameters.put("offset", offset);
        parameters.put("search", search);
        var result = cypherClient.runQuery(GET_ALL_GENES, parameters);
        return result.stream().map(this::createGene).map(Gene::getGeneIdentifier).collect(Collectors.toList());
    }

    private long getGeneCount(String search, CypherClient cypherClient) throws IOException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("search", search);
        var result = cypherClient.runQuery(GET_GENE_COUNT, parameters);
        return result.stream().findFirst().map(row -> (Long) row.get("gene_count")).orElse(-1l);
    }


    private Optional<Gene> getGeneById(GeneIdentifier geneIdentifier, CypherClient cypherClient) throws IOException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("geneId", geneIdentifier.getName());
        var result = cypherClient.runQuery(GET_GENE, parameters);
        return result.stream().map(this::createGene).findFirst();
    }


    private Map<String, Gene> getGenes(GeneIdentifier geneIdentifier, int distance, CypherClient cypherClient) throws IOException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("geneId", geneIdentifier.getName());
        var result = cypherClient.runQuery(String.format(GET_ALL_GENES_WITHIN_DISTANCE, distance), parameters);
        return result.stream().map(this::createGene).collect(Collectors.toMap(Gene::getGeneIdentifier, Function.identity()));
    }

    private List<On> getOnLinks(String geneIdentifier, CypherClient cypherClient) throws IOException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("geneId", geneIdentifier);
        var result = cypherClient.runQuery(GET_GENE_ON, parameters);
        return result.stream().map(this::createOnLink).collect(Collectors.toList());
    }

    private Map<String, Organism> getOrganisms(Map<String, Gene> genes, CypherClient cypherClient) throws IOException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("genes", genes.keySet());
        var result = cypherClient.runQuery(GET_ORGANISMS, parameters);
        return result.stream().map(this::createOrganism).collect(Collectors.toMap(Organism::getName, Function.identity()));
    }

    private Map<String, Sequence> getSequences(Map<String, Gene> genes, CypherClient cypherClient) throws IOException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("genes", genes.keySet());
        var result = cypherClient.runQuery(GET_ALL_SEQUENCES, parameters);
        return result.stream().map(this::createSequence).distinct().collect(Collectors.toMap(Sequence::getName, Function.identity()));
    }

    private List<Order> getOrderLinks(Map<String, Gene> genes, CypherClient cypherClient) throws IOException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("genes", genes.keySet());
        var result = cypherClient.runQuery(GET_ORDER_LINKS, parameters);
        return result.stream().map(this::createOrderLink).collect(Collectors.toList());
    }

    private List<Backbone> getBackboneLinks(Map<String, Gene> genes, CypherClient cypherClient) throws IOException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("genes", genes.keySet());
        var result = cypherClient.runQuery(GET_BACKBONE_LINKS, parameters);
        return result.stream().map(this::createBackboneLink).collect(Collectors.toList());

    }

    private Gene createGene(Map<String, Object> result) {
        return new Gene(
                get(result, "g", "name").map(Value::asString).orElse(""),
                get(result, "g", "id").map(Value::asLong).orElse(10l)
        );
    }

    private On createOnLink(Map<String, Object> result) {
        return new On(
                get(result, "g", "id").map(Value::asLong).orElse(0l),
                get(result, "s", "id").map(Value::asLong).orElse(0l),
                getRelationship(result, "r", "id", Value::asLong).orElse(0l),
                getRelationship(result, "r", "start", Value::asLong).orElse(0l),
                getRelationship(result, "r", "end", Value::asLong).orElse(0l)
        );
    }

    private Order createOrderLink(Map<String, Object> result) {
        return new Order(
                get(result, "g", "id").map(Value::asLong).orElse(0l),
                get(result, "t", "id").map(Value::asLong).orElse(0l),
                getRelationship(result, "r", "id", Value::asLong).orElse(0l),
                getRelationship(result, "r", "in", Value::asString).orElse("N/A"));
    }

    private Backbone createBackboneLink(Map<String, Object> result) {
        return new Backbone(
                get(result, "g", "id").map(Value::asLong).orElse(0l),
                get(result, "t", "id").map(Value::asLong).orElse(0l),
                getRelationship(result, "r", "id", Value::asLong).orElse(0l),
                getRelationship(result, "r", "of", Value::asLong).orElse(0l)
        );
    }

    private Sequence createSequence(Map<String, Object> result) {
        return new Sequence(
                get(result, "s", "name").map(Value::asString).orElse(""),
                get(result, "s", "organism").map(Value::asString).orElse(""),
                get(result, "s", "length").map(Value::asLong).orElse(0l),
                get(result, "s", "id").map(Value::asLong).orElse(0l)
        );
    }

    private Organism createOrganism(Map<String, Object> result) {
        return new Organism(
                get(result, "o", "name").map(Value::asString).orElse(""),
                get(result, "o", "id").map(Value::asLong).orElse(0l)
        );
    }

    private Optional<Value> get(Map<String, Object> row, String column, String field) {
        Node node = (Node) row.get(column);
        if (node.containsKey(field)) {
            return Optional.of(node.get(field));
        } else {
            return Optional.empty();
        }
    }

    private <T> Optional<T> getRelationship(Map<String, Object> row, String column, String field, Function<Value, T> fmap) {
        Relationship relationship = (Relationship) row.get(column);
        if (relationship.asMap().containsKey(field)) {
            return Optional.of(relationship.get(field)).map(fmap);
        } else {
            return Optional.empty();
        }
    }
}
