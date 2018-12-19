package corwur.microbrew.pathway;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;

public class CypherHandler implements HttpHandler {

    private final ReactomeClientProvider reactomeClientProvider;

    public CypherHandler(ReactomeClientProvider reactomeClientProvider) {
        this.reactomeClientProvider = reactomeClientProvider;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        String query = read(is); // .. read the request body

        try(ReactomeClient reactomeClient = reactomeClientProvider.get()) {
            String response = reactomeClient.runQuery(query);
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (Exception e) {
            OutputStream os = exchange.getResponseBody();
            e.printStackTrace(new PrintWriter(os));
            os.close();
        }
    }

    private String read(InputStream is) throws IOException {
        return new String(is.readAllBytes(), Charset.forName("UTF-8"));
    }
}
