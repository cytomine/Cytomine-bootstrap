package be.cytomine.ontology

import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize

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
import com.vividsolutions.jts.io.WKTReader
import com.vividsolutions.jts.io.WKTWriter
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import org.hibernate.FetchMode
import org.hibernate.criterion.Restrictions
import org.hibernatespatial.criterion.SpatialRestrictions
import be.cytomine.Exception.CytomineException

class AnnotationService extends ModelService {

    static transactional = true
    def cytomineService
    def transactionService
    def annotationTermService
    def retrievalService
    def algoAnnotationTermService
    def responseService
    def domainService

    boolean saveOnUndoRedoStack = true

    @PreAuthorize("#image.hasPermission(#image.project,'READ') or hasRole('ROLE_ADMIN')")
    def list(ImageInstance image) {
        Annotation.findAllByImage(image)
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Project project) {
        Annotation.findAllByProject(project)
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Project project, Collection<SecUser> userList, Collection<ImageInstance> imageInstanceList, boolean noTerm, boolean multipleTerm) {
         if (!userList.isEmpty() && userList.getAt(0) instanceof UserJob ) {
             listForUserJob(project,userList,imageInstanceList,noTerm,multipleTerm)
         } else {
             listForUser(project,userList,imageInstanceList,noTerm,multipleTerm)
         }
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def listForUser(Project project, Collection<SecUser> userList, Collection<ImageInstance> imageInstanceList, boolean noTerm, boolean multipleTerm) {
        log.info("project/userList/noTerm/multipleTerm project=$project.id userList=$userList imageInstanceList=$imageInstanceList noTerm=$noTerm multipleTerm=$multipleTerm")
        if (userList.isEmpty()) return []
        if (imageInstanceList.isEmpty()) return []
        else if (multipleTerm) {
            log.info "multipleTerm"
            def terms = Term.findAllByOntology(project.getOntology())
            def annotationsWithTerms = AnnotationTerm.withCriteria() {
                inList("term", terms)
                join("annotation")
                createAlias("annotation", "a")
                eq("a.project", project)
                inList("a.image", imageInstanceList)
                inList("a.user", userList)
                projections {
                    groupProperty("annotation")
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
                join("annotation")
                createAlias("annotation", "a")
                inList("a.image", imageInstanceList)
                inList("a.user", userList)
                projections {
                    eq("a.project", project)
                    groupProperty("annotation.id")
                }
            }
            log.info "Found="+annotationsWithTerms.size()
            annotationsWithTerms.each {println it}


            //inList crash is argument is an empty list so we have to use if/else at this time
            def annotations = null
            if (annotationsWithTerms.size() == 0) {
                annotations = Annotation.createCriteria().list {
                    eq("project", project)
                    inList("image", imageInstanceList)
                    inList("user", userList)
                    order 'created', 'desc'
//                    firstResult(offset)
//                    maxResults(max)
                }
            } else {
                annotations = Annotation.createCriteria().list {
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
            def annotations = Annotation.createCriteria().list {
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
            println "time = " + (end - start) + "ms"
            return annotations
        }
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
     def listForUserJob(Project project, Collection<UserJob> userList, Collection<ImageInstance> imageInstanceList, boolean noTerm, boolean multipleTerm) {
         log.info("project/userList/noTerm/multipleTerm for UserJob")
         if (userList.isEmpty()) return []
         if (imageInstanceList.isEmpty()) return []
         else if (multipleTerm) {
             log.info "multipleTerm"
             def terms = Term.findAllByOntology(project.getOntology())
             def annotationsWithTerms = AlgoAnnotationTerm.withCriteria() {
                 inList("term", terms)
                 join("annotation")
                 createAlias("annotation", "a")
                 eq("a.project", project)
                 inList("a.image", imageInstanceList)
                 inList("a.user", userList)
                 projections {
                     groupProperty("annotation")
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
             def annotationsWithTerms = AlgoAnnotationTerm.createCriteria().list {
                 inList("term", terms)
                 join("annotation")
                 createAlias("annotation", "a")
                 inList("a.image", imageInstanceList)
                 inList("a.user", userList)
                 projections {
                     eq("a.project", project)
                     groupProperty("annotation.id")
                 }
             }

             //inList crash is argument is an empty list so we have to use if/else at this time
             def annotations = null
             if (annotationsWithTerms.size() == 0) {
                 annotations = Annotation.createCriteria().list {
                     eq("project", project)
                     inList("image", imageInstanceList)
                     inList("user", userList)
                     order 'created', 'desc'
                 }
             } else {
                 annotations = Annotation.createCriteria().list {
                     eq("project", project)
                     inList("image", imageInstanceList)
                     inList("user", userList)
                     not {
                         inList("id", annotationsWithTerms)
                     }
                     order 'created', 'desc'
                 }
             }

             return annotations
         } else {
             log.info "findAllByProjectAndUserInList=" + project + " users=" + userList
             long start = new Date().time
             def annotations = Annotation.createCriteria().list {
                 eq("project", project)
                 inList("user", userList)
                 inList("image", imageInstanceList)
                 fetchMode 'image', FetchMode.JOIN
                 fetchMode 'image.baseImage', FetchMode.JOIN
                 order 'created', 'desc'
             }
             long end = new Date().time
             println "time = " + (end - start) + "ms"
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
        def criteria = Annotation.withCriteria() {
            eq('project', project)
            annotationTerm {
                eq('term', realTerm)
                inList('user', userList)
            }
        }
        def annotations = criteria.unique()

        //Get all annotation from this project with this suggestedTerm
        def algoAnnotationsTerm = AlgoAnnotationTerm.createCriteria().list {
            eq("userJob", user)
            eq("term", suggestedTerm)
            inList("annotation",annotations)
            join("annotation")
            createAlias("annotation", "a")
            projections {
                groupProperty("annotation")
            }
        }

        return algoAnnotationsTerm

    }

    @PreAuthorize("#image.hasPermission(#image.project,'READ') or hasRole('ROLE_ADMIN')")
    def list(ImageInstance image, SecUser user) {
        return Annotation.findAllByImageAndUser(image, user)
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
        Annotation.createCriteria()
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
            def criteria = Annotation.withCriteria() {
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
            def criteria = Annotation.withCriteria() {
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
                createAlias("annotation", "a")
                eq('project', project)
                eq('term', term)
                inList('userJob', userList)
                projections {
                    groupProperty("annotation")
                }
                //order 'a.created', 'desc'

            }
            return criteria.unique()
        } else {
            def criteria = AlgoAnnotationTerm.withCriteria() {
                createAlias("annotation", "a")
                eq('project', project)
                eq('term', term)
                inList('userJob', userList)
                inList("a.image", imageInstanceList)
                projections {
                    groupProperty("annotation")
                }
               // order 'a.created', 'desc'

            }
            return criteria.unique()
        }
    }

    Annotation get(def id) {
        Annotation.get(id)
    }

    Annotation read(def id) {
        Annotation.read(id)
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    def add(def json) {

        SecUser currentUser = cytomineService.getCurrentUser()

        //simplify annotation
        try {
            def data = simplifyPolygon(json.location)
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
            log.info "annotation=" + annotationID + " json.term=" + json.term
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
            if(Annotation.read(annotationID).location.getNumPoints() >= 3)  {
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
            def annotation = Annotation.read(json.id)
            def data = simplifyPolygon(json.location, annotation?.geometryCompression)
            json.location = new WKTWriter().write(data.geometry)
        } catch (Exception e) {
            log.error("Cannot simplify:" + e)
        }

        def result = executeCommand(new EditCommand(user: currentUser), json)

        if (result.success) {
            Long id = result.annotation.id
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

    def deleteAnnotation(Annotation annotation, SecUser currentUser, boolean printMessage, Transaction transaction) {

        println "*** deleteAnnotation1.vesion=" + annotation.version

        if (annotation) {
            //Delete Annotation-Term before deleting Annotation
            annotationTermService.deleteAnnotationTermFromAllUser(annotation, currentUser, transaction)
            println "*** deleteAnnotation2.vesion=" + annotation.version
            //Delete Suggested-Term before deleting Annotation
            algoAnnotationTermService.deleteAlgoAnnotationTermFromAllUser(annotation, currentUser, transaction)
            println "*** deleteAnnotation3.vesion=" + annotation.version
            //Delete Shared annotation:
            def sharedAnnotation = SharedAnnotation.findAllByAnnotation(annotation)
            sharedAnnotation.each {
                it.delete()
            }
            println "*** deleteAnnotation4.vesion=" + annotation.version
        }
        //Delete annotation
        def json = JSON.parse("{id: $annotation.id}")
        def result = executeCommand(new DeleteCommand(user: currentUser, transaction: transaction), json)
        return result
    }

    def deleteAnnotation(Long idAnnotation, SecUser currentUser, boolean printMessage, Transaction transaction) {
        log.info "Delete annotation: " + idAnnotation
        Annotation annotation = Annotation.read(idAnnotation)
        return deleteAnnotation(annotation, currentUser, printMessage, transaction)
    }


    private indexRetrievalAnnotation(Long id) {
        //index in retrieval (asynchronous)
        RetrievalServer retrieval = RetrievalServer.findByDescription("retrieval")
        log.info "annotation.id=" + id + " stevben-server=" + retrieval
        if (id && retrieval) {
            log.info "index annotation " + id + " on  " + retrieval.url
            retrievalService.indexAnnotationAsynchronous(Annotation.read(id),RetrievalServer.findByDescription("retrieval"))

        }
    }

    private deleteRetrievalAnnotation(Long id) {
        RetrievalServer retrieval = RetrievalServer.findByDescription("retrieval")
        log.info "annotation.id=" + id + " retrieval-server=" + retrieval
        if (id && retrieval) {
            log.info "delete annotation " + id + " on  " + retrieval.url
            retrievalService.deleteAnnotationAsynchronous(id)
        }
    }

    private updateRetrievalAnnotation(Long id) {
        RetrievalServer retrieval = RetrievalServer.findByDescription("retrieval")
        log.info "annotation.id=" + id + " retrieval-server=" + retrieval
        if (id && retrieval) {
            log.info "update annotation " + id + " on  " + retrieval.url
            retrievalService.updateAnnotationAsynchronous(id)
        }
    }

    //experimental
    private def simplifyPolygonPreserving(String geometryString) {
        Geometry geometry = new WKTReader().read(geometryString);
        TopologyPreservingSimplifier tps = new TopologyPreservingSimplifier(geometry)
        return [geometry: tps.getResultGeometry(), rate: 0]

    }

    private def simplifyPolygon(String form) {

        Geometry annotationFull = new WKTReader().read(form);
        Geometry lastAnnotationFull = annotationFull
        log.info "points=" + annotationFull.getNumPoints() + " " + annotationFull.getArea();
        log.info "annotationFull:" + annotationFull.getNumPoints() + " |" + new WKTWriter().write(annotationFull);

        /* Number of point (ex: 500 points) */
        double numberOfPoint = annotationFull.getNumPoints()
        /* Maximum number of point that we would have (500/5 (max 150)=max 100 points)*/
        double rateLimitMax = Math.min(numberOfPoint / 7.5d, 150)
        /* Minimum number of point that we would have (500/10 (min 10 max 100)=min 50 points)*/
        double rateLimitMin = Math.min(Math.max(numberOfPoint / 10d, 10), 100)
        /* Increase value for the increment (allow to converge faster) */
        float incrThreshold = 0.25f
        double increaseIncrThreshold = numberOfPoint / 100d
        float i = 0;
        /* Max number of loop (prevent infinite loop) */
        int maxLoop = 500
        double rate = 0

        while (numberOfPoint > rateLimitMax && maxLoop > 0) {
            rate = i
            lastAnnotationFull = DouglasPeuckerSimplifier.simplify(annotationFull, rate)
            //log.debug "annotationFull=" + rate + " " + lastAnnotationFull.getNumPoints()
            if (lastAnnotationFull.getNumPoints() < rateLimitMin) break;
            annotationFull = lastAnnotationFull
            i = i + (incrThreshold * increaseIncrThreshold); maxLoop--;
        }

        log.debug "annotationFull good=" + i + " " + annotationFull.getNumPoints() + " |" + new WKTWriter().write(lastAnnotationFull);
        return [geometry: lastAnnotationFull, rate: rate]
    }

    private def simplifyPolygon(String form, double rate) {
        Geometry annotation = new WKTReader().read(form);
        annotation = DouglasPeuckerSimplifier.simplify(annotation, rate)
        return [geometry: annotation, rate: rate]
    }

    /**
     * Restore domain which was previously deleted
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def create(JSONObject json, boolean printMessage) {
        create(Annotation.createFromDataWithId(json), printMessage)
    }

    def create(Annotation domain, boolean printMessage) {
        //Save new object
        domainService.saveDomain(domain)
        //Build response message
        return responseService.createResponseMessage(domain, [domain.user.toString(), domain.image?.baseImage?.filename], printMessage, "Add", domain.getCallBack())
    }
    /**
     * Destroy domain which was previously added
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(Annotation.get(json.id), printMessage)
    }

    def destroy(Annotation domain, boolean printMessage) {
        //Build response message
        log.info "destroy remove " + domain.id
        def response = responseService.createResponseMessage(domain, [domain.user.toString(), domain.image?.baseImage?.filename], printMessage, "Delete", domain.getCallBack())
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
        edit(fillDomainWithData(new Annotation(), json), printMessage)
    }

    def edit(Annotation domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.user.toString(), domain.image?.baseImage?.filename], printMessage, "Edit", domain.getCallBack())
        //Save update
        domainService.saveDomain(domain)
        return response
    }

    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    Annotation createFromJSON(def json) {
        return Annotation.createFromData(json)
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     */
    def retrieve(JSONObject json) {
        Annotation annotation = Annotation.get(json.id)
        if (!annotation) throw new ObjectNotFoundException("Annotation " + json.id + " not found")
        return annotation
    }

}
