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
        new Sql(dataSource).executeUpdate("DROP TABLE task_comment")
        new Sql(dataSource).executeUpdate("DROP TABLE task")

        new Sql(dataSource).executeUpdate("CREATE TABLE task (id bigint,progress bigint,project_id bigint,user_id bigint)")
        new Sql(dataSource).executeUpdate("CREATE TABLE task_comment (task_id bigint,comment character varying(255),timestamp bigint)")
        def usersSamples = [
                [username : 'rmaree', firstname : 'Raphaël', lastname : 'Marée', email : 'rmaree@ulg.ac.be', group : [[name : "GIGA"]], password : 'rM$2011', color : "#FF0000", roles : ["ROLE_USER", "ROLE_ADMIN"]],
                [username : 'lrollus', firstname : 'Loïc', lastname : 'Rollus', email : 'lrollus@ulg.ac.be', group : [[name : "GIGA"]], password : 'lR$2011', color : "#00FF00", roles : ["ROLE_USER", "ROLE_ADMIN"]],
                [username : 'stevben', firstname : 'Benjamin', lastname : 'Stévens', email : 'bstevens@ulg.ac.be', group : [[name : "GIGA"]], password : 'sB$2011', color : "#0000FF",roles : ["ROLE_USER", "ROLE_ADMIN"]],
                [username : 'pansen', firstname : 'Pierre', lastname : 'Ansen', email : 'pierreansen@gmail.com', group : [[name : "GIGA"]], password : 'pA$2013', color : "#FF0000", roles : ["ROLE_USER", "ROLE_ADMIN"]]
        ]

        bootstrapUtilsService.createUsers(usersSamples)
        bootstrapUtilsService.createRelation()
    }


}
