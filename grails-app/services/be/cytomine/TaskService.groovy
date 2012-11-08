package be.cytomine

import be.cytomine.Exception.ForbiddenException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.social.LastConnection
import be.cytomine.utils.Utils
import org.apache.commons.collections.ListUtils
import org.codehaus.groovy.grails.plugins.springsecurity.acl.AclSid
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.access.prepost.PreAuthorize

import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION
import static org.springframework.security.acls.domain.BasePermission.READ
import be.cytomine.command.Task

class TaskService {

    static transactional = true

    def get(def id) {
        Task.get(id)
    }

    def read(def id) {
        Task.read(id)
    }

    def updateTask(Task task, int progress, String comment) {
        if(!task) {
            log.info "task is null, ignore task"
            return
        }
        task.progress = progress
        if(comment!=null && !comment.equals("")) task.addToComments(comment)
        task.save(flush: true)
    }

    def createNewTask(Project project, SecUser user) {
        Task task = new Task(project: project, user: user)
        //task.addToComments("Task started...")
        task.validate()
        task.save(flush: true)
        task
    }

    def finishTask(Task task) {
        if(!task) {
            log.info "task is null, ignore task"
            return
        }
        task.progress = 100
        task.addToComments("Task completed...")
        task.validate()
        task.save(flush: true)
        log.info "task.id="+task.id
        log.info "task.progress="+task.progress
        task
    }
}
