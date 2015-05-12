package be.cytomine.security

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

import be.cytomine.project.Project
import be.cytomine.social.LastConnection
import be.cytomine.social.PersistentConnection
import grails.converters.JSON
import grails.converters.XML
import grails.plugin.springsecurity.annotation.Secured

//import grails.plugin.springsecurity.annotation.Secured
import javax.imageio.ImageIO

class ServerController {

    def imageRetrievalService

    def springSecurityService
    def grailsApplication
    def dataSource


    @Secured(['IS_AUTHENTICATED_REMEMBERED'])
    def ping () {
        def jsonContent = request.JSON
        def data = [:]
        data.alive = true
        data.authenticated = springSecurityService.isLoggedIn()
        data.version = grailsApplication.metadata['app.version']
        data.serverURL = grailsApplication.config.grails.serverURL
        if (data.authenticated)  {
            data.user = springSecurityService.principal.id
            def idProject = null
            def idUser = data.user
            if(!jsonContent.project.toString().equals("null")) {
                idProject = Long.parseLong(jsonContent.project+"")
            }
            addLastConnection(idUser,idProject)
        }
        withFormat {
            json { render data as JSON }
            xml { render data as XML}
        }
    }

    def status() {
        def data = [:]
        data.alive = true
        data.version = grailsApplication.metadata['app.version']
        data.serverURL = grailsApplication.config.grails.serverURL
        withFormat {
            json { render data as JSON }
            xml { render data as XML}
        }
    }

    def addLastConnection(def idUser, def idProject) {
        LastConnection connection = new LastConnection()
        connection.user = SecUser.read(idUser)
        connection.project = Project.read(idProject)
        connection.date = new Date()
        connection.insert(flush:true) //don't use save (stateless collection)
        PersistentConnection connectionPersist = new PersistentConnection()
        connectionPersist.user = SecUser.read(idUser)
        connectionPersist.project = Project.read(idProject)
        connectionPersist.date = new Date()
        connectionPersist.insert(flush:true) //don't use save (stateless collection)
    }



    @Secured(['ROLE_USER','ROLE_ADMIN','ROLE_SUPER_ADMIN'])
    def retrieval() {
        def response = imageRetrievalService.indexImageAsync(
                ImageIO.read(new File("/home/lrollus/git/CBIRestAPI/testdata/images/crop5.jpg")),
                new Date().getTime()+"",
                "toto",
                new HashMap<>()
        )
        println response
        render response
    }

    @Secured(['ROLE_USER','ROLE_ADMIN','ROLE_SUPER_ADMIN'])
    def missing() {
        return imageRetrievalService.indexMissingAnnotation()
    }

}
