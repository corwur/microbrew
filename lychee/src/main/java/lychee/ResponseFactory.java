package lychee;

import com.sun.net.httpserver.HttpExchange;

public interface ResponseFactory {
    Response create(HttpExchange exchange);
}
