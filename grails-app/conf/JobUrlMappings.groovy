class JobUrlMappings {

    static mappings = {
        /* Job */
        "/api/job"(controller:"restJob"){
            action = [GET: "list",POST:"add"]
        }
        "/api/project/$id/job"(controller:"restJob"){
            action = [GET: "listByProject",POST: "add"]
        }
        "/api/job/$id"(controller:"restJob"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
        "/api/software/$idSoftware/project/$idProject/job"(controller:"restJob"){
            action = [GET: "listBySoftwareAndProject"]
        }
        "/api/job/$id/alldata"(controller:"restJob") {
            action = [DELETE: "deleteAllJobData", GET: "listAllJobData"]
        }
    }
}
