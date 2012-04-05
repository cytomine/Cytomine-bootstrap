/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class ImageFilterUrlMappings {

    static mappings = {
        /* Image Filters */
        "/api/imagefilter"(controller: "restImageFilter"){
            action = [GET:"list"]
        }
        "/api/imagefilter/$id"(controller: "restImageFilter"){
            action = [GET:"show"]
        }

        "/api/project/imagefilter"(controller: "restImageFilter"){
            action = [GET:"list"]
        }
        "/api/project/imagefilter/$id"(controller: "restImageFilter"){
            action = [GET:"show"]
        }

        "/api/project/$project/imagefilterproject"(controller: "restImageFilterProject"){
            action = [GET:"listByProject"]
        }
        "/api/imagefilterproject" (controller: "restImageFilterProject"){
            action = [GET:"list", POST : "add"]
        }
        "/api/imagefilterproject/$id"(controller: "restImageFilterProject"){
            action = [DELETE : "delete"]
        }
    }
}
