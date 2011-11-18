package be.cytomine.api

import be.cytomine.ontology.Annotation
import be.cytomine.security.User
import be.cytomine.project.Project
import be.cytomine.image.ImageInstance
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import be.cytomine.ontology.Term
import be.cytomine.Exception.CytomineException

class RestAnnotationController extends RestController {

    def springSecurityService
    def exportService
    def annotationService
    def termService
    def transactionService

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

        if (project) responseSuccess(annotationService.list(project,userList,(params.noTerm == "true")))
        else responseNotFound("Project", params.id)
    }

    def listByImageAndUser = {
        def image = ImageInstance.read(params.idImage)
        def user = User.read(params.idUser)

        if (image && user) responseSuccess(annotationService.list(image,user))
        else if (!user) responseNotFound("User", params.idUser)
        else if (!image) responseNotFound("Image", params.idImage)
    }

    def listAnnotationByTerm = {
        Term term = termService.read(params.idterm)
        if (term) responseSuccess(annotationService.list(term))
        else responseNotFound("Annotation Term", "Term", params.idterm)
    }

    def listAnnotationByProjectAndTerm = {
         Term term = Term.read(params.idterm)
         Project project = Project.read(params.idproject)
         List<User> userList = project.users()

         if (params.users) {
             String[] paramsIdUser = params.users.split("_")
             List<User> userListTemp = new ArrayList<User>()
             userList.each { user ->
                 if (Arrays.asList(paramsIdUser).contains(user.id + "")) userListTemp.push(user);
             }
             userList = userListTemp;
         }
         log.info "List by idTerm " + term.id + " with user:" + userList


         if (term == null) responseNotFound("Term", params.idterm)
         if (project == null) responseNotFound("Project", params.idproject)
         def annotationFromTermAndProject = annotationService.list(project,term, userList)
         responseSuccess(annotationFromTermAndProject)
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
        Annotation annotation = annotationService.read(params.id)
        if (annotation) responseSuccess(annotation)
        else responseNotFound("Annotation", params.id)
    }

    def add = {
        try {
            def result = annotationService.addAnnotation(request.JSON)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.message], e.code)
        } finally {
            transactionService.stopIfTransactionInProgress()
        }
    }

    def update = {
        try {
            def result = annotationService.updateAnnotation(request.JSON)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.message], e.code)
        } finally {
            transactionService.stopIfTransactionInProgress()
        }
    }

    def delete = {
        try {
            def result = annotationService.deleteAnnotation(params.id)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.message], e.code)
        } finally {
            transactionService.stopIfTransactionInProgress()
        }
    }


}
