/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class ImageSequenceUrlMappings {

    static mappings = {
        /* Image Instance */
        "/api/imagesequence.$format"(controller: "restImageSequence"){
            action = [POST:"add"]
        }
        "/api/imagesequence/$id.$format"(controller: "restImageSequence"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
        "/api/imagegroup/$id/imagesequence.$format"(controller: "restImageSequence"){
            action = [GET:"listByImageGroup"]
        }
        "/api/imageinstance/$id/imagesequence.$format"(controller: "restImageSequence"){
            action = [GET:"getByImageInstance"]
        }
        "/api/imageinstance/$id/imagesequence/possibilities.$format"(controller: "restImageSequence"){
            action = [GET:"getSequenceInfo"]
        }

        "/api/imagegroup/$id/$channel/$zstack/$slice/$time/imagesequence.$format"(controller: "restImageSequence"){
            action = [GET:"getByImageGroupAndIndex"]
        }

    }
}
