import sys
from neo4j import GraphDatabase, basic_auth
from collections import defaultdict
import json
# server settings
hostname = "localhost"
username = "neo4j"
password = "Neo4j"

minRelativeScore = 7.5

# connect to neo4j
driver = GraphDatabase.driver("bolt://{}".format(hostname), auth=basic_auth(username, password))
session = driver.session()

#reset alignment links:
session.run("match (:gene)-[r:alignsWith]-(:gene) set r.best = true;")
session.run("match (g1:gene)-[a1:alignsWith]-(g2:gene) match (g2)-[a2:alignsWith]-(g3) where g1 <> g3 and g1.organism = g3.organism and a1.score_alignmentLength < a2.score_alignmentLength set a1.best = false;")

# get all nodes at the beginning of a sequence:
startNodes = session.run("match (g1:gene) -[:order]-> (g2:gene) where not (:gene)-[:order]->(g1) return g1.ID as ID;")
for startNode in startNodes:
    print(startNode["ID"])
    allNodes = session.run("match (g1:gene {{ID: '{id}' }}) -[:order*0..]-> (g2:gene) return g2.ID as ID;".format(id = startNode["ID"]))

    # find all genes on the same sequence using order link        
    for allNode in allNodes:
        #  find nodes it aligns to:
        alignsWith = session.run("match(g1:gene {{ID: '{id}' }}) -[r:alignsWith]- (g2:gene) where r.score_alignmentLength >= {score} and r.best return g2.ID as ID".format(id = allNode["ID"], score = minRelativeScore))
        # is there an alignment?
        if alignsWith.peek() is None:
            session.run("match (g1:gene {{ID: '{id}' }}) MERGE (g1) <-[:abstracts]-(:pangene);".format(id=allNode["ID"]))
            session.run("match (p1:pangene) -[abstracts]- (g:gene) -[:order]-> (g1:gene)<-[:abstracts]-(p2:pangene) where g1.ID = '{id}' merge (p1)-[:backbone {{score:{score} }}]-(p2)".format(id=allNode["ID"], score=minRelativeScore))

        for aligns in alignsWith:
            pan = session.run("match (g1:gene)<-[:abstracts]-(g2:pangene) where g1.ID in ['{id}','{id2}'] return ID(g2) as pan;".format(id=allNode["ID"], id2=aligns["ID"]))
            if pan.peek() is not None:
                for p in pan:
                    #  if a pangene aligns to the target, link it to this gene
                    session.run("match (g1:gene), (g2:pangene) where g1.ID in ['{id}','{id2}'] and ID(g2) = {pan} MERGE (g1) <-[:abstracts]-(g2);".format(id=allNode["ID"], id2=aligns["ID"], pan=p["pan"]))
                    #print("match (g1:gene), (g2:pangene) where g1.ID in ['{id}','{id2}'] and g2.id = {{pan}} MERGE (g1) <-[:abstracts]-(g2);".format(id=allNode["ID"], id2=aligns["ID"], pan=p["pan"]))
            else:
                #  if there is no link, create a new pangene
                session.run("match (g1:gene {{ID: '{id}' }}), (g2:gene {{ID: '{id2}'}}) MERGE (g1) <-[:abstracts]-(:pangene)-[:abstracts]->(g2);".format(id=allNode["ID"], id2=aligns["ID"]))
                #print("match (g1:gene {{ID: '{id}' }}), (g2:gene {{ID: '{id2}'}}) MERGE (g1) <-[:abstracts]-(:pangene)-[:abstracts]->(g2);".format(id=allNode["ID"], id2=aligns["ID"]))
            #  link created or linked pangene to any pangene linked to the previous gene
            session.run("match (p1:pangene) -[abstracts]- (g:gene) -[:order]-> (g1:gene)<-[:abstracts]-(p2:pangene) where g1.ID in ['{id}','{id2}'] and ID(p1) <> ID(p2) merge (p1)-[:backbone {{score:{score} }} ]-(p2)".format(id=allNode["ID"], id2=aligns["ID"], score=minRelativeScore))
