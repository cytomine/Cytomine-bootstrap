package be.cytomine.security

import be.cytomine.Exception.ForbiddenException
import be.cytomine.ModelService
import be.cytomine.command.user.AddUserCommand
import be.cytomine.command.user.DeleteUserCommand
import be.cytomine.command.user.EditUserCommand
import be.cytomine.project.Project
import be.cytomine.project.ProjectGroup

class UserService extends ModelService {

    static transactional = true

    def springSecurityService
    def transactionService
    def cytomineService
    def commandService



    def get(def id) {
        User.get(id)
    }

    def read(def id) {
        User.read(id)
    }

    def readCurrentUser() {
        cytomineService.getCurrentUser()
    }

    def list() {
        User.list(sort: "username", order: "asc")
    }

    def list(Project project) {
        project.users()
    }

    def add(def json) {
        User currentUser = cytomineService.getCurrentUser()
        def result = commandService.processCommand(new AddUserCommand(user: currentUser), json)
        return result
    }

    def update(def json) {
        User currentUser = cytomineService.getCurrentUser()
        def result = commandService.processCommand(new EditUserCommand(user: currentUser), json)
        return result
    }

    def delete(def json) {
        User currentUser = cytomineService.getCurrentUser()

        def result = null
        if (json.id == springSecurityService.principal.id) throw new ForbiddenException("The user can't delete herself")
        else {
            result = commandService.processCommand(new DeleteUserCommand(user: currentUser), json)
        }
        return result
    }

    /*def clearUser = {
   log.info "ClearUser"
   User currentUser = getCurrentUser(springSecurityService.principal.id)
   log.info "User:" + currentUser.username + " params.idProject=" + params.id
   ///ouh le vilain code!
   Project project = Project.get(params.id)
   log.info "project= " + project
   if (project) {
     Group group = Group.findByName(project.name)
     log.info "group= " + group
     if (group) {
       ProjectGroup.unlink(project, group)
       def users = group.users();
       log.info users.size()
       users.each { user ->
         log.info "remove " + user.username + " from " + group.name
         UserGroup.unlink(user, group)
       }
       group.delete(flush:true)

       log.info "Group and project are unlink"
     }
     else {
       log.error "Cannot find group " + project.name
     }
   }
   response.status = 201
   def ret = [data: [message: "ok"], status: 201]
   response(ret)

 }   */

    def deleteUserFromProject(User user, Project project) {
        synchronized (this.getClass()) {

            if (project) {
                Group group = Group.findByName(project.name)
                log.info "group= " + group
                if (!group) {
                    group = new Group(name: project.name)
                    group.save()
                    ProjectGroup.link(project, group)
                }

                UserGroup.unlink(user, group);
            }
        }
    }

    def addUserFromProject(User user, Project project) {
        synchronized (this.getClass()) {
            if (project) {
                Group group = Group.findByName(project.name)
                log.info "group= " + group
                if (!group) {
                    group = new Group(name: project.name)
                    group.save()
                    ProjectGroup.link(project, group)
                }
                UserGroup.link(user, group);
            }
        }
        response.status = 201
        def ret = [data: [message: "OK"], status: 201]
        response(ret)

    }

    def list(def currentPage, def maxRows, def sortIndex, def sortOrder, def firstName, def lastName, def email) {
        def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows
        def users = User.createCriteria().list(max: maxRows, offset: rowOffset) {
            if (firstName)
                ilike('firstname', "%${firstName}%")

            if (lastName)
                ilike('lastname', "%${lastName}%")

            if (email)
                ilike('email', "%${email}%")

            order(sortIndex, sortOrder).ignoreCase()
        }
        return users
    }
}
