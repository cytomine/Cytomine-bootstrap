package be.cytomine.utils.bootstrap

import groovy.sql.Sql

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 13/03/13
 * Time: 11:30
 */
class BootstrapTestDataService {

    def bootstrapUtilsService
    def dataSource

    def initData() {

        new Sql(dataSource).executeUpdate("DROP TABLE IF EXISTS  task_comment")
        new Sql(dataSource).executeUpdate("DROP TABLE IF EXISTS  task")

        new Sql(dataSource).executeUpdate("CREATE TABLE task (id bigint,progress bigint,project_id bigint,user_id bigint,print_in_activity boolean)")
        new Sql(dataSource).executeUpdate("CREATE TABLE task_comment (task_id bigint,comment character varying(255),timestamp bigint)")

//        new Sql(dataSource).executeUpdate("DROP TABLE keywords")
//        new Sql(dataSource).executeUpdate("CREATE TABLE keywords (key character varying(255))")


        def imageServerSamples = [
                [className : 'IIPResolver', name : 'IIP', service : '/fcgi-bin/iipsrv.fcgi', url : 'http://localhost:8081', available : true]
        ]
        bootstrapUtilsService.createImageServers(imageServerSamples)
        def mimeSamples = [
                [extension : 'mrxs', mimeType : 'openslide/mrxs'],
                [extension : 'vms', mimeType : 'openslide/vms'],
                [extension : 'tif', mimeType : 'image/tiff'],
                [extension : 'tiff', mimeType : 'image/tiff'],
                [extension : 'svs', mimeType : 'openslide/svs'],
                [extension : 'jp2', mimeType : 'image/jp2'],
                [extension : 'scn', mimeType : 'openslide/scn']
        ]
        bootstrapUtilsService.createMimes(mimeSamples)
        bootstrapUtilsService.createMimeImageServers()


        def usersSamples = [
                [username : 'rmaree', firstname : 'Raphaël', lastname : 'Marée', email : 'rmaree@ulg.ac.be', group : [[name : "GIGA"]], password : 'rM$2011', color : "#FF0000", roles : ["ROLE_USER", "ROLE_ADMIN"]],
                [username : 'lrollus', firstname : 'Loïc', lastname : 'Rollus', email : 'lrollus@ulg.ac.be', group : [[name : "GIGA"]], password : 'lR$2011', color : "#00FF00", roles : ["ROLE_USER", "ROLE_ADMIN"]],
                [username : 'stevben', firstname : 'Benjamin', lastname : 'Stévens', email : 'bstevens@ulg.ac.be', group : [[name : "GIGA"]], password : 'sB$2011', color : "#0000FF",roles : ["ROLE_USER", "ROLE_ADMIN"]],
                [username : 'botta', firstname : 'Vincent', lastname : 'Botta', email : 'vincent.botta@ulg.ac.be', group : [[name : "GIGA"]], password : 'vB$2013', color : "#0000FF",roles : ["ROLE_USER", "ROLE_ADMIN"]],
                [username : 'johndoe', firstname : 'John', lastname : 'Doe', email : 'lrollus@ulg.ac.be', group : [[name : "GIGA"]], password : 'test', color : "#FF0000", roles : ["ROLE_USER"]],
                [username : 'guest', firstname : 'Simply', lastname : 'guest', email : 'lrollus@ulg.ac.be', group : [[name : "GIGA"]], password : 'guest', color : "#FF0000", roles : ["ROLE_GUEST"]],
                [username : 'admin', firstname : 'Admin', lastname : 'Master', email : 'lrollus@ulg.ac.be', group : [[name : "GIGA"]], password : 'admin', color : "#FF0000", roles : ["ROLE_USER", "ROLE_ADMIN"]],
                [username : 'ImageServer1', firstname : 'Image', lastname : 'Server', email : 'info@cytomine.be', group : [[name : "GIGA"]], password : 'passwordIS', color : "#FF0000", roles : ["ROLE_USER", "ROLE_ADMIN"]]
        ]

        bootstrapUtilsService.createUsers(usersSamples)
        bootstrapUtilsService.createRelation()

    }


}
