/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class StatsUrlMappings {

    static mappings = {
        "/api/project/$id/stats/term"(controller:"stats"){
            action = [GET:"statTerm"]
        }
        "/api/project/$id/stats/user"(controller:"stats"){
            action = [GET:"statUser"]
        }
        "/api/project/$id/stats/termslide"(controller:"stats"){
            action = [GET:"statTermSlide"]
        }
        "/api/project/$id/stats/userslide"(controller:"stats"){
            action = [GET:"statUserSlide"]
        }
        "/api/project/$id/stats/userannotations"(controller:"stats"){
            action = [GET:"statUserAnnotations"]
        }
        "/api/project/$id/stats/retrievalsuggestion"(controller:"stats"){
            action = [GET:"statRetrievalsuggestion"]
        }


        "/api/stats/retrieval/avg"(controller:"retrievalSuggestStats"){
            action = [GET:"statRetrievalAVG"]
        }
        "/api/stats/retrieval/confusionmatrix"(controller:"retrievalSuggestStats"){
            action = [GET:"statRetrievalConfusionMatrix"]
        }
        "/api/stats/retrieval/worstTerm"(controller:"retrievalSuggestStats"){
            action = [GET:"statRetrievalWorstTerm"]
        }
        "/api/stats/retrieval/worstTermWithSuggest"(controller:"retrievalSuggestStats"){
            action = [GET:"statWorstTermWithSuggestedTerm"]
        }
        "/api/stats/retrieval/worstAnnotation"(controller:"retrievalSuggestStats"){
            action = [GET:"statRetrievalWorstAnnotation"]
        }
        "/api/stats/retrieval/evolution"(controller:"retrievalSuggestStats"){
            action = [GET:"statRetrievalEvolution"]
        }


        "/api/stats/retrieval-evolution/evolution"(controller:"retrievalEvolutionStats"){
            action = [GET:"statRetrievalEvolution"]
        }
    }
}
