package lychee;

public interface RequestPath {
    String getPath();

    String get(int i);

    String get(String name);

    boolean containsKey(String name);
}
