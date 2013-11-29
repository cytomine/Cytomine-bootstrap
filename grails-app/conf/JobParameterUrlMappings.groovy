class JobParameterUrlMappings {

    static mappings = {
        "/api/jobparameter.$format"(controller:"restJobParameter"){
            action = [GET: "list",POST:"add"]
        }
        "/api/jobparameter/$id.$format"(controller:"restJobParameter"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
        "/api/job/$id/parameter.$format"(controller:"restJobParameter"){
            action = [GET: "listByJob"]
        }
    }
}
