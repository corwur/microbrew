module corwur.microbrew.structure {
    requires jdk.httpserver;
    requires java.sql;
    requires neo4j.java.driver;
    requires gson;
    requires lychee;
	requires corwur.microbrew.neo4j;
    exports corwur.microbrew.structure.model;
}