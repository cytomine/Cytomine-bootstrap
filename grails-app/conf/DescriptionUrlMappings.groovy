class DescriptionUrlMappings {

    static mappings = {
        /* Job */
        "/api/description.$format"(controller:"restJob"){
            action = [GET: "list",POST:"add"]
        }
        "/api/domain/$domainClassName/$domainIdent/description.$format"(controller:"restDescription"){
            action = [GET:"showByDomain",POST:"add",PUT:"update", DELETE:"delete"]
        }
    }
}
