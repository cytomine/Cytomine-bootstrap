package be.cytomine.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.command.*
import be.cytomine.image.ImageInstance
import be.cytomine.processing.Job
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.UserJob
import be.cytomine.sql.AlgoAnnotationListing
import be.cytomine.sql.AnnotationListing
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import com.vividsolutions.jts.io.WKTWriter

import static org.springframework.security.acls.domain.BasePermission.READ

class AlgoAnnotationService extends ModelService {

    static transactional = true
    def propertyService

    def cytomineService
    def transactionService
    def annotationTermService
    def algoAnnotationTermService
    def simplifyGeometryService
    def dataSource
    def reviewedAnnotationService
    def kmeansGeometryService
    def annotationListingService
    def securityACLService

    def currentDomain() {
        return AlgoAnnotation
    }

    AlgoAnnotation read(def id) {
        def annotation = AlgoAnnotation.read(id)
        if (annotation) {
            securityACLService.check(annotation.container(),READ)
        }
        annotation
    }

    def list(Project project,def propertiesToShow = null) {
        securityACLService.check(project,READ)
        AnnotationListing al = new AlgoAnnotationListing(columnToPrint: propertiesToShow,project : project.id)
        annotationListingService.executeRequest(al)
    }

    def list(Job job,def propertiesToShow = null) {
        securityACLService.check(job.container(),READ)
        List<UserJob> users = UserJob.findAllByJob(job);
        List algoAnnotations = []
        users.each { user ->
            AnnotationListing al = new AlgoAnnotationListing(columnToPrint: propertiesToShow,user : user.id)
            algoAnnotations.addAll(annotationListingService.executeRequest(al))
        }
        return algoAnnotations
    }

    def listIncluded(ImageInstance image, String geometry, SecUser user,  List<Long> terms, AnnotationDomain annotation = null,def propertiesToShow = null) {
        securityACLService.check(image.container(),READ)

        def annotations = []
        AnnotationListing al = new AlgoAnnotationListing(
                columnToPrint: propertiesToShow,
                image : image.id,
                user : user.id,
                suggestedTerms : terms,
                excludedAnnotation : annotation?.id,
                bbox: geometry
        )
        annotations.addAll(annotationListingService.executeRequest(al))
        return annotations
    }



    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    def add(def json, def minPoint = null, def maxPoint = null) {
        securityACLService.check(json.project, Project, READ)
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

        //Add annotation user
        json.user = currentUser.id
        //Add Annotation
        log.debug this.toString()
        Command command = new AddCommand(user: currentUser, transaction: transaction)
        def result = executeCommand(command,null,json)

        return result
    }

    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param jsonNewData New domain datas
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(AlgoAnnotation annotation, def jsonNewData) {
        SecUser currentUser = cytomineService.getCurrentUser()
        securityACLService.checkIsCreator(annotation,currentUser)
        //simplify annotation
        try {
            def data = simplifyGeometryService.simplifyPolygon(jsonNewData.location, annotation?.geometryCompression)
            jsonNewData.location = new WKTWriter().write(data.geometry)
        } catch (Exception e) {
            log.error("Cannot simplify:" + e)
        }

        def result = executeCommand(new EditCommand(user: currentUser),annotation,jsonNewData)

        return result
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(AlgoAnnotation domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        securityACLService.checkIsCreator(domain,currentUser)
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }


    def getStringParamsI18n(def domain) {
        return [domain.user.toString(), domain.image?.baseImage?.filename]
    }

    def afterAdd(def domain, def response) {
        response.data['annotation'] = response.data.algoannotation
        response.data.remove('algoannotation')
    }

    def afterDelete(def domain, def response) {
        response.data['annotation'] = response.data.algoannotation
        response.data.remove('algoannotation')
    }

    def afterUpdate(def domain, def response) {
        response.data['annotation'] = response.data.algoannotation
        response.data.remove('algoannotation')
    }



    def deleteDependentAlgoAnnotationTerm(AlgoAnnotation ao, Transaction transaction, Task task = null) {
        AlgoAnnotationTerm.findAllByAnnotationIdent(ao.id).each {
            algoAnnotationTermService.delete(it,transaction,null,false)
        }
    }

    def deleteDependentReviewedAnnotation(AlgoAnnotation aa, Transaction transaction, Task task = null) {
        ReviewedAnnotation.findAllByParentIdent(aa.id).each {
            reviewedAnnotationService.delete(it,transaction,null,false)
        }
    }

    def deleteDependentProperty(AlgoAnnotation aa, Transaction transaction, Task task = null) {
        Property.findAllByDomainIdent(aa.id).each {
            propertyService.delete(it,transaction,null,false)
        }

    }


}
