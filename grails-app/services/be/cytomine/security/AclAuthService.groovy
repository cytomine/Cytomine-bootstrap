package be.cytomine.security

import be.cytomine.CytomineDomain
import be.cytomine.project.Project
import be.cytomine.utils.ModelService
import org.springframework.security.acls.domain.BasePermission

import static org.springframework.security.acls.domain.BasePermission.READ

class AclAuthService extends ModelService {

    static transactional = true
    def permissionService
    def cytomineService
    def securityACLService

    def get(CytomineDomain domain, SecUser user) {
        securityACLService.checkAdmin(cytomineService.currentUser)
        return domain.getPermissionInACL(domain,user)
    }

    def add(CytomineDomain domain, SecUser user, BasePermission permission) {
        securityACLService.checkAdmin(cytomineService.currentUser)
        def oldPerms = domain.getPermissionInACL(domain,user)
        if(permission.equals(BasePermission.ADMINISTRATION)) {
            if(!oldPerms.contains(BasePermission.READ.mask)) permissionService.addPermission(domain,user.username,BasePermission.READ)
            if(!oldPerms.contains(BasePermission.ADMINISTRATION.mask)) permissionService.addPermission(domain,user.username,BasePermission.ADMINISTRATION)
        } else {
            if(!oldPerms.contains(permission.mask)) permissionService.addPermission(domain,user.username,permission)
        }

        //if domain is a project, add permission to its ontology too
        if(domain instanceof Project) {
            add(((Project)domain).ontology,user,READ)
        }

        return domain.getPermissionInACL(domain,user)
    }


    def delete(CytomineDomain domain, SecUser user, BasePermission permission) {
        securityACLService.checkAdmin(cytomineService.currentUser)
        permissionService.deletePermission(domain,user.username,permission)
        return domain.getPermissionInACL(domain,user)
    }
}
