package be.cytomine.command.annotation

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.command.DeleteCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.ontology.Annotation
import grails.converters.JSON

class DeleteAnnotationCommand extends DeleteCommand implements UndoRedoCommand {


}
