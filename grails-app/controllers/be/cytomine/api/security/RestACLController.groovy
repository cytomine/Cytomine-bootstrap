package be.cytomine.api.security

import be.cytomine.CytomineDomain
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityACL
import be.cytomine.api.RestController
import be.cytomine.image.server.Storage
import be.cytomine.ontology.Ontology
import be.cytomine.processing.Software
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import groovy.sql.Sql
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApi

import org.restapidoc.annotation.RestApiParams
import org.restapidoc.annotation.RestApiResponseObject
import org.restapidoc.pojo.RestApiParamType
import org.springframework.security.acls.domain.BasePermission

import static org.springframework.security.acls.domain.BasePermission.*

@RestApi(name = "acl services", description = "Methods for managing ACL, a permission for an user on a specific domain instance")
class RestACLController extends RestController {

    def springSecurityService
    def cytomineService
    def secUserService
    def projectService
    def ontologyService
    def imageInstanceService
    def aclAuthService
    def dataSource
    def softwareService
    def storageService

    @RestApiMethod(description="Get all ACL for a user and a class.", listing=true)
    @RestApiParams(params=[
        @RestApiParam(name="domainClassName", type="string", paramType = RestApiParamType.PATH, description = "The domain class"),
        @RestApiParam(name="domainIdent", type="long", paramType = RestApiParamType.PATH, description = "The domain id"),
        @RestApiParam(name="user", type="long", paramType = RestApiParamType.PATH, description = "The user id")
    ])
    @RestApiResponseObject(objectIdentifier="List of all permission name (empty if user has no permission)")
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

    def listDomain() {
        List<Project> projects = projectService.list()
        List<Ontology> ontologies = ontologyService.list()
        List<Software> softwares = softwareService.list()
        List<Storage> storages = storageService.list()

        def data = []
//        data.project = projects.collect{return [id:it.id,name:it.name]}
//        data.ontology = ontologies.collect{return [id:it.id,name:it.name]}
//        data.software = softwares.collect{return [id:it.id,name:it.name]}
//        data.storage = storages.collect{return [id:it.id,name:it.name]}
        data.addAll(projects.collect{return [id:it.id,name:it.name,className:it.class.name]})
        data.addAll(ontologies.collect{return [id:it.id,name:it.name,className:it.class.name]})
        data.addAll(softwares.collect{return [id:it.id,name:it.name,className:it.class.name]})
        data.addAll(storages.collect{return [id:it.id,name:it.name,className:it.class.name]} )
        responseSuccess(data)
    }

