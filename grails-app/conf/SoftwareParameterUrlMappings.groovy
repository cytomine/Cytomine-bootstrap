class SoftwareParameterUrlMappings {

    static mappings = {
        /* Software */
        "/api/softwareparameter"(controller:"restSoftwareParameter"){
            action = [GET: "list",POST:"add"]
        }
        "/api/softwareparameter/$id"(controller:"restSoftwareParameter"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
        "/api/software/$id/parameter"(controller:"restSoftwareParameter"){
            action = [GET: "listBySoftware"]
        }


    }
}
