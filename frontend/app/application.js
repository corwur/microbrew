'use strict';
const rx = require('rxjs');
const rx_operators = require('rxjs/operators');

var bootstrap = require('bootstrap');
var jquery = require('jquery');
var pileup = require('pileup');
var cytoscape = require('cytoscape');
var cytoscapeContextMenus = require('cytoscape-menus-app');
var contextMenus = require('cytoscape-context-menus');
var graphTable = require('graph-table');
var geneStructure = require('gene-structure');
var reactome = require('reactome');
var circos = require('circos');
var Colors = require('Colors');

cytoscape.use(contextMenus, jquery ); // register extension


const geneIdentifierSubject = new rx.Subject();


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
        //observable.subscribe(App.renderCircleDiagram);

    },

    expandNodeLabelsMenu : function(event) {
    	var observable = App.cytoscapeContextMenus.getExpandNodeLabelsMenu(event).pipe(rx_operators.share());
    	observable.subscribe(App.cytoscapeContextMenus.createExpandNodeLabelsMenu); 
    },
    expandNodeEdgesMenu : function(event) {
    	var observable = App.cytoscapeContextMenus.getExpandNodeEdgesMenu(event).pipe(rx_operators.share());
    	observable.subscribe(App.cytoscapeContextMenus.createExpandNodeEdgesMenu); 
    },
    

    createCircosSelect: function() {
    	App.organisms = new Set();
        for(var index = 0; index < App.structureData.sequences.length; index ++) {
        	App.organisms.add(App.structureData.sequences[index].organism)
        }
        var select = $('#circos_organism');
        select.empty();
        App.organisms.forEach(function(o){
        	select.append($('<option>', {value:o, text: o, selected:true}))
        });
    },
    
    renderStructureGraph: function(data) {
    	App.structureData = data;
    	App.createCircosSelect();
        const convertToCyData = function(data) {
            var cyData = []
            for(var index =0; index < data.genes.length ; index++) {
            	var selected = data.genes[index].name == App.geneIdentifier;
                cyData.push({ group:'nodes',  position: { x: 200, y: 200 }, data: {
                	label:data.genes[index].name,
                	id:data.genes[index].id, 
                	weight:20, 
                	selected:selected,
                	origin:"neo4j"
                	} } )
            }
            // for(var index =0; index < data.order.length ; index++) {
            //     cyData.push({ group:'edges', data: { organism:data.order[index].organism,  id:"order" + index, source:data.order[index].from, target:data.order[index].to } } )
            // }
            for(var index =0; index < data.backbone.length ; index++) {
                cyData.push({ group:'edges', data: { 
                	id:data.backbone[index].id, 
                	source:data.backbone[index].from, 
                	target:data.backbone[index].to,
                	label: "backbone",
                	of: data.backbone[index].of,
                	weight: data.backbone[index].of,
                	origin:"neo4j"}} )
            }

            return cyData;
        }

        var cyData = convertToCyData(data);
        App.cy.remove('*');
        App.cy.add(cyData);
        
        App.cy.layout({ name: 'cose'}).run();
    },

    renderCircleDiagram: function() {
    	var seqlength = parseInt($('#circos_seqlength')[0][$('#circos_seqlength')[0].selectedIndex].value);
    	var structureData  = App.structureData;
    	if (structureData == null) return;
    	document.getElementById('circos').innerHTML = "";
    	App.circos =  new circos({
            container: document.getElementById('circos'),
            width: 600,
            height: 600,
        });
    	
        var configuration = {
            innerRadius: 250,
            outerRadius: 260,
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

    	var selectedOrganisms = new Set();
        var select = $('#circos_organism');
        for (var o=0; o < select[0].selectedOptions.length; o++) {
        	selectedOrganisms.add(select[0].selectedOptions[o].value);
        }
        
        for(var index = 0; index < structureData.sequences.length; index ++) {
        	var name = structureData.sequences[index].name + "(" + structureData.sequences[index].organism + ")"

        	//console.log(structureData.sequences[index].length);
        	
        	if (structureData.sequences[index].length >= seqlength && selectedOrganisms.has(structureData.sequences[index].organism) ) {
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
    	var pathways = [];
    	var genes = [];
        for (var index =0; index < structureData.genes.length; index++){
            var data = [];
        	// get nodes from cytoscape
        	var pathwayNodes = App.cy.nodes("#"+structureData.genes[index].id).connectedEdges("[origin ='reactome']").connectedNodes();
        	for (var source=0; source < structureData.genes[index].on.length; source++){
        		if (contigs.has(structureData.genes[index].on[source].sequenceID)) {
        			genes.push({block_id: "" + structureData.genes[index].on[source].sequenceID, 
	        					start:structureData.genes[index].on[source].start,
	        					end:structureData.genes[index].on[source].end})
        			for (var p=0; p < pathwayNodes.length; p++) {
        				if (pathwayNodes[p].data().origin == "reactome"){
	        				pathways.push({block_id: "" + structureData.genes[index].on[source].sequenceID, 
	        					start:structureData.genes[index].on[source].start,
	        					end:structureData.genes[index].on[source].end});
        				}
        			}
        		}
            	for (var target=source+1; target < structureData.genes[index].on.length; target++){
            		if (contigs.has(structureData.genes[index].on[source].sequenceID) && contigs.has(structureData.genes[index].on[target].sequenceID) ) { 
            			data.push({
            					source:{
            						id:""+structureData.genes[index].on[source].sequenceID, 
            						start:structureData.genes[index].on[source].start,
            						end:structureData.genes[index].on[source].end},
            					target:{
            						id:""+structureData.genes[index].on[target].sequenceID, 
            						start:structureData.genes[index].on[target].start,
            						end:structureData.genes[index].on[target].end},
            					})
            		}
            		
            						
            	}
            	
           
        	}
        	
            App.circos.chords("links_"+structureData.genes[index].name.replace(/[\|\.]/g,"_"), data, {
            	radius: 0.89,
            	color:colors.pick(),
                tooltipContent: function (datum, index) {
                    return "";
                  }});
        }
    	App.circos.stack('pathways', pathways, {
    		color:'red',
    		innerRadius:1.01,
    		outerRadius:1.2});
    	App.circos.stack('genes', genes, {
    		color:'blue',
    		innerRadius:0.9,
    		outerRadius:0.99});

        
   
        
        App.circos.render();
    },

    createDiagram: function(){
    	App.diagram = Reactome.Diagram.create({
            "placeHolder" : "reactome",
            "width" : 900,
            "height" : 900
    	});
    	App.diagram.onObjectSelected(function(obj) {console.log(obj)});
    },

    init(config) {
    
        App.geneStructure = geneStructure;
        App.structureData = null;
        App.organisms = new Set();
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
                        'label': 'data(label)'
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
                    selector: 'node[origin = "reactome"]',
                    style: {
                        'background-color': 'red',
                        'color' : 'red',
                    	'width':5,
                    	'height':5
                    }
                	
                },
                {
                    selector: 'node[schemaClass = "Pathway"]',
                    style: {
                        'background-color': 'black',
                        'color' : 'black',
                    	'width':8,
                    	'height':8
                    }
                	
                },
                {
                    selector: 'edge',
                    style: {
                        'width': 1,
                        'font-size' : 2,
                        'line-color': 'blue',
                        'target-arrow-color': 'blue',
                        'target-arrow-shape': 'triangle',
                        "curve-style": "bezier",
                        label:"data(label)"//,
                        //width:"mapData(weight, 0, 100, 2, 10)"
                    }
                },
                {
                    selector: 'edge[origin = "reactome"]',
                    style: {
                        'line-color': 'red',
                        'target-arrow-color': 'red',
                        'target-arrow-shape': 'triangle'
                    }
                	
                }
            ],

            layout: {
            	name: 'cose'
            }
        });

      
        App.cytoscapeContextMenu = cytoscapeContextMenus.create(App.cy);
        App.cytoscapeContextMenus = cytoscapeContextMenus
        geneIdentifierSubject.subscribe((req) => App.getGeneStructure(req.geneId,req.distance));
        geneIdentifierSubject.subscribe((req) => console.log("gene identifier is: "  + JSON.stringify(req)));
        //geneIdentifierSubject.subscribe((req) => App.getPathwayInformation(req.geneId));
        
        App.cy.on('cxttapstart','node', function(event) { 
        	App.cytoscapeContextMenus.removeAddedMenuItems();
        	if (event.target.data().origin == "neo4j") {
	        	App.expandNodeLabelsMenu(event);
	        	App.expandNodeEdgesMenu(event);
        	}
        	});
        App.cy.on('click', 'node', function(event) {
        	graphTable.nodeTable(event);
        	reactome.getPathwayInformation(event);
        });
        App.cy.on('click', 'edge', function(event) {
        	graphTable.edgeTable(event);
        });
        App.cy.on('add', 'node', function(event){
        	reactome.getPathwayInformationForCytoscape(event.target.data());
            // Add select options
            
      
        });
        
        
        
        
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