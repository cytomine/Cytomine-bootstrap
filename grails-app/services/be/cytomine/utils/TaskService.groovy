package be.cytomine.utils


import be.cytomine.project.Project
import be.cytomine.security.SecUser
import org.springframework.security.access.prepost.PreAuthorize
import be.cytomine.test.BasicInstance

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

    /**
     * Create a new task
     * @param project Project concerning by this task
     * @param user User that create the task
     * @return Task created
     */
    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def createNewTask(Project project, SecUser user) {
        Task task = new Task(projectIdent: project?.id, userIdent: user.id)
        //task.addToComments("Task started...")
        task = task.saveOnDatabase()
        return task
    }

    /**
     * Update task status, don't change progress
     * @param task Task to update
     * @param comment Comment for the new task status
     */
    @PreAuthorize("#task==null or #task.userIdent == principal.id or hasRole('ROLE_ADMIN')")
    def updateTask(Task task, String comment) {
        updateTask(task,(task? task.progress : -1),comment)
    }

    /**
     * Update task status
     * @param task Task to update
     * @param progress New progress value (0-100)
     * @param comment Comment for the new task status
     */
    @PreAuthorize("#task==null or #task.userIdent == principal.id or hasRole('ROLE_ADMIN')")
    def updateTask(Task task, int progress, String comment) {
            if(!task) {
                //log.info "task is null, ignore task"
                return
            }
            log.info "Progress = $progress"
            task.progress = progress
            task.addComment(comment)
            task = task.saveOnDatabase()
            return task
    }

    /**
     * Close task and put progress to 100
     * @param task Task to close
     * @return Closed task
     */
    @PreAuthorize("#task==null or #task.userIdent == principal.id or hasRole('ROLE_ADMIN')")
    def finishTask(Task task) {
        if(!task) {
            log.info "task is null, ignore task"
            return
        }
        task.progress = 100
        updateTask(task,100,"Task completed...")
        task = get(task.id)
        task
    }
}
