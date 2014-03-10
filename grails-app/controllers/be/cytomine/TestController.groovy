package be.cytomine

import groovy.sql.Sql

class TestController {

    def bootstrapTestDataService
    def dataSource

    def index() {}


    def insert() {
        bootstrapTestDataService.initSoftwareAndJobTemplate(params.long('project'),params.long('term'))
    }

    def attack1() {
        //just a example of SQL injection

        def param = params.str
        def keys = []
        //http://localhost:8080/test/attack?str=lrollus => good keys

        //http://localhost:8080/test/attack?str=lrollus'%20or%20'1'='1 => all keys

       def request = "select private_key from sec_user where username like '${param}'"
//        println request
//        new Sql(dataSource).eachRow(request) {
//            keys << it[0]
//        }


        //solve by:


        //http://localhost:8080/test/attack?str=lrollus =>

        //http://localhost:8080/test/attack?str=lrollus' or '1'='1 =>

        request = "select private_key from sec_user where username like ?"
        println request
        new Sql(dataSource).eachRow(request,[param]) {
            keys << it[0]
        }
        render keys



    }
}
