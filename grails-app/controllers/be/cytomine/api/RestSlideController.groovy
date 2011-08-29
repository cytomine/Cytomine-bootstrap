package be.cytomine.api

import be.cytomine.api.RestController
import be.cytomine.project.Slide
import be.cytomine.security.User
import grails.orm.PagedResultList

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
        def data = [:]
        User user=null
        if(params.id!=null) {
          user = User.read(params.id)
        } else {
           user = getCurrentUser(springSecurityService.principal.id)
        }

        String page = params.page
        String limit = params.rows
        String sortedRow = params.sidx
        String sord = params.sord

        log.info "page="+page + " limit="+limit+ " sortedRow="+sortedRow  +" sord="+sord



        if(params.page || params.rows || params.sidx || params.sord) {
          int pg = Integer.parseInt(page)-1
          int max = Integer.parseInt(limit)
          int offset = pg * max

          PagedResultList results = user.slides(max,offset,sortedRow,sord)
          data.page = pg+""
          data.records = results.totalCount
          data.total =  Math.ceil(results.totalCount/max)+"" //[100/10 => 10 page]
          data.rows = results.list
        }
        else {
           data = user?.slides()
        }

        if(user!=null) responseSuccess(data)
        else responseNotFound("User",params.id)
      }




































    def show = {
        log.info "Show:"+ params.id
        Slide slide = Slide.read(params.id)
        if(slide) responseSuccess(slide)
        else responseNotFound("Slide",params.id)
    }
}
