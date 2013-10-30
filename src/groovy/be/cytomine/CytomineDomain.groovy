package be.cytomine

import be.cytomine.security.SecUser
import groovy.sql.Sql
import org.springframework.security.acls.model.Permission

import static org.springframework.security.acls.domain.BasePermission.*

/**
 * CytomineDomain is the parent class for all domain.
 * It allow to give an id to each instance of a domain, to get a created date,...
 */
abstract class CytomineDomain  implements Comparable{

    def springSecurityService
    def cytomineService
    def sequenceService

    static def grailsApplication
    Long id
    Date created
    Date updated

    static mapping = {
        tablePerHierarchy false
        id generator: "assigned"
        sort "id"
    }

    static constraints = {
        created nullable: true
        updated nullable: true
    }

    public beforeInsert() {
        if (!created) {
            created = new Date()
        }
        if (id == null) {
            id = sequenceService.generateID()
        }
    }

  def beforeValidate() {
      if (!created) {
          created = new Date()
      }
      if (id == null) {
          id = sequenceService.generateID()
      }
  }

    public beforeUpdate() {
        updated = new Date()
    }

    /**
     * This function check if a domain already exist (e.g. project with same name).
     * A domain that must be unique should rewrite it and throw AlreadyExistException
     */
    void checkAlreadyExist() {
        //do nothing ; if override by a sub-class, should throw AlreadyExist exception
    }

    /**
     * Return domain user (annotation user, image user...)
     * By default, a domain has no user.
     * You need to override userDomainCreator() in domain class
     * @return Domain user
     */
    public SecUser userDomainCreator() {
        return null
    }

    /**
     * Get the container domain for this domain (usefull for security)
     * @return Container of this domain
     */
    public CytomineDomain container() {
        return null
    }

    /**
     * Build callback data for a domain (by default null)
     * Callback are metadata used by client
     * You need to override getCallBack() in domain class
     * @return Callback data
     */
    def getCallBack() {
        return null
    }


    boolean hasPermission(Permission permission) {
        try {
            return hasPermission(this,permission)
        } catch (Exception e) {e.printStackTrace()}
        return false
    }

    boolean checkPermission(Permission permission) {
        boolean right = hasPermission(permission) || cytomineService.currentUser.admin
        return right
    }

    /**
     *  This method check if current user has permission on a domain
     * @param domain Domainto check
     * @param permissionStr Type of permission
     * @return  True if user has this permission on the specific domain, otherwise false
     */
    boolean hasPermission(def domain,String permissionStr) {
        try {
            Permission permission = null;
            if(permissionStr.equals("READ")) permission = READ
            else if(permissionStr.equals("WRITE")) permission = WRITE
            else if(permissionStr.equals("DELETE")) permission = DELETE
            else if(permissionStr.equals("CREATE")) permission = CREATE
            else if(permissionStr.equals("ADMIN")) permission = ADMINISTRATION
            hasPermission(domain,permission)

        } catch (Exception e) {
            log.error e.toString()
            e.printStackTrace()
        }
        return false
    }

    def dataSource
    boolean hasPermission(def domain,Permission permission) {
        println "hasPermission"
        try {
            def masks = getPermission(domain,cytomineService.getCurrentUser())

            return masks.max() >= permission.mask

        } catch (Exception e) {
            log.error e.toString()
            e.printStackTrace()
        }
        return false
    }

    List getPermission(def domain, def user) {
        try {
            println "getPermission"
            String request = "SELECT mask FROM acl_object_identity aoi, acl_sid sid, acl_entry ae " +
            "WHERE aoi.object_id_identity = ${domain.id} " +
            "AND sid.sid = '${user.humanUsername()}' " +
            "AND ae.acl_object_identity = aoi.id "+
            "AND ae.sid = sid.id "
            println "getPermission ok"
            def masks = []
            new Sql(dataSource).eachRow(request) {
                masks<<it[0]
            }
            println "getPermission okok"
            return masks

        } catch (Exception e) {
            log.error e.toString()
            e.printStackTrace()
        }
        return []
    }


    int compareTo(obj) {
        created.compareTo(obj.created)
    }

}
