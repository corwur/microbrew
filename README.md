# Microbrew

Yeast genome browser, an example genome information application using Reactome and Adam

Add [this database](https://www.dropbox.com/s/aj82hxjgvl4f0x0/yeast.db.dump?dl=0)  to your neo4j instance (neo4j-admin load).

## Commands to compile / run

```{bash}
cd structure
mvn clean install -Plocalhost
java -jar target/structure-1.0-SNAPSHOT-jar-with-dependencies.jar &
cd -; cd neo4j
mvn clean install -Plocalhost
java -jar target/neo4j-1.0-SNAPSHOT-jar-with-dependencies.jar &
cd -; cd gateway
mvn clean install; mvn spring-boot:run -Dspring-boot.run.profiles=localhost
cd -; cd frontend
npx brunch watch --server -P 4200
```
