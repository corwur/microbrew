export class ReactomeComponent {

    constructor(model) {
        this.model = model
        this.service = new ReactomeService()
        this.selectedPathway = null
    }

    setPathwayInformation (event) {
        var data = event.target.data();
        if (data.origin = "reactome") {
            this.selectedPathway = data.stId;
        } else {
            this.selectedPathway = null;
        }
    }

    loadDiagram() {
        this.getPathwayJS(selectedPathway);
    }

    mapReactomeForCytoscape(cyData, element, node, edge = "") {
        if ((typeof element) == "object") {
            if (element instanceof Array) {
                for (var e in element) {
                    this.mapReactomeForCytoscape(cyData, element[e], node, edge);
                }
            } else {
                // first copy properties
                var newElement = Object.assign({}, element);
                // map values
                if (!node.id)
                    node.id = "reactome_" + node.dbId;
                newElement.id = "reactome_" + element.dbId;
                if (element.displayName)
                    newElement.label = element.displayName;
                if (element.schemaClass && element.schemaClass == "Summation")
                    newElement.label = "Summation";
                newElement.origin = "reactome";
                if (node.id != newElement.id) {
                    cyData.push({group: 'nodes', data: newElement});
                    cyData.push({
                        group: 'edges', data: {
                            id: node.id + "_" + newElement.id,
                            source: node.id,
                            target: newElement.id,
                            label: edge == "" ? "reactome link" : edge,
                            origin: "reactome"
                        }
                    });
                }
                for (var e in element) {
                    if ((typeof element[e]) == "object") {
                        this.mapReactomeForCytoscape(element[e], newElement, "" + e);
                    }
                }
            }
        }
    }

    showPathwayForCytoscape(pathway, node) {
        const cyData = this.mapReactomeForCytoscape([], pathway, node);
    }

    showPathwaysForCytoscape(gene, node) {
        for (var index = 0; index < gene.results[0].entries.length; index++) {
            this.getPathway(gene.results[0].entries[index].stId).subscribe(
                data => this.showPathwayForCytoscape(data, node));
        }
    }

    getPathwayJS(id) {
        if (id != null) {
            diagram.loadDiagram(id);
            diagram.onDiagramLoaded(function (loaded) {
                //diagram.flagItems("FYN");
                if (loaded == name) App.diagram.selectItem(name);
            });
        }
    }

    getPathwayInformationForCytoscape(node) {
        if (node.origin == "neo4j") {
            this.service.findGene(node.label).subscribe(
                data => this.showPathwaysForCytoscape(data, node));
        }
    }
}
