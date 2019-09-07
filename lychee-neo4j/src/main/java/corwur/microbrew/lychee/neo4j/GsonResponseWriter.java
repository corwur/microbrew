package corwur.microbrew.lychee.neo4j;

import com.google.gson.Gson;
import lychee.ResponseWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class GsonResponseWriter implements ResponseWriter {

    @Override
    public void write(OutputStream outputStream, Object value) throws IOException {
        Gson gson = new Gson();
        String json = gson.toJson(value);
        outputStream.write(json.getBytes(Charset.defaultCharset()));
    }
}
