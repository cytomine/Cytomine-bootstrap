package be.cytomine.ontology

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.image.ImageInstance
import be.cytomine.image.server.RetrievalServer
import be.cytomine.project.Project
import be.cytomine.security.User
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.io.WKTReader
import com.vividsolutions.jts.io.WKTWriter
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.access.AccessDeniedException
import be.cytomine.test.Infos
import org.codehaus.groovy.grails.commons.ConfigurationHolder

class AnnotationService extends ModelService {

    static transactional = true
    def cytomineService
    def transactionService
    def annotationTermService
    def retrievalService
    def suggestedTermService
    def responseService
    def domainService

    boolean saveOnUndoRedoStack = true

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostFilter("hasPermission(filterObject.project.id,'be.cytomine.project.Project',read) or hasPermission(filterObject.project.id,'be.cytomine.project.Project', admin) or hasRole('ROLE_ADMIN')")
    def list(User user) {
        Annotation.findAllByUser(user)
    }

    @PreAuthorize("hasPermission(#image.project.id,'be.cytomine.project.Project',read) or hasPermission(#image.project.id,'be.cytomine.project.Project',admin) or hasRole('ROLE_ADMIN')")
    def list(ImageInstance image) {
        Annotation.findAllByImage(image)
    }

    @PreAuthorize("hasPermission(#project,read) or hasPermission(#project,admin) or hasRole('ROLE_ADMIN')")
    def list(Project project) {
        Annotation.findAllByProject(project)
    }

    @PreAuthorize("hasPermission(#project ,read) or hasPermission(#project,admin) or hasRole('ROLE_ADMIN')")
    def list(Project project, List<User> userList, boolean noTerm, boolean multipleTerm) {
        if (userList.isEmpty()) return []
        else if (multipleTerm) {
            log.info "multipleTerm"
            def terms = Term.findAllByOntology(project.getOntology())
            def annotationsWithTerms = AnnotationTerm.withCriteria() {
                inList("term", terms)
                join("annotation")
                createAlias("annotation", "a")
                eq("a.project", project)
                projections {
                    groupProperty("annotation")
                    countDistinct("term")
                }
            }
            def annotations = []
            annotationsWithTerms.each {  result ->
                if (result[1] > 1) annotations.add(result[0]) //filter in groovy, to do : I tried greaterThan criteria on alias nbTerms whithout success
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
                projections {
                    eq("a.project", project)
                    groupProperty("annotation.id")
                }
            }
            println "annotationsWithTerms ---> " + annotationsWithTerms.size()
            //inList crash is argument is an empty list so we have to use if/else at this time
            def annotations = null
            if (annotationsWithTerms.size() == 0) {
                annotations = Annotation.createCriteria().list {
                    eq("project", project)
                    inList("user", userList)

                }
            } else {
                annotations = Annotation.createCriteria().list {
                    eq("project", project)
                    inList("user", userList)
                    not {
                        inList("id", annotationsWithTerms)
                    }
                }
            }

            return annotations
        } else {
            log.info "findAllByProjectAndUserInList="+ project + " users="+userList
            return Annotation.findAllByProjectAndUserInList(project, userList)
        }
    }

