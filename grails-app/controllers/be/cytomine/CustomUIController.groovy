package be.cytomine

/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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


    def retrieveUIConfig() {
        log.info "retrieveUIConfig"
        Set<SecRole> roles = currentRoleServiceProxy.findCurrentRole(cytomineService.currentUser)
        Project project = projectService.read(params.long('project'))

        def config = [:]
        config.putAll(getGlobalConfig(roles))
        if(project) {
            config.putAll(getProjectConfig(roles, project))
        }
        responseSuccess(config)
    }

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

    public def getGlobalConfig(Set<SecRole> roles) {
        def globalConfig = [:]
        grailsApplication.config.cytomine.customUI.global.each {
            boolean print
            def mandatoryRoles = it.value
            if (mandatoryRoles.contains("ALL")) {
                print = true
            } else {
                print = mandatoryRoles.find { roles.collect { it.authority }.contains(it) }
            }
            globalConfig[it.key] = print
        }
        return globalConfig
    }

    public def getProjectConfig(Set<SecRole> roles, Project project) {
        def configProject = grailsApplication.config.cytomine.customUI.project
        boolean isProjectAdmin = projectService.listByAdmin(cytomineService.currentUser)
        List<Property> properties = Property.findAllByDomainIdentAndKey(project.id,CUSTOM_UI_PROJECT,[max: 1,sort:"created", order:"desc" ])
        def result = [:]

        configProject.each{
            result[it.key] = shouldBeShow(roles,isProjectAdmin,it.value)
        }

        //if a property is save, we override the default config
        if(!properties.isEmpty()) {
            configProject = JSON.parse(properties.first().value)
        }
        configProject.each{
            result[it.key] = shouldBeShow(roles,isProjectAdmin,it.value)
        }

        return result
    }

    boolean shouldBeShow(Set<SecRole> roles, boolean isProjectAdmin, def config) {
        if(currentRoleServiceProxy.isAdminByNow(cytomineService.currentUser))
            return true;

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

}
