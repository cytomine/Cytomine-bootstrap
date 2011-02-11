package be.cytomine.api.project

import be.cytomine.project.Term
import grails.converters.XML
import grails.converters.JSON
import be.cytomine.project.Annotation
import be.cytomine.project.AnnotationTerm
import be.cytomine.security.User
import be.cytomine.command.Command
import be.cytomine.command.term.AddTermCommand
import be.cytomine.command.Transaction
import be.cytomine.command.UndoStack

class RestTermController {

  def springSecurityService


  def list = {

    println "..."
    println params.idannotation
    def data = [:]
    Annotation annotation = Annotation.get(params.idannotation);
    println annotation
    // println annotation.terms()[0].name
    if(params.idannotation == null) {
      data.term = Term.list()
    } else
    {
      //TODO: check if annotation exist
      data.term = Annotation.get(params.idannotation).terms()
    }
    withFormat {
      json { render data as JSON }
      xml { render data as XML}
    }
  }

  def show = {

    if(params.idterm && Term.exists(params.idterm)) {
      def data = Term.findById(params.idterm)
      withFormat {
        json { render data as JSON }
        xml { render data as XML }
      }
    } else {
      response.status = 404
      render contentType: "application/xml", {
        errors {
          message("Term not found with id: " + params.id)
        }
      }
    }
  }

  def add = {
    println "add Term pwet"
    User currentUser = User.get(springSecurityService.principal.id)
    println "currentUser="+ currentUser.username
    println "force"
    println "request.JSON.toString()="+request.JSON.toString()
    Command addTermCommand = new AddTermCommand(postData : request.JSON.toString())
    Transaction currentTransaction = currentUser.getNextTransaction()
    currentTransaction.addToCommands(addTermCommand)
    def result = addTermCommand.execute()

    if (result.status == 201) {
      addTermCommand.save()
      new UndoStack(command : addTermCommand, user: currentUser).save()
    }

    response.status = result.status
    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }


}
