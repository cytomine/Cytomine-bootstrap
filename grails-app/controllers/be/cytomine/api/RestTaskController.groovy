package be.cytomine.api

import grails.plugins.springsecurity.Secured

import be.cytomine.Exception.ConstraintException
import be.cytomine.Exception.CytomineException
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.AlgoAnnotationTerm
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.processing.Job
import be.cytomine.processing.JobData
import be.cytomine.processing.Software
import be.cytomine.project.Project
import be.cytomine.security.SecUserSecRole
import be.cytomine.security.User
import be.cytomine.security.UserJob
import grails.converters.JSON
import be.cytomine.command.Task
import be.cytomine.security.SecUser

/**
 * Controller for task
 * A task can be used to provide progress info for long request
 * client ask for a new task id => do long request (with task id as params) => check for request status by looking for task info
 */
class RestTaskController extends RestController {

    def taskService
    def projectService
    def cytomineService

    /**
     * Get a task info
     */
    def show = {
        Task task = taskService.read(params.long('id'))
        if (task) {
            responseSuccess(task)
        } else {
            responseNotFound("Task", params.id)
        }
    }

    /**
     * Create a new task
     */
    def add = {
        Project project = projectService.read(request.JSON.project,new Project())
        SecUser user = cytomineService.getCurrentUser()
        if(!project) {
            responseNotFound("Project", params.project)
        } else {
            Task task = taskService.createNewTask(project,user)
            responseSuccess([task:task])
        }
    }
}
