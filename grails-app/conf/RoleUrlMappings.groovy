/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class RoleUrlMappings {

    static mappings = {
        "/api/role"(controller:"restSecRole") {
            action = [GET:"list", POST:"save"]
        }
    }
}