    def listACL() {

        def idUser = params.long('idUser')
        def idDomain = params.long('idDomain')
        def currentUser = cytomineService.currentUser

        def domainTableMap = [
                "project" : Project.class.name,
                "ontology" : Ontology.class.name,
                "storage" : Storage.class.name,
                "software" : Software.class.name
        ]

        def data = []


        domainTableMap.each {
            String selectOnlyACLWhereCurrentUserIsAdmin = ""

            /*
               Take a permission set (class,idobject,sid, mask...) that can be view for example as
                [ {class:"be.cyto...project", name: "project a", id: 10, sid: "johndoe", mask:16 },... ]
                (Means that johndoe has access admin (16) to "project a"
             */

            if(!currentUser.isAdmin()) {
                /*
                    This sub request select only permission on object on which current user has admin access.
                    e.g. if "janedoe" execute this request, she must have admin (=16) access to  "project a"
                    If current user is admin, get all permission
                 */
                selectOnlyACLWhereCurrentUserIsAdmin = "AND ${it.key}.id IN (" +
                        "                SELECT acl_object_identity.object_id_identity as id\n" +
                                "                FROM acl_object_identity, acl_class, acl_entry, acl_sid, ${it.key}\n" +
                                "                WHERE acl_object_identity.object_id_class = acl_class.id\n" +
                                "                AND acl_entry.acl_object_identity = acl_object_identity.id\n" +
                                "                AND acl_sid.id = acl_entry.sid\n" +
                                "                AND ${it.key}.id = acl_object_identity.object_id_identity\n" +
                                "                AND acl_class.class like '${it.value}'\n" +
                                "                AND acl_sid.sid like '${currentUser.username}'\n" +
                                "                AND acl_entry.mask = ${ADMINISTRATION.mask}\n" +
                                "        )"
            }


            String request = "SELECT DISTINCT acl_class.class as domainClassName, ${it.key}.name as name,acl_object_identity.object_id_identity as domainIdent, acl_entry.mask as mask,acl_sid.sid as sid, sec_user.id as idUser\n" +
                    "      FROM acl_object_identity, acl_class, acl_entry, acl_sid, ${it.key}, sec_user\n" +
                    "      WHERE acl_object_identity.object_id_class = acl_class.id\n" +
                    "      AND acl_entry.acl_object_identity = acl_object_identity.id\n" +
                    "      AND acl_sid.id = acl_entry.sid\n" +
                    "      AND ${it.key}.id = acl_object_identity.object_id_identity\n" +
                    "      AND acl_class.class like '${it.value}'\n" +
                    (idUser? "AND sec_user.id = ${idUser}\n" : "")+
                    (idDomain? "AND ${it.key}.id = ${idDomain}\n" : "")+
                    "      AND sec_user.username = acl_sid.sid\n" +
                    "      ${selectOnlyACLWhereCurrentUserIsAdmin};"

            log.info request

            def sql = new Sql(dataSource)
             sql.eachRow(request) {
                def perm = [:]
                perm.domainClassName=it.domainClassName
                perm.name=it.name
                perm.domainIdent=it.domainIdent
                perm.sid=it.sid
                perm.idUser=it.idUser
                perm.mask=it.mask
                data << perm
            }
            try {
                sql.close()
            }catch (Exception e) {}



        }


        responseSuccess(data)

    }




    @RestApiMethod(description="Add a new permission for a user on a domain", listing=true)
    @RestApiParams(params=[
        @RestApiParam(name="domainClassName", type="string", paramType = RestApiParamType.PATH, description = "The domain class"),
        @RestApiParam(name="domainIdent", type="long", paramType = RestApiParamType.PATH, description = "The domain id"),
        @RestApiParam(name="user", type="long", paramType = RestApiParamType.PATH, description = "The user id"),
        @RestApiParam(name="auth", type="string", paramType = RestApiParamType.QUERY, description = "(Optional, default READ) The permission (READ, WRITE, DELETE or PERMISSION)")
    ])
    @RestApiResponseObject(objectIdentifier="List of all permission name (empty if user has no permission)")
    def add (){
        try {
            if(params.domainClassName && params.domainIdent && params.user && params.user!="undefined") {
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

    @RestApiMethod(description="Delete a permission for a user on a domain", listing=true)
    @RestApiParams(params=[
        @RestApiParam(name="domainClassName", type="string", paramType = RestApiParamType.PATH, description = "The domain class"),
        @RestApiParam(name="domainIdent", type="long", paramType = RestApiParamType.PATH, description = "The domain id"),
        @RestApiParam(name="user", type="long", paramType = RestApiParamType.PATH, description = "The user id"),
        @RestApiParam(name="auth", type="string", paramType = RestApiParamType.PATH, description = "(Optional, default READ)  The permission (READ, WRITE, DELETE or PERMISSION)")
    ])
    @RestApiResponseObject(objectIdentifier="Delete a permission")
    def delete() {
        try {
            def user = SecUser.read(params.long('user'))
            log.info "user ${user} with id = ${params.long('user')}"
            if(params.domainClassName && params.domainIdent && user) {
                def perm = findPermissionName(params.auth)
                def domain = retrieveCytomineDomain(params.domainClassName,params.long('domainIdent'))

                responseSuccess(aclAuthService.delete(domain,user,perm))
            } else {
                throw new ObjectNotFoundException("Request not valid: domainClassName=${params.domainClassName}, domainIdent=${params.domainIdent} and user=${params.user} (${user})")
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
        } else if(auth=="ADMINISTRATION" || auth=="ADMIN") {
            return ADMINISTRATION
        } else {
            return READ
        }

    }
}
