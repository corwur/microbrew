import sys
from neo4j.v1 import GraphDatabase, basic_auth
from collections import defaultdict
import json
# server settings
hostname = "localhost"
username = "annotation"
password = "annotation"

minRelativeScore = 6.0

# connect to neo4j
driver = GraphDatabase.driver("bolt://{}".format(hostname), auth=basic_auth(username, password))
session = driver.session()

def toStr(var):
    return "'" + str(var) + "'"

class Hit:
    
    def __init__(self, line):
        l = line.strip().split(',')
        for i in range(len(l)):
            l[i] = l[i].strip()
        self.properties = {}
        self.properties['query'] = toStr(l[0])
        self.properties['target'] = toStr(l[1])
        self.properties['queryStart'] = l[2]
        self.properties['queryEnd'] = l[3]
        self.properties['targetStart'] = l[4]
        self.properties['targetEnd'] = l[5]
        self.properties['score'] = l[6]
        self.properties['matches'] = l[7]
        self.properties['mismatches'] = l[8]
        self.properties['gaps'] = l[9]
        self.properties['alignmentLength'] = l[10]
        self.properties['score_alignmentLength'] = l[11]
        self.properties['queryLength'] = l[12]
        self.properties['targetLength'] = l[13]
        self.properties['score_queryLength'] = l[14]
        self.properties['score_targetLength'] = l[15]
        
    def query(self):
        return self.properties["query"]
    
    def target(self):
        return self.properties["target"]

    
    def score(self):
        return float(self.properties['score'])
    
    def relativeScore(self):
        return float(self.properties['score_alignmentLength'])
    
    def __str__(self):
        #print(self.properties)
        return (" -[:alignsWith {{ " + ",".join([k + ": {" + k + "}" for k in self.properties.keys()]) + " }}] -> ").format(**self.properties)

bestHits = {}

for f in open(sys.argv[1], "r"):
    if "," in f:
        hit = Hit(f)
        if hit.relativeScore() >= minRelativeScore:
            if hit.query() not in bestHits:
                bestHits[hit.query()] = hit
            elif bestHits[hit.query()].score() < hit.score():
                bestHits[hit.query()] = hit

for i in bestHits:
    #print(bestHits[i].relativeScore())
    query = "match (n1:gene {{protein_id: {n1_protein_id}}}), (n2:gene {{protein_id: {n2_protein_id}}}) merge (n1) ".format(
            n1_protein_id = bestHits[i].query(),
            n2_protein_id = bestHits[i].target()) + str(bestHits[i]) + "(n2) return n1,n2"
    print(query)
session.close()
