package be.cytomine.utils

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
import grails.util.Holders
import groovy.sql.Sql

import static org.springframework.security.acls.domain.BasePermission.READ

class TaskService  {

    def cytomineService
    def securityACLService
    def dataSource

    static transactional = true

    def get(def id) {
        getFromDatabase(id)
    }

    def read(def id) {
        getFromDatabase(id)
    }

    def listLastComments(Project project) {
       securityACLService.check(project,READ)
       def comments = listFromDatabase(project.id)
       return comments
    }

    /**
     * Create a new task
     * @param project Project concerning by this task
     * @param user User that create the task
     * @return Task created
     */
    def createNewTask(Project project, SecUser user, boolean printInActivity) {
        securityACLService.checkGuest(cytomineService.currentUser)
        Task task = new Task(projectIdent: project?.id, userIdent: user.id,printInActivity:printInActivity)
        //task.addToComments("Task started...")
        task = saveOnDatabase(task)
        return task
    }

    /**
     * Update task status, don't change progress
     * @param task Task to update
     * @param comment Comment for the new task status
     */
    def updateTask(Task task, String comment) {
        if (task) {
            securityACLService.checkIsSameUser(SecUser.read(task.userIdent),cytomineService.currentUser)
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
            securityACLService.checkIsSameUser(SecUser.read(task.userIdent),cytomineService.currentUser)
            task.progress = progress
            addComment(task,progress+"%:" + comment)
            task = saveOnDatabase(task)
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
        securityACLService.checkIsSameUser(SecUser.read(task.userIdent),cytomineService.currentUser)
        task.progress = 100
        updateTask(task,100,"Task completed...")
        task = get(task.id)
        task
    }


    def getLastComments(Task task,int max) {
        //sql request retrieve n last comments for task
        def data = []
        Sql sql = createSQLDB()
        sql.eachRow("SELECT comment FROM task_comment where task_id = ${task.id} order by timestamp desc limit $max") {
            data << it[0]
        }
        closeSQL(sql)
        data
    }

    Task saveOnDatabase(Task task) {
        boolean isAlreadyInDatabase = false
        Sql sql = createSQLDB()
        sql.eachRow("SELECT id FROM task where id = ${task.id}") {
            isAlreadyInDatabase = true
        }

        if(!isAlreadyInDatabase) {
            task.id = Holders.getGrailsApplication().mainContext.sequenceService.generateID()
            sql.executeInsert("INSERT INTO task (id,progress,project_id,user_id,print_in_activity) VALUES (${task.id},${task.progress},${task.projectIdent},${task.userIdent},${task.printInActivity})")
        } else {
            sql.executeUpdate("UPDATE task set progress=${task.progress} WHERE id=${task.id}")
        }
        closeSQL(sql)
        getFromDatabase(task.id)
    }

    def getFromDatabase(def id) {
        Task task = null
        Sql sql = createSQLDB()
        sql.eachRow("SELECT id,progress,project_id,user_id FROM task where id = ${id}") {
            task = new Task()
            task.id = it[0]
            task.progress = it[1]
            task.projectIdent = it[2]
            task.userIdent = it[3]
        }
        closeSQL(sql)
        return task
    }

    def listFromDatabase(def idProject) {
        def comments = []
        null
        Sql sql = createSQLDB()
        sql.eachRow("SELECT comment, timestamp FROM task_comment tc, task t WHERE tc.task_id = t.id  AND project_id = $idProject and t.print_in_activity=true ORDER BY timestamp DESC") {
            TaskComment comment = new TaskComment()
            comment.comment = it[0]
            comment.timestamp = it[1]
            comments << comment
        }
        closeSQL(sql)
        return comments
    }

    def addComment(Task task,String comment) {
        if(comment!=null && !comment.equals("")) {
            TaskComment taskComment = new TaskComment(taskIdent: task.id,comment: comment,timestamp: new Date().getTime())
            saveCommentOnDatabase(taskComment)
        }
    }



    def saveCommentOnDatabase(TaskComment comment) {
        Sql sql = createSQLDB()
        sql.executeInsert("INSERT INTO task_comment (task_id,comment,timestamp) VALUES (${comment.taskIdent},${comment.comment},${comment.timestamp})")
        closeSQL(sql)
    }


    Sql createSQLDB() {
//        def db = [url:Holders.config.dataSource.url, user:Holders.config.dataSource.username, password:Holders.config.dataSource.password, driver:Holders.config.dataSource.driverClassName]
//        def sql = Sql.newInstance(db.url, db.user, db.password, db.driver)
        return new Sql(dataSource)
    }

    def closeSQL(Sql sql) {
        sql.close()
    }
}
