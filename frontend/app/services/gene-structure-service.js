import {Observable} from "rxjs";
import axios from "axios";

export class GeneStructureService {

    constructor() {
    }

    findGene(geneIdentifier) {
        return new Observable((observer) => {
            axios.get('/api/structure/gene/search', {params: {'query': geneIdentifier}})
                .then((response) => {
                    observer.next(response.data);
                    observer.complete();
                })
                .catch((error) => {
                    observer.error(error);
                });
        });
    }

    getStructure(geneIdentifier, distance) {
        return new Observable((observer) => {
            axios.get('/api/structure/gene', {params: {'id': geneIdentifier, 'distance': distance}})
                .then((response) => {
                    observer.next(response.data);
                    observer.complete();
                })
                .catch((error) => {
                    observer.error(error);
                });
        });
    }

    getGeneToOrganisms(geneIdentifier) {
        return new Observable((observer) => {
            axios.get('/api/structure/gene/organisms', {params: {'id': geneIdentifier}})
                .then((response) => {
                    observer.next(response.data);
                    observer.complete();
                })
                .catch((error) => {
                    observer.error(error);
                });
        });
    }
}
