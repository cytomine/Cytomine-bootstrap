package be.cytomine.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.utils.ModelService
import be.cytomine.SecurityCheck
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.command.Transaction
import be.cytomine.image.ImageInstance
import be.cytomine.processing.Job
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.UserJob
import be.cytomine.utils.GeometryUtils
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.io.WKTWriter
import grails.converters.JSON
import groovy.sql.Sql
import org.codehaus.groovy.grails.web.json.JSONObject
import org.hibernate.FetchMode
import org.springframework.security.access.prepost.PreAuthorize

class AlgoAnnotationService extends ModelService {

    static transactional = true

    boolean saveOnUndoRedoStack = true

    def cytomineService
    def transactionService
    def annotationTermService
    def retrievalService
    def algoAnnotationTermService
    def responseService
    def modelService
    def simplifyGeometryService
    def dataSource


    AlgoAnnotation get(def id) {
        def annotation = AlgoAnnotation.get(id)
        if (annotation) {
            SecurityCheck.checkReadAuthorization(annotation.project)
        }
        annotation
    }

    AlgoAnnotation read(def id) {
        def annotation = AlgoAnnotation.read(id)
        if (annotation) {
            SecurityCheck.checkReadAuthorization(annotation.project)
        }
        annotation
    }

    @PreAuthorize("#image.hasPermission(#image.project,'READ') or hasRole('ROLE_ADMIN')")
    def list(ImageInstance image) {
        AlgoAnnotation.findAllByImage(image)
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Project project) {
        AlgoAnnotation.findAllByProject(project)
    }

    @PreAuthorize("#job.hasPermission(#job.project,'READ') or hasRole('ROLE_ADMIN')")
    def list(Job job) {
        List<UserJob> user = UserJob.findAllByJob(job);
        List<AlgoAnnotation> algoAnnotations = []
        user.each {
            algoAnnotations.addAll(AlgoAnnotation.findAllByUser(it))
        }
        return algoAnnotations
    }

    @PreAuthorize("#image.hasPermission(#image.project,'READ') or hasRole('ROLE_ADMIN')")
    def list(ImageInstance image, SecUser user) {
        return AlgoAnnotation.findAllByImageAndUser(image, user)
    }

    /**
     * List annotation created by algorithm
     * @param image Image filter
     * @param user User Job that created annotation filter
     * @param bbox Boundary area filter
     * @param notReviewedOnly Flag to get only annotation that are not reviewed
     * @return Algo Annotation list
     */
    @PreAuthorize("#image.hasPermission(#image.project,'READ') or hasRole('ROLE_ADMIN')")
    def list(ImageInstance image, SecUser user, String bbox, Boolean notReviewedOnly) {
        Geometry boundingbox = GeometryUtils.createBoundingBox(bbox)

        //we use SQL request (not hibernate) to speedup time request
        String request

        if (!notReviewedOnly) {
            request = "SELECT annotation.id, annotation.wkt_location, at.term_id \n" +
                    " FROM algo_annotation annotation LEFT OUTER JOIN algo_annotation_term at ON annotation.id = at.annotation_ident\n" +
                    " WHERE annotation.image_id = $image.id\n" +
                    " AND annotation.user_id= $user.id\n" +
                    " AND ST_Intersects(annotation.location,GeometryFromText('" + boundingbox.toString() + "',0)) " +
                    " ORDER BY annotation.id "

        } else {
            //show only annotation that are not reviewed (use in review mode)
            request = "SELECT annotation.id, annotation.wkt_location, at.term_id \n" +
                    " FROM algo_annotation annotation LEFT OUTER JOIN algo_annotation_term at ON annotation.id = at.annotation_ident\n" +
                    " WHERE annotation.image_id = $image.id\n" +
                    " AND annotation.user_id= $user.id\n" +
                    " AND annotation.count_reviewed_annotations = 0 " +
                    " AND ST_Intersects(annotation.location,GeometryFromText('" + boundingbox.toString() + "',0)) " +
                    " ORDER BY annotation.id "
        }

        def sql = new Sql(dataSource)

        def data = []

        /*
        Request result will come like this if an annotation ahs multiple term;
        -annotation A - Term 1
        -annotation B - Term 1
        -annotation B - Term 2
        ...
        So during the sql result loop, we will group term by annotation like this:
        -annotation A - Term 1
        -annotation B - Term 1 & Term 2
        */

        long lastAnnotationId = -1

        sql.eachRow(request) {

            long idAnnotation = it[0]
            String location = it[1]
            def idTerm = it[2]

            if (idAnnotation != lastAnnotationId) {
                //if its a new annotation, create a new data line
                data << [id: idAnnotation, location: location, term: idTerm ? [idTerm] : []]
            } else {
                //annotation id is the same as the previous iteration, so, just add term
                if (idTerm)
                    data.last().term.add(idTerm)
            }
            lastAnnotationId = idAnnotation
        }
        data
    }

