package be.cytomine.ontology

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
import be.cytomine.SecurityCheck
import be.cytomine.api.UrlApi
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.command.Transaction
import be.cytomine.image.ImageInstance
import be.cytomine.image.server.RetrievalServer
import be.cytomine.processing.Job
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.UserJob
import be.cytomine.social.SharedAnnotation
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.io.WKTWriter
import grails.converters.JSON
import groovy.sql.Sql
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.access.prepost.PreAuthorize

class UserAnnotationService extends ModelService {

    static transactional = true
    def cytomineService
    def transactionService
    def annotationTermService
    def retrievalService
    def algoAnnotationTermService
    def responseService
    def domainService
    def securityService
    def simplifyGeometryService
    def dataSource

    boolean saveOnUndoRedoStack = true


    UserAnnotation get(def id) {
        def annotation = UserAnnotation.get(id)
        if (annotation) {
            SecurityCheck.checkReadAuthorization(annotation.project)
        }
        annotation
    }

    UserAnnotation read(def id) {
        def annotation = UserAnnotation.read(id)
        if (annotation) {
            SecurityCheck.checkReadAuthorization(annotation.project)
        }
        annotation
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Project project) {
        UserAnnotation.findAllByProject(project)
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
    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Project project, List<Long> userList, Term realTerm, Term suggestedTerm, Job job) {
        log.info "list with suggestedTerm"
        if (userList.isEmpty()) {
            return []
        }
        //Get last userjob
        SecUser user = UserJob.findByJob(job)

        //Get all annotation from this project with this term
        def criteria = UserAnnotation.withCriteria() {
            eq('project', project)
            annotationTerm {
                eq('term', realTerm)
                inList('user.id', userList)
            }
            projections {
                groupProperty("id")
            }
        }

        def algoAnnotationsTerm = AnnotationTerm.executeQuery("SELECT ua " +
                "FROM AlgoAnnotationTerm aat, UserAnnotation ua " +
                "WHERE aat.userJob = :user " +
                "AND aat.term = :suggestedTerm " +
                "AND aat.annotationIdent = ua.id " +
                "AND aat.annotationIdent IN (:annotations)", [user: user, suggestedTerm: suggestedTerm, annotations: criteria])

        return algoAnnotationsTerm
    }

    /**
     * List annotation created by user
     * Rem1: We use SQL request (instead of using GORM/hibernate), this improve a lot perf.
     * @param project Project annotation filter
     * @param userList Annotation user filter
     * @param imageInstanceList Image filter
     * @param noTerm Only get annotation with no term
     * @param multipleTerm Only get annotation with multiple (diff) term
     * @return Annotation listing (light)
     */
    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def listLight(Project project, List<Long> userList, List<Long> imageInstanceList, boolean noTerm, boolean multipleTerm) {
        if (!userList.isEmpty() && userList.getAt(0) instanceof UserJob) {
            throw new IllegalArgumentException("Method not supported for this type of data!!!")
        } else {
            String request
            if (multipleTerm)
                request = "SELECT a.id as id, a.image_id as image, a.geometry_compression as geometryCompression, a.project_id as project, a.user_id as user,a.count_comments as nbComments,extract(epoch from a.created)*1000 as created, extract(epoch from a.updated)*1000 as updated, a.count_reviewed_annotations as countReviewedAnnotations,at2.term_id as term, at2.id as annotationTerm,at2.user_id as userTerm,a.wkt_location as location  \n" +
                        " FROM user_annotation a, annotation_term at2, annotation_term at3\n" +
                        " WHERE a.project_id = " + project.id + "\n" +
                        " AND a.id = at2.user_annotation_id\n" +
                        " AND a.id = at3.user_annotation_id\n" +
                        " AND at2.id <> at3.id \n" +
                        " AND at2.term_id <> at3.term_id \n" +
                        " AND at2.user_id IN (" + userList.join(",") + ") \n" +
                        (imageInstanceList.size() == project.countImageInstance() ? "" : "AND a.image_id IN(" + imageInstanceList.join(",") + ") \n") +
                        " ORDER BY id desc, term"
            else if (noTerm)
                request = "SELECT a.id as id, a.image_id as image, a.geometry_compression as geometryCompression, a.project_id as project, a.user_id as user,a.count_comments as nbComments,extract(epoch from a.created)*1000 as created, extract(epoch from a.updated)*1000 as updated, a.count_reviewed_annotations as countReviewedAnnotations,null as term, null as annotationTerm,null as userTerm,a.wkt_location as location  \n" +
                        " FROM user_annotation a LEFT JOIN (SELECT * from annotation_term x where x.user_id IN (" + userList.join(",") + ")) at ON a.id = at.user_annotation_id \n" +
                        " WHERE a.project_id = " + project.id + "\n" +
                        " AND at.id IS NULL\n" +
                        " AND a.user_id IN (" + userList.join(",") + ") \n" +
                        (imageInstanceList.size() == project.countImageInstance() ? "" : "AND a.image_id IN(" + imageInstanceList.join(",") + ") \n") +
                        " ORDER BY id desc, term"
            else
                request = "SELECT a.id as id, a.image_id as image, a.geometry_compression as geometryCompression, a.project_id as project, a.user_id as user,a.count_comments as nbComments,extract(epoch from a.created)*1000 as created, extract(epoch from a.updated)*1000 as updated, a.count_reviewed_annotations as countReviewedAnnotations,at2.term_id as term, at2.id as annotationTerm,at2.user_id as userTerm,a.wkt_location as location  \n" +
                        " FROM user_annotation a LEFT OUTER JOIN annotation_term at2 ON a.id = at2.user_annotation_id \n" +
                        " WHERE a.project_id = " + project.id + "\n" +
                        " AND a.user_id IN (" + userList.join(",") + ") \n" +
                        (imageInstanceList.size() == project.countImageInstance() ? "" : "AND a.image_id IN(" + imageInstanceList.join(",") + ") \n") +
                        " ORDER BY id desc, term"

            selectUserAnnotationFull(request)
        }
    }

