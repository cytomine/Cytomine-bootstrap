package be.cytomine.api.project

import be.cytomine.api.RestController
import be.cytomine.project.Discipline
import grails.converters.JSON

class RestDisciplineController extends RestController {

    def disciplineService

    def list = {
        responseSuccess(disciplineService.list())
    }

    def show = {
        Discipline discipline = disciplineService.read(params.long('id'))
        if (discipline) responseSuccess(discipline)
        else responseNotFound("Discipline", params.id)
    }

    def add = {
        add(disciplineService, request.JSON)
    }

    def update = {
        update(disciplineService, request.JSON)
    }

    def delete = {
        delete(disciplineService, JSON.parse("{id : $params.id}"))
    }

}
