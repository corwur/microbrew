import circos from 'circos';
import {Colors} from './colors';
import {filter, map} from "rxjs/operators";
import jQuery from 'jquery'

const $ = jQuery

export class CircleDiagramComponent {

    constructor(model) {
        this.model = model
        this.organisms = new Set()
        this.geneStructure = {}

        this.configuration = {
            innerRadius: 250,
            outerRadius: 260,
            cornerRadius: 10,
            gap: 0.01, // in radian
            labels: {
                display: true,
                position: 'center',
                size: '14px',
                color: '#000000',
                radialOffset: 20
            },
            ticks: {
                display: false,
                color: 'grey',
                spacing: 10000000,
                labels: true,
                labelSpacing: 10,
                //labelSuffix: 'Mb',
                //labelDenominator: 1000000,
                labelDisplay0: true,
                labelSize: '40px',
                labelColor: '#000000',
                labelFont: 'default',
                majorSpacing: 5,
                size: {
                    minor: 2,
                    major: 5,
                }
            },
            events: {}
        }
    }

    init() {

        document.getElementById('circos').innerHTML = "";
        this.circos = new circos({
            container: document.getElementById('circos'),
            width: 600,
            height: 600,
        });

        document.getElementById("circos_seqlength").addEventListener('change', event => this.renderCircleDiagram())
        document.getElementById("circos_organism").addEventListener('change', event => this.renderCircleDiagram())

        this.model
            .pipe(filter( data => data.hasOwnProperty('structure')))
            .pipe( map(data => data.geneId ))
            .subscribe(geneId => this.getGeneToOrganisms(geneId))
    }


    getGeneToOrganisms(geneId) {
        this.service.getGeneToOrganisms(geneId)
            .subscribe(geneStructure => {
                this.updateSelectOrganisms(geneStructure)
            })
    }

    updateSelectOrganisms(geneStructure) {
        document.getElementById('circos').innerHTML = "";
        this.circos = new circos({
            container: document.getElementById('circos'),
            width: 600,
            height: 600,
        });
        this.organisms.clear()
        this.geneStructure = geneStructure
        this.geneStructure.sequences
            .map(sequence => sequence.organism)
            .filter(organism => !this.organisms.has(organism))
            .forEach(organism => this.organisms.add(organism))

        const selectedOrganisms = new Set()
        const selectbox = $('#circos_organism')
        $('option:selected', selectbox).each(option => selectedOrganisms.add(option.value))
        selectbox.empty()
        this.organisms.forEach(organism => {
            selectbox.append($('<option>', {
                value: organism,
                text: organism,
                selected: selectedOrganisms.has(organism)
            }))
        })
    }

    renderCircleDiagram() {
        if (!this.geneStructure.sequences) {
            return
        }
        const seqlength = $('#circos_seqlength option:selected').val();
        //organisms
        const contigs = new Set();
        const genes = []
        const colors = new Colors();
        const selectbox = $('#circos_organism')
        const selectedOrganisms = $('option:selected', selectbox).map(option => option.value)
        const circosSequences = []
        this.geneStructure.sequences.forEach(sequence => {
            const name = sequence.name;
            if (sequence.length >= seqlength && selectedOrganisms.has(sequence.organism)) {
                if (!colors[sequence.organism]) {
                    colors[sequence.organism] = colors.pick();
                }
                circosSequences.push({
                    len: sequence.length,
                    color: colors[sequence.organism],
                    label: name,
                    id: "" + sequence.id,
                    organims: sequence.organism
                });
                contigs.add(sequence.id);
            }
        })

        circosSequences.sort((x, y) => {
            if (x.organism == y.organism) {
                return x.label < y.label;
            } else {
                return x.organism < y.organism;
            }
        })


        this.circos.layout(circosSequences, this.configuration);
        this.geneStructure.genes.forEach(gene => {
            gene.on.forEach(on => {
                    if (contigs.has(on.sequenceID)) {
                        genes.push({
                            block_id: "" + on.sequenceID,
                            start: on.start,
                            end: on.end,
                            label: gene.name
                        })
                    }
                }
            )
        })


        this.geneStructure.genes.forEach(gene => {
            const connections = []
            for(var source = 0; source < gene.on.length; source++) {
                for (var target = source + 1; target < gene.on.length; target++) {
                    if (contigs.has(gene.on[source].sequenceID) && contigs.has(gene.on[target].sequenceID)) {
                        connections.push({
                            source: {
                                id: "" + gene.on[source].sequenceID,
                                start: gene.on[source].start,
                                end: gene.on[source].end
                            },
                            target: {
                                id: "" + gene.on[target].sequenceID,
                                start: gene.on[target].start,
                                end: gene.on[target].end
                            }
                        })
                    }
                }
            }
            if(connections.length > 0) {
                this.circos.chords("links_" +  gene.name.replace(/[\|\.]/g, "_"), connections, {
                    radius: 0.89,
                    color: colors.pick(),
                    tooltipContent: function (datum, index) {
                        return "";
                    }
                })
            }
        })
        console.log(JSON.stringify(genes))
        this.circos.stack('genes', genes, {
            color: 'blue',
            innerRadius: 0.9,
            outerRadius: 0.99,
            tooltipContent: function (datum, index) {
                return datum.label;
            }
        });
        this.circos.render();
    }
}

