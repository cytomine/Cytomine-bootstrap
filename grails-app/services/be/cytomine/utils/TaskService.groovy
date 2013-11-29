package be.cytomine.utils

import be.cytomine.SecurityACL
import be.cytomine.project.Project
import be.cytomine.security.SecUser

import static org.springframework.security.acls.domain.BasePermission.READ

class TaskService  {

    def cytomineService

    static transactional = true

    def get(def id) {
        Task task = new Task().getFromDatabase(id)
        task
    }

    def read(def id) {
        Task task = new Task().getFromDatabase(id)
        task
    }

    def listLastComments(Project project) {
       SecurityACL.check(project,READ)
       def comments = new Task().listFromDatabase(project.id)
       return comments
    }

    /**
     * Create a new task
     * @param project Project concerning by this task
     * @param user User that create the task
     * @return Task created
     */
    def createNewTask(Project project, SecUser user, boolean printInActivity) {
        SecurityACL.checkGuest(cytomineService.currentUser)
        Task task = new Task(projectIdent: project?.id, userIdent: user.id,printInActivity:printInActivity)
        //task.addToComments("Task started...")
        task = task.saveOnDatabase()
        return task
    }

    /**
     * Update task status, don't change progress
     * @param task Task to update
     * @param comment Comment for the new task status
     */
    def updateTask(Task task, String comment) {
        if (task) {
            SecurityACL.checkIsSameUser(SecUser.read(task.userIdent),cytomineService.currentUser)
        }
        updateTask(task,(task? task.progress : -1),comment)
    }

    /**
     * Update task status
     * @param task Task to update
     * @param progress New progress value (0-100)
     * @param comment Comment for the new task status
     */
    def updateTask(Task task, int progress, String comment) {
            if(!task) {
                return
            }
            SecurityACL.checkIsSameUser(SecUser.read(task.userIdent),cytomineService.currentUser)
            task.progress = progress
            task.addComment(progress+"%:" + comment)
            task = task.saveOnDatabase()
            return task
    }

    /**
     * Close task and put progress to 100
     * @param task Task to close
     * @return Closed task
     */
    def finishTask(Task task) {
        if(!task) {
            return
        }
        SecurityACL.checkIsSameUser(SecUser.read(task.userIdent),cytomineService.currentUser)
        task.progress = 100
        updateTask(task,100,"Task completed...")
        task = get(task.id)
        task
    }
}
