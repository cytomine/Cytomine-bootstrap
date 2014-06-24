package be.cytomine.utils.bootstrap

import be.cytomine.processing.*
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import groovy.sql.Sql

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

    def initVentana() {
        //def ventanaImageServer = [className : 'VentanaResolver', name : 'Ventana', service : '/ventana', url : grailsApplication.config.grails.imageServerURL, available : true]
        def ventanaImageServer = [className : 'VentanaResolver', name : 'IIP', service : '/fcgi-bin/iipsrv.fcgi', url : grailsApplication.config.iipImageServer, available : true]
        def mimeSamples = [
                [extension : 'tif', mimeType : 'ventana/tif']
        ]
        bootstrapUtilsService.createMimes(mimeSamples)
        bootstrapUtilsService.createImageServers([ventanaImageServer])
        bootstrapUtilsService.createMimeImageServers([ventanaImageServer], mimeSamples)
    }

    def initData() {

        recreateTableFromNotDomainClass()

//        new Sql(dataSource).executeUpdate("DROP TABLE keywords")
//        new Sql(dataSource).executeUpdate("CREATE TABLE keywords (key character varying(255))")

        def IIPImageServer = [className : 'IIPResolver', name : 'IIP', service : '/image/tile', url : 'http://localhost:9080', available : true]
        def LociImageServer = [className : 'LociResolver', name : 'Loci', service : '/loci/tile', url : 'http://localhost:9080', available : true]

        bootstrapUtilsService.createImageServers([IIPImageServer])
        bootstrapUtilsService.createImageServers([LociImageServer])
        def IIPMimeSamples = [
                [extension : 'mrxs', mimeType : 'openslide/mrxs'],
                [extension : 'vms', mimeType : 'openslide/vms'],
                [extension : 'tif', mimeType : 'image/tif'],
                [extension : 'tiff', mimeType : 'image/tiff'],
                [extension : 'svs', mimeType : 'openslide/svs'],
                [extension : 'jp2', mimeType : 'image/jp2'],
                [extension : 'scn', mimeType : 'openslide/scn'],
                [extension : 'ndpi', mimeType : 'openslide/ndpi'],
                [extension : 'bif', mimeType : 'openslide/bif']
        ]
        bootstrapUtilsService.createMimes(IIPMimeSamples)
        bootstrapUtilsService.createMimeImageServers([IIPImageServer], IIPMimeSamples)
        def LociMimeSamples = [
                [extension : 'zvi', mimeType : 'zeiss/zvi']
        ]
        bootstrapUtilsService.createMimes(LociMimeSamples)
        bootstrapUtilsService.createMimeImageServers([LociImageServer], LociMimeSamples)
        def usersSamples = [
                [username : 'anotheruser', firstname : 'Another', lastname : 'User', email : 'info@cytomine.be', group : [[name : "GIGA"]], password : 'password', color : "#FF0000", roles : ["ROLE_USER", "ROLE_ADMIN","ROLE_SUPER_ADMIN"]],
                [username : 'ImageServer1', firstname : 'Image', lastname : 'Server', email : 'info@cytomine.be', group : [[name : "GIGA"]], password : 'passwordIS', color : "#FF0000", roles : ["ROLE_USER", "ROLE_ADMIN"]],
                [username : 'superadmin', firstname : 'Super', lastname : 'Admin', email : 'info@cytomine.be', group : [[name : "GIGA"]], password : 'superadmin', color : "#FF0000", roles : ["ROLE_USER", "ROLE_ADMIN","ROLE_SUPER_ADMIN"]],
                [username : 'admin', firstname : 'Just an', lastname : 'Admin', email : 'info@cytomine.be', group : [[name : "GIGA"]], password : 'admin', color : "#FF0000", roles : ["ROLE_USER", "ROLE_ADMIN"]]
        ]

        bootstrapUtilsService.createUsers(usersSamples)
        bootstrapUtilsService.createRelation()

        //set public/private keys for special image server user
        SecUser imageServerUser = SecUser.findByUsername("anotheruser")
        imageServerUser.setPrivateKey("70f35a45-c317-405a-8056-353db3d2bf56")
        imageServerUser.setPublicKey("4a5c7004-b6f8-4705-a118-c15d5c90dcdb")
        imageServerUser.save(flush : true)

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
