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
    }
}
