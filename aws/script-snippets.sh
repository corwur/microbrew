#!/bin/bash
#TODO: Test if it works ;)

#create bucket
aws s3 create-bucket --bucket microbrewery --region eu-west-1 > microbrewery.json

#create volume
aws ec2 create-volume --volume-type gp2 --size 80 --availability-zone eu-west-1a > volume.json


#create ecr docker repositories for gateway, structure and  yeastdb
aws ecr create-repository --repository-name microbrewery/gateway > ecr-gateway.json
aws ecr create-repository --repository-name microbrewery/structure > ecr-structure.json
aws ecr create-repository --repository-name microbrewery/cytoscape-menu > ecr-cytoscape-menu.json

ECR_GATEWAY_IMAGE = $(cat ecr-gateway.json | py jq.py repository repositoryUri)
ECR_STRUCTURE_IMAGE = $(cat ecr-structure.json | py jq.py repository repositoryUri)
ECR_CYTOSCAPE_MENU_IMAGE = $(cat ecr-ecr-cytoscape-menu.json.json | py jq.py repository repositoryUri)

#tag docker images
docker tag microbere/gateway:1.0-SNAPSHOT $ECR_GATEWAY_IMAGE:1.0-SNAPSHOT
docker tag microbere/structure:1.0-SNAPSHOT $ECR_STRUCTURE_IMAGE:1.0-SNAPSHOT
docker tag microbere/yeastdb:1.0 $ECR_YEASTDB_IMAGE:1.0

#create ecs cluster
aws ecs create-cluster --cluster-name=microbrewery > microbrewery-cluster.json

#create task-definitions
aws ecs create-task-definition

#create services
aws ecs create-service --service-name=gateway > gateway-service.json
aws ecs create-service --service-name=structure > structure-service.json
aws ecs create-service --service-name=yeastdb > yeastdb-service.json


#create ecs service discovery
#create ecs services


eksctl create nodegroup \
--cluster microbrewery \
--version auto \
--name standard-workers \
--node-type t3.medium \
--node-ami auto \
--nodes 3 \
--nodes-min 1 \
--nodes-max 4 \
--region eu-west-1


#copy yeast db to aws
aws s3 cp yeast.db.dump s3://microbrewery/

#copy gff files to aws
aws s3 cp *.gff s3://microbrewery/

#deploy application with helm
helm install --name microbrewery ../deployment/helm-microbrewery

#set neo4j password with api
kubectl exec $NEO4J_SERVER_POD curl -H "Content-Type: application/json" -X POST -d '{"password":"Neo4j"}' -u neo4j:neo4j http://localhost:7474/user/neo4j/password

#start neo4j in maintenance
# stop running neo4j server
kubectl delete deployment neo4j-server

#start neo4j-server  maintenance pod SET THE VOLUME ID in neo4j-server-maintenance.yaml
kubectl apply -f ../deployment/neo4j-server-maintenance.yaml

#install aws cle
kubectl exec $NEO4J_SERVER_MAINTENANCE_POD apt-get update && apt-get install -y python && apt-get install -y python-pip && pip install awscli
#configure aws
kubectl exec $NEO4J_SERVER_MAINTENANCE_POD awscli configure

#copy database files
kubectl exec $NEO4J_SERVER_MAINTENANCE_POD cd /data && aws s3 cp s3://microbrewery/yeast.db.dump
kubectl exec $NEO4J_SERVER_MAINTENANCE_POD neo4j-admin load --from=/data/yeast.db.dump --force
kubectl exec $NEO4J_SERVER_MAINTENANCE_POD cd /data && chown -R ne04j:neo4j .

#stop neo4j maintenance
kubectl delete deployment neo4j-server-maintenance
#start neo4j server
kubectl apply -f ../deployment/helm-microbrewery/templates/deployment/neo4j-server.yaml


