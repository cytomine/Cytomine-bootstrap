/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:51
 */
class GroupUrlMappings {

    static mappings = {
        /* Group */
        "/api/group"(controller: "restGroup"){
            action = [GET:"list", POST:"add"]
        }
        "/api/group/$id"(controller: "restGroup"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
        "/api/group/$idgroup/image"(controller:"restImage"){
            action = [GET: "listByGroup"]
        }
        "/api/group/grid"(controller:"restGroup"){
            action = [GET:"grid"]
        }
    }
}

