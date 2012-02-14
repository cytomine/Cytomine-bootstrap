class SoftwareProjectUrlMappings {

    static mappings = {
        /* Software */
        "/api/softwareproject"(controller:"restSoftwareProject"){
            action = [GET: "list",POST:"add"]
        }
        "/api/softwareproject/$id"(controller:"restSoftwareProject"){
            action = [GET:"show", DELETE:"delete"]
        }
        "/api/software/$id/project"(controller:"restSoftwareProject"){
            action = [GET: "listBySoftware"]
        }
        "/api/project/$id/software"(controller:"restSoftwareProject"){
            action = [GET: "listByProject"]
        }

    }
}
