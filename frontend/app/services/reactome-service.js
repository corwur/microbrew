import {Observable} from "rxjs";
import axios from "axios";

export class ReactomeService {


    findGene(geneIdentifier) {
        return new Observable((observer) => {
            axios.get('/api/reactome/ContentService/search/query?query=' + geneIdentifier)
                .then((response) => {
                    observer.next(response.data);
                    observer.complete();
                })
                .catch((error) => {
                    observer.error(error);
                });
        });
    }
    getPathway(geneIdentifier) {
        return new Observable((observer) => {
            console.log('https://reactome.org/ContentService/data/pathways/low/entity/' + geneIdentifier + '?species=yeast')
            axios.get('https://reactome.org/ContentService/data/pathways/low/entity/' + geneIdentifier + '?species=yeast')
                .then((response) => {
                    observer.next(response.data);
                    observer.complete();
                })
                .catch((error) => {
                    observer.error(error);
                });
        });
    }

    query(identifier) {
        if (identifier.startsWith("reactome")) {
            identifier = identifier.split("_")[1];
        }
        return new Observable((observer) => {
            axios.get('https://reactome.org/ContentService/data/query/enhanced/' + identifier)
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