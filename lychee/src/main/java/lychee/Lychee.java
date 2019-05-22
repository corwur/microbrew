package lychee;

import java.util.function.Supplier;

public final class Lychee {

    private Lychee() {}

    public static RequestMatcher regex(String path) {
        return new RegexRequestMatcher(path);
    }
}
