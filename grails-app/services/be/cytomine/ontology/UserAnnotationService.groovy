package be.cytomine.ontology

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
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
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.Polygon
import com.vividsolutions.jts.io.WKTReader
import com.vividsolutions.jts.io.WKTWriter
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import org.hibernate.FetchMode
import org.hibernate.criterion.Restrictions
import org.hibernatespatial.criterion.SpatialRestrictions
import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize
import be.cytomine.utils.GeometryUtils
import groovy.sql.Sql
import be.cytomine.api.UrlApi

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

    @PreAuthorize("#image.hasPermission(#image.project,'READ') or hasRole('ROLE_ADMIN')")
    def list(ImageInstance image) {
        UserAnnotation.findAllByImage(image)
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Project project) {
        UserAnnotation.findAllByProject(project)
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostFilter("filterObject.hasPermission('READ')")
    def list(Term term) {
        term.annotations()
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def listUserAnnotation(Project project) {
        List<SecUser> users = securityService.getUserList(project)
        return UserAnnotation.findAllByProjectAndUserInList(project,users)
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Project project, List<Long> userList, Term realTerm, Term suggestedTerm, Job job) {
        // POUR realTerm == null => voir dans la fonction précédente le bloc else if (noTerm) {
        log.info "list with suggestedTerm"
        if (userList.isEmpty()) return []
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
        //def annotations = criteria.unique()

        def algoAnnotationsTerm = AnnotationTerm.executeQuery("SELECT ua " +
                "FROM AlgoAnnotationTerm aat, UserAnnotation ua " +
                "WHERE aat.userJob = :user " +
                "AND aat.term = :suggestedTerm " +
                "AND aat.annotationIdent = ua.id " +
                "AND aat.annotationIdent IN (:annotations)",[user:user,suggestedTerm:suggestedTerm,annotations:criteria])


        return algoAnnotationsTerm
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def listMap(Project project, List<Long> userList, List<Long> imageInstanceList, boolean noTerm, boolean multipleTerm) {
        if (!userList.isEmpty() && userList.getAt(0) instanceof UserJob ) {
            throw new IllegalArgumentException("Method not supported for this type of data!!!")
        } else {
            String request
            if(multipleTerm)
                request = "SELECT a.id as id, a.image_id as image, a.geometry_compression as geometryCompression, a.project_id as project, a.user_id as user,a.count_comments as nbComments,extract(epoch from a.created)*1000 as created, extract(epoch from a.updated)*1000 as updated, a.count_reviewed_annotations as countReviewedAnnotations,at2.term_id as term, at2.id as annotationTerm,at2.user_id as userTerm,a.wkt_location as location  \n" +
                    " FROM user_annotation a, annotation_term at2, annotation_term at3\n" +
                    " WHERE a.project_id = " + project.id + "\n"+
                    " AND a.id = at2.user_annotation_id\n"+
                    " AND a.id = at3.user_annotation_id\n"+
                    " AND at2.id <> at3.id \n"+
                    " AND at2.term_id <> at3.term_id \n"+
                    " AND at2.user_id IN (" + userList.join(",") +") \n" +
                    (imageInstanceList.size()==project.countImageInstance()? "" : "AND a.image_id IN("+imageInstanceList.join(",")+") \n") +
                    " ORDER BY id desc, term"
            else if(noTerm)
                request = "SELECT a.id as id, a.image_id as image, a.geometry_compression as geometryCompression, a.project_id as project, a.user_id as user,a.count_comments as nbComments,extract(epoch from a.created)*1000 as created, extract(epoch from a.updated)*1000 as updated, a.count_reviewed_annotations as countReviewedAnnotations,null as term, null as annotationTerm,null as userTerm,a.wkt_location as location  \n" +
                    " FROM user_annotation a LEFT JOIN (SELECT * from annotation_term x where x.user_id IN ("+userList.join(",")+")) at ON a.id = at.user_annotation_id \n" +
                    " WHERE a.project_id = " + project.id + "\n"+
                    " AND at.id IS NULL\n"+
                    " AND a.user_id IN (" + userList.join(",") +") \n" +
                      (imageInstanceList.size()==project.countImageInstance()? "" : "AND a.image_id IN("+imageInstanceList.join(",")+") \n") +
                    " ORDER BY id desc, term"
            else
                request = "SELECT a.id as id, a.image_id as image, a.geometry_compression as geometryCompression, a.project_id as project, a.user_id as user,a.count_comments as nbComments,extract(epoch from a.created)*1000 as created, extract(epoch from a.updated)*1000 as updated, a.count_reviewed_annotations as countReviewedAnnotations,at2.term_id as term, at2.id as annotationTerm,at2.user_id as userTerm,a.wkt_location as location  \n" +
                    " FROM user_annotation a LEFT OUTER JOIN annotation_term at2 ON a.id = at2.user_annotation_id \n" +
                    " WHERE a.project_id = " + project.id + "\n"+
                    " AND a.user_id IN (" + userList.join(",") +") \n" +
                     (imageInstanceList.size()==project.countImageInstance()? "" : "AND a.image_id IN("+imageInstanceList.join(",")+") \n") +
                    " ORDER BY id desc, term"

            selectUserAnnotationFull(request)
        }
    }

    @PreAuthorize("#image.hasPermission(#image.project,'READ') or hasRole('ROLE_ADMIN')")
    def listMap(ImageInstance image, SecUser user) {
        String request = "SELECT a.id as id, a.image_id as image, a.geometry_compression as geometryCompression, a.project_id as project, a.user_id as user,a.count_comments as nbComments,extract(epoch from a.created)*1000 as created, extract(epoch from a.updated)*1000 as updated, a.count_reviewed_annotations as countReviewedAnnotations,at2.term_id as term, at2.id as annotationTerm,at2.user_id as userTerm,a.wkt_location as location  \n" +
            " FROM user_annotation a LEFT OUTER JOIN annotation_term at2 ON a.id = at2.user_annotation_id\n" +
            " WHERE a.image_id = " + image.id + "\n"+
            " AND a.user_id = " + user.id +"\n" +
            " ORDER BY id desc, term"
        return selectUserAnnotationFull(request)
    }

    @PreAuthorize("#image.hasPermission(#image.project,'READ') or hasRole('ROLE_ADMIN')")
     def listMap(ImageInstance image, SecUser user, Geometry boundingbox, Boolean notReviewedOnly) {
         String request = "SELECT DISTINCT annotation.id, annotation.wkt_location, at.term_id \n" +
                 " FROM user_annotation annotation LEFT OUTER JOIN annotation_term at ON annotation.id = at.user_annotation_id\n" +
                 " WHERE annotation.image_id = $image.id\n" +
                 " AND annotation.user_id= $user.id\n" +
                 (notReviewedOnly? " AND annotation.count_reviewed_annotations = 0\n" :"") +
                 " AND ST_Intersects(annotation.location,GeometryFromText('" + boundingbox.toString() + "',0)) \n" +
                 " ORDER BY annotation.id desc"
         return selectUserAnnotationLight(request)
     }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Project project, Term term, List<Long> userList, List<Long> imageInstanceList) {
        if (!userList.isEmpty() && userList.getAt(0) instanceof UserJob ) {
            listForUserJob(project, term, userList, imageInstanceList)
        } else {
            String request = "SELECT a.id as id, a.image_id as image, a.geometry_compression as geometryCompression, a.project_id as project, a.user_id as user,a.count_comments as nbComments,extract(epoch from a.created)*1000 as created, extract(epoch from a.updated)*1000 as updated, a.count_reviewed_annotations as countReviewedAnnotations,at2.term_id as term, at2.id as annotationTerm,at2.user_id as userTerm,a.wkt_location as location  \n" +
                    " FROM user_annotation a, annotation_term at,annotation_term at2,annotation_term at3\n" +
                    " WHERE a.id = at.user_annotation_id \n" +
                    " AND a.project_id = " + project.id + "\n"+
                    " AND at3.term_id = " + term.id + "\n"+
                    " AND a.id = at2.user_annotation_id\n"+
                    " AND a.id = at3.user_annotation_id\n"+
                    " AND at.user_id IN (" + userList.collect {it}.join(",") +") \n" +
                    " ORDER BY id desc, term"
            selectUserAnnotationFull(request)
        }
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def listMap(Project project) {
        String request = "SELECT a.id as id, a.image_id as image, a.geometry_compression as geometryCompression, a.project_id as project, a.user_id as user,a.count_comments as nbComments,extract(epoch from a.created)*1000 as created, extract(epoch from a.updated)*1000 as updated, a.count_reviewed_annotations as countReviewedAnnotations,at2.term_id as term, at2.id as annotationTerm,at2.user_id as userTerm,a.wkt_location as location  \n" +
                " FROM user_annotation a LEFT OUTER JOIN annotation_term at2 ON a.id = at2.user_annotation_id\n" +
                " WHERE a.project_id = " + project.id + "\n"+
                " ORDER BY id desc, term"
        selectUserAnnotationFull(request)
    }

    @PreAuthorize("#image.hasPermission(#image.project,'READ') or hasRole('ROLE_ADMIN')")
    def listMap(ImageInstance image) {
        String request = "SELECT a.id as id, a.image_id as image, a.geometry_compression as geometryCompression, a.project_id as project, a.user_id as user,a.count_comments as nbComments,extract(epoch from a.created)*1000 as created, extract(epoch from a.updated)*1000 as updated, a.count_reviewed_annotations as countReviewedAnnotations,at2.term_id as term, at2.id as annotationTerm,at2.user_id as userTerm,a.wkt_location as location  \n" +
                " FROM user_annotation a LEFT OUTER JOIN annotation_term at2 ON a.id = at2.user_annotation_id\n" +
                " WHERE a.image_id = " + image.id + "\n"+
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


    UserAnnotation get(def id) {
        UserAnnotation.get(id)
    }

    UserAnnotation read(def id) {
        UserAnnotation.read(id)
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    def add(def json) {

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
                        annotationTermService.addAnnotationTerm(annotationID, idTerm, null,currentUser.id, currentUser, transaction)
                    }
                }
            }


            //Stop transaction
            transactionService.stop()

            //add annotation on the retrieval
            if(annotationID && UserAnnotation.read(annotationID).location.getNumPoints() >= 3)  {
                if(!currentUser.algo())  {
                    try {if (annotationID) indexRetrievalAnnotation(annotationID)
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

    @PreAuthorize("#domain.user.id == principal.id  or hasRole('ROLE_ADMIN')")
    def update(def domain, def json) {
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

    @PreAuthorize("#domain.user.id == principal.id  or hasRole('ROLE_ADMIN')")
    def delete(def domain, def json) {

        SecUser currentUser = cytomineService.getCurrentUser()

        //Start transaction
        Transaction transaction = transactionService.start()

        //Delete annotation (+cascade)
        def result = deleteAnnotation(json.id, currentUser, transaction)

        //Stop transaction
        transactionService.stop()

        //Remove annotation from retrieval
        Long idAnnotation = json.id
        log.info "Remove " + json.id + " from retrieval"
        try {if (idAnnotation) deleteRetrievalAnnotation(idAnnotation) } catch (Exception e) { log.error "Cannot delete in retrieval:" + e.toString()}
        return result
    }


    def deleteAnnotation(Long idAnnotation, SecUser currentUser, Transaction transaction) {
        return deleteAnnotation(idAnnotation, currentUser, true, transaction)
    }

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

    def deleteAnnotation(Long idAnnotation, SecUser currentUser, boolean printMessage, Transaction transaction) {
        log.info "Delete userAnnotation: " + idAnnotation
        UserAnnotation annotation = UserAnnotation.read(idAnnotation)
        return deleteAnnotation(annotation, currentUser, printMessage, transaction)
    }


    private indexRetrievalAnnotation(Long id) {
        //index in retrieval (asynchronous)
        RetrievalServer retrieval = RetrievalServer.findByDescription("retrieval")
        log.info "userAnnotation.id=" + id + " stevben-server=" + retrieval
        if (id && retrieval) {
            log.info "index userAnnotation " + id + " on  " + retrieval.url
            retrievalService.indexAnnotationAsynchronous(UserAnnotation.read(id),RetrievalServer.findByDescription("retrieval"))

        }
    }

    private deleteRetrievalAnnotation(Long id) {
        RetrievalServer retrieval = RetrievalServer.findByDescription("retrieval")
        log.info "userAnnotation.id=" + id + " retrieval-server=" + retrieval
        if (id && retrieval) {
            log.info "delete userAnnotation " + id + " on  " + retrieval.url
            retrievalService.deleteAnnotationAsynchronous(id)
        }
    }

    private updateRetrievalAnnotation(Long id) {
        RetrievalServer retrieval = RetrievalServer.findByDescription("retrieval")
        log.info "userAnnotation.id=" + id + " retrieval-server=" + retrieval
        if (id && retrieval) {
            log.info "update userAnnotation " + id + " on  " + retrieval.url
            retrievalService.updateAnnotationAsynchronous(id)
        }
    }

    /**
     * Restore domain which was previously deleted
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def create(JSONObject json, boolean printMessage) {
        create(UserAnnotation.createFromDataWithId(json), printMessage)
    }

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
     * Destroy domain which was previously added
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(UserAnnotation.get(json.id), printMessage)
    }

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
     * Edit domain which was previously edited
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def edit(JSONObject json, boolean printMessage) {
        //Rebuilt previous state of object that was previoulsy edited
        edit(fillDomainWithData(new UserAnnotation(), json), printMessage)
    }

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


    def selectUserAnnotationFull(String request) {
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
            if(it.id!=lastAnnotationId) {
                data << [
                        'class': 'be.cytomine.ontology.UserAnnotation',
                        id: it.id,
                        image: it.image,
                        geometryCompression: it.geometryCompression,
                        project:it.project,
                        container:it.project,
                        user: it.user,
                        nbComments:it.nbComments,
                        created:it.created,
                        updated:it.updated,
                        reviewed:(it.countReviewedAnnotations>0),
                        cropURL : UrlApi.getUserAnnotationCropWithAnnotationId(cytomineBaseUrl,it.id),
                        smallCropURL : UrlApi.getUserAnnotationCropWithAnnotationIdWithMaxWithOrHeight(cytomineBaseUrl,it.id, 256),
                        url : UrlApi.getUserAnnotationCropWithAnnotationId(cytomineBaseUrl,it.id),
                        imageURL : UrlApi.getAnnotationURL(cytomineBaseUrl,it.project, it.image, it.id),
                        term : (it.term? [it.term]:[]),
                        userByTerm : (it.term? [[id: it.annotationTerm,term:it.term,user: [it.userTerm]]]:[]),
                        location : it.location
                ]
            } else {
                if(it.term) {
                    data.last().term.add(it.term)
                    data.last().term.unique()
                    if(it.term==lastTermId) {
                        data.last().userByTerm.last().user.add(it.userTerm)
                        data.last().userByTerm.last().user.unique()
                    } else {
                        data.last().userByTerm.add([id: it.annotationTerm,term:it.term,user: [it.userTerm]])
                    }
                }
            }
            lastTermId = it.term
            lastAnnotationId = it.id
        }
        data
    }

    def selectUserAnnotationLight(String request) {
         def data = []
        long lastAnnotationId = -1
         new Sql(dataSource).eachRow(request) {

            long idAnnotation = it[0]
            String location = it[1]
            def idTerm = it[2]

            if(idAnnotation!=lastAnnotationId) {
                data << [id: idAnnotation, location: location, term: idTerm? [idTerm]:[]]
            } else {
                if(idTerm)
                    data.last().term.add(idTerm)
            }
            lastAnnotationId = idAnnotation
        }
        data
    }

}
