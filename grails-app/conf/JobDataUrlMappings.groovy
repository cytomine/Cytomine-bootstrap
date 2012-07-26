class JobDataUrlMappings {

    static mappings = {
        /* Job */
        "/api/jobdata"(controller: "restJobData"){
            action = [GET:"list", POST:"add"]
        }
        "/api/jobdata/$id"(controller: "restJobData"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }

        "/api/job/$id/jobdata"(controller: "restJobData"){
            action = [GET:"listByJob"]
        }

        "/api/jobdata/$id/upload"(controller:"restJobData"){
            action = [PUT:"upload", POST: "upload"]
        }

        "/api/jobdata/$id/download"(controller:"restJobData"){
            action = [GET: "download"]
        }
    }
}
