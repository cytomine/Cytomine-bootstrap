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
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.Exception.CytomineException
import be.cytomine.test.Infos
import be.cytomine.security.SecUser
import be.cytomine.social.SharedAnnotation
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

class RestAnnotationController extends RestController {

    def exportService
    def annotationService
    def termService
    def imageInstanceService
    def userService
    def projectService
    def cytomineService
    def mailService

    def list = {
        def annotations = []
        def projects = projectService.list()
        projects.each {
            annotations.addAll(annotationService.list(it))
        }
        responseSuccess(annotations)
    }

    def listByImage = {
        ImageInstance image = imageInstanceService.read(params.long('id'))
        if (image) responseSuccess(annotationService.list(image))
        else responseNotFound("Image", params.id)
    }

    def listByUser = {
        User user = userService.read(params.long('id'))
        //TODO: SECURITY! How to filter?
        if (user) responseSuccess(annotationService.list(user))
        else responseNotFound("User", params.id)
    }

    def listByProject = {
        Project project = projectService.read(params.long('id'))
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
        def image = imageInstanceService.read(params.long('idImage'))
        def user = userService.read(params.idUser)

        if (image && user) responseSuccess(annotationService.list(image, user))
        else if (!user) responseNotFound("User", params.idUser)
        else if (!image) responseNotFound("Image", params.idImage)
    }

    def listAnnotationByTerm = {
        Term term = termService.read(params.long('idterm'))
        if (term) responseSuccess(annotationService.list(term))
        else responseNotFound("Annotation Term", "Term", params.idterm)
    }

    def listAnnotationByProjectAndTerm = {
        Term term = termService.read(params.long('idterm'))
        Project project = projectService.read(params.long('idproject'))
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
        log.info "annotationService.list: " + project + " # " + term + " # " + userList
        def annotationFromTermAndProject = annotationService.list(project, term, userList)
        responseSuccess(annotationFromTermAndProject)
    }

    def downloadDocumentByProject = {  //and filter by users and terms !
        // Export service provided by Export plugin
        Project project = projectService.read(params.long('id'))
        if (!project) responseNotFound("Project", params.long('id'))
        projectService.checkAuthorization(project.id)
        def users = []
        params.users.split(",").each { id ->
            users << Long.parseLong(id)
        }
        def terms = []
        params.terms.split(",").each {  id ->
            terms << Long.parseLong(id)
        }
        def termsName = Term.findAllByIdInList(terms).collect{ it.toString() }
        def usersName = SecUser.findAllByIdInList(users).collect{ it.toString() }
        if (params?.format && params.format != "html") {
            def exporterIdentifier = params.format;
            if (exporterIdentifier == "xls") exporterIdentifier = "excel"
            response.contentType = ConfigurationHolder.config.grails.mime.types[params.format]
            SimpleDateFormat  simpleFormat = new SimpleDateFormat("yyyyMMdd_hhmmss");
            String datePrefix = simpleFormat.format(new Date())
            response.setHeader("Content-disposition", "attachment; filename=${datePrefix}_annotations_project${project.id}.${params.format}")
            def annotations = Annotation.createCriteria().list {
                eq("project", project)
                inList("user.id", users)
            }
            def annotationTerms = AnnotationTerm.createCriteria().list {
                inList("annotation", annotations)
                inList("term.id", terms)
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
            String title = "Annotations in " + project.getName() + " created by " + usersName.join(" or ") + " and associated with " + termsName.join(" or ") + " @ " + (new Date()).toLocaleString()
            exportService.export(exporterIdentifier, response.outputStream, exportResult, fields, labels, null, ["column.widths": [0.04,0.06,0.06,0.04,0.08,0.06,0.06,0.25,0.25], "title": title, "csv.encoding": "UTF-8", "separator": ";"])
        }
    }


    def share = {
        //try {
        User sender = User.read(springSecurityService.principal.id)
        Annotation annotation = Annotation.read(request.JSON.annotation)
        BufferedImage bufferedImage = getImageFromURL(annotation.getCropURL())
        File annnotationCrop = File.createTempFile("temp", ".jpg")
        annnotationCrop.deleteOnExit()
        ImageIO.write(bufferedImage, "JPG", annnotationCrop)

        List<User> receivers = request.JSON.users.collect { userID ->
            User.read(userID)
        }
        String[] receiversEmail = new String[receivers.size()]
        for (int i = 0; i < receivers.size(); i++) {
            receiversEmail[i] = receivers[i].getEmail();
        }
        mailService.send("cytomine.ulg@gmail.com", receiversEmail, sender.getEmail(), request.JSON.subject, request.JSON.message, [[cid : "annotation", file : annnotationCrop]])
        def sharedAnnotation = new SharedAnnotation(
                from : sender,
                to : receivers,
                comment : request.JSON.comment,
                annotation: annotation
        )
        sharedAnnotation.save()
        response([success: true, message: "Annotation shared to " + receivers.toString()], 200)
        /* } catch (Exception e) {
            response([success: false, message: e.toString()], 400)
        }*/

    }

    def show = {
        Annotation annotation = annotationService.read(params.long('id'))
        if (annotation) {
            annotationService.checkAuthorization(annotation.project.id)
            responseSuccess(annotation)
        }
        else responseNotFound("Annotation", params.id)
    }

    def add = {
        def json = request.JSON
        try {
            if(!json.project) {
                ImageInstance image = ImageInstance.read(json.image)
                if(image) json.project = image.project.id
            }
            if(!json.project || !Project.read(json.project)) throw new WrongArgumentException("Annotation must have a valide project:"+json.project)
            log.info "json.project="+json.project
            annotationService.checkAuthorization(Long.parseLong(json.project.toString()))
            def result = annotationService.add(json)
            responseResult(result)
        } catch (CytomineException e) {
            log.error("add error:" + e.msg)
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        } finally {
            transactionService?.stopIfTransactionInProgress()
        }
    }

//    def update = {
//        update(annotationService, request.JSON)
//    }

    def update= {
        def json = request.JSON
        try {
            def domain = annotationService.retrieve(json)
            log.info "CurrentUser = "+cytomineService.getCurrentUser().id
            log.info "Annotation.user.id = "+domain.user.id
            def result = annotationService.update(domain,json)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        } finally {
            transactionService?.stopIfTransactionInProgress()
        }
    }


    def delete = {
        def json = JSON.parse("{id : $params.id}")
        delete(annotationService, json)
    }
}
