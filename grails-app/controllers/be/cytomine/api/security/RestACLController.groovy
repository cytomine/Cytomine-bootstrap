package be.cytomine.api.security

import be.cytomine.CytomineDomain
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.api.RestController
import be.cytomine.security.SecUser
import jsondoc.annotation.ApiMethodLight
import org.jsondoc.core.annotation.Api
import org.jsondoc.core.annotation.ApiParam
import org.jsondoc.core.annotation.ApiParams
import org.jsondoc.core.annotation.ApiResponseObject
import org.jsondoc.core.pojo.ApiParamType
import org.springframework.security.acls.domain.BasePermission

import static org.springframework.security.acls.domain.BasePermission.*

@Api(name = "acl services", description = "Methods for managing ACL, a permission for an user on a specific domain instance")
class RestACLController extends RestController {

    def springSecurityService
    def cytomineService
    def secUserService
    def projectService
    def ontologyService
    def imageInstanceService
    def aclAuthService

    @ApiMethodLight(description="Get all ACL for a user and a class.", listing=true)
    @ApiParams(params=[
        @ApiParam(name="domainClassName", type="string", paramType = ApiParamType.PATH, description = "The domain class"),
        @ApiParam(name="domainIdent", type="long", paramType = ApiParamType.PATH, description = "The domain id"),
        @ApiParam(name="user", type="long", paramType = ApiParamType.PATH, description = "The user id")
    ])
    @ApiResponseObject(objectIdentifier="List of all permission name (empty if user has no permission)")
    def list() {
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

    @ApiMethodLight(description="Add a new permission for a user on a domain", listing=true)
    @ApiParams(params=[
        @ApiParam(name="domainClassName", type="string", paramType = ApiParamType.PATH, description = "The domain class"),
        @ApiParam(name="domainIdent", type="long", paramType = ApiParamType.PATH, description = "The domain id"),
        @ApiParam(name="user", type="long", paramType = ApiParamType.PATH, description = "The user id")
    ])
    @ApiResponseObject(objectIdentifier="List of all permission name (empty if user has no permission)")
    def add (){
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

    @ApiMethodLight(description="Delete a permission for a user on a domain", listing=true)
    @ApiParams(params=[
        @ApiParam(name="domainClassName", type="string", paramType = ApiParamType.PATH, description = "The domain class"),
        @ApiParam(name="domainIdent", type="long", paramType = ApiParamType.PATH, description = "The domain id"),
        @ApiParam(name="user", type="long", paramType = ApiParamType.PATH, description = "The user id")
    ])
    @ApiResponseObject(objectIdentifier="List of all permission name (empty if user has no permission)")
    def delete() {
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
        domain
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
