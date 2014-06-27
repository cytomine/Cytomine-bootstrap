package be.cytomine.utils.security

import be.cytomine.CytomineDomain
import be.cytomine.Exception.ForbiddenException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.image.server.Storage
import be.cytomine.ontology.Ontology
import be.cytomine.processing.Software
import be.cytomine.processing.SoftwareProject
import be.cytomine.project.Project
import be.cytomine.security.Group
import be.cytomine.security.SecRole
import be.cytomine.security.SecUser
import be.cytomine.security.UserGroup
import org.springframework.security.acls.model.Permission

import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION
import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION
import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION

class SecurityACLService {

    def cytomineService
    static transactional = false
    def currentRoleServiceProxy

    void check(def id, Class classObj, Permission permission) {
        check(id,classObj.getName(),permission)
    }

    void check(def id, Class classObj, String method, Permission permission) {
        check(id, classObj.getName(), method, permission)
    }

    void checkAtLeastOne(CytomineDomain domain, Permission permission) {
        checkAtLeastOne(domain.id,domain.class.name,"containers",permission)
    }

    void checkAtLeastOne(def id, String className, String method, Permission permission) {
        def simpleObject =  Class.forName(className, false, Thread.currentThread().contextClassLoader).read(id)
        if (simpleObject) {
            def containerObjects = simpleObject."$method"()
            log.info "containerObjects=${containerObjects}"
            def atLeastOne = containerObjects.find {
                log.info "checkPermission=${permission} => storage ${it.id}"
                it.checkPermission(permission,currentRoleServiceProxy.isAdminByNow(cytomineService.currentUser))
            }
            log.info "atLeastOne=${atLeastOne}"
            if (!atLeastOne) throw new ForbiddenException("You don't have the right to read or modity this resource! ${className} ${id}")

        } else {
            throw new ObjectNotFoundException("ACL error: ${className} with id ${id} was not found! Unable to process auth checking")
        }
    }

    void checkAll(CytomineDomain domain, Permission permission) {
        checkAll(domain.id,domain.class.name,"containers",permission)
    }

    void checkAll(def id, String className, String method, Permission permission) {
        def simpleObject =  Class.forName(className, false, Thread.currentThread().contextClassLoader).read(id)
        if (simpleObject) {
            def containerObjects = simpleObject."$method"()
            def atLeastOne = containerObjects.find {
                !it.checkPermission(permission,currentRoleServiceProxy.isAdminByNow(cytomineService.currentUser))
            }
            if (atLeastOne) throw new ForbiddenException("You don't have the right to read or modity this resource! ${className} ${id}")
        } else {
            throw new ObjectNotFoundException("ACL error: ${className} with id ${id} was not found! Unable to process auth checking")
        }
    }

    void check(def id, String className, String method, Permission permission) {
        log.info "check:" + id + " className=$className method=$method"
        def simpleObject =  Class.forName(className, false, Thread.currentThread().contextClassLoader).read(id)
        if (simpleObject) {
            def containerObject = simpleObject."$method"()
            check(containerObject,permission)
        } else {
            throw new ObjectNotFoundException("ACL error: ${className} with id ${id} was not found! Unable to process auth checking")
        }
    }

