package be.cytomine.security

import be.cytomine.Exception.ObjectNotFoundException
import groovy.sql.Sql
import org.springframework.security.acls.model.Permission

class PermissionService {

    static transactional = true

    def aclService
    def aclUtilService
    def aclPermissionFactory
    def objectIdentityRetrievalStrategy
    def dataSource
    def cytomineService


//    synchronized void deletePermission(CytomineDomain domain, String username, Permission permission) {
//        def acl = aclUtilService.readAcl(domain)
//        log.info "Delete Permission " +  permission.mask + " for " + username + " from " + domain.class + " " + domain.id
//        // Remove all permissions associated with this particular recipient
//        acl.entries.eachWithIndex { entry, i ->
//            if (entry.sid.getPrincipal().equals(username) && entry.permission.equals(permission)) {
//                acl.deleteAce(i)
//            }
//        }
//        aclService.updateAcl(acl)
//    }

    void deletePermission(def domain, String username, Permission permission) {
        println "Delete permission for $username, ${permission.mask}, ${domain.id}"
        def aoi = executeAclRequest("SELECT id FROM acl_object_identity WHERE object_id_identity = ?",[domain.id])
        int mask = permission.mask
        def sid = executeAclRequest("SELECT id FROM acl_sid WHERE sid = ?",[username])

        if(!aoi || !sid) throw ObjectNotFoundException("User ${username} or Object ${domain.id} are not in ACL")

        executeAclCUD("DELETE FROM acl_entry WHERE acl_object_identity = ? AND mask = ? AND sid = ?",[aoi,mask,sid])
    }

    /**
     * Add Permission right
     * @param domain
     * @param username
     * @param permission
     */
    void addPermission(def domain, String username, int permission) {
        addPermission(domain, username, aclPermissionFactory.buildFromMask(permission))
    }

    void addPermission(def domain, String username, Permission permission) {

        //get domain class id
        def ac = getAclClass(domain)

        //get acl sid for current user (run request)
        def sidCurrentUser = getAclSid(cytomineService.currentUser.username)

        //get acl object id
        def aoi = getAclObjectIdentity(domain, ac, sidCurrentUser)

        //get acl sid for the user
        def sid = getAclSid(username)

        //get acl entry
        def ace = getAclEntry(aoi,sid,permission.mask)




//        id   | ace_order | acl_object_identity | audit_failure | audit_success | granting | mask |  sid
//    --------+-----------+---------------------+---------------+---------------+----------+------+--------
//     333386 |         0 |              304690 | f             | f             | t        |   16 | 304445
//     333387 |         1 |              304690 | f             | f             | t        |   16 | 304436
//     333388 |         2 |              304690 | f             | f             | t        |   16 | 304451
//     333389 |         3 |              304690 | f             | f             | t        |   16 | 304700
//     333390 |         4 |              304690 | f             | f             | t        |   16 | 304706
//     333391 |         5 |              304690 | f             | f             | t        |   16 | 304713
//     333392 |         6 |              304690 | f             | f             | t        |   16 | 304721
//     333393 |         7 |              304690 | f             | f             | t        |   16 | 304730



    }

    public void getAclEntry(def aoi, def sid, def mask) {
        def ace = executeAclRequest("SELECT id FROM acl_entry WHERE acl_object_identity = ? AND mask = ? AND sid=?",[aoi,mask,sid])
        if (!ace) {
            def max = executeAclRequest("SELECT max(ace_order) FROM acl_entry WHERE acl_object_identity = ?",[aoi])
            if(max==null) {
                max=0
            } else {
                max = max +1
            }
            executeAclCUD("" +
                    "INSERT INTO acl_entry(id,ace_order,acl_object_identity,audit_failure,audit_success,granting,mask,sid) " +
                    "VALUES(nextval('hibernate_sequence'),?,?,false,false,true,?,?)",[max,aoi,mask,sid])

            ace = executeAclRequest("SELECT id FROM acl_entry WHERE acl_object_identity = ? AND mask = ? AND sid=?",[aoi,mask,sid])
        }
        ace
    }

    public def getAclObjectIdentity(domain, ac, sidCurrentUser) {
        def aoi = executeAclRequest("SELECT id FROM acl_object_identity WHERE object_id_identity = ?",[domain.id])
        if (!aoi) {
            //id=nextVal()
            executeAclCUD("" +
                    "INSERT INTO acl_object_identity(id,object_id_class,entries_inheriting,object_id_identity,owner_sid,parent_object) " +
                    "VALUES (nextval('hibernate_sequence'),?,true,?,?,null)",[ac, domain.id,sidCurrentUser])
            aoi = executeAclRequest("SELECT id FROM acl_object_identity WHERE object_id_identity = ?",[domain.id])
        }
        aoi
    }

    public def getAclSid(String username) {
        def sidCurrentUser = executeAclRequest("SELECT id FROM acl_sid WHERE sid = ?",[username])
        if (!sidCurrentUser) {
            executeAclCUD("INSERT INTO acl_sid(id,principal,sid) VALUES(nextval('hibernate_sequence'),true,?)",[username])
            sidCurrentUser = executeAclRequest("SELECT id FROM acl_sid WHERE sid = ?",[username])
        }
        sidCurrentUser
    }

    public def getAclClass(domain) {
        def ac = executeAclRequest("SELECT id FROM acl_class WHERE class = ?",[domain.class.name])
        if (!ac) {
            executeAclCUD("INSERT INTO acl_class(id,class) VALUES(nextval('hibernate_sequence'),?)",[domain.class.name])
            ac = executeAclRequest("SELECT id FROM acl_class WHERE class = ?",[domain.class.name])
        }
        ac
    }

    def executeAclRequest(String request,def params = []) {
        def id = null
        println request
        new Sql(dataSource).eachRow(request,params) {
            id = it[0]
        }
        return id
    }

    def executeAclCUD(String request,def params = []) {
        //println request
        new Sql(dataSource).execute(request,params)
    }


}



