import {GenomeService} from "../../services/genome-service"
import pileup from 'pileup'
export class GenomeBrowser {

    constructor() {
        this.service = new GenomeService()
        this.contig = 'chrI'
        this.start = 0
        this.end = 100
    }

    init(config) {
        this.service.getReferenceContigNames().subscribe(contigs => this.initPileUp(config, contigs));
    }

    initPileUp(config, contigs) {

        var p = pileup.create(config.node, {
            range: {contig: contigs[0], start: 0, stop:100},
            tracks: [
                {
                    viz: pileup.viz.genome(),
                    isReference: true,
                    data: pileup.formats.twoBit({url: '/api/genome/reference'}),
                    name: 'Reference'
                },
                {
                    viz: pileup.viz.scale(),
                    name: 'Scale'
                },
                {
                    viz: pileup.viz.location(),
                    name: 'Location'
                }
                //     ,
                //     {
                //         viz: pileup.viz.pileup(),
                //         data: pileup.formats.alignmentJson(JSON.stringify({
                //             alignment:'TACG',
                //             start: 0,
                //             end:4,
                //             contig:'1',
                //             url: '/api/genome/reference'
                //         })),
                //         cssClass: 'normal',
                //         name: 'Alignments'
                //
                //     }
            ]
        });

    }
}