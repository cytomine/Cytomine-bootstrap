package be.cytomine.security

import be.cytomine.CytomineDomain
import be.cytomine.SecurityACL
import be.cytomine.utils.ModelService
import org.springframework.security.acls.domain.BasePermission

class AclAuthService extends ModelService {

    static transactional = true
    def permissionService
    def cytomineService

    def get(CytomineDomain domain, SecUser user) {
        SecurityACL.checkAdmin(cytomineService.currentUser)
        return domain.getPermissionInACL(domain,user)
    }

    def add(CytomineDomain domain, SecUser user, BasePermission permission) {
        SecurityACL.checkAdmin(cytomineService.currentUser)
        def oldPerms = domain.getPermissionInACL(domain,user)
        if(permission.equals(BasePermission.ADMINISTRATION)) {
            if(!oldPerms.contains(BasePermission.READ.mask)) permissionService.addPermission(domain,user.username,BasePermission.READ)
            if(!oldPerms.contains(BasePermission.ADMINISTRATION.mask)) permissionService.addPermission(domain,user.username,BasePermission.ADMINISTRATION)
        } else {
            if(!oldPerms.contains(permission.mask)) permissionService.addPermission(domain,user.username,permission)
        }
        return domain.getPermissionInACL(domain,user)
    }


    def delete(CytomineDomain domain, SecUser user, BasePermission permission) {
        SecurityACL.checkAdmin(cytomineService.currentUser)
        permissionService.deletePermission(domain,user.username,permission)
        return domain.getPermissionInACL(domain,user)
    }
}
