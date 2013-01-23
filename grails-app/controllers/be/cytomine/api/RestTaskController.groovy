package be.cytomine.api

import be.cytomine.command.Task
import be.cytomine.project.Project
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
        Project project = projectService.read(request.JSON.project)
        SecUser user = cytomineService.getCurrentUser()
        if(!project) {
            responseNotFound("Project", params.project)
        } else {
            Task task = taskService.createNewTask(project,user)
            responseSuccess([task:task])
        }
    }
}
