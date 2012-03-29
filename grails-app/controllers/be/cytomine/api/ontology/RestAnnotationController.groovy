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

import be.cytomine.security.SecUser
import be.cytomine.social.SharedAnnotation
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import be.cytomine.security.UserJob
import be.cytomine.processing.Job

class RestAnnotationController extends RestController {

    def exportService
    def grailsApplication
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

//    def listByUser = {
    //        User user = userService.read(params.long('id'))
    //        //TODO: Security with postfilter = (very) bad performance!
    //        if (user) responseSuccess(annotationService.list(user))
    //        else responseNotFound("User", params.id)
    //    }

    def listByProject = {
        Project project = projectService.read(params.long('id'), new Project())
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

        log.info "Check user =" + cytomineService.getCurrentUser()

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
        Project project = projectService.read(params.long('idproject'), new Project())

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
        log.info "annotationService.list: " + project + " # " + term + " # " + userList
        if (term == null) responseNotFound("Term", params.idterm)
        else if (project == null) responseNotFound("Project", params.idproject)
        else if (userList.isEmpty()) responseNotFound("Users", params.users)
        else if(!params.suggestTerm) {
            responseSuccess(annotationService.list(project, term, userList))
        }
        else {
            Term suggestedTerm = termService.read(params.suggestTerm)
            responseSuccess(annotationService.list(project, userList, term, suggestedTerm, Job.read(params.long('job'))))
        }
    }

    def downloadDocumentByProject = {  //and filter by users and terms !
        // Export service provided by Export plugin
        log.info params
        Project project = projectService.read(params.long('id'),new Project())
        if (!project) responseNotFound("Project", params.long('id'))
        log.info "check authorization on project " + project
        projectService.checkAuthorization(project)
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
        log.info "termsName="+termsName + " usersName=" +usersName
        if (params?.format && params.format != "html") {
            def exporterIdentifier = params.format;
            if (exporterIdentifier == "xls") exporterIdentifier = "excel"
            response.contentType = grailsApplication.config.grails.mime.types[params.format]
            SimpleDateFormat  simpleFormat = new SimpleDateFormat("yyyyMMdd_hhmmss");
            String datePrefix = simpleFormat.format(new Date())
            response.setHeader("Content-disposition", "attachment; filename=${datePrefix}_annotations_project${project.id}.${params.format}")
            log.info "request header"
            def annotations = Annotation.createCriteria().list {
                eq("project", project)
                inList("user.id", users)
            }
            log.info "annotation request"
            def annotationTerms = AnnotationTerm.createCriteria().list {
                inList("annotation", annotations)
                inList("term.id", terms)
                order("term.id", "asc")
            }
            log.info "annotation term request"
            def exportResult = []
            annotationTerms.each { annotationTerm ->
                Annotation annotation = annotationTerm.annotation
                def centroid = annotation.getCentroid()
                Term term = annotationTerm.term
                def data = [:]
                data.id = annotation.id
                data.area = annotation.computeArea()
                data.perimeter = annotation.computePerimeter()
                if (centroid != null) {
                    data.XCentroid = (int) Math.floor(centroid.x)
                    data.YCentroid = (int) Math.floor(centroid.y)
                } else {
                    data.XCentroid = "undefined"
                    data.YCentroid = "undefined"
                }
                data.image = annotation.image.id
                data.filename = annotation.getFilename()
                data.user = annotation.user.toString()
                data.term = term.name
                data.cropURL =UrlApi.getAnnotationCropWithAnnotationId(grailsApplication.config.grails.serverURL,annotation.id)
                data.cropGOTO = UrlApi.getAnnotationURL(grailsApplication.config.grails.serverURL,annotation.image.getIdProject(), annotation.image.id, annotation.id)
                exportResult.add(data)
            }
            log.info "export result"
            List fields = ["id", "area", "perimeter", "XCentroid", "YCentroid", "image", "filename", "user", "term", "cropURL", "cropGOTO"]
            Map labels = ["id": "Id", "area": "Area (µm²)", "perimeter": "Perimeter (µm)", "XCentroid" : "X", "YCentroid" : "Y", "image": "Image Id", "filename": "Image Filename", "user": "User", "term": "Term", "cropURL": "View annotation picture", "cropGOTO": "View annotation on image"]
            String title = "Annotations in " + project.getName() + " created by " + usersName.join(" or ") + " and associated with " + termsName.join(" or ") + " @ " + (new Date()).toLocaleString()
            log.info "export service"
            exportService.export(exporterIdentifier, response.outputStream, exportResult, fields, labels, null, ["column.widths": [0.04,0.06,0.06,0.04, 0.04, 0.04,0.08,0.06,0.06,0.25,0.25], "title": title, "csv.encoding": "UTF-8", "separator": ";"])
        }
    }