    /**
     * List annotation created by user
     * Rem1: We use SQL request (instead of using GORM/hibernate), this improve a lot perf.
     * @param image Image list
     * @param user Annotation user filter
     * @return Annotation listing (light)
     */
    @PreAuthorize("#image.hasPermission(#image.project,'READ') or hasRole('ROLE_ADMIN')")
    def listLight(ImageInstance image, SecUser user) {
        String request = "SELECT a.id as id, a.image_id as image, a.geometry_compression as geometryCompression, a.project_id as project, a.user_id as user,a.count_comments as nbComments,extract(epoch from a.created)*1000 as created, extract(epoch from a.updated)*1000 as updated, a.count_reviewed_annotations as countReviewedAnnotations,at2.term_id as term, at2.id as annotationTerm,at2.user_id as userTerm,a.wkt_location as location  \n" +
                " FROM user_annotation a LEFT OUTER JOIN annotation_term at2 ON a.id = at2.user_annotation_id\n" +
                " WHERE a.image_id = " + image.id + "\n" +
                " AND a.user_id = " + user.id + "\n" +
                " ORDER BY id desc, term"
        return selectUserAnnotationFull(request)
    }

    /**
     * List annotation created by user
     * Rem1: We use SQL request (instead of using GORM/hibernate), this improve a lot perf.
     * @param image Image list
     * @param user Annotation user filter
     * @param boundingbox WKT restricted Area
     * @param notReviewedOnly Don't get annotation that have been reviewed
     * @return Annotation listing (light)
     */
    @PreAuthorize("#image.hasPermission(#image.project,'READ') or hasRole('ROLE_ADMIN')")
    def listLight(ImageInstance image, SecUser user, Geometry boundingbox, Boolean notReviewedOnly) {
        String request = "SELECT DISTINCT annotation.id, annotation.wkt_location, at.term_id \n" +
                " FROM user_annotation annotation LEFT OUTER JOIN annotation_term at ON annotation.id = at.user_annotation_id\n" +
                " WHERE annotation.image_id = $image.id\n" +
                " AND annotation.user_id= $user.id\n" +
                (notReviewedOnly ? " AND annotation.count_reviewed_annotations = 0\n" : "") +
                " AND ST_Intersects(annotation.location,GeometryFromText('" + boundingbox.toString() + "',0)) \n" +
                " ORDER BY annotation.id desc"
        return selectUserAnnotationLight(request)
    }

