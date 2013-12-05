/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class AttachedFileUrlMappings {

    static mappings = {

        "/api/attachedfile.$format"(controller: "restAttachedFile") {
            action = [GET:"list", POST:"upload",PUT:"upload"]
        }

        "/api/domain/$domainClassName/$domainIdent/attachedfile.$format"(controller: "restAttachedFile") {
            action = [GET:"listByDomain"]
        }

        "/api/attachedfile/$id.$format"(controller: "restAttachedFile") {
            action = [GET:"show"]
        }

        "/api/attachedfile/$id/download"(controller: "restAttachedFile") {
            action = [GET:"download"]
        }
    }
}
