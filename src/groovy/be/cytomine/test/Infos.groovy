package be.cytomine.test

import be.cytomine.CytomineDomain
import be.cytomine.image.server.Storage
import be.cytomine.ontology.Ontology
import be.cytomine.processing.Software
import be.cytomine.project.Project
import be.cytomine.security.PermissionService
import be.cytomine.security.SecUser
import be.cytomine.security.SecUserSecRole
import be.cytomine.security.User
import grails.converters.JSON
import grails.util.Holders
import groovy.util.logging.Log
//import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder as SCH

import static org.springframework.security.acls.domain.BasePermission.*

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 10/02/11
 * Time: 9:34
 * To change this template use File | Settings | File Templates.
 */
@Log
class Infos {

    def springSecurityService

    def permissionService


    public static String CYTOMINEURL = Holders.getGrailsApplication().config.grails.serverURL + "/"

    public static String ADMINLOGIN = "admin"
    public static String ADMINPASSWORD = 'admin'

    public static String SUPERADMINLOGIN = "superadmin"
    public static String SUPERADMINPASSWORD = Holders.getGrailsApplication().config.grails.adminPassword

    public static String ANOTHERLOGIN = "anotheruser"
    public static String ANOTHERPASSWORD = 'password'

    public static String GOODPASSWORDUSERJOB = 'PasswordUserJob'

    public static String BADLOGIN = 'badlogin'
    public static String BADPASSWORD = 'badpassword'



    public static String UNDOURL = "command/undo.json"
    public static String REDOURL = "command/redo.json"


    /**
     * Add the admin right on a project to a user
     * @param username Username
     * @param project Project
     */
    static void addUserRight(String username, Project project) {
        addUserRight(User.findByUsername(username), project)
    }

    /**
     * Add the admin right on a project to a user
     * @param user User
     * @param project Project
     */
    static void addUserRight(User user, Project project) {
        SCH.context.authentication = new UsernamePasswordAuthenticationToken(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD, AuthorityUtils.createAuthorityList('ROLE_ADMIN'))
        def aclUtilService = Holders.getGrailsApplication().getMainContext().getBean("aclUtilService")
        PermissionService service = grails.util.Holders.getGrailsApplication().mainContext.getBean('permissionService')
        service.addPermission(project,user.username,ADMINISTRATION,SecUser.findByUsername(Infos.SUPERADMINLOGIN))
        service.addPermission(project,user.username,READ,SecUser.findByUsername(Infos.SUPERADMINLOGIN))
        service.addPermission(project.ontology,user.username,READ,SecUser.findByUsername(Infos.SUPERADMINLOGIN))
    }

    static void addUserRight(String username, Ontology ontology) {
        addUserRight(User.findByUsername(username), ontology)
    }

    static void addUserRight(User user, Ontology ontology) {
        SCH.context.authentication = new UsernamePasswordAuthenticationToken(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD, AuthorityUtils.createAuthorityList('ROLE_ADMIN'))
        PermissionService service = grails.util.Holders.getGrailsApplication().mainContext.getBean('permissionService')
        service.addPermission(ontology,user.username,READ,SecUser.findByUsername(Infos.SUPERADMINLOGIN))
        service.addPermission(ontology,user.username,WRITE,SecUser.findByUsername(Infos.SUPERADMINLOGIN))
        service.addPermission(ontology,user.username,DELETE,SecUser.findByUsername(Infos.SUPERADMINLOGIN))
        service.addPermission(ontology,user.username,ADMINISTRATION,SecUser.findByUsername(Infos.SUPERADMINLOGIN))
    }

    static void addUserRight(String username, Software software) {
        addUserRight(User.findByUsername(username), software)
    }

    static void addUserRight(User user, Software software) {
        SCH.context.authentication = new UsernamePasswordAuthenticationToken(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD, AuthorityUtils.createAuthorityList('ROLE_ADMIN'))
        PermissionService service = grails.util.Holders.getGrailsApplication().mainContext.getBean('permissionService')
        service.addPermission(software,user.username,READ,SecUser.findByUsername(Infos.SUPERADMINLOGIN))
        service.addPermission(software,user.username,ADMINISTRATION,SecUser.findByUsername(Infos.SUPERADMINLOGIN))
    }

    static void addUserRight(String username, Storage storage) {
        addUserRight(User.findByUsername(username), storage)
    }

    static void addUserRight(User user, Storage storage) {
        SCH.context.authentication = new UsernamePasswordAuthenticationToken(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD, AuthorityUtils.createAuthorityList('ROLE_ADMIN'))
        PermissionService service = grails.util.Holders.getGrailsApplication().mainContext.getBean('permissionService')
        service.addPermission(storage,user.username,READ,SecUser.findByUsername(Infos.SUPERADMINLOGIN))
        service.addPermission(storage,user.username,ADMINISTRATION,SecUser.findByUsername(Infos.SUPERADMINLOGIN))
    }

    static void addUserRight(User user, CytomineDomain domain, def perms) {
        SCH.context.authentication = new UsernamePasswordAuthenticationToken(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD, AuthorityUtils.createAuthorityList('ROLE_ADMIN'))
        PermissionService service = grails.util.Holders.getGrailsApplication().mainContext.getBean('permissionService')
        perms.each {
            service.addPermission(domain,user.username,it,SecUser.findByUsername(Infos.SUPERADMINLOGIN))
        }
    }
    /**
     * Print all right info for a specific domain
     * @param domain Domain to check
     */
    static void printRight(def domain) {
        println domain.getPermissionInACL(domain)
    }

    /**
     * Print all right info for a specific user
     * @param user User to check
     */
    static void printUserRight(User user) {
        def roles = SecUserSecRole.findAllBySecUser(user).collect { it.secRole }
        roles.each { role ->
            log.info role.authority
        }
    }


    static def toJSON(CytomineDomain domain) {
        return domain.encodeAsJSON()
    }
}
