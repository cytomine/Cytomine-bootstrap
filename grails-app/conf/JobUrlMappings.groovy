class JobUrlMappings {

    static mappings = {
        /* Job */
        "/api/job"(controller:"restJob"){
            action = [GET: "list",POST:"add"]
        }
        "/api/job/$id"(controller:"restJob"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
        "/api/software/$id/job"(controller:"restJob"){
            action = [GET: "listBySoftware"]
        }
        "/api/software/$idSoftware/project/$idProject/job"(controller:"restJob"){
            action = [GET: "listBySoftwareAndProject"]
        }
    }
}
