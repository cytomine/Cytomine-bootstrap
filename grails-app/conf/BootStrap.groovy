import be.cytomine.image.Mime
import be.cytomine.image.server.ImageServer
import be.cytomine.image.server.ImageServerStorage
import be.cytomine.image.server.MimeImageServer
import be.cytomine.image.server.Storage
import be.cytomine.security.SecUser
import org.codehaus.groovy.grails.plugins.springsecurity.SecurityFilterPosition
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

import be.cytomine.ViewPortToBuildXML
import be.cytomine.ontology.Relation
import be.cytomine.ontology.RelationTerm
import be.cytomine.security.Group
import be.cytomine.security.SecRole
import be.cytomine.security.SecUserSecRole
import be.cytomine.security.User
import grails.util.GrailsUtil

import java.lang.management.ManagementFactory
import be.cytomine.project.Project
import org.springframework.security.acls.domain.BasePermission
import be.cytomine.ontology.Ontology
import be.cytomine.processing.Software
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import be.cytomine.security.UserGroup

/**
 * Bootstrap contains code that must be execute during application (re)start
 */
class BootStrap {

    def sequenceService
    def marshallersService
    def indexService
    def grailsApplication
    def messageSource
    def triggerService
    def grantService
    def userGroupService
    def termService

    def dataSource


    static def development = "development"
    static def production = "production"
    static def test = "test"
    static def perf = "perf"


    def init = { servletContext ->


        //Register API Authentifier
        log.info "Current directory2="+new File( 'test.html' ).absolutePath

        SpringSecurityUtils.clientRegisterFilter( 'apiAuthentificationFilter', SecurityFilterPosition.DIGEST_AUTH_FILTER.order + 1)
        log.info "###################" + grailsApplication.config.grails.serverURL + "##################"

        log.info "GrailsUtil.environment= " + GrailsUtil.environment + " BootStrap.development=" + BootStrap.development

        if (GrailsUtil.environment == BootStrap.development) { //scripts are not present in productions mode
            compileJS();
        }

        marshallersService.initMarshallers()
        sequenceService.initSequences()
        triggerService.initTrigger()
        indexService.initIndex()
        grantService.initGrant()

        termService.initialize()

        //countersService.updateCounters()

        grailsApplication.domainClasses.each {domainClass ->//iterate over the domainClasses
            if (domainClass.clazz.name.contains("be.cytomine")) {//only add it to the domains in my plugin

                domainClass.metaClass.retrieveErrors = {
                    def list = delegate?.errors?.allErrors?.collect {messageSource.getMessage(it, null)}
                    return list?.join('\n')
                }
            }
        }

        /* Print JVM infos like XMX/XMS */
        List inputArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
        for (int i = 0; i < inputArgs.size(); i++) {
            log.info inputArgs.get(i)
        }

        /* Fill data just in test environment*/
        if (GrailsUtil.environment == BootStrap.test) {
            initData(GrailsUtil.environment)
        }

        //toVersion1()
        toVersion1_ben()
    }

    def userService
    def permissionService
    def fileSystemService

    private def toVersion1_ben() {
        SecurityContextHolder.context.authentication = new UsernamePasswordAuthenticationToken("lrollus", "lR\$2011", AuthorityUtils.createAuthorityList('ROLE_ADMIN'))
        //a image server has now many storage
        for (imageServer in ImageServer.findAll()) {
            ImageServerStorage imageServerStorage = ImageServerStorage.findByImageServer(imageServer)
            if (!imageServerStorage) {
                //imageServerStorage = new ImageServerStorage(imageServer : imageServer, storage : imageServer.getStorage())
                //imageServerStorage.save(flush : true)
            }
        }
        //create storage for each user
        for (user in User.findAll()) {
            if (!Storage.findByOwner(user)) {

                String storage_base_path = grailsApplication.config.storage_path
                println "storage_base_path : $storage_base_path"
                String remotePath = [storage_base_path, user.id.toString()].join(File.separator)
                long timestamp = new Date().getTime()
                String user_home = System.getProperty("user.home")
                log.info "user_home $user_home"
                Storage storage = new Storage(
                        name: "storage_"+ timestamp,
                        basePath: remotePath,
                        ip: "139.165.108.28",
                        username: "storage_cytomine",
                        password: "bioinfo;3u54",
                        publicKey: null,
                        //publicKey: "$user_home/.ssh/id_rsa",
                        port: 22,
                        owner: user
                )

                if (storage.validate()) {
                    storage.save()
                    permissionService.addPermission(storage,user.username,BasePermission.ADMINISTRATION)
                    fileSystemService.makeRemoteDirectory(
                            storage.getIp(),
                            storage.getPort(),
                            storage.getUsername(),
                            storage.getPassword(),
                            storage.getPublicKey(),
                            storage.getBasePath())

                    for (imageServer in ImageServer.findAll()) {
                        ImageServerStorage imageServerStorage = new ImageServerStorage(imageServer : imageServer, storage : storage)
                        imageServerStorage.save(flush : true)
                    }
                } else {
                    storage.errors.each {
                        log.error it
                    }
                }
            }

        }
    }

