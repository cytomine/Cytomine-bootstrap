/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class ImageGroupUrlMappings {

    static mappings = {
        /* Image Instance */
        "/api/imagegroup"(controller: "restImageGroup"){
            action = [POST:"add"]
        }
        "/api/imagegroup/$id"(controller: "restImageGroup"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
        "/api/project/$id/imagegroup"(controller: "restImageGroup"){
            action = [GET:"listByProject"]
        }
    }
}
