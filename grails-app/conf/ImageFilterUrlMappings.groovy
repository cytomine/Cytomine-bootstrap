/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class ImageFilterUrlMappings {

    static mappings = {
        /* Image Filters */
        "/api/imagefilter.$format"(controller: "restImageFilter"){
            action = [GET:"list"]
        }
        "/api/imagefilter/$id.$format"(controller: "restImageFilter"){
            action = [GET:"show"]
        }

        "/api/project/imagefilter.$format"(controller: "restImageFilter"){
            action = [GET:"list"]
        }
        "/api/project/imagefilter/$id.$format"(controller: "restImageFilter"){
            action = [GET:"show"]
        }

        "/api/project/$project/imagefilterproject.$format"(controller: "restImageFilterProject"){
            action = [GET:"listByProject"]
        }
        "/api/imagefilterproject.$format" (controller: "restImageFilterProject"){
            action = [GET:"list", POST : "add"]
        }
        "/api/imagefilterproject/$id.$format"(controller: "restImageFilterProject"){
            action = [DELETE : "delete"]
        }
    }
}
