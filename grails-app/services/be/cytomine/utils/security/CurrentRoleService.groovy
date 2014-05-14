package be.cytomine.utils.security

import be.cytomine.Exception.ForbiddenException
import be.cytomine.security.SecRole
import be.cytomine.security.SecUser
import be.cytomine.security.SecUserSecRole
import be.cytomine.security.User
import be.cytomine.test.Infos

//method "byNow" get current roles. An admin is by default connected as USER (isAdmin=true, isAdminByNow=false).
//when he ask to open an admin session, he becomes admin (isAdmin=true, isAdminByNow=true)
class CurrentRoleService {

    static scope = 'session'

    def cytomineService


    static transactional = false

    public isAdmin = false

    def activeAdminSession() {
        if(findRealRole().find{it.authority=="ROLE_ADMIN"}) {
            isAdmin = true
        } else {
            throw new ForbiddenException("You are not an admin!")
        }
    }
    def closeAdminSession() {
        if(findRealRole().find{it.authority=="ROLE_ADMIN"}) {
            isAdmin = false
        } else {
            throw new ForbiddenException("You are not an admin!")
        }
    }

    Set<SecRole> findRealRole(SecUser user = cytomineService.currentUser) {
        log.info "Look for role for ${user.username}"
        Set<SecRole> roles = SecUserSecRole.findAllBySecUser(user).collect { it.secRole }
        log.info "Roles found ${roles.collect{it.authority}}"
        return roles
    }

    Set<SecRole> findCurrentRole(SecUser user = cytomineService.currentUser) {
        Set<SecRole> roles = findRealRole(user)
        boolean isSuperAdmin =  (roles.find {it.authority=="ROLE_SUPER_ADMIN"}!=null)
        //role super admin don't need to open a admin session, so we don't remove the role admin from the current role
        log.info "isSuperAdmin=$isSuperAdmin isAdmin=$isAdmin"
        if(!isAdmin && !isSuperAdmin) {
            roles = roles.findAll {it.authority!="ROLE_ADMIN"}
        }
        println "roles=${roles.collect{it.authority}}"
        return roles
    }

    boolean isAdminByNow(SecUser user = cytomineService.currentUser) {
        return findCurrentRole(user).collect{it.authority}.contains("ROLE_ADMIN")
    }
    boolean isUserByNow(SecUser user = cytomineService.currentUser) {
        println "****************************************"
        println findCurrentRole(user).collect{it.authority}.contains("ROLE_USER")
        return findCurrentRole(user).collect{it.authority}.contains("ROLE_USER")
    }
    boolean isGuestByNow(SecUser user = cytomineService.currentUser) {
        return findCurrentRole(user).collect{it.authority}.contains("ROLE_GUEST")
    }

    boolean isAdmin(SecUser user = cytomineService.currentUser) {
        return findRealRole(user).collect{it.authority}.contains("ROLE_ADMIN")
    }
    boolean isUser(SecUser user = cytomineService.currentUser) {
        return findRealRole(user).collect{it.authority}.contains("ROLE_USER")
    }
    boolean isGuest(SecUser user = cytomineService.currentUser) {
        return findRealRole(user).collect{it.authority}.contains("ROLE_GUEST")
    }
}