package be.cytomine.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.ConstraintException
import be.cytomine.Exception.CytomineException
import be.cytomine.SecurityACL
import be.cytomine.api.UrlApi
import be.cytomine.command.*
import be.cytomine.image.ImageInstance
import be.cytomine.image.server.RetrievalServer
import be.cytomine.processing.Job
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.security.UserJob
import be.cytomine.social.SharedAnnotation
import be.cytomine.sql.AnnotationListing
import be.cytomine.sql.UserAnnotationListing
import be.cytomine.utils.JSONUtils
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.io.WKTWriter
import groovy.sql.Sql
import org.hibernate.criterion.Restrictions
import org.hibernatespatial.criterion.SpatialRestrictions

import static org.springframework.security.acls.domain.BasePermission.READ

class UserAnnotationService extends ModelService {

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
    def kmeansGeometryService
    def annotationListingService


    def currentDomain() {
        return UserAnnotation
    }

    UserAnnotation read(def id) {
        def annotation = UserAnnotation.read(id)
        if (annotation) {
            SecurityACL.check(annotation.container(),READ)
        }
        annotation
    }

    def list(Project project,def propertiesToShow = null) {
        SecurityACL.check(project.container(),READ)
        annotationListingService.executeRequest(new UserAnnotationListing(project: project.id, columnToPrint: propertiesToShow))
    }

    def listIncluded(ImageInstance image, String geometry, SecUser user,  List<Long> terms, AnnotationDomain annotation = null,def propertiesToShow = null) {
        SecurityACL.check(image.container(),READ)
        println "x" + AnnotationListing.availableColumnDefault
        AnnotationListing al = new UserAnnotationListing(
                columnToPrint: propertiesToShow,
                image : image.id,
                user : user.id,
                terms : terms,
                excludedAnnotation: annotation?.id,
                bbox: geometry
        )
        annotationListingService.executeRequest(al)
    }

    def count(User user) {
        return UserAnnotation.countByUser(user)
    }

    /**
     * List all annotation with a very light strcuture: id, project and crop url
     * Use for retrieval server (suggest term)
     */
    def listLightForRetrieval() {
        SecurityACL.checkAdmin(cytomineService.currentUser)
        String request = "SELECT a.id as id, a.project_id as project FROM user_annotation a WHERE GeometryType(a.location) != 'POINT' ORDER BY id desc"
        selectUserAnnotationLightForRetrieval(request)
    }

    /**
     * List annotation where a user from 'userList' has added term 'realTerm' and for which a specific job has predicted 'suggestedTerm'
     * @param project Annotation project
     * @param userList Annotation user list filter
     * @param realTerm Annotation term (add by user)
     * @param suggestedTerm Annotation predicted term (from job)
     * @param job Job that make prediction
     * @return
     */
    def list(Project project, List<Long> userList, Term realTerm, Term suggestedTerm, Job job,def propertiesToShow = null) {
        SecurityACL.check(project.container(),READ)
        log.info "list with suggestedTerm"
        if (userList.isEmpty()) {
            return []
        }
        //Get last userjob
        SecUser user = UserJob.findByJob(job)
        AnnotationListing al = new UserAnnotationListing(
                columnToPrint: propertiesToShow,
                project : project.id,
                users : userList,
                term : realTerm.id,
                suggestedTerm: suggestedTerm.id,
                userForTermAlgo: user.id
        )
        annotationListingService.executeRequest(al)
    }


