package be.cytomine.test

import grails.util.Holders
import org.springframework.security.core.context.SecurityContextHolder as SCH

import be.cytomine.ontology.Ontology
import be.cytomine.processing.Software
import be.cytomine.project.Project
import be.cytomine.security.User
import org.codehaus.groovy.grails.plugins.springsecurity.acl.AclObjectIdentity
import org.springframework.security.acls.domain.BasePermission
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils

import static org.springframework.security.acls.domain.BasePermission.*

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 10/02/11
 * Time: 9:34
 * To change this template use File | Settings | File Templates.
 */
class Infos {

    def springSecurityService

    public static String CYTOMINEURL = Holders.getGrailsApplication().config.grails.serverURL + "/"

    public static String GOODLOGIN = "lrollus"
    public static String GOODPASSWORD = 'lR$2011'

    public static String ANOTHERLOGIN = "rmaree"
    public static String ANOTHERPASSWORD = 'rM$2011'

    public static String GOODPASSWORDUSERJOB = 'PasswordUserJob'

    public static String BADLOGIN = 'badlogin'
    public static String BADPASSWORD = 'badpassword'

    public static String UNDOURL = "command/undo"
    public static String REDOURL = "command/redo"


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
        long start = System.currentTimeMillis()
        SCH.context.authentication = new UsernamePasswordAuthenticationToken(Infos.GOODLOGIN, Infos.GOODPASSWORD, AuthorityUtils.createAuthorityList('ROLE_ADMIN'))
        def aclUtilService = Holders.getGrailsApplication().getMainContext().getBean("aclUtilService")
        aclUtilService.addPermission project, user.username, ADMINISTRATION
        aclUtilService.addPermission project.ontology, user.username, READ
        aclUtilService.addPermission project.ontology, user.username, WRITE
        aclUtilService.addPermission project.ontology, user.username, DELETE

        def sessionFactory = Holders.getGrailsApplication().getMainContext().getBean("sessionFactory")
        sessionFactory.currentSession.flush()
        SCH.clearContext()
    }

    static void addUserRight(User user, Ontology ontology) {
        long start = System.currentTimeMillis()
        SCH.context.authentication = new UsernamePasswordAuthenticationToken(Infos.GOODLOGIN, Infos.GOODPASSWORD, AuthorityUtils.createAuthorityList('ROLE_ADMIN'))

        def aclUtilService = Holders.getGrailsApplication().getMainContext().getBean("aclUtilService")
        aclUtilService.addPermission ontology, user.username, READ
        aclUtilService.addPermission ontology, user.username, WRITE
        aclUtilService.addPermission ontology, user.username, DELETE

        def sessionFactory = Holders.getGrailsApplication().getMainContext().getBean("sessionFactory")
        sessionFactory.currentSession.flush()
        SCH.clearContext()
    }

    static void addUserRight(User user, Software software) {
        long start = System.currentTimeMillis()
        SCH.context.authentication = new UsernamePasswordAuthenticationToken(Infos.GOODLOGIN, Infos.GOODPASSWORD, AuthorityUtils.createAuthorityList('ROLE_ADMIN'))

        def aclUtilService = Holders.getGrailsApplication().getMainContext().getBean("aclUtilService")
        aclUtilService.addPermission software, user.username, ADMINISTRATION
        aclUtilService.addPermission software, user.username, READ
        aclUtilService.addPermission software, user.username, WRITE
        aclUtilService.addPermission software, user.username, DELETE

        def sessionFactory = Holders.getGrailsApplication().getMainContext().getBean("sessionFactory")
        sessionFactory.currentSession.flush()
        SCH.clearContext()
    }


    /**
     * Print all right info for a specific domain
     * @param domain Domain to check
     */
    static void printRight(def domain) {
        def aclUtilService = Holders.getGrailsApplication().getMainContext().getBean("aclUtilService")
        if(!AclObjectIdentity.findByObjectId(domain.id)) return
        def acl = aclUtilService.readAcl(domain)

        acl.entries.eachWithIndex { entry, i ->
            println entry.sid.toString() + " " + findPermissionName(entry.permission)
        }
    }

    /**
     * Print all right info for a specific user
     * @param user User to check
     */
    static void printUserRight(User user) {
        def roles = user.getAuthorities()
        roles.each { role ->
             println role.authority
        }
    }

    /**
     * Convert permission object to a permission string
     * @param permission Permission object
     * @return Permission string
     */
    static String findPermissionName(BasePermission permission) {
        if (permission.equals(READ)) return "READ"
        if (permission.equals(ADMINISTRATION)) return "ADMINISTRATION"
        if (permission.equals(WRITE)) return "WRITE"
        if (permission.equals(DELETE)) return "DELETE"
        return "NOT FOUND"
    }
}
