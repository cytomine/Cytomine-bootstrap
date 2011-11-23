package be.cytomine.command.job

import be.cytomine.command.AddCommand
import be.cytomine.command.SimpleCommand
import be.cytomine.processing.Job

class AddJobCommand extends AddCommand implements SimpleCommand {

    def execute() {
        Job job = Job.createFromData(json)
        return super.validateAndSave(job, ["#ID#", job] as Object[])
    }
}