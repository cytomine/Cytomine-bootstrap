class JobParameterUrlMappings {

    static mappings = {
        "/api/jobparameter"(controller:"restJobParameter"){
            action = [GET: "list",POST:"add"]
        }
        "/api/jobparameter/$id"(controller:"restJobParameter"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
        "/api/job/$id/parameter"(controller:"restJobParameter"){
            action = [GET: "listByJob"]
        }
    }
}
