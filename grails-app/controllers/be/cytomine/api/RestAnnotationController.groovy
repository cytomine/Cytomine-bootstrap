package be.cytomine.api

import grails.converters.*
import be.cytomine.ontology.Annotation
import be.cytomine.security.User
import be.cytomine.command.annotation.AddAnnotationCommand
import be.cytomine.command.annotation.DeleteAnnotationCommand
import be.cytomine.command.annotation.EditAnnotationCommand
import be.cytomine.project.Project
import com.vividsolutions.jts.io.WKTReader
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier
import com.vividsolutions.jts.io.WKTWriter
import be.cytomine.image.ImageInstance
import be.cytomine.command.TransactionController
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.command.annotationterm.DeleteAnnotationTermCommand
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.perf4j.LoggingStopWatch
import org.perf4j.StopWatch
import be.cytomine.image.server.RetrievalServer
import be.cytomine.ontology.Term
import be.cytomine.ontology.SuggestedTerm
import be.cytomine.command.suggestedTerm.DeleteSuggestedTermCommand
import be.cytomine.command.annotationterm.AddAnnotationTermCommand

class RestAnnotationController extends RestController {

    def springSecurityService
    def exportService

    def list = {
        responseSuccess(Annotation.list())
    }

    def listByImage = {
        ImageInstance image = ImageInstance.read(params.id)
        if (image) responseSuccess(Annotation.findAllByImage(image))
        else responseNotFound("Image", params.id)
    }

    def listByUser = {
        User user = User.read(params.id)
        if (user) responseSuccess(Annotation.findAllByUser(user))
        else responseNotFound("User", params.id)
    }

    def listByProject = {
        Project project = Project.read(params.id)
        List<User> userList = project.users()

        if (params.users) {
            String[] paramsIdUser = params.users.split("_")
            List<User> userListTemp = new ArrayList<User>()
            userList.each { user ->
                if (Arrays.asList(paramsIdUser).contains(user.id + "")) userListTemp.push(user);
            }
            userList = userListTemp;
        }
        log.info "List by project " + project.id + " with user:" + userList

        if (userList.isEmpty()) responseSuccess([])
        else if (params.noTerm == "true") {

            def terms = Term.findAllByOntology(project.getOntology())
            def annotationsWithTerms = AnnotationTerm.createCriteria().list {
                inList("term", terms)
                join("annotation")
                createAlias("annotation", "a")
                projections {
                    inList("a.image", project.imagesinstance())
                    groupProperty("annotation.id")
                }
            }
            println "annotationsWithTerms ---> " + annotationsWithTerms.size()
            //inList crash is argument is an empty list so we have to use if/else at this time
            def annotations = null
            if (annotationsWithTerms.size() == 0) {
                annotations = Annotation.createCriteria().list {
                    inList("image", project.imagesinstance())
                    inList("user", userList)

                }
            } else {
                annotations = Annotation.createCriteria().list {
                    inList("image", project.imagesinstance())
                    inList("user", userList)
                    not {
                        inList("id", annotationsWithTerms)
                    }
                }
            }

            if (project) responseSuccess(annotations)
            else responseNotFound("Project", params.id)
        } else {
            def annotations = Annotation.findAllByImageInListAndUserInList(project.imagesinstance(), userList)
            if (project) responseSuccess(annotations)
            else responseNotFound("Project", params.id)
        }
    }

    def listByImageAndUser = {
        def image = ImageInstance.read(params.idImage)
        def user = User.read(params.idUser)

        if (image && user) responseSuccess(Annotation.findAllByImageAndUser(image, user))
        else if (!user) responseNotFound("User", params.idUser)
        else if (!image) responseNotFound("Image", params.idImage)
    }

