package corwur.microbrew.neo4j;
import org.neo4j.driver.v1.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CypherClient implements AutoCloseable {

    private final Driver driver;

    public CypherClient( String uri, String user, String password ) {
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

