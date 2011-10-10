/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class ImageInstanceUrlMappings {

    static mappings = {
        /* Image Instance */
        "/api/imageinstance"(controller: "restImageInstance"){
            action = [GET:"list", POST:"add"]
        }
        "/api/imageinstance/$id"(controller: "restImageInstance"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
    }
}
