class SoftwareUrlMappings {

    static mappings = {
        /* Software */
        "/api/software"(controller:"restSoftware"){
            action = [GET: "list",POST:"add"]
        }
        "/api/software/$id"(controller:"restSoftware"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
        "/api/project/$id/software"(controller:"restSoftware"){
            action = [GET: "listByProject"]
        }

    }
}
