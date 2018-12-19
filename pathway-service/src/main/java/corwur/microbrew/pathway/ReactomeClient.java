package corwur.microbrew.pathway;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import org.neo4j.driver.v1.*;

import java.io.IOException;
import java.io.StringWriter;

public class ReactomeClient implements AutoCloseable {

    private final Driver driver;

    public ReactomeClient( String uri, String user, String password ) {
        driver = GraphDatabase.driver( uri, AuthTokens.basic( user, password ) );
    }
    public String runQuery( final String query) throws IOException {
        try ( Session session = driver.session() ) {
            StatementResult result = session.writeTransaction((tx) -> tx.run(query));
            Gson gson = new Gson();
            StringWriter stringWriter = new StringWriter();
            JsonWriter jsonWriter = gson.newJsonWriter(stringWriter);

            jsonWriter.beginArray();
            while(result.hasNext()) {
                Record record = result.next();
                JsonElement jsonElement  = gson.toJsonTree(record.asMap());
                gson.toJson(jsonElement, jsonWriter);
            }
            jsonWriter.endArray();
            return stringWriter.toString();
        }
    }
    @Override
    public void close() {
        driver.close();
    }
}
