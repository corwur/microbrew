import sys
from neo4j import GraphDatabase, basic_auth
from collections import defaultdict
import json
# server settings
hostname = "localhost"
username = "neo4j"
password = "Neo4j"

minRelativeScore = 0.0

# connect to neo4j
driver = GraphDatabase.driver("bolt://{}".format(hostname), auth=basic_auth(username, password))
session = driver.session()

def toStr(var):
    return "'" + str(var) + "'"

#SequenceName    class   complete        start   end     NB-ARC  2-NBLRR-Signal  MotifList
class Hit:
    
    def __init__(self, line):
        l = line.strip().split('\t')
        for i in range(len(l)):
            l[i] = l[i].strip()
        self.properties = {}
        self.properties['SequenceName'] = toStr(l[0])
        self.properties['class'] = toStr(l[1])
        self.properties['complete'] = toStr(l[2])
        self.properties['start'] = l[3].split(":")[1]
        self.properties['end'] = l[4].split(":")[1]
        self.properties['`NB-ARC`'] = toStr(l[5])
        self.properties['`2-NBLRR-Signal`'] = toStr(l[6])
        self.properties['MotifList'] = "''" #[int(x) for x in l[7].split(',')]
        
        
    def query(self):
        return self.properties["SequenceName"]

    def NLR(self):
        return self.properties["class"]
    
    
    def __str__(self):
        #print(self.properties)
        return (" -[:fromMotif {{ " + ",".join([k + ": {" + k + "}" for k in self.properties.keys()]) + " }}] - ").format(**self.properties)

bestHits = {}

for f in open(sys.argv[1], "r"):
    if "," in f:
        hit = Hit(f)
        bestHits[hit.query()] = hit

for i in bestHits:
    #print(bestHits[i].relativeScore())
    print(bestHits[i].query())
    query = "match (n1:gene ) where {n1_protein_id} in n1.protein_id merge (n1) ".format(
            n1_protein_id = bestHits[i].query()) + str(bestHits[i]) + " (:NLR {{class: {NLR}}});".format(NLR=bestHits[i].NLR())
    session.run(query)
session.close()
