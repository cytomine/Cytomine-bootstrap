package be.cytomine.api.project

import be.cytomine.api.RestController
import be.cytomine.project.Discipline
import grails.converters.JSON

/**
 * Controller for discipline
 * A discipline can be link with a project
 */
class RestDisciplineController extends RestController {

    def disciplineService

    /**
     * List all discipline
     */
    def list = {
        responseSuccess(disciplineService.list())
    }

    /**
     * Get a single discipline
     */
    def show = {
        Discipline discipline = disciplineService.read(params.long('id'))
        if (discipline) {
            responseSuccess(discipline)
        } else {
            responseNotFound("Discipline", params.id)
        }
    }

    /**
     * Add a new discipline
     */
    def add = {
        add(disciplineService, request.JSON)
    }

    /**
     * Update a existing discipline
     */
    def update = {
        update(disciplineService, request.JSON)
    }

    /**
     * Delete discipline
     */
    def delete = {
        delete(disciplineService, JSON.parse("{id : $params.id}"))
    }

}
