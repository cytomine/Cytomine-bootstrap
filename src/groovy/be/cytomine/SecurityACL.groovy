package be.cytomine

import be.cytomine.Exception.ForbiddenException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.image.server.Storage
import be.cytomine.ontology.Ontology
import be.cytomine.processing.Software
import be.cytomine.project.Project
import be.cytomine.security.Group
import be.cytomine.security.SecUser
import be.cytomine.security.UserGroup
import org.springframework.security.acls.model.Permission

/**
 * User: lrollus
 * Date: 17/01/13
 * GIGA-ULg
 *
 * Security object usefull to check right access for a domain
 */
class SecurityACL {

    static void check(def id, Class classObj, Permission permission) {
        check(id,classObj.getName(),permission)
    }

    static void check(def id, Class classObj, String method, Permission permission) {
        def simpleObject =  Class.forName(classObj.getName(), false, Thread.currentThread().contextClassLoader).read(id)
       if (simpleObject) {
           def containerObject = simpleObject."$method"()
           check(containerObject,permission)
       } else {
           throw new ObjectNotFoundException("ACL error: ${classObj.getName()} with id ${id} was not found! Unable to process auth checking")
       }


    }

    static void check(def id, String className, Permission permission) {
        println "id=$id"
        println "className=${className}"
        try {
            def domain = Class.forName(className, false, Thread.currentThread().contextClassLoader).read(id)
            if (domain) {
                check(domain,permission)
            } else {
                throw new ObjectNotFoundException("ACL error: ${className} with id ${id} was not found! Unable to process auth checking")
            }
        } catch(IllegalArgumentException ex) {
            throw new ObjectNotFoundException("ACL error: ${className} with id ${id} was not found! Unable to process auth checking")
        }

    }

    static void check(CytomineDomain domain, Permission permission) {
          if (domain) {
              if (!domain.checkPermission(permission)) {
                  throw new ForbiddenException("You don't have the right to read this resource! ${domain.class.getName()}")
              }
          } else {
              throw new ObjectNotFoundException("ACL error: domain is null! Unable to process project auth checking")
          }

      }

    static public List<Storage> getStorageList(SecUser user) {
        //faster method
        if (user.admin) return Storage.list()
        else {
            return Storage.executeQuery(
                    "select distinct storage "+
                    "from AclObjectIdentity as aclObjectId, AclEntry as aclEntry, AclSid as aclSid, SecUser as secUser, Storage as storage "+
                    "where aclObjectId.objectId = project.id " +
                    "and aclEntry.aclObjectIdentity = aclObjectId.id "+
                    "and aclEntry.sid = aclSid.id and aclSid.sid like '"+user.username+"'")
        }
    }

    static public List<Ontology> getOntologyList(SecUser user) {
        //faster method
        if (user.admin) return Ontology.list()
        else {
            return Ontology.executeQuery(
                    "select distinct ontology "+
                    "from AclObjectIdentity as aclObjectId, AclEntry as aclEntry, AclSid as aclSid, SecUser as secUser, Ontology as ontology "+
                    "where aclObjectId.objectId = ontology.id " +
                    "and aclEntry.aclObjectIdentity = aclObjectId.id "+
                    "and aclEntry.sid = aclSid.id and aclSid.sid like '"+user.username+"'")
        }
    }

    static public List<Project> getProjectList(SecUser user, Ontology ontology = null, Software software = null) {
        //faster method
        if (user.admin) {
            return ontology ? Project.findAllByOntology(ontology) : Project.list()
        }
        else {
            return Project.executeQuery(
                    "select distinct project "+
                    "from AclObjectIdentity as aclObjectId, AclEntry as aclEntry, AclSid as aclSid, SecUser as secUser, Project as project, SoftwareProject as softwareProject "+
                    "where aclObjectId.objectId = project.id " +
                    "and aclEntry.aclObjectIdentity = aclObjectId.id "+
                    (ontology? "and project.ontology.id = ${ontology.id} " : " ") +
                    (software? " and project.id = softwareProject.project.id and softwareProject.software.id = ${software.id} " : " ") +
                    "and aclEntry.sid = aclSid.id and aclSid.sid like '"+user.username+"'")
        }
    }



    static public def checkAdmin(SecUser user) {
       if (!user.admin) {
           throw new ForbiddenException("You don't have the right to read this resource! You must be admin!")
       }
    }

    static public def checkUser(SecUser user) {
       if (!user.admin && !user.getAuthorities().collect{it.authority}.contains("ROLE_USER")) {
           throw new ForbiddenException("You don't have the right to read this resource! You must be user!")
       }
    }

    static public def checkIsSameUser(SecUser user,SecUser currentUser) {
        if (!currentUser.admin && (user.id!=currentUser.id)) {
            throw new ForbiddenException("You don't have the right to read this resource! You must be the same user!")
        }
    }

    static public def checkIsCreator(CytomineDomain domain,SecUser currentUser) {
        if (!currentUser.admin && (currentUser.id!=domain.userDomainCreator().id)) {
            throw new ForbiddenException("You don't have the right to read this resource! You must be the same user!")
        }
    }

    static public def checkIsNotSameUser(SecUser user,SecUser currentUser) {
        if ((currentUser.id==user.id)) {
            throw new ForbiddenException("You cannot do this action with your own profil!")
        }
    }

    /**
     * Check if currentUserId is memeber of the group set in constructor
     */
    static public def checkIfUserIsMemberGroup(SecUser user, Group group) {
        if(!group) {
            throw new ObjectNotFoundException("Group from domain ${group} was not found! Unable to process group/user auth checking")
        }
        boolean isInside = false
        UserGroup.findAllByGroup(group).each {
            if(it.user.id==user.id) {
                isInside = true
                return true
            }
        }
        if (!isInside && !user.isAdmin())
            throw new ForbiddenException("User must be in this group!")
    }

}
