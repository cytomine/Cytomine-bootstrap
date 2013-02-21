package be.cytomine

import be.cytomine.Exception.ForbiddenException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.Term
import be.cytomine.processing.Job
import be.cytomine.processing.Software
import be.cytomine.project.Project
import be.cytomine.security.UserGroup
import be.cytomine.test.Infos

/**
 * User: lrollus
 * Date: 17/01/13
 * GIGA-ULg
 *
 * Security object usefull to check right access for a domain
 */
class SecurityCheck {

    def domain

    public SecurityCheck() {

    }

    public SecurityCheck(def domain) {
        this.domain = domain
    }

    /**
     * Check if current user has read access on the cytomineDomain object
     */
    public static void checkReadAuthorization(CytomineDomain cytomineDomain) {
        if(cytomineDomain && !cytomineDomain.checkReadPermission()) {
            throw new ForbiddenException("You don't have the right to read this resource!")
        }
    }

    /**
     * Check if current user has read right in the project id
     */
    public static boolean checkProjectAccess(def id) {
        println "checkProjectAccess="+id
        def project = Project.read(id)
        println "project="+project
        if(!project) {
            throw new ObjectNotFoundException("Project $id was not found! Unable to process project auth checking")
        }
        println "permission="+project.checkReadPermission()
        println "2**************"
        Infos.printRight(project)
        println "2**************"
        return project.checkReadPermission()
    }

    /**
     * Check if current user has read right in the term id
     */
    public static boolean checkTermAccess(def id) {
        def term = Term.read(id)
        if(!term) {
            throw new ObjectNotFoundException("Term ${id} was not found! Unable to process term ontology auth checking")
        }
        return term.ontology.checkReadPermission()
    }

    /**
     * Check if current user has read right in the ontology id
     */
    public static boolean checkOntologyAccess(def id) {
        def ontology = Ontology.read(id)
        if(!ontology) {
            throw new ObjectNotFoundException("Ontology ${id} was not found! Unable to process ontology auth checking")
        }
        return ontology.checkReadPermission()
    }

    /**
     * Check if current user has read right in the job id
     */
    public static boolean checkJobAccess(def id) {
        def job = Job.read(id)
        if(!job) {
            throw new ObjectNotFoundException("Job $id was not found! Unable to process project auth checking")
        }
        return job.project.checkReadPermission()
    }

    /**
     * Check if user in argment is user creator
     */
    boolean checkCurrentUserCreator(def idPrincipal) {
         def creator = domain.userDomainCreator()
         return creator && creator.id==idPrincipal
    }

    /**
     * Check if currentUserId is memeber of the group set in constructor
     */
    boolean checkIfUserIsMemberGroup(def currentUserId) {
        def group = domain
        if(!group) {
            throw new ObjectNotFoundException("Group from domain ${domain} was not found! Unable to process group/user auth checking")
        }
        boolean isInside = false
        UserGroup.findAllByGroup(group).each {
            if(it.user.id==currentUserId) {
                isInside = true
                return true
            }
        }
        return isInside
    }

    /**
     * Check if current user has read right in the project of the domain (set in constructor)
     */
    boolean checkProjectAccess() {
        checkProjectPermission("READ")
    }

    /**
     * Check if current user has write right in the project of the domain (set in constructor)
     */
    boolean checkProjectWrite() {
        checkProjectPermission("WRITE")
    }

    /**
     * Check if current user has delete right in the project of the domain (set in constructor)
     */
    boolean checkProjectDelete() {
        checkProjectPermission("DELETE")
    }

    private boolean checkProjectPermission(String permission) {
        def project = domain.projectDomain()
        if(!project) {
            throw new ObjectNotFoundException("Project from domain ${domain} was not found! Unable to process project auth checking")
        }
        return project.checkPermission(permission)
    }

    /**
     * Check if current user has read right in the ontology of the domain (set in constructor)
     */
    boolean checkOntologyAccess() {
        checkOntologyPermission("READ")
    }

    /**
     * Check if current user has write right in the ontology of the domain (set in constructor)
     */
    boolean checkOntologyWrite() {
        checkOntologyPermission("WRITE")
    }

    /**
     * Check if current user has delete right in the ontology of the domain (set in constructor)
     */
    boolean checkOntologyDelete() {
        checkOntologyPermission("DELETE")
    }

    private boolean checkOntologyPermission(String permission) {
        def ontology = domain.ontologyDomain()
        if(!ontology) {
            throw new ObjectNotFoundException("Ontology from domain ${domain} was not found! Unable to process ontology auth checking")
        }
        return ontology.checkPermission(permission)
    }

    /**
     * Check if current user has read right in the ontology of the domain (set in constructor)
     */
    boolean checkStorageAccess() {
        checkStoragePermission("READ")
    }

    /**
     * Check if current user has write right in the ontology of the domain (set in constructor)
     */
    boolean checkStorageWrite() {
        checkStoragePermission("WRITE")
    }

    /**
     * Check if current user has delete right in the ontology of the domain (set in constructor)
     */
    boolean checkStorageDelete() {
        checkStoragePermission("DELETE")
    }

    private boolean checkStoragePermission(String permission) {
        def storage = domain.storageDomain()
        if(!storage) {
            throw new ObjectNotFoundException("Storage from domain ${domain} was not found! Unable to process storage auth checking")
        }
        return storage.checkPermission(permission)
    }
    

    /**
     * Check if current user has read right in the software of the domain
     */
    boolean checkSoftwareAccess(def id) {
        def software = Software.read(id)
        if(!software) {
            throw new ObjectNotFoundException("Software $id was not found! Unable to process software auth checking")
        }
        return software.checkReadPermission()
    }

    /**
     * Check if current user has read right in the software of the domain (set in constructor)
     */
    boolean checkSoftwareAccess() {
        checkSoftwarePermission("READ")
    }

    /**
     * Check if current user has write right in the software of the domain (set in constructor)
     */
    boolean checkSoftwareWrite() {
        checkSoftwarePermission("WRITE")
    }

    /**
     * Check if current user has delete right in the software of the domain (set in constructor)
     */
    boolean checkSoftwareDelete() {
        checkSoftwarePermission("DELETE")
    }

    private boolean checkSoftwarePermission(String permission) {
        def software = domain.softwareDomain()
        if(!software) {
            throw new ObjectNotFoundException("Software from domain ${domain} was not found! Unable to process software auth checking")
        }
        return software.checkPermission(permission)
    }
}
