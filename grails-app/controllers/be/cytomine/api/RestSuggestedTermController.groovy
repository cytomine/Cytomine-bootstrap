package be.cytomine.api

import be.cytomine.command.Command
import be.cytomine.command.term.AddTermCommand
import be.cytomine.command.term.DeleteTermCommand
import be.cytomine.command.term.EditTermCommand
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.Term
import be.cytomine.project.Project
import be.cytomine.security.User
import grails.converters.JSON
import be.cytomine.ontology.Annotation
import be.cytomine.ontology.SuggestedTerm
import be.cytomine.processing.Job
import be.cytomine.command.suggestedTerm.AddSuggestedTermCommand
import be.cytomine.command.suggestedTerm.DeleteSuggestedTermCommand

class RestSuggestedTermController extends RestController{

  def springSecurityService
  def transactionService

  def list = {
    Annotation annotation = Annotation.read(params.idannotation)
    if(params.idannotation) {
        if(annotation)
            responseSuccess(SuggestedTerm.findAllByAnnotation(annotation))
        else
            responseNotFound("SuggestedTerm","Annotation",params.idannotation)
    } else responseSuccess(SuggestedTerm.list())
  }

  def show = {
    Term term = Term.read(params.idterm)
    Annotation annotation = Annotation.read(params.idannotation)
    Job job = Job.read(params.idjob)
    SuggestedTerm suggestedTerm = SuggestedTerm.findWhere(annotation:annotation,term:term,job:job)
    if(suggestedTerm) responseSuccess(suggestedTerm)
    else responseNotFound("SuggestedTerm","Term",params.idterm,"Annotation",params.idannotation,"Job",params.idjob)
  }

  def worstAnnotation = {
      List<SuggestedTerm> results = new ArrayList<SuggestedTerm>()
      Project project = Project.read(params.idproject)
      int max = params.max ? Integer.parseInt(params.max) : 20

      List<SuggestedTerm> suggest = SuggestedTerm.findAllByProject(project,[sort:"rate", order:"desc"])

      for(int i=0;i<suggest.size() && max>results.size();i++) {
          if(suggest.get(i).annotationMapWithBadTerm())
              results.add(suggest.get(i));
      }
      responseSuccess(results)
  }

  def worstTerm = {
      Map<Term,Integer> termMap = new HashMap<Term,Integer>()
      Project project = Project.read(params.idproject)
      int max = params.max ? Integer.parseInt(params.max) : 20

      List<Term> termList = Term.findAllByOntology(project.ontology)
      termList.each {termMap.put(it,0)}

      List<SuggestedTerm> suggest = SuggestedTerm.findAllByProject(project,[sort:"rate", order:"desc"])

      for(int i=0;i<suggest.size();i++) {
          if(suggest.get(i).annotationMapWithBadTerm()) {
             Term term = suggest.get(i).term
             termMap.put(term,termMap.get(term)+1);
          }
      }
      termList.clear()
      termMap.each {  key, value ->
          key.rate = value
          termList.add(key)
      }

      responseSuccess(termList)
  }


  def add = {
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    def result = processCommand(new AddSuggestedTermCommand(user: currentUser), request.JSON)
    response(result)
  }


  def delete = {
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    def json = ([annotation : params.idannotation,term : params.idterm,job : params.idjob]) as JSON
    def result = processCommand(new DeleteSuggestedTermCommand(user: currentUser), json)
    response(result)
  }



}
