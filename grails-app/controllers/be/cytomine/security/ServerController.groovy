package be.cytomine.security

import grails.converters.JSON
import grails.converters.XML
import grails.plugins.springsecurity.Secured
import groovy.sql.Sql

class ServerController {

    def springSecurityService
    def grailsApplication
    def dataSource


    @Secured(['IS_AUTHENTICATED_REMEMBERED'])
    def ping () {

        def jsonContent = request.JSON
        def data = [:]
        data.alive = true
        data.authenticated = springSecurityService.isLoggedIn()
        data.version = grailsApplication.metadata['app.version']
        data.serverURL = grailsApplication.config.grails.serverURL
        if (data.authenticated)  {
            data.user = springSecurityService.principal.id
            def idProject = null
            def idUser = data.user
            if(!jsonContent.project.toString().equals("null")) {
                idProject = Long.parseLong(jsonContent.project+"")
            }
            addLastConnection(idUser,idProject)
        }
        withFormat {
            json { render data as JSON }
            xml { render data as XML}
        }
    }

    def addLastConnection(def idUser, def idProject) {
        def  data = [idUser]
        def reqcreate = "UPDATE last_connection SET updated = '" +new Date()+ "', date = '" +new Date()+ "' WHERE user_id = ?"
        if(idProject) {
            data << idProject
            reqcreate = reqcreate + " AND project_id = ?"
        } else {
            reqcreate = reqcreate + " AND project_id is null"
        }

        //synchronized (this.getClass()) { //may be not synchronized for perf reasons (but table content will not be consistent)
        def sql = new Sql(dataSource)
        int affectedRow = sql.executeUpdate(reqcreate,data)
        sql.close()
        if(affectedRow==0) {
            def reqinsert = "INSERT INTO last_connection(id,version,user_id,date,project_id,created) VALUES (nextval('hibernate_sequence'),0,"+idUser+",'" +new Date()+ "'," +idProject+",'" +new Date()+ "')"
            sql = new Sql(dataSource)
            sql.execute(reqinsert)
            sql.close()
        }

        //}
    }
}
