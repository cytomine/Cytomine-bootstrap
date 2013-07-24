class JobUrlMappings {

    static mappings = {
        /* Job */
        "/api/job"(controller:"restJob"){
            action = [GET: "list",POST:"add"]
        }
        "/api/job/$id"(controller:"restJob"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
        "/api/job/$id/alldata"(controller:"restJob") {
            action = [DELETE: "deleteAllJobData", GET: "listAllJobData"]
        }
        "/api/job/$id/execute" (controller : "restJob") {
            action = [POST : "execute"]
        }
        "/api/job/$id/preview_roi" (controller : "restJob") {
            action = [GET : "getPreviewRoi"]
        }
        "/api/job/$id/preview" (controller : "restJob") {
            action = [POST : "preview", GET : "getPreview"]
        }

        "/api/project/$id/job/purge"(controller : "restJob") {
            action = [POST : "purgeJobNotReviewed", GET : "purgeJobNotReviewed"]
        }
    }
}
