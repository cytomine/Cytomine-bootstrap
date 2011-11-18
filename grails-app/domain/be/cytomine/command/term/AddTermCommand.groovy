package be.cytomine.command.term

import be.cytomine.command.UndoRedoCommand
import grails.converters.JSON
import be.cytomine.ontology.Term
import be.cytomine.command.AddCommand
import be.cytomine.ontology.Ontology

import org.codehaus.groovy.grails.validation.exceptions.ConstraintException
import be.cytomine.Exception.CytomineException

class AddTermCommand extends AddCommand implements UndoRedoCommand {
    boolean saveOnUndoRedoStack = true;

    def execute() throws CytomineException {
        log.info("Execute")
        Term newTerm = Term.createFromData(json)
        return super.validateAndSave(newTerm, ["#ID#", json.name, Ontology.read(json.ontology)?.name] as Object[])
    }

    def undo() {
        log.info("Undo")
        def termData = JSON.parse(data)
        Term term = Term.get(termData.id)
        def callback = [ontologyID: term?.ontology?.id]
        term.delete(flush: true)
        String id = termData.id
        return super.createUndoMessage(id, term, [termData.id, termData.name, Ontology.read(termData.ontology).name] as Object[], callback);
    }

    def redo() {
        log.info("Undo")
        def termData = JSON.parse(data)
        def term = Term.createFromData(termData)
        term.id = termData.id
        term.save(flush: true)
        def callback = [ontologyID: term?.ontology?.id]
        return super.createRedoMessage(term, [termData.id, termData.name, Ontology.read(termData.ontology)?.name] as Object[], callback);
    }

}
