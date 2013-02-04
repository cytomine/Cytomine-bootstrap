package be.cytomine.processing

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityCheck
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.command.Transaction
import be.cytomine.processing.Job
import be.cytomine.processing.JobParameter
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.ModelService
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.access.prepost.PreAuthorize

class JobDataBinaryValueService extends ModelService {

    static transactional = true
    def cytomineService
    def commandService
    def modelService

    //TODO: move here code for JobDataBinaryValue CRUD

}
