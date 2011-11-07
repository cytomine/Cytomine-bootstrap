/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class UserGroupUrlMappings {

    static mappings = {
        /* UserGroup */
        "/api/user/$user/group"(controller: "restUserGroup") {
            action = [GET:"list", POST:"save"]
        }

        "/api/user/$user/group/$group"(controller: "restUserGroup") {
            action = [GET:"show",  DELETE:"delete"]
        }
    }
}