    def downloadDocumentByProject = {
        // Export service provided by Export plugi
        Project project = Project.read(params.id)
        if (project) {
            if (params?.format && params.format != "html") {
                def exporterIdentifier = params.format;
                if (exporterIdentifier == "xls") exporterIdentifier = "excel"
                response.contentType = ConfigurationHolder.config.grails.mime.types[params.format]
                response.setHeader("Content-disposition", "attachment; filename=annotations_project${project.id}.${params.format}")
                def annotations = project.annotations()
                List fields = ["id", "area", "perimeter", "centroid", "image", "filename", "zoomLevel", "user", "created", "updated", "annotationTerm", "URLForCrop", "URLForServerGoTo",]
                Map labels = ["id": "Id", "area": "Area", "perimeter": "Perimeter", "centroid": "Centroid", "image": "Image Id", "filename": "Image Filename", "zoomLevel": "Zoom Level", "user": "User", "created": "Created", "updated": "Last update", "annotationTerm": "Term list", "URLForCrop": "View annotation picture", "URLForServerGoTo": "View annotation on image"]

                // Formatter closure
                def wkt = { domain, value -> return domain.location.toString() }
                def area = { domain, value -> return domain.computeArea()}
                def perim = { domain, value -> return domain.computePerimeter()}
                def centroid = { domain, value -> return domain.getCentroid()}
                def imageId = { domain, value -> return domain.image.id}
                def imageName = { domain, value -> return domain.getFilename()}
                def user = { domain, value -> return domain.user.username}
                def term = { domain, value -> return domain.getTermsname()}
                def crop = { domain, value -> return UrlApi.getAnnotationCropWithAnnotationId(domain.id)}
                def server = { domain, value -> return UrlApi.getAnnotationURL(domain.image.getIdProject(), domain.image.id, domain.id)}

                Map formaters = [area: area, perimeter: perim, centroid: centroid, image: imageId, user: user, annotationTerm: term, URLForCrop: crop, URLForServerGoTo: server, filename: imageName]

                exportService.export(exporterIdentifier, response.outputStream, annotations, fields, labels, formaters, ["csv.encoding": "UTF-8", "separator": ";"])
            }
            log.info "annotationInstanceList"
            [annotationInstanceList: annotations]
        }
        else responseNotFound("Project", params.id)
    }

    def show = {
        Annotation annotation = Annotation.read(params.id)
        if (annotation != null) responseSuccess(annotation)
        else responseNotFound("Annotation", params.id)
    }

    def add = {
        def json = request.JSON
        User currentUser = getCurrentUser(springSecurityService.principal.id)

        //simplify annotation
        try {
            json.location = new WKTWriter().write(simplifyPolygon(json.location))
        } catch (Exception e) {
            log.error("Cannot simplify:" + e)
        }

        //add annotation
        log.info "Start transaction"
        TransactionController transaction = new TransactionController();
        transaction.start()

        def result = processCommand(new AddAnnotationCommand(user: currentUser), json)

        Long id = result?.annotation?.id

        //add annotation-term if term
        if (id) {
            def term = json.term;
            if (term) {
                term.each { idTerm ->
                    def jsonAnnotationTerm = ([user: currentUser.id, annotation: id, term: idTerm]) as JSON
                    def resultTerm = processCommand(new AddAnnotationTermCommand(user: currentUser), jsonAnnotationTerm)
                }
            }
        }

        transaction.stop()

        //add annotation on the retrieval
        try {if (id) indexRetrievalAnnotation(id) } catch (Exception e) { log.error "Cannot index in retrieval:" + e.toString()}

        response(result)
    }

    def update = {
        def json = request.JSON
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        def result = processCommand(new EditAnnotationCommand(user: currentUser), json)

        if (result.success) {
            Long id = result.annotation.id
            try {updateRetrievalAnnotation(id)} catch (Exception e) { log.error "Cannot update in retrieval:" + e.toString()}
        }
        response(result)
    }

