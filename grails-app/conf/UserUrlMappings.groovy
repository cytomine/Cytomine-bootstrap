/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class UserUrlMappings {

    static mappings = {
        /* User */
        "/api/user"(controller:"restUser"){
            action = [GET:"list", POST:"add"]
        }
        "/api/user/$id"(controller:"restUser"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
        "/api/user/grid"(controller:"restUser"){
            action = [GET:"grid"]
        }
        "/api/user/current"(controller:"restUser"){
            action = [GET:"showCurrent"]
        }

        "/api/userJob"(controller:"restUser"){
            action = [POST:"addChild"]
        }
        "/api/userJob/$id"(controller:"restUser"){
            action = [GET:"showUserJob"]
        }
    }
}
