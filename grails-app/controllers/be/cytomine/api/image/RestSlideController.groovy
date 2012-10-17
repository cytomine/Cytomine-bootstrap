package be.cytomine.api.image

import be.cytomine.Exception.CytomineException
import be.cytomine.api.RestController
import be.cytomine.project.Slide
import be.cytomine.security.SecUser
import grails.converters.JSON

class RestSlideController extends RestController {

    def springSecurityService
    def userService
    def slideService
    def cytomineService

    def list = {
        responseSuccess(slideService.list())
    }

    def listByUser = {
        SecUser user
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

    def add = {
        try {
            def json = request.JSON
            def result = slideService.add(json)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }


    def update = {
        def json = request.JSON
        try {
            def domain = slideService.retrieve(json)
            def result = slideService.update(domain,json)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    def delete = {
        def json = JSON.parse("{id : $params.id}")
        try {
            def domain = slideService.retrieve(json)
            def result = slideService.delete(domain,json)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }
}
