/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class UserGroupUrlMappings {

    static mappings = {
        /* UserGroup */
        "/api/user/$user/group.$format"(controller: "restUserGroup") {
            action = [GET:"list", POST:"add"]
        }

        "/api/user/$user/group/$group.$format"(controller: "restUserGroup") {
            action = [GET:"show",  DELETE:"delete"]
        }
    }
}
