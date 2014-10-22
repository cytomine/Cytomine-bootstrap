package be.cytomine

import be.cytomine.security.SecUser
import grails.converters.JSON
import grails.util.Holders
import groovy.sql.Sql
import groovy.util.logging.Log
import org.restapidoc.annotation.RestApiObjectField
import org.restapidoc.annotation.RestApiObjectFields
import org.springframework.security.acls.model.Permission

/**
 * CytomineDomain is the parent class for all domain.
 * It allow to give an id to each instance of a domain, to get a created date,...
 */
@Log
abstract class CytomineDomain  implements Comparable,Serializable{

    def springSecurityService
    def cytomineService
    def sequenceService
    def dataSource

    static def grailsApplication

    @RestApiObjectField(description = "The domain id", useForCreation = false)
    Long id

    @RestApiObjectField(description = "The date of the domain creation", useForCreation = false)
    Date created

    @RestApiObjectField(description = "The date of the domain modification", useForCreation = false)
    Date updated

    @RestApiObjectField(description = "When domain was removed from Cytomine", useForCreation = false)
    Date deleted

    @RestApiObjectFields(params=[
        @RestApiObjectField(apiFieldName = "class", description = "The full class name of the domain",allowedType = "string",useForCreation = false)
    ])
    static transients = []

    static mapping = {
        tablePerHierarchy false
        id generator: "assigned"
        sort "id"

    }

    static constraints = {
        created nullable: true
        updated nullable: true
        deleted nullable : true
    }

    public boolean checkDeleted() {
        return deleted!=null
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
     * Get the container domains for this domain (usefull for security)
     * @return Container of this domain
     */
    public CytomineDomain[] containers() {
        def container = container()
        if (container)
            return [container]
        else
            return []
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

    public static boolean isGrailsDomain(String fullName) {
        def domain = Holders.getGrailsApplication().getDomainClasses().find {
            it.fullName.equals(fullName)
        }
        return domain != null
    }

    static def getDataFromDomain(def domain) {
        def returnArray = [:]
        returnArray['class'] = domain?.class
        returnArray['id'] = domain?.id
        returnArray['created'] = domain?.created?.time?.toString()
        returnArray['updated'] = domain?.updated?.time?.toString()
        returnArray['deleted'] = domain?.deleted?.time?.toString()
        return returnArray
    }

    /**
     * Check if user has permission on the curret domain
     * @param permission Permission to check (READ,...)
     * @return true if user has this permission on current domain
     */
    boolean checkPermission(Permission permission, boolean isAdmin) {
        boolean right = hasACLPermission(permission) || isAdmin
        return right
    }

    /**
     * Check if user has ACL entry for this permission and this domain.
     * IT DOESN'T CHECK IF CURRENT USER IS ADMIN
     * @param permission Permission to check (READ,...)
     * @return true if user has this permission on current domain
     */
    boolean hasACLPermission(Permission permission) {
        try {
            return hasACLPermission(this,permission)
        } catch (Exception e) {}
        return false
    }

    boolean hasACLPermission(def domain,Permission permission) {
        def masks = getPermissionInACL(domain,cytomineService.getCurrentUser())
        return masks.max() >= permission.mask
    }


    List getPermissionInACL(def domain, def user = null) {
        try {
            String request = "SELECT mask FROM acl_object_identity aoi, acl_sid sid, acl_entry ae " +
            "WHERE aoi.object_id_identity = ${domain.id} " +
                    (user? "AND sid.sid = '${user.humanUsername()}' " : "") +
            "AND ae.acl_object_identity = aoi.id "+
            "AND ae.sid = sid.id "

            def masks = []
            def sql = new Sql(dataSource)
            sql.eachRow(request) {
                masks<<it[0]
            }
            sql.close()
            return masks

        } catch (Exception e) {
            log.error e.toString()
        }
        return []
    }


    int compareTo(obj) {
        created.compareTo(obj.created)
    }

    boolean canUpdateContent() {
        //by default, we allow a non-admin user to update domain content
        return true
    }

    String encodeAsJSON() {
        return (this as JSON).toString()
    }

    def get(String id) {
        if(id) {
            return get(Long.parseLong(id))
        } else {
            return null
        }
    }


    def get(org.codehaus.groovy.grails.web.json.JSONObject id) {
        println "GET JSON"

        if(id) {
            return get(Long.parseLong(id.toString()))
        } else {
            return null
        }
    }

    def get(org.codehaus.groovy.grails.web.json.JSONObject.Null id) {
        println "GET JSON2"
        return null
    }


    def read(String id) {
        if(id) {
            return read(Long.parseLong(id))
        } else {
            return null
        }
    }

}
