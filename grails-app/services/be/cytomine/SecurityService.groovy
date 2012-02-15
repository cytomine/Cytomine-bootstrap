package be.cytomine

import be.cytomine.security.User
import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION
import org.springframework.security.acls.model.Acl
import org.springframework.transaction.annotation.Transactional
import org.springframework.security.acls.model.NotFoundException
import org.codehaus.groovy.grails.plugins.springsecurity.acl.AclObjectIdentity
import be.cytomine.security.SecUser
import org.codehaus.groovy.grails.plugins.springsecurity.acl.AclEntry

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

    List<SecUser> getAdminList(def domain) {

        def users = SecUser.executeQuery("select distinct secUser from AclObjectIdentity as aclObjectId, AclEntry as aclEntry, AclSid as aclSid, SecUser as secUser "+
            "where aclObjectId.objectId = "+domain.id+" and aclEntry.aclObjectIdentity = aclObjectId.id and aclEntry.mask = 16 and aclEntry.sid = aclSid.id and aclSid.sid = secUser.username and secUser.class like 'be.cytomine.security.User'")

        return users
    }

    //@Transactional(noRollbackFor = NotFoundException.class)

    List<SecUser> getUserList(def domain) {
        List<SecUser> users = SecUser.executeQuery("select distinct secUser from AclObjectIdentity as aclObjectId, AclEntry as aclEntry, AclSid as aclSid, SecUser as secUser "+
            "where aclObjectId.objectId = "+domain.id+" and aclEntry.aclObjectIdentity = aclObjectId.id and aclEntry.sid = aclSid.id and aclSid.sid = secUser.username and secUser.class like 'be.cytomine.security.User'")

        return users
    }

}
