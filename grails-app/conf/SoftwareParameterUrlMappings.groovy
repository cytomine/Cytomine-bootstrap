class SoftwareParameterUrlMappings {

    static mappings = {
        /* Software */
        "/api/softwareparameter.$format"(controller:"restSoftwareParameter"){
            action = [GET: "list",POST:"add"]
        }
        "/api/softwareparameter/$id.$format"(controller:"restSoftwareParameter"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
        "/api/software/$id/parameter.$format"(controller:"restSoftwareParameter"){
            action = [GET: "listBySoftware"]
        }
    }
}
