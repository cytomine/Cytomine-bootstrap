package be.cytomine

import be.cytomine.command.Command
import be.cytomine.command.CommandHistory
import grails.plugins.springsecurity.Secured
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import be.cytomine.ontology.ReviewedAnnotation

@Secured(['ROLE_ADMIN'])
class AdminController {


    def grailsApplication

    def index() {



    }


    def test() {

        Command c1 = Command.read(9332073)

//        Command c2 = Command.read(9332057)


//        Command c = Command.read(8968359)     //9299032
//        println c.id
//        println c.properties
//
//        CommandHistory ch = CommandHistory.read(8968360)
//        println ch.id
//        println ch.properties


    }
}
