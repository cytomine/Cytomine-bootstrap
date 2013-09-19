import be.cytomine.Exception.InvalidRequestException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.image.Mime
import be.cytomine.image.multidim.ImageGroup
import be.cytomine.image.multidim.ImageSequence
import be.cytomine.image.server.ImageServer
import be.cytomine.image.server.MimeImageServer
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.AnnotationIndex
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.security.Group
import be.cytomine.security.SecRole
import be.cytomine.security.SecUser
import be.cytomine.security.SecUserSecRole
import be.cytomine.security.User
import be.cytomine.security.UserGroup
import be.cytomine.utils.News
import grails.util.Environment
import org.codehaus.groovy.grails.plugins.springsecurity.SecurityFilterPosition
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.hibernate.jdbc.Work

import java.lang.management.ManagementFactory
import java.sql.Connection
import java.sql.SQLException
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

        if (Environment.getCurrent()  != Environment.TEST) {
            addSomeNews()
            addImageServerUser()
        }

//        if(AnnotationIndex.count()==0) {
//
//
//               sessionFactory.currentSession.doWork(
//                       new Work() {
//                           public void execute(Connection connection) throws SQLException
//                           {
//                               try {
//                                   def statement = connection.createStatement()
//                                   statement.execute("  INSERT INTO annotation_index(user_id,image_id,count_annotation,count_reviewed_annotation,id,version) \n" +
//                                           "            SELECT ua.user_id, ua.image_id, count(*) as count, (SELECT count(*) FROM reviewed_annotation r WHERE r.user_id=ua.user_id AND r.image_id = ua.image_id),nextval('hibernate_sequence'),0 \n" +
//                                           "            FROM algo_annotation ua \n" +
//                                           "            GROUP by ua.user_id, ua.image_id\n" +
//                                           "            UNION\n" +
//                                           "            SELECT ua.user_id, ua.image_id, count(*) as count, (SELECT count(*) FROM reviewed_annotation r WHERE r.user_id=ua.user_id AND r.image_id = ua.image_id),nextval('hibernate_sequence'),0 \n" +
//                                           "            FROM user_annotation ua \n" +
//                                           "            GROUP by ua.user_id, ua.image_id\n" +
//                                           "            ORDER BY count desc;")
//                               } catch (org.postgresql.util.PSQLException e) {
//                                   log.info e
//                               }
//                           }
//                       }
//               )
//
//
//        }
//
//        //create SCN MIME
//        Mime mime = new Mime(extension : "scn", mimeType : "openslide/scn")
//        if (mime.validate()) {
//            mime.save()
//            for (imageServer in ImageServer.list()) {
//                MimeImageServer mimeImageServer = new MimeImageServer( mime : mime, imageServer : imageServer)
//                if (mimeImageServer.validate()) {
//                    mimeImageServer.save()
//                } else {
//                    mimeImageServer.errors?.each {
//                        println it
//                    }
//                }
//            }
//        } else {
//            mime.errors?.each {
//                println it
//            }
//        }

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


    def addImageServerUser() {


        def user = SecUser.findByUsername("ImageServer1")
        if (!user) {
            user = new User(
                    username: "ImageServer1",
                    firstname: "Image",
                    lastname: "Server",
                    email: "info@cytomine.be",
                    password: "passwordIS",
                    enabled: true)
            user.generateKeys()
            saveDomain(user)
            SecUserSecRole.create(user,SecRole.findByAuthority("ROLE_USER"),true)
            SecUserSecRole.create(user,SecRole.findByAuthority("ROLE_ADMIN"),true)
        }
        user
    }



    def mergeMultiDimImage18054742() {
        int idProject = 18054742
        Project project = Project.read(idProject)
        def images = ImageInstance.findAllByProject(project)
        def filenames = images.collect{
            println it.baseImage.originalFilename
            String fileNameEnd = it.baseImage.originalFilename.replace("_sin_delta.png","")
            fileNameEnd = fileNameEnd.replace("_I0.png","")
            fileNameEnd = fileNameEnd.replace("_phi.png","")
            fileNameEnd
        }
        println filenames
        filenames = filenames.unique().sort()
        /**
         * SELECT ai.original_filename
         FROM abstract_image ai, image_instance ii
         WHERE ai.id = ii.base_image_id
         AND ii.project_id = 18054742
         ORDER BY ai.original_filename;

         */


        filenames.each { filename ->

            images = ImageInstance.findAllByProjectAndBaseImageInList(project,AbstractImage.findAllByOriginalFilenameLike("${filename}%"))
//            println "images=$images"

            println "groupame=$filename"

            ImageGroup imageGroup = new ImageGroup(project:project,name:filename)
            saveDomain(imageGroup)

            def channels = []
            //sort slice index (convert to long to avoid: string sort like 10, 1, 20...)


//              println "${filename}_I0.png"
//               println "img=${img}"
//              println "project=$project"
            def img = AbstractImage.findAllByOriginalFilenameLike("${filename}_I0.png")
            if(img) {
                channels << ImageInstance.findByProjectAndBaseImageInList(project,img)
            }
            img = AbstractImage.findAllByOriginalFilenameLike("${filename}_phi.png")
              if(img) {
                  channels << ImageInstance.findByProjectAndBaseImageInList(project,img)
              }
            img = AbstractImage.findAllByOriginalFilenameLike("${filename}_sin_delta.png")
              if(img) {
                  channels << ImageInstance.findByProjectAndBaseImageInList(project,img)
             }
//              channels << ImageInstance.findByProjectAndBaseImageInList(project,AbstractImage.findAllByOriginalFilenameLike("${filename}_phi.png"))
//              channels << ImageInstance.findByProjectAndBaseImageInList(project,AbstractImage.findAllByOriginalFilenameLike("${filename}_sin_delta.png"))

            println channels

            channels.eachWithIndex{ channel, index ->
                println "file=${channel.baseImage.filename}"
                ImageSequence imageSequence = new  ImageSequence(image:channel,channel:index,zStack:0,slice:0,time:0,imageGroup:imageGroup)
                saveDomain(imageSequence)

            }

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


    def addSomeNews() {
        if(News.count==0) {
            def data = [
                    [date:'12/08/2013', text:'A project has now a user and admin list. A project admin is able to edit annotations from other user. Furthermore, a project admin is not affected by the "private layer" options. Only project creator and project admin can raise a user as project admin.'],
                    [date:'27/07/2013',text:'Project can be locked. If a project is locked, you can delete all job data with no reviewed annotation.'],
                    [date:'14/06/2013',text:'Project, Image and Annotation can now have a description.'],
                    [date:'27/05/2013',text:'Review view is now available in project. This helps meet specific needs especially for Cytology review.'],
                    [date: '08/05/2013',text:'You can now use keyboard shortcuts to perform some actions. Look at the "Help" section on the top of this windows.']

            ]

            data.each {

                News news = new News(added:new SimpleDateFormat("dd/MM/yyyy").parse(it.date),text:it.text, user: User.read(16))
                assert news.validate()
                println news.errors
                assert news.save(flush:true)
            }
        }
    }
}
