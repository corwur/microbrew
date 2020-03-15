'use strict';
import bootstrap from 'bootstrap'
import {Subject} from 'rxjs'
import 'lodash.merge';
import {GeneStructureService} from "./services/gene-structure-service";
import {GenomeService} from "./services/genome-service";
import {Neo4jNodeMenuService} from "./services/neo4j-node-menu-service";
import {ReactomeService} from "./services/reactome-service";
import { GeneStructureComponent } from 'components/gene-structure/gene-structure-component';
import { SelectGeneForm } from 'components/gene-structure/select-gene-form'
import { CircleDiagramComponent } from 'components/circle-diagram/circle-diagram-component'
import { GenomeBrowser} from "./components/genome-browser/genome-browser-component";
import {ReactomeComponent} from "./components/reactome/reactome-component";
import {filter, flatMap, map} from "rxjs/operators";


class Application {

    constructor() {
        this.subject = new Subject()
        this.geneStructureService = new GeneStructureService()
        this.neo4jNodeMenuService = new Neo4jNodeMenuService()
        this.genomeService = new GenomeService()
        this.reactomeService = new ReactomeService()
    }

    in(observable, method) {
        observable.subscribe(data => {
            console.log("Application in " + JSON.stringify(data))
            method.apply(this, [data])
        })
    }

    out(object, methodName, appEvent) {
        const method = object[methodName]
        this.subject.asObservable()
            .pipe(filter(data => data.hasOwnProperty(appEvent)))
            .subscribe(data => method.apply(object, [data[appEvent]]))
    }

    onSelectGeneId(arg) {
        console.log("onSelectGeneId:" + JSON.stringify(arg))
        this.getGeneStructure(arg.geneId, arg.distance)
    }

    onFindGeneId(arg) {
        console.log("onFindGeneId:" + JSON.stringify(arg))
        this.findGene(arg)
    }

    onExpandGene() {
        this.neo4jNodeMenuService.expandNodeQuery()
    }

    onExpandNodeEdgesMenu(arg) {
        console.log("onExpandNodeEdgesMenu " + JSON.stringify(arg))
    }

    onExpandNodeLabelsMenu(arg) {
        console.log("onExpandNodeLabelsMenu " + JSON.stringify(arg))
        this.getNeo4jNodeMenu(arg.id, "labels")
    }

    onExpandNodeMenu(arg) {
        console.log("onExpandNodeMenu " + JSON.stringify(arg))
        // if (dataNode.origin == "neo4j") {
        // }
        // else if (dataNode.origin == "reactome"){
        // 	this.subject.next({expandNodeQuery:event.target.id()})
        // 	var observable = this.reactomeService.query(event.target.id())
        // 	var node = Object();
        // 	node.id = event.target.id();
        // 	node.dbId = event.target.id().split("_")[1];
        // 	observable.subscribe(data => reactome.mapReactomeForCytoscape(data, node));
        // }

    }
    onExpandNode(arg) {
        console.log("onExpandNode " + JSON.stringify(arg))
        this.getNeo4jExpandNode(arg.id)
    }

    getNeo4jExpandNode(nodeId, type) {
        this.neo4jNodeMenuService.expandNodeQuery(nodeId)
            .subscribe(data => this.subject.next({expandNode: data}))
    }

    getNeo4jNodeMenu(nodeId, type) {
        this.neo4jNodeMenuService.getExpandNodeMenu(nodeId, type)
            .subscribe(data => this.subject.next({expandNodeMenu: data}))
    }

    getGeneStructure(geneIdentifier, distance) {
        this.geneStructureService.getStructure(geneIdentifier, distance).subscribe(geneStructure => {
            this.subject.next({"geneStructure":geneStructure})
        })
    }

    findGene(geneIdentifier) {
        this.geneStructureService.findGene(geneIdentifier + ".*")
            .subscribe(geneList => {
                this.subject.next({"findGene":geneList})
            })
    }
}
const app = new Application()

const selectGeneForm = new SelectGeneForm()
const geneStructureComponent = new GeneStructureComponent()

app.in(selectGeneForm.getSelectedGeneIdObservable(), app.onSelectGeneId)
app.in(selectGeneForm.getFindGeneIdObservable(), app.onFindGeneId)
app.out(selectGeneForm, "showGeneIdList", "findGene")

app.in(geneStructureComponent.getExpandNodeEdgesMenuAsObservable(), app.onExpandNodeEdgesMenu)
app.in(geneStructureComponent.getExpandNodeLabelsMenuAsObservable(), app.onExpandNodeLabelsMenu)
app.in(geneStructureComponent.getExpandNodeMenuAsObservable(), app.onExpandNodeMenu)
app.in(geneStructureComponent.getExpandNodeAsObservable(), app.onExpandNode)

app.out(geneStructureComponent,  "renderStructureGraph", "geneStructure")
app.out(geneStructureComponent, "expandNodeMenu" , "expandNodeMenu")
app.out(geneStructureComponent, "addStructure" , "expandNode")

selectGeneForm.init()
geneStructureComponent.init({ id:'cy'})

