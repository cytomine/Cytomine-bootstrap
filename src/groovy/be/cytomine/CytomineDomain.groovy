package be.cytomine

import be.cytomine.project.Project
import be.cytomine.security.SecUser
import org.codehaus.groovy.grails.plugins.springsecurity.acl.AclEntry
import org.codehaus.groovy.grails.plugins.springsecurity.acl.AclObjectIdentity
import org.codehaus.groovy.grails.plugins.springsecurity.acl.AclSid

import static org.springframework.security.acls.domain.BasePermission.*
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.Exception.ServerException

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
            id = sequenceService.generateID(this)
        }
    }

  def beforeValidate() {
      if (!created) {
          created = new Date()
      }
      if (id == null) {
          id = sequenceService.generateID(this)
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
     * Return domain project (annotation project, image project...)
     * By default, a domain has no project.
     * You need to override getProject() in domain class
     * @return Domain project
     */
    public Project projectDomain() {
        return null;
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

    /**
     * This method check if current user has permission on the current domain
     * @param permission Type of permission (read, admin,...)
     * @return True if user has this permission on the current domain, otherwise false
     */
    boolean hasPermission(String permission) {
        try {
            return hasPermission(this,permission)
        } catch (Exception e) {e.printStackTrace()}
        return false
    }

    /**
     * This method check if current user has permission on the domain from className with this id
     * @param id  Domain id
     * @param className Domain class
     * @param permission Type of permission
     * @return  True if user has this permission on the specific domain, otherwise false
     */
    boolean hasPermission(Long id,String className, String permission) {
        try {
            def obj = grailsApplication.classLoader.loadClass(className).get(id)
            return hasPermission(obj,permission)
        } catch (Exception e) {
            log.error e.toString()
            e.printStackTrace()}
        return false
    }

    /**
     *  This method check if current user has permission on a domain
     * @param domain Domainto check
     * @param permissionStr Type of permission
     * @return  True if user has this permission on the specific domain, otherwise false
     */
    boolean hasPermission(def domain,String permissionStr) {
        try {
            SecUser currentUser = cytomineService.getCurrentUser()
            String usernameParentUser = currentUser.realUsername()
            int permission = -1
            if(permissionStr.equals("READ")) permission = READ.mask
            else if(permissionStr.equals("WRITE")) permission = WRITE.mask
            else if(permissionStr.equals("DELETE")) permission = DELETE.mask
            else if(permissionStr.equals("CREATE")) permission = CREATE.mask
            else if(permissionStr.equals("ADMIN")) permission = ADMINISTRATION.mask

            //TODO:: this function is call very often, make a direct SQL request instead 3 request

            AclObjectIdentity aclObject = AclObjectIdentity.findByObjectId(domain.id)
            AclSid aclSid = AclSid.findBySid(usernameParentUser)
            if(!aclObject) return false
            if(!aclSid) return false

            boolean hasPermission = false;
            List<AclEntry> acls = AclEntry.findAllByAclObjectIdentityAndSid(aclObject,aclSid)
            acls.each { acl ->
                if(acl.mask>=permission) {
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





















    static protected String getJSONAttrStr(def json, String attr) {
        println "get attr $attr"
       if(json[attr]!=null && !json[attr].toString().equals("null")) {
            return json[attr].toString()
       } else {
           return null
       }
    }



    static protected Date getJSONAttrDate(def json, String attr) {
        println "get attr $attr"
       if(json[attr]!=null && !json[attr].toString().equals("null")) {
            return new Date(Long.parseLong(json.created))
       } else {
           return null
       }
    }

    static protected def getJSONAttrDomain(def json, String attr, def domain, boolean mandatory) {
        getJSONAttrDomain(json,attr,domain,'id','Long',mandatory)
    }

    static protected def getJSONAttrDomain(def json, String attr, def domain,String column,String columnType, boolean mandatory) {
        println "get attr $attr"
       if(json[attr]!=null && !json[attr].toString().equals("null")) {
             def domainRead = domain.findWhere("$column":convertValue(json[attr].toString(),columnType))
             if(!domainRead)  {
                 throw new WrongArgumentException("$attr was not found with id:${json[attr]}")
             }
           return domainRead
       } else {
           if(mandatory) {
               throw new WrongArgumentException("$attr must be set! value=${json[attr]}")
           }
           return null
       }
    }

    static protected def convertValue(String value, String type) {
        if(type.equals("String")) {
            return value
        } else if(type.equals("Long")) {
            return Long.parseLong(value);
        }
        throw new ServerException("Type $type not supported! See cytominedomain class")
    }

    static protected Long getJSONAttrLong(def json, String attr, Long defaultValue) {
        println "get attr $attr"
       if(json[attr]!=null && !json[attr].toString().equals("null")) {
           try {
               return Long.parseLong(json[attr].toString())
           } catch(Exception e) {
               return defaultValue
           }
       } else {
           return defaultValue
       }
    }


    static protected Long getJSONAttrInteger(def json, String attr, Long defaultValue) {
        println "get attr $attr"
       if(json[attr]!=null && !json[attr].toString().equals("null")) {
           try {
               return Integer.parseInt(json[attr].toString())
           } catch(Exception e) {
               return defaultValue
           }
       } else {
           return defaultValue
       }
    }









}
