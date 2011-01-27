package be.cytomine.command

interface UndoRedoCommand  {

    def execute()
    def undo()
    def redo()
}
