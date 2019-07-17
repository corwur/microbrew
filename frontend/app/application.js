'use strict';
const rx = require('rxjs')
const rx_operators = require('rxjs/operators')

var bootstrap = require('bootstrap')
var jquery = require('jquery')
var pileup = require('pileup')
var cytoscape = require('cytoscape')
var geneStructure = require('gene-structure');
var reactome = require('reactome')
var circos = require('circos')
const geneIdentifierSubject = new rx.Subject()

var Colors = class  {
	constructor() {
		this.colors = ["#A4262C", "#8F7034","#407855","#038387","#CA5010","#0078D4","#40587C","#4052AB","#854085","#8764B8","#737373","#867365"];
		this.currentColor = 0;
	}
	pick(){
		var col = this.colors[this.currentColor];
		this.currentColor++;
		if (this.currentColor >= this.colors.length) {
			this.currentColor = 0;
		}
		return col;
	}
}

const App = {


    selectGene: function(elem, event) {
        event.returnValue = false;
        var geneId = elem.elements[0].value
        var distance = elem.elements[1].value ? elem.elements[1].value : 1
        geneIdentifierSubject.next({ geneId:geneId, distance:distance})
        return false
    },
    clickGene: function(geneId, event) {
        event.returnValue = false;
        document.getElementById("selectGeneId").value=geneId
        document.getElementById("gene-id-autocomplete").innerHTML=""
        return false
    },
    findGene: function(elem) {
        var geneId = elem.value;
        if(geneId && geneId.length >=1) {
            geneStructure.findGene(geneId + ".*").subscribe(data => {
                var list = document.getElementById("gene-id-autocomplete")
                //console.log(JSON.stringify(data))
                var html = '<ul class="select-gene">' +
                    data.genes.map(geneId => '<li><a href="#" onclick="App.clickGene(\'' + geneId + '\', event)">' + geneId + '</a></li>').reduce((a,b) => a + b) + '</ul>'
                list.innerHTML = html
            })
        }
    },


    getGeneStructure: function(geneIdentifier, distance) {
    	App.geneIdentifier = geneIdentifier;
        var observable = App.geneStructure.getStructure(geneIdentifier, distance).pipe(rx_operators.share());

        observable.subscribe(App.renderStructureGraph);
        observable.subscribe(App.renderCircleDiagram)

    },

    showPathway: function(pathway) {
    	for (var index=0; index < 1; index++) { //pathway.length
			var name = pathway[index].stId;
	        App.diagram.loadDiagram(name);
	        App.diagram.onDiagramLoaded(function (loaded) {
	            console.info("Loaded ", loaded);
	            //diagram.flagItems("FYN");
	            if (loaded == name) App.diagram.selectItem(name);
	        });
    	}    	
    },
    
    showPathways: function(gene) {
    	for (var index=0; index < gene.results[0].entries.length; index++){
    		reactome.getPathway(gene.results[0].entries[index].stId).subscribe(
    	            data => App.showPathway(data));
    	}
    },
    
    getPathwayInformation: function(geneIdentifier) {
        var elem = document.getElementById("reactome");
        reactome.findGene(geneIdentifier).subscribe(
            data => App.showPathways(data),
            error => elem.innerHTML = "No pathway data in reactome");
    },

    
    renderStructureGraph: function(data) {
        const convertToCyData = function(data) {
            var cyData = []
            for(var index =0; index < data.genes.length ; index++) {
            	var selected = data.genes[index].geneIdentifier == App.geneIdentifier;
                cyData.push({ group:'nodes',  position: { x: 200, y: 200 }, data: { id:data.genes[index].geneIdentifier, weight:20, selected:selected } } )
            }
            // for(var index =0; index < data.order.length ; index++) {
            //     cyData.push({ group:'edges', data: { organism:data.order[index].organism,  id:"order" + index, source:data.order[index].from, target:data.order[index].to } } )
            // }
            for(var index =0; index < data.backbone.length ; index++) {
                cyData.push({ group:'edges', data: { 
                	id:"backbone" + index, 
                	source:data.backbone[index].from, 
                	target:data.backbone[index].to,
                	label: "backbone: " + data.backbone[index].of,
                	of: data.backbone[index].of,
                	weight: data.backbone[index].of}} )
            }

            return cyData;
        }

        var cyData = convertToCyData(data);
        App.cy.remove('*');
        App.cy.add(cyData);
        
        App.cy.layout({ name: 'cose'}).run();

    },

    renderCircleDiagram: function(structureData) {
    	document.getElementById('circos').innerHTML = "";
    	App.circos =  new circos({
            container: document.getElementById('circos'),
            width: 800,
            height: 600,
        });
    	
        var configuration = {
            innerRadius: 250,
            outerRadius: 300,
            cornerRadius: 10,
            gap: 0.01, // in radian
            labels: {
                display: true,
                position: 'center',
                size: '14px',
                color: '#000000',
                radialOffset: 20,
            },
            ticks: {
                display: false,
                color: 'grey',
                spacing: 10000000,
                labels: true,
                labelSpacing: 10,
                //labelSuffix: 'Mb',
                //labelDenominator: 1000000,
                labelDisplay0: true,
                labelSize: '40px',
                labelColor: '#000000',
                labelFont: 'default',
                majorSpacing: 5,
                size: {
                    minor: 2,
                    major: 5,
                }
            },
            events: {}
        }
        
        //organisms
        var data = [];
        var contigs = new Set();
        
        for(var index = 0; index < structureData.sequences.length; index ++) {
        	var name = structureData.sequences[index].name + "(" + structureData.sequences[index].organism + ")"
        	//console.log(structureData.sequences[index].length);
        	if (structureData.sequences[index].length > 100000) {
	            data.push( { len: structureData.sequences[index].length, 
	            			color: "#8dd3c7", 
	            			label: name, 
	            			id: ""+structureData.sequences[index].id} );
	        	contigs.add(structureData.sequences[index].id);
	        }
        }
        //console.log(contigs);	
        App.circos.layout(data, configuration);
        var colors = new Colors(); 
        for (var index =0; index < structureData.genes.length; index++){
            data = [];
        	for (var source=0; source < structureData.genes[index].on.length; source++){
            	for (var target=source+1; target < structureData.genes[index].on.length; target++){
            		if (contigs.has(structureData.genes[index].on[source].id) && contigs.has(structureData.genes[index].on[target].id) ) 
            		data.push({
            					source:{
            						id:""+structureData.genes[index].on[source].id, 
            						start:structureData.genes[index].on[source].start,
            						end:structureData.genes[index].on[source].end},
            					target:{
            						id:""+structureData.genes[index].on[target].id, 
            						start:structureData.genes[index].on[target].start,
            						end:structureData.genes[index].on[target].end},
            					})
            		
            						
            	}
        	}
            App.circos.chords("links_"+structureData.genes[index].geneIdentifier.replace(/[\|\.]/g,"_"), data, {color:colors.pick(),
                tooltipContent: function (datum, index) {
                    return "";
                  }});
        }
        App.circos.render();
    },

    createDiagram: function(){
    	App.diagram = Reactome.Diagram.create({
            "placeHolder" : "reactome",
            "width" : 900,
            "height" : 500
    	});
    },

    init(config) {
    
        App.geneStructure = geneStructure;
        App.cy = cytoscape({
            container: document.getElementById('cy'),
            elements: [
            ],

            style: [ // the stylesheet for the graph
                {
                    selector: 'node',
                    style: {
                    	'font-size':4,
                    	'width':6,
                    	'height':6,
                        'background-color': "green",
                        'label': 'data(id)'
                    }
                },
                {
                    selector: 'node[?selected]',
                    style: {
                        'background-color': 'blue',
                        'color' : 'blue'
                    }
                	
                },
                {
                    selector: 'edge',
                    style: {
                        'width': 1,
                        'font-size' : 2,
                        'line-color': 'lightblue',
                        'target-arrow-color': 'lightblue',
                        'target-arrow-shape': 'triangle',
                        "curve-style": "bezier",
                        label:"data(label)",
                        width:"mapData(weight, 0, 100, 2, 10)"
                    }
                }
            ],

            layout: {
            	name: 'cose'
            }
        });

        geneIdentifierSubject.subscribe((req) => App.getGeneStructure(req.geneId,req.distance));
        geneIdentifierSubject.subscribe((req) => console.log("gene identifier is: "  + JSON.stringify(req)));
        geneIdentifierSubject.subscribe((req) => App.getPathwayInformation(req.geneId));
        //App.cy.on('tap','node', function(event) { console.log(event.target.id()); App.getPathwayInformation(event.target.id());});
        
        console.log('App initialized.');


        // var pileup = require('pileup');
        var p = pileup.create(config.node, {
            range: {contig: 'chr17', start: 7512384, stop: 7512544},
            tracks: [
                {
                    viz: pileup.viz.genome(),
                    isReference: true,
                    data: pileup.formats.twoBit({
                        url: 'http://www.biodalliance.org/datasets/hg19.2bit'
                    }),
                    name: 'Reference'
                },
                {
                    viz: pileup.viz.pileup(),
                    data: pileup.formats.bam({
                        url: '/test-data/synth3.normal.17.7500000-7515000.bam',
                        indexUrl: '/test-data/synth3.normal.17.7500000-7515000.bam.bai'
                    }),
                    cssClass: 'normal',
                    name: 'Alignments'
                }
            ]
        });


    }
};

module.exports = App;