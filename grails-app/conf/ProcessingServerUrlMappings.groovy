class ProcessingServerUrlMappings {

    static mappings = {
        "/api/processing_server"(controller: "restProcessingServer"){
            action = [GET:"list"]
        }
        "/api/processing_server/$id"(controller: "restProcessingServer"){
            action = [GET:"show"]
        }
    }
}