    def addComment = {
        //try {
        User sender = User.read(springSecurityService.principal.id)
        Annotation annotation = Annotation.read(request.JSON.annotation)
        log.info "add comment from " + sender + " and annotation " + annotation
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
        log.info "send mail to " + receiversEmail
        def sharedAnnotation = new SharedAnnotation(
                sender : sender,
                receiver : receivers,
                comment : request.JSON.comment,
                annotation: annotation
        )
        if (sharedAnnotation.save()) {
            mailService.send("cytomine.ulg@gmail.com", receiversEmail, sender.getEmail(), request.JSON.subject, request.JSON.message, [[cid : "annotation", file : annnotationCrop]])
            response([success: true, message: "Annotation shared to " + receivers.toString()], 200)
        } else {
            response([success: false, message: "Error"], 400)
        }
        /* } catch (Exception e) {
            response([success: false, message: e.toString()], 400)
        }*/

    }

    def showComment = {
        Annotation annotation = annotationService.read(params.long('annotation'))
        User user = User.read(springSecurityService.principal.id)
        if (!annotation)  responseNotFound("Annotation", params.annotation)
        annotationService.checkAuthorization(annotation.project)
        def sharedAnnotation = SharedAnnotation.findById(params.long('id'))
        if (!sharedAnnotation) responseNotFound("SharedAnnotation", params.id)
        responseSuccess(sharedAnnotation)
    }

    def listComments = {
        Annotation annotation = annotationService.read(params.long('annotation'))
        User user = User.read(springSecurityService.principal.id)
        if (annotation) {
            annotationService.checkAuthorization(annotation.project)
            def sharedAnnotations = SharedAnnotation.createCriteria().list {
                eq("annotation", annotation)
                or {
                    eq("sender", user)
                    receiver {
                        eq("id", user.id)
                    }
                }
                order("created", "desc")
            }
            responseSuccess(sharedAnnotations.unique())
        } else {
            responseNotFound("Annotation", params.id)
        }
    }

    def show = {
        log.info "Show controller " + params.long('id')
        Annotation annotation = annotationService.read(params.long('id'))
        log.info "Annotation = " + annotation
        if (annotation) {
            annotationService.checkAuthorization(annotation.project)
            responseSuccess(annotation)
        }
        else responseNotFound("Annotation", params.id)
    }

    def add = {
        def json = request.JSON
        try {
            if(!json.project || json.isNull('project')) {
                log.debug "No project was set"
                ImageInstance image = ImageInstance.read(json.image)
                log.debug "Get image = "+image
                log.debug "Get poroject = "+image.project
                if(image) json.project = image.project.id
                log.debug "Get poroject 2 = "+json.project
            }
            log.debug "json.project="+json.project + " (" + json.isNull('project') + ")"
            log.debug "json.location="+json.location + " (" + json.isNull('location') + ")"
            if(json.isNull('project')) throw new WrongArgumentException("Annotation must have a valide project:"+json.project)
            if(json.isNull('location')) {
                log.debug "json location is null!"
                throw new WrongArgumentException("Annotation must have a valide geometry:"+json.location)
            }

            annotationService.checkAuthorization(Long.parseLong(json.project.toString()), new Annotation())
            def result = annotationService.add(json)
            responseResult(result)
        } catch (CytomineException e) {
            log.error("add error:" + e.msg)
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

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
        }
    }


    def delete = {
        def json = JSON.parse("{id : $params.id}")
        delete(annotationService, json)
    }
}
