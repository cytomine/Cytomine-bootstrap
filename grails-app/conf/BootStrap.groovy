import be.cytomine.image.server.Storage
import be.cytomine.security.SecUser
import grails.util.Environment
import groovy.sql.Sql
import org.codehaus.groovy.grails.plugins.springsecurity.SecurityFilterPosition
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

import java.lang.management.ManagementFactory

/**
 * Bootstrap contains code that must be execute during application (re)start
 */
class BootStrap {

    def grailsApplication
    def messageSource

    def sequenceService
    def marshallersService
    def indexService
    def triggerService
    def grantService
    def termService
    def tableService
    def secUserService
    def countersService
    def retrieveErrorsService
    def bootstrapProdDataService
    def bootstrapTestDataService
    def bootstrapUtilsService
    def javascriptService
    def dataSource

    def init = { servletContext ->

        //Register API Authentifier
        log.info "Current directory2="+new File( 'test.html' ).absolutePath

        SpringSecurityUtils.clientRegisterFilter( 'apiAuthentificationFilter', SecurityFilterPosition.DIGEST_AUTH_FILTER.order + 1)
        log.info "###################" + grailsApplication.config.grails.serverURL + "##################"

        log.info "GrailsUtil.environment= " + Environment.getCurrent() + " BootStrap.development=" + Environment.DEVELOPMENT

        if (Environment.getCurrent() == Environment.DEVELOPMENT) { //scripts are not present in productions mode
            javascriptService.compile();
        }

        //Initialize marshallers and services
        marshallersService.initMarshallers()
        sequenceService.initSequences()
        triggerService.initTrigger()
        indexService.initIndex()
        grantService.initGrant()
        tableService.initTable()
        termService.initialize()
        retrieveErrorsService.initMethods()

        /* Print JVM infos like XMX/XMS */
        List inputArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
        for (int i = 0; i < inputArgs.size(); i++) {
            log.info inputArgs.get(i)
        }

        /* Fill data just in test environment*/
        if (Environment.getCurrent() == Environment.TEST) {
            new Sql(dataSource).executeUpdate("DELETE FROM task_comment")
            new Sql(dataSource).executeUpdate("DELETE FROM task")

            bootstrapTestDataService.initData()
        }

        //create non admin user for test
        if (Environment.getCurrent()  != Environment.TEST) {
            bootstrapUtilsService.createUsers([
                    [username : 'johndoe', firstname : 'John', lastname : 'Doe', email : 'lrollus@ulg.ac.be', group : [[name : "GIGA"]], password : 'test', color : "#FF0000", roles : ["ROLE_USER"]]])
        }

        //if database is empty, create admin user
        if (SecUser.count() == 0) {
            bootstrapUtilsService.createUsers([[username : 'admin', firstname : 'Admin', lastname : 'Master', email : 'lrollus@ulg.ac.be', group : [[name : "GIGA"]], password : 'test', color : "#FF0000", roles : ["ROLE_ADMIN"]]])
        }

        /* Tmp : migration script */
        if (Environment.getCurrent() != Environment.TEST && Storage.count() == 1) {
//            bootstrapProdDataService.toVersion1()
//            countersService.updateCommentsCounters()
        }

    }
}
