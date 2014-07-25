package be.cytomine

import groovy.sql.Sql
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import static grails.async.Promises.*

class TestController {

    def bootstrapTestDataService
    def dataSource
    def attachedFileService
    def springSecurityService
    def auroraService

    def index() {}


    def insert() {
        bootstrapTestDataService.initSoftwareAndJobTemplate(params.long('project'),params.long('term'))
    }

    def test() {

//        println springSecurityService.principal
//        println springSecurityService.principal.authorities
//
//        springSecurityService.principal.authorities = springSecurityService.principal.authorities.findAll{it.authority!="ROLE_ADMIN"}
//
//        println springSecurityService.principal.authorities

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(auth.getAuthorities());
        println "x="+authorities
        authorities.add(new GrantedAuthorityImpl('ROLE_SUPERADMIN'));
        println "y="+authorities
        Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(),auth.getCredentials(),authorities)
        SecurityContextHolder.getContext().setAuthentication(newAuth);


        println "z="+authorities
    }


    def aurora() {
        auroraService.notifyImage()
    }




    def async() {
        def p1 = task { 2 * 2 }
        def p2 = task { 4 * 4 }
        def p3 = task { 8 * 8 }
        assert [4,16,64] == waitAll(p1, p2, p3)
        println waitAll(p1, p2, p3)
        println "test"
    }











//    def attack1() {
//        //just a example of SQL injection
//
//        def param = params.str
//        def keys = []
//        //http://localhost:8080/test/attack?str=lrollus => good keys
//
//        //http://localhost:8080/test/attack?str=lrollus'%20or%20'1'='1 => all keys
//
//       def request = "select private_key from sec_user where username like '${param}'"
////        println request
////        new Sql(dataSource).eachRow(request) {
////            keys << it[0]
////        }
//
//
//        //solve by:
//
//
//        //http://localhost:8080/test/attack?str=lrollus =>
//
//        //http://localhost:8080/test/attack?str=lrollus' or '1'='1 =>
//
//        request = "select private_key from sec_user where username like ?"
//        println request
//        new Sql(dataSource).eachRow(request,[param]) {
//            keys << it[0]
//        }
//        render keys
//
//
//
//    }
//
//
//    def attack2() {
//        def keys = []
//        def request = "select private_key from sec_user where username like '${params.key}'"
//        println request
//        new Sql(dataSource).eachRow(request) {
//            keys << it[0]
//        }
//        render keys
//        //http://localhost:8080/test/attack?key=lrollus => good keys
//
//        //http://localhost:8080/test/attack?key=lrollus'%20or%20'1'='1 => all keys
//
//        //solve by:
//
//
//        //http://localhost:8080/test/attack?str=lrollus =>
//
//        //http://localhost:8080/test/attack?str=lrollus' or '1'='1 =>
//
//        request = "select private_key from sec_user where username like ?"
//        println request
//        new Sql(dataSource).eachRow(request,[param]) {
//            keys << it[0]
//        }
//        render keys
//    }
}
