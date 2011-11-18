package be.cytomine.command.term

import be.cytomine.command.UndoRedoCommand
import grails.converters.JSON
import be.cytomine.ontology.Term
import be.cytomine.command.EditCommand
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException

class EditTermCommand extends EditCommand implements UndoRedoCommand {
    boolean saveOnUndoRedoStack = true;

    def execute() {
        log.info "Execute"
        Term updatedTerm = Term.get(json.id)
        return super.validateAndSave(json, updatedTerm, [updatedTerm.id, updatedTerm.name, updatedTerm.ontology?.name] as Object[])
    }

    def undo() {
        log.info "Undo"
        def termData = JSON.parse(data)
        Term term = Term.findById(termData.previousTerm.id)
        term = term.getFromData(term, termData.previousTerm)
        term.save(flush: true)
        def callback = [ontologyID: term?.ontology?.id]
        super.createUndoMessage(termData, term, [term.id, term.name, term.ontology?.name] as Object[], callback)
    }

    def redo() {
        log.info "Redo"
        def termData = JSON.parse(data)
        Term term = Term.findById(termData.newTerm.id)
        term = Term.getFromData(term, termData.newTerm)
        term.save(flush: true)
        def callback = [ontologyID: term?.ontology?.id]
        super.createRedoMessage(termData, term, [term.id, term.name, term.ontology?.name] as Object[], callback)
    }
}
