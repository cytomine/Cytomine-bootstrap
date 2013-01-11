package be.cytomine.api.laboratory

import be.cytomine.Exception.CytomineException
import be.cytomine.api.RestController
import be.cytomine.laboratory.Sample
import be.cytomine.security.SecUser
import grails.converters.JSON

/**
 * //TODO:: document/refactor/test this class
 */
class RestSampleController extends RestController {

    def springSecurityService
    def userService
    def sampleService
    def cytomineService

    def list = {
        responseSuccess(sampleService.list(cytomineService.getCurrentUser()))
    }


    def show = {
        Sample sample = sampleService.read(params.long('id'))
        if (sample) {
            responseSuccess(sample)
        } else {
            responseNotFound("Sample", params.id)
        }
    }

    def add = {
        try {
            def json = request.JSON
            def result = sampleService.add(json)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }


    def update = {
        def json = request.JSON
        try {
            def domain = sampleService.retrieve(json)
            def result = sampleService.update(domain,json)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    def delete = {
        def json = JSON.parse("{id : $params.id}")
        try {
            def domain = sampleService.retrieve(json)
            def result = sampleService.delete(domain,json)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }
}
