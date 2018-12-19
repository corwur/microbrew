#!/bin/bash
DIRECTORY=$(realpath $(dirname $0))
cd $DIRECTORY
echo $DIRECTORY

mvn clean install
mkdir -p target

cd pathway-service
trap EXIT
java -jar target\pathway-service-1.0-SNAPSHOT.jar > $DIRECTORY/target/pathway-service.log  2>&1 &
cd $DIRECTORY

cd gateway
trap EXIT
java -jar target\gateway-1.0-SNAPSHOT.jar > $DIRECTORY/target/gateway.log  2>&1 &
cd $DIRECTORY

cd frontend
npx elm-serve --proxyHost=http://localhost:8090 --proxyPrefix=/api
