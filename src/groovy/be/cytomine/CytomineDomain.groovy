package be.cytomine

import be.cytomine.project.Project
import org.codehaus.groovy.grails.plugins.springsecurity.acl.AclObjectIdentity
import org.codehaus.groovy.grails.plugins.springsecurity.acl.AclSid
import be.cytomine.security.SecUser
import org.codehaus.groovy.grails.plugins.springsecurity.acl.AclEntry
import static org.springframework.security.acls.domain.BasePermission.*
import org.codehaus.groovy.grails.commons.GrailsApplication

abstract class CytomineDomain {

    def cytomineService
    def sequenceService
    def jsonService
    def grailsApplication
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

    void checkAlreadyExist() {
        //do nothing ; if override by a sub-class, should throw AlreadyExist exception
    }

    /**
     * Return domain project (annotation project, ...)
     * By default, a domain has no project.
     * You need to override getProject() in domain class
     * @return Domain project
     */
    public Project projectDomain() {
        return null;
    }

    def getCallBack() {
        return null
    }

    boolean hasPermission(String permission) {
        try {
            //println "hasPermission($permission)"
            return hasPermission(this,permission)
        } catch (Exception e) {e.printStackTrace()}
        return false
    }

    boolean hasPermission(Long id,String className, String permission) {
        try {
            //println "hasPermission($id,$className,$permission)"
            def obj = grailsApplication.classLoader.loadClass(className).get(id)
            return hasPermission(obj,permission)
        } catch (Exception e) {
            log.error e.toString()
            e.printStackTrace()}
        return false
    }

    boolean hasPermission(def domain,String permissionStr) {
        try {
            //println "hasPermission($domain,$permissionStr)"
            SecUser currentUser = cytomineService.getCurrentUser()
            String username = currentUser.realUsername()
            int permission = -1
            if(permissionStr.equals("READ")) permission = READ.mask
            else if(permissionStr.equals("WRITE")) permission = WRITE.mask
            else if(permissionStr.equals("DELETE")) permission = DELETE.mask
            else if(permissionStr.equals("CREATE")) permission = CREATE.mask
            else if(permissionStr.equals("ADMIN")) permission = ADMINISTRATION.mask

            AclObjectIdentity aclObject = AclObjectIdentity.findByObjectId(domain.id)
            AclSid aclSid = AclSid.findBySid(username)
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

}
