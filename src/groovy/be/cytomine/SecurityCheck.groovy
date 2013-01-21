package be.cytomine

import be.cytomine.project.Project
import be.cytomine.Exception.ObjectNotFoundException
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.security.SecUser
import be.cytomine.ontology.Ontology
import org.springframework.security.access.prepost.PreAuthorize
import be.cytomine.Exception.ForbiddenException
import be.cytomine.ontology.Term

/**
 * User: lrollus
 * Date: 17/01/13
 * GIGA-ULg
 * 
 */
class SecurityCheck {

    def cytomineService
    def securityService

    def domain

    public SecurityCheck() {

    }

    public SecurityCheck(def domain) {
        this.domain = domain
    }


    boolean checkCurrentUserCreator(def idPrincipal) {
         def creator = domain.userDomain()
        return creator && creator.id==idPrincipal
    }

    boolean checkProjectAccess(def id) {
        def project = Project.read(id)
        if(!project) {
            throw new ObjectNotFoundException("Project $id was not found! Unable to process project auth checking")
        }
        return project.checkReadPermission()
    }

    boolean checkProjectAccess() {
        def project = domain.projectDomain()
        if(!project) {
            throw new ObjectNotFoundException("Project from domain ${domain} was not found! Unable to process project auth checking")
        }
        return project.checkReadPermission()
    }

    boolean checkProjectWrite() {
        def project = domain.projectDomain()
        if(!project) {
            throw new ObjectNotFoundException("Project from domain ${domain} was not found! Unable to process project auth checking")
        }
        return project.checkWritePermission()
    }

    boolean checkProjectDelete() {
        println "checkProjectDelete"
        def project = domain.projectDomain()
        println "project=$project"
        if(!project) {
            throw new ObjectNotFoundException("Project from domain ${domain} was not found! Unable to process project auth checking")
        }
        println "checkDeletePermission"
        return project.checkDeletePermission()
    }

    boolean checkTermAccess(def id) {
        def term = Term.read(id)
        if(!term) {
            throw new ObjectNotFoundException("Term ${id} was not found! Unable to process term ontology auth checking")
        }
        return term.ontology.checkReadPermission()
    }
    
    boolean checkOntologyAccess() {
        def ontology = domain.ontologyDomain()
        if(!ontology) {
            throw new ObjectNotFoundException("Ontology from domain ${domain} was not found! Unable to process ontology auth checking")
        }
        return ontology.checkReadPermission()
    }

    boolean checkOntologyAccess(def id) {
        def ontology = Ontology.read(id)
        if(!ontology) {
            throw new ObjectNotFoundException("Ontology from domain ${domain} was not found! Unable to process ontology auth checking")
        }
        return ontology.checkReadPermission()
    }

    boolean checkOntologyWrite() {
        def ontology = domain.ontologyDomain()
        if(!ontology) {
            throw new ObjectNotFoundException("Ontology from domain ${domain} was not found! Unable to process ontology auth checking")
        }
        return ontology.checkWritePermission()
    }

    boolean checkOntologyDelete() {
        println "checkOntologyDelete"
        def ontology = domain.ontologyDomain()
        println "ontology=$ontology"
        if(!ontology) {
            throw new ObjectNotFoundException("Ontology from domain ${domain} was not found! Unable to process ontology auth checking")
        }
        println "checkDeletePermission"
        return ontology.checkDeletePermission()
    }

    static void checkReadAuthorization(CytomineDomain cytomineDomain) {
        if(cytomineDomain && !cytomineDomain.checkReadPermission()) {
            throw new ForbiddenException("You don't have the right to read this resource!")
        }
    }
    
    
    
    
    
    
}
