package be.cytomine.command.annotationterm

import be.cytomine.command.AddCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.ontology.Annotation
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Term
import grails.converters.JSON

class AddAnnotationTermCommand extends AddCommand implements UndoRedoCommand {


//    def execute() {
//        json.user = user.id
//        //Init new domain object
//        AnnotationTerm newRelation = AnnotationTerm.createFromData(json)
//        //Link relation domain
//        newRelation = AnnotationTerm.link(newRelation.annotation, newRelation.term, newRelation.user)
//        //Build response message
//        String message = createMessage(newRelation,[newRelation.id, newRelation.annotation.id, newRelation.term.name, newRelation.user?.username])
//        //Init command info
//        fillCommandInfo(newRelation,message)
//        //Create and return response
//        super.initCurrentCommantProject(newRelation.annotation.image.project)
//        return responseService.createResponseMessage(newRelation,message,printMessage)
//    }

}
