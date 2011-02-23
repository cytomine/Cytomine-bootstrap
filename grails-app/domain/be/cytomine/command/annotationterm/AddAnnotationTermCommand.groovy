package be.cytomine.command.annotationterm
import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import grails.converters.JSON
import be.cytomine.project.AnnotationTerm

class AddAnnotationTermCommand extends Command implements UndoRedoCommand {

  def execute() {
    log.info("Execute")
    try
    {
      def json = JSON.parse(postData)
      AnnotationTerm newAnnotationTerm = AnnotationTerm.createAnnotationTermFromData(json.annotationTerm)
      if (newAnnotationTerm.validate()) {
        newAnnotationTerm =  AnnotationTerm.link(newAnnotationTerm.annotation,newAnnotationTerm.term)
        log.info("Save AnnotationTerm with id:"+newAnnotationTerm.id)
        data = newAnnotationTerm.encodeAsJSON()
        return [data : [success : true, message:"ok", annotationTerm : newAnnotationTerm], status : 201]
      } else {
        return [data : [annotationTerm : newAnnotationTerm, errors : [newAnnotationTerm.errors]], status : 400]
      }
    }catch(IllegalArgumentException ex)
    {
      log.error("Cannot save annotationTerm:"+ex.toString())
      return [data : [annotationTerm : null , errors : ["Cannot save annotationTerm:"+ex.toString()]], status : 400]
    }
  }

  def undo() {
    log.info("Undo")
    def annotationTermData = JSON.parse(data)
    def annotationTerm = AnnotationTerm.findById(annotationTermData.id)
    AnnotationTerm.unlink(annotationTerm.annotation,annotationTerm.term)
    log.debug("Delete annotationTerm with id:"+annotationTermData.id)
    return [data : ["AnnotationTerm deleted"], status : 201]
  }

  def redo() {
    log.info("Redo:"+data.replace("\n",""))
    def annotationTermData = JSON.parse(data)
    def json = JSON.parse(postData)
    log.debug("Redo json:"+ json.toString() )
    def annotationTerm = AnnotationTerm.createAnnotationTermFromData(json.annotationTerm)
    annotationTerm = AnnotationTerm.link(annotationTermData.id,annotationTerm.annotation,annotationTerm.term)
    println "annotationTermData.id="+annotationTermData.id

    log.debug("Save annotationTerm:"+annotationTerm.id)
    return [data : [annotationTerm : annotationTerm], status : 200]
  }

}
