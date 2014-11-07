/**
 * Cytomine @ GIGA-ULG
 * User: rhoyoux
 * Date: 30/10/14
 * Time: 10:19
 */
class SearchEngineFilterUrlMappings {

    static mappings = {
        "/api/searchenginefilter.$format"(controller:"restSearchEngineFilter"){
            action = [GET: "list"]
        }
        "/api/user/$id/searchenginefilter.$format"(controller:"restSearchEngineFilter"){
            action = [GET:"listByUser",POST:"add"]
        }
        "/api/user/$id/searchenginefilter/$id.$format"(controller:"restSearchEngineFilter"){
            action = [GET:"show",DELETE:"delete"]
        }
    }
}
