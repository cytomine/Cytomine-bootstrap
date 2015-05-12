package be.cytomine.api

/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.utils.Task

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
            responseSuccess(task.getMap(taskService))
        } else {
            responseNotFound("Task", params.id)
        }
    }

    /**
     * Create a new task
     */
    def add = {
        Project project
        try {
            project = projectService.read(request.JSON.project)
        } catch(Exception e) {
            //my be null
        }
        SecUser user = cytomineService.getCurrentUser()
        boolean printInActivity = params.getBoolean('printInActivity')
        Task task = taskService.createNewTask(project,user,printInActivity)
        responseSuccess([task:task.getMap(taskService)])
    }

    def listCommentByProject = {
        Project project = projectService.read(params.long('idProject'))
        responseSuccess(taskService.listLastComments(project))
    }
}
