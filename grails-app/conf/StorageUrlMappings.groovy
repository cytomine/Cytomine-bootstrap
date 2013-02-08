
/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 7/02/13
 * Time: 11:06
 */
class StorageUrlMappings {

    static mappings = {
        /* Storage */
        "/api/storage"(controller: "restStorage"){
            action = [GET:"list", POST:"add"]
        }
        "/api/storage/$id"(controller: "restStorage"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
    }
}