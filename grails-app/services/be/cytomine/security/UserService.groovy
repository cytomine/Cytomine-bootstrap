package be.cytomine.security

import be.cytomine.Exception.ForbiddenException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.ontology.Ontology
import be.cytomine.project.Project
import be.cytomine.social.LastConnection
import be.cytomine.utils.Utils
import org.apache.commons.collections.ListUtils
import org.codehaus.groovy.grails.plugins.springsecurity.acl.AclSid
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.access.prepost.PreAuthorize

import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION
import static org.springframework.security.acls.domain.BasePermission.READ
import static org.springframework.security.acls.domain.BasePermission.WRITE
import static org.springframework.security.acls.domain.BasePermission.DELETE
import org.springframework.security.acls.model.Permission
import org.springframework.transaction.annotation.Transactional
import be.cytomine.SecurityCheck
import be.cytomine.CytomineDomain
import org.springframework.security.acls.model.ObjectIdentity
import org.springframework.security.acls.domain.ObjectIdentityImpl
import org.springframework.security.acls.model.MutableAcl
import org.springframework.security.acls.model.NotFoundException
import org.springframework.security.core.context.SecurityContextHolder as SCH

class UserService extends ModelService {

    static transactional = true

    def springSecurityService
    def transactionService
    def cytomineService
    def commandService
    def domainService
    def userGroupService
    def securityService
    def aclService
    def aclUtilService
    def aclPermissionFactory
    def objectIdentityRetrievalStrategy


    //TODO:: these call MUST BE DONE FROM CONTROLLER
    void addPermission(def domain, String username, int permission) {
        log.info "##### Add Permission 1: " +  permission + " for " + username + " to " + domain
        addPermission(domain, username, aclPermissionFactory.buildFromMask(permission))
    }

    //TODO:: these call MUST BE DONE FROM CONTROLLER
    @PreAuthorize("#domain.hasPermission('ADMIN') or hasRole('ROLE_ADMIN')")
    synchronized void addPermission(def domain, String username, Permission permission) {
        log.info "##### Add Permission: " +  permission.mask + " for " + username + " to " + domain.id

        println SCH.context.authentication

        ObjectIdentity oi = new ObjectIdentityImpl(domain.class, domain.id);
        try {
            MutableAcl acl = (MutableAcl) aclService.readAclById(oi);
        } catch (NotFoundException nfe) {
            aclService.createAcl objectIdentityRetrievalStrategy.getObjectIdentity(domain)
        }

        aclUtilService.addPermission(domain, username, permission)
        log.info "#####  Permission added"
    }

    //TODO:: these call MUST BE DONE FROM CONTROLLER
    @Transactional
    @PreAuthorize("#domain.hasPermission('ADMIN') or #user.id == principal.id or hasRole('ROLE_ADMIN')")
    void deletePermission(CytomineDomain domain, SecUser user, Permission permission) {
        def acl = aclUtilService.readAcl(domain)
        log.info "deletePermission $domain $user $permission"
        // Remove all permissions associated with this particular recipient
        acl.entries.eachWithIndex { entry, i ->
            log.debug "entry.permission.equals(permission)="+entry.permission.equals(permission) + " entry.sid="+entry.sid.getPrincipal()
            if (entry.sid.getPrincipal().equals(user.username) && entry.permission.equals(permission)) {
                log.debug "REMOVE PERMISSION FOR"
                acl.deleteAce(i)
            }
        }

        aclService.updateAcl(acl)
    }


    def get(def id) {
        SecUser.get(id)
    }

    SecUser getByPublicKey(String key) {
        SecUser.findByPublicKey(key)
    }

    def read(def id) {
        SecUser.read(id)
    }

    def readCurrentUser() {
        cytomineService.getCurrentUser()
    }

    def list() {
        User.list(sort: "username", order: "asc")
    }

    def list(Project project) {
        securityService.getUserList(project)
    }

    def list(Project project, List ids) {
        SecUser.findAllByIdInList(ids)
    }

    //TODO:: secure!
    def listCreator(Project project) {
        securityService.getCreator(project)
    }

    def listAdmins(Project project) {
        securityService.getAdminList(project)
    }

    def listUsers(Project project) {
        securityService.getUserList(project)
    }

