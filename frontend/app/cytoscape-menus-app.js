const rx = require("rxjs");
const axios = require("axios");
const rx_operators = require('rxjs/operators');

const CytoscapeContextMenus = {
		
	remove : function (event) {
		var target = event.target || event.cyTarget;
		target.remove();
	},
	
	
	removeAddedMenuItems : function() {
		for (var menuItem in CytoscapeContextMenus.addedMenuItems) {
			App.cytoscapeContextMenu.removeMenuItem(CytoscapeContextMenus.addedMenuItems[menuItem]);
		}
		CytoscapeContextMenus.addedMenuItems = [];
	},
	
	createExpandNodeLabelMenu: function(data) {
		if (!CytoscapeContextMenus.addedMenuItems) {
			CytoscapeContextMenus.addedMenuItems = [];
		}
		for (var item in data) {
			for (var direction of data[item].directions) {
				var functionName = 'expand_node_' + data[item].nodeLabel + '_' + direction;
				window[functionName] = function(event) {
					console.log(arguments.callee);
				}
				window[functionName].target = {};
				window[functionName].target[item] = data[item];
				window[functionName].direction = direction;
				
				if (direction == "IN") {
					var menuItem = {
						id: 'expand_node_' + data[item].nodeLabel + '_' + direction,
						content: 'Expand from <b>' + data[item].nodeLabel  + "</b>",
						toolTipText: 'Expand from ' + data[item].nodeLabel,
						selector: 'node',
						show: true,
						onClickFunction : window[functionName]
					};
					App.cytoscapeContextMenu.appendMenuItem(menuItem);
					CytoscapeContextMenus.addedMenuItems.push(menuItem.id);
				}
				if (direction == "OUT") {

					var menuItem = {
						id: 'expand_node_' + data[item].nodeLabel + '_' + direction,
						content: 'Expand to <b>' + data[item].nodeLabel  + "</b>",
						toolTipText: 'Expand to ' + data[item].nodeLabel,
						selector: 'node',
						show: true,
						onClickFunction : window[functionName] 
					};
					App.cytoscapeContextMenu.appendMenuItem(menuItem);
					CytoscapeContextMenus.addedMenuItems.push(menuItem.id);
				}
			}
			if (data[item].directions.length ==2) {
				var functionName = 'expand_node_' + data[item].nodeLabel;
				window[functionName] = function(event) {
					console.log(arguments.callee);
				}
				window[functionName].target = {};
				window[functionName].target[item] = data[item];

				var menuItem = {
						id: 'expand_node_' + data[item].nodeLabel + '_both',
						content: 'Expand to/from <b>' + data[item].nodeLabel + "</b>",
						toolTipText: 'Expand to/from ' + data[item].nodeLabel,
						selector: 'node',
						show: true,
						onClickFunction : window[functionName]
					};
					App.cytoscapeContextMenu.appendMenuItem(menuItem);
					CytoscapeContextMenus.addedMenuItems.push(menuItem.id);
			}
			
		}
	},
	
	getExpandNodeLabelMenu : function(event) {
		var target = event.target || event.cyTarget;

		return new rx.Observable( ( observer ) => {
            axios.get( '/api/neo4j/node/menu/labels/' + target.id())
                .then( ( response ) => {
                    observer.next( response.data );
                    observer.complete();
                } )
                .catch( ( error ) => {
                    observer.error( error );
                } );
        });
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
            for(var node in data.nodeTable) {
            	if (data.nodeTable[node]["id"]) {
            		if (App.cy.nodes("#" + data.nodeTable[node]["properties"].id).length == 0) {
	            		cyData.push({ group:'nodes', data:  {
	            			"label" : data.nodeTable[node]["properties"].name,
	            			"name" : data.nodeTable[node]["properties"].name,
	            			"id" : data.nodeTable[node]["properties"].id,
	            			"type": data.nodeTable[node].labels[0]
	            			}} );
            		}
            	}
                //cyData.push({ group:'nodes', data:  data.nodes[0] } );
            }
            for(var edge in data.edgeTable) {
            	if (App.cy.edges("#" + data.edgeTable[edge]["properties"].id).length == 0) {
	                cyData.push({ group:'edges', data: { 
	                	id:data.edgeTable[edge].id, 
	                	source:data.edgeTable[edge].start, 
	                	target:data.edgeTable[edge].end,
	                	label: data.edgeTable[edge].type
	                	}} );
            	}
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