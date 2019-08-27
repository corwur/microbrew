const rx = require("rxjs")
const axios = require("axios")

const Reactome = {
	
    mapReactomeForCytoscape: function(element,node, edge = "") {
    	if ((typeof element) == "object") {
	    	if (element instanceof Array) {
	    		for (var e in element) {
	    			Reactome.mapReactomeForCytoscape(element[e],node, edge);
	    		}
	    	}
	    	else {
				var cyData = []
				// first copy properties
				var newElement = Object.assign({}, element);
				// map values
				if (!node.id)
					node.id = "reactome_" + node.dbId;
				newElement.id = "reactome_" + element.dbId;
				if (element.displayName) 
					newElement.label = element.displayName;
				if (element.schemaClass && element.schemaClass == "Summation" )
					newElement.label = "Summation";
				newElement.origin = "reactome";
				if (node.id != newElement.id) {
			    	cyData.push({ group:'nodes', data:  newElement});
					cyData.push({ group:'edges', data: { 
			        	id:node.id + "_" + newElement.id, 
			        	source:node.id, 
			        	target:newElement.id,
			        	label: edge == "" ? "reactome link" : edge,
			        	origin:"reactome"
			        	}} );
				}
				App.cy.add(cyData);
		        App.cy.layout({ name: 'cose'}).run();
	    		for (var e in element) {
					if ((typeof element[e]) == "object") {
						Reactome.mapReactomeForCytoscape(element[e], newElement, "" + e);
					}
		    	}    	
	    	}
    	}
    },

    
    showPathwayForCytoscape: function(pathway,node) {
    	Reactome.mapReactomeForCytoscape(pathway,node);
    },
    
  
    showPathwaysForCytoscape: function(gene, node) {
    	for (var index=0; index < gene.results[0].entries.length; index++){
    		Reactome.getPathway(gene.results[0].entries[index].stId).subscribe(
    	            data => Reactome.showPathwayForCytoscape(data,node));
    	}
    },
    
    getPathwayInformation: function(event) {
    	var data = event.target.data(); 
    	if (data.origin == "reactome"){
        	App.diagram.loadDiagram(data.stdId);
        	App.diagram.onDiagramLoaded(function (loaded) {
        		console.info("Loaded ", loaded);
        		//diagram.flagItems("FYN");
        		if (loaded == name) App.diagram.selectItem(name);
    	       });

    	}
    },

    getPathwayInformationForCytoscape: function(node) {
    	if (node.origin == "neo4j") {
    		Reactome.findGene(node.label).subscribe(
	            data => Reactome.showPathwaysForCytoscape(data, node));
    	}
    },

    findGene: function(geneIdentifier) {
        return new rx.Observable( ( observer ) => {
            axios.get( '/api/reactome/ContentService/search/query?query=' + geneIdentifier)
                .then( ( response ) => {
                    observer.next( response.data );
                    observer.complete();
                } )
                .catch( ( error ) => {
                    observer.error( error );
                } );
        });
    },
    
    getPathway: function(geneIdentifier) {
        return new rx.Observable( ( observer ) => {
            console.log( 'https://reactome.org/ContentService/data/pathways/low/entity/' + geneIdentifier + '?species=yeast' )
            axios.get( 'https://reactome.org/ContentService/data/pathways/low/entity/' + geneIdentifier + '?species=yeast' )
                .then( ( response ) => {
                    observer.next( response.data );
                    observer.complete();
                } )
                .catch( ( error ) => {
                    observer.error( error );
                } );
        });
    	
    },
    query: function(identifier) {
    	if (identifier.startsWith("reactome")){
    		identifier = identifier.split("_")[1];
    	}
        return new rx.Observable( ( observer ) => {
            axios.get( 'https://reactome.org/ContentService/data/query/enhanced/' + identifier)
                .then( ( response ) => {
                    observer.next( response.data );
                    observer.complete();
                } )
                .catch( ( error ) => {
                    observer.error( error );
                } );
        });
    	
    }

	
}
module.exports = Reactome;