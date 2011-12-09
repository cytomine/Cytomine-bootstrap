package be.cytomine.test

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import be.cytomine.security.User
import be.cytomine.project.Project
import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION
import static org.springframework.security.acls.domain.BasePermission.DELETE
import static org.springframework.security.acls.domain.BasePermission.READ
import static org.springframework.security.acls.domain.BasePermission.WRITE
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION
import static org.springframework.security.acls.domain.BasePermission.DELETE
import static org.springframework.security.acls.domain.BasePermission.READ
import static org.springframework.security.acls.domain.BasePermission.WRITE
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.springframework.security.acls.domain.BasePermission
import org.springframework.security.acls.model.MutableAcl
import org.springframework.security.acls.model.NotFoundException
import org.springframework.security.acls.model.ObjectIdentity
import org.springframework.security.acls.domain.ObjectIdentityImpl
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

    public static String BADLOGIN = 'badlogin'
    public static String BADPASSWORD = 'badpassword'

    public static String UNDOURL = "command/undo"
    public static String REDOURL = "command/redo"

    public static String BEGINTRANSACT = "transaction/begin"
    public static String ENDTRANSACT = "transaction/end"

    static void addUserRight(String username, Project project) {
        addUserRight(User.findByUsername(username), project)
    }

    static void addUserRight(User user, Project project) {
        println "Add user right " + user.username + " to project " + project.name
        SCH.context.authentication = new UsernamePasswordAuthenticationToken(Infos.GOODLOGIN, Infos.GOODPASSWORD, AuthorityUtils.createAuthorityList('ROLE_ADMIN'))
        def aclService = ApplicationHolder.application.getMainContext().getBean("aclService")
        def objectIdentityRetrievalStrategy = ApplicationHolder.application.getMainContext().getBean("objectIdentityRetrievalStrategy")
        def aclUtilService = ApplicationHolder.application.getMainContext().getBean("aclUtilService")
//        try {
//            //if(!aclService.readAclById(objectIdentityRetrievalStrategy.getObjectIdentity(project))) {
//            aclService.createAcl objectIdentityRetrievalStrategy.getObjectIdentity(project)
//            // }
//        } catch (Exception e) {println e}


        ObjectIdentity oi = new ObjectIdentityImpl(project.class, project.id);
        try {
            MutableAcl acl = (MutableAcl) aclService.readAclById(oi);
        } catch (NotFoundException nfe) {
            aclService.createAcl objectIdentityRetrievalStrategy.getObjectIdentity(project)
        }




        println "Add permission " + user.username + " to project " + project.name
        aclUtilService.addPermission project, user.username, ADMINISTRATION

        def sessionFactory = ApplicationHolder.application.getMainContext().getBean("sessionFactory")
        sessionFactory.currentSession.flush()
        SCH.clearContext()
    }

    static void printRight(def domain) {
        def aclUtilService = ApplicationHolder.application.getMainContext().getBean("aclUtilService")
        def acl = aclUtilService.readAcl(domain)
        println "Right for domain " + domain.id + " name=" + domain?.name

        acl.entries.eachWithIndex { entry, i ->
            println entry.sid.toString() + " " + findPermissionName(entry.permission)
        }
    }

    static void printUserRight(User user) {
        def roles = user.getAuthorities()
        println "User $user.username has role:"
        roles.each { role ->
             println role.authority
        }
    }

    static String findPermissionName(BasePermission permission) {
        if (permission.equals(READ)) return "READ"
        if (permission.equals(ADMINISTRATION)) return "ADMINISTRATION"
        if (permission.equals(WRITE)) return "WRITE"
        if (permission.equals(DELETE)) return "DELETE"
        return "NOT FOUND"
    }

}
