package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.processing.Software
import be.cytomine.processing.SoftwareParameter
import grails.converters.JSON

class RestSoftwareParameterController extends RestController{

    def softwareParameterService


    def list = {
        responseSuccess(softwareParameterService.list())
    }

    def listBySoftware = {
        Software software = Software.read(params.long('id'))
        if(software) responseSuccess(softwareParameterService.list(software))
        else responseNotFound("Software", params.id)
    }

    def show = {
        SoftwareParameter parameter = softwareParameterService.read(params.long('id'))
        if (parameter) responseSuccess(parameter)
        else responseNotFound("SoftwareParameter", params.id)
    }


    def add = {
        add(softwareParameterService, request.JSON)
    }

    def update = {
        update(softwareParameterService, request.JSON)
    }

    def delete = {
        delete(softwareParameterService, JSON.parse("{id : $params.id}"))
    }
}
