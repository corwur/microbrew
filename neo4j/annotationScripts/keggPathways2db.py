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


kegg = defaultdict(list)

ecToGene = defaultdict(list)
ecToGeneOrg = {}
KOToEC = defaultdict(list)
KOToGene = defaultdict(list)

stats = {}
orgList = []

currentGene = ""
# open kegg file other
# expand org list with files and extract IDs:

proteinMapping = {}
for f in sys.argv[2:]:
    orgName = f.split("/")[-1].split(".")[0]
    print(orgName)
    orgList.append(orgName)
    for l in open(f,"r"):
        if len(l) > 0 and l[0] == ">":
            proteinMapping[l[1:].split(" ")[0].strip()] = orgName
    
for l in open(sys.argv[1],"r"):
    if "<xref id=" in l:
        currentGene = l.split('"')[1]
    elif '<pathway-xref db="KEGG"' in l:
        keggID = l.split('"')[3].split("+")
        if len(keggID) > 1:
            if currentGene in proteinMapping: # skip those which are not found in the protein files
                kegg[keggID[0]].extend([x for x in keggID[1:] if x not in kegg[keggID[0]]])
                org = proteinMapping[currentGene]
                for ec in keggID[1:]:
                    if org not in ecToGeneOrg:
                        ecToGeneOrg[org] = defaultdict(list)
                    if currentGene not in ecToGeneOrg[org][ec]:
                        ecToGeneOrg[org][ec].append(currentGene)

# process all found kegg pathways
for k in kegg:
    print("Processing: {}".format(k))
    # load current pathway
    pathway = []
    pathwayInDB = session.run("match(a:pathway{{number:'{number}'}}) return a.number;".format(number=k))
    if (pathwayInDB.peek() is None and int(k) < 1000):
        try:
            print("Getting pathway from KEGG")
            pathway = ET.parse(kegg_get("ec{}".format(k), "kgml")).getroot()
            session.run("merge (a:pathway{{number:'{}', name:'{}', title:'{}', image:'{}'}})".format(pathway.attrib['number'], pathway.attrib['name'], pathway.attrib['title'], pathway.attrib['image']))
        except:
            pass
    #print("merge (a:pathway{{number:'{}', name:'{}', title:'{}', image:'{}'}}) return a".format(pathway.attrib['number'], pathway.attrib['name'], pathway.attrib['title'], pathway.attrib['image']))

    for p in pathway:
        if 'name' in p.attrib and p.attrib['name'] != 'undefined':
            if p.tag == 'entry':
                p0 = defaultdict(str)
                p0['x'] = 0
                p0['y'] = 0
                p0['width'] = 0
                p0['height'] = 0
                p0.update(p[0].attrib)
                title = p0['title']
                
                session.run("merge (a:{}{{name:'{}', title:'{}'}})".format(p.attrib['type'], p.attrib['name'], title))
                session.run("match (a:{}{{name:'{}'}}), (p:pathway{{number:'{}'}}) merge (a)-[r:in{{id: {}, graphics:'{}', x:{}, y:{}, width:{}, height:{}}}]->(p)".format(
                    p.attrib['type'],
                    p.attrib['name'],
                    pathway.attrib['number'],
                    p.attrib['id'],
                    p0['type'],
                    p0['x'],
                    p0['y'],
                    p0['width'],
                    p0['height']))


                
        if p.tag == 'relation':
            session.run("match (a)-[ar:in]->(p:pathway{{number:'{}'}}),(b)-[br:in]->(p:pathway{{number:'{}'}})  where ar.id = {} and br.id ={} merge (a)-[:{}]->(b)".format(
                pathway.attrib['number'],pathway.attrib['number'], p.attrib['entry1'],  p.attrib['entry2'], p.attrib['type']))

        if p.tag == 'reaction':
            session.run("match (a)-[ar:in]->(p:pathway{{number:'{}'}}),(b)-[br:in]->(p:pathway{{number:'{}'}})  where ar.id = {} and br.id ={} merge (a)-[:{}]->(b)".format(
                pathway.attrib['number'],pathway.attrib['number'], p[0].attrib['id'],  p[1].attrib['id'], 'reaction'))

session.run("match (m:map), (p:pathway) where m.name = p.name merge (m)-[:is]->(p)")
            
for orgName in ecToGeneOrg.keys():

    for ec in ecToGeneOrg[orgName].keys():
        for protein in ecToGeneOrg[orgName][ec]:
            session.run("match (a:enzyme {{name:'ec:{name}'}}), (g:gene) where '{protein}' in g.protein_id merge (a)<-[:is]-(ec:EC)<-[:has]-(g) set ec=a;".format(name=ec, protein=protein)) 
        
session.close()


        
        
    
        
