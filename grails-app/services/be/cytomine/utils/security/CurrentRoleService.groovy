package be.cytomine.utils.security

import be.cytomine.Exception.ForbiddenException
import be.cytomine.security.SecRole
import be.cytomine.security.SecUser
import be.cytomine.security.SecUserSecRole
import be.cytomine.security.User
import be.cytomine.test.Infos
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.security.core.context.SecurityContextHolder

//method "byNow" get current roles. An admin is by default connected as USER (isAdmin=true, isAdminByNow=false).
//when he ask to open an admin session, he becomes admin (isAdmin=true, isAdminByNow=true)
class CurrentRoleService implements Serializable {

    static scope = 'session'

    static transactional = false

    public isAdmin = false

    def activeAdminSession(SecUser user) {
        if(findRealRole(user).find{it.authority=="ROLE_ADMIN"}) {
            isAdmin = true

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(auth.getAuthorities());
            authorities.add(new GrantedAuthorityImpl('ROLE_ADMIN'));
            Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(),auth.getCredentials(),authorities)
            SecurityContextHolder.getContext().setAuthentication(newAuth);
        } else {
            throw new ForbiddenException("You are not an admin!")
        }
    }
    def closeAdminSession(SecUser user) {
        if(findRealRole(user).find{it.authority=="ROLE_ADMIN"}) {
            isAdmin = false
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(auth.getAuthorities());
            authorities = authorities.findAll{it.authority!="ROLE_ADMIN"}
            Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(),auth.getCredentials(),authorities)
            SecurityContextHolder.getContext().setAuthentication(newAuth);
        } else {
            throw new ForbiddenException("You are not an admin!")
        }
    }

    Set<SecRole> findRealRole(SecUser user) {
        //log.info "Look for role for ${user.username}"
        Set<SecRole> roles = SecUserSecRole.findAllBySecUser(user).collect { it.secRole }
        //log.info "Roles found ${roles.collect{it.authority}}"
        return roles
    }

    Set<SecRole> findCurrentRole(SecUser user) {
        Set<SecRole> roles = findRealRole(user)
        boolean isSuperAdmin =  (roles.find {it.authority=="ROLE_SUPER_ADMIN"}!=null)
        //role super admin don't need to open a admin session, so we don't remove the role admin from the current role
        //log.info "isSuperAdmin=$isSuperAdmin isAdmin=$isAdmin"
        if(!isAdmin && !isSuperAdmin) {
            roles = roles.findAll {it.authority!="ROLE_ADMIN"}
        }
        return roles
    }

    boolean isAdminByNow(SecUser user) {
        return findCurrentRole(user).collect{it.authority}.contains("ROLE_ADMIN")
    }
    boolean isUserByNow(SecUser user) {
        return findCurrentRole(user).collect{it.authority}.contains("ROLE_USER")
    }
    boolean isGuestByNow(SecUser user) {
        return findCurrentRole(user).collect{it.authority}.contains("ROLE_GUEST")
    }

    boolean isAdmin(SecUser user) {
        return findRealRole(user).collect{it.authority}.contains("ROLE_ADMIN")
    }
    boolean isUser(SecUser user) {
        return findRealRole(user).collect{it.authority}.contains("ROLE_USER")
    }
    boolean isGuest(SecUser user) {
        return findRealRole(user).collect{it.authority}.contains("ROLE_GUEST")
    }
}
