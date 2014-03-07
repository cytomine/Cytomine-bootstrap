package be.cytomine.security

import be.cytomine.CytomineDomain
import be.cytomine.Exception.ConstraintException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityACL
import be.cytomine.command.*
import be.cytomine.image.ImageInstance
import be.cytomine.image.NestedImageInstance
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

import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION
import static org.springframework.security.acls.domain.BasePermission.READ

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

    def currentDomain() {
        User
    }

    def get(def id) {
        SecurityACL.checkGuest(cytomineService.currentUser)
        SecUser.get(id)
    }

    def findByUsername(def username) {
        if(!username) return null
        SecurityACL.checkGuest(cytomineService.currentUser)
        SecUser.findByUsername(username)
    }

    def findByEmail(def email) {
        if(!email) return null
        SecurityACL.checkGuest(cytomineService.currentUser)
        User.findByEmail(email)
    }

    SecUser getByPublicKey(String key) {
        //SecurityACL.checkGuest(cytomineService.currentUser)
        SecUser.findByPublicKey(key)
    }

    def read(def id) {
        SecurityACL.checkGuest(cytomineService.currentUser)
        SecUser.read(id)
    }

    def getAuth(SecUser user) {
        def data = [:]
        data['admin'] = user.isAdminAuth()
        data['user'] = !data['admin'] && user.isUserAuth()
        data['guest'] = !data['admin'] && !data['user'] && user.isGuestAuth()
//        data['admin'] = false
//        data['user'] = false
//        data['ghest'] = true
        return data
    }

    def readCurrentUser() {
        SecurityACL.checkGuest(cytomineService.currentUser)
        cytomineService.getCurrentUser()
    }

    def list() {
        SecurityACL.checkGuest(cytomineService.currentUser)
        User.list(sort: "username", order: "asc")
    }

    def list(Project project, List ids) {
        SecurityACL.check(project,READ)
        SecUser.findAllByIdInList(ids)
    }

    def listAll(Project project) {
        def data = []
        data.addAll(listUsers(project))
        //TODO: could be optim!!!
        data.addAll(UserJob.findAllByJobInList(Job.findAllByProject(project)))
        data
    }

    def listUsers(Project project) {
        SecurityACL.check(project,READ)
        List<SecUser> users = SecUser.executeQuery("select distinct secUser from AclObjectIdentity as aclObjectId, AclEntry as aclEntry, AclSid as aclSid, SecUser as secUser "+
                "where aclObjectId.objectId = "+project.id+" and aclEntry.aclObjectIdentity = aclObjectId.id and aclEntry.sid = aclSid.id and aclSid.sid = secUser.username and secUser.class = 'be.cytomine.security.User'")
        return users
    }

    def listCreator(Project project) {
        SecurityACL.check(project,READ)
        List<User> users = SecUser.executeQuery("select secUser from AclObjectIdentity as aclObjectId, AclSid as aclSid, SecUser as secUser where aclObjectId.objectId = "+project.id+" and aclObjectId.owner = aclSid.id and aclSid.sid = secUser.username and secUser.class = 'be.cytomine.security.User'")
        User user = users.isEmpty() ? null : users.first()
        return user
    }


    def listAdmins(Project project) {
        SecurityACL.check(project,READ)
        def users = SecUser.executeQuery("select distinct secUser from AclObjectIdentity as aclObjectId, AclEntry as aclEntry, AclSid as aclSid, SecUser as secUser "+
                "where aclObjectId.objectId = "+project.id+" and aclEntry.aclObjectIdentity = aclObjectId.id and aclEntry.mask = 16 and aclEntry.sid = aclSid.id and aclSid.sid = secUser.username and secUser.class = 'be.cytomine.security.User'")
        return users
    }

    def listUsers(Ontology ontology) {
        SecurityACL.check(ontology,READ)
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


    private def getUserJobImage(ImageInstance image) {
                    //better perf with sql request
                String request = "SELECT u.id as id, u.username as username, s.name as softwareName, j.created as created \n" +
                        "FROM annotation_index ai, sec_user u, job j, software s\n" +
                        "WHERE ai.image_id = ${image.id}\n" +
                        "AND ai.user_id = u.id\n" +
                        "AND u.job_id = j.id\n" +
                        "AND j.software_id = s.id\n" +
                        "ORDER BY j.created"
                def data = []
                new Sql(dataSource).eachRow(request) {
                    def item = [:]
                    item.id = it.id
                    item.username = it.username
                    item.softwareName = it.softwareName
                    item.created = it.created
                    item.algo = true
                    data << item
                }
        data
    }


    /**
     * List all layers from a project
     * Each user has its own layer
     * If project has private layer, just get current user layer
     */
    def listLayers(Project project, ImageInstance image = null) {
        SecurityACL.check(project,READ)
        SecUser currentUser = cytomineService.getCurrentUser()
        def users = []
        def humans = listUsers(project)
        users.addAll(humans)

        if(image) {
            def jobs = getUserJobImage(image)
            users.addAll(jobs)
        }
        def  admins = listAdmins(project)




        if(project.checkPermission(ADMINISTRATION)) {
            return users
        } else if(project.hideAdminsLayers && project.hideUsersLayers && users.contains(currentUser)) {
            return [currentUser]
        } else if(project.hideAdminsLayers && !project.hideUsersLayers && users.contains(currentUser)) {
            users.removeAll(admins)
            return users
        } else if(!project.hideAdminsLayers && project.hideUsersLayers && users.contains(currentUser)) {
            admins.add(currentUser)
            return admins
         }else if(!project.hideAdminsLayers && !project.hideUsersLayers && users.contains(currentUser)) {
            return users
         }else { //should no arrive but possible if user is admin and not in project
             []
         }

//
//        //if user is admin of the project, show all layers
//        if (!project.checkPermission(ADMINISTRATION) && project.privateLayer && users.contains(currentUser)) {
//            return [currentUser]
//        } else if (!project.privateLayer || project.checkPermission(ADMINISTRATION)) {
//            return  users
//        } else { //should no arrive but possible if user is admin and not in project
//            []
//        }
    }

    /**
     * Get all online user
     */
    List<SecUser> getAllOnlineUsers() {
        SecurityACL.checkGuest(cytomineService.currentUser)
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
    List<SecUser> getAllOnlineUsers(Project project) {
        SecurityACL.check(project,READ)
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
    List<SecUser> getAllFriendsUsers(SecUser user) {
        SecurityACL.checkIsSameUser(user,cytomineService.currentUser)
        AclSid sid = AclSid.findBySid(user.username)
        List<SecUser> users = SecUser.executeQuery(
                "select distinct secUser from AclSid as aclSid, AclEntry as aclEntry, SecUser as secUser "+
                        "where aclEntry.aclObjectIdentity in (select  aclEntry.aclObjectIdentity from aclEntry where sid = ${sid.id}) and aclEntry.sid = aclSid.id and aclSid.sid = secUser.username and aclSid.id!=${sid.id}")

        return users
    }

    /**
     * Get all online user that share at least a same project as user from argument
     */
    List<SecUser> getAllFriendsUsersOnline(SecUser user) {
        SecurityACL.checkIsSameUser(user,cytomineService.currentUser)
        return ListUtils.intersection(getAllFriendsUsers(user),getAllOnlineUsers())
    }

    /**
     * Get all user that share at least a same project as user from argument and
     */
    List<SecUser> getAllFriendsUsersOnline(SecUser user, Project project) {
        SecurityACL.check(project,READ)
        //no need to make insterect because getAllOnlineUsers(project) contains only friends users
        return getAllOnlineUsers(project)
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    def add(def json) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.checkAdmin(currentUser)
        return executeCommand(new AddCommand(user: currentUser),null,json)
    }

    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param jsonNewData New domain datas
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(SecUser user, def jsonNewData) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.checkIsCreator(user,currentUser)
        return executeCommand(new EditCommand(user: currentUser),user, jsonNewData)
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(SecUser domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        if(domain.algo()) {
            Job job = ((UserJob)domain).job
            SecurityACL.check(job?.container(),READ)
        } else {
            SecurityACL.checkAdmin(currentUser)
            SecurityACL.checkIsNotSameUser(domain,currentUser)
        }
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }

    /**
     * Add a user in project user or admin list
     * @param user User to add in project
     * @param project Project that will be accessed by user
     * @param admin Flaf if user will become a simple user or a project admin
     * @return Response structure
     */
    def addUserToProject(SecUser user, Project project, boolean admin) {
        SecurityACL.check(project,ADMINISTRATION)
        log.info "service.addUserToProject"
        if (project) {
            log.info "addUserToProject project=" + project + " username=" + user?.username + " ADMIN=" + admin
            if(admin) {
                synchronized (this.getClass()) {
                    permissionService.addPermission(project,user.username,ADMINISTRATION)
                    permissionService.addPermission(project,user.username,READ)
                    permissionService.addPermission(project.ontology,user.username,READ)
                }
            }
            else {
                synchronized (this.getClass()) {
                    log.info "addUserToProject project=" + project + " username=" + user?.username + " ADMIN=" + admin
                    permissionService.addPermission(project,user.username,READ)
                    log.info "addUserToProject ontology=" + project.ontology + " username=" + user?.username + " ADMIN=" + admin
                    permissionService.addPermission(project.ontology,user.username,READ)
                }

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
    def deleteUserFromProject(SecUser user, Project project, boolean admin) {
        if (cytomineService.currentUser.id!=user.id) {
            SecurityACL.check(project,ADMINISTRATION)
        }
        if (project) {
            log.info "deleteUserFromProject project=" + project?.id + " username=" + user?.username + " ADMIN=" + admin
            if(admin) {
                //TODO:: a user admin can remove another admin user?
                permissionService.deletePermission(project,user.username,ADMINISTRATION)
                //TODO:: bug code: if user x has access to ontology o thx to project p1 & p2, if x is removed from p1, it loose right from o... => it should keep this right thx to p2!
                //permissionService.deletePermission(project.ontology,user.username,READ)
            }
            else {
                permissionService.deletePermission(project,user.username,READ)
                //TODO:: bug code: if user x has access to ontology o thx to project p1 & p2, if x is removed from p1, it loose right from o... => it should keep this right thx to p2!
                permissionService.deletePermission(project.ontology,user.username,READ)
            }
        }
        [data: [message: "OK"], status: 201]
    }

    def beforeDelete(def domain) {
        Command.findAllByUser(domain).each {
            UndoStackItem.findAllByCommand(it).each { it.delete()}
            RedoStackItem.findAllByCommand(it).each { it.delete()}
            CommandHistory.findAllByCommand(it).each {it.delete()}
            it.delete()
        }
    }

    def afterAdd(def domain, def response) {
        SecUserSecRole.create(domain,SecRole.findByAuthority("ROLE_USER"),true)
    }

    def getStringParamsI18n(def domain) {
        return [domain.id, domain.username]
    }

    /**
      * Retrieve domain thanks to a JSON object
      * WE MUST OVERRIDE THIS METHOD TO READ USER AND USERJOB (ALL SECUSER)
      * @param json JSON with new domain info
      * @return domain retrieve thanks to json
      */
    def retrieve(Map json) {
        SecUser user = SecUser.get(json.id)
        if (!user) throw new ObjectNotFoundException("User " + json.id + " not found")
        return user
    }



    def deleteDependentAlgoAnnotation(SecUser user, Transaction transaction, Task task = null) {
        if(user instanceof UserJob) {
            AlgoAnnotation.findAllByUser((UserJob)user).each {
                algoAnnotationService.delete(it,transaction,null,false)
            }
        }
    }

    def deleteDependentAlgoAnnotationTerm(SecUser user, Transaction transaction, Task task = null) {
        if(user instanceof UserJob) {
            AlgoAnnotationTerm.findAllByUserJob((UserJob)user).each {
                algoAnnotationTermService.delete(it,transaction,null, false)
            }
        }
    }

    def deleteDependentAnnotationFilter(SecUser user, Transaction transaction, Task task = null) {
        if(user instanceof User) {
            AnnotationFilter.findAllByUser(user).each {
                annotationFilterService.delete(it,transaction, null,false)
            }
        }
    }

    def deleteDependentAnnotationTerm(SecUser user, Transaction transaction, Task task = null) {
        if(user instanceof User) {
            AnnotationTerm.findAllByUser(user).each {
                annotationTermService.delete(it,transaction,null, false)
            }
        }
    }

    def deleteDependentImageInstance(SecUser user, Transaction transaction, Task task = null) {
        if(user instanceof User) {
            ImageInstance.findAllByUser(user).each {
                imageInstanceService.delete(it,transaction,null, false)
            }
        }
    }

    def deleteDependentOntology(SecUser user, Transaction transaction, Task task = null) {
        if(user instanceof User) {
            Ontology.findAllByUser(user).each {
                ontologyService.delete(it,transaction,null, false)
            }
        }
    }

    def deleteDependentForgotPasswordToken(SecUser secUser, Transaction transaction, Task task = null) {
          if (secUser instanceof User) {
              User user = (User) secUser
              ForgotPasswordToken.findAllByUser(user).each {
                  it.delete()
              }
          }

    }

    def deleteDependentReviewedAnnotation(SecUser user, Transaction transaction, Task task = null) {
        if(user instanceof User) {
            ReviewedAnnotation.findAllByUser(user).each {
                reviewedAnnotationService.delete(it,transaction,null, false)
            }
        }
    }

    def deleteDependentSecUserSecRole(SecUser user, Transaction transaction, Task task = null) {
        SecUserSecRole.findAllBySecUser(user).each {
            secUserSecRoleService.delete(it,transaction,null, false)
        }
    }

    def deleteDependentAbstractImage(SecUser user, Transaction transaction, Task task = null) {
        //:to do implemented this ? allow this or not ?
    }

    def deleteDependentUserAnnotation(SecUser user, Transaction transaction, Task task = null) {
        if(user instanceof User) {
            UserAnnotation.findAllByUser(user).each {
                userAnnotationService.delete(it,transaction,null, false)
            }
        }
    }

    def deleteDependentUserGroup(SecUser user, Transaction transaction, Task task = null) {
        if(user instanceof User) {
            UserGroup.findAllByUser((User)user).each {
                userGroupService.delete(it,transaction,null, false)
            }
        }
    }

    def deleteDependentUserPosition(SecUser user, Transaction transaction, Task task = null) {
        if(user instanceof User) {
            UserPosition.findAllByUser((User)user).each {
                it.delete()
            }
        }
    }

    def deleteDependentUserJob(SecUser user, Transaction transaction, Task task = null) {
        if(user instanceof User) {
            UserJob.findAllByUser((User)user).each {
                delete(it,transaction,null,false)
            }
        }
    }

    def deleteDependentUploadedFile(SecUser user, Transaction transaction, Task task = null) {
        if(user instanceof User) {
            UploadedFile.findAllByUser((User)user).each {
                it.delete()
            }
        }
    }

    def deleteDependentNews(SecUser user, Transaction transaction, Task task = null) {
        if(user instanceof User) {
            News.findAllByUser((User)user).each {
                it.delete()
            }
        }
    }

    def deleteDependentLastConnection(SecUser user, Transaction transaction, Task task = null) {
        if(user instanceof User) {
            LastConnection.findAllByUser((User)user).each {
                it.delete()
            }
        }
    }

    def deleteDependentHasManyAnnotationFilter(SecUser user, Transaction transaction, Task task = null) {
        def criteria = AnnotationFilter.createCriteria()
        def results = criteria.list {
            users {
                inList("id", user.id)
            }
        }
        results.each {
            it.removeFromUsers(user)
            it.save()
        }
    }

    def deleteDependentStorage(SecUser user,Transaction transaction, Task task = null) {
        for (storage in Storage.findAllByUser(user)) {
            if (StorageAbstractImage.countByStorage(storage) > 0) {
                throw new ConstraintException("Storage contains data, cannot delete user. Remove or assign storage to an another user first")
            } else {
                ImageServerStorage.findAllByStorage(storage).each {
                    it.delete()
                }
                storage.delete()
            }
        }
    }

    def deleteDependentSharedAnnotation(SecUser user, Transaction transaction, Task task = null) {
        if(user instanceof User) {
            //TODO:: implement cascade deleteting/update for shared annotation
            if(SharedAnnotation.findAllBySender(user)) {
                throw new ConstraintException("This user has send/receive annotation comments. We cannot delete it! ")
            }
        }
    }

    def deleteDependentHasManySharedAnnotation(SecUser user, Transaction transaction, Task task = null) {
        if(user instanceof User) {
            //TODO:: implement cascade deleteting/update for shared annotation
            def criteria = SharedAnnotation.createCriteria()
            def results = criteria.list {
                receivers {
                    inList("id", user.id)
                }
            }

            if(!results.isEmpty()) {
                throw new ConstraintException("This user has send/receive annotation comments. We cannot delete it! ")
            }
        }
    }

    def deleteDependentAnnotationIndex(SecUser user,Transaction transaction, Task task = null) {
        AnnotationIndex.findAllByUser(user).each {
            it.delete()
         }
    }

    def deleteDependentNestedImageInstance(SecUser user, Transaction transaction,Task task=null) {
        NestedImageInstance.findAllByUser(user).each {
            it.delete(flush: true)
        }
    }
}
