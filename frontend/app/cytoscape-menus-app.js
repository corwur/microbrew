const rx = require("rxjs");
const axios = require("axios");
const rx_operators = require('rxjs/operators');

const CytoscapeContextMenus = {
		
	remove : function (event) {
		var target = event.target || event.cyTarget;
		target.remove();
	},
	
	expandNodeQuery : function(event) {
		var target = event.target || event.cyTarget;

		return new rx.Observable( ( observer ) => {
            axios.get( '/api/neo4j/node/expand/' + target.id())
                .then( ( response ) => {
                    observer.next( response.data );
                    observer.complete();
                } )
                .catch( ( error ) => {
                    observer.error( error );
                } );
        });
	},
	
	expandNode : function(event) {
		var observable = CytoscapeContextMenus.expandNodeQuery(event).pipe(rx_operators.share());
		observable.subscribe(data => CytoscapeContextMenus.expandGraph(event, data));
	},

	expandGraph : function(event, data) {
        const convertToCyData = function(data) {
            var cyData = []
            for(var index =0; index < data.nodeTable.length ; index++) {
                cyData.push({ group:'nodes', data:  data.nodes[0] } );
            }
            for(var index =0; index < data.edgeTable.length ; index++) {
                cyData.push({ group:'edges', data: { 
                	id:data.edges[index].id, 
                	source:data.edges[index].from, 
                	target:data.edges[index].to,
                	label: data.edges[index].label
                	}} );
            }

            return cyData;
        }

        var cyData = convertToCyData(data);
        App.cy.add(cyData);
        App.cy.layout({ name: 'cose'}).run();
		
	},
	
	create : function(cytoscape) {
		var options = {
				menuItems: [{
					id: 'remove-node',
					content: 'Remove node',
					toolTipText: 'Remove node',
					selector: 'node',
					show: true,
					onClickFunction : CytoscapeContextMenus.remove 
				},
				{
					id: 'remove-edge',
					content: 'Remove edge',
					toolTipText: 'Remove edge',
					selector: 'edge',
					show: true,
					onClickFunction : CytoscapeContextMenus.remove
				},
				{
					id: 'expand-node',
					content: 'Expand node',
					toolTipText: 'Expand node',
					selector: 'node',
					show: true,
					onClickFunction : CytoscapeContextMenus.expandNode
				}
				]
		};
		
		return cytoscape.contextMenus(options);
		
	} 
};

module.exports = CytoscapeContextMenus;