package be.cytomine.api.security

import be.cytomine.AnnotationDomain
import be.cytomine.CytomineDomain
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityACL
import be.cytomine.api.RestController
import be.cytomine.ontology.Ontology
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.SecurityUtils
import be.cytomine.utils.Utils
import grails.converters.JSON
import org.springframework.security.acls.domain.BasePermission
import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION
import static org.springframework.security.acls.domain.BasePermission.READ
import static org.springframework.security.acls.domain.BasePermission.WRITE
import static org.springframework.security.acls.domain.BasePermission.DELETE
/**
 * Handle HTTP Requests for CRUD operations on the User domain class.
 */
class RestACLController extends RestController {

    def springSecurityService
    def cytomineService
    def secUserService
    def projectService
    def ontologyService
    def imageInstanceService
    def aclAuthService

    def list = {
        log.info "list"
        try {
            if(params.domainClassName && params.domainIdent && params.user) {
                responseSuccess(aclAuthService.get(retrieveCytomineDomain(params.domainClassName,params.long('domainIdent')),SecUser.read(params.long('user'))) )
            } else {
                throw new ObjectNotFoundException("Request not valid: domainClassName=${params.domainClassName}, domainIdent=${params.domainIdent} and user=${params.user}")
            }
        } catch(CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    def add ={
        try {
            if(params.domainClassName && params.domainIdent && params.user) {
                def perm = findPermissionName(params.auth)
                def domain = retrieveCytomineDomain(params.domainClassName,params.long('domainIdent'))
                def user = SecUser.read(params.long('user'))
                responseSuccess(aclAuthService.add(domain,user,perm))
            } else {
                throw new ObjectNotFoundException("Request not valid: domainClassName=${params.domainClassName}, domainIdent=${params.domainIdent} and user=${params.user}")
            }
        } catch(CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    def delete = {
        try {
        if(params.domainClassName && params.domainIdent && params.user) {
            def perm = findPermissionName(params.auth)
            def domain = retrieveCytomineDomain(params.domainClassName,params.long('domainIdent'))
            def user = SecUser.read(params.long('user'))
            responseSuccess(aclAuthService.delete(domain,user,perm))
        } else {
            throw new ObjectNotFoundException("Request not valid: domainClassName=${params.domainClassName}, domainIdent=${params.domainIdent} and user=${params.user}")
        }
        } catch(CytomineException e) {
             log.error(e)
             response([success: false, errors: e.msg], e.code)
         }
    }

    public CytomineDomain retrieveCytomineDomain(String domainClassName, Long domainIdent) {
        try {
            Class.forName(domainClassName, false, Thread.currentThread().contextClassLoader).read(domainIdent)
        } catch(Exception e) {
            throw new ObjectNotFoundException("Cannot find object $domainClassName with id $domainIdent ")
        }
    }

    static BasePermission findPermissionName(String auth) {
        if(auth=="READ") return READ
        if(auth=="WRITE") return WRITE
        if(auth=="DELETE") return DELETE
        if(auth=="ADMINISTRATION") return ADMINISTRATION
        return READ
    }


}
