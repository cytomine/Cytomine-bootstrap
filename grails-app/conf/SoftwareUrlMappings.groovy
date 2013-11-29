class SoftwareUrlMappings {

    static mappings = {
        /* Software */
        "/api/software.$format"(controller:"restSoftware"){
            action = [GET: "list",POST:"add"]
        }
        "/api/software/$id.$format"(controller:"restSoftware"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
        "/api/project/$id/software.$format"(controller:"restSoftware"){
            action = [GET: "listByProject"]
        }

    }
}