//
//
// renderCircleDiagram(geneStructureModel) {
//     var seqlength = parseInt($('#circos_seqlength')[0][$('#circos_seqlength')[0].selectedIndex].value);
//     var structureData = geneStructureModel;
//     if (structureData == null) return;
//     document.getElementById('circos').innerHTML = "";
//     this.circos = new circos({
//         container: document.getElementById('circos'),
//         width: 600,
//         height: 600,
//     });
//
//     var configuration = {
//         innerRadius: 250,
//         outerRadius: 260,
//         cornerRadius: 10,
//         gap: 0.01, // in radian
//         labels: {
//             display: true,
//             position: 'center',
//             size: '14px',
//             color: '#000000',
//             radialOffset: 20
//         },
//         ticks: {
//             display: false,
//             color: 'grey',
//             spacing: 10000000,
//             labels: true,
//             labelSpacing: 10,
//             //labelSuffix: 'Mb',
//             //labelDenominator: 1000000,
//             labelDisplay0: true,
//             labelSize: '40px',
//             labelColor: '#000000',
//             labelFont: 'default',
//             majorSpacing: 5,
//             size: {
//                 minor: 2,
//                 major: 5,
//             }
//         },
//         events: {}
//     }
//
//     //organisms
//     var data = [];
//     var contigs = new Set();
//     var colors = new Colors();
//     var selectedOrganisms = new Set();
//     var select = $('#circos_organism');
//     for (var o = 0; o < select[0].selectedOptions.length; o++) {
//         selectedOrganisms.add(select[0].selectedOptions[o].value);
//     }
//
//     for (var index = 0; index < structureData.sequences.length; index++) {
//         var name = structureData.sequences[index].name;
//
//         //console.log(structureData.sequences[index].length);
//
//         if (structureData.sequences[index].length >= seqlength && selectedOrganisms.has(structureData.sequences[index].organism)) {
//             if (!colors[structureData.sequences[index].organism]) {
//                 colors[structureData.sequences[index].organism] = colors.pick();
//             }
//             data.push({
//                 len: structureData.sequences[index].length,
//                 color: colors[structureData.sequences[index].organism],
//                 label: name,
//                 id: "" + structureData.sequences[index].id,
//                 organims: structureData.sequences[index].organism
//             });
//             contigs.add(structureData.sequences[index].id);
//         }
//     }
//     data.sort(function (x, y) {
//         if (x.organism == y.organism) {
//             return x.label < y.label;
//         } else {
//             return x.organism < y.organism;
//         }
//     })
//
//     //console.log(contigs);
//     this.circos.layout(data, configuration);
//     var colors = new Colors();
//     var pathways = [];
//     var genes = [];
//     for (var index = 0; index < structureData.genes.length; index++) {
//         var data = [];
//         // get nodes from cytoscape
//         var pathwayNodes = this.pathwayNodes[index];
//         for (var source = 0; source < structureData.genes[index].on.length; source++) {
//             if (contigs.has(structureData.genes[index].on[source].sequenceID)) {
//                 genes.push({
//                     block_id: "" + structureData.genes[index].on[source].sequenceID,
//                     start: structureData.genes[index].on[source].start,
//                     end: structureData.genes[index].on[source].end,
//                     label: structureData.genes[index].name
//                 })
//                 for (var p = 0; p < pathwayNodes.length; p++) {
//                     if (pathwayNodes[p].data().origin == "reactome") {
//                         pathways.push({
//                             block_id: "" + structureData.genes[index].on[source].sequenceID,
//                             start: structureData.genes[index].on[source].start,
//                             end: structureData.genes[index].on[source].end,
//                             label: pathwayNodes[p].data().name
//                         });
//                     }
//                 }
//             }
//             for (var target = source + 1; target < structureData.genes[index].on.length; target++) {
//                 if (contigs.has(structureData.genes[index].on[source].sequenceID) && contigs.has(structureData.genes[index].on[target].sequenceID)) {
//                     data.push({
//                         source: {
//                             id: "" + structureData.genes[index].on[source].sequenceID,
//                             start: structureData.genes[index].on[source].start,
//                             end: structureData.genes[index].on[source].end
//                         },
//                         target: {
//                             id: "" + structureData.genes[index].on[target].sequenceID,
//                             start: structureData.genes[index].on[target].start,
//                             end: structureData.genes[index].on[target].end
//                         },
//                     })
//                 }
//             }
//         }
//
//         this.circos.chords("links_" + structureData.genes[index].name.replace(/[\|\.]/g, "_"), data, {
//             radius: 0.89,
//             color: colors.pick(),
//             tooltipContent: function (datum, index) {
//                 return "";
//             }
//         });
//     }
//     this.circos.stack('pathways', pathways, {
//         color: 'red',
//         innerRadius: 1.01,
//         outerRadius: 1.2,
//         tooltipContent: function (datum, index) {
//             return datum.label;
//         }
//     });
//     this.circos.stack('genes', genes, {
//         color: 'blue',
//         innerRadius: 0.9,
//         outerRadius: 0.99,
//         tooltipContent: function (datum, index) {
//             return datum.label;
//         }
//     });
//
//
//     this.circos.render();
// }
