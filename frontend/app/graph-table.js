var Tabulator = require('tabulator-tables');

const GraphTable = {
		nodeTable : function(event) {
			var node = event.target.data();
			var edges = event.target.connectedEdges();
			console.log(node);
			var data = [];
			for (var e=0; e < edges.length; e++) {
				var connectedNodes = edges[e].connectedNodes();
				for (var n=0; n < connectedNodes.length; n++ ) {
					if (connectedNodes[n].data().id != node.id) {
						if (data.length == 0)
							data.push({'name': node.label, 'edge' : edges[e].data().label, 'node': connectedNodes[n].data().label});
						else
							data.push({'edge' : edges[e].data().label, 'node': connectedNodes[n].data().label});
					}
				}
			}
			var table = new Tabulator("#graph-table", {
			    columns:[
			        {title:"Name", field:"name", sorter:"string"},
			        {title:"Edge", field:"edge", sorter:"string"},
			        {title:"Node", field:"node", sorter:"string"}
			    ],
			    data : data
			});
			var raw = new Tabulator("#raw-table", {
			    data : [node],
			    autoColumns:true
			});
		},
		edgeTable : function(event) {
			var edge = event.target.data();
			var cy = event.cy;
			var table = new Tabulator("#graph-table", {
			    columns:[
			        {title:"Name", field:"name", sorter:"string"},
			        {title:"Source", field:"source", sorter:"string"},
			        {title:"Target", field:"target", sorter:"string"}
			    ],
			    data : [
			    	{'name': edge.label,
			    	'source': cy.getElementById(edge.source).data().label,
			    	'target': cy.getElementById(edge.target).data().label} 
			    	]
			});
			var raw = new Tabulator("#raw-table", {
			    data : [edge],
			    autoColumns:true
			});

		}
}

module.exports = GraphTable;