package be.cytomine.api.ontology

import be.cytomine.api.RestController
import be.cytomine.api.UrlApi
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.Annotation
import be.cytomine.ontology.Term
import be.cytomine.project.Project
import be.cytomine.security.User
import grails.converters.JSON
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import java.text.SimpleDateFormat
import be.cytomine.ontology.AnnotationTerm

class RestAnnotationController extends RestController {

    def exportService
    def annotationService
    def termService
    def imageInstanceService
    def userService
    def projectService

    def list = {
        responseSuccess(annotationService.list())
    }

    def listByImage = {
        ImageInstance image = imageInstanceService.read(params.id)
        if (image) responseSuccess(annotationService.list(image))
        else responseNotFound("Image", params.id)
    }

    def listByUser = {
        User user = userService.read(params.id)
        if (user) responseSuccess(annotationService.list(user))
        else responseNotFound("User", params.id)
    }

    def listByProject = {
        Project project = projectService.read(params.id)
        List<User> userList = userService.list(project)

        if (params.users) {
            String[] paramsIdUser = params.users.split("_")
            List<User> userListTemp = new ArrayList<User>()
            userList.each { user ->
                if (Arrays.asList(paramsIdUser).contains(user.id + "")) userListTemp.push(user);
            }
            userList = userListTemp;
        }
        log.info "List by project " + project.id + " with user:" + userList

        if (project) responseSuccess(annotationService.list(project, userList, (params.noTerm == "true"), (params.multipleTerm == "true")))
        else responseNotFound("Project", params.id)
    }

    def listByImageAndUser = {
        def image = imageInstanceService.read(params.idImage)
        def user = userService.read(params.idUser)

        if (image && user) responseSuccess(annotationService.list(image, user))
        else if (!user) responseNotFound("User", params.idUser)
        else if (!image) responseNotFound("Image", params.idImage)
    }

    def listAnnotationByTerm = {
        Term term = termService.read(params.idterm)
        if (term) responseSuccess(annotationService.list(term))
        else responseNotFound("Annotation Term", "Term", params.idterm)
    }

    def listAnnotationByProjectAndTerm = {
        Term term = termService.read(params.idterm)
        Project project = projectService.read(params.idproject)
        List<User> userList = userService.list(project)

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
        def annotationFromTermAndProject = annotationService.list(project, term, userList)
        responseSuccess(annotationFromTermAndProject)
    }


    def downloadDocumentByProject = {
        // Export service provided by Export plugin
        Project project = projectService.read(params.id)
        if (project) {
            if (params?.format && params.format != "html") {
                def exporterIdentifier = params.format;
                if (exporterIdentifier == "xls") exporterIdentifier = "excel"
                response.contentType = ConfigurationHolder.config.grails.mime.types[params.format]
                SimpleDateFormat  simpleFormat = new SimpleDateFormat("yyyyMMdd_hhmmss");
                String datePrefix = simpleFormat.format(new Date())
                response.setHeader("Content-disposition", "attachment; filename=${datePrefix}_annotations_project${project.id}.${params.format}")
                def annotations = project.annotations()
                def annotationTerms = AnnotationTerm.createCriteria().list {
                    inList("annotation", annotations)
                    order("term.id", "asc")
                }
                def exportResult = []
                annotationTerms.each { annotationTerm ->
                    Annotation annotation = annotationTerm.annotation
                    Term term = annotationTerm.term
                    def data = [:]
                    data.id = annotation.id
                    data.area = annotation.computeArea()
                    data.perimeter = annotation.computePerimeter()
                    data.image = annotation.image.id
                    data.filename = annotation.getFilename()
                    data.user = annotation.user.toString()
                    data.term = term.name
                    data.cropURL =UrlApi.getAnnotationCropWithAnnotationId(annotation.id)
                    data.cropGOTO = UrlApi.getAnnotationURL(annotation.image.getIdProject(), annotation.image.id, annotation.id)
                    exportResult.add(data)
                }
                List fields = ["id", "area", "perimeter", "image", "filename", "user", "term", "cropURL", "cropGOTO"]
                Map labels = ["id": "Id", "area": "Area (µm²)", "perimeter": "Perimeter (µm)", "image": "Image Id", "filename": "Image Filename", "user": "User", "term": "Term", "cropURL": "View annotation picture", "cropGOTO": "View annotation on image"]
                String title = "Annotations in " + project.getName() + " ( " + new Date().toLocaleString() + " ) "
                exportService.export(exporterIdentifier, response.outputStream, exportResult, fields, labels, null, ["column.widths": [0.04,0.06,0.06,0.04,0.08,0.06,0.06,0.25,0.25], "title": title, "csv.encoding": "UTF-8", "separator": ";"])
            }
        }
        else responseNotFound("Project", params.id)
    }

    def show = {
        Annotation annotation = annotationService.read(params.id)
        if (annotation) responseSuccess(annotation)
        else responseNotFound("Annotation", params.id)
    }

    def add = {
        add(annotationService, request.JSON)
    }

    def update = {
        update(annotationService, request.JSON)
    }

    def delete = {
        def json = JSON.parse("{id : $params.id}")
        delete(annotationService, json)
    }
}
