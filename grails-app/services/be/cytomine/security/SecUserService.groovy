package be.cytomine.security

import be.cytomine.CytomineDomain
import be.cytomine.Exception.ForbiddenException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityCheck
import be.cytomine.ontology.Ontology
import be.cytomine.project.Project
import be.cytomine.social.LastConnection
import be.cytomine.utils.ModelService
import be.cytomine.utils.Utils
import groovy.sql.Sql
import org.apache.commons.collections.ListUtils
import org.codehaus.groovy.grails.plugins.springsecurity.acl.AclSid
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.access.prepost.PreAuthorize
import be.cytomine.command.*

import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION
import static org.springframework.security.acls.domain.BasePermission.READ
import be.cytomine.image.ImageInstance
import grails.converters.JSON
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.AlgoAnnotationTerm
import be.cytomine.ontology.AnnotationFilter
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.social.SharedAnnotation
import be.cytomine.ontology.UserAnnotation
import be.cytomine.social.UserPosition
import be.cytomine.image.UploadedFile

class SecUserService extends ModelService {

    static transactional = true

    def springSecurityService
    def transactionService
    def cytomineService
    def commandService
    def modelService
    def userGroupService
    def dataSource
    def permissionService
    def algoAnnotationService
    def algoAnnotationTermService
    def annotationFilterService
    def annotationTermService
    def imageInstanceService
    def ontologyService
    def reviewedAnnotationService
    def secUserSecRoleService
    def userAnnotationService

