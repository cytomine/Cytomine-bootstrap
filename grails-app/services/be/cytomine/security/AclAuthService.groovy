package be.cytomine.security

import be.cytomine.CytomineDomain
import be.cytomine.Exception.ConstraintException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityACL
import be.cytomine.command.*
import be.cytomine.image.ImageInstance
import be.cytomine.image.UploadedFile
import be.cytomine.image.server.ImageServerStorage
import be.cytomine.image.server.Storage
import be.cytomine.image.server.StorageAbstractImage
import be.cytomine.ontology.*
import be.cytomine.processing.Job
import be.cytomine.project.Project
import be.cytomine.social.LastConnection
import be.cytomine.social.SharedAnnotation
import be.cytomine.social.UserPosition
import be.cytomine.utils.ModelService
import be.cytomine.utils.News
import be.cytomine.utils.Task
import be.cytomine.utils.Utils
import groovy.sql.Sql
import org.apache.commons.collections.ListUtils
import org.codehaus.groovy.grails.plugins.springsecurity.acl.AclSid
import org.springframework.security.acls.domain.BasePermission

import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION
import static org.springframework.security.acls.domain.BasePermission.READ

class AclAuthService extends ModelService {

    static transactional = true
    def permissionService

    def get(CytomineDomain domain, SecUser user) {
        log.info "Get permission"
        SecurityACL.checkAdmin(cytomineService.currentUser)
        log.info "Search permission"
        return domain.getPermission(domain,user)
    }

    def add(CytomineDomain domain, SecUser user, BasePermission permission) {
        SecurityACL.checkAdmin(cytomineService.currentUser)
        def oldPerms = domain.getPermission(domain,user)
        if(permission.equals(BasePermission.ADMINISTRATION)) {
            if(!oldPerms.contains(BasePermission.READ.mask)) permissionService.addPermission(domain,user.username,BasePermission.READ)
            if(!oldPerms.contains(BasePermission.ADMINISTRATION.mask)) permissionService.addPermission(domain,user.username,BasePermission.ADMINISTRATION)
        } else {
            if(!oldPerms.contains(permission.mask)) permissionService.addPermission(domain,user.username,permission)
        }
        return domain.getPermission(domain,user)
    }


    def delete(CytomineDomain domain, SecUser user, BasePermission permission) {
        SecurityACL.checkAdmin(cytomineService.currentUser)
        permissionService.deletePermission(domain,user.username,permission)
        return domain.getPermission(domain,user)
    }
}
