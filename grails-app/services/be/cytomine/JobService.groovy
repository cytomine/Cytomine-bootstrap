package be.cytomine

import be.cytomine.processing.Job
import be.cytomine.security.User
import be.cytomine.command.job.AddJobCommand

class JobService {

    static transactional = true
    def cytomineService
    def commandService

    def list() {
        Job.list()
    }

    def read(def id) {
        Job.read(id)
    }

    def add(def json) {
        User currentUser = cytomineService.getCurrentUser()
        return commandService.processCommand(new AddJobCommand(user: currentUser), json)
    }
}
