const rx = require("rxjs")
const axios = require("axios")

const Reactome = {
    findGene: function(geneIdentifier) {
        return new rx.Observable( ( observer ) => {
            axios.get( '/api/reactome/ContentService/search/query?query=' + geneIdentifier)
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
module.exports = Reactome;