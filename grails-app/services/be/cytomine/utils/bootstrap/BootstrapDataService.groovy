package be.cytomine.utils.bootstrap

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

import be.cytomine.image.server.ImageServer
import be.cytomine.processing.*
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.test.Infos
import groovy.sql.Sql
import org.apache.commons.lang.RandomStringUtils

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 13/03/13
 * Time: 11:30
 */
class BootstrapDataService {

    def grailsApplication
    def bootstrapUtilsService
    def dataSource
    def amqpQueueConfigService

    def initData() {

        recreateTableFromNotDomainClass()
        amqpQueueConfigService.initAmqpQueueConfigDefaultValues()

        def IIPImageServer = [className : 'IIPResolver', name : 'IIP', service : '/image/tile', url : grailsApplication.config.grails.imageServerURL, available : true]

        bootstrapUtilsService.createImageServers([IIPImageServer])
        def IIPMimeSamples = [
                [extension : 'mrxs', mimeType : 'openslide/mrxs'],
                [extension : 'vms', mimeType : 'openslide/vms'],
                [extension : 'tif', mimeType : 'openslide/ventana'],					
                [extension : 'tif', mimeType : 'image/tif'],
                [extension : 'tiff', mimeType : 'image/tiff'],
                [extension : 'tif', mimeType : 'image/pyrtiff'],
                [extension : 'svs', mimeType : 'openslide/svs'],
                [extension : 'jp2', mimeType : 'image/jp2'],
                [extension : 'scn', mimeType : 'openslide/scn'],
                [extension : 'ndpi', mimeType : 'openslide/ndpi'],
                [extension : 'bif', mimeType : 'openslide/bif'],
                [extension : 'zvi', mimeType : 'zeiss/zvi']
        ]
        bootstrapUtilsService.createMimes(IIPMimeSamples)
        bootstrapUtilsService.createMimeImageServers([IIPImageServer], IIPMimeSamples)


        def usersSamples = [
                //[username : 'anotheruser', firstname : 'Another', lastname : 'User', email : grailsApplication.config.grails.admin.email, group : [[name : "GIGA"]], password : 'password', color : "#FF0000", roles : ["ROLE_USER", "ROLE_ADMIN","ROLE_SUPER_ADMIN"]],
                [username : 'ImageServer1', firstname : 'Image', lastname : 'Server', email : grailsApplication.config.grails.admin.email, group : [[name : "GIGA"]], password : RandomStringUtils.random(32,  (('A'..'Z') + ('0'..'0')).join().toCharArray()), color : "#FF0000", roles : ["ROLE_USER", "ROLE_ADMIN"]],
                [username : 'superadmin', firstname : 'Super', lastname : 'Admin', email : grailsApplication.config.grails.admin.email, group : [[name : "GIGA"]], password : grailsApplication.config.grails.adminPassword, color : "#FF0000", roles : ["ROLE_USER", "ROLE_ADMIN","ROLE_SUPER_ADMIN"]],
                [username : 'admin', firstname : 'Just an', lastname : 'Admin', email : grailsApplication.config.grails.admin.email, group : [[name : "GIGA"]], password : grailsApplication.config.grails.adminPassword, color : "#FF0000", roles : ["ROLE_USER", "ROLE_ADMIN"]],
                [username : 'rabbitmq', firstname : 'rabbitmq', lastname : 'user', email : grailsApplication.config.grails.admin.email, group : [[name : "GIGA"]], password : RandomStringUtils.random(32,  (('A'..'Z') + ('0'..'0')).join().toCharArray()), color : "#FF0000", roles : ["ROLE_USER"]],
                [username : 'jsnow', firstname : 'John', lastname : 'Snow', email : grailsApplication.config.grails.admin.email, group : [[name : "GIGA"]], password : "jsnow", color : "#FF0000", roles : ["ROLE_USER"]],
                [username : 'estark', firstname : 'Eddard', lastname : 'Stark', email : grailsApplication.config.grails.admin.email, group : [[name : "GIGA"]], password : "estark", color : "#FF0000", roles : ["ROLE_USER"]],
                [username : 'clannister', firstname : 'Cersei', lastname : 'Lannister', email : grailsApplication.config.grails.admin.email, group : [[name : "GIGA"]], password : "clannister", color : "#FF0000", roles : ["ROLE_USER"]]

        ]

        bootstrapUtilsService.createUsers(usersSamples)
        bootstrapUtilsService.createRelation()

        SecUser admin = SecUser.findByUsername("admin")
        if(!grailsApplication.config.grails.adminPrivateKey) {
            throw new IllegalArgumentException("adminPrivateKey must be set!")
        }
        if(!grailsApplication.config.grails.adminPublicKey) {
            throw new IllegalArgumentException("adminPublicKey must be set!")
        }
        admin.setPrivateKey((String) grailsApplication.config.grails.adminPrivateKey)
        admin.setPublicKey((String) grailsApplication.config.grails.adminPublicKey)
        admin.save(flush : true)

        SecUser superAdmin = SecUser.findByUsername("superadmin")
        if(!grailsApplication.config.grails.superAdminPrivateKey) {
            throw new IllegalArgumentException("superadminPrivateKey must be set!")
        }
        if(!grailsApplication.config.grails.superAdminPublicKey) {
            throw new IllegalArgumentException("superadminPublicKey must be set!")
        }
        superAdmin.setPrivateKey((String) grailsApplication.config.grails.superAdminPrivateKey)
        superAdmin.setPublicKey((String) grailsApplication.config.grails.superAdminPublicKey)
        superAdmin.save(flush : true)

    }

    public void recreateTableFromNotDomainClass() {
        new Sql(dataSource).executeUpdate("DROP TABLE IF EXISTS  task_comment")
        new Sql(dataSource).executeUpdate("DROP TABLE IF EXISTS  task")

        new Sql(dataSource).executeUpdate("CREATE TABLE task (id bigint,progress bigint,project_id bigint,user_id bigint,print_in_activity boolean)")
        new Sql(dataSource).executeUpdate("CREATE TABLE task_comment (task_id bigint,comment character varying(255),timestamp bigint)")
    }

}
