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
	
	createExpandNodeLabelsMenu: function(data) {
		CytoscapeContextMenus.createExpandNodeMenu(data, "node");
	},
	createExpandNodeEdgesMenu: function(data) {
		CytoscapeContextMenus.createExpandNodeMenu(data, "edge");
	},

	createExpandNodeMenu: function(data, type) {
		if (!CytoscapeContextMenus.addedMenuItems) {
			CytoscapeContextMenus.addedMenuItems = [];
		}
		var eventFactory = {};
		for (var item in data) {
			for (var direction of data[item].directions) {
				var menuTitle = "";
				var functionName = 'expand_' + type + '_' + data[item].nodeLabel + '_' + direction;
				eventFactory[functionName] = function(event) {
					var observable = CytoscapeContextMenus.expandNodeOnQuery(event, arguments.callee).pipe(rx_operators.share());
					observable.subscribe(data => CytoscapeContextMenus.expandGraph(event, data));
				}
				eventFactory[functionName].target = item;
				eventFactory[functionName].direction = [direction];
				eventFactory[functionName].type = type;
				
				if (direction == "IN") {
					if (type == 'node')
						menuTitle += "From node(s) ";
					else
						menuTitle += "Incoming edge(s) "
					var menuItem = {
						id: 'expand_' + type + '_' + data[item].label + '_' + direction,
						content: menuTitle + '<b>' + data[item].label  + "</b>",
						toolTipText: menuTitle + data[item].label,
						selector: 'node',
						show: true,
						onClickFunction : eventFactory[functionName]
					};
					App.cytoscapeContextMenu.appendMenuItem(menuItem);
					CytoscapeContextMenus.addedMenuItems.push(menuItem.id);
				}
				if (direction == "OUT") {
					if (type == 'node')
						menuTitle += "To node(s) ";
					else
						menuTitle += "Outgoing edge(s) "

					var menuItem = {
						id: 'expand_' + type + '_' + data[item].label + '_' + direction,
						content: menuTitle + '<b>' + data[item].label  + "</b>",
						toolTipText: menuTitle + data[item].label,
						selector: 'node',
						show: true,
						onClickFunction : eventFactory[functionName] 
					};
					App.cytoscapeContextMenu.appendMenuItem(menuItem);
					CytoscapeContextMenus.addedMenuItems.push(menuItem.id);
				}
			}
			if (data[item].directions.length ==2) {
				var menuTitle = "";
				if (type == 'node')
					menuTitle = "To/from node(s) ";
				else
					menuTitle = "In-/outgoing edge(s) ";
				var functionName = 'expand_' + type + '_' + data[item].label;
				eventFactory[functionName] = function(event) {
					var observable = CytoscapeContextMenus.expandNodeOnQuery(event, arguments.callee).pipe(rx_operators.share());
					observable.subscribe(data => CytoscapeContextMenus.expandGraph(event, data));
				}
				eventFactory[functionName].target = item;
				eventFactory[functionName].direction = data[item].directions;
				eventFactory[functionName].type = type;

				var menuItem = {
						id: 'expand_' + type + '_' + data[item].label + '_both',
						content: menuTitle + '<b>' + data[item].label + "</b>",
						toolTipText: menuTitle + data[item].label,
						selector: 'node',
						show: true,
						onClickFunction : eventFactory[functionName]
					};
					App.cytoscapeContextMenu.appendMenuItem(menuItem);
					CytoscapeContextMenus.addedMenuItems.push(menuItem.id);
			}
			
		}
	},
	
	getExpandNodeLabelsMenu : function(event) {
		return CytoscapeContextMenus.getExpandNodeMenu(event, "labels");
	},
	getExpandNodeEdgesMenu : function(event) {
		return CytoscapeContextMenus.getExpandNodeMenu(event, "edges"); 
	},
	getExpandNodeMenu : function(event, type) {
		var target = event.target || event.cyTarget;

		return new rx.Observable( ( observer ) => {
            axios.get( '/api/neo4j/node/menu/' + type + '/' + target.id())
                .then( ( response ) => {
                    observer.next( response.data );
                    observer.complete();
                } )
                .catch( ( error ) => {
                    observer.error( error );
                } );
        });
	},
	
	expandNodeOnQuery : function (event, callee){
		var type = callee.type;
		var target = event.target || event.cyTarget;
		var direction;
		if (callee.direction.length == 2) {
			direction = "BIDIRECTIONAL";
		}
		else {
			direction = callee.direction[0];
		}
			
		return new rx.Observable( ( observer ) => {
			axios.get( '/api/neo4j/node/expand/' + type + '/' + target.id(), {params: {'label': callee.target, 'direction': direction}} )
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