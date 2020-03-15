import cytoscape from 'cytoscape';
import contextMenus from 'cytoscape-context-menus';
import $ from 'jquery';

cytoscape.use(contextMenus, $);

export class CytoscapeContextMenus {

    constructor(cy, subject) {
        this.cy = cy
        this.subject = subject
        this.cyContextMenus = this.createMenu(cy)
    }

    createMenu(cy) {
        const options = {
            menuItems: [{
                id: 'remove-node',
                content: 'Remove node',
                toolTipText: 'Remove node',
                selector: 'node',
                show: true,
                onClickFunction: (event) => this.remove(event)
            }, {
                id: 'remove-edge',
                content: 'Remove edge',
                toolTipText: 'Remove edge',
                selector: 'edge',
                show: true,
                onClickFunction: (event) => this.remove(event)
            }, {
                id: 'expand-node',
                content: 'Expand node',
                toolTipText: 'Expand node',
                selector: 'node',
                show: true,
                onClickFunction: (event) => this.expandNode(event)
            }]
        };
        return cy.contextMenus(options);
    }


    remove(event) {
        var target = event.target || event.cyTarget;
        target.remove();
    }

    removeAddedMenuItems() {
        for (var menuItem in this.addedMenuItems) {
            this.removeMenuItem(this.addedMenuItems[menuItem]);
        }
        this.addedMenuItems = [];
    }

    createExpandNodeLabelsMenu(data) {
        this.createExpandNodeMenu(data, "node");
    }

    createExpandNodeEdgesMenu(data) {
        this.createExpandNodeMenu(data, "edge");
    }

    createExpandNodeMenu(data, type) {
        console.log("createExpandNodeMenu " + JSON.stringify(data))
        const _this = this
        if (!this.addedMenuItems) {
            this.addedMenuItems = [];
        }
        var eventFactory = {};
        for (var item in data) {
            for (var direction of data[item].directions) {
                var menuTitle = "";
                var functionName = 'expand_' + type + '_' + data[item].nodeLabel + '_' + direction;
                eventFactory[functionName] = function (event) {
                    _this.expandNodeOnQuery(event, type, direction).subscribe(data => _this.expandGraph(event, data));
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
                        content: menuTitle + '<b>' + data[item].label + "</b>",
                        toolTipText: menuTitle + data[item].label,
                        selector: 'node',
                        show: true,
                        onClickFunction: eventFactory[functionName]
                    };
                    this.appendMenuItem(menuItem);
                    this.addedMenuItems.push(menuItem.id);
                }
                if (direction == "OUT") {
                    if (type == 'node')
                        menuTitle += "To node(s) ";
                    else
                        menuTitle += "Outgoing edge(s) "

                    var menuItem = {
                        id: 'expand_' + type + '_' + data[item].label + '_' + direction,
                        content: menuTitle + '<b>' + data[item].label + "</b>",
                        toolTipText: menuTitle + data[item].label,
                        selector: 'node',
                        show: true,
                        onClickFunction: eventFactory[functionName]
                    };
                    this.appendMenuItem(menuItem);
                    this.addedMenuItems.push(menuItem.id);
                }
            }
            if (data[item].directions.length == 2) {
                var menuTitle = "";
                if (type == 'node')
                    menuTitle = "To/from node(s) ";
                else
                    menuTitle = "In-/outgoing edge(s) ";
                var functionName = 'expand_' + type + '_' + data[item].label;
                eventFactory[functionName] = function (event) {
                    this.expandNodeOnQuery(event, type).subscribe(data => this.expandGraph(event, data));
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
                    onClickFunction: eventFactory[functionName]
                };
                this.appendMenuItem(menuItem);
                this.addedMenuItems.push(menuItem.id);
            }

        }
    }

    appendMenuItem(menuItem) {
        this.cyContextMenus.appendMenuItem(menuItem)
    }

    removeMenuItem(menuItem) {
        this.cyContextMenus.removeMenuItem(menuItem)
    }

    getExpandNodeLabelsMenu(event) {
        return this.getExpandNodeMenu(event, "labels");
    }

    getExpandNodeEdgesMenu(event) {
        return this.getExpandNodeMenu(event, "edges");
    }

    getExpandNodeMenu(event, type) {
        var target = event.target || event.cyTarget;
        console.log("getExpandNodeMenu " + target.id() + " " + type)
        // return this.service.getExpandNodeMenu(target.id(), type)
    }
	//
    // expandNodeQuery(event) {
    //     var target = event.target || event.cyTarget;
    //     // return this.service.expandNodeQuery(target.id())
    // }

    expandNodeOnQuery(event, type, directions) {
        const target = event.target || event.cyTarget;
        const direction = (function () {
            if (directions.length == 2) {
                return "BIDIRECTIONAL";
            } else {
                return directions[0];
            }
        })();
        // return this.service.expandNodeOnQuery(target.id(), 'label', direction, type)
    }

    expandNode(event) {
    	console.log("expandNode: " + event.target.id())
        const _this = this
        var dataNode = event.target.data();
        this.subject.next({expandNode: {id: event.target.id(), origin: dataNode.origin}})
    }

    expandGraph(event, data) {
        const _this = this
        const convertToCyData = function (data) {
            var cyData = []
            for (var node in data.nodeTable) {
                if (data.nodeTable[node]["id"]) {
                    if (_this.cy.nodes("#" + data.nodeTable[node]["properties"].id).length == 0) {
                        cyData.push({
                            group: 'nodes', data: {
                                "label": data.nodeTable[node]["properties"].name,
                                "name": data.nodeTable[node]["properties"].name,
                                "id": data.nodeTable[node]["properties"].id,
                                "type": data.nodeTable[node].labels[0],
                                origin: "neo4j"
                            }
                        });
                    }
                }
                // cyData.push({ group:'nodes', data:  data.nodes[0] } );
            }
            for (var edge in data.edgeTable) {
                if (_this.cy.edges("#" + data.edgeTable[edge]["properties"].id).length == 0) {
                    cyData.push({
                        group: 'edges', data: {
                            id: data.edgeTable[edge].id,
                            source: data.edgeTable[edge].start,
                            target: data.edgeTable[edge].end,
                            label: data.edgeTable[edge].type,
                            origin: "neo4j"
                        }
                    });
                }
            }

            return cyData;
        }

        var cyData = convertToCyData(data);
        this.cy.add(cyData);
        this.cy.layout({name: 'cose'}).run();
    }


};