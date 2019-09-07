module corwur.microbrew.lychee.neo4j {
    requires jdk.httpserver;
    requires java.sql;
    requires neo4j.java.driver;
    requires gson;
    requires lychee;
    exports corwur.microbrew.lychee.neo4j;
}