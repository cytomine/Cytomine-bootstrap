class SoftwareProjectUrlMappings {

    static mappings = {
        /* Software */
        "/api/softwareproject.$format"(controller:"restSoftwareProject"){
            action = [GET: "list",POST:"add"]
        }
        "/api/softwareproject/$id.$format"(controller:"restSoftwareProject"){
            action = [GET:"show", DELETE:"delete"]
        }
        "/api/project/$id/softwareproject.$format"(controller:"restSoftwareProject"){
            action = [GET: "listByProject"]
        }
        "/api/project/$idProject/software/$idSoftware/stats.$format"(controller:"restSoftware"){
            action = [GET: "softwareInfoForProject"]
        }

    }
}
