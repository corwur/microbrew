import sys
from neo4j import GraphDatabase, basic_auth
from collections import defaultdict
# server settings
hostname = "localhost"
username = "neo4j"
password = "Neo4j"

# connect to neo4j
driver = GraphDatabase.driver("bolt://{}".format(hostname), auth=basic_auth(username, password))
session = driver.session()

#print("Script assumes GFF is ordered (chromosome, then start)!")

class Gene:
    
    def __init__(self, id):
        self.properties = defaultdict(str)
        self.properties["id"] = id
        self.properties["name"] = ""
        self.properties["protein_id"] = set()
        self.properties["product"] = set()
        
    def set(self, property):
        self.properties.update(property)
    def add(self, property, value):
        self.properties[property].add(value)
        
    def merge(self):
        self.properties['product'] = list(self.properties['product'])
        self.properties['protein_id'] = list(self.properties['protein_id'])
        return "(:gene {{ ID: '{id}', name: '{name}', protein_id: {protein_id}, product: {product} }})".format(**self.properties)
    def create(self):
        return ":gene {{ ID: '{id}'}}".format(**self.properties)

for f in sys.argv[1:]:
    # filename with annotations in GFF format
    GFF = open(f,"r")
    # filename with annotations in GFF format
    organism = ".".join(f.split(".")[:-1])
    #print(organism)

    # set containing the chromosome names in the GFF
    sequences = set()
    # set of gene IDs
    geneIDs = {}
    # admin while reading IDs
    prevID = ""
    properties = {}
    structureQuery = []
    chrQuery = []

    # step through the GFF, line by line

    #session.run("merge(m:organism {{name:'{org}'}})".format(org=organism))
    print("merge(m:organism {{name:'{org}'}});".format(org=organism))
    for l in GFF:
        # columns are separated by tabs
        l = l.split("\t")
        if len(l) == 1:
            #no tab by space?
            l = l[0].split(" ")
        if len(l)>= 9 and l[0][0] not in ["#", ">"]:
            # get annotation info from file
            chr = l[0]
            chrName = ""
            node = l[2]
        
            start = l[3]
            end = l[4]
            geneID = l[8].strip()
            if ";" in geneID:
                ids = geneID.split(";")
                for g in ids:
                    key_value = g.split("=")
                    if key_value[0] == "ID":
                        geneID = key_value[1]
                    if key_value[0] == "chromosome":
                        chrName = key_value[1]
                    properties[key_value[0].lower()] = key_value[1]
            else:
                geneID = geneID.split(",")[0]
           
            if chr not in sequences:
                #print(chr)
                #session.run("merge(m:sequence {{name:'{seq}', organism:'{org}', chromosome: '{chromosome}'}})".format(seq=chr, org=organism, chromosome=chrName))
                print("merge(m:sequence {{name:'{seq}', organism:'{org}', chromosome: '{chromosome}'}});".format(seq=chr, org=organism, chromosome=chrName))
                sequences.add(chr)
                finalQuery = []
                mergeQuery = []
                prevGene = None
                for gene in structureQuery:
                    mergeQuery.append("MERGE " + gene.merge() + ";")
                    if prevGene != None:
                        finalQuery.append("MATCH (n1{gene1}), (n2{gene2}) MERGE (n1) -[:order {{in: '{org}'}}] - >(n2);".format(org = organism, gene1= prevGene.create(), gene2=gene.create()))
                    prevGene = gene
                for i in mergeQuery:
                    #session.run(i)
                    print(i)
                for i in finalQuery:
                    #session.run(i)
                    print(i)
                for i in chrQuery:
                    #session.run(i)
                    print(i)
                structureQuery = []
                chrQuery = []

            #geneIDs.add(geneID)
            # add only specific elements to database:
            if node in ["gene"]:
                gene = Gene(geneID)
                geneIDs[geneID] = gene
                structureQuery.append(gene)
                gene.set({"name": properties["name"]})
                properties = {}
                chrQuery.append("match(m:sequence), (n:gene) where m.name ='{seq}' and m.organism = '{org}' and n.ID = '{gene}' merge (n) -[:on {{start: {start}, end: {end}}}]-> (m) ;".format(start=start, end=end, gene=geneID, seq=chr, org=organism))
            elif node in ["mRNA","lnc_RNA", "CDS"] :
                if "transcript_id" in properties:
                    structureQuery[-1].add("protein_id",properties["transcript_id"])
                if "product" in properties:
                    structureQuery[-1].add("product",properties["product"].replace("'","_"))
                if "protein_id" in properties:
                    structureQuery[-1].add("protein_id",properties["protein_id"])
    
    finalQuery = []
    mergeQuery = []
    prevGene = None
    for gene in structureQuery:
        mergeQuery.append("MERGE " + gene.merge() + ";")
        if prevGene != None:
            finalQuery.append("MATCH (n1{gene1}), (n2{gene2}) MERGE (n1) -[:order {{in: '{org}'}}] - >(n2);".format(org = organism, gene1= prevGene.create(), gene2=gene.create()))
        prevGene = gene
    for i in mergeQuery:
        #session.run(i)
        print(i)
    for i in finalQuery:
        #session.run(i)
        print(i)
    for i in chrQuery:
        #session.run(i)
        print(i)

    for s in sequences:
        #session.run("match (b:organism), (n:sequence) where b.name = '{org}' and n.name = '{seq}' and n.organism = '{org}' merge (n) -[:from]->(b) ;".format(seq=s, org=organism))
        print("match (b:organism), (n:sequence) where b.name = '{org}' and n.name = '{seq}' and n.organism = '{org}' merge (n) -[:from]->(b) ;".format(seq=s, org=organism))
    #createSequenceStructure(organism)

session.close()
