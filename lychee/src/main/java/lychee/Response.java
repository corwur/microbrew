package lychee;

import com.sun.net.httpserver.HttpExchange;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Response {

    private final HttpExchange exchange;
    private MediaType mediaType = MediaType.PLAIN_TEXT;
    private ResponseWriter responseWriter;

    Response(HttpExchange exchange, ResponseWriter responseWriter, MediaType mediaType) {
        this.exchange = exchange;
        this.responseWriter = responseWriter;
        this.mediaType = mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public <T> void ok(T t) throws IOException {
        var out = new ByteArrayOutputStream();
        responseWriter.write(out, t);
        write(out.toByteArray(), 200);
    }

    public <T> void forbidden(T t) throws IOException {
        var out = new ByteArrayOutputStream();
        responseWriter.write(out, t);
        write(out.toByteArray(), 403);
    }

    public <T> void unauthorized(T t) throws IOException {
        var out = new ByteArrayOutputStream();
        responseWriter.write(out, t);
        write(out.toByteArray(), 401);
    }

    public <T> void notFound(T t) throws IOException {
        var out = new ByteArrayOutputStream();
        responseWriter.write(out, t);
        write(out.toByteArray(), 404);
    }

    public <T> void internalServerError(T t) throws IOException {
        var out = new ByteArrayOutputStream();
        responseWriter.write(out, t);
        write(out.toByteArray(), 500);
    }

    public <T> void badRequest(T t) throws IOException {
        var out = new ByteArrayOutputStream();
        responseWriter.write(out, t);
        write(out.toByteArray(), 400);
    }


    private void write(byte[] bytes, int responseCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", mediaType.getMediaType());
        exchange.sendResponseHeaders(responseCode, bytes.length);
        var outputStream = exchange.getResponseBody();
        outputStream.write(bytes);
        outputStream.flush();
        outputStream.close();
    }
}
