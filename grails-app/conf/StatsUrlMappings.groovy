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
        "/api/stats/retrieval/avg"(controller:"stats"){
            action = [GET:"statRetrievalAVG"]
        }
        "/api/stats/retrieval/confusionmatrix"(controller:"stats"){
            action = [GET:"statRetrievalConfusionMatrix"]
        }
        "/api/stats/retrieval/worstTerm"(controller:"stats"){
            action = [GET:"statRetrievalWorstTerm"]
        }
        "/api/stats/retrieval/worstTermWithSuggest"(controller:"stats"){
            action = [GET:"statWorstTermWithSuggestedTerm"]
        }
        "/api/stats/retrieval/worstAnnotation"(controller:"stats"){
            action = [GET:"statRetrievalWorstAnnotation"]
        }
        "/api/stats/retrieval/evolution"(controller:"stats"){
            action = [GET:"statRetrievalEvolution"]
        }
        "/api/downloadPDF" (controller : "stats") {
            action = [GET:"convertHtmlContentToPDF"]
        }
    }
}
