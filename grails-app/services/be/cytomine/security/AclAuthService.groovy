package be.cytomine.security

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
