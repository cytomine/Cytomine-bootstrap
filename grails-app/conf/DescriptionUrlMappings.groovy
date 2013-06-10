class DescriptionUrlMappings {

    static mappings = {
        /* Job */
        "/api/description"(controller:"restJob"){
            action = [GET: "list",POST:"add"]
        }
        "/api/domain/$domainClassName/$domainIdent/description"(controller:"restDescription"){
            action = [GET:"showByDomain",POST:"add",PUT:"update", DELETE:"delete"]
        }
    }
}
