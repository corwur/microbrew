# Microbrew

Yeast genome browser, an example genome information application using Reactome and Adam

Add [this database](https://www.dropbox.com/s/aj82hxjgvl4f0x0/yeast.db.dump?dl=0)  to your neo4j instance (neo4j-admin load).

## Commands to compile / run

```{bash}
cd lychee; mvn clean install; cd ..
cd lychee-neo4j; mvn clean install; cd ..

cd structure
mvn clean install
java -jar target/structure-1.0-SNAPSHOT-jar-with-dependencies.jar &
cd -; cd cytoscape-menu
mvn clean install 
java -jar target/cytoscape-menu-1.0-SNAPSHOT-jar-with-dependencies.jar &
cd -; cd gateway
mvn clean install; mvn spring-boot:run 
cd -; cd frontend
npx brunch watch --server -P 4200
```
