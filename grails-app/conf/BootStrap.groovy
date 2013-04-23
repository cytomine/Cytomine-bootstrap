import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AnnotationIndex
import be.cytomine.security.Group
import be.cytomine.security.SecRole
import be.cytomine.security.SecUser
import be.cytomine.security.SecUserSecRole
import be.cytomine.security.User
import be.cytomine.security.UserGroup
import grails.util.Environment
import org.codehaus.groovy.grails.plugins.springsecurity.SecurityFilterPosition
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.hibernate.jdbc.Work

import java.lang.management.ManagementFactory
import java.sql.Connection
import java.sql.SQLException

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
    def bootstrapTestDataService
    def bootstrapProdDataService
    def bootstrapUtilsService
    def javascriptService
    def dataSource
    def sessionFactory

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

        if(AnnotationIndex.count()==0) {


               sessionFactory.currentSession.doWork(
                       new Work() {
                           public void execute(Connection connection) throws SQLException
                           {
                               try {
                                   def statement = connection.createStatement()
                                   statement.execute("  INSERT INTO annotation_index(user_id,image_id,count_annotation,count_reviewed_annotation,id,version) \n" +
                                           "            SELECT ua.user_id, ua.image_id, count(*) as count, (SELECT count(*) FROM reviewed_annotation r WHERE r.user_id=ua.user_id AND r.image_id = ua.image_id),nextval('hibernate_sequence'),0 \n" +
                                           "            FROM algo_annotation ua \n" +
                                           "            GROUP by ua.user_id, ua.image_id\n" +
                                           "            UNION\n" +
                                           "            SELECT ua.user_id, ua.image_id, count(*) as count, (SELECT count(*) FROM reviewed_annotation r WHERE r.user_id=ua.user_id AND r.image_id = ua.image_id),nextval('hibernate_sequence'),0 \n" +
                                           "            FROM user_annotation ua \n" +
                                           "            GROUP by ua.user_id, ua.image_id\n" +
                                           "            ORDER BY count desc;")
                               } catch (org.postgresql.util.PSQLException e) {
                                   log.info e
                               }
                           }
                       }
               )


        }

        //bootstrapProdDataService.initUserStorages()


//        if(SecUserSecRole.count()<3) {
//            User.findAll().each { user ->
//
//                def userGroupName = user.username
//                def userGroup = [
//                        [name: userGroupName]
//                ]
//                bootstrapUtilsService.createGroups(userGroup)
//                Group group = Group.findByName(userGroupName)
//                UserGroup ug = new UserGroup(user:user, group:group)
//                ug.save(flush:true,failOnError: true)
//
//                if(user.authorities.isEmpty()) {
//                    if (user.username.equals("lrollus") || user.username.equals("rmaree") || user.username.equals("stevben")) {
//                        SecUserSecRole.create(user, SecRole.findByAuthority("ROLE_ADMIN"))
//                        SecUserSecRole.create(user, SecRole.findByAuthority("ROLE_USER"))
//                    } else {
//                        SecUserSecRole.create(user, SecRole.findByAuthority("ROLE_USER"))
//                    }
//                }
//           }
//        }
    }


    def merge57() {
        //read all images

        //sort by filename

        //split(".").





    }



}
