/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:51
 */
class GroupUrlMappings {

    static mappings = {
        /* Group */
        "/api/group.$format"(controller: "restGroup"){
            action = [GET:"list", POST:"add"]
        }
        "/api/group/$id.$format"(controller: "restGroup"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
        "/api/image/$idabstractimage/group.$format"(controller:"restGroup"){
            action = [GET: "listGroupByAbstractImage"]
        }

    }
}

