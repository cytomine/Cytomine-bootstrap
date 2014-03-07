package be.cytomine.processing

import be.cytomine.SecurityACL
import be.cytomine.command.*
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.sql.RoiAnnotationListing
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import com.vividsolutions.jts.io.WKTWriter

import static org.springframework.security.acls.domain.BasePermission.READ

class RoiAnnotationService extends ModelService {

    static transactional = true
    def cytomineService
    def transactionService
    def annotationTermService
    def retrievalService
    def algoAnnotationTermService
    def modelService
    def simplifyGeometryService
    def dataSource
    def reviewedAnnotationService
    def propertyService
    def annotationListingService


    def currentDomain() {
        return RoiAnnotation
    }

    RoiAnnotation read(def id) {
        def annotation = RoiAnnotation.read(id)
        if (annotation) {
            SecurityACL.check(annotation.container(),READ)
        }
        annotation
    }

    def list(Project project,def propertiesToShow = null) {
        SecurityACL.check(project.container(),READ)
        annotationListingService.executeRequest(new RoiAnnotationListing(project: project.id, columnToPrint: propertiesToShow))
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    def add(def json,def minPoint = null, def maxPoint = null) {
        SecurityACL.check(json.project, Project,READ)
        SecUser currentUser = cytomineService.getCurrentUser()

        //simplify annotation
        try {
            def data = simplifyGeometryService.simplifyPolygon(json.location,minPoint,maxPoint)
            json.location = new WKTWriter().write(data.geometry)
            json.geometryCompression = data.rate
        } catch (Exception e) {
            log.error("Cannot simplify:" + e)
        }

        //Start transaction
        Transaction transaction = transactionService.start()
        json.user = currentUser.id
        return executeCommand(new AddCommand(user: currentUser, transaction: transaction),null,json)
    }

    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param jsonNewData New domain datas
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(RoiAnnotation annotation, def jsonNewData) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.checkIsSameUserOrAdminContainer(annotation,annotation.user,currentUser)
        //simplify annotation
        try {
            def data = simplifyGeometryService.simplifyPolygon(json.location, annotation?.geometryCompression)
            json.location = new WKTWriter().write(data.geometry)
        } catch (Exception e) {
            log.error("Cannot simplify:" + e)
        }

        return executeCommand(new EditCommand(user: currentUser),annotation,jsonNewData)
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(RoiAnnotation domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.checkIsSameUserOrAdminContainer(domain,domain.user,currentUser)
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }

    def getStringParamsI18n(def domain) {
        return [domain.user.toString(), domain.image?.baseImage?.originalFilename]
    }

    def afterAdd(def domain, def response) {
        response.data['annotation'] = response.data.roiannotation

    }

    def afterDelete(def domain, def response) {
        response.data['annotation'] = response.data.roiannotation
    }

    def afterUpdate(def domain, def response) {
        response.data['annotation'] = response.data.roiannotation
    }

}
