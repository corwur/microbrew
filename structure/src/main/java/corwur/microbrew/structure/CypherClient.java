package corwur.microbrew.structure;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CypherClient implements AutoCloseable {

    private final Driver driver;

    public CypherClient(String uri, String user, String password ) {
        driver = GraphDatabase.driver( uri, AuthTokens.basic( user, password ) );
    }
    public List<Map<String, Object>> runQuery(final String query) throws IOException {
        List<Map<String, Object>> result = new ArrayList<>();
        try ( Session session = driver.session() ) {
            StatementResult statementResult = session.writeTransaction((tx) -> tx.run(query));
            while(statementResult.hasNext()) {
                Record record = statementResult.next();
                result.add(record.asMap());
            }
            return result;
        }
    }
    public List<Map<String, Object>> runQuery(final String query, Map<String, Object> params) throws IOException {
        List<Map<String, Object>> result = new ArrayList<>();
        try ( Session session = driver.session() ) {
            StatementResult statementResult = session.writeTransaction((tx) -> tx.run(query, params));
            while(statementResult.hasNext()) {
                Record record = statementResult.next();
                result.add(record.asMap());
            }
            return result;
        }
    }


    @Override
    public void close() {
        driver.close();
    }
}

