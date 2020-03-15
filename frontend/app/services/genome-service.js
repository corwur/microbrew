import {Observable} from "rxjs"
import axios from 'axios'

export class GenomeService  {
    getReferenceContigNames() {
        return new Observable( ( observer ) => {
            axios.get( '/api/genome/reference/names')
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
