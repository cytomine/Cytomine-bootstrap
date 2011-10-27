package be.cytomine.command.annotationterm

import be.cytomine.command.UndoRedoCommand
import grails.converters.JSON
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Annotation
import be.cytomine.ontology.Term
import be.cytomine.command.AddCommand
import be.cytomine.ontology.RelationTerm
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException

class AddAnnotationTermCommand extends AddCommand implements UndoRedoCommand {
  boolean saveOnUndoRedoStack = true;
  def execute() {
    log.info("Execute")
    AnnotationTerm newAnnotationTerm=null
    try {
      def json = JSON.parse(postData)
      json.user = user.id
      newAnnotationTerm = AnnotationTerm.createAnnotationTermFromData(json)
      AnnotationTerm.link(newAnnotationTerm.annotation,newAnnotationTerm.term,newAnnotationTerm.user)
      super.changeCurrentProject(newAnnotationTerm.annotation.image.project)
        return super.validateWithoutSave(newAnnotationTerm,["#ID#",newAnnotationTerm.annotation.id,newAnnotationTerm.term.name,newAnnotationTerm.user?.username] as Object[])
      }catch(ConstraintException  ex){
      return [data : [annotationterm:newAnnotationTerm,errors:newAnnotationTerm.retrieveErrors()], status : 400]
    }catch(IllegalArgumentException ex){
      return [data : [annotationterm:null,errors:["Cannot save annotation-term:"+ex.toString()]], status : 400]
    }
  }

  def undo() {
    log.info("Undo")
    log.info("data="+data)
    def annotationTermData = JSON.parse(data)
    def annotation = Annotation.get(annotationTermData.annotation)
    def term = Term.get(annotationTermData.term)
    def user = be.cytomine.security.User.get(annotationTermData.user)
    def annotationTerm = AnnotationTerm.findWhere('annotation':annotation,'term':term,'user':user)

    AnnotationTerm.unlink(annotationTerm.annotation,annotationTerm.term,annotationTerm.user)

    HashMap<String,Object> callback = new HashMap<String,Object>();
    callback.put("annotationID",annotation.id)
    callback.put("termID",term.id)
    callback.put("imageID",annotation.image.id)

    log.debug "AnnotationTerm=" + annotationTermData.id +" annotation.name=" + annotation.name  + " term.name=" + term.name + " user.username=" + user?.username
    String id = annotationTermData.id
    return super.createUndoMessage(id,annotationTerm,[id,annotation.id,term.name,user?.username] as Object[],callback);
  }



  def redo() {
    log.info("Redo="+data)
    def annotationTermData = JSON.parse(data)

    def annotation = Annotation.get(annotationTermData.annotation)
    def term = Term.get(annotationTermData.term)
    def user =  be.cytomine.security.User.get(annotationTermData.user)

    def annotationTerm = AnnotationTerm.createAnnotationTermFromData(annotationTermData)

    AnnotationTerm.link(annotation,term,user)

    HashMap<String,Object> callback = new HashMap<String,Object>();
    callback.put("annotationID",annotation.id)
    callback.put("termID",term.id)
    callback.put("imageID",annotation.image.id)

    return super.createRedoMessage( annotationTerm,[id,annotation.id,term.name,user?.username] as Object[],callback);
  }

}
