
/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 7/02/13
 * Time: 11:06
 */
class StorageUrlMappings {

    static mappings = {
        /* Storage */
        "/api/storage.$format"(controller: "restStorage"){
            action = [GET:"list", POST:"add"]
        }
        "/api/storage/$id.$format"(controller: "restStorage"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
        "/api/storage/create/$user.$format"(controller : "restStorage") {
            action = [POST:"create"]
        }



        "/api/imageserver.$format"(controller : "restStorage") {
            action = [GET:"listByMime"]
        }
    }
}