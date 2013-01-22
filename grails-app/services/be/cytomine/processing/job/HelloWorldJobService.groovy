package be.cytomine.processing.job

import be.cytomine.processing.Job
import be.cytomine.security.UserJob

/**
 * Simple software example
 * Just print hello world
 */
class HelloWorldJobService extends AbstractJobService {

    static transactional = true
    def cytomineService
    def commandService
    def domainService

    def jobParameterService

    def init(Job job, UserJob userJob) {
        log.info "Do nothing..."
    }

    def execute(Job job) {
        log.info "Hello world"
    }


    @Override
    Double computeRate(Job job) {
        return null;
    }
}