    /**
     * List annotations according to some filters parameters (rem : use list light if you only need the response, not
     * the objects)
     * @param image the image instance
     * @param bbox Geometry restricted Area
     * @param termsIDS filter terms ids
     * @param userIDS filter user ids
     * @return Annotation listing
     */
    def list(ImageInstance image, Geometry bbox, List<Long> termsIDS, List<Long> userIDS) {
        //:to do use listlight and parse WKT instead ?
        Collection<UserAnnotation> annotationsInRoi = []

        annotationsInRoi = UserAnnotation.createCriteria()
                .add(Restrictions.in("user.id", userIDS))
                .add(Restrictions.eq("image.id", image.id))
                .add(SpatialRestrictions.intersects("location",bbox))
                .list()

        Collection<UserAnnotation> annotations = []

        if (!annotationsInRoi.isEmpty()) {
            annotations = (Collection<UserAnnotation>) AnnotationTerm.createCriteria().list {
                inList("term.id", termsIDS)
                join("userAnnotation")
                createAlias("userAnnotation", "a")
                projections {
                    inList("a.id", annotationsInRoi.collect{it.id})
                    groupProperty("userAnnotation")
                }
            }
        }

        return annotations
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    def add(def json,def minPoint = null, def maxPoint = null) {
        log.info "log.addannotation1"

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
        def annotationID
        def result

            //Add annotation user
            json.user = currentUser.id
            //Add Annotation
            log.debug this.toString()
        //def image = ImageInstance.lock(Long.parseLong(json["image"].toString()))
            result = executeCommand(new AddCommand(user: currentUser, transaction: transaction),null,json)

            annotationID = result?.data?.annotation?.id
            log.info "userAnnotation=" + annotationID + " json.term=" + json.term
            //Add annotation-term if term
            if (annotationID) {
                def term = JSONUtils.getJSONList(json.term);
                println "term=$term"
                println "class=${term.class}"
                if (term) {
                    term.each { idTerm ->
                        annotationTermService.addAnnotationTerm(annotationID, idTerm, null, currentUser.id, currentUser, transaction)
                    }
                }
            }


            //add annotation on the retrieval
        log.info "annotationID=$annotationID"
            if (annotationID && UserAnnotation.read(annotationID).location.getNumPoints() >= 3) {
                if (!currentUser.algo()) {
                    try {
                        log.info "log.addannotation2"
                        if (annotationID) indexRetrievalAnnotation(annotationID)
                    } catch (CytomineException ex) {
                        log.error "CytomineException index in retrieval:" + ex.toString()
                    } catch (Exception e) {
                        log.error "Exception index in retrieval:" + e.toString()
                    }
                }
            }

            return result
    }

    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param jsonNewData New domain datas
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(UserAnnotation annotation, def jsonNewData) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.checkIsSameUserOrAdminContainer(annotation,annotation.user,currentUser)
        //simplify annotation
        try {
            def data = simplifyGeometryService.simplifyPolygon(json.location, annotation?.geometryCompression)
            json.location = new WKTWriter().write(data.geometry)
        } catch (Exception e) {
            log.error("Cannot simplify:" + e)
        }

        def result = executeCommand(new EditCommand(user: currentUser),annotation,jsonNewData)

        if (result.success) {
            Long id = result.userannotation.id
            try {
                updateRetrievalAnnotation(id)
            } catch (Exception e) {
                log.error "Cannot update in retrieval:" + e.toString()
            }
        }
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
    def delete(UserAnnotation domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.checkIsSameUserOrAdminContainer(domain,domain.user,currentUser)
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
//        new Sql(dataSource).execute("delete from annotation_term where user_annotation_id=${domain.id}",[])
//        new Sql(dataSource).execute("delete from user_annotation where id=${domain.id}",[])
//        return [status:200,data:[]]
    }

    /**
     * Add annotation to retrieval server for similar annotation listing and term suggestion
     */
    private indexRetrievalAnnotation(Long id) {
        //index in retrieval (asynchronous)
        log.info "log.addannotation3"
        RetrievalServer retrieval = RetrievalServer.findByDescription("retrieval")
        log.info "userAnnotation.id=" + id + " stevben-server=" + retrieval
        if (id && retrieval) {
            log.info "index userAnnotation " + id + " on  " + retrieval.url
            retrievalService.indexAnnotationAsynchronous(UserAnnotation.read(id), RetrievalServer.findByDescription("retrieval"))

        }
    }

    /**
     * Add annotation from retrieval server
     */
    private deleteRetrievalAnnotation(Long id) {
        RetrievalServer retrieval = RetrievalServer.findByDescription("retrieval")
        log.info "userAnnotation.id=" + id + " retrieval-server=" + retrieval
        if (id && retrieval) {
            log.info "delete userAnnotation " + id + " on  " + retrieval.url
            retrievalService.deleteAnnotationAsynchronous(id)
        }
    }

    /**
     *  Update annotation in retrieval server
     */
    private updateRetrievalAnnotation(Long id) {
        RetrievalServer retrieval = RetrievalServer.findByDescription("retrieval")
        log.info "userAnnotation.id=" + id + " retrieval-server=" + retrieval
        if (id && retrieval) {
            log.info "update userAnnotation " + id + " on  " + retrieval.url
            retrievalService.updateAnnotationAsynchronous(id)
        }
    }

    def afterAdd(def domain, def response) {
        response.data['annotation'] = response.data.userannotation
        response.data.remove('userannotation')

    }

    def afterDelete(def domain, def response) {
        response.data['annotation'] = response.data.userannotation
        response.data.remove('userannotation')
    }

    def afterUpdate(def domain, def response) {
        response.data['annotation'] = response.data.userannotation
        response.data.remove('userannotation')
    }

    def getStringParamsI18n(def domain) {
        return [domain.user.toString(), domain.image?.baseImage?.originalFilename]
    }


    /**
     * Execute request and format result into a list of map
     */
    private def selectUserAnnotationLightForRetrieval(String request) {
        def data = []
        new Sql(dataSource).eachRow(request) {

            long idAnnotation = it[0]
            long idContainer = it[1]
            def url = UrlApi.getAnnotationMinCropWithAnnotationId(idAnnotation)
            data << [id: idAnnotation, container: idContainer, url: url]
        }
        data
    }

    def deleteDependentAlgoAnnotationTerm(UserAnnotation ua, Transaction transaction, Task task = null) {
        AlgoAnnotationTerm.findAllByAnnotationIdent(ua.id).each {
            algoAnnotationTermService.delete(it,transaction,null,false)
        }
    }

    def deleteDependentAnnotationTerm(UserAnnotation ua, Transaction transaction, Task task = null) {
        AnnotationTerm.findAllByUserAnnotation(ua).each {
            annotationTermService.delete(it,transaction,null,false)
        }
    }

    def deleteDependentReviewedAnnotation(UserAnnotation ua, Transaction transaction, Task task = null) {
//        ReviewedAnnotation.findAllByParentIdent(ua.id).each {
//            reviewedAnnotationService.delete(it,transaction,null,false)
//        }
     }

    def deleteDependentSharedAnnotation(UserAnnotation ua, Transaction transaction, Task task = null) {
        //TODO: we should implement a full service for sharedannotation and delete them if annotation is deleted
//        if(SharedAnnotation.findByUserAnnotation(ua)) {
//            throw new ConstraintException("There are some comments on this annotation. Cannot delete it!")
//        }

        SharedAnnotation.findAllByUserAnnotation(ua).each {
            annotationTermService.removeDomain(it)
        }

    }

    def deleteDependentProperty(UserAnnotation ua, Transaction transaction, Task task = null) {
        Property.findAllByDomainIdent(ua.id).each {
            propertyService.delete(it,transaction,null,false)
        }

    }

}
