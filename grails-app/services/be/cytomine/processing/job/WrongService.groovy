package be.cytomine.processing.job

import be.cytomine.processing.Job

class WrongService {

    static transactional = true
    def cytomineService
    def commandService
    def modelService

    /**
     * SERVICE MUST BE DELETE AFTER THE SERVICENAME HAS BEEN SET FOR EACH SOFTWARE
     */

    Double computeRate(Job job) {
        return null;
    }
}
