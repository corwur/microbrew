module Genome exposing (Request(..), Response(..), handleRequest, Dna, Nucleotide, decodeDna, encodeDna)
import Http
import Url
import Url.Builder
import Json.Decode exposing (Decoder, field, int)
import Json.Decode as Decode exposing (Decoder, int, string, float)
import Json.Decode.Pipeline exposing (required, optional, hardcoded)

type Nucleotide = A | T | C | G | X
type Dna = Dna (List Nucleotide) | EmptyDna

type alias Alignments =
    { alignment: List String
    , nextPageToken: String
    }

type alias Sequence =
    { name: String
    , length: Int
    , referenceIndex: Int
    }

type Sequences = Sequences (List Sequence)

type Request =
    SequenceDictionary |
    Reference {contig:String, start:Int, end:Int} |
    Reads {table:String, contig:String, start:Int, end:Int} |
    Variants {table:String, contig:String, start:Int, end:Int, binning:Int}

type Response =
    SequenceDictionaryResponse (Result Error Sequences)|
    ReferenceResponse (Result Error Dna) |
    ReadsResponse (Result Error String) |
    VariantsResponse (Result Error String) |
    Empty

type Error = Error


decodeSequences: Decoder Sequences
decodeSequences=Json.Decode.map Sequences (Json.Decode.list decodeSequence)

decodeSequence: Decoder Sequence
decodeSequence =
    Decode.succeed Sequence
        |> optional "name" string "chr17"
        |> optional "length" int 0
        |> optional "referenceIndex" int 100


handle: ((Result Error b) -> Response) ->  (Result Http.Error b) -> Response
handle f result =
    case result of
        Ok value -> f (Ok value)
        Err err -> f(Err Error)

handleWithMap: (a->b) -> ((Result Error b) -> Response) ->  (Result Http.Error a) -> Response
handleWithMap m f result =
    case result of
        Ok value -> f (Ok (m value))
        Err err -> f(Err Error)


handleRequest: Request -> Cmd Response
handleRequest request =
    case request of
        SequenceDictionary -> Http.get
            { url =Url.Builder.absolute ["api", "genome", "sequenceDictionary"] []
            , expect = Http.expectJson (handle SequenceDictionaryResponse) decodeSequences
            }
        Reference data -> Http.get
            { url =Url.Builder.absolute ["api", "genome", "reference", data.contig ] [ Url.Builder.int "start" data.start, Url.Builder.int "end" data.end ]
            , expect = Http.expectString (handleWithMap decodeDna ReferenceResponse)
            }
        Reads data -> Http.get
           { url =Url.Builder.absolute ["api", "genome", "reads", data.table, data.contig ] [ Url.Builder.int"start" data.start, Url.Builder.int "end" data.end ]
           , expect = Http.expectString (handle ReadsResponse)
           }
        Variants data-> Http.get
           { url =Url.Builder.absolute ["api", "genome", "reads", data.table, data.contig ] [ Url.Builder.int"start" data.start, Url.Builder.int "end" data.end, Url.Builder.int "binning" data.binning ]
           , expect = Http.expectString (handle VariantsResponse)
           }


charToNucleotoid c =
    case c of
        'A' -> A
        'T' -> T
        'C' -> C
        'G' -> G
        _ -> X
nucleotoidToChar: Nucleotide -> Char
nucleotoidToChar n =
    case n of
        A -> 'A'
        T -> 'T'
        C -> 'C'
        G -> 'G'
        X -> '_'

decodeDna : String -> Dna
decodeDna string =
    Dna(List.map charToNucleotoid (String.toList string))


encodeDna: Dna -> String
encodeDna dna =
    case dna of
        Dna nucletoides -> String.fromList (List.map nucleotoidToChar nucletoides)
        EmptyDna -> ""
