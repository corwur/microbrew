package lychee;

import java.io.IOException;

public interface ExceptionHandler {
    void handle(Exception exception, Request request, Response response) throws IOException;
}
