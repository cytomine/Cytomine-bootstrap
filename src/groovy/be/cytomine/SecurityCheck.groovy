package be.cytomine

import be.cytomine.project.Project
import be.cytomine.Exception.ObjectNotFoundException
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.security.SecUser

/**
 * User: lrollus
 * Date: 17/01/13
 * GIGA-ULg
 * 
 */
class SecurityCheck {

    def cytomineService

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
}
