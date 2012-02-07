package be.cytomine

import be.cytomine.security.User
import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION
import org.springframework.security.acls.model.Acl
import org.springframework.transaction.annotation.Transactional
import org.springframework.security.acls.model.NotFoundException
import org.codehaus.groovy.grails.plugins.springsecurity.acl.AclObjectIdentity
import be.cytomine.security.SecUser

class SecurityService {

    def aclUtilService

    def transactional = false

    static transaction = false

    def serviceMethod() {

    }

    User getCreator(def domain) {
        User user
        try {
            AclObjectIdentity aclObject = AclObjectIdentity.findByObjectId(domain.id)
            if (aclObject) {
                Acl acl = aclUtilService.readAcl(domain)
                def owner = acl.getOwner()
                user = User.findByUsername(owner.getPrincipal())
            }
        } catch (Exception e) {e.printStackTrace()}
        return user
    }

    List<User> getAdminList(def domain) {
        List<User> users = []
        try {
            AclObjectIdentity aclObject = AclObjectIdentity.findByObjectId(domain.id)
            if (aclObject) {
                def acl = aclUtilService.readAcl(domain)
                acl.entries.each { entry ->
                    if (entry.permission.equals(ADMINISTRATION))
                        users.add(User.findByUsername(entry.sid.getPrincipal()))
                }
            }
        } catch (Exception e) {e.printStackTrace()}
        return users
    }

    //@Transactional(noRollbackFor = NotFoundException.class)

    List<SecUser> getUserList(def domain) {
        //log.info "*************"+domain
        List<SecUser> users = []
        try {
            AclObjectIdentity aclObject = AclObjectIdentity.findByObjectId(domain.id)
            if (aclObject) {
                def acl = aclUtilService.readAcl(domain)
                acl.entries.each { entry ->
                    SecUser user = SecUser.findByUsername(entry.sid.getPrincipal())
                    //log.info "user authorize="+user.username
                    if(!user.algo() && !users.contains(user))
                        users.add(user)
                }
            }
        }
        catch (org.springframework.security.acls.model.NotFoundException e) {
            println e
        }
        return users
    }

}
