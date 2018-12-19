import Browser
import Browser.Navigation as Navigation
import Html exposing (Html, button, input, div, text, textarea)
import Html.Events exposing (onClick, onInput)
import Html.Attributes exposing (..)
import Url
import Genome
import Pathway

main =
  Browser.application
        { init = init
        , update = update
        , view = view
        , subscriptions = subscriptions
        , onUrlChange = UrlChanged
        , onUrlRequest  = LinkClicked
        }


type alias Model =
    { response : Genome.Response
    , request: Genome.Request
    , query: Pathway.Request
    , pathway: Pathway.Response
    , start: Int
    , end: Int
    , chromosome: String
    }


type Msg =
    LinkClicked Browser.UrlRequest |
    UrlChanged Url.Url |
    GenomeRequest Genome.Request |
    GenomeResponse Genome.Response |
    PathwayRequest Pathway.Request |
    PathwayResponse Pathway.Response |
    ChangeStart String |
    ChangeEnd String |
    ChangePathwayQuery String

initialModel: Model
initialModel = {
    response = Genome.Empty
    ,request = Genome.Reference {
                   contig = "chr17"
                   , start = 0
                   , end = 0
                   }
        ,query = Pathway.CypherQuery "MATCH (n) RETURN n LIMIT 25"
        ,pathway = Pathway.QueryResponse (Pathway.CypherResult "")
        ,start = 0
        , end = 100
        ,chromosome = "chr17"
    }

init : () -> Url.Url -> Navigation.Key -> (Model, Cmd msg)
init flags url key =( initialModel, Cmd.none )

toInt: Maybe Int -> Int -> Int
toInt value default =
    case value of
        Just i -> i
        Nothing -> default

update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
  case msg of
      GenomeRequest request -> (model, Cmd.map GenomeResponse (Genome.handleRequest request))
      GenomeResponse response -> ({ model | response = response }, Cmd.none)
      PathwayRequest request -> (model, Cmd.map PathwayResponse (Pathway.handleRequest request))
      PathwayResponse response -> ({model | pathway = response }, Cmd.none)
      ChangeStart value -> ({ model | start = toInt (String.toInt value) model.start}, Cmd.none)
      ChangeEnd value -> ({ model | end = toInt (String.toInt value) model.end}, Cmd.none)
      ChangePathwayQuery query -> ({ model | query = Pathway.CypherQuery query}, Cmd.none)
      LinkClicked _ -> (model, Cmd.none)
      UrlChanged _ -> (model, Cmd.none)


subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.none

queryAsString : Pathway.Request -> String
queryAsString request = case request of
    Pathway.CypherQuery query -> query

view : Model -> Browser.Document Msg
view model = {
    title = "Micro Brewery"
    , body = [
        div [ class ""]
            [ div [] [ input [ placeholder "Start", value (String.fromInt model.start), onInput ChangeStart ]  []
                    , input [ placeholder "End", value (String.fromInt model.end), onInput ChangeEnd ]  []
             ]
             , div []
                [ button [ onClick (GenomeRequest (Genome.Reference { contig = "chr17", start = model.start , end = model.end })) ] [ text "get reference" ]
                ,button [ onClick (GenomeRequest (Genome.Reads { table="abc", contig = "chr17", start = model.start , end = model.end })) ] [ text "get reads" ]
                ,button [ onClick (GenomeRequest (Genome.Variants { table="abc", contig = "chr17", start = model.start , end = model.end, binning=10 })) ] [ text "get variants" ]
                , div [] [ case model.response of
                     Genome.ReferenceResponse (Ok dna) -> text (Genome.encodeDna dna)
                     Genome.ReferenceResponse (Err error) -> text "Error"
                     Genome.Empty -> text "empty"
                     _ -> text "somthing else"
                 ]
                ]
            , div [] [
                 div [] [ textarea [ cols 40, rows 10, value ( queryAsString model.query ) , onInput ChangePathwayQuery ] [] ]
                ,div [] [ button [ onClick (PathwayRequest model.query) ] [ text "Pathway Cypher Query" ] ]
                , div [] [ case model.pathway of
                        Pathway.QueryResponse (Pathway.CypherResult result) -> text result
                        _ -> text "somthing else"
                    ]
                ]
            ]
         ]
    }
