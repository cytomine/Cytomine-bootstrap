package be.cytomine.geb

import geb.junit4.*
import org.junit.Test
import geb.Page
import geb.Browser
import be.cytomine.test.http.ProjectAPI
import be.cytomine.test.Infos
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray

/**
 * User: lrollus
 * Date: 5/02/13
 * GIGA-ULg
 * 
 */
class GebTests extends GebReportingTest{
//    @Test
//   	void doSomeCrud() {
//        println "toto!"
//        assert 1 != 2
//
//    }
//
//    @Test
//     void login() {
//         to LoginPage
//         at LoginPage
//        println LoginPage.text()
//
//        println $("html")
//
//        Thread.sleep(10000)
//
//        //println username.present
//
//
//        waitFor { username.present }
//
//         username = "lrollus"
//         password = "lR\$2011"
//
//         loginButton.click()
//
//         assert at(IndexPage)
////
////         link.click()
//
//     }

    void testListProjectWithCredential() {
        def result = ProjectAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }


     @Test
     void checkGoogle() {


Browser.drive {
    println "toto!!!"
    go "http://google.com/ncr"

      // make sure we actually got to the page
     assert title == "Google"

    $("input", name: "q").value("wikipedia")

    $("input", name: "btnG").click()

    waitFor { title.endsWith("Google Search") }

}

//         go "http://google.com/ncr"
//
//             // make sure we actually got to the page
//             assert title == "Google"
//
//             // enter wikipedia into the search field
//             println $("html")
//             println $("head").children()
//             println $("input", name: "q")
//         println title
//
//         println $("body").children()
//
//             $("input", name: "q").value("wikipedia")
//
//         println $("body").children()
//         println $("ol", id: "rso").children()
//         Thread.sleep(3000)
//          println $("body").children()
//         println $("ol", id: "rso").children()
//         Thread.sleep(3000)
//          println $("body").children()
//         println $("ol", id: "rso").children()
//         Thread.sleep(3000)
//          println $("body").children()
//         println $("ol", id: "rso").children()
//         //println $("input", name: "q").value()
//
//             // wait for the change to results page to happen
//             // (google updates the page dynamically without a new request)
//             println title
//             Thread.sleep(5000)
//         println title
//         Thread.sleep(5000)
//     println title
//             waitFor { title.endsWith("Google Search") }
//
//             // is the first link to wikipedia?
//             def firstLink = $("li.g", 0).find("a.l")
//             assert firstLink.text() == "Wikipedia"
//
//             // click the link
//             firstLink.click()
//
//             // wait for Google's javascript to redirect to Wikipedia
//             waitFor { title == "Wikipedia" }
     }
}

//
//class LoginPage extends Page {
//
//    static url = "http://localhost:8090"
//
//    static at = {
//        title ==~ /Cytomine/
//    }
//
//    static content = {
////        loginForm { $("form", id: "loginForm") }
//        username { $("input", type:"text", id:"j_username") }
//        password { $("input", type:"password", id:"j_password") }
//        loginButton{ $("input", type:"submit", id:"submit-login") }
//    }
//
//}
//
//class IndexPage extends Page {
//
//    static url = "http://localhost:8090/#project"
//
//    static at = {
//        title ==~ /Cytomine/
//    }
//
//    static content = {
////        description { $('h1') }
////        link { $('a') }
//    }

//}