    void check(def id, String className, Permission permission) {
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

    void check(CytomineDomain domain, Permission permission) {
        if (domain) {
            if (!domain.container().checkPermission(permission,currentRoleServiceProxy.isAdminByNow(cytomineService.currentUser))) {
                throw new ForbiddenException("You don't have the right to read or modity this resource! ${domain.class.getName()} ${domain.id}")
            }

        } else {
            throw new ObjectNotFoundException("ACL error: domain is null! Unable to process project auth checking")
        }

    }


    void checkReadOnly(def id, Class className) {
        checkReadOnly(id,className.getName())
    }


    void checkReadOnly(def id, String className) {
        try {
            def domain = Class.forName(className, false, Thread.currentThread().contextClassLoader).read(id)
            if (domain) {
                checkReadOnly(domain)
            } else {
                throw new ObjectNotFoundException("ACL error: ${className} with id ${id} was not found! Unable to process auth checking")
            }
        } catch(IllegalArgumentException ex) {
            throw new ObjectNotFoundException("ACL error: ${className} with id ${id} was not found! Unable to process auth checking")
        }

    }


    //check if the container (e.g. Project) is not in readonly. If in readonly, only admins can edit this.
    void checkReadOnly(CytomineDomain domain) {
        if (domain) {
            boolean readOnly = !domain.container().canUpdateContent()
            boolean containerAdmin = domain.container().hasACLPermission(domain.container(),ADMINISTRATION)
            if(readOnly && !containerAdmin) {
                throw new ForbiddenException("The project for this data is in readonly mode! You must be project admin to add, edit or delete this resource in a readonly project.")
            }

        } else {
            throw new ObjectNotFoundException("ACL error: domain is null! Unable to process project auth checking")
        }
    }

    public List<Storage> getStorageList(SecUser user) {
        //faster method
        if (currentRoleServiceProxy.isAdminByNow(user)) return Storage.list()
        else {
            return Storage.executeQuery(
                    "select distinct storage "+
                            "from AclObjectIdentity as aclObjectId, AclEntry as aclEntry, AclSid as aclSid,  Storage as storage "+
                            "where aclObjectId.objectId = storage.id " +
                            "and aclEntry.aclObjectIdentity = aclObjectId.id "+
                            "and aclEntry.sid = aclSid.id and aclSid.sid like '"+user.username+"'")
        }
    }

    public List<Ontology> getOntologyList(SecUser user) {
        //faster method
        if (currentRoleServiceProxy.isAdminByNow(user)) return Ontology.list()
        else {
            return Ontology.executeQuery(
                    "select distinct ontology "+
                            "from AclObjectIdentity as aclObjectId, AclEntry as aclEntry, AclSid as aclSid, Ontology as ontology "+
                            "where aclObjectId.objectId = ontology.id " +
                            "and aclEntry.aclObjectIdentity = aclObjectId.id "+
                            "and aclEntry.sid = aclSid.id and aclSid.sid like '"+user.username+"'")
        }
    }

    public List<Project> getProjectList(SecUser user) {
        //faster method
        if (currentRoleServiceProxy.isAdminByNow(user)) {
            Project.findAllByDeletedIsNull()
        }
        else {
            return Project.executeQuery(
                    "select distinct project "+
                            "from AclObjectIdentity as aclObjectId, AclEntry as aclEntry, AclSid as aclSid, Project as project "+
                            "where aclObjectId.objectId = project.id " +
                            "and aclEntry.aclObjectIdentity = aclObjectId.id "+
                            "and aclEntry.sid = aclSid.id and aclSid.sid like '"+user.username+"' and project.deleted is null")
        }
    }

    public List<Project> getProjectList(SecUser user, Ontology ontology) {
        //faster method
        if (currentRoleServiceProxy.isAdminByNow(user)) {
            def projects = Project.findAllByOntologyAndDeletedIsNull(ontology)
            return projects
        }
        else {
            return Project.executeQuery(
                    "select distinct project "+
                            "from AclObjectIdentity as aclObjectId, AclEntry as aclEntry, AclSid as aclSid, Project as project "+
                            "where aclObjectId.objectId = project.id " +
                            "and aclEntry.aclObjectIdentity = aclObjectId.id "+
                            (ontology? "and project.ontology.id = ${ontology.id} " : " ") +
                            "and aclEntry.sid = aclSid.id and aclSid.sid like '"+user.username+"' and project.deleted is null")
        }
    }

    public List<Project> getProjectList(SecUser user, Software software) {
        //faster method
        if (currentRoleServiceProxy.isAdminByNow(user)) {
            SoftwareProject.findAllBySoftware(software).collect{it.project}.findAll{!it.checkDeleted()}
        }
        else {
            return Project.executeQuery(
                    "select distinct project "+
                            "from AclObjectIdentity as aclObjectId, AclEntry as aclEntry, AclSid as aclSid, Project as project, SoftwareProject as softwareProject "+
                            "where aclObjectId.objectId = project.id " +
                            "and aclEntry.aclObjectIdentity = aclObjectId.id "+
                            (software? " and project.id = softwareProject.project.id and softwareProject.software.id = ${software.id} " : " ") +
                            "and aclEntry.sid = aclSid.id and aclSid.sid like '"+user.username+"' and project.deleted is null")
        }
    }



    public def checkAdmin(SecUser user) {
        if (!currentRoleServiceProxy.isAdminByNow(user)) {
            throw new ForbiddenException("You don't have the right to read this resource! You must be admin!")
        }
    }

    public def checkGuest(SecUser user) {
        if (!currentRoleServiceProxy.isAdminByNow(user) && !currentRoleServiceProxy.isUserByNow(user) && !currentRoleServiceProxy.isGuestByNow(user)) {
            throw new ForbiddenException("You don't have the right to read this resource! You must be user!")
        }
    }

    public def checkUser(SecUser user) {
        if (!currentRoleServiceProxy.isAdminByNow(user) && !currentRoleServiceProxy.isUserByNow(user)) {
            throw new ForbiddenException("You don't have the right to read this resource! You must be user!")
        }
    }

    public def checkIsSameUser(SecUser user,SecUser currentUser) {
        if (!currentRoleServiceProxy.isAdminByNow(currentUser) && (user.id!=currentUser.id)) {
            throw new ForbiddenException("You don't have the right to read this resource! You must be the same user!")
        }
    }

    public def checkIsAdminContainer(CytomineDomain domain,SecUser currentUser) {
        if (domain) {
            if (!domain.container().checkPermission(ADMINISTRATION,currentRoleServiceProxy.isAdminByNow(cytomineService.currentUser))) {
                throw new ForbiddenException("You don't have the right to do this. You must be the creator or the container admin")
            }
        } else {
            throw new ObjectNotFoundException("ACL error: domain is null! Unable to process project auth checking")
        }

    }

    public def checkIsSameUserOrAdminContainer(CytomineDomain domain,SecUser user,SecUser currentUser) {
        boolean isNotSameUser = (!currentRoleServiceProxy.isAdminByNow(currentUser) && (user.id!=currentUser.id))
        if (isNotSameUser) {
            if (domain) {
                if (!domain.container().checkPermission(ADMINISTRATION,currentRoleServiceProxy.isAdminByNow(cytomineService.currentUser))) {
                    throw new ForbiddenException("You don't have the right to do this. You must be the creator or the container admin")
                }
            } else {
                throw new ObjectNotFoundException("ACL error: domain is null! Unable to process project auth checking")
            }
        }

    }

    public def checkIsCreator(CytomineDomain domain,SecUser currentUser) {
        if (!currentRoleServiceProxy.isAdminByNow(currentUser) && (currentUser.id!=domain.userDomainCreator().id)) {
            throw new ForbiddenException("You don't have the right to read this resource! You must be the same user!")
        }
    }

    public def checkIsNotSameUser(SecUser user,SecUser currentUser) {
        if ((currentUser.id==user.id)) {
            throw new ForbiddenException("You cannot do this action with your own profil!")
        }
    }

    /**
     * Check if currentUserId is memeber of the group set in constructor
     */
    public def checkIfUserIsMemberGroup(SecUser user, Group group) {
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
        if (!isInside && !currentRoleServiceProxy.isAdminByNow(user))
            throw new ForbiddenException("User must be in this group!")
    }


}
