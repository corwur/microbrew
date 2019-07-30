const rx = require("rxjs");
const axios = require("axios");

const GeneStructure = {

    findGene(geneIdentifier) {
        return new rx.Observable( ( observer ) => {
            axios.get( '/api/structure/gene' + '?search=' + geneIdentifier)
                .then( ( response ) => {
                    observer.next( response.data );
                    observer.complete();
                } )
                .catch( ( error ) => {
                    observer.error( error );
                } );
        });
    },

    getStructure(geneIdentifier, distance) {
        return new rx.Observable( ( observer ) => {
            axios.get( '/api/structure/gene/' + geneIdentifier + '?distance=' + distance )
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

module.exports = GeneStructure;