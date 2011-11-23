package be.cytomine.command.job

import be.cytomine.command.AddCommand
import be.cytomine.command.SimpleCommand
import be.cytomine.processing.Job

class AddJobCommand extends AddCommand implements SimpleCommand {

    def execute() {
        //Init new domain object
        Job domain = Job.createFromData(json)
        //Validate and save domain
        domainService.saveDomain(domain)
        //Build response message
        String message = createMessage(domain, [domain.id, Job])
        //Init command info
        fillCommandInfo(domain,message)
        //Create and return response
        return responseService.createResponseMessage(domain,message,printMessage)
    }
}