    @PreAuthorize("hasRole('ROLE_USER')")
    def get(def id) {
        SecUser.get(id)
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    SecUser getByPublicKey(String key) {
        SecUser.findByPublicKey(key)
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    def read(def id) {
        SecUser.read(id)
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    def readCurrentUser() {
        cytomineService.getCurrentUser()
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    def list() {
        User.list(sort: "username", order: "asc")
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Project project, List ids) {
        SecUser.findAllByIdInList(ids)
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def listUsers(Project project) {
        List<SecUser> users = SecUser.executeQuery("select distinct secUser from AclObjectIdentity as aclObjectId, AclEntry as aclEntry, AclSid as aclSid, SecUser as secUser "+
            "where aclObjectId.objectId = "+project.id+" and aclEntry.aclObjectIdentity = aclObjectId.id and aclEntry.sid = aclSid.id and aclSid.sid = secUser.username and secUser.class = 'be.cytomine.security.User'")
        return users
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def listCreator(Project project) {
        List<User> users = SecUser.executeQuery("select secUser from AclObjectIdentity as aclObjectId, AclSid as aclSid, SecUser as secUser where aclObjectId.objectId = "+project.id+" and aclObjectId.owner = aclSid.id and aclSid.sid = secUser.username and secUser.class = 'be.cytomine.security.User'")
        User user = users.isEmpty() ? null : users.first()
        return user
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def listAdmins(Project project) {
        def users = SecUser.executeQuery("select distinct secUser from AclObjectIdentity as aclObjectId, AclEntry as aclEntry, AclSid as aclSid, SecUser as secUser "+
            "where aclObjectId.objectId = "+project.id+" and aclEntry.aclObjectIdentity = aclObjectId.id and aclEntry.mask = 16 and aclEntry.sid = aclSid.id and aclSid.sid = secUser.username and secUser.class = 'be.cytomine.security.User'")
        return users
    }

    @PreAuthorize("#ontology.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def listUsers(Ontology ontology) {
        //TODO:: Not optim code a single SQL request will be very faster
        def users = []
        def projects = Project.findAllByOntology(ontology)
        projects.each { project ->
            users.addAll(listUsers(project))
        }
        users.unique()
    }

    /**
     * Get all allowed user id for a specific domain instance
     * E.g; get all user id for a project
     */
    List<Long> getAllowedUserIdList(CytomineDomain domain) {
        String request = "SELECT DISTINCT sec_user.id \n" +
                " FROM acl_object_identity, acl_entry,acl_sid, sec_user \n" +
                " WHERE acl_object_identity.object_id_identity = $domain.id\n" +
                " AND acl_entry.acl_object_identity=acl_object_identity.id\n" +
                " AND acl_entry.sid = acl_sid.id " +
                " AND acl_sid.sid = sec_user.username " +
                " AND sec_user.class = 'be.cytomine.security.User' "
        def data = []
        new Sql(dataSource).eachRow(request) {
            data << it[0]
        }
        return data
    }

    /**
     * List all layers from a project
     * Each user has its own layer
     * If project has private layer, just get current user layer
     */
    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def listLayers(Project project) {
        Collection<SecUser> users = listUsers(project)
        SecUser currentUser = cytomineService.getCurrentUser()
        if (project.privateLayer && users.contains(currentUser)) {
            return [currentUser]
        } else if (!project.privateLayer) {
            return  users
        } else { //should no arrive but possible if user is admin and not in project
            []
        }
    }

    /**
     * Get all online user
     */
    @PreAuthorize("hasRole('ROLE_USER')")
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

    /**
     * Get all online user for a project
     */
    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
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

    /**
     * Get all user that share at least a same project as user from argument
     */
    @PreAuthorize("#user.id == principal.id or hasRole('ROLE_ADMIN')")
    List<SecUser> getAllFriendsUsers(SecUser user) {
        AclSid sid = AclSid.findBySid(user.username)
        List<SecUser> users = SecUser.executeQuery(
            "select distinct secUser from AclSid as aclSid, AclEntry as aclEntry, SecUser as secUser "+
            "where aclEntry.aclObjectIdentity in (select  aclEntry.aclObjectIdentity from aclEntry where sid = ${sid.id}) and aclEntry.sid = aclSid.id and aclSid.sid = secUser.username and aclSid.id!=${sid.id}")

        return users
    }

    /**
     * Get all online user that share at least a same project as user from argument
     */
    @PreAuthorize("#user.id == principal.id or hasRole('ROLE_ADMIN')")
    List<SecUser> getAllFriendsUsersOnline(SecUser user) {
       return ListUtils.intersection(getAllFriendsUsers(user),getAllOnlineUsers())
    }

    /**
     * Get all user that share at least a same project as user from argument and
     */
    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    List<SecUser> getAllFriendsUsersOnline(SecUser user, Project project) {
        //no need to make insterect because getAllOnlineUsers(project) contains only friends users
       return getAllOnlineUsers(project)
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    def add(def json,SecurityCheck security) {
        User currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    /**
     * Update this domain with new data from json
     * @param json JSON with new data
     * @param security Security service object (user for right check)
     * @return  Response structure (new domain data, old domain data..)
     */
    @PreAuthorize("#security.checkCurrentUserCreator(principal.id) or hasRole('ROLE_ADMIN')")
    def update(def json, SecurityCheck security) {
        User currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser), json)
    }

    /**
     * Delete domain in argument
     * @param json JSON that was passed in request parameter
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    def delete(def json, SecurityCheck security) {
        if (json.id == springSecurityService.principal.id) {
            throw new ForbiddenException("A user can't delete herself")
        }
        return delete(retrieve(json),transactionService.start())
    }

    def delete(SecUser user, Transaction transaction = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        def json = JSON.parse("{id: ${user.id}}")
        return executeCommand(new DeleteCommand(user: currentUser), json)
    }

    /**
     * Add a user in project user or admin list
     * @param user User to add in project
     * @param project Project that will be accessed by user
     * @param admin Flaf if user will become a simple user or a project admin
     * @return Response structure
     */
    @PreAuthorize("#project.hasPermission('ADMIN') or hasRole('ROLE_ADMIN')")
    def addUserFromProject(SecUser user, Project project, boolean admin) {
            if (project) {
                log.debug "addUserFromProject project=" + project + " username=" + user?.username + " ADMIN=" + admin
                if(admin) {
                    permissionService.addPermission(project,user.username,ADMINISTRATION)
                    permissionService.addPermission(project.ontology,user.username,READ)
                }
                else {
                    permissionService.addPermission(project,user.username,READ)
                    permissionService.addPermission(project.ontology,user.username,READ)
                }
            }
        [data: [message: "OK"], status: 201]
    }

    /**
     * Delete a user from a project user or admin list
     * @param user User to remove from project
     * @param project Project that will not longer be accessed by user
     * @param admin Flaf if user will become a simple user or a project admin
     * @return Response structure
     */
    @PreAuthorize("#project.hasPermission('ADMIN') or #user.id == principal.id or hasRole('ROLE_ADMIN')")
    def deleteUserFromProject(SecUser user, Project project, boolean admin) {
            if (project) {
                log.info "deleteUserFromProject project=" + project?.id + " username=" + user?.username + " ADMIN=" + admin
                if(admin) {
                    //TODO:: a user admin can remove another admin user?
                    permissionService.deletePermission(project,user.username,ADMINISTRATION)
                    //TODO:: bug code: if user x has access to ontology o thx to project p1 & p2, if x is removed from p1, it loose right from o... => it should keep this right thx to p2!
                    permissionService.deletePermission(project.ontology,user.username,READ)
                }
                else {
                    permissionService.deletePermission(project,user.username,READ)
                    //TODO:: bug code: if user x has access to ontology o thx to project p1 & p2, if x is removed from p1, it loose right from o... => it should keep this right thx to p2!
                    permissionService.deletePermission(project.ontology,user.username,READ)
                }
            }
       [data: [message: "OK"], status: 201]
    }

    /**
     * Create new domain in database
     * @param json JSON data for the new domain
     * @param printMessage Flag to specify if confirmation message must be show in client
     * Usefull when we create a lot of data, just print the root command message
     * @return Response structure (status, object data,...)
     */
    def create(JSONObject json, boolean printMessage) {
        create(User.createFromDataWithId(json), printMessage)
    }

    /**
     * Create new domain in database
     * @param domain Domain to store
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def create(User domain, boolean printMessage) {
        //Save new object
        saveDomain(domain)
        //Build response message
        return responseService.createResponseMessage(domain, [domain.id, domain.username], printMessage, "Add", domain.getCallBack())
    }

    /**
     * Destroy domain from database
     * @param json JSON with domain data (to retrieve it)
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(User.get(json.id), printMessage)
    }

    /**
     * Destroy domain from database
     * @param domain Domain to remove
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(SecUser domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id, domain.username], printMessage, "Delete", domain.getCallBack())

        Command.findAllByUser(domain).each {
            UndoStackItem.findAllByCommand(it).each { it.delete()}
            RedoStackItem.findAllByCommand(it).each { it.delete()}
            CommandHistory.findAllByCommand(it).each {it.delete()}
            it.delete()
        }

        //Delete object
        deleteDomain(domain)
        return response
    }

    /**
     * Edit domain from database
     * @param json domain data in json
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def edit(JSONObject json, boolean printMessage) {
        //Rebuilt previous state of object that was previoulsy edited
        edit(fillDomainWithData(new User(), json), printMessage)
    }

    /**
     * Edit domain from database
     * @param domain Domain to update
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def edit(User domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id, domain.username], printMessage, "Edit", domain.getCallBack())
        //Save update
        saveDomain(domain)
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
        SecUser user = SecUser.get(json.id)
        if (!user) throw new ObjectNotFoundException("User " + json.id + " not found")
        return user
    }




    def deleteDependentAlgoAnnotation(SecUser user, Transaction transaction) {
        if(user instanceof UserJob) {
            AlgoAnnotation.findAllByUser((UserJob)user).each {
                algoAnnotationService.delete(it,transaction, false)
            }
        }
    }

    def deleteDependentAlgoAnnotationTerm(SecUser user, Transaction transaction) {
        if(user instanceof UserJob) {
            AlgoAnnotationTerm.findAllByUserJob((UserJob)user).each {
                algoAnnotationTermService.delete(it,transaction, false)
            }
        }
    }

    def deleteDependentAnnotationFilter(SecUser user, Transaction transaction) {
        if(user instanceof User) {
            AnnotationFilter.findAllByUser(user).each {
                annotationFilterService.delete(it,transaction, false)
            }
        }
    }

    def deleteDependentAnnotationTerm(SecUser user, Transaction transaction) {
        if(user instanceof User) {
            AnnotationTerm.findAllByUser(user).each {
                annotationTermService.delete(it,transaction, false)
            }
        }
    }

    def deleteDependentImageInstance(SecUser user, Transaction transaction) {
        if(user instanceof User) {
            ImageInstance.findAllByUser(user).each {
                imageInstanceService.delete(it,transaction, false)
            }
        }
    }

    def deleteDependentOntology(SecUser user, Transaction transaction) {
        if(user instanceof User) {
            Ontology.findAllByUser(user).each {
                ontologyService.delete(it,transaction, false)
            }
        }
    }

    def deleteDependentReviewedAnnotation(SecUser user, Transaction transaction) {
        if(user instanceof User) {
            ReviewedAnnotation.findAllByUser(user).each {
                reviewedAnnotationService.delete(it,transaction, false)
            }
        }
    }

    def deleteDependentSecUserSecRole(SecUser user, Transaction transaction) {
        SecUserSecRole.findAllBySecUser(user).each {
            secUserSecRoleService.delete(it,transaction, false)
        }
    }

    def deleteDependentUserAnnotation(SecUser user, Transaction transaction) {
        if(user instanceof User) {
            UserAnnotation.findAllByUser(user).each {
                userAnnotationService.delete(it,transaction, false)
            }
        }
    }

    def deleteDependentUserGroup(SecUser user, Transaction transaction) {
        if(user instanceof User) {
            UserGroup.findAllByUser((User)user).each {
                userGroupService.delete(it,transaction, false)
            }
        }
    }

    def deleteDependentUserPosition(SecUser user, Transaction transaction) {
        if(user instanceof User) {
            UserPosition.findAllByUser((User)user).each {
                it.delete()
            }
        }
    }

    def deleteDependentUserJob(SecUser user, Transaction transaction) {
        if(user instanceof User) {
            UserJob.findAllByUser((User)user).each {
                delete(it,transaction,false)
            }
        }
    }

    def deleteDependentUploadedFile(SecUser user, Transaction transaction) {
        if(user instanceof User) {
            UploadedFile.findAllByUser((User)user).each {
                it.delete()
            }
        }
    }

    def deleteDependentTask(SecUser user, Transaction transaction) {
        if(user instanceof User) {
            Task.findAllByUser((User)user).each {
                it.delete()
            }
        }
    }

    def deleteDependentLastConnection(SecUser user, Transaction transaction) {
        if(user instanceof User) {
            LastConnection.findAllByUser((User)user).each {
                it.delete()
            }
        }
    }

}
