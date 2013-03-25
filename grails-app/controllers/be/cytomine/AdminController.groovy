package be.cytomine

import be.cytomine.api.RestController
import geb.Browser
import grails.plugins.springsecurity.Secured

@Secured(['ROLE_ADMIN'])
class AdminController extends RestController {


    def grailsApplication

    def index() {
        testCyto()

    }

    def testCyto() {
        Browser.drive {
            println "toto!!!"
            go "http://localhost:8080"
           // go "http://google.com/ncr"
//            println it
//            println $("body")
//            println $("body").text()
//            println $("body").children()
//
//            println $("img")
//            println $("input")

            printChildren($("body"),0)

            Thread.sleep(10000)

            waitFor {
                title.endsWith("Google Search")
            }

            printChildren($("body"),0)

              // make sure we actually got to the page
             assert title == "Google"

            $("input", name: "q").value("wikipedia")

            $("input", name: "btnG").click()

            waitFor { title.endsWith("Google Search") }

        }
    }

    def printChildren(def elem, int deep) {

        def prefix =""
        for(int i=0;i<deep;i++) {
            prefix = prefix + "\t"
        }

        println prefix + elem
//        println elem
        def childrens = elem.children()
        childrens.each {
            printChildren(it,deep+1)
        }
    }


    def testGeb() {
        Browser.drive {
            println "toto!!!"
            go "http://google.com/ncr"

            println it
            println $("body")
            println $("img")
            println $("input")

              // make sure we actually got to the page
             assert title == "Google"

            $("input", name: "q").value("wikipedia")

            $("input", name: "btnG").click()

            waitFor { title.endsWith("Google Search") }

        }
    }

}
