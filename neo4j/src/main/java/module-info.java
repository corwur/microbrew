module corwur.microbrew.neo4j {
    requires jdk.httpserver;
    requires java.sql;
    requires neo4j.java.driver;
    requires gson;
    requires lychee;
	requires log4j;
    exports corwur.microbrew.neo4j;
}