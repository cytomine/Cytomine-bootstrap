/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class SlideUrlMappings {

    static mappings = {
        /* Slide */
        "/api/slide"(controller: "restSlide"){
            action = [GET:"list", POST:"add"]
        }
        "/api/slide/$id"(controller: "restSlide"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
        "/api/currentuser/slide"(controller: "restSlide"){
            action = [GET:"listByUser"]
        }
        "/api/project/$idproject/slide"(controller:"restProjectSlide"){
            action = [GET: "listSlideByProject"]
        }
        "/api/slide/$idslide/project"(controller:"restProjectSlide"){
            action = [GET: "listProjectBySlide"]
        }

    }
}
