import be.cytomine.Exception.InvalidRequestException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.image.multidim.ImageGroup
import be.cytomine.image.multidim.ImageSequence
import be.cytomine.project.Project
import be.cytomine.security.SecRole
import be.cytomine.security.SecUser
import be.cytomine.security.SecUserSecRole
import be.cytomine.security.User
import be.cytomine.utils.News
import grails.util.Environment
import jsondoc.APIUtils
import jsondoc.JSONUtils
import jsondoc.TimerMethods
import org.codehaus.groovy.grails.plugins.springsecurity.SecurityFilterPosition
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

import java.lang.management.ManagementFactory
import java.text.SimpleDateFormat

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

    def retrieveErrorsService
    def bootstrapTestDataService
    def bootstrapProdDataService
    def bootstrapUtilsService
    def javascriptService
    def dataSource
    def sessionFactory

    def init = { servletContext ->

        //Register API Authentifier
        log.info "Current directory2="+new File( 'test.html' ).absolutePath
        println "HeadLess:" +java.awt.GraphicsEnvironment.isHeadless();

        SpringSecurityUtils.clientRegisterFilter( 'apiAuthentificationFilter', SecurityFilterPosition.DIGEST_AUTH_FILTER.order + 1)
        log.info "###################" + grailsApplication.config.grails.serverURL + "##################"
        log.info "GrailsUtil.environment= " + Environment.getCurrent() + " BootStrap.development=" + Environment.DEVELOPMENT

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
            bootstrapTestDataService.initData()
        }

        //if database is empty, put minimal data
        if (SecUser.count() == 0 && Environment.getCurrent() != Environment.TEST) {
            bootstrapTestDataService.initData()
        }

        //jsondoc init
        JSONUtils.registerMarshallers()
        use (TimerMethods) {
            def timer = new Timer()
            def task = timer.runEvery(1000, 10000) {
                println "Task executed at ${new Date()}."
                APIUtils.buildApiRegistry(grailsApplication.mainContext, grailsApplication)
            }
            println "Current date is ${new Date()}."
        }


    }

    def saveDomain(def newObject, boolean flush = true) {
        newObject.checkAlreadyExist()
        if (!newObject.validate()) {
            log.error newObject.errors
            log.error newObject.retrieveErrors().toString()
            throw new WrongArgumentException(newObject.retrieveErrors().toString())
        }
        if (!newObject.save(flush: flush)) {
            throw new InvalidRequestException(newObject.retrieveErrors().toString())
        }
    }



}
