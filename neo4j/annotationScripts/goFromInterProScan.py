'''
This script processes the interproscan XML output for GO term annotations and adds them to the neo4j database.
Please change hostname, username and password. 
Change levels for more or less details (line 89)
Input: interproscan.xml [proteinFiles.fasta]+ (python3 addAllCounts2DB interproscan.xml proteinFiles*.fasta)
Output: statistics on each protein file on level 1 and 2 GO terms  
'''

import sys
from collections import defaultdict
from neo4j import GraphDatabase, basic_auth

hostname = "localhost"
username = "neo4j"
password = "Neo4j"

driver = GraphDatabase.driver("bolt://{}".format(hostname), auth=basic_auth(username, password))
session = driver.session()


currentGene = ""
processed = set()
orgNames = set()

for f in sys.argv[2:]:
    proteinMapping = {}
    orgName = f.split("/")[-1].split(".")[0]
    orgNames.add(orgName)
    print(orgName)
    for l in open(f,"r"):
        if len(l) > 0 and l[0] == ">":
            proteinMapping[l[1:].split(" ")[0].strip()] = orgName
    #reset data
    
    #session.run("create index on :GOTerm({})".format(orgName))
    for l in open(sys.argv[1], "r"):
        if "<xref id=" in l:
            currentGene = l.split('"')[1]
            print(currentGene)
        elif "<go-xref" in l and "<model" not in l:
            l = l.split('"')
            if len(l) >=8 and currentGene in proteinMapping:
                goID = l[5]
                goCat = l[1]
                goTerm = l[7]
                line = "\t".join([currentGene, goCat, goTerm, goID])
                print(goID)
                if line not in processed:
                    session.run("match (a:GOTerm {{id:'{goID}'}}), (g:gene) where '{gene}' in g.protein_id merge (g)-[:ontology]->(go:GO)-[:term]->(a) set go=a;".format(goID = goID, gene=currentGene))


session.close()
