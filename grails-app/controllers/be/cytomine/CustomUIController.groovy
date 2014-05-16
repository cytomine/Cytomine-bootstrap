package be.cytomine

import be.cytomine.api.RestController
import be.cytomine.ontology.Property
import be.cytomine.project.Project
import be.cytomine.security.SecRole
import grails.converters.JSON
import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION

/**
 * Custom UI allow to show/hide componenent from the javascript UI client.
 * E.g.: hide jobs tabs for guest user, ...
 * Global: main cytomine tab (dashboard, project, ontology,...); config for all projects
 * Project: project tab (dashboard, images, annotations,...); config inside the project
 */
class CustomUIController extends RestController {

    def currentRoleServiceProxy
    def grailsApplication
    def cytomineService

    def  projectService
    def propertyService
    def securityACLService

    static String CUSTOM_UI_PROJECT = "@CUSTOM_UI_PROJECT"

    /**
     * Retrieve de custom UI config GLOBAL
     */
    def retrieveGlobalUIConfig() {
        Set<SecRole> roles = currentRoleServiceProxy.findCurrentRole()
        println roles.collect{it.authority}
        def globalConfig = [:]

        println grailsApplication.config.cytomine.customUI.global
        println grailsApplication.config.cytomine.customUI.global.class

        grailsApplication.config.cytomine.customUI.global.each{
            boolean print
            def mandatoryRoles = it.value
            if(mandatoryRoles.contains("ALL")) {
                print = true
            } else {
                print = mandatoryRoles.find {roles.collect{it.authority}.contains(it)}
            }
            globalConfig[it.key] = print
        }
        responseSuccess(globalConfig)
    }

    /**
     * Retrieve de custom UI config PROJECT
     */
    def retrieveProjectUIConfig() {
        Project project = projectService.read(params.long('project'))
        Set<SecRole> roles = currentRoleServiceProxy.findCurrentRole()

        def configProject = §
        def result = [:]

        if(project) {
            boolean isProjectAdmin = projectService.listByAdmin(cytomineService.currentUser)
            List<Property> properties = Property.findAllByDomainIdentAndKey(project.id,CUSTOM_UI_PROJECT,[max: 1,sort:"created", order:"desc" ])

            if(!properties.isEmpty()) {
                configProject = JSON.parse(properties.first().value)

                configProject
                /**  "project-annotations-tab": {
 "USER_PROJECT": true,
 "ADMIN_PROJECT": true,
 "GUEST_PROJECT": true
 },
 "project-images-tab": {
 "USER_PROJECT": true,
 "ADMIN_PROJECT": true,
 "GUEST_PROJECT": true
 },
 "project-configuration-tab": {
 "USER_PROJECT": false,
 "ADMIN_PROJECT": true,
 "GUEST_PROJECT": false
 },
 "project-jobs-tab": {
 "USER_PROJECT": true,
 "ADMIN_PROJECT": true,
 "GUEST_PROJECT": false
 },
 "project-properties-tab": {
 "USER_PROJECT": true,
 "ADMIN_PROJECT": true,
 "GUEST_PROJECT": true
 }**/


            }

            configProject.each{
                println it.value
                result[it.key] = shouldBeShow(roles,isProjectAdmin,it.value)
            }
            responseSuccess(result)
        } else {
            responseNotFound("Project", params.project)
        }

    }

    //config = ["ADMIN_PROJECT":true,"USER_PROJECT":true,"GUEST_PROJECT":true]
    boolean shouldBeShow(Set<SecRole> roles, boolean isProjectAdmin, def config) {
        boolean mustBeShow = false
        if(isProjectAdmin) {
            mustBeShow = config["ADMIN_PROJECT"]
        } else if(roles.find{it.authority=="ROLE_USER"}) {
            mustBeShow = config["USER_PROJECT"]
        }else if(roles.find{it.authority=="ROLE_GUEST"}) {
            mustBeShow = config["GUEST_PROJECT"]
        } else {
            return true
        }
        return mustBeShow
    }





    def retrieveUIRoles() {
        def data = [:]
        data.main = [[authority:"ALL", name: "All"],[authority:"NONE", name: "Nobody"]]
        data.global = SecRole.list().collect{[authority:it.authority, name:it.authority.toLowerCase().replace("ROLE","").replace("_"," ")]}
        data.project = [[authority:"ADMIN_PROJECT", name: "project admin"],[authority:"USER_PROJECT", name: "project user"]]
        responseSuccess(data)
    }


    //7. Tester en rajoutant des panneaux dans avec un vieux json( par défaut mettre vert)

    //8. tester la sécrutié ACL POUR LA MODIF projet

    def addCustomUIForProject() {
        Project project = projectService.read(params.long('project'))
        def json = request.JSON

        if(project) {
            List<Property> properties = Property.findAllByDomainIdentAndKey(project.id,CUSTOM_UI_PROJECT,[max: 1,sort:"created", order:"desc" ])
            securityACLService.check(project,ADMINISTRATION)
            if(properties.isEmpty()) {
                Property property = new Property(key: CUSTOM_UI_PROJECT,value:json.toString())
                property.setDomain(project)
                def result = propertyService.add(JSON.parse(property.encodeAsJSON()))
                responseSuccess(JSON.parse(result.data.property.value))
            } else {
                def jsonEdit = JSON.parse(properties.first().encodeAsJSON())
                jsonEdit.value = json.toString()
                def result = propertyService.update(properties.first(),jsonEdit)
                responseSuccess(JSON.parse(result.data.property.value))
            }
        } else {
            responseNotFound("Project", params.project)
        }
    }

    def showCustomUIForProject() {
        Project project = projectService.read(params.long('project'))

        if(project) {
            List<Property> properties = Property.findAllByDomainIdentAndKey(project.id,CUSTOM_UI_PROJECT,[max: 1,sort:"created", order:"desc" ])
            if(properties.isEmpty()) {
                responseSuccess(grailsApplication.config.cytomine.customUI.project)
            } else {
                responseSuccess(JSON.parse(properties.first().value))
            }
        } else {
            responseNotFound("Project", params.project)
        }
    }

}
