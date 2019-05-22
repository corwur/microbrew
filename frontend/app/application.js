'use strict';
const rx = require('rxjs')
var bootstrap = require('bootstrap')
var jquery = require('jquery')
var pileup = require('pileup')
var cytoscape = require('cytoscape')
var geneStructure = require('gene-structure');
var reactome = require('reactome')

const geneIdentifierSubject = new rx.Subject()

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
                console.log(JSON.stringify(data))
                var html = '<ul class="select-gene">' +
                    data.genes.map(geneId => '<li><a href="#" onclick="App.clickGene(\'' + geneId + '\', event)">' + geneId + '</a></li>').reduce((a,b) => a + b) + '</ul>'
                list.innerHTML = html
            })
        }
    },


    getGeneStructure: function(geneIdentifier, distance) {

        const convertToCyData = function(data) {
            var cyData = []
            for(var index =0; index < data.genes.length ; index++) {
                cyData.push({ group:'nodes',  position: { x: 200, y: 200 }, data: { id:data.genes[index].geneIdentifier, weight:20 } } )
            }
            // for(var index =0; index < data.order.length ; index++) {
            //     cyData.push({ group:'edges', data: { organism:data.order[index].organism,  id:"order" + index, source:data.order[index].from, target:data.order[index].to } } )
            // }
            for(var index =0; index < data.backbone.length ; index++) {
                cyData.push({ group:'edges', data: { id:"backbone" + index, source:data.backbone[index].from, target:data.backbone[index].to } } )
            }

            return cyData;
        }
        var observable = App.geneStructure.getStructure(geneIdentifier, distance);
        observable.subscribe(data => {
            var cyData = convertToCyData(data);
            App.cy.remove('*');
            App.cy.add(cyData);
            App.cy.style().selector('edge').style(
                {
                    "curve-style": "bezier",
                    label:"data(id)"
                }
            ).update()
            App.cy.layout({ name:'grid'}).run()
        })
    },

    getPathwayInformation: function(geneIdentifier) {
        var elem = document.getElementById("reactome")
        reactome.findGene(geneIdentifier).subscribe(
            data => elem.innerHTML = JSON.stringify(data),
            error => elem.innerHTML = "No pathway data in reactome")
    },


    init(config) {
        geneIdentifierSubject.subscribe((req) => App.getGeneStructure(req.geneId,req.distance))
        geneIdentifierSubject.subscribe((req) => console.log("gene identifier is: "  + JSON.stringify(req)))
        geneIdentifierSubject.subscribe((req) => App.getPathwayInformation(req.geneId))

        App.geneStructure = geneStructure;
        App.cy = cytoscape({
            container: document.getElementById('cy'),
            elements: [
            ],

            style: [ // the stylesheet for the graph
                {
                    selector: 'node',
                    style: {
                        'background-color': '#666',
                        'label': 'data(id)'
                    }
                },

                {
                    selector: 'edge',
                    style: {
                        'width': 3,
                        'line-color': '#ccc',
                        'target-arrow-color': '#ccc',
                        'target-arrow-shape': 'triangle'
                    }
                }
            ],

            layout: {
                name: 'grid',
                rows: 1
            }
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