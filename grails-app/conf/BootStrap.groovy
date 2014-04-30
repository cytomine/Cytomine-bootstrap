import be.cytomine.Exception.InvalidRequestException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.ontology.Relation
import be.cytomine.ontology.RelationTerm
import be.cytomine.security.SecUser
import grails.util.Environment
import org.codehaus.groovy.grails.commons.ApplicationAttributes
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

    def retrieveErrorsService
    def bootstrapTestDataService
    def bootstrapTestRunDataService
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
        log.info "GrailsUtil.environment= " + Environment.getCurrent().name + " BootStrap.development=" + Environment.DEVELOPMENT


        def ctx = servletContext.getAttribute(
                ApplicationAttributes.APPLICATION_CONTEXT
        )
        def dataSource = ctx.dataSourceUnproxied

        println "configuring database connection pool"

        dataSource.properties.each { println it }




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
        } else  if(Environment.getCurrent().name.equals("testrun")) {
            bootstrapTestRunDataService.initData()
        }

        //if database is empty, put minimal data
        if (SecUser.count() == 0 && Environment.getCurrent() != Environment.TEST && !Environment.getCurrent().name.equals("testrun")) {
            bootstrapTestDataService.initData()
        }

        if(!SecUser.findByUsername("admin")) {
            bootstrapUtilsService.createUsers([[username : 'admin', firstname : 'Admin', lastname : 'Master', email : 'lrollus@ulg.ac.be', group : [[name : "GIGA"]], password : '123admin456', color : "#FF0000", roles : ["ROLE_USER", "ROLE_ADMIN"]]])
        }

        if(!Relation.findByName(RelationTerm.names.PARENT)) {
            Relation relation = new Relation(name: RelationTerm.names.PARENT)
            relation.save(flush:true,failOnError: true)
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
