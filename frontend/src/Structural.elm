module Pathway exposing(Pathway(..), Request(..), Response(..), handleRequest)

import Http
import Url
import Url.Builder
import Json.Decode exposing (Decoder, field, int)
import Json.Decode as Decode exposing (Decoder, int, string, float)
import Json.Decode.Pipeline exposing (required, optional, hardcoded)


type Pathway = CypherResult String
type Error = Error
type Request = CypherQuery String
type Response = QueryResponse Pathway| ErrorResponse



handle: (String -> Response) -> (Result Http.Error String) -> Response
handle fn result=
    case Result.map fn result of
        Ok value -> value
        Err error -> ErrorResponse

decodePathway: String -> Response
decodePathway value =
    QueryResponse (CypherResult value)

handleRequest: Request -> Cmd Response
handleRequest request = case request of
    CypherQuery query -> Http.post
        { url = Url.Builder.absolute ["api", "pathway", "query"] []
        , body = Http.stringBody "text/plain" query
        , expect = Http.expectString (handle decodePathway)
        }