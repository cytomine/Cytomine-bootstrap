import org.codehaus.groovy.grails.plugins.springsecurity.SecurityFilterPosition
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

import be.cytomine.data.*

import be.cytomine.ViewPortToBuildXML
import be.cytomine.ontology.Relation
import be.cytomine.ontology.RelationTerm
import be.cytomine.security.Group
import be.cytomine.security.SecRole
import be.cytomine.security.SecUserSecRole
import be.cytomine.security.User
import grails.util.GrailsUtil

import java.lang.management.ManagementFactory

/**
 * TODO: cleaner le bootstrap
 */
class BootStrap {
    def springSecurityService
    def sequenceService
    def marshallersService
    def indexService
    def grailsApplication
    def storageService
    def messageSource
    def imagePropertiesService
    def countersService
    def triggerService
    def grantService
    def userGroupService
    def aclService
    def aclUtilService
    def objectIdentityRetrievalStrategy
    def sessionFactory
    def JSONMinService



    static def development = "development"
    static def production = "production"
    static def test = "test"
    static def perf = "perf"


    def init = { servletContext ->


//        println "TEST***************************************"
//        def test = UserAnnotation.createCriteria().list {
//            inList("image.id", [])
//        }
//        println test
//        println "***************************************"




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
                userGroupService.link(user, group)

                /* Handle groups */
                item.group.each { elem ->
                    def newGroup = [
                            [name: elem.name]
                    ]
                    createGroups(newGroup)
                    log.info "Fetch group " + elem.name
                    group = Group.findByName(elem.name)
                    userGroupService.link(user, group)
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