    def listUsers(Ontology ontology) {
        //TODO:: Not optim code a single SQL request will be very faster
        def users = []
        def projects = Project.findAllByOntology(ontology)
        projects.each { project ->
            users.addAll(listUsers(project))
        }
        users.unique()
    }

    def listLayers(Project project) {
        Collection<SecUser> users = securityService.getUserList(project)
        SecUser currentUser = cytomineService.getCurrentUser()
        if (project.privateLayer && users.contains(currentUser)) {
            return [currentUser]
        } else if (!project.privateLayer) {
            return  users
        } else { //should no arrive but possible if user is admin and not in project
            []
        }
    }


    def add(def json,SecurityCheck security) {
        User currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    def update(def json, SecurityCheck security) {
        User currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser), json)
    }

    def delete(def json, SecurityCheck security) {
        User currentUser = cytomineService.getCurrentUser()

        if (json.id == springSecurityService.principal.id) throw new ForbiddenException("The user can't delete herself")
        return executeCommand(new DeleteCommand(user: currentUser), json)
    }

    List<SecUser> getAllFriendsUsers(SecUser user) {
        AclSid sid = AclSid.findBySid(user.username)
        List<SecUser> users = SecUser.executeQuery(
            "select distinct secUser from AclSid as aclSid, AclEntry as aclEntry, SecUser as secUser "+
            "where aclEntry.aclObjectIdentity in (select  aclEntry.aclObjectIdentity from aclEntry where sid = ${sid.id}) and aclEntry.sid = aclSid.id and aclSid.sid = secUser.username and aclSid.id!=${sid.id}")

        return users
    }

    List<SecUser> getAllOnlineUsers() {
        //get date with -X secondes
        def xSecondAgo = Utils.getDatePlusSecond(-20000)
        def results = LastConnection.withCriteria {
            ge('date', xSecondAgo)
            projections {
                groupProperty("user")
            }
        }
        return results
    }

    List<SecUser> getAllOnlineUsers(Project project) {
        if(!project) return getAllOnlineUsers()
        def xSecondAgo = Utils.getDatePlusSecond(-20)
        def results = LastConnection.withCriteria {
            ge('date', xSecondAgo)
            eq('project',project)
            projections {
                groupProperty("user")
            }
        }
        return results
    }

    List<SecUser> getAllFriendsUsersOnline(SecUser user) {
       return ListUtils.intersection(getAllFriendsUsers(user),getAllOnlineUsers())
    }

    List<SecUser> getAllFriendsUsersOnline(SecUser user, Project project) {
        //no need to make insterect because getAllOnlineUsers(project) contains only friends users
       return getAllOnlineUsers(project)
    }

    /*def clearUser = {
   log.info "ClearUser"
   User currentUser = getCurrentUser(springSecurityService.principal.id)
   log.info "User:" + currentUser.username + " params.idProject=" + params.id
   ///ouh le vilain code!
   Project project = Project.get(params.id)
   log.info "project= " + project
   if (project) {
     Group group = Group.findByName(project.name)
     log.info "group= " + group
     if (group) {
       ProjectGroup.unlink(project, group)
       def users = group.users();
       log.info users.size()
       users.each { user ->
         log.info "remove " + user.username + " from " + group.name
         UserGroup.unlink(user, group)
       }
       group.delete(flush:true)

       log.info "Group and project are unlink"
     }
     else {
       log.error "Cannot find group " + project.name
     }
   }
   response.status = 201
   def ret = [data: [message: "ok"], status: 201]
   response(ret)

 }   */

