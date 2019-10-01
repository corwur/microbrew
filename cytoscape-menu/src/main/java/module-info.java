module corwur.microbrew.cytoscapemenu {
    requires jdk.httpserver;
    requires java.sql;
    requires neo4j.java.driver;
    requires gson;
    requires lychee;
    requires commons.cli;
	requires corwur.microbrew.lychee.neo4j;
    requires cytoscape.neo4j.plugin;
    exports corwur.microbrew.cytoscapemenu;
}