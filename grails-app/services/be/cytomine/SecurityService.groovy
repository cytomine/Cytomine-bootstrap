package be.cytomine

import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION
import org.springframework.security.acls.model.Acl

import org.springframework.security.acls.model.NotFoundException

import be.cytomine.security.SecUser
import be.cytomine.security.User

class SecurityService {

    def aclUtilService

    def transactional = false

    static transaction = false

    def serviceMethod() {

    }

    User getCreator(def domain) {
        List<User> users = SecUser.executeQuery("select secUser from AclObjectIdentity as aclObjectId, AclSid as aclSid, SecUser as secUser where aclObjectId.objectId = "+domain.id+" and aclObjectId.owner = aclSid.id and aclSid.sid = secUser.username and secUser.class = 'be.cytomine.security.User'")
        User user = users.isEmpty() ? null : users.first()
        return user
    }

    List<SecUser> getAdminList(def domain) {

        def users = SecUser.executeQuery("select distinct secUser from AclObjectIdentity as aclObjectId, AclEntry as aclEntry, AclSid as aclSid, SecUser as secUser "+
            "where aclObjectId.objectId = "+domain.id+" and aclEntry.aclObjectIdentity = aclObjectId.id and aclEntry.mask = 16 and aclEntry.sid = aclSid.id and aclSid.sid = secUser.username and secUser.class = 'be.cytomine.security.User'")

        return users
    }

    //@Transactional(noRollbackFor = NotFoundException.class)

    List<SecUser> getUserList(def domain) {
        List<SecUser> users = SecUser.executeQuery("select distinct secUser from AclObjectIdentity as aclObjectId, AclEntry as aclEntry, AclSid as aclSid, SecUser as secUser "+
            "where aclObjectId.objectId = "+domain.id+" and aclEntry.aclObjectIdentity = aclObjectId.id and aclEntry.sid = aclSid.id and aclSid.sid = secUser.username and secUser.class = 'be.cytomine.security.User'")

        return users
    }

}
