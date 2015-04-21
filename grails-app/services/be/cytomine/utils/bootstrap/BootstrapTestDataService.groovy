package be.cytomine.utils.bootstrap

import be.cytomine.image.server.ImageServer
import be.cytomine.middleware.AmqpQueueConfig
import be.cytomine.processing.*
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import groovy.sql.Sql
import org.apache.commons.lang.RandomStringUtils

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 13/03/13
 * Time: 11:30
 */
class BootstrapTestDataService {

    def grailsApplication
    def bootstrapUtilsService
    def dataSource
    def amqpQueueConfigService

    def initVentana() {
        def mimeSamples = [
                [extension : 'tif', mimeType : 'ventana/tif']
        ]
        bootstrapUtilsService.createMimes(mimeSamples)
        bootstrapUtilsService.createMimeImageServers([ImageServer.findByName('IIP')], mimeSamples)
    }

    def initData() {

        recreateTableFromNotDomainClass()
        amqpQueueConfigService.initAmqpQueueConfigDefaultValues()

//        new Sql(dataSource).executeUpdate("DROP TABLE keywords")
//        new Sql(dataSource).executeUpdate("CREATE TABLE keywords (key character varying(255))")

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
                [username : 'admin', firstname : 'Just an', lastname : 'Admin', email : grailsApplication.config.grails.admin.email, group : [[name : "GIGA"]], password : grailsApplication.config.grails.adminPassword, color : "#FF0000", roles : ["ROLE_USER", "ROLE_ADMIN"]]
        ]

        bootstrapUtilsService.createUsers(usersSamples)
        bootstrapUtilsService.createRelation()

        SecUser admin = SecUser.findByUsername("admin")
        admin.setPrivateKey((String) grailsApplication.config.grails.adminPrivateKey)
        admin.setPublicKey((String) grailsApplication.config.grails.adminPublicKey)
        admin.save(flush : true)

    }

    public void recreateTableFromNotDomainClass() {
        new Sql(dataSource).executeUpdate("DROP TABLE IF EXISTS  task_comment")
        new Sql(dataSource).executeUpdate("DROP TABLE IF EXISTS  task")

        new Sql(dataSource).executeUpdate("CREATE TABLE task (id bigint,progress bigint,project_id bigint,user_id bigint,print_in_activity boolean)")
        new Sql(dataSource).executeUpdate("CREATE TABLE task_comment (task_id bigint,comment character varying(255),timestamp bigint)")
    }

    def initSoftwareAndJobTemplate(Long idProject, Long term) {

        Project project = Project.read(idProject)

        Software software = new Software(
                name: "computeTermStats",
                serviceName: 'launchLocalScriptService',
                resultName:'DownloadFiles',
                description: 'Compute term stats area for an annotation',
                executeCommand: "groovy -cp algo/computeAnnotationStats/Cytomine-Java-Client.jar:algo/computeAnnotationStats/jts-1.13.jar algo/computeAnnotationStats/computeAnnotationStats.groovy"
        )
        software.save(failOnError: true,flush: true)

        SoftwareProject softwareProject = new SoftwareProject(software:software, project: project)
        softwareProject.save(failOnError: true,flush: true)

        SoftwareParameter param0 = new SoftwareParameter(software:software, name:"host",type:"String",required: true, index:10,setByServer:true)
        param0.save(failOnError: true,flush:true)

        SoftwareParameter param1 = new SoftwareParameter(software:software, name:"publicKey",type:"String",required: true, index:100,setByServer:true)
        param1.save(failOnError: true,flush:true)
        SoftwareParameter param2 = new SoftwareParameter(software:software, name:"privateKey",type:"String",required: true, index:200,setByServer:true)
        param2.save(failOnError: true,flush:true)

        SoftwareParameter param3 = new SoftwareParameter(software:software, name:"annotation",type:"Domain",required: true, index:400)
        param3.save(failOnError: true,flush:true)
        SoftwareParameter param4 = new SoftwareParameter(software:software, name:"term",type:"Domain",required: true, index:500, uri: '/api/project/$currentProject$/term.json',uriPrintAttribut: 'name',uriSortAttribut: 'name')
        param4.save(failOnError: true,flush:true)

        JobTemplate jobTemplate = new JobTemplate(name:"ComputeAdenocarcinomesStat", software: software, project: project)
        jobTemplate.save(failOnError: true,flush:true)

        JobParameter paramTmpl1 = new JobParameter(job: jobTemplate,softwareParameter: param4, value: term)
        paramTmpl1.save(failOnError: true, flush:true)

    }

}
