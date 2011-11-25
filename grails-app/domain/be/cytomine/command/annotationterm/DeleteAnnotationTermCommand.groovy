package be.cytomine.command.annotationterm

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.command.DeleteCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.ontology.Annotation
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Term
import be.cytomine.security.User
import grails.converters.JSON

class DeleteAnnotationTermCommand extends DeleteCommand implements UndoRedoCommand {

//    boolean saveOnUndoRedoStack = true;
//
//    def execute() {
//        //Retrieve domain
//        Annotation annotation = Annotation.get(json.annotation)
//        Term term = Term.get(json.term)
//        User user = User.get(json.user)
//        AnnotationTerm relation = AnnotationTerm.findWhere('annotation': annotation, 'term': term, 'user': user)
//        if (!relation) throw new ObjectNotFoundException("Annotation term not found ($annotation,$term,$user)")
//        String id = relation.id
//         //Build response message
//        String message = createMessage(relation, [id, annotation.id, term.name, user?.username])
//        //Init command info
//        super.initCurrentCommantProject(relation.annotation.image.project)
//        fillCommandInfo(relation,message)
//        //Delete domain
//        relation.unlink(relation.annotation, relation.term, relation.user)
//        //Create and return response
//        return responseService.createResponseMessage(relation,message,printMessage)
//    }
//
//    def undo() {
//        return restore(annotationTermService,JSON.parse(data))
//    }
//
//    def redo() {
//        return destroy(annotationTermService,JSON.parse(data))
//    }

}