    /**
     * List annotation created by user
     * Rem1: We use SQL request (instead of using GORM/hibernate), this improve a lot perf.
     * @param project Project annotation filter
     * @param term Term filter
     * @param userList User filter
     * @param imageInstanceList Image filter
     * @return Annotation listing (light)
     */
    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Project project, Term term, List<Long> userList, List<Long> imageInstanceList) {
        if (!userList.isEmpty() && userList.getAt(0) instanceof UserJob) {
            listForUserJob(project, term, userList, imageInstanceList)
        } else {
            String request = "SELECT a.id as id, a.image_id as image, a.geometry_compression as geometryCompression, a.project_id as project, a.user_id as user,a.count_comments as nbComments,extract(epoch from a.created)*1000 as created, extract(epoch from a.updated)*1000 as updated, a.count_reviewed_annotations as countReviewedAnnotations,at2.term_id as term, at2.id as annotationTerm,at2.user_id as userTerm,a.wkt_location as location  \n" +
                    " FROM user_annotation a, annotation_term at,annotation_term at2,annotation_term at3\n" +
                    " WHERE a.id = at.user_annotation_id \n" +
                    " AND a.project_id = " + project.id + "\n" +
                    " AND at3.term_id = " + term.id + "\n" +
                    " AND a.id = at2.user_annotation_id\n" +
                    " AND a.id = at3.user_annotation_id\n" +
                    " AND at.user_id IN (" + userList.collect {it}.join(",") + ") \n" +
                    " ORDER BY id desc, term"
            selectUserAnnotationFull(request)
        }
    }

