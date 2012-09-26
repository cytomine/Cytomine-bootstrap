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
        "/api/user/$id/friends"(controller:"restUser"){
            action = [GET:"listFriends"]
        }
        "/api/userJob"(controller:"restUser"){
            action = [POST:"addChild"]
        }
        "/api/userJob/$id"(controller:"restUser"){
            action = [GET:"showUserJob"]
        }
        "/api/project/$id/user"(controller: "restUser"){
            action = [GET:"showByProject",POST:"addUser"]
        }
        "/api/project/$id/admin"(controller: "restUser"){
            action = [GET:"showAdminByProject"]
        }
        "/api/project/$id/creator"(controller: "restUser"){
            action = [GET:"showCreatorByProject"]
        }
        "/api/project/$id/user/$idUser"(controller: "restUser"){
            action = [DELETE:"deleteUser",POST:"addUser"]
        }
        "/api/project/$id/user/$idUser/admin"(controller: "restUser"){
            action = [DELETE:"deleteUserAdmin",POST:"addUserAdmin"]
        }
        "/api/project/$id/userjob"(controller: "restUser"){
            action = [GET:"listUserJobByProject"]
        }
        "/api/ontology/$id/user"(controller: "restUser"){
            action = [GET:"showUserByOntology"]
        }
        "/api/ontology/$id/creator"(controller: "restUser"){
            action = [GET:"showCreatorByOntology"]
        }
        "/api/project/$id/userlayer"(controller: "restUser"){
            action = [GET:"showLayerByProject"]
        }




        "/api/project/$id/online/user"(controller: "restUser"){
            action = [GET:"listOnlineFriendsWithPosition"]
        }
    }
}
