"""
Adds KEGG pathway information based on the Interproscan output to a Neo4j database. Calculates statistics on each of the groups.
python3 interproscan.xml *.faa
"""
from Bio import SeqIO
from Bio.KEGG.REST import *
from Bio.KEGG.KGML import KGML_parser
from Bio.Graphics.KGML_vis import KGMLCanvas
from Bio.Graphics.ColorSpiral import ColorSpiral
from collections import defaultdict
import sys
import os

from pprint import pprint
import xml.etree.ElementTree as ET

from neo4j import GraphDatabase, basic_auth

hostname = "localhost"
username = "neo4j"
password = "Neo4j"

driver = GraphDatabase.driver("bolt://{}".format(hostname), auth=basic_auth(username, password))
session = driver.session()


reactome = set()
reactomeNames= defaultdict(str)
reactomeToGene = defaultdict(list)
reactomeToGeneOrg = {}
orgList = []

currentGene = ""
# open reactome file other
# expand org list with files and extract IDs:

proteinMapping = {}
for f in sys.argv[2:]:
    orgName = f.split("/")[-1].split(".")[0]
    print(orgName)
    orgList.append(orgName)
    for l in open(f,"r"):
        if len(l) > 0 and l[0] == ">":
            proteinMapping[l[1:].split(" ")[0].strip()] = orgName
            
# <pathway-xref db="Reactome" id="R-HSA-6798695" name="Neutrophil degranulation"/>
for l in open(sys.argv[1],"r"):
    if "<xref id=" in l:
        currentGene = l.split('"')[1]
    elif '<pathway-xref db="Reactome"' in l:
        reactomeID = l.split('"')[3]
        reactomeName= l.split('"')[5]
        reactomeNames[reactomeID] = reactomeName
        reactome.add(reactomeID)
        if len(reactomeID) > 1:
            if currentGene in proteinMapping: # skip those which are not found in the protein files
                org = proteinMapping[currentGene]
                if org not in reactomeToGeneOrg:
                    reactomeToGeneOrg[org] = defaultdict(list)
                if currentGene not in reactomeToGeneOrg[org][reactomeID]:
                    reactomeToGeneOrg[org][reactomeID].append(currentGene)

# process all found reactome pathways
for k in reactome:
    print("Processing: {}".format(k))
    # load current pathway
    session.run("merge (a:reactome{{id:'{}', name:\"{}\"}})".format(k, reactomeNames[k]))

for orgName in reactomeToGeneOrg.keys():
    for reactomeID in reactomeToGeneOrg[orgName].keys():
        for protein in reactomeToGeneOrg[orgName][reactomeID]:
            session.run("match (a:reactome {{id:'{id}'}}), (g:gene) where '{protein}' in g.protein_id merge (a)<-[:is]-(r:reactomePathway)<-[:partOf]-(g) set r=a;".format(id=reactomeID, protein=protein)) 
        
session.close()


        
        
    
        
