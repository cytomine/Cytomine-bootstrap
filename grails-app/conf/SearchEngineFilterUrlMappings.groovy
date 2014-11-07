/**
 * Cytomine @ GIGA-ULG
 * User: rhoyoux
 * Date: 30/10/14
 * Time: 10:19
 */
class SearchEngineFilterUrlMappings {

    static mappings = {
        "/api/searchenginefilter.$format"(controller:"restSearchEngineFilter"){
            action = [GET: "list",POST:"add"]
        }
        "/api/searchenginefilter/$id.$format"(controller:"restSearchEngineFilter"){
            action = [GET:"show",DELETE:"delete"]
        }
    }
}
