package lychee;

import com.sun.net.httpserver.HttpExchange;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RegexRequestMatcher implements RequestMatcher {

    private final Pattern pattern;

    RegexRequestMatcher(String path) {
        pattern = Pattern.compile(path);
    }

    @Override
    public boolean matches(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        Matcher matcher = pattern.matcher(path);
        return matcher.matches();
    }

    public Request create(HttpExchange exchange) {
        return Request.create(exchange, new RegexRequestPath(exchange.getRequestURI().getPath(), pattern));
    }

}