    @PreAuthorize("hasPermission(#image.project.id,'be.cytomine.project.Project',read) or hasPermission(#image.project.id,'be.cytomine.project.Project',admin) or hasRole('ROLE_ADMIN')")
    def list(ImageInstance image, User user) {
        return Annotation.findAllByImageAndUser(image, user)
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostFilter("hasPermission(filterObject.project.id,'be.cytomine.project.Project',read) or hasPermission(filterObject.project.id,'be.cytomine.project.Project', admin) or hasRole('ROLE_ADMIN')")
    def list(Term term) {
        term.annotations()
    }

    @PreAuthorize("hasPermission(#project ,read) or hasPermission(#project,admin) or hasRole('ROLE_ADMIN')")
    def list(Project project, Term term, List<User> userList) {
        def criteria = Annotation.withCriteria() {
            eq('project',project)
            annotationTerm {
                eq('term',term)
                inList('user',userList)
            }
        }
        criteria.unique()
    }

//    @PreAuthorize("hasPermission(#project ,read) or hasPermission(#project,admin) or hasRole('ROLE_ADMIN')")
//    def list(Project project, Term term, List<User> userList) {
//        def annotationFromTermAndProject = []
//        def annotationFromTerm = term.annotations()
//        annotationFromTerm.each { annotation ->
//            if (annotation.project() != null && annotation.project().id == project.id && userList.contains(annotation.user))
//                annotationFromTermAndProject << annotation
//        }
//        annotationFromTermAndProject
//    }

    Annotation get(def id) {
        Annotation.get(id)
    }

    Annotation read(def id) {
        Annotation.read(id)
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    def add(def json) {

        User currentUser = cytomineService.getCurrentUser()
        //simplify annotation
        try {
            def data = simplifyPolygon(json.location)
            json.location = new WKTWriter().write(data.geometry)
            json.geometryCompression = data.rate
        } catch (Exception e) {
            log.error("Cannot simplify:" + e)
        }

        //Start transaction
        transactionService.start()

        //Add annotation user
        json.user = currentUser.id
        //Add Annotation
        log.debug this.toString()
        def result = executeCommand(new AddCommand(user: currentUser), json)
        def annotation = result?.data?.annotation?.id
        log.info "annotation=" + annotation + " json.term=" + json.term
        //Add annotation-term if term
        if (annotation) {
            def term = json.term;
            if (term) {
                term.each { idTerm ->
                    annotationTermService.addAnnotationTerm(annotation, idTerm, currentUser.id, currentUser)
                }
            }
        }

        //Stop transaction
        transactionService.stop()

        //add annotation on the retrieval
        try {if (annotation) indexRetrievalAnnotation(annotation) } catch (Exception e) { log.error "Cannot index in retrieval:" + e.toString()}

        return result
    }

    @PreAuthorize("#domain.user.id == principal.id  or hasRole('ROLE_ADMIN')")
    def update(def domain,def json) {
        User currentUser = cytomineService.getCurrentUser()
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
    def delete(def domain,def json) {

        User currentUser = cytomineService.getCurrentUser()

        //Start transaction
        transactionService.start()

        //Delete annotation (+cascade)
        def result = deleteAnnotation(json.id, currentUser)

        //Stop transaction
        transactionService.stop()

        //Remove annotation from retrieval
        Long idAnnotation = result?.annotation?.id
        try {if (idAnnotation) deleteRetrievalAnnotation(idAnnotation) } catch (Exception e) { log.error "Cannot delete in retrieval:" + e.toString()}
        return result
    }


    def deleteAnnotation(def idAnnotation, User currentUser) {
        return deleteAnnotation(idAnnotation, currentUser, true)
    }

    def deleteAnnotation(def idAnnotation, User currentUser, boolean printMessage) {
        log.info "Delete annotation: " + idAnnotation
        Annotation annotation = Annotation.read(idAnnotation)
        if (annotation) {
            //Delete Annotation-Term before deleting Annotation
            annotationTermService.deleteAnnotationTermFromAllUser(annotation, currentUser)

            //Delete Suggested-Term before deleting Annotation
            suggestedTermService.deleteSuggestedTermFromAllUser(annotation, currentUser)
        }
        //Delete annotation
        def json = JSON.parse("{id: $idAnnotation}")
        def result = executeCommand(new DeleteCommand(user: currentUser), json)
        return result
    }


    private indexRetrievalAnnotation(Long id) {
        //index in retrieval (asynchronous)
        RetrievalServer retrieval = RetrievalServer.findByDescription("retrieval")
        log.info "annotation.id=" + id + " stevben-server=" + retrieval
        if (id && retrieval) {
            log.info "index annotation " + id + " on  " + retrieval.url
            retrievalService.indexAnnotationAsynchronous(Annotation.read(id))

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


    private def simplifyPolygon(String form) {

        Geometry annotationFull = new WKTReader().read(form);
        Geometry lastAnnotationFull = annotationFull
        println "points=" + annotationFull.getNumPoints() + " " + annotationFull.getArea();
        println "annotationFull:" + annotationFull.getNumPoints() + " |" + new WKTWriter().write(annotationFull);

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
        double rate=0

        while (numberOfPoint > rateLimitMax && maxLoop > 0) {
            rate = i
            lastAnnotationFull = DouglasPeuckerSimplifier.simplify(annotationFull, rate)
            log.debug "annotationFull=" + rate + " " + lastAnnotationFull.getNumPoints()
            if (lastAnnotationFull.getNumPoints() < rateLimitMin) break;
            annotationFull = lastAnnotationFull
            i = i + (incrThreshold * increaseIncrThreshold); maxLoop--;
        }

        log.debug "annotationFull good=" + i + " " + annotationFull.getNumPoints() + " |" + new WKTWriter().write(lastAnnotationFull);
        return [geometry : lastAnnotationFull, rate : rate]
    }

    private def simplifyPolygon(String form, double rate) {
        Geometry annotation = new WKTReader().read(form);
        annotation = DouglasPeuckerSimplifier.simplify(annotation, rate)
        return [geometry : annotation, rate : rate]
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
        def response = responseService.createResponseMessage(domain, [domain.user.toString(),  domain.image?.baseImage?.filename], printMessage, "Delete", domain.getCallBack())
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
        def response = responseService.createResponseMessage(domain, [domain.user.toString(),  domain.image?.baseImage?.filename], printMessage, "Edit", domain.getCallBack())
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
