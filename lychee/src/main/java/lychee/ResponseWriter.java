package lychee;

import java.io.IOException;
import java.io.OutputStream;

public interface ResponseWriter {
    void write(OutputStream outputStream, Object value) throws IOException;
}
