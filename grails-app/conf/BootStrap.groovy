import be.cytomine.Exception.InvalidRequestException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.image.AbstractImage
import be.cytomine.image.UploadedFile
import be.cytomine.image.server.ImageServer
import be.cytomine.image.server.MimeImageServer
import be.cytomine.integration.NotifyAuroraUploadJob
import be.cytomine.image.Mime
import be.cytomine.ontology.Property
import be.cytomine.ontology.Relation
import be.cytomine.ontology.RelationTerm
import be.cytomine.security.SecRole
import be.cytomine.security.SecUser
import be.cytomine.security.SecUserSecRole
import be.cytomine.utils.Version
import grails.util.Environment
import grails.util.Holders
import org.codehaus.groovy.grails.commons.ApplicationAttributes
import grails.plugin.springsecurity.SecurityFilterPosition
import grails.plugin.springsecurity.SpringSecurityUtils

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
    def noSQLCollectionService

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
        log.info "################### SERVER " + grailsApplication.config.grails.serverURL + "##################"
        log.info "################### IMAGE SERVER " + grailsApplication.config.grails.imageServerURL + "##################"
        log.info "GrailsUtil.environment= " + Environment.getCurrent().name + " BootStrap.development=" + Environment.DEVELOPMENT

        log.info "################################################"
        log.info "################## DATABASES ###################"
        log.info "################################################"
        log.info "SQL: " + [url:Holders.config.dataSource.url, user:Holders.config.dataSource.username, password:Holders.config.dataSource.password, driver:Holders.config.dataSource.driverClassName]
        log.info "NOSQL: " + [host:Holders.config.grails.mongo.host, port:Holders.config.grails.mongo.port, databaseName:Holders.config.grails.mongo.databaseName]


        def ctx = servletContext.getAttribute(
                ApplicationAttributes.APPLICATION_CONTEXT
        )
        def dataSource = ctx.dataSourceUnproxied

        println "configuring database connection pool"

        dataSource.properties.each { println it }

        if(Version.count()==0) {
            log.info "Version was not set, set to 0"
            Version.setCurrentVersion(0)
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
            bootstrapTestDataService.initData()
            noSQLCollectionService.cleanActivityDB()
        } else  if(Environment.getCurrent().name.equals("testrun")) {
            bootstrapTestRunDataService.initData()
        }

        //if database is empty, put minimal data
        if (SecUser.count() == 0 && Environment.getCurrent() != Environment.TEST && !Environment.getCurrent().name.equals("testrun")) {
            bootstrapTestDataService.initData()
        }

        //ventana
        println "Create ventana"
