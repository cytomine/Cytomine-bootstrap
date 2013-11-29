class JobUrlMappings {

    static mappings = {
        /* Job */
        "/api/job.$fomat"(controller:"restJob"){
            action = [GET: "list",POST:"add"]
        }
        "/api/job/$id.$format"(controller:"restJob"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
        "/api/job/$id/alldata.$format"(controller:"restJob") {
            action = [DELETE: "deleteAllJobData", GET: "listAllJobData"]
        }
        "/api/job/$id/execute.$format" (controller : "restJob") {
            action = [POST : "execute"]
        }
        "/api/job/$id/preview_roi.$format" (controller : "restJob") {
            action = [GET : "getPreviewRoi"]
        }
        "/api/job/$id/preview.$format" (controller : "restJob") {
            action = [POST : "preview", GET : "getPreview"]
        }

        "/api/project/$id/job/purge.$format"(controller : "restJob") {
            action = [POST : "purgeJobNotReviewed", GET : "purgeJobNotReviewed"]
        }
    }
}
