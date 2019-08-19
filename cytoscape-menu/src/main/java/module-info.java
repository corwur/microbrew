module corwur.microbrew.cytoscapemenu {
    requires jdk.httpserver;
    requires java.sql;
    requires transitive neo4j.java.driver;
    requires gson;
    requires lychee;
    requires transitive cytoscape.neo4j.plugin;
	requires log4j;
	requires corwur.microbrew.neo4j;
    exports corwur.microbrew.cytoscapemenu;
}