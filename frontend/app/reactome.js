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
    },
    
    getPathway: function(geneIdentifier) {
        return new rx.Observable( ( observer ) => {
            console.log( 'https://reactome.org/ContentService/data/pathways/low/entity/' + geneIdentifier + '?species=yeast' )
            axios.get( 'https://reactome.org/ContentService/data/pathways/low/entity/' + geneIdentifier + '?species=yeast' )
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