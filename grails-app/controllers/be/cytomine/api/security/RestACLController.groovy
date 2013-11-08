package be.cytomine.api.security

import be.cytomine.CytomineDomain
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.api.RestController
import be.cytomine.security.SecUser
import org.springframework.security.acls.domain.BasePermission

import static org.springframework.security.acls.domain.BasePermission.*

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
        try {
            if(params.domainClassName && params.domainIdent && params.user) {
                def domain = retrieveCytomineDomain(params.domainClassName,params.long('domainIdent'))
                responseSuccess(aclAuthService.get(domain,SecUser.read(params.long('user'))) )
            } else {
                throw new ObjectNotFoundException("Request not valid: domainClassName=${params.domainClassName}, domainIdent=${params.domainIdent} and user=${params.user}")
            }
        } catch(CytomineException e) {
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
            def user = SecUser.read(params.long('user'))
            if(params.domainClassName && params.domainIdent && user) {
                def perm = findPermissionName(params.auth)
                def domain = retrieveCytomineDomain(params.domainClassName,params.long('domainIdent'))

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
        CytomineDomain domain
        try {
            domain = Class.forName(domainClassName, false, Thread.currentThread().contextClassLoader).read(domainIdent)
        } catch(Exception e) {
            throw new ObjectNotFoundException("Cannot find object $domainClassName with id $domainIdent ")
        }
        if(!domain) throw new ObjectNotFoundException("Request not valid: domainClassName=${params.domainClassName}, domainIdent=${params.domainIdent} and user=${params.user}")
    }

    static BasePermission findPermissionName(String auth) {
        if(auth=="READ") {
            return READ
        } else if(auth=="WRITE") {
            return WRITE
        } else if(auth=="DELETE") {
            return DELETE
        } else if(auth=="ADMINISTRATION") {
            return ADMINISTRATION
        } else {
            return READ
        }

    }


}
