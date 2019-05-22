package lychee;


import com.sun.net.httpserver.HttpExchange;

interface RequestMatcher {
    boolean matches(HttpExchange exchange);
    Request create(HttpExchange exchange);
}