//        if (!Mime.findByMimeType("ventana/tif")) {
//            bootstrapTestDataService.initVentana()
//        }

        if(!SecUser.findByUsername("admin")) {
            bootstrapUtilsService.createUsers([[username : 'admin', firstname : 'Admin', lastname : 'Master', email : 'lrollus@ulg.ac.be', group : [[name : "GIGA"]], password : '123admin456', color : "#FF0000", roles : ["ROLE_USER", "ROLE_ADMIN"]]])
        }
        if(!SecUser.findByUsername("superadmin")) {
            bootstrapUtilsService.createUsers([[username : 'superadmin', firstname : 'Super', lastname : 'Admin', email : 'lrollus@ulg.ac.be', group : [[name : "GIGA"]], password : '123admin456', color : "#FF0000", roles : ["ROLE_USER", "ROLE_ADMIN","ROLE_SUPER_ADMIN"]]])
        }

        if(!SecUser.findByUsername("monitoring")) {
            bootstrapUtilsService.createUsers([[username : 'monitoring', firstname : 'Monitoring', lastname : 'Monitoring', email : 'lrollus@ulg.ac.be', group : [[name : "GIGA"]], password : '123admin456', color : "#FF0000", roles : ["ROLE_USER","ROLE_SUPER_ADMIN"]]])
        }


        if(!Relation.findByName(RelationTerm.names.PARENT)) {
            Relation relation = new Relation(name: RelationTerm.names.PARENT)
            relation.save(flush:true,failOnError: true)
        }



        if(Version.isOlderVersion(20140601)) {
            //version>2014 05 12
            if(!SecRole.findByAuthority("ROLE_SUPER_ADMIN")) {
                SecRole role = new SecRole(authority:"ROLE_SUPER_ADMIN")
                role.save(flush:true,failOnError: true)
            }

            //version>2014 05 12  OTOD: DO THIS FOR IFRES,...
            if(SecUser.findByUsername("ImageServer1")) {
                def imageUser = SecUser.findByUsername("ImageServer1")
                def superAdmin = SecRole.findByAuthority("ROLE_SUPER_ADMIN")
                if(!SecUserSecRole.findBySecUserAndSecRole(imageUser,superAdmin)) {
                    new SecUserSecRole(secUser: imageUser,secRole: superAdmin).save(flush:true)
                }

            }

            if(SecUser.findByUsername("vmartin")) {
                def imageUser = SecUser.findByUsername("vmartin")
                def superAdmin = SecRole.findByAuthority("ROLE_SUPER_ADMIN")
                if(!SecUserSecRole.findBySecUserAndSecRole(imageUser,superAdmin)) {
                    new SecUserSecRole(secUser: imageUser,secRole: superAdmin).save(flush:true)
                }
            }
        }

        bootstrapUtilsService.createMultipleIS()
        bootstrapUtilsService.createMultipleRetrieval()


        if(Version.isOlderVersion(20140625) && (UploadedFile.count() == 0 || UploadedFile.findByImageIsNull()?.size > 0)) {
            bootstrapUtilsService.checkImages()
        }

        if(Version.isOlderVersion(20140630)) {
            bootstrapUtilsService.transfertProperty()
        }

//        if(Version.isOlderVersion(20140712)) {
//            bootstrapUtilsService.createNewIS()
//        }
//
//        if(Version.isOlderVersion(20140713)) {
//            bootstrapUtilsService.checkImages2() //fix uploadedFile path
//        }

        /*if(Version.isOlderVersion(20140715) && Environment.getCurrent() == Environment.PRODUCTION) {
            bootstrapUtilsService.createNewIS2() //add image1.cytomine.be -> image10.cytomine.be
        }*/

        if(Version.isOlderVersion(20140716)) {
            bootstrapUtilsService.addMimePyrTiff()
        }

        if(Version.isOlderVersion(20140717)) {
            bootstrapUtilsService.addMimePhilipsTiff()
        }

//        if(Version.isOlderVersion(20140717)) {
//            bootstrapUtilsService.createMultipleIS()
//        }


        if(Version.isOlderVersion(20140925)) {
            bootstrapUtilsService.addMimeVentanaTiff()
        }


        Version.setCurrentVersion(Long.parseLong(grailsApplication.metadata.'app.version'))


        println "********************************************"
        println grailsApplication.config.grails.client




        //             properties.put("patient_id","PATIENT"+i);
//             properties.put("sample_id","SAMPLE"+i);
//             properties.put("image_type","IMAGETYPE"+i);
//             properties.put("version","VERSION"+i);


            if(grailsApplication.config.grails.client=="AURORA") {
//                if(Environment.getCurrent() == Environment.DEVELOPMENT) {
//                    if(!Property.findByKeyLike("patient_id")) {
//                        new Property(key:"patient_id",value:"0001",domainIdent: 140736353,domainClassName: "be.cytomine.image.AbstractImage").save(flush:true,failOnError: true)
//                        new Property(key:"sample_type",value:"Primary",domainIdent: 140736353,domainClassName: "be.cytomine.image.AbstractImage").save(flush:true,failOnError: true)
//                        new Property(key:"image_type",value:"HER2",domainIdent: 140736353,domainClassName: "be.cytomine.image.AbstractImage").save(flush:true,failOnError: true)
//                    }
//                }

                if(Environment.getCurrent() != Environment.TEST) {
                    NotifyAuroraUploadJob.schedule(grailsApplication.config.grails.integration.aurora.interval,-1, [:])
                }

            }
    }
}
