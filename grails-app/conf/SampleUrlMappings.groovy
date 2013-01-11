/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class SampleUrlMappings {

    static mappings = {
        /* Sample */
        "/api/sample"(controller: "restSample"){
            action = [GET:"list", POST:"add"]
        }
        "/api/sample/$id"(controller: "restSample"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
    }
}
