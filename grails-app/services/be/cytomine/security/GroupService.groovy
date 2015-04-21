package be.cytomine.security

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.command.*
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import grails.converters.JSON
import groovy.sql.Sql
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

class GroupService extends ModelService {

    static transactional = true
    def cytomineService
    def commandService
    def userGroupService
    def transactionService
    def securityACLService
    def ldapSearchService
    def CASLdapUserDetailsService
    def dataSource

    def currentDomain() {
        Group
    }

    def list() {
        securityACLService.checkGuest(cytomineService.currentUser)
        return Group.list(sort: "name", order: "asc")
    }

    def listWithUser() {
        securityACLService.checkGuest(cytomineService.currentUser)
        log.info "listWithUser"

        String request = "SELECT g.id as group_id, g.name as group_name, u.id as user_id, u.username as user_name, u.lastname as user_lastname, u.firstname  as user_firstname " +
                "FROM user_group ug, \"group\" g, sec_user u " +
                "WHERE ug.user_id=u.id AND g.id = ug.group_id " +
                "ORDER BY g.name, u.lastname;";

        log.info request
        def sql = new Sql(dataSource)

        def groups = [];
        def group;
        def idGroup = -1;
        def users = new JSONArray();
        sql.eachRow(request) {
            log.info it
            if(idGroup != it.group_id) {
                if(group != null) {
                    group.put("users", users)
                    groups << group
                }

                group = new JSONObject();
                group.put("id", it.group_id)
                group.put("name", it.group_name)
                idGroup = it.group_id
                users = new JSONArray();
            }
            def user = new JSONObject();
            user.put("id", it.user_id)
            user.put("username", it.user_name)
            user.put("lastname", it.user_lastname)
            user.put("firstname", it.user_firstname)

            users.add(user)
        }

        if(group != null) {
            group.put("users", users)
            groups << group
        }


        return groups
    }

    def list(User user) {
        securityACLService.checkGuest(cytomineService.currentUser)
        UserGroup.findByUser(user).collect{it.group}
    }

    def read(def id) {
        securityACLService.checkGuest(cytomineService.currentUser)
        return Group.read(id)
    }

    def get(def id) {
        securityACLService.checkGuest(cytomineService.currentUser)
        return Group.get(id)
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    def add(def json) {
        SecUser currentUser = cytomineService.getCurrentUser()
        securityACLService.checkGuest(currentUser)
        return executeCommand(new AddCommand(user: currentUser),null,json)
    }

    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param jsonNewData New domain datas
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(Group group, def jsonNewData) {
        SecUser currentUser = cytomineService.getCurrentUser()
        securityACLService.checkIfUserIsMemberGroup(currentUser,group)
        return executeCommand(new EditCommand(user: currentUser),group, jsonNewData)
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(Group domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        securityACLService.checkAdmin(currentUser)
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }


    public boolean isInLdap(Long id) {
        SecUser currentUser = cytomineService.getCurrentUser()
        securityACLService.checkGuest(currentUser)
        def group = Group.get(id)
        return !ldapSearchService.searchByUid(group.gid, "cn", "uid").isEmpty();
    }

    def createFromLDAP(def json) {

        SecUser currentUser = cytomineService.getCurrentUser()
        securityACLService.checkGuest(currentUser)

        def map = ldapSearchService.searchByCn(json.name, "uid", "member");

        if (map.isEmpty()) throw ObjectNotFoundException;

        Group result = new Group();
        result.name = json.name
        result.gid = map.get("uid")[0]

        // create group here
        result.discard()
        def reponse = add(JSON.parse(result.encodeAsJSON()))

        result = Group.get(reponse.data.group.id)


        def usernames = map.get("member").collect {it.split(",")[0].split("=")[1]}

        def users = usernames.collect {CASLdapUserDetailsService.getUserByUsername(it)}

        users.each {
            user ->
                //for each user, create a user Group
                UserGroup userGroup = new UserGroup(user:user, group:result)
                userGroup.save()
        }
        return reponse;
    }

    def resetFromLDAP(Long id) {

        SecUser currentUser = cytomineService.getCurrentUser()
        securityACLService.checkGuest(currentUser)

        def group = Group.get(id)

        def map = ldapSearchService.searchByUid(group.gid, "cn", "uid", "member");

        if (map.isEmpty()) throw ObjectNotFoundException;

        def json = JSON.parse(group.encodeAsJSON());
        json["cn"] = map.get("cn")[0]
        json["uid"] = map.get("uid")[0]

        update(group, json)

        group = Group.get(id)

        // delete all the previous user and add the users as described in LDAP
        def users = UserGroup.findAllByGroup(group)
        users.each {
            it.delete()
        }

        def usernames = map.get("member").collect {it.split(",")[0].split("=")[1]}

        users = usernames.collect {CASLdapUserDetailsService.getUserByUsername(it)}

        users.each {
            user ->
                //for each user, create a user Group
                UserGroup userGroup = new UserGroup(user:user, group:group)
                userGroup.save()
        }

        return group
    }


    def getStringParamsI18n(def domain) {
        return [domain.id, domain.name]
    }

    def deleteDependentUserGroup(Group group, Transaction transaction, Task task = null) {
        UserGroup.findAllByGroup(group).each {
            userGroupService.delete(it, transaction,null,false)
        }
    }

}
