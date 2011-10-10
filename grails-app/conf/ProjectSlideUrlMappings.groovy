/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class ProjectSlideMappings {

    static mappings = {
        "/api/project/$idproject/slide/$idslide"(controller:"restProjectSlide"){
            action = [GET:"show",DELETE:"delete",POST:"add"]
        }
    }
}
