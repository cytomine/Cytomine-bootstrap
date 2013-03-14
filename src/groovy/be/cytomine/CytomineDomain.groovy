package be.cytomine

import be.cytomine.image.server.Storage
import be.cytomine.ontology.Ontology
import be.cytomine.processing.Software
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import org.codehaus.groovy.grails.plugins.springsecurity.acl.AclEntry
import org.codehaus.groovy.grails.plugins.springsecurity.acl.AclObjectIdentity
import org.codehaus.groovy.grails.plugins.springsecurity.acl.AclSid
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
        return hasPermission(permission) || cytomineService.currentUser.admin
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

    boolean hasPermission(def domain,Permission permission) {
        try {
            SecUser currentUser = cytomineService.getCurrentUser()
            String usernameParentUser = currentUser.humanUsername()
            int mask = permission.mask

            //TODO:: this function is call very often, make a direct SQL request instead 3 request

            AclObjectIdentity aclObject = AclObjectIdentity.findByObjectId(domain.id)
            AclSid aclSid = AclSid.findBySid(usernameParentUser)
            if(!aclObject) return false
            if(!aclSid) return false

            boolean hasPermission = false;
            List<AclEntry> acls = AclEntry.findAllByAclObjectIdentityAndSid(aclObject,aclSid)
            acls.each { acl ->
                if(acl.mask>=mask) {
                    hasPermission=true
                }
            }
            return hasPermission

        } catch (Exception e) {
            log.error e.toString()
            e.printStackTrace()
        }
        return false
    }

    int compareTo(obj) {
        created.compareTo(obj.created)
    }
}
