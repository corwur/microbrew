#!/bin/bash

TAG=1.0-SNAPSHOT
AWS_REGISTRY=465129930157.dkr.ecr.eu-west-1.amazonaws.com
modules=("cytoscape-menu","gateway","structure")

aws ecr get-login --no-include-email > login.json
eval $(cat login.json)


for module in "${modules[@]}"
do:
   docker tag microbrewery/$module "$AWS_REGISTRY/microbrewery-$module:$TAG"
   docker push "$AWS_REGISTRY/microbrewery-$module:$TAG"
done
