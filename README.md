# Microbrew

Yeast genome browser, an example genome information application using Reactome and Adam

## Commands to compile / run

```{bash}
cd structure
mvn clean install
java -jar target/structure-1.0-SNAPSHOT-jar-with-dependencies.jar --spring.profiles.active=localhost &
cd -; cd neo4j
java -jar target/neo4j-1.0-SNAPSHOT-jar-with-dependencies.jar --spring.profiles.active=localhost &
cd -; cd gateway
mvn clean install; mvn spring-boot:run -Dspring-boot.run.profiles=localhost
cd -; cd frontend
npx brunch watch --server -P 4200
```