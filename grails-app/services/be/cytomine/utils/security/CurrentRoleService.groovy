package be.cytomine.utils.security

/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import be.cytomine.Exception.ForbiddenException
import be.cytomine.security.SecRole
import be.cytomine.security.SecUser
import be.cytomine.security.SecUserSecRole
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.security.core.context.SecurityContextHolder

//method "byNow" get current roles. An admin is by default connected as USER (isAdmin=true, isAdminByNow=false).
//when he ask to open an admin session, he becomes admin (isAdmin=true, isAdminByNow=true)
class CurrentRoleService implements Serializable {

    static final long serialVersionUID = 1L; //assign a long value

    static scope = 'session'

    static transactional = false

    public isAdmin = false

    /**
     * Active an admin session for a user
     * (by default user with ROLE_ADMIN are connected as ROLE_USER)
     * @param user
     */
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

    /**
     * Disable admin session for a user
     * @param user
     */
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

    /**
     * Get all user roles (even disabled role)
     */
    Set<SecRole> findRealRole(SecUser user) {
        //log.info "Look for role for ${user.username}"
        Set<SecRole> roles = SecUserSecRole.findAllBySecUser(user).collect { it.secRole }
        //log.info "Roles found ${roles.collect{it.authority}}"
        return roles
    }

    /**
     * Get all active roles
     */
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

    /**
     * Check if user is admin (with admin session opened)
     */
    boolean isAdminByNow(SecUser user) {
        def auths = findCurrentRole(user).collect{it.authority}
        return auths.contains("ROLE_ADMIN") || auths.contains("ROLE_SUPER_ADMIN")
    }
    boolean isUserByNow(SecUser user) {
        return findCurrentRole(user).collect{it.authority}.contains("ROLE_USER")
    }
    boolean isGuestByNow(SecUser user) {
        return findCurrentRole(user).collect{it.authority}.contains("ROLE_GUEST")
    }

    /**
     * Check if user is admin (with admin session closed or opened)
     */
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
