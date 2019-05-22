package lychee;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class DefaultResponseWriter implements ResponseWriter {
    @Override
    public void write(OutputStream outputStream, Object value) throws IOException {
        if(value != null) {
            outputStream.write(value.toString().getBytes(Charset.defaultCharset()));
        }
    }
}
