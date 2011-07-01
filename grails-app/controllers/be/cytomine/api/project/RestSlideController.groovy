package be.cytomine.api.project

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

    def list = {
        responseSuccess(Slide.list())
    }

  def listByUser = {
    log.info "List with id user:"+params.id
    User user=null
    if(params.id!=null) {
      user = User.read(params.id)
    } else {
       user = getCurrentUser(springSecurityService.principal.id)
    }

    if(user!=null) responseSuccess(user.slides())
    else responseNotFound("User",params.id)
  }

    def show = {
        log.info "Show:"+ params.id
        Slide slide = Slide.read(params.id)
        if(slide) responseSuccess(slide)
        else responseNotFound("Slide",params.id)
    }
}