//
//    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
//    def listLight(Project project) {
//        String request = "SELECT a.id as id, a.image_id as image, a.geometry_compression as geometryCompression, a.project_id as project, a.user_id as user,a.count_comments as nbComments,extract(epoch from a.created)*1000 as created, extract(epoch from a.updated)*1000 as updated, a.count_reviewed_annotations as countReviewedAnnotations,at2.term_id as term, at2.id as annotationTerm,at2.user_id as userTerm,a.wkt_location as location  \n" +
//                " FROM user_annotation a LEFT OUTER JOIN annotation_term at2 ON a.id = at2.user_annotation_id\n" +
//                " WHERE a.project_id = " + project.id + "\n"+
//                " ORDER BY id desc, term"
//        selectUserAnnotationFull(request)
//    }

    /**
     * List all annotation with a very light strcuture: id, project and crop url
     * Use for retrieval server (suggest term)
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    def listLightForRetrieval() {
        String request = "SELECT a.id as id, a.project_id as project\n" +
                " FROM user_annotation a\n" +
                " WHERE GeometryType(a.location) != 'POINT'\n"
        " ORDER BY id desc"
        selectUserAnnotationLightForRetrieval(request)
    }

    /**
     * List annotation created by user
     * @param image Image filter
     */
    @PreAuthorize("#image.hasPermission(#image.project,'READ') or hasRole('ROLE_ADMIN')")
    def listLight(ImageInstance image) {
        String request = "SELECT a.id as id, a.image_id as image, a.geometry_compression as geometryCompression, a.project_id as project, a.user_id as user,a.count_comments as nbComments,extract(epoch from a.created)*1000 as created, extract(epoch from a.updated)*1000 as updated, a.count_reviewed_annotations as countReviewedAnnotations,at2.term_id as term, at2.id as annotationTerm,at2.user_id as userTerm,a.wkt_location as location  \n" +
                " FROM user_annotation a LEFT OUTER JOIN annotation_term at2 ON a.id = at2.user_annotation_id\n" +
                " WHERE a.image_id = " + image.id + "\n" +
                " ORDER BY id desc, term"
        selectUserAnnotationFull(request)
    }


    private def listForUserJob(Project project, Term term, List<Long> userList, List<Long> imageInstanceList) {
        //TODO: mus be improved!!!!!!!!!!
        if (userList.isEmpty()) return []
        if (imageInstanceList.isEmpty()) return []
        if (imageInstanceList.size() == project.countImages) {
            def criteria = AlgoAnnotationTerm.withCriteria() {
                createAlias("userAnnotation", "a")
                eq('project', project)
                eq('term', term)
                inList('userJob.id', userList)
                projections {
                    groupProperty("userAnnotation")
                    groupProperty("rate")
                    groupProperty("term.id")
                    groupProperty("expectedTerm.id")
                }
                order 'rate', 'desc'
            }
            return criteria
        } else {
            def criteria = AlgoAnnotationTerm.withCriteria() {
                createAlias("userAnnotation", "a")
                eq('project', project)
                eq('term', term)
                inList('userJob.id', userList)
                inList("a.image.id", imageInstanceList)
                projections {
                    groupProperty("userAnnotation")
                    groupProperty("rate")
                    groupProperty("term.id")
                    groupProperty("expectedTerm.id")
                }
                order 'rate', 'desc'
            }
            return criteria
        }
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("hasRole('ROLE_USER')")
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
            def annotationID = result?.data?.annotation?.id
            log.info "userAnnotation=" + annotationID + " json.term=" + json.term
            //Add annotation-term if term
            if (annotationID) {
                def term = json.term;
                if (term) {
                    term.each { idTerm ->
                        annotationTermService.addAnnotationTerm(annotationID, idTerm, null, currentUser.id, currentUser, transaction)
                    }
                }
            }

            //Stop transaction
            transactionService.stop()

            //add annotation on the retrieval
            if (annotationID && UserAnnotation.read(annotationID).location.getNumPoints() >= 3) {
                if (!currentUser.algo()) {
                    try {
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
    }

    /**
     * Update this domain with new data from json
     * @param json JSON with new data
     * @param security Security service object (user for right check)
     * @return Response structure (new domain data, old domain data..)
     */
    @PreAuthorize("#security.checkCurrentUserCreator(principal.id) or hasRole('ROLE_ADMIN')")
    def update(def json, SecurityCheck security) {

        SecUser currentUser = cytomineService.getCurrentUser()
        //simplify annotation
        try {
            def annotation = UserAnnotation.read(json.id)
            def data = simplifyGeometryService.simplifyPolygon(json.location, annotation?.geometryCompression)
            json.location = new WKTWriter().write(data.geometry)
        } catch (Exception e) {
            log.error("Cannot simplify:" + e)
        }

        def result = executeCommand(new EditCommand(user: currentUser), json)

        if (result.success) {
            Long id = result.userannotation.id
            try {updateRetrievalAnnotation(id)} catch (Exception e) { log.error "Cannot update in retrieval:" + e.toString()}
        }
        return result
    }

    /**
     * Delete domain in argument
     * @param json JSON that was passed in request parameter
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("#security.checkCurrentUserCreator(principal.id) or hasRole('ROLE_ADMIN')")
    def delete(def json, SecurityCheck security) {

        SecUser currentUser = cytomineService.getCurrentUser()

        //Start transaction
        Transaction transaction = transactionService.start()

        //Delete annotation (+cascade)
        def result = deleteAnnotation(UserAnnotation.read(json.id), currentUser, true, transaction)

        //Stop transaction
        transactionService.stop()

        //Remove annotation from retrieval
        Long idAnnotation = json.id
        log.info "Remove " + json.id + " from retrieval"
        try {if (idAnnotation) deleteRetrievalAnnotation(idAnnotation) } catch (Exception e) { log.error "Cannot delete in retrieval:" + e.toString()}
        return result
    }

    /**
     * Delete a user annotation and domain that reference it
     * @param annotation Annotation to delete
     * @param currentUser User that delete annotation
     * @param printMessage Flag to tells client to print or not confirmation message
     * @param transaction Transaction that packed command
     * @return Response structure
     */
    def deleteAnnotation(UserAnnotation annotation, SecUser currentUser, boolean printMessage, Transaction transaction) {

        if (annotation) {
            //Delete Annotation-Term before deleting Annotation
            annotationTermService.deleteAnnotationTermFromAllUser(annotation, currentUser, transaction)
            //Delete Shared annotation:
            def sharedAnnotation = SharedAnnotation.findAllByUserAnnotation(annotation)
            sharedAnnotation.each {
                it.delete()
            }
        }
        //Delete annotation
        def json = JSON.parse("{id: $annotation.id}")
        def result = executeCommand(new DeleteCommand(user: currentUser, transaction: transaction), json)
        return result
    }

    /**
     * Add annotation to retrieval server for similar annotation listing and term suggestion
     */
    private indexRetrievalAnnotation(Long id) {
        //index in retrieval (asynchronous)
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

    /**
     * Create new domain in database
     * @param json JSON data for the new domain
     * @param printMessage Flag to specify if confirmation message must be show in client
     * Usefull when we create a lot of data, just print the root command message
     * @return Response structure (status, object data,...)
     */
    def create(JSONObject json, boolean printMessage) {
        create(UserAnnotation.createFromDataWithId(json), printMessage)
    }

    /**
     * Create new domain in database
     * @param domain Domain to store
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def create(UserAnnotation domain, boolean printMessage) {
        //Save new object
        domainService.saveDomain(domain)
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.user.toString(), domain.image?.baseImage?.filename], printMessage, "Add", domain.getCallBack())

        //we store data into annotation instead of userannotation
        response.data['annotation'] = response.data.userannotation
        response.data.remove('userannotation')

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
        destroy(UserAnnotation.get(json.id), printMessage)
    }

    /**
     * Destroy domain from database
     * @param domain Domain to remove
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(UserAnnotation domain, boolean printMessage) {
        //Build response message
        log.info "destroy remove " + domain.id
        def response = responseService.createResponseMessage(domain, [domain.user.toString(), domain.image?.baseImage?.filename], printMessage, "Delete", domain.getCallBack())
        //we store data into annotation instead of userannotation
        response.data['annotation'] = response.data.userannotation
        response.data.remove('userannotation')
        //Delete object
        domainService.deleteDomain(domain)
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
        edit(fillDomainWithData(new UserAnnotation(), json), printMessage)
    }

    /**
     * Edit domain from database
     * @param domain Domain to update
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def edit(UserAnnotation domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.user.toString(), domain.image?.baseImage?.filename], printMessage, "Edit", domain.getCallBack())
        //we store data into annotation instead of userannotation
        response.data['annotation'] = response.data.userannotation
        response.data.remove('userannotation')
        //Save update
        domainService.saveDomain(domain)
        return response
    }

    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    UserAnnotation createFromJSON(def json) {
        return UserAnnotation.createFromData(json)
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     */
    def retrieve(JSONObject json) {
        UserAnnotation annotation = UserAnnotation.get(json.id)
        if (!annotation) throw new ObjectNotFoundException("UserAnnotation " + json.id + " not found")
        return annotation
    }

    /**
     * Execute request and format result into a list of map
     */
    private def selectUserAnnotationFull(String request) {
        println "REQUEST=" + request
        def data = []
        long lastAnnotationId = -1
        long lastTermId = -1

        def cytomineBaseUrl = grailsApplication.config.grails.serverURL
        new Sql(dataSource).eachRow(request) {
            /**
             * If an annotation has n multiple term, it will be on "n" lines.
             * For the first line for this annotation (it.id!=lastAnnotationId), add the annotation data,
             * For the other lines, we add term data to the last annotation
             */
            if (it.id != lastAnnotationId) {
                data << [
                        'class': 'be.cytomine.ontology.UserAnnotation',
                        id: it.id,
                        image: it.image,
                        geometryCompression: it.geometryCompression,
                        project: it.project,
                        container: it.project,
                        user: it.user,
                        nbComments: it.nbComments,
                        created: it.created,
                        updated: it.updated,
                        reviewed: (it.countReviewedAnnotations > 0),
                        cropURL: UrlApi.getUserAnnotationCropWithAnnotationId(cytomineBaseUrl, it.id),
                        smallCropURL: UrlApi.getUserAnnotationCropWithAnnotationIdWithMaxWithOrHeight(cytomineBaseUrl, it.id, 256),
                        url: UrlApi.getUserAnnotationCropWithAnnotationId(cytomineBaseUrl, it.id),
                        imageURL: UrlApi.getAnnotationURL(cytomineBaseUrl, it.project, it.image, it.id),
                        term: (it.term ? [it.term] : []),
                        userByTerm: (it.term ? [[id: it.annotationTerm, term: it.term, user: [it.userTerm]]] : []),
                        location: it.location
                ]
            } else {
                if (it.term) {
                    data.last().term.add(it.term)
                    data.last().term.unique()
                    if (it.term == lastTermId) {
                        data.last().userByTerm.last().user.add(it.userTerm)
                        data.last().userByTerm.last().user.unique()
                    } else {
                        data.last().userByTerm.add([id: it.annotationTerm, term: it.term, user: [it.userTerm]])
                    }
                }
            }
            lastTermId = it.term
            lastAnnotationId = it.id
        }
        data
    }

    /**
     * Execute request and format result into a list of map
     */
    private def selectUserAnnotationLight(String request) {
        def data = []
        long lastAnnotationId = -1
        new Sql(dataSource).eachRow(request) {

            long idAnnotation = it[0]
            String location = it[1]
            def idTerm = it[2]

            if (idAnnotation != lastAnnotationId) {
                data << [id: idAnnotation, location: location, term: idTerm ? [idTerm] : []]
            } else {
                if (idTerm)
                    data.last().term.add(idTerm)
            }
            lastAnnotationId = idAnnotation
        }
        data
    }

    /**
     * Execute request and format result into a list of map
     */
    private def selectUserAnnotationLightForRetrieval(String request) {
        def data = []
        def cytomineBaseUrl = grailsApplication.config.grails.serverURL
        new Sql(dataSource).eachRow(request) {

            long idAnnotation = it[0]
            long idContainer = it[1]
            def url = UrlApi.getAnnotationMinCropWithAnnotationId(cytomineBaseUrl, idAnnotation)
            data << [id: idAnnotation, container: idContainer, url: url]
        }
        data
    }

}
