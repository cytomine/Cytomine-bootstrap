package be.cytomine.api.image

import be.cytomine.api.RestController
import be.cytomine.project.Slide
import be.cytomine.security.User

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 30/05/11
 * Time: 13:47
 */
class RestSlideController extends RestController {

    def springSecurityService
    def userService
    def slideService
    def cytomineService

    def list = {
        responseSuccess(slideService.list())
    }

    def listByUser = {
        User user = null
        if (params.id != null) {
            user = userService.read(params.long('id'))
        } else {
            user = cytomineService.getCurrentUser()
        }

        if (user != null) {
            String page = params.page
            String limit = params.rows
            String sortedRow = params.sidx
            String sord = params.sord
            if (page || limit || sortedRow || sord)
                responseSuccess(slideService.list(user, page, limit, sortedRow, sord))
            else
                responseSuccess(slideService.list(user))

        }
        else responseNotFound("User", params.id)
    }


    def show = {
        Slide slide = slideService.read(params.long('id'))
        if (slide) responseSuccess(slide)
        else responseNotFound("Slide", params.id)
    }
}
