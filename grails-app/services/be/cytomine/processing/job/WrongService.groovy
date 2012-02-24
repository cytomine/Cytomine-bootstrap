package be.cytomine.processing.job

import be.cytomine.Exception.WrongArgumentException
import be.cytomine.processing.Job
import be.cytomine.processing.JobParameter
import be.cytomine.processing.SoftwareParameter

class WrongService {

    static transactional = true
    def cytomineService
    def commandService
    def domainService

    /**
     * SERVICE MUST BE DELETE AFTER THE SERVICENAME HAS BEEN SET FOR EACH SOFTWARE
     */


}