    def delete = {
        User currentUser = getCurrentUser(springSecurityService.principal.id)

        def json = ([id: params.id]) as JSON

        //Start transaction
        TransactionController transaction = new TransactionController();
        transaction.start()

        Annotation annotation = Annotation.read(params.id)

        if (annotation) {
            //Delete Annotation-Term before deleting Annotation
            def annotationTerm = AnnotationTerm.findAllByAnnotation(annotation)

            annotationTerm.each { annotterm ->
                log.info "unlink annotterm:" + annotterm.id
                def jsonDataRT = ([term: annotterm.term.id, annotation: annotterm.annotation.id, user: annotterm.user.id]) as JSON
                def result = processCommand(new DeleteAnnotationTermCommand(user: currentUser, printMessage: false), jsonDataRT)
            }

            //Delete Suggested-Term before deleting Annotation
            def suggestTerm = SuggestedTerm.findAllByAnnotation(annotation)
            log.info "suggestTerm= " + suggestTerm.size()
            suggestTerm.each { suggestterm ->
                log.info "unlink suggestterm:" + suggestterm.id
                def jsonDataRT = ([term: suggestterm.term.id, annotation: suggestterm.annotation.id, job: suggestterm.job.id]) as JSON
                def result = processCommand(new DeleteSuggestedTermCommand(user: currentUser, printMessage: false), jsonDataRT)
            }
        }
        //Delete annotation
        log.info "delete annotation"
        def result = processCommand(new DeleteAnnotationCommand(user: currentUser), json)

        //Stop transaction
        transaction.stop()

        //Remove annotation from retrieval
        Long id = result?.annotation?.id
        try {if (id) deleteRetrievalAnnotation(id) } catch (Exception e) { log.error "Cannot delete in retrieval:" + e.toString()}
        response(result)
    }

    private indexRetrievalAnnotation(Long id) {
        //index in retrieval (asynchronous)
        RetrievalServer retrieval = RetrievalServer.findByDescription("retrieval")
        log.info "annotation.id=" + id + " stevben-server=" + retrieval
        if (id && retrieval) {
            log.info "index annotation " + id + " on  " + retrieval.url
            RestRetrievalController.indexAnnotationSynchronous(Annotation.read(id))
        }
    }

    private deleteRetrievalAnnotation(Long id) {
        RetrievalServer retrieval = RetrievalServer.findByDescription("retrieval")
        log.info "annotation.id=" + id + " retrieval-server=" + retrieval
        if (id && retrieval) {
            log.info "delete annotation " + id + " on  " + retrieval.url
            RestRetrievalController.deleteAnnotationSynchronous(id)
        }
    }

    private updateRetrievalAnnotation(Long id) {
        RetrievalServer retrieval = RetrievalServer.findByDescription("retrieval")
        log.info "annotation.id=" + id + " retrieval-server=" + retrieval
        if (id && retrieval) {
            log.info "update annotation " + id + " on  " + retrieval.url
            RestRetrievalController.updateAnnotationSynchronous(id)
        }
    }


    private Geometry simplifyPolygon(String form) {

        Geometry annotationFull = new WKTReader().read(form);
        Geometry lastAnnotationFull = annotationFull
        println "points=" + annotationFull.getNumPoints() + " " + annotationFull.getArea();
        println "annotationFull:" + annotationFull.getNumPoints() + " |" + new WKTWriter().write(annotationFull);
        StopWatch stopWatch = new LoggingStopWatch();
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

        while (numberOfPoint > rateLimitMax && maxLoop > 0) {
            lastAnnotationFull = DouglasPeuckerSimplifier.simplify(annotationFull, i)
            log.debug "annotationFull=" + i + " " + lastAnnotationFull.getNumPoints()
            if (lastAnnotationFull.getNumPoints() < rateLimitMin) break;
            annotationFull = lastAnnotationFull
            i = i + (incrThreshold * increaseIncrThreshold); maxLoop--;
        }

        stopWatch.stop("compress:");
        log.debug "annotationFull good=" + i + " " + annotationFull.getNumPoints() + " |" + new WKTWriter().write(lastAnnotationFull);
        return lastAnnotationFull
    }

}
