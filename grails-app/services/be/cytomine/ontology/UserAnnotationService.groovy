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

    boolean saveOnUndoRedoStack = true

    @PreAuthorize("#image.hasPermission(#image.project,'READ') or hasRole('ROLE_ADMIN')")
    def list(ImageInstance image) {
        UserAnnotation.findAllByImage(image)
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Project project) {
        UserAnnotation.findAllByProject(project)
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def listUserAnnotation(Project project) {
        List<SecUser> users = securityService.getUserList(project)
        return UserAnnotation.findAllByProjectAndUserInList(project,users)
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Project project, Collection<SecUser> userList, Collection<ImageInstance> imageInstanceList, boolean noTerm, boolean multipleTerm) {
         if (!userList.isEmpty() && userList.getAt(0) instanceof UserJob ) {
             throw new IllegalArgumentException("Method not supported for this type of data!!!")
         } else {
             listForUser(project,userList,imageInstanceList,noTerm,multipleTerm)
         }
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def listForUser(Project project, Collection<SecUser> userList, Collection<ImageInstance> imageInstanceList, boolean noTerm, boolean multipleTerm) {
        log.info("project/userList/noTerm/multipleTerm project=$project.id userList=$userList imageInstanceList=${imageInstanceList.size()} noTerm=$noTerm multipleTerm=$multipleTerm")
        if (userList.isEmpty()) return []
        if (imageInstanceList.isEmpty()) return []
        else if (multipleTerm) {
            log.info "multipleTerm"
            def terms = Term.findAllByOntology(project.getOntology())
            def annotationsWithTerms = AnnotationTerm.withCriteria() {
                inList("term", terms)
                join("userAnnotation")
                createAlias("userAnnotation", "a")
                eq("a.project", project)
                inList("a.image", imageInstanceList)
                inList("a.user", userList)
                projections {
                    groupProperty("userAnnotation")
                    countDistinct("term")
                    countDistinct('created', 'createdSort')

                }
                order('createdSort','desc')
            }
            def annotations = []
            annotationsWithTerms.eachWithIndex {  result, index ->
                if (result[1] > 1) annotations.add(result[0])
                //filter in groovy, to do : I tried greaterThan criteria on alias nbTerms whithout success
                //+todo: add  (index>=offset && index<max) in request to improve perf
            }
            annotations
        }
        else if (noTerm) {
            log.info "noTerm"
            def terms = Term.findAllByOntology(project.getOntology())
            def annotationsWithTerms = AnnotationTerm.createCriteria().list {
                inList("term", terms)
                join("userAnnotation")
                createAlias("userAnnotation", "a")
                inList("a.image", imageInstanceList)
                inList("a.user", userList)
                projections {
                    eq("a.project", project)
                    groupProperty("userAnnotation.id")
                }
            }


            //inList crash is argument is an empty list so we have to use if/else at this time
            def annotations = null
            if (annotationsWithTerms.size() == 0) {
                annotations = UserAnnotation.createCriteria().list {
                    eq("project", project)
                    inList("image", imageInstanceList)
                    inList("user", userList)
                    order 'created', 'desc'
//                    firstResult(offset)
//                    maxResults(max)
                }
            } else {
                annotations = UserAnnotation.createCriteria().list {
                    eq("project", project)
                    inList("image", imageInstanceList)
                    inList("user", userList)
                    not {
                        inList("id", annotationsWithTerms)
                    }
                    order 'created', 'desc'
//                    firstResult(offset)
//                    maxResults(max)
                }
            }

            return annotations
        } else {
            log.info "findAllByProjectAndUserInList=" + project + " users=" + userList
            long start = new Date().time
            def annotations = UserAnnotation.createCriteria().list {
                eq("project", project)
                inList("user", userList)
                inList("image", imageInstanceList)
                fetchMode 'image', FetchMode.JOIN
                fetchMode 'image.baseImage', FetchMode.JOIN
                order 'created', 'desc'
//                firstResult(offset)
//                maxResults(max)
            }
            long end = new Date().time
            log.info "time = " + (end - start) + "ms"
            return annotations
        }
    }



    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Project project, List<SecUser> userList, Term realTerm, Term suggestedTerm, Job job) {
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
                inList('user', userList)
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

    @PreAuthorize("#image.hasPermission(#image.project,'READ') or hasRole('ROLE_ADMIN')")
    def list(ImageInstance image, SecUser user) {
        return UserAnnotation.findAllByImageAndUser(image, user)
    }

    @PreAuthorize("#image.hasPermission(#image.project,'READ') or hasRole('ROLE_ADMIN')")
    def list(ImageInstance image, SecUser user, String bbox) {
        String[] coordinates = bbox.split(",")
        double bottomX = Double.parseDouble(coordinates[0])
        double bottomY = Double.parseDouble(coordinates[1])
        double topX = Double.parseDouble(coordinates[2])
        double topY = Double.parseDouble(coordinates[3])
        Coordinate[] boundingBoxCoordinates = [new Coordinate(bottomX, bottomY), new Coordinate(bottomX, topY), new Coordinate(topX, topY), new Coordinate(topX, bottomY), new Coordinate(bottomX, bottomY)]
        Geometry boundingbox = new GeometryFactory().createPolygon(new GeometryFactory().createLinearRing(boundingBoxCoordinates), null)
        UserAnnotation.createCriteria()
                .add(Restrictions.eq("user", user))
                .add(Restrictions.eq("image", image))
                .add(SpatialRestrictions.within("location",boundingbox))
                .list()

    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostFilter("filterObject.hasPermission('READ')")
    def list(Term term) {
        term.annotations()
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Project project, Term term, Collection<SecUser> userList, Collection<ImageInstance> imageInstanceList) {
        if (!userList.isEmpty() && userList.getAt(0) instanceof UserJob ) {
            listForUserJob(project, term, userList, imageInstanceList)
        } else {
            listForUser(project, term, userList, imageInstanceList)
        }
    }

    private def listForUser(Project project, Term term, Collection<SecUser> userList, Collection<ImageInstance> imageInstanceList) {
        if (userList.isEmpty()) return []
        if (imageInstanceList.isEmpty()) return []
        if (imageInstanceList.size() == project.countImages) {
            def criteria = UserAnnotation.withCriteria() {
                eq('project', project)
                annotationTerm {
                    eq('term', term)
                    inList('user', userList)
                }
                order 'created', 'desc'
//                firstResult(offset)
//                maxResults(max)
            }
            return criteria.unique()
        } else {
            def criteria = UserAnnotation.withCriteria() {
                eq('project', project)
                inList("image", imageInstanceList)
                annotationTerm {
                    eq('term', term)
                    inList('user', userList)
                }
                order 'created', 'desc'
//                firstResult(offset)
//                maxResults(max)
            }
            return criteria.unique()
        }
    }

    private def listForUserJob(Project project, Term term, Collection<SecUser> userList, Collection<ImageInstance> imageInstanceList) {
        if (userList.isEmpty()) return []
        if (imageInstanceList.isEmpty()) return []
        if (imageInstanceList.size() == project.countImages) {
            def criteria = AlgoAnnotationTerm.withCriteria() {
                createAlias("userAnnotation", "a")
                eq('project', project)
                eq('term', term)
                inList('userJob', userList)
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
                inList('userJob', userList)
                inList("a.image", imageInstanceList)
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

}
