/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class StatsUrlMappings {

    static mappings = {
        "/api/project/$id/stats/term.$format"(controller:"stats"){
            action = [GET:"statTerm"]
        }
        "/api/project/$id/stats/user.$format"(controller:"stats"){
            action = [GET:"statUser"]
        }
        "/api/project/$id/stats/termslide.$format"(controller:"stats"){
            action = [GET:"statTermSlide"]
        }
        "/api/project/$id/stats/userslide.$format"(controller:"stats"){
            action = [GET:"statUserSlide"]
        }
        "/api/project/$id/stats/userannotations.$format"(controller:"stats"){
            action = [GET:"statUserAnnotations"]
        }
        "/api/project/$id/stats/retrievalsuggestion.$format"(controller:"stats"){
            action = [GET:"statRetrievalsuggestion"]
        }
        "/api/project/$id/stats/annotationevolution.$format"(controller:"stats"){
            action = [GET:"statAnnotationEvolution"]
        }

        "/api/stats/retrieval/avg.$format"(controller:"retrievalSuggestStats"){
            action = [GET:"statRetrievalAVG"]
        }
        "/api/stats/retrieval/confusionmatrix.$format"(controller:"retrievalSuggestStats"){
            action = [GET:"statRetrievalConfusionMatrix"]
        }
        "/api/stats/retrieval/worstTerm.$format"(controller:"retrievalSuggestStats"){
            action = [GET:"statRetrievalWorstTerm"]
        }
        "/api/stats/retrieval/worstTermWithSuggest.$format"(controller:"retrievalSuggestStats"){
            action = [GET:"statWorstTermWithSuggestedTerm"]
        }
        "/api/stats/retrieval/worstAnnotation.$format"(controller:"retrievalSuggestStats"){
            action = [GET:"statRetrievalWorstAnnotation"]
        }
        "/api/stats/retrieval/evolution.$format"(controller:"retrievalSuggestStats"){
            action = [GET:"statRetrievalEvolution"]
        }


        "/api/stats/retrieval-evolution/evolution.$format"(controller:"retrievalEvolutionStats"){
            action = [GET:"statRetrievalEvolution"]
        }
        "/api/stats/retrieval-evolution/evolutionByTerm.$format"(controller:"retrievalEvolutionStats"){
            action = [GET:"statRetrievalEvolutionByTerm"]
        }




    }
}