    /**
     * List Annotation created by algorithm
     * @param project Annotation project
     * @param userList Annotation user job
     * @param imageInstanceList Annotation image
     * @param noTerm Flag to get only annotation with no term
     * @param multipleTerm Flag to get only annotation with many terms
     * @return Algo annotation list
     */
    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Project project, List<Long> userList, List<Long> imageInstanceList, boolean noTerm, boolean multipleTerm) {
        log.info("project/userList/noTerm/multipleTerm project=$project.id userList=$userList imageInstanceList=${imageInstanceList.size()} noTerm=$noTerm multipleTerm=$multipleTerm")
        if (userList.isEmpty()) {
            return []
        } else if (imageInstanceList.isEmpty()) {
            return []
        } else if (multipleTerm) {
            log.info "multipleTerm"
            //TODO: could be improve with a single SQL Request
            //get all algoannotationterm where annotation id is twice

            def data = []

            def annotationsWithTerms = AlgoAnnotationTerm.withCriteria() {
                eq("project", project)
                inList("userJob.id", userList)
                projections {
                    groupProperty("annotationIdent")
                    groupProperty("annotationClassName")
                    countDistinct("term")
                    countDistinct('created', 'createdSort')
                }
                order('createdSort', 'desc')
            }
            annotationsWithTerms.each {
                String id = it[0]
                String className = it[1]
                Long termNumber = (Long) it[2]

                if (termNumber > 1) {
                    AnnotationDomain annotation = AlgoAnnotationTerm.retrieveAnnotationDomain(id, className)
                    if (imageInstanceList.contains(annotation.image.id)) {
                        data << annotation
                    }
                }
            }
            return data
        }
        else if (noTerm) {
            log.info "noTerm"
            //TODO: could be improve with a single SQL Request
            def annotationsWithTerms = AlgoAnnotationTerm.withCriteria() {
                eq("project", project)
                inList("userJob.id", userList)
                projections {
                    groupProperty("annotationIdent")
                }
            }

            println "annotationsWithTerms=" + annotationsWithTerms

            //annotationsWithTerms = annotationsWithTerms.collect{it[0]}

            //inList crash is argument is an empty list so we have to use if/else at this time
            def annotations = []


            if (annotationsWithTerms.size() == 0) {
                annotations.addAll(UserAnnotation.createCriteria().list {
                    eq("project", project)
                    inList("image.id", imageInstanceList)
                    inList("user.id", userList)
                    order 'created', 'desc'
                })
                annotations.addAll(AlgoAnnotation.createCriteria().list {
                    eq("project", project)
                    inList("image.id", imageInstanceList)
                    inList("user.id", userList)
                    order 'created', 'desc'
                })
            } else {
                annotations.addAll(UserAnnotation.createCriteria().list {
                    eq("project", project)
                    inList("image.id", imageInstanceList)
                    inList("user.id", userList)
                    not {
                        inList("id", annotationsWithTerms)
                    }
                    order 'created', 'desc'
                })
                annotations.addAll(AlgoAnnotation.createCriteria().list {
                    eq("project", project)
                    inList("image.id", imageInstanceList)
                    inList("user.id", userList)
                    not {
                        inList("id", annotationsWithTerms)
                    }
                    order 'created', 'desc'
                })
            }

            return annotations
        } else {
            log.info "findAllByProjectAndUserInList=" + project + " users=" + userList
            long start = new Date().time
            def annotations = AlgoAnnotation.createCriteria().list {
                eq("project", project)
                inList("user.id", userList)
                inList("image.id", imageInstanceList)
                fetchMode 'image', FetchMode.JOIN
                fetchMode 'image.baseImage', FetchMode.JOIN
                order 'created', 'desc'
            }
            long end = new Date().time
            log.info "time = " + (end - start) + "ms"
            return annotations
        }
    }

    /**
     * List all annotation created by algo
     * @param project Annotation project
     * @param term Term map with this algo annotation from at least an AlgoAnnotationTerm from the same AlgoAnnotation user
     * @param userList Annotation user job
     * @param imageInstanceList Annotation Imageinstance
     * @return Algo Annotation List
     */
    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def listForUserJob(Project project, Term term, List<Long> userList, List<Long> imageInstanceList) {
        if (userList.isEmpty()) {
            return []
        } else if (imageInstanceList.isEmpty()) {
            return []
        } else if (imageInstanceList.size() == project.countImages) {
            //Get all images
            //TODO:: May be speedup without using hibernate (direct SQL request)
            List annotationsUsers = AlgoAnnotation.executeQuery(
                    "SELECT a, aat.rate, aat.term.id,aat.expectedTerm.id FROM UserAnnotation a, AlgoAnnotationTerm aat WHERE aat.project = :project AND aat.term = :term AND aat.userJob.id IN (:userList) AND aat.annotationIdent=a.id ORDER BY aat.rate desc", [project: project, term: term, userList: userList])

            List annotationsAlgo = AlgoAnnotation.executeQuery(
                    "SELECT a, aat.rate, aat.term.id,aat.expectedTerm.id FROM AlgoAnnotation a, AlgoAnnotationTerm aat WHERE aat.project = :project AND aat.term = :term AND aat.userJob.id IN (:userList) AND aat.annotationIdent=a.id ORDER BY aat.rate desc", [project: project, term: term, userList: userList])

            annotationsUsers.addAll(annotationsAlgo)
            return annotationsUsers
        } else {

            List annotationsUsers = AlgoAnnotation.executeQuery(
                    "SELECT a, aat.rate, aat.term.id,aat.expectedTerm.id FROM UserAnnotation a, AlgoAnnotationTerm aat WHERE aat.project = :project AND aat.term = :term AND aat.userJob.id IN (:userList) AND a.image.id IN (:imageInstanceList) AND aat.annotationIdent=a.id ORDER BY aat.rate desc", [project: project, term: term, userList: userList, imageInstanceList: imageInstanceList])

            List annotationsAlgo = AlgoAnnotation.executeQuery(
                    "SELECT a, aat.rate, aat.term.id,aat.expectedTerm.id FROM AlgoAnnotation a, AlgoAnnotationTerm aat WHERE aat.project = :project AND aat.term = :term AND aat.userJob.id IN (:userList) AND a.image.id IN (:imageInstanceList) AND aat.annotationIdent=a.id ORDER BY aat.rate desc", [project: project, term: term, userList: userList, imageInstanceList: imageInstanceList])

            annotationsUsers.addAll(annotationsAlgo)
            return annotationsUsers
        }
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("#security.checkProjectAccess(#json['project']) or hasRole('ROLE_ADMIN')")
    def add(def json, SecurityCheck security) {

        SecUser currentUser = cytomineService.getCurrentUser()

        //simplify annotation
        try {
            def data = simplifyGeometryService.simplifyPolygon(json.location)
            json.location = new WKTWriter().write(data.geometry)
            json.geometryCompression = data.rate
        } catch (Exception e) {
            log.error("Cannot simplify:" + e)
        }

        //Start transaction
        Transaction transaction = transactionService.start()

        //Synchronzed this part of code, prevent two annotation to be add at the same time
        synchronized (this.getClass()) {
            //Add annotation user
            json.user = currentUser.id
            //Add Annotation
            log.debug this.toString()
            def result = executeCommand(new AddCommand(user: currentUser, transaction: transaction), json)
            //Stop transaction
            transactionService.stop()

            return result
        }
    }

    /**
     * Update this domain with new data from json
     * @param json JSON with new data
     * @param security Security service object (user for right check)
     * @return Response structure (new domain data, old domain data..)
     */
    @PreAuthorize("#security.checkCurrentUserCreator(principal.id)  or hasRole('ROLE_ADMIN')")
    def update(def json, SecurityCheck security) {
        SecUser currentUser = cytomineService.getCurrentUser()
        //simplify annotation
        try {
            def annotation = AlgoAnnotation.read(json.id)
            def data = simplifyGeometryService.simplifyPolygon(json.location, annotation?.geometryCompression)
            json.location = new WKTWriter().write(data.geometry)
        } catch (Exception e) {
            log.error("Cannot simplify:" + e)
        }

        def result = executeCommand(new EditCommand(user: currentUser), json)

        return result
    }

    /**
     * Delete domain in argument
     * @param json JSON that was passed in request parameter
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("#security.checkCurrentUserCreator(principal.id)   or hasRole('ROLE_ADMIN')")
    def delete(def json, SecurityCheck security) {

        SecUser currentUser = cytomineService.getCurrentUser()

        //Start transaction
        Transaction transaction = transactionService.start()

        //Delete annotation (+cascade) and print message in client
        def result = deleteAnnotation(AlgoAnnotation.read(json.id), currentUser, true, transaction)

        //Stop transaction
        transactionService.stop()

        //Remove annotation from retrieval
        log.info "Remove " + json.id + " from retrieval"
        return result
    }

    /**
     * Delete algo annotation
     * This method may delete DATA link with this annotation (AlgoAnnotationTerm,...)
     * The printMessage flag will tell client to show/hide feeback message.
     * @param annotation Annotation to delete
     * @param currentUser User that will delete annotation
     * @param printMessage Flat to tell client to print or not message
     * @param transaction Transaction that will packed the delete command
     * @return Command result
     */
    def deleteAnnotation(AnnotationDomain annotation, SecUser currentUser, boolean printMessage, Transaction transaction) {
        if (annotation) {
            //TODO:: delete all domain that reference the deleted domain
        }
        //Delete annotation
        def json = JSON.parse("{id: $annotation.id}")
        def result = executeCommand(new DeleteCommand(user: currentUser, transaction: transaction), json)
        return result
    }

    /**
     * Create new domain in database
     * @param json JSON data for the new domain
     * @param printMessage Flag to specify if confirmation message must be show in client
     * Usefull when we create a lot of data, just print the root command message
     * @return Response structure (status, object data,...)
     */
    def create(JSONObject json, boolean printMessage) {
        create(AlgoAnnotation.createFromDataWithId(json), printMessage)
    }

    /**
     * Create new domain in database
     * @param domain Domain to store
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def create(AlgoAnnotation domain, boolean printMessage) {
        //Save new object
        saveDomain(domain)
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.user.toString(), domain.image?.baseImage?.filename], printMessage, "Add", domain.getCallBack())

        //we store data into annotation instead of algoannotation
        response.data['annotation'] = response.data.algoannotation
        response.data.remove('algoannotation')

        return response
    }

    /**
     * Destroy domain from database
     * @param json JSON with domain data (to retrieve it)
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(AlgoAnnotation.get(json.id), printMessage)
    }

    /**
     * Destroy domain from database
     * @param domain Domain to remove
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(AlgoAnnotation domain, boolean printMessage) {
        //Build response message
        log.info "destroy remove " + domain.id
        def response = responseService.createResponseMessage(domain, [domain.user.toString(), domain.image?.baseImage?.filename], printMessage, "Delete", domain.getCallBack())

        //we store data into annotation instead of algoannotation
        response.data['annotation'] = response.data.algoannotation
        response.data.remove('algoannotation')

        //Delete object
        deleteDomain(domain)
        return response
    }

    /**
     * Edit domain from database
     * @param json domain data in json
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def edit(JSONObject json, boolean printMessage) {
        //Rebuilt previous state of object that was previoulsy edited
        edit(fillDomainWithData(new AlgoAnnotation(), json), printMessage)
    }

    /**
     * Edit domain from database
     * @param domain Domain to update
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def edit(AlgoAnnotation domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.user.toString(), domain.image?.baseImage?.filename], printMessage, "Edit", domain.getCallBack())

        //we store data into annotation instead of algoannotation
        response.data['annotation'] = response.data.algoannotation
        response.data.remove('algoannotation')

        //Save update
        saveDomain(domain)
        return response
    }

    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    AlgoAnnotation createFromJSON(def json) {
        return AlgoAnnotation.createFromData(json)
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     */
    def retrieve(JSONObject json) {
        AlgoAnnotation annotation = AlgoAnnotation.get(json.id)
        if (!annotation) throw new ObjectNotFoundException("AlgoAnnotation " + json.id + " not found")
        return annotation
    }
}
