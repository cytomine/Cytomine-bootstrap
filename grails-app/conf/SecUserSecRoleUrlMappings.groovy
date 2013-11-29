/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class SecUserSecRoleUrlMappings {

    static mappings = {
        /* SecUserSecRole */
        "/api/user/$user/role.$format"(controller: "restSecUserSecRole") {
            action = [GET:"list", POST:"add"]
        }

        "/api/user/$user/role/$role.$format"(controller: "restSecUserSecRole") {
            action = [GET:"show", DELETE:"delete"]
        }
    }
}
