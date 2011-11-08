package be.cytomine.api

import grails.converters.*
import be.cytomine.ontology.Annotation
import be.cytomine.security.User
import be.cytomine.command.Command
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

class RestAnnotationController extends RestController {

    def springSecurityService
    def exportService

    def list = {
        def data = Annotation.list()
        responseSuccess(data)
    }

    def listByImage = {
        log.info "List with id image:"+params.id
        ImageInstance image = ImageInstance.read(params.id)

        if(image!=null) responseSuccess(Annotation.findAllByImage(image))
        else responseNotFound("Image",params.id)
    }

    def listByUser = {
        log.info "List with id user:"+params.id
        User user = User.read(params.id)

        if(user!=null) responseSuccess(Annotation.findAllByUser(user))
        else responseNotFound("User",params.id)
    }

    def listByProject = {
        Project project = Project.read(params.id)
        List<User> userList = project.users()

        if(params.users) {
            String[] paramsIdUser = params.users.split("_")
            List<User> userListTemp = new ArrayList<User>()
            userList.each { user ->
                if(Arrays.asList(paramsIdUser).contains(user.id+"")) userListTemp.push(user);
            }
            userList = userListTemp;
        }
        log.info "List by project " + project.id + " with user:"+ userList

        if(userList.isEmpty()) responseSuccess([])
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
            def annotations = Annotation.createCriteria().list {
                inList("image", project.imagesinstance())
                inList("user",userList)
                not {
                    inList("id", annotationsWithTerms)
                }
            }
            if(project) responseSuccess(annotations)
            else responseNotFound("Project",params.id)
        } else {
            def annotations = Annotation.findAllByImageInListAndUserInList(project.imagesinstance(),userList)
            if(project) responseSuccess(annotations)
            else responseNotFound("Project",params.id)
        }
    }

    def listByImageAndUser = {
        log.info "List with id image:"+params.idImage + " and id user:" + params.idUser
        def image = ImageInstance.read(params.idImage)
        def user = User.read(params.idUser)

        if(image && user) responseSuccess(Annotation.findAllByImageAndUser(image,user))
        else if(!user) responseNotFound("User",params.idUser)
        else if(!image) responseNotFound("Image",params.idImage)
    }

    def downloadDocumentByProject = {
        // Export service provided by Export plugin

        log.info "List with id project:"+params.id +" params.format="+params.format
        Project project = Project.read(params.id)
        if(project)
        {
            if(params?.format && params.format != "html"){
                def exporterIdentifier = params.format;
                if (exporterIdentifier == "xls") exporterIdentifier = "excel"
                response.contentType = ConfigurationHolder.config.grails.mime.types[params.format]
                response.setHeader("Content-disposition", "attachment; filename=annotations_project${project.id}.${params.format}")
                log.info "List annotation size="+ project.annotations().size()



                List fields = ["id","area","perimeter","centroid","image","filename","zoomLevel","user","created","updated","annotationTerm","URLForCrop","URLForServerGoTo",]
                Map labels = [
                        "id": "Id",
                        "area":"Area",
                        "perimeter" : "Perimeter",
                        "centroid" : "Centroid",
                        "image":"Image Id",
                        "filename":"Image Filename",
                        "zoomLevel":"Zoom Level",
                        "user":"User",
                        "created":"Created",
                        "updated":"Last update",
                        "annotationTerm":"Term list",
                        "URLForCrop":"View annotation picture",
                        "URLForServerGoTo":"View annotation on image"]

                /* Formatter closure in previous releases
                def upperCase = { value ->
                    return value.toUpperCase()
                }
                */

                // Formatter closure
                def wkt = { domain, value ->
                    return domain.location.toString()
                }
                def area = { domain, value ->
                    return domain.computeArea()
                }
                def perim = { domain, value ->
                    return domain.computePerimeter()
                }
                def centroid = { domain, value ->
                    return domain.getCentroid()
                }
                def imageId = { domain, value ->
                    return domain.image.id
                }
                def imageName = { domain, value ->
                    return domain.getFilename()
                }

                def user = { domain, value ->
                    return domain.user.username
                }

                def term = { domain, value ->
                    //return domain.termsName()
                    return domain.getTermsname()
                }

                def crop = { domain, value ->
                    return UrlApi.getAnnotationCropWithAnnotationId(domain.id)
                }

                def server = { domain, value ->
                    return UrlApi.getAnnotationURL(domain.image.getIdProject(),domain.image.id,domain.id)
                }
                Map formaters = [area : area,perimeter:perim, centroid:centroid,image:imageId,user:user,annotationTerm:term,URLForCrop:crop,URLForServerGoTo:server,filename:imageName]

                exportService.export(exporterIdentifier, response.outputStream,project.annotations(), fields, labels, formaters, ["csv.encoding":"UTF-8","separator":";"])
            }
            log.info "annotationInstanceList"
            [ annotationInstanceList: project.annotations() ]
        }
        else responseNotFound("Project",params.id)
    }

    def show = {
        log.info "Show with id:" + params.id
        Annotation annotation = Annotation.read(params.id)

        if(annotation!=null) responseSuccess(annotation)
        else responseNotFound("Annotation",params.id)
    }

    def add = {
        log.info "Add"
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        def json = request.JSON
        println "json = " + json
        println "json.location = " + json.location
        try {
            String form = json.location;
            Geometry annotation = simplifyPolygon(form)
            json.location =  new WKTWriter().write(annotation)
        } catch(Exception e) {}
        log.info "User:" + currentUser.username + " transaction:" +  currentUser.transactionInProgress  + " request:" +json.toString()
        Command addAnnotationCommand = new AddAnnotationCommand(postData : json.toString(), user: currentUser)
        def result = processCommand(addAnnotationCommand, currentUser)
        log.info "Index annotation with id=" +result?.annotation?.id
        Long id = result?.annotation?.id

        try {if(id) indexRetrievalAnnotation(id) } catch(Exception e) { log.error "Cannot index in retrieval:"+e.toString()}

        response(result)
    }


    def delete = {
        log.info "Delete"
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        log.info "User:" + currentUser.username +" transaction:" +  currentUser.transactionInProgress  + " params.id=" + params.id
        //TODO: delete annotation-term if annotation is deleted
        def postData = ([id : params.id]) as JSON

        log.info "Start transaction"
        TransactionController transaction = new TransactionController();
        transaction.start()

        Annotation annotation = Annotation.read(params.id)

        if(annotation) {
            def terms = annotation.terms()
            log.debug "annotation.terms.size=" + terms.size()
            terms.each { term ->

                def annotationTerm = AnnotationTerm.findAllByTermAndAnnotation(term,annotation)
                log.info "annotationTerm= " +annotationTerm.size()

                annotationTerm.each{ annotterm ->
                    log.info "unlink annotterm:" +annotterm.id
                    def postDataRT = ([term: annotterm.term.id,annotation: annotterm.annotation.id]) as JSON
                    Command deleteAnnotationTermCommand = new DeleteAnnotationTermCommand(postData :postDataRT.toString() ,user: currentUser,printMessage:false)
                    def result = processCommand(deleteAnnotationTermCommand, currentUser)
                }

                def suggestTerm = SuggestedTerm.findAllByAnnotation(annotation)
                log.info "suggestTerm= " +suggestTerm.size()

                suggestTerm.each{ suggestterm ->
                    log.info "unlink suggestterm:" +suggestterm.id
                    def postDataRT = ([term: suggestterm.term.id,annotation: suggestterm.annotation.id, job:suggestterm.job.id]) as JSON
                    Command deleteSuggestedTermCommand = new DeleteSuggestedTermCommand(postData :postDataRT.toString() ,user: currentUser,printMessage:false)
                    def result = processCommand(deleteSuggestedTermCommand, currentUser)
                }
            }
        }
        log.info "delete annotation"

        Command deleteAnnotationCommand = new DeleteAnnotationCommand(postData : postData.toString(), user: currentUser)
        def result = processCommand(deleteAnnotationCommand, currentUser)
        transaction.stop()
        Long id = result?.annotation?.id
        try {if(id) deleteRetrievalAnnotation(id) } catch(Exception e) { log.error "Cannot delete in retrieval:"+e.toString()}
        response(result)
    }

    private indexRetrievalAnnotation(Long id) {
        //index in retrieval (asynchronous)
        RetrievalServer retrieval = RetrievalServer.findByDescription("retrieval")
        log.info "annotation.id="+id + " stevben-server="+ retrieval
        if(id && retrieval) {
            log.info "index annotation " + id + " on  " +  retrieval.url
            RestRetrievalController.indexAnnotationSynchronous(Annotation.read(id))
        }

    }
    private deleteRetrievalAnnotation(Long id) {
        RetrievalServer retrieval = RetrievalServer.findByDescription("retrieval")
        log.info "annotation.id="+id + " retrieval-server="+ retrieval
        if(id && retrieval) {
            log.info "delete annotation " + id + " on  " +  retrieval.url
            RestRetrievalController.deleteAnnotationSynchronous(id)
        }
    }

    private updateRetrievalAnnotation(Long id) {
        RetrievalServer retrieval = RetrievalServer.findByDescription("retrieval")
        log.info "annotation.id="+id + " retrieval-server="+ retrieval
        if(id && retrieval) {
            log.info "update annotation " + id + " on  " +  retrieval.url
            RestRetrievalController.updateAnnotationSynchronous(id)
        }
    }

    def update = {
        log.info "Update"
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        log.info "User:" + currentUser.username + " request:" + request.JSON.toString()
        Command editAnnotationCommand = new EditAnnotationCommand(postData : request.JSON.toString(), user: currentUser)
        def result = processCommand(editAnnotationCommand, currentUser)
        if(result.success) {
            Long id = result.annotation.id
            try {updateRetrievalAnnotation(id)} catch(Exception e) { log.error "Cannot update in retrieval:"+e.toString()}
        }
        response(result)
    }


    private Geometry simplifyPolygon(String form) {

        Geometry annotationFull = new WKTReader().read(form);
        Geometry lastAnnotationFull = annotationFull
        println "points=" + annotationFull.getNumPoints() + " " + annotationFull.getArea();
        println "annotationFull:"+annotationFull.getNumPoints() + " |" + new WKTWriter().write(annotationFull);
        StopWatch stopWatch = new LoggingStopWatch();
        /* Number of point (ex: 500 points) */
        double numberOfPoint = annotationFull.getNumPoints()
        /* Maximum number of point that we would have (500/5 (max 150)=max 100 points)*/
        double rateLimitMax = Math.min(numberOfPoint/7.5d,150)
        /* Minimum number of point that we would have (500/10 (min 10 max 100)=min 50 points)*/
        double rateLimitMin = Math.min(Math.max(numberOfPoint/10d,10),100)
        /* Increase value for the increment (allow to converge faster) */
        float incrThreshold = 0.25f
        double increaseIncrThreshold = numberOfPoint/100d
        float i = 0;
        /* Max number of loop (prevent infinite loop) */
        int maxLoop = 500

        while(numberOfPoint>rateLimitMax && maxLoop>0)
        {
            lastAnnotationFull = DouglasPeuckerSimplifier.simplify(annotationFull,i)
            println "annotationFull=" + i + " "+lastAnnotationFull.getNumPoints()
            if(lastAnnotationFull.getNumPoints()<rateLimitMin) break;
            annotationFull = lastAnnotationFull
            i=i+(incrThreshold*increaseIncrThreshold); maxLoop--;
        }

        stopWatch.stop("compress:");
        println "annotationFull good=" + i + " "+annotationFull.getNumPoints() + " |" + new WKTWriter().write(lastAnnotationFull);
        return lastAnnotationFull
    }

}
