package be.cytomine.api.project

import be.cytomine.api.RestController
import be.cytomine.project.Slide

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 30/05/11
 * Time: 13:47
 */
class RestSlideController extends RestController {

    def springSecurityService

    def list = {
        responseSuccess(Slide.list())
    }

    def show = {
        log.info "Show:"+ params.id
        Slide slide = Slide.read(params.id)
        if(slide) responseSuccess(slide)
        else responseNotFound("Slide",params.id)
    }
}
