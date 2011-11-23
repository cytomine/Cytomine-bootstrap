package be.cytomine.command.term

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.command.EditCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.ontology.Term
import grails.converters.JSON

class EditTermCommand extends EditCommand implements UndoRedoCommand {
    boolean saveOnUndoRedoStack = true;

    def execute() throws CytomineException {
        log.info "Execute"
        Term updatedTerm = Term.get(json.id)
        if (!updatedTerm) throw new ObjectNotFoundException("Term ${json.id} not found")
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
