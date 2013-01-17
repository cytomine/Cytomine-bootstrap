package be.cytomine

import be.cytomine.command.Task
import be.cytomine.project.Project
import be.cytomine.security.SecUser

class TaskService {

    static transactional = true

    //TODO: acl, only task creator can see its task?
    def get(def id) {
        Task.get(id)
    }

    //TODO: acl, only task creator can see its task?
    def read(def id) {
        Task.read(id)
    }

    /**
     * Create a new task
     * @param project Project concerning by this task
     * @param user User that create the task
     * @return Task created
     */
    def createNewTask(Project project, SecUser user) {
        Task task = new Task(project: project, user: user)
        //task.addToComments("Task started...")
        task.validate()
        task.save(flush: true)
        task
    }

    /**
     * Update task status
     * @param task Task to update
     * @param progress New progress value (0-100)
     * @param comment Comment for the new task status
     */
    def updateTask(Task task, int progress, String comment) {
        if(!task) {
            log.info "task is null, ignore task"
            return
        }
        task.progress = progress
        if(comment!=null && !comment.equals("")) task.addToComments(comment)
        task.save(flush: true)
    }

    /**
     * Close task and put progress to 100
     * @param task Task to close
     * @return Closed task
     */
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
