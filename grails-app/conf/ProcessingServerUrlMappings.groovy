class ProcessingServerUrlMappings {

    static mappings = {
        "/api/processing_server"(controller: "restProcessingServer"){
            action = [GET:"list"]
        }
    }
}