class SoftwareProjectUrlMappings {

    static mappings = {
        /* Software */
        "/api/softwareproject"(controller:"restSoftwareProject"){
            action = [GET: "list",POST:"add"]
        }
        "/api/softwareproject/$id"(controller:"restSoftwareProject"){
            action = [GET:"show", DELETE:"delete"]
        }
        "/api/project/$id/softwareproject"(controller:"restSoftwareProject"){
            action = [GET: "listByProject"]
        }
    }
}
