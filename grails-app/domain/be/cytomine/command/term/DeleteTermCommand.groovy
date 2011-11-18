package be.cytomine.command.term

import be.cytomine.command.UndoRedoCommand
import be.cytomine.ontology.Term
import grails.converters.JSON
import be.cytomine.command.DeleteCommand
import java.util.prefs.BackingStoreException
import be.cytomine.ontology.Ontology
import org.hibernate.exception.ConstraintViolationException
import java.sql.SQLException
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ontology.SuggestedTerm
import be.cytomine.Exception.ConstraintException
import be.cytomine.ontology.AnnotationTerm

class DeleteTermCommand extends DeleteCommand implements UndoRedoCommand {
    boolean saveOnUndoRedoStack = true;

    def execute() throws CytomineException {
        log.info "Execute"

        Term term = Term.get(json.id)
        if (!term) throw new ObjectNotFoundException("Term " + json.id + " was not found")
        if(!SuggestedTerm.findAllByTerm(term).isEmpty()) throw new ConstraintException("Term " + json.id + " has suggested term")
        if(!AnnotationTerm.findAllByTerm(term).isEmpty()) throw new ConstraintException("Term " + json.id + " has annotation term")
        return super.deleteAndCreateDeleteMessage(json.id, term, [term.id, term.name, term.ontology?.name] as Object[])
    }

    def undo() {
        log.info("Undo")
        def termData = JSON.parse(data)
        Term term = Term.createFromData(termData)
        term.id = termData.id;
        term.save(flush: true)
        def callback = [ontologyID: term?.ontology?.id]
        log.error "Term errors = " + term.errors
        return super.createUndoMessage(term, [term.id, term.name, term.ontology] as Object[], callback);
    }

    def redo() {
        log.info("Redo postData=" + postData)
        def termData = JSON.parse(postData)
        Term term = Term.findById(termData.id)
        String id = termData.id
        String name = term.name
        String ontologyName = term.ontology?.name
        def callback = [ontologyID: term?.ontology?.id]
        term.delete(flush: true);

        return super.createRedoMessage(id, term, [id, name, ontologyName] as Object[], callback);
    }

}
