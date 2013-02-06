package be.cytomine.security

import be.cytomine.CytomineDomain
import org.springframework.security.acls.domain.ObjectIdentityImpl
import org.springframework.security.acls.model.MutableAcl
import org.springframework.security.acls.model.NotFoundException
import org.springframework.security.acls.model.ObjectIdentity
import org.springframework.security.acls.model.Permission

class PermissionService {

    static transactional = true

    def aclService
    def aclUtilService
    def aclPermissionFactory
    def objectIdentityRetrievalStrategy

    /**
     * Add Permission right
     * @param domain
     * @param username
     * @param permission
     */
    void addPermission(def domain, String username, int permission) {
        addPermission(domain, username, aclPermissionFactory.buildFromMask(permission))
    }

    void addPermission(def domain, String username, Permission permission) {
        synchronized (this.getClass()) {
            log.info "Add Permission " +  permission.mask + " for " + username + " to " + domain.class + " " + domain.id

            ObjectIdentity oi = new ObjectIdentityImpl(domain.class, domain.id);
            try {
                MutableAcl acl = (MutableAcl) aclService.readAclById(oi);
            } catch (NotFoundException nfe) {
                aclService.createAcl objectIdentityRetrievalStrategy.getObjectIdentity(domain)
            }

            println "${domain.class} ${domain.id} ${domain.version}"
            println "${username} ${SecUser.findByUsername(username)} ${SecUser.findByUsername(username).version}"

            log.info "Try to add permission..."
            aclUtilService.addPermission(domain, username, permission)
            log.info "Permission added..."
        }
    }

    synchronized void deletePermission(CytomineDomain domain, String username, Permission permission) {
        def acl = aclUtilService.readAcl(domain)
        log.info "Delete Permission " +  permission.mask + " for " + username + " from " + domain.class + " " + domain.id
        // Remove all permissions associated with this particular recipient
        acl.entries.eachWithIndex { entry, i ->
            if (entry.sid.getPrincipal().equals(username) && entry.permission.equals(permission)) {
                acl.deleteAce(i)
            }
        }
        aclService.updateAcl(acl)
    }
}
