/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class ImageGroupUrlMappings {

    static mappings = {
        /* Image Instance */
        "/api/imagegroup.$format"(controller: "restImageGroup"){
            action = [POST:"add"]
        }
        "/api/imagegroup/$id.$format"(controller: "restImageGroup"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
        "/api/project/$id/imagegroup.$format"(controller: "restImageGroup"){
            action = [GET:"listByProject"]
        }
    }
}
