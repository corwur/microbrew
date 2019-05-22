package lychee;

class PlainRequestPath implements RequestPath {

    private final String path;

    PlainRequestPath(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String get(int i) {
        return null;
    }

    @Override
    public String get(String name) {
        return null;
    }

    @Override
    public boolean containsKey(String name) {
        return false;
    }
}
