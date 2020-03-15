import {filter, map, share} from "rxjs/operators";
import cytoscape from 'cytoscape';
import {CytoscapeContextMenus} from './cytoscape-menus-app';
import {GraphTable} from './graph-table';
import {Subject} from "rxjs";

export class GeneStructureComponent {

    constructor() {
        this.cy = null
        this.subject = new Subject()
    }

    init(config) {

        this.cy = cytoscape({
            container: document.getElementById(config.id),
            elements: [],
            style: [ // the stylesheet for the graph
                {
                    selector: 'node',
                    style: {
                        'font-size': 4,
                        'width': 6,
                        'height': 6,
                        'background-color': "green",
                        'label': 'data(label)'
                    }
                },
                {
                    selector: 'node[?selected]',
                    style: {
                        'background-color': 'blue',
                        'color': 'blue'
                    }
                },
                {
                    selector: 'node[origin = "reactome"]',
                    style: {
                        'background-color': 'red',
                        'color': 'red',
                        'width': 5,
                        'height': 5
                    }

                },
                {
                    selector: 'node[schemaClass = "Pathway"]',
                    style: {
                        'background-color': 'black',
                        'color': 'black',
                        'width': 8,
                        'height': 8
                    }

                },
                {
                    selector: 'edge',
                    style: {
                        'width': 1,
                        'font-size': 2,
                        'line-color': 'blue',
                        'target-arrow-color': 'blue',
                        'target-arrow-shape': 'triangle',
                        "curve-style": "bezier",
                        label: "data(label)"//,
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
        })
        this.cytoscapeContextMenus = new CytoscapeContextMenus(this.cy, this.subject)
        this.graphTable = new GraphTable()
        const _this = this
        this.cy.on('cxttapstart', 'node', function (event) {
            console.log("cxttapstart")
            _this.cytoscapeContextMenus.removeAddedMenuItems();
            if (event.target.data().origin == "neo4j") {
                _this.subject.next({ "expandNodeLabelsMenu": event.target._private.data})
                _this.subject.next({ "expandNodeEdgesMenu": event.target._private.data})
            }
        });
        this.cy.on('click', 'node', function (event) {
            _this.graphTable.nodeTable(event);
            //_this.setPathwayInformation(event);
        });
        this.cy.on('click', 'edge', function (event) {
            _this.graphTable.edgeTable(event);
        });
        this.cy.on('add', 'node', function (event) {
            //reactome.getPathwayInformationForCytoscape(event.target.data());
            console.log('add node')
        });
        this.cy.on('remove', 'node', function (event) {
            console.log('remove node')
            //_this.model.next({removeNode:event.target.data()})
        });

    }

    addStructure(data) {
        console.log("add structure " + JSON.stringify(data))
        var genes = [];
        for (var j = 0; j < data.genes.length; j++) {
            var found = 0

            while (found < this.genes.length && this.genes[found].id != data.genes[j].id) {
                found += 1;
            }
            if (found == this.genes.length) {
                genes.push(data.genes[j]);
            }
        }
        this.genes = this.genes.concat(genes);

        var organisms = [];
        for (var j = 0; j < data.organisms.length; j++) {
            var found = 0

            while (found < this.organisms.length && this.organisms[found].id != data.organisms[j].id) {
                found += 1;
            }
            if (found == this.organisms.length) {
                organisms.push(data.organisms[j]);
            }
        }
        this.organisms = this.organisms.concat(organisms);


        var sequences = [];
        for (var j = 0; j < data.sequences.length; j++) {
            var found = 0

            while (found < this.sequences.length && this.sequences[found].id != data.sequences[j].id) {
                found += 1;
            }
            if (found == this.sequences.length) {
                sequences.push(data.sequences[j]);
            }
        }
        this.sequences = this.sequences.concat(sequences);
    }

    renderStructureGraph(data) {
        const convertToCyData = function (data) {
            var cyData = []
            for (var index = 0; index < data.genes.length; index++) {
                var selected = data.genes[index].name == data.geneIdentifier;
                cyData.push({
                    group: 'nodes', position: {x: 200, y: 200}, data: {
                        label: data.genes[index].name,
                        id: data.genes[index].id,
                        weight: 20,
                        selected: selected,
                        origin: "neo4j",
                        type:"gene",
                        name: data.genes[index].name
                    }
                })
            }
            for (var index = 0; index < data.backbone.length; index++) {
                cyData.push({
                    group: 'edges', data: {
                        id: data.backbone[index].id,
                        source: data.backbone[index].from,
                        target: data.backbone[index].to,
                        label: "backbone",
                        of: data.backbone[index].of,
                        weight: data.backbone[index].of,
                        origin: "neo4j"
                    }
                })
            }

            return cyData;
        }

        const cyData = convertToCyData(data);
        this.cy.remove('*');
        this.cy.add(cyData);
        this.cy.layout({name: 'cose'}).run();

    }

    createExpandNodeEdgesMenu(data) {
        this.cytoscapeContextMenus.createExpandNodeEdgesMenu(data)
    }

    expandNodeMenu(data) {
        console.log("expandNodeMenu: " + JSON.stringify(data))
        this.cytoscapeContextMenus.createExpandNodeMenu(data)
    }


    expandNode(data) {
        console.log("expandNode: " + JSON.stringify(data))
        this.addStructure(data)
    }

    getExpandNodeLabelsMenuAsObservable() {
        return this.bind("expandNodeLabelsMenu")
        // this.cytoscapeContextMenus.getExpandNodeLabelsMenu(event)
        //     .subscribe(data => this.cytoscapeContextMenus.createExpandNodeLabelsMenu(data));
    }

    getExpandNodeEdgesMenuAsObservable() {
        return this.bind("expandNodeEdgesMenu")
        // this.cytoscapeContextMenus.getExpandNodeEdgesMenu(event)
        //     .pipe(share())
        //     .subscribe(data => this.cytoscapeContextMenus.createExpandNodeEdgesMenu(data));
    }


    getExpandNodeMenuAsObservable() {
        return this.bind("expandNodeMenu")
    }

    getExpandNodeQueryAsObservable() {
        return this.bind("expandNodeQuery")
    }

    getExpandNodeAsObservable (){
        return this.bind("expandNode")
    }


    bind(eventName) {
        return this.subject.asObservable()
            .pipe(filter(componentEvent => componentEvent.hasOwnProperty(eventName)))
            .pipe(map(componentEvent => componentEvent[eventName]))
    }
}