    private def toVersion1() {
        SecurityContextHolder.context.authentication = new UsernamePasswordAuthenticationToken("lrollus", "lR\$2011", AuthorityUtils.createAuthorityList('ROLE_ADMIN'))
        /*
        =======> Script boostrap:
        -Pour chaque project
        --Pour chaque user du projet
        ---Ajouter le droit de read a l'ontologie du projet
        -Pour chaque ontologie
        --Ajouter le doit d'admin au créateur de l'ontologie
        -Pour chaque software
        --Ajouter un droit de créateur/admin a qqun
         */
        Project.withTransaction {
            Project.list().each { project ->
                def users = userService.listUsers(project)
                users.each { user ->
                    permissionService.addPermission(project.ontology,user.username,BasePermission.READ)
                }
            }

            Ontology.list().each { ontology ->
                permissionService.addPermission(ontology,ontology.user.username,BasePermission.ADMINISTRATION)
            }

            Software.list().each { software ->
                permissionService.addPermission(software,User.findByUsername("lrollus").username,BasePermission.ADMINISTRATION)
                permissionService.addPermission(software,User.findByUsername("rmaree").username,BasePermission.ADMINISTRATION)
                permissionService.addPermission(software,User.findByUsername("stevben").username,BasePermission.ADMINISTRATION)
            }
        }
    }









    private def compileJS() {
        log.info "========= C O M P I L E == J S ========= "
        ViewPortToBuildXML.process()
        def proc = "./scripts/yui-compressor-ant-task/doc/example/deploy.sh".execute()
        proc.in.eachLine { line -> log.info line }
        proc = "./scripts/yui-compressor-ant-task/doc/lib/deploy.sh".execute()
        proc.in.eachLine { line -> log.info line }
        log.info "======================================== "
    }

    private def initData(String env) {
        createUsers()
        createRelation()
    }

    def createUsers() {

        def usersSamples = [
                [username : 'rmaree', firstname : 'Raphaël', lastname : 'Marée', email : 'rmaree@ulg.ac.be', group : [[name : "GIGA"]], password : 'rM$2011', color : "#FF0000", roles : ["ROLE_USER", "ROLE_ADMIN"]],
                [username : 'lrollus', firstname : 'Loïc', lastname : 'Rollus', email : 'lrollus@ulg.ac.be', group : [[name : "GIGA"]], password : 'lR$2011', color : "#00FF00", roles : ["ROLE_USER", "ROLE_ADMIN"]],
                [username : 'stevben', firstname : 'Benjamin', lastname : 'Stévens', email : 'bstevens@ulg.ac.be', group : [[name : "GIGA"]], password : 'sB$2011', color : "#0000FF",roles : ["ROLE_USER", "ROLE_ADMIN"]]
        ]

        SecRole.findByAuthority("ROLE_USER") ?: new SecRole(authority: "ROLE_USER").save(flush: true)
        SecRole.findByAuthority("ROLE_ADMIN") ?: new SecRole(authority: "ROLE_ADMIN").save(flush: true)
        usersSamples.each { item ->
            User user = User.findByUsername(item.username)
            if (user)  return
            user = new User(
                    username: item.username,
                    firstname: item.firstname,
                    lastname: item.lastname,
                    email: item.email,
                    color: item.color,
                    password: item.password,
                    enabled: true)
            user.generateKeys()


            log.info "Before validating ${user.username}..."
            if (user.validate()) {
                log.info "Creating user ${user.username}..."

                try {user.save(flush: true) } catch(Exception e) {println e}
                log.info "Save ${user.username}..."

                /* Create a special group the user */
                def userGroupName = item.username
                def userGroup = [
                        [name: userGroupName]
                ]
                createGroups(userGroup)
                Group group = Group.findByName(userGroupName)
                UserGroup ug = new UserGroup(user:user, group:group)
                ug.save(flush:true,failOnError: true)

                /* Handle groups */
                item.group.each { elem ->
                    def newGroup = [
                            [name: elem.name]
                    ]
                    createGroups(newGroup)
                    log.info "Fetch group " + elem.name
                    group = Group.findByName(elem.name)
                    ug = new UserGroup(user:user, group:group)
                    ug.save(flush:true,failOnError: true)
                }

                /* Add Roles */
                item.roles.each { authority ->
                    log.info "Add SecRole " + authority + " for user " + user.username
                    SecRole secRole = SecRole.findByAuthority(authority)
                    if (secRole) SecUserSecRole.create(user, secRole)
                }

            } else {
                log.info("\n\n\n Errors in account boostrap for ${item.username}!\n\n\n")
                user.errors.each {
                    err -> log.info err
                }
            }
        }
    }

    def createGroups(groupsSamples) {
        groupsSamples.each { item ->
            if (Group.findByName(item.name)) return
            def group = new Group(name: item.name)
            if (group.validate()) {
                log.info "Creating group ${group.name}..."
                group.save(flush: true)
                log.info "Creating group ${group.name}... OK"
            }
            else {
                log.info("\n\n\n Errors in group boostrap for ${item.name}!\n\n\n")
                group.errors.each {
                    err -> log.info err
                }
            }
        }
    }

    def createRelation() {
        def relationSamples = [
                [name: RelationTerm.names.PARENT],
                [name: RelationTerm.names.SYNONYM]
        ]

        log.info "createRelation"
        relationSamples.each { item ->
            if (Relation.findByName(item.name)) return
            def relation = new Relation(name: item.name)
            log.info "create relation=" + relation.name

            if (relation.validate()) {
                log.info "Creating relation : ${relation.name}..."
                relation.save(flush: true)

            } else {
                log.info("\n\n\n Errors in account boostrap for ${item.name}!\n\n\n")
                relation.errors.each {
                    err -> log.info err
                }

            }
        }
    }

}
