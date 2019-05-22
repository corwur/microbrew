package lychee;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RegexRequestPath implements RequestPath {

    private final String path;
    private final Matcher matcher;
    private final boolean matches;

    RegexRequestPath(String path, Pattern pattern) {
        this.path = path;
        this.matcher = pattern.matcher(path);
        this.matches = this.matcher.matches();
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String get(int i) {
        if(matches) {
            return matcher.group(i);
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public String get(String name) {
        if(matches) {
            return matcher.group(name);
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public boolean containsKey(String name) {
        try {
            return matches && matcher.group(name) != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
