package be.cytomine.test

import org.springframework.security.core.context.SecurityContextHolder as SCH

import be.cytomine.project.Project
import be.cytomine.security.User
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.codehaus.groovy.grails.plugins.springsecurity.acl.AclObjectIdentity
import org.springframework.security.acls.domain.BasePermission
import org.springframework.security.acls.domain.ObjectIdentityImpl
import org.springframework.security.acls.model.MutableAcl
import org.springframework.security.acls.model.NotFoundException
import org.springframework.security.acls.model.ObjectIdentity
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

    def aclService
    def aclUtilService
    def objectIdentityRetrievalStrategy
    def sessionFactory
    def springSecurityService

    public static String CYTOMINEURL = ConfigurationHolder.config.grails.serverURL + "/"

    public static String GOODLOGIN = "lrollus"
    public static String GOODPASSWORD = 'lR$2011'

    public static String ANOTHERLOGIN = "rmaree"
    public static String ANOTHERPASSWORD = 'rM$2011'

    public static String GOODPASSWORDUSERJOB = 'PasswordUserJob'

    public static String BADLOGIN = 'badlogin'
    public static String BADPASSWORD = 'badpassword'

    public static String UNDOURL = "command/undo"
    public static String REDOURL = "command/redo"

    public static String BEGINTRANSACT = "transaction/begin"
    public static String ENDTRANSACT = "transaction/end"

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
//        def aclService = ApplicationHolder.application.getMainContext().getBean("aclService")
//        def objectIdentityRetrievalStrategy = ApplicationHolder.application.getMainContext().getBean("objectIdentityRetrievalStrategy")
        def aclUtilService = ApplicationHolder.application.getMainContext().getBean("aclUtilService")

//        ObjectIdentity oi = new ObjectIdentityImpl(project.class, project.id);
//        try {
//            MutableAcl acl = (MutableAcl) aclService.readAclById(oi);
//        } catch (NotFoundException nfe) {
//            aclService.createAcl objectIdentityRetrievalStrategy.getObjectIdentity(project)
//        }

        aclUtilService.addPermission project, user.username, ADMINISTRATION
        aclUtilService.addPermission project.ontology, user.username, READ
        aclUtilService.addPermission project.ontology, user.username, WRITE
        aclUtilService.addPermission project.ontology, user.username, DELETE

        def sessionFactory = ApplicationHolder.application.getMainContext().getBean("sessionFactory")
        sessionFactory.currentSession.flush()
        SCH.clearContext()
        println "###### ADD RIGHT TIME = ${System.currentTimeMillis()-start}ms"
    }




    /**
     * Print all right info for a specific domain
     * @param domain Domain to check
     */
    static void printRight(def domain) {
        def aclUtilService = ApplicationHolder.application.getMainContext().getBean("aclUtilService")
        if(!AclObjectIdentity.findByObjectId(domain.id)) return
        def acl = aclUtilService.readAcl(domain)
        println "Right for domain " + domain.id + " name=" + domain?.name

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
        println "User $user.username has role:"
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