//    def deleteUserFromProject(User user, Project project) {
//        synchronized (this.getClass()) {
//
//            if (project) {
//                Group group = Group.findByName(project.name)
//                log.info "group= " + group
//                if (!group) {
//                    group = new Group(name: project.name)
//                    group.save()
//                    ProjectGroup.link(project, group)
//                }
//
//                userGroupService.unlink(user, group)
//            }
//        }
//    }
//
//    def addUserFromProject(User user, Project project) {
//        synchronized (this.getClass()) {
//            if (project) {
//                Group group = Group.findByName(project.name)
//                log.info "group= " + group
//                if (!group) {
//                    group = new Group(name: project.name)
//                    group.save()
//                    ProjectGroup.link(project, group)
//                }
//                userGroupService.link(user, group);
//            }
//        }
//        def ret = [data: [message: "OK"], status: 201]
//        ret
//    }

    //TODO: when project private/public, a user must have permission to add himself in a public project
    // #user.id == principal.id?????????
    @PreAuthorize("#project.hasPermission('ADMIN') or hasRole('ROLE_ADMIN')")
    def addUserFromProject(SecUser user, Project project, boolean admin) {
            if (project) {
                log.debug "addUserFromProject project=" + project + " username=" + user.username + " ADMIN=" + admin
                if(admin) {
                    //TODO:: these call MUST BE DONE FROM CONTROLLER
                    addPermission(project,user.username,ADMINISTRATION)
                    addPermission(project.ontology,user.username,READ)
                    addPermission(project.ontology,user.username,WRITE)
                    addPermission(project.ontology,user.username,DELETE)
                }
                else {
                    //TODO:: these call MUST BE DONE FROM CONTROLLER
                    addPermission(project,user.username,READ)
                    addPermission(project.ontology,user.username,READ)
                    addPermission(project.ontology,user.username,WRITE)
                    addPermission(project.ontology,user.username,DELETE)
                }
            }
        [data: [message: "OK"], status: 201]
    }

    @PreAuthorize("#project.hasPermission('ADMIN') or #user.id == principal.id or hasRole('ROLE_ADMIN')")
    def deleteUserFromProject(SecUser user, Project project, boolean admin) {
            if (project) {
                log.info "deleteUserFromProject project=" + project?.id + " username=" + user?.username + " ADMIN=" + admin
                //TODO:: these call MUST BE DONE FROM CONTROLLER
                if(admin) {
                    deletePermission(project,user,ADMINISTRATION)
                    deletePermission(project.ontology,user,READ)
                    deletePermission(project.ontology,user,WRITE)
                    deletePermission(project.ontology,user,DELETE)
                }
                else {
                    //TODO:: these call MUST BE DONE FROM CONTROLLER
                    deletePermission(project,user,READ)
                    deletePermission(project.ontology,user,READ)
                    deletePermission(project.ontology,user,WRITE)
                    deletePermission(project.ontology,user,DELETE)
                    //projectService.deletePermission(project,user.username,WRITE)
                }
            }
       [data: [message: "OK"], status: 201]
    }



    def list(def currentPage, def maxRows, def sortIndex, def sortOrder, def firstName, def lastName, def email) {
        def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows
        def users = User.createCriteria().list(max: maxRows, offset: rowOffset) {
            if (firstName)
                ilike('firstname', "%${firstName}%")

            if (lastName)
                ilike('lastname', "%${lastName}%")

            if (email)
                ilike('email', "%${email}%")

            order(sortIndex, sortOrder)
        }
        return users
    }

    /**
     * Restore domain which was previously deleted
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def create(JSONObject json, boolean printMessage) {
        create(User.createFromDataWithId(json), printMessage)
    }

    def create(User domain, boolean printMessage) {
        //Save new object
        domainService.saveDomain(domain)
        //Build response message
        return responseService.createResponseMessage(domain, [domain.id, domain.username], printMessage, "Add", domain.getCallBack())
    }
    /**
     * Destroy domain which was previously added
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(User.get(json.id), printMessage)
    }

    def destroy(User domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id, domain.username], printMessage, "Delete", domain.getCallBack())
        //Delete object
        domainService.deleteDomain(domain)
        return response
    }

    /**
     * Edit domain which was previously edited
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def edit(JSONObject json, boolean printMessage) {
        //Rebuilt previous state of object that was previoulsy edited
        edit(fillDomainWithData(new User(), json), printMessage)
    }

    def edit(User domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id, domain.username], printMessage, "Edit", domain.getCallBack())
        //Save update
        domainService.saveDomain(domain)
        return response
    }

    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    User createFromJSON(def json) {
        return User.createFromData(json)
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     */
    def retrieve(JSONObject json) {
        User user = User.get(json.id)
        if (!user) throw new ObjectNotFoundException("User " + json.id + " not found")
        return user
    }
}
