import {Observable} from "rxjs";
import axios from "axios";

export class Neo4jNodeMenuService {
    getExpandNodeMenu(nodeId, type) {
        return new Observable( ( observer ) => {
            axios.get( '/api/neo4j/node/menu/' + type,  {params: {'id': nodeId}})
                .then( ( response ) => {
                    observer.next( response.data );
                    observer.complete();
                } )
                .catch( ( error ) => {
                    observer.error( error );
                } );
        });
    }

    expandNodeOnQuery (nodeId, label, direction, type) {
        return new Observable( ( observer ) => {
            axios.get( '/api/neo4j/node/expand/' + type, {params: {'id': nodeId, 'label': label, 'direction': direction}} )
                .then( ( response ) => {
                    observer.next( response.data );
                    observer.complete();
                } )
                .catch( ( error ) => {
                    observer.error( error );
                } );
        });
    }

    expandNodeQuery(nodeId) {
        return new Observable( ( observer ) => {
            axios.get( '/api/neo4j/node/expand', {params: {'id': nodeId}})